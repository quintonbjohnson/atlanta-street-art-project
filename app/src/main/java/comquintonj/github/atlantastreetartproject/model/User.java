package comquintonj.github.atlantastreetartproject.model;

public class User {

    /**
     * The email of the user
     */
    private String email;

    /**
     * Profile name for the user
     */
    private String profileName;

    /**
     * No-args constructor for a User object for Firebase.
     */
    public User() {

    }

    /**
     * Constructor for a User object.
     * @param profileName The username the user enters upon registering
     * @param email The email associated with the user's account
     */
    public User(String profileName, String email) {
        this.profileName = profileName;
        this.email = email;
    }

    /**
     * Get the profile name of the user.
     * @return The profile name
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Get the email of the user
     * @return The email of the user
     */
    public String getEmail() {
        return email;
    }
}
