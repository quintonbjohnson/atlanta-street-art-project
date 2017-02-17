package comquintonj.github.atlantastreetartproject;

import com.google.firebase.auth.FirebaseUser;

public class ArtInformation {

    // Instance Variables
    public String title;
    private String artist;
    private String location;
    private FirebaseUser user;

    /**
     * No args constructor
     */
    public ArtInformation(){

    }

    /**
     * Art information object used to store necessary info for Firebase
     * @param title The title of the art piece.
     * @param artist Who created the art.
     * @param location Any necessary tags for the art.
     * @param user Who submitted the piece.
     */
    public ArtInformation(String title, String location, String artist, FirebaseUser user) {
        this.title = title;
        this.location = location;
        this.artist = artist;
        this.user = user;
    }
}
