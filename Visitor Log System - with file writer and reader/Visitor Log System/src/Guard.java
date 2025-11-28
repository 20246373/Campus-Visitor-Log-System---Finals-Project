class Guard {
  private final String name;
  private final String post;
  private final String shift;
  private final String username;
  private final String password;

  Guard(String name, String post, String shift, String username, String password) {
    this.name = name;
    this.post = post;
    this.shift = shift;
    this.username = username;
    this.password = password;
  }

  String getName() {
    return name;
  }

  String getPost() {
    return post;
  }

  String getShift() {
    return shift;
  }

  String getUsername() {
    return username;
  }

  boolean matchesCredentials(String providedUsername, String providedPassword) {
    return username.equalsIgnoreCase(providedUsername) && password.equals(providedPassword);
  }

  String getDisplayLabel() {
    String postInfo = post == null || post.isEmpty() ? "" : " - " + post;
    String shiftInfo = shift == null || shift.isEmpty() ? "" : " (" + shift + ")";
    return name + postInfo + shiftInfo;
  }

  @Override
  public String toString() {
    return getDisplayLabel();
  }
}
