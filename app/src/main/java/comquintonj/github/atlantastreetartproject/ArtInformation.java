package comquintonj.github.atlantastreetartproject;

import com.google.firebase.auth.FirebaseUser;

public class ArtInformation {

    // Instance Variables
    public String title;
    private String artist;
    private String tag;
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
     * @param tag Any necessary tags for the art.
     * @param user Who submitted the piece.
     */
    public ArtInformation(String title, String artist, String tag, FirebaseUser user) {
        this.title = title;
        this.artist = artist;
        this.tag = tag;
        this.user = user;
    }
}
