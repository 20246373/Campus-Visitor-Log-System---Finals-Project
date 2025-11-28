enum UserRole {
  GUARD("Guard"),
  ADMIN("Administrator");

  private final String label;

  UserRole(String label) {
    this.label = label;
  }

  String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }
}
