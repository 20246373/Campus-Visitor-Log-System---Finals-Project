class Guard extends Person {
  private final String post;
  private final String shift;

  Guard(String name, String post, String shift, String username, String password) {
    super(name, username, password);
    this.post = post;
    this.shift = shift;
  }

  String getPost() {
    return post;
  }

  String getShift() {
    return shift;
  }

  @Override
  public String getDisplayLabel() {
    return name;
  }
}
