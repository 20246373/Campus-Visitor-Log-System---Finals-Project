import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AdminDirectory {
  private final List<Admin> admins = new ArrayList<>();

  AdminDirectory() {
    seedDefaults();
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
}
