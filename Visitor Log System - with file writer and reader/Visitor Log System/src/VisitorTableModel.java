import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.table.AbstractTableModel;

class VisitorTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
      "Visitor Name",
      "Contact #",
      "Host Office",
      "Purpose",
      "Campus Area",
      "Guard on Duty",
      "Admin Reviewer",
      "Admin Status",
      "Admin Notes",
      "Check-In",
      "Check-Out"
    };

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a");
  private final List<Visitor> visitors = new ArrayList<>();

  // References to available guards/admins so we can restore saved rows
  private final List<Guard> guardsRef;
  private final List<Admin> adminsRef;

  private static final String SAVE_FILE = "visitor_logs.txt";
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
      case 2 -> visitor.getHostOffice();
      case 3 -> visitor.getPurpose();
      case 4 -> visitor.getCampusArea();
      case 5 -> visitor.getGuardLabel();
      case 6 -> visitor.getAdminLabel();
      case 7 -> visitor.getAdminStatus().getLabel();
      case 8 -> visitor.getAdminNotes();
      case 9 -> formatDate(visitor.getCheckIn());
      case 10 -> formatDate(visitor.getCheckOut());
      default -> "";
    };
  }

  VisitorTableModel() {
    this.guardsRef = List.of();
    this.adminsRef = List.of();
    loadFromFile();
  }

  VisitorTableModel(List<Guard> guards, List<Admin> admins) {
    this.guardsRef = guards == null ? List.of() : guards;
    this.adminsRef = admins == null ? List.of() : admins;
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

  boolean updateAdminStatus(int rowIndex, AdminStatus status, String note) {
    boolean changed = visitors.get(rowIndex).updateAdminStatus(status, note);
    if (changed) {
      fireTableRowsUpdated(rowIndex, rowIndex);
      saveToFile();
    }
    return changed;
  }

  void appendAdminNote(int rowIndex, String note) {
    visitors.get(rowIndex).appendAdminNote(note);
    fireTableRowsUpdated(rowIndex, rowIndex);
    saveToFile();
  }

  // Persistence helpers
  private void saveToFile() {
    File f = new File(SAVE_FILE);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
      for (Visitor v : visitors) {
        String checkIn = v.getCheckIn() == null ? "" : v.getCheckIn().toString();
        String checkOut = v.getCheckOut() == null ? "" : v.getCheckOut().toString();
        String notes = v.getAdminNotes() == null ? "" : v.getAdminNotes().replace(System.lineSeparator(), "\\n");
        String[] parts = new String[] {
            escape(v.getVisitorName()),
            escape(v.getContactNumber()),
            escape(v.getHostOffice()),
            escape(v.getPurpose()),
            escape(v.getCampusArea()),
            escape(v.getGuardLabel()),
            escape(v.getAdminLabel()),
            escape(v.getAdminStatus().name()),
            escape(notes),
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
    File f = new File(SAVE_FILE);
    if (!f.exists()) {
      return;
    }
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String line;
      while ((line = br.readLine()) != null) {
        try {
          String[] parts = splitLine(line);
          // Expecting 11 columns: name, contact, host, purpose, campus, guardLabel, adminLabel, adminStatus, adminNotes, checkIn, checkOut
          String name = unescape(partSafe(parts, 0));
          String contact = unescape(partSafe(parts, 1));
          String host = unescape(partSafe(parts, 2));
          String purpose = unescape(partSafe(parts, 3));
          String campus = unescape(partSafe(parts, 4));
          String guardLabel = unescape(partSafe(parts, 5));
          String adminLabel = unescape(partSafe(parts, 6));
          String statusName = unescape(partSafe(parts, 7));
          String notes = unescape(partSafe(parts, 8)).replace("\\n", System.lineSeparator());
          String checkInRaw = unescape(partSafe(parts, 9));
          String checkOutRaw = unescape(partSafe(parts, 10));

          Guard guard = findGuardByLabel(guardLabel);
          Admin admin = findAdminByLabel(adminLabel);

          LocalDateTime checkIn = checkInRaw.isEmpty() ? null : LocalDateTime.parse(checkInRaw);
          LocalDateTime checkOut = checkOutRaw.isEmpty() ? null : LocalDateTime.parse(checkOutRaw);
          AdminStatus status = null;
          if (!statusName.isEmpty()) {
            try { status = AdminStatus.valueOf(statusName); } catch (IllegalArgumentException ignore) {}
          }

          Visitor v = Visitor.fromCsvFields(name, contact, host, purpose, campus, guard, admin, checkIn, checkOut, status, notes);
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

  /**
   * Export visitor list to JSON file (simple array of objects). The file will be written to the given
   * filename relative to the working directory.
   */
  void exportToJsonFile(String filename) {
    File f = new File(filename);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
      bw.write("[");
      boolean first = true;
      for (Visitor v : visitors) {
        if (!first) bw.write(",\n");
        first = false;
        bw.write("  {");
        bw.write(String.format("\"visitorName\": \"%s\",", escapeJson(v.getVisitorName())));
        bw.write(String.format("\"contactNumber\": \"%s\",", escapeJson(v.getContactNumber())));
        bw.write(String.format("\"hostOffice\": \"%s\",", escapeJson(v.getHostOffice())));
        bw.write(String.format("\"purpose\": \"%s\",", escapeJson(v.getPurpose())));
        bw.write(String.format("\"campusArea\": \"%s\",", escapeJson(v.getCampusArea())));
        bw.write(String.format("\"guardLabel\": \"%s\",", escapeJson(v.getGuardLabel())));
        bw.write(String.format("\"adminLabel\": \"%s\",", escapeJson(v.getAdminLabel())));
        bw.write(String.format("\"adminStatus\": \"%s\",", v.getAdminStatus().name()));
        bw.write(String.format("\"adminNotes\": \"%s\",", escapeJson(v.getAdminNotes())));
        bw.write(String.format("\"checkIn\": \"%s\",", v.getCheckIn() == null ? "" : v.getCheckIn().toString()));
        bw.write(String.format("\"checkOut\": \"%s\"", v.getCheckOut() == null ? "" : v.getCheckOut().toString()));
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

  /**
   * Export visitor list to a readable multi-line TXT file.
   */
  void exportToReadableTxtFile(String filename) {
    File f = new File(filename);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
      int idx = 1;
      for (Visitor v : visitors) {
        bw.write(String.format("Visitor #%d\n", idx++));
        bw.write(String.format("  Name: %s\n", safe(v.getVisitorName())));
        bw.write(String.format("  Contact: %s\n", safe(v.getContactNumber())));
        bw.write(String.format("  Host Office: %s\n", safe(v.getHostOffice())));
        bw.write(String.format("  Purpose: %s\n", safe(v.getPurpose())));
        bw.write(String.format("  Campus Area: %s\n", safe(v.getCampusArea())));
        bw.write(String.format("  Guard: %s\n", safe(v.getGuardLabel())));
        bw.write(String.format("  Admin: %s\n", safe(v.getAdminLabel())));
        bw.write(String.format("  Admin Status: %s\n", v.getAdminStatus().name()));
        bw.write(String.format("  Admin Notes:\n%s\n", v.getAdminNotes().isEmpty() ? "    (none)" : indent(v.getAdminNotes(), "    ")));
        bw.write(String.format("  Check-In: %s\n", v.getCheckIn() == null ? "" : v.getCheckIn().toString()));
        bw.write(String.format("  Check-Out: %s\n", v.getCheckOut() == null ? "" : v.getCheckOut().toString()));
        bw.write("----\n");
      }
    } catch (IOException e) {
      System.err.println("Failed to export readable TXT: " + e.getMessage());
    }
  }

  private static String safe(String s) { return s == null ? "" : s; }

  private static String indent(String s, String prefix) {
    return s.replace(System.lineSeparator(), System.lineSeparator() + prefix);
  }

  private Guard findGuardByLabel(String label) {
    if (label == null || label.isEmpty()) return null;
    for (Guard g : guardsRef) {
      if (label.equals(g.getDisplayLabel())) return g;
    }
    return null;
  }

  private Admin findAdminByLabel(String label) {
    if (label == null || label.isEmpty()) return null;
    for (Admin a : adminsRef) {
      if (label.equals(a.getDisplayLabel())) return a;
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
    return parts.toArray(new String[0]);
  }

  private static String partSafe(String[] arr, int idx) {
    return idx < arr.length ? arr[idx] : "";
  }

  private String formatDate(LocalDateTime date) {
    return date == null ? "" : date.format(FORMATTER);
  }
}
