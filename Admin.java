class Admin extends Person {
  private final String office;
  private final String role;

  Admin(String name, String office, String role, String username, String password) {
    super(name, username, password);
    this.office = office;
    this.role = role;
  }

  String getOffice() {
    return office;
  }

  String getRole() {
    return role;
  }

  @Override
  public String getDisplayLabel() {
    String officeInfo = office == null || office.isEmpty() ? "" : " - " + office;
    String roleInfo = role == null || role.isEmpty() ? "" : " (" + role + ")";
    return name + officeInfo + roleInfo;
  }
}
