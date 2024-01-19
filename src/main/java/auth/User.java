package auth;


import org.mindrot.jbcrypt.BCrypt;

public class User {
    private String username;
    private String passwordHash;  // Change to store the hashed password
    private Boolean isAdmin; 

    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.passwordHash = BCrypt.hashpw(password,BCrypt.gensalt(12));  // Hash the password during construction
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = BCrypt.hashpw(password,BCrypt.gensalt(12));  // Hash the new password when set
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

}