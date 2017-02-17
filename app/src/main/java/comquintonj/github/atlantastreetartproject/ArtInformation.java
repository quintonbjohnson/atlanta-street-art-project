package comquintonj.github.atlantastreetartproject;

public class ArtInformation {

    // Instance Variables
    private String title;
    private String location;
    private String artist;
    private String photoPath;
    private String displayName;

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
     * @param displayName Who submitted the piece.
     */
    public ArtInformation(String title, String location, String artist,
                          String photoPath, String displayName) {
        this.title = title;
        this.location = location;
        this.artist = artist;
        this.photoPath = photoPath;
        this.displayName = displayName;

    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLocation() {
        return location;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public String getDisplayName() {
        return displayName;
    }
}
