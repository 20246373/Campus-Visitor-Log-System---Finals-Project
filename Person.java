abstract class Person implements Authenticatable {
    protected final String name;
    protected final String username;
    protected final String password;
    
    protected Person(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public boolean matchesCredentials(String providedUsername, String providedPassword) {
        return username.equalsIgnoreCase(providedUsername) && password.equals(providedPassword);
    }
    
    public abstract String getDisplayLabel();
    
    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
