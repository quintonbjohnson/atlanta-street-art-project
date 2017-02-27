package comquintonj.github.atlantastreetartproject.model;

public class ArtInformation {

    /**
     * The artist of the art
     */
    private String artist;

    /**
     * The display name of the user that submitted the art
     */
    private String displayName;

    /**
     * The location of the art
     */
    private String location;

    /**
     * The path of the image of the art
     */
    private String photoPath;

    /**
     * The rating of the art
     */
    private String rating;

    /**
     * The title of the art
     */
    private String title;

    /**
     * Constructor for a piece of art
     * @param artist the artist
     * @param displayName the display name of the user that submitted the art
     * @param location the location of the art
     * @param photoPath the path to the image of the art
     * @param rating the rating of the ar
     * @param title the title of the art
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

    /**
     * The title of the art.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * The artist of the art.
     * @return the artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * The location of the art.
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * The path to the image of the art.
     * @return the path
     */
    public String getPhotoPath() {
        return photoPath;
    }

    /**
     * The display name of the user that submitted the art.
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * The rating of the art.
     * @return the rating
     */
    public String getRating() {
        return rating;
    }


}
