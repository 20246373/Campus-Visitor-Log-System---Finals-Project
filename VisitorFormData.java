class VisitorFormData {
  private final String visitorName;
  private final String contactNumber;
  private final String presentedId;
  private final String purpose;
  private final String campusArea;
  private Guard guard;

  VisitorFormData(String visitorName,
                  String contactNumber,
                  String presentedId,
                  String purpose,
                  String campusArea,
                  Guard guard) {
    this.visitorName = safeTrim(visitorName);
    this.contactNumber = safeTrim(contactNumber);
    this.presentedId = safeTrim(presentedId);
    this.purpose = safeTrim(purpose);
    this.campusArea = campusArea == null ? "" : campusArea;
    this.guard = guard;
  }

  String getPresentedId() {
    return presentedId;
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

  String getPurpose() {
    return purpose;
  }

  String getCampusArea() {
    return campusArea;
  }

  Guard getGuard() {
    return guard;
  }

  void setGuard(Guard guard) {
    this.guard = guard;
  }
}
