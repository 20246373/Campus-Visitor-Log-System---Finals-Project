import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Visitor {
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

  private String visitorName;
  private String contactNumber;
  private String presentedId;
  private String purpose;
  private String campusArea;
  private Guard guard;
  private final LocalDateTime checkIn;
  private LocalDateTime checkOut;

  private Visitor(String visitorName,
                  String contactNumber,
                  String presentedId,
                  String purpose,
                  String campusArea,
                  Guard guard,
                  LocalDateTime checkIn) {
    this.visitorName = visitorName;
    this.contactNumber = contactNumber;
    this.presentedId = presentedId;
    this.purpose = purpose;
    this.campusArea = campusArea;
    this.guard = guard;
    this.checkIn = checkIn;
  }

  static Visitor fromFormData(VisitorFormData data) {
    return new Visitor(
        data.getVisitorName(),
        data.getContactNumber(),
        data.getPresentedId(),
        data.getPurpose(),
        data.getCampusArea(),
        data.getGuard(),
        LocalDateTime.now()
    );
  }

  String getPresentedId() {
    return presentedId;
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

  String getGuardLabel() {
    return guard == null ? "" : guard.getDisplayLabel();
  }
  
  String getGateEntered() {
    return guard == null ? "" : (guard.getPost() == null ? "" : guard.getPost());
  }

  LocalDateTime getCheckIn() {
    return checkIn;
  }

  LocalDateTime getCheckOut() {
    return checkOut;
  }
  
  String getCheckInTime() {
    return checkIn == null ? "" : checkIn.format(TIME_FORMATTER);
  }
  
  String getCheckOutTime() {
    return checkOut == null ? "" : checkOut.format(TIME_FORMATTER);
  }

  static Visitor fromCsvFields(String visitorName,
                               String contactNumber,
                               String presentedId,
                               String purpose,
                               String campusArea,
                               Guard guard,
                               java.time.LocalDateTime checkIn,
                               java.time.LocalDateTime checkOut) {
    Visitor v = new Visitor(visitorName, contactNumber, presentedId, purpose, campusArea, guard, checkIn);
    v.checkOut = checkOut;
    return v;
  }

  boolean markCheckout(LocalDateTime checkoutTime) {
    if (checkOut != null) {
      return false;
    }
    checkOut = checkoutTime;
    return true;
  }

  void updateDetails(VisitorFormData data) {
    if (data == null) {
      return;
    }
    this.visitorName = data.getVisitorName();
    this.contactNumber = data.getContactNumber();
    this.presentedId = data.getPresentedId();
    this.purpose = data.getPurpose();
    this.campusArea = data.getCampusArea();
    this.guard = data.getGuard();
  }
}
