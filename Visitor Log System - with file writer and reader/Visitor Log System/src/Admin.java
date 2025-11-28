class Admin {
  private final String name;
  private final String office;
  private final String role;
  private final String username;
  private final String password;

  Admin(String name, String office, String role, String username, String password) {
    this.name = name;
    this.office = office;
    this.role = role;
    this.username = username;
    this.password = password;
  }

  String getName() {
    return name;
  }

  String getOffice() {
    return office;
  }

  String getRole() {
    return role;
  }

  boolean matchesCredentials(String providedUsername, String providedPassword) {
    return username.equalsIgnoreCase(providedUsername) && password.equals(providedPassword);
  }

  String getDisplayLabel() {
    String officeInfo = office == null || office.isEmpty() ? "" : " - " + office;
    String roleInfo = role == null || role.isEmpty() ? "" : " (" + role + ")";
    return name + officeInfo + roleInfo;
  }

  @Override
  public String toString() {
    return getDisplayLabel();
  }
}
