enum AdminStatus {
  PENDING("Pending Review"),
  APPROVED("Approved"),
  DENIED("Denied");

  private final String label;

  AdminStatus(String label) {
    this.label = label;
  }

  String getLabel() {
    return label;
  }
}
