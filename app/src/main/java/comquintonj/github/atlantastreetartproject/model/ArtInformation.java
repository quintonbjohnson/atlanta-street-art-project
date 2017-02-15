package comquintonj.github.atlantastreetartproject.model;

import com.google.firebase.auth.FirebaseUser;

public class ArtInformation {

    // Instance Variables
    public String title;
    private String address;
    private String artist;
    private String description;
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
     * @param address Where the art is located.
     * @param artist Who created the art.
     * @param description A description of what the art is.
     * @param tag Any necessary tags for the art.
     * @param user Who submitted the piece.
     */
    public ArtInformation(String title, String address, String artist,
                      String description, String tag, FirebaseUser user) {
    this.title = title;
    this.address = address;
    this.artist = artist;
    this.description = description;
    this.tag = tag;
    this.user = user;
    }
}
