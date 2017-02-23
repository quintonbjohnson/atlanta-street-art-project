package comquintonj.github.atlantastreetartproject.model;

public class ArtInformation {

    // Instance Variables
    private String artist;
    private String displayName;
    private String location;
    private String photoPath;
    private String rating;
    private String title;

    /**
     * No args constructor
     */
    public ArtInformation(){

    }

    /**
     * Art information object used to store necessary info for Firebase.
     * @param title The title of the art piece
     * @param artist Who created the art
     * @param location Any necessary tags for the art
     * @param displayName Who submitted the piece
     */
    public ArtInformation(String artist, String displayName, String location,
                          String photoPath, String rating, String title) {
        this.artist = artist;
        this.displayName = displayName;
        this.location = location;
        this.photoPath = photoPath;
        this.rating = rating;
        this.title = title;
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

    public String getRating() {
        return rating;
    }


}
