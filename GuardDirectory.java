import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class GuardDirectory {
  private final List<Guard> guards = new ArrayList<>();
  private static final String SAVE_FILE = "src/data/guards.txt";

  GuardDirectory() {
    loadFromFile();
    if (guards.isEmpty()) {
      seedDefaults();
      saveToFile();
    }
  }

  private void seedDefaults() {
    guards.add(new Guard("Adrian Bencila", "Gate A", "Morning", "guard.bencila", "ub123"));
    guards.add(new Guard("Luis Dizon", "Gate A", "Afternoon", "guard.dizon", "ub123"));
    guards.add(new Guard("Maria Ramos", "Gate A", "Night", "guard.ramos", "ub123"));
    
    guards.add(new Guard("Jose Fernandez", "Gate B", "Morning", "guard.fernandez", "ub123"));
    guards.add(new Guard("Carlos Santos", "Gate B", "Afternoon", "guard.santos", "ub123"));
    guards.add(new Guard("Ana Lopez", "Gate B", "Night", "guard.lopez", "ub123"));
    
    guards.add(new Guard("Miguel Torres", "UB Square", "Morning", "guard.torres", "ub123"));
    guards.add(new Guard("Sofia Cruz", "UB Square", "Afternoon", "guard.cruz", "ub123"));
    guards.add(new Guard("Rafael Reyes", "UB Square", "Night", "guard.reyes", "ub123"));
    
    guards.add(new Guard("Diego Santos", "F Main Entrance", "Morning", "guard.dsantos", "ub123"));
    guards.add(new Guard("Elena Martinez", "F Main Entrance", "Afternoon", "guard.martinez", "ub123"));
    guards.add(new Guard("Pedro Garcia", "F Main Entrance", "Night", "guard.garcia", "ub123"));
    
    guards.add(new Guard("Rosa Mendoza", "Gate D", "Morning", "guard.mendoza", "ub123"));
    guards.add(new Guard("Juan Dela Cruz", "Gate D", "Afternoon", "guard.delacruz", "ub123"));
    guards.add(new Guard("Carmen Aquino", "Gate D", "Night", "guard.aquino", "ub123"));
    
    guards.add(new Guard("Antonio Valdez", "Legacy", "Morning", "guard.valdez", "ub123"));
    guards.add(new Guard("Linda Salazar", "Legacy", "Afternoon", "guard.salazar", "ub123"));
    guards.add(new Guard("Roberto Navarro", "Legacy", "Night", "guard.navarro", "ub123"));
    
    guards.add(new Guard("Gloria Perez", "Science High", "Morning", "guard.perez", "ub123"));
    guards.add(new Guard("Ricardo Morales", "Science High", "Afternoon", "guard.morales", "ub123"));
    guards.add(new Guard("Teresa Castillo", "Science High", "Night", "guard.castillo", "ub123"));
    
    guards.add(new Guard("Fernando Gomez", "RCB", "Morning", "guard.gomez", "ub123"));
    guards.add(new Guard("Patricia Flores", "RCB", "Afternoon", "guard.flores", "ub123"));
    guards.add(new Guard("Manuel Rivera", "RCB", "Night", "guard.rivera", "ub123"));
    
    guards.add(new Guard("Beatriz Ortega", "Centennial", "Morning", "guard.ortega", "ub123"));
    guards.add(new Guard("Jorge Herrera", "Centennial", "Afternoon", "guard.herrera", "ub123"));
    guards.add(new Guard("Victoria Ramos", "Centennial", "Night", "guard.vramos", "ub123"));
  }

  List<Guard> getGuards() {
    return Collections.unmodifiableList(guards);
  }

  Guard authenticate(String username, String password) {
    return guards.stream()
        .filter(guard -> guard.matchesCredentials(username, password))
        .findFirst()
        .orElse(null);
  }

  void addGuard(Guard guard) {
    guards.add(guard);
    saveToFile();
  }

  void updateGuard(Guard oldGuard, Guard newGuard) {
    int index = guards.indexOf(oldGuard);
    if (index >= 0) {
      guards.set(index, newGuard);
      saveToFile();
    }
  }

  boolean removeGuard(Guard guard) {
    boolean removed = guards.remove(guard);
    if (removed) {
      saveToFile();
    }
    return removed;
  }

  private void saveToFile() {
    File f = new File(SAVE_FILE);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
      for (Guard g : guards) {
        String[] parts = new String[] {
            escape(g.getName()),
            escape(g.getPost()),
            escape(g.getShift()),
            escape(g.getUsername()),
            escape(g.getPassword())
        };
        bw.write(String.join("|", parts));
        bw.newLine();
      }
    } catch (IOException e) {
      System.err.println("Failed to save guards: " + e.getMessage());
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
        String post = unescape(partSafe(parts, 1));
        String shift = unescape(partSafe(parts, 2));
        String username = unescape(partSafe(parts, 3));
        String password = unescape(partSafe(parts, 4));
        guards.add(new Guard(name, post, shift, username, password));
      }
    } catch (IOException e) {
      System.err.println("Failed to load guards: " + e.getMessage());
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
