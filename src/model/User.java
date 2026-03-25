package model;

public class User {
    private String email;
    private String userId;
    private String password;

    public User(String email, String userId, String password) {
        this.email = email;
        this.userId = userId;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String toFileString() {
        return email + "," + userId + "," + password;
    }
}