import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Visitor {
  private static final DateTimeFormatter NOTE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a");

  private final String visitorName;
  private final String contactNumber;
  private final String hostOffice;
  private final String purpose;
  private final String campusArea;
  private final Guard guard;
  private final Admin admin;
  private final LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private AdminStatus adminStatus = AdminStatus.PENDING;
  private String adminNotes = "";

  private Visitor(String visitorName,
                  String contactNumber,
                  String hostOffice,
                  String purpose,
                  String campusArea,
                  Guard guard,
                  Admin admin,
                  LocalDateTime checkIn) {
    this.visitorName = visitorName;
    this.contactNumber = contactNumber;
    this.hostOffice = hostOffice;
    this.purpose = purpose;
    this.campusArea = campusArea;
    this.guard = guard;
    this.admin = admin;
    this.checkIn = checkIn;
  }

  static Visitor fromFormData(VisitorFormData data) {
    return new Visitor(
        data.getVisitorName(),
        data.getContactNumber(),
        data.getHostOffice(),
        data.getPurpose(),
        data.getCampusArea(),
        data.getGuard(),
        data.getAdmin(),
        LocalDateTime.now()
    );
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

  String getGuardLabel() {
    return guard == null ? "" : guard.getDisplayLabel();
  }

  String getAdminLabel() {
    return admin == null ? "" : admin.getDisplayLabel();
  }

  LocalDateTime getCheckIn() {
    return checkIn;
  }

  LocalDateTime getCheckOut() {
    return checkOut;
  }

  AdminStatus getAdminStatus() {
    return adminStatus;
  }

  String getAdminNotes() {
    return adminNotes;
  }

  // Factory for restoring saved visitors (used by persistence loader).
  static Visitor fromCsvFields(String visitorName,
                               String contactNumber,
                               String hostOffice,
                               String purpose,
                               String campusArea,
                               Guard guard,
                               Admin admin,
                               java.time.LocalDateTime checkIn,
                               java.time.LocalDateTime checkOut,
                               AdminStatus adminStatus,
                               String adminNotes) {
    Visitor v = new Visitor(visitorName, contactNumber, hostOffice, purpose, campusArea, guard, admin, checkIn);
    v.checkOut = checkOut;
    if (adminStatus != null) {
      v.adminStatus = adminStatus;
    }
    v.adminNotes = adminNotes == null ? "" : adminNotes;
    return v;
  }

  boolean markCheckout(LocalDateTime checkoutTime) {
    if (checkOut != null) {
      return false;
    }
    checkOut = checkoutTime;
    return true;
  }

  boolean updateAdminStatus(AdminStatus status, String note) {
    boolean statusChanged = this.adminStatus != status;
    this.adminStatus = status;
    boolean noteAdded = appendAdminNoteInternal(note);
    return statusChanged || noteAdded;
  }

  void appendAdminNote(String note) {
    appendAdminNoteInternal(note);
  }

  private boolean appendAdminNoteInternal(String note) {
    if (note == null) {
      return false;
    }
    String trimmed = note.trim();
    if (trimmed.isEmpty()) {
      return false;
    }
    String stamped = String.format("%s - %s",
        LocalDateTime.now().format(NOTE_FORMATTER),
        trimmed);
    if (adminNotes.isEmpty()) {
      adminNotes = stamped;
    } else {
      adminNotes = adminNotes + System.lineSeparator() + stamped;
    }
    return true;
  }
}
