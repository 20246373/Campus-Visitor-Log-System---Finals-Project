class VisitorFormData {
  private final String visitorName;
  private final String contactNumber;
  private final String hostOffice;
  private final String purpose;
  private final String campusArea;
  private final Guard guard;
  private final Admin admin;

  VisitorFormData(String visitorName,
                  String contactNumber,
                  String hostOffice,
                  String purpose,
                  String campusArea,
                  Guard guard,
                  Admin admin) {
    this.visitorName = safeTrim(visitorName);
    this.contactNumber = safeTrim(contactNumber);
    this.hostOffice = safeTrim(hostOffice);
    this.purpose = safeTrim(purpose);
    this.campusArea = campusArea == null ? "" : campusArea;
    this.guard = guard;
    this.admin = admin;
  }

  private String safeTrim(String value) {
    return value == null ? "" : value.trim();
  }

  String getVisitorName() {
    return visitorName;
  }

  String getContactNumber() {
    return contactNumber;
  }

  String getHostOffice() {
    return hostOffice;
  }

  String getPurpose() {
    return purpose;
  }

  String getCampusArea() {
    return campusArea;
  }

  Guard getGuard() {
    return guard;
  }

  Admin getAdmin() {
    return admin;
  }
}
