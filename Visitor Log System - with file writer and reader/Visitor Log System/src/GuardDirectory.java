import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class GuardDirectory {
  private final List<Guard> guards = new ArrayList<>();

  GuardDirectory() {
    seedDefaults();
  }

  private void seedDefaults() {
    guards.add(new Guard("Adrian Bencila", "Main Gate", "Day", "guard.bencila", "ub123"));
    guards.add(new Guard("Luis Dizon", "Science High Gate", "Night", "guard.dizon", "ub123"));
    guards.add(new Guard("Maria Ramos", "Engineering Gate", "Swing", "guard.ramos", "ub123"));
    guards.add(new Guard("Jose Fernandez", "High School Lobby", "Day", "guard.fernandez", "ub123"));
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
  }

  boolean removeGuard(Guard guard) {
    return guards.remove(guard);
  }
}
