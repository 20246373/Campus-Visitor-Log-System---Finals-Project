interface Authenticatable {
    boolean matchesCredentials(String username, String password);
    String getUsername();
    String getPassword();
}
