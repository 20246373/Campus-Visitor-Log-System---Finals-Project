public class AdminStatus {
  public static final AdminStatus PENDING = new AdminStatus("Pending Review");
  public static final AdminStatus APPROVED = new AdminStatus("Approved");
  public static final AdminStatus DENIED = new AdminStatus("Denied");

  private final String label;

  private AdminStatus(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public String name() {
    if (this == PENDING) return "PENDING";
    if (this == APPROVED) return "APPROVED";
    if (this == DENIED) return "DENIED";
    return "";
  }

  public static AdminStatus valueOf(String name) {
    if (name.equals("PENDING")) return PENDING;
    if (name.equals("APPROVED")) return APPROVED;
    if (name.equals("DENIED")) return DENIED;
    return PENDING;
  }
}
