package es.model;

public class User {
    private Integer userId;
    private String username;
    private String dateOfBirth;
    private String email;

    public User() {
    }
    
    public User(Integer userId,String username, String dateOfBirth, String email) {
        this.userId= userId;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
    }

    
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
