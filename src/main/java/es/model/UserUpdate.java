package sos.model;


public class UserUpdate {
    private String username;
    private String dateOfBirth;
    private String email;

    public UserUpdate() {
    }

    public UserUpdate(String username, String dateOfBirth, String email) {
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
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
