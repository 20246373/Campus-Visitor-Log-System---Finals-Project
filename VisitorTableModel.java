import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

class VisitorTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
      "Visitor Name",
      "Contact #",
      "Presented ID",
      "Purpose",
      "Campus Area",
      "Guard on Duty",
      "Gate Entered",
      "Check-In",
      "Check-Out"
    };

  private final List<Visitor> visitors = new ArrayList<>();

  // References to available guards so we can restore saved rows
  private final List<Guard> guardsRef;

  private static final String SAVE_FOLDER = "src/data/";
  private static final String SEP = "|"; // pipe-separated to avoid commas in text

  @Override
  public int getRowCount() {
    return visitors.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Override
  public String getColumnName(int column) {
    return COLUMNS[column];
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Visitor visitor = visitors.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> visitor.getVisitorName();
      case 1 -> visitor.getContactNumber();
      case 2 -> visitor.getPresentedId();
      case 3 -> visitor.getPurpose();
      case 4 -> visitor.getCampusArea();
      case 5 -> visitor.getGuardLabel();
      case 6 -> visitor.getGateEntered();
      case 7 -> visitor.getCheckInTime();
      case 8 -> visitor.getCheckOutTime();
      default -> "";
    };
  }

  VisitorTableModel(List<Guard> guards, List<Admin> admins) {
    this.guardsRef = guards == null ? List.of() : guards;
    loadFromFile();
  }

  void addVisitor(Visitor visitor) {
    visitors.add(visitor);
    int row = visitors.size() - 1;
    fireTableRowsInserted(row, row);
    saveToFile();
  }

  boolean markCheckout(int rowIndex, LocalDateTime checkoutTime) {
    boolean updated = visitors.get(rowIndex).markCheckout(checkoutTime);
    if (updated) {
      fireTableRowsUpdated(rowIndex, rowIndex);
      saveToFile();
    }
    return updated;
  }

  void removeVisitor(int rowIndex) {
    visitors.remove(rowIndex);
    fireTableRowsDeleted(rowIndex, rowIndex);
    saveToFile();
  }

  void updateVisitor(int rowIndex, VisitorFormData data) {
    if (rowIndex < 0 || rowIndex >= visitors.size()) {
      return;
    }
    visitors.get(rowIndex).updateDetails(data);
    fireTableRowsUpdated(rowIndex, rowIndex);
    saveToFile();
  }

  // Persistence helpers
  private void saveToFile() {
    // Get current date for filename
    String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    File f = new File(SAVE_FOLDER + "visitor_logs_" + dateStr + ".txt");
    
    // Ensure directory exists
    f.getParentFile().mkdirs();
    
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
      for (Visitor v : visitors) {
        String checkIn = v.getCheckIn() == null ? "" : v.getCheckIn().toString();
        String checkOut = v.getCheckOut() == null ? "" : v.getCheckOut().toString();
        String[] parts = new String[] {
            escape(v.getVisitorName()),
            escape(v.getContactNumber()),
            escape(v.getPresentedId()),
            escape(v.getPurpose()),
            escape(v.getCampusArea()),
            escape(v.getGuardLabel()),
            escape(v.getGateEntered()),
            escape(checkIn),
            escape(checkOut)
        };
        bw.write(String.join(SEP, parts));
        bw.newLine();
      }
    } catch (IOException e) {
      System.err.println("Failed to save visitor log: " + e.getMessage());
    }
  }

  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("|", "\\|");
  }

  private static String unescape(String s) {
    if (s == null) return "";
    return s.replace("\\|", "|").replace("\\\\", "\\");
  }

  private void loadFromFile() {
    // Try to load today's file first, then check for most recent file
    String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    File f = new File(SAVE_FOLDER + "visitor_logs_" + dateStr + ".txt");
    
    if (!f.exists()) {
      // Try to find the most recent log file
      File folder = new File(SAVE_FOLDER);
      if (folder.exists() && folder.isDirectory()) {
        File[] files = folder.listFiles((dir, name) -> name.startsWith("visitor_logs_") && name.endsWith(".txt"));
        if (files != null && files.length > 0) {
          // Sort by date (filename) and get the most recent
          java.util.Arrays.sort(files, (a, b) -> b.getName().compareTo(a.getName()));
          f = files[0];
        } else {
          return;
        }
      } else {
        return;
      }
    }
    
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String line;
      while ((line = br.readLine()) != null) {
        try {
          String[] parts = splitLine(line);
          int idx = 0;
          // New format: name, contact, presentedId, purpose, campus, guard, gate, checkIn, checkOut
          String name = unescape(partSafe(parts, idx++));
          String contact = unescape(partSafe(parts, idx++));
          String presentedId = unescape(partSafe(parts, idx++));
          String purpose = unescape(partSafe(parts, idx++));
          String campus = unescape(partSafe(parts, idx++));
          String guardLabel = unescape(partSafe(parts, idx++));
          idx++; // skip gate column (it's derived from guard)
          String checkInRaw = unescape(partSafe(parts, idx++));
          String checkOutRaw = unescape(partSafe(parts, idx));

          Guard guard = findGuardByLabel(guardLabel);

          LocalDateTime checkIn = checkInRaw.isEmpty() ? null : LocalDateTime.parse(checkInRaw);
          LocalDateTime checkOut = checkOutRaw.isEmpty() ? null : LocalDateTime.parse(checkOutRaw);

          Visitor v = Visitor.fromCsvFields(name, contact, presentedId, purpose, campus, guard, checkIn, checkOut);
          visitors.add(v);
        } catch (Exception inner) {
          // skip malformed rows but continue
          System.err.println("Skipping malformed saved visitor row: " + inner.getMessage());
        }
      }
      // ensure view gets new data after load
      if (!visitors.isEmpty()) {
        fireTableDataChanged();
      }
    } catch (IOException e) {
      System.err.println("Failed to load visitor log: " + e.getMessage());
    }
  }

  // Expose visitor by model index (used by advanced filtering and exports)
  Visitor getVisitor(int modelIndex) {
    if (modelIndex < 0 || modelIndex >= visitors.size()) return null;
    return visitors.get(modelIndex);
  }

  void loadFromSpecificFile(String filename) {
    File f = new File(SAVE_FOLDER + filename);
    if (!f.exists()) {
      System.err.println("File not found: " + filename);
      return;
    }
    
    visitors.clear();
    
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String line;
      while ((line = br.readLine()) != null) {
        try {
          String[] parts = splitLine(line);
          int idx = 0;
          String name = unescape(partSafe(parts, idx++));
          String contact = unescape(partSafe(parts, idx++));
          String presentedId = unescape(partSafe(parts, idx++));
          String purpose = unescape(partSafe(parts, idx++));
          String campus = unescape(partSafe(parts, idx++));
          String guardLabel = unescape(partSafe(parts, idx++));
          idx++;
          String checkInRaw = unescape(partSafe(parts, idx++));
          String checkOutRaw = unescape(partSafe(parts, idx));

          Guard guard = findGuardByLabel(guardLabel);

          LocalDateTime checkIn = checkInRaw.isEmpty() ? null : LocalDateTime.parse(checkInRaw);
          LocalDateTime checkOut = checkOutRaw.isEmpty() ? null : LocalDateTime.parse(checkOutRaw);

          Visitor v = Visitor.fromCsvFields(name, contact, presentedId, purpose, campus, guard, checkIn, checkOut);
          visitors.add(v);
        } catch (Exception inner) {
          System.err.println("Skipping malformed row: " + inner.getMessage());
        }
      }
      fireTableDataChanged();
    } catch (IOException e) {
      System.err.println("Failed to load file: " + e.getMessage());
    }
  }

  void exportToJsonFile(String filename) {
    File dataDir = new File("src/data");
    if (!dataDir.exists()) dataDir.mkdirs();
    File f = new File(dataDir, filename);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
      bw.write("[");
      boolean first = true;
      for (Visitor v : visitors) {
        if (!first) bw.write(",\n");
        first = false;
        bw.write("  {");
        bw.write(String.format("\"visitorName\": \"%s\",", escapeJson(v.getVisitorName())));
        bw.write(String.format("\"contactNumber\": \"%s\",", escapeJson(v.getContactNumber())));
        bw.write(String.format("\"presentedId\": \"%s\",", escapeJson(v.getPresentedId())));
        bw.write(String.format("\"purpose\": \"%s\",", escapeJson(v.getPurpose())));
        bw.write(String.format("\"campusArea\": \"%s\",", escapeJson(v.getCampusArea())));
        bw.write(String.format("\"guardLabel\": \"%s\",", escapeJson(v.getGuardLabel())));
        bw.write(String.format("\"gateEntered\": \"%s\",", escapeJson(v.getGateEntered())));
        bw.write(String.format("\"checkInTime\": \"%s\",", escapeJson(v.getCheckInTime())));
        bw.write(String.format("\"checkOutTime\": \"%s\"", escapeJson(v.getCheckOutTime())));
        bw.write("  }");
      }
      bw.write("\n]");
    } catch (IOException e) {
      System.err.println("Failed to export JSON: " + e.getMessage());
    }
  }

  private static String escapeJson(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
  }

  void exportToReadableTxtFile(String filename) {
    File dataDir = new File("src/data");
    if (!dataDir.exists()) dataDir.mkdirs();
    File f = new File(dataDir, filename);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
      int idx = 1;
      for (Visitor v : visitors) {
        bw.write(String.format("Visitor #%d\n", idx++));
        bw.write(String.format("  Name: %s\n", safe(v.getVisitorName())));
        bw.write(String.format("  Contact: %s\n", safe(v.getContactNumber())));
        bw.write(String.format("  Presented ID: %s\n", safe(v.getPresentedId())));
        bw.write(String.format("  Purpose: %s\n", safe(v.getPurpose())));
        bw.write(String.format("  Campus Area: %s\n", safe(v.getCampusArea())));
        bw.write(String.format("  Guard: %s\n", safe(v.getGuardLabel())));
        bw.write(String.format("  Gate Entered: %s\n", safe(v.getGateEntered())));
        bw.write(String.format("  Check-In: %s\n", safe(v.getCheckInTime())));
        bw.write(String.format("  Check-Out: %s\n", safe(v.getCheckOutTime())));
        bw.write("----\n");
      }
    } catch (IOException e) {
      System.err.println("Failed to export readable TXT: " + e.getMessage());
    }
  }

  private static String safe(String s) { return s == null ? "" : s; }

  private Guard findGuardByLabel(String label) {
    if (label == null || label.isEmpty()) return null;
    for (Guard g : guardsRef) {
      if (label.equals(g.getDisplayLabel())) return g;
    }
    return null;
  }

  private static String[] splitLine(String line) {
    // split by separator '|', but honor escaped pipes (\\|)
    List<String> parts = new ArrayList<>();
    StringBuilder buf = new StringBuilder();
    boolean escape = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (escape) {
        buf.append(c);
        escape = false;
      } else if (c == '\\') {
        // next char is escaped
        escape = true;
      } else if (c == '|') {
        parts.add(buf.toString());
        buf.setLength(0);
      } else {
        buf.append(c);
      }
    }
    parts.add(buf.toString());
    return parts.toArray(String[]::new);
  }

  private static String partSafe(String[] arr, int idx) {
    return idx < arr.length ? arr[idx] : "";
  }
}
