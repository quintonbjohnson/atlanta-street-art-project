package comquintonj.github.atlantastreetartproject.model;


public class User {

    private String profileName;
    private String email;

    public User() {

    }

    public User(String profileName, String email) {
        this.profileName = profileName;
        this.email = email;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getEmail() {
        return email;
    }
}
