import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AdminDirectory {
  private final List<Admin> admins = new ArrayList<>();
  private static final String SAVE_FILE = "src/data/admins.txt";

  AdminDirectory() {
    loadFromFile();
    if (admins.isEmpty()) {
      seedDefaults();
      saveToFile();
    }
  }

  private void seedDefaults() {
    admins.add(new Admin("Adrian Bencila", "Office of Student Affairs", "Director", "adrian.bencila", "ubAdmin"));
    admins.add(new Admin("Miguel Cruz", "Quality Assurance Office", "Coordinator", "admin.cruz", "ubAdmin"));
    admins.add(new Admin("Liza Villanueva", "Finance Department", "Manager", "admin.villanueva", "ubAdmin"));
    admins.add(new Admin("Rafael Mendoza", "Research & Development Center", "Supervisor", "admin.mendoza", "ubAdmin"));
  }

  List<Admin> getAdmins() {
    return Collections.unmodifiableList(admins);
  }

  Admin authenticate(String username, String password) {
    return admins.stream()
        .filter(admin -> admin.matchesCredentials(username, password))
        .findFirst()
        .orElse(null);
  }

  void addAdmin(Admin admin) {
    admins.add(admin);
    saveToFile();
  }

  private void saveToFile() {
    File f = new File(SAVE_FILE);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
      for (Admin a : admins) {
        String[] parts = new String[] {
            escape(a.getName()),
            escape(a.getOffice()),
            escape(a.getRole()),
            escape(a.getUsername()),
            escape(a.getPassword())
        };
        bw.write(String.join("|", parts));
        bw.newLine();
      }
    } catch (IOException e) {
      System.err.println("Failed to save admins: " + e.getMessage());
    }
  }

  private void loadFromFile() {
    File f = new File(SAVE_FILE);
    if (!f.exists()) {
      return;
    }
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isBlank()) continue;
        String[] parts = split(line);
        String name = unescape(partSafe(parts, 0));
        String office = unescape(partSafe(parts, 1));
        String role = unescape(partSafe(parts, 2));
        String username = unescape(partSafe(parts, 3));
        String password = unescape(partSafe(parts, 4));
        admins.add(new Admin(name, office, role, username, password));
      }
    } catch (IOException e) {
      System.err.println("Failed to load admins: " + e.getMessage());
    }
  }

  private static String escape(String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\").replace("|", "\\|");
  }

  private static String unescape(String value) {
    if (value == null) return "";
    return value.replace("\\|", "|").replace("\\\\", "\\");
  }

  private static String[] split(String line) {
    List<String> parts = new ArrayList<>();
    StringBuilder buf = new StringBuilder();
    boolean escape = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (escape) {
        buf.append(c);
        escape = false;
      } else if (c == '\\') {
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
