public class UserRole {
  public static final UserRole GUARD = new UserRole("Guard");
  public static final UserRole ADMIN = new UserRole("Administrator");

  private final String label;

  private UserRole(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }

  public static UserRole[] values() {
    return new UserRole[]{GUARD, ADMIN};
  }
}
