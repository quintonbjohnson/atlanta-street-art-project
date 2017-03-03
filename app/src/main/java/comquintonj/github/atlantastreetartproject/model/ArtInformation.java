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
     * The latitude of the art
     */
    private String latitude;

    /**
     * The longitude of the art
     */
    private String longitude;

    /**
     * The path of the image of the art
     */
    private String photoPath;

    /**
     * The downvotes of the art
     */
    private String ratingDownvotes;

    /**
     * The upvotes of the art
     */
    private String ratingUpvotes;

    /**
     * The title of the art
     */
    private String title;

    /**
     * No-args constructor for an Art object for Firebase.
     */
    public ArtInformation() {

    }

    /**
     * Constructor for a piece of art
     * @param artist the artist
     * @param displayName the display name of the user that submitted the art
     * @param latitude the latitude of the art
     * @param longitude the longitude of the art
     * @param photoPath the path to the image of the art
     * @param ratingDownvotes the downvotes of the art
     * @param ratingUpvotes the upvotes of the art
     * @param title the title of the art
     */
    public ArtInformation(String artist, String displayName, String latitude, String longitude,
                          String photoPath, String ratingDownvotes, String ratingUpvotes,
                          String title) {
        this.artist = artist;
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
        this.ratingDownvotes = ratingDownvotes;
        this.ratingUpvotes = ratingUpvotes;
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
     * Get the latitude of the art
     * @return the latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Get the longitude of the art
     * @return the longitude
     */
    public String getLongitude() {
        return longitude;
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
     * Get the normalized rating of the piece of art
     * @return the number of upvotes minus the number of downvotes
     */
    public int getRating() {
        return Integer.parseInt(ratingUpvotes) - Integer.parseInt(ratingDownvotes);
    }

    /**
     * The downvotes of the art.
     * @return the number of downvotes
     */
    public String getRatingDownvotes() {
        return ratingDownvotes;
    }

    /**
     * The upvotes of the art.
     * @return the number of upvotes
     */
    public String getRatingUpvotes() {
        return ratingUpvotes;
    }

    /**
     * Add one additional upvote to the art.
     */
    public void incUpvote() {
        this.ratingUpvotes = String.valueOf(Integer.parseInt(ratingUpvotes) + 1);
    }

    /**
     * Add one additional downvote to the art.
     */
    public void incDownvote() {
        this.ratingDownvotes = String.valueOf(Integer.parseInt(ratingDownvotes) + 1);
    }

    /**
     * Remove one upvote to the art.
     */
    public void decUpvote() {
        if (Integer.parseInt(ratingUpvotes) > 0) {
            this.ratingUpvotes = String.valueOf(Integer.parseInt(ratingUpvotes) - 1);
        }
    }

    /**
     * Remove one downvote to the art.
     */
    public void decDownvote() {
        if (Integer.parseInt(ratingDownvotes) > 0) {
            this.ratingDownvotes = String.valueOf(Integer.parseInt(ratingDownvotes) - 1);
        }
    }




}
