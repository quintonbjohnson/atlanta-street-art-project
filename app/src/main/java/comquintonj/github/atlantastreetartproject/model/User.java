package comquintonj.github.atlantastreetartproject.model;

import java.util.HashMap;

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
     * A list of titles that the user has rated
     */
    private HashMap<String, String> artRated;

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
    public User(String profileName, String email, HashMap<String, String> artRated) {
        this.profileName = profileName;
        this.email = email;
        this.artRated = new HashMap<>();
    }

    /**
     * Get the profile name of the user.
     * @return The profile name
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Get the email of the user.
     * @return The email of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the art the user has voted on.
     * @return the HashSet containing titles the user has voted for.
     */
    public HashMap<String, String> getArtRated() {
        return artRated;
    }

    /**
     * Add art that the user has rated.
     * @param title the title of the art
     */
    public void addRatedArt(String title) {
        artRated.put(title, "");
    }
}


