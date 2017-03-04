package comquintonj.github.atlantastreetartproject.model;

import java.util.Date;

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
     * The distance away from the art
     */
    private double distance;

    /**
     * The latitude of the art
     */
    private double latitude;

    /**
     * The longitude of the art
     */
    private double longitude;

    /**
     * The path of the image of the art
     */
    private String photoPath;

    /**
     * The downvotes of the art
     */
    private int ratingDownvotes;

    /**
     * The upvotes of the art
     */
    private int ratingUpvotes;

    /**
     * The title of the art
     */
    private String title;

    /**
     * The creation time of the art
     */
    private long createdAt;

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
    public ArtInformation(String artist, String displayName, double latitude, double longitude,
                          String photoPath, int ratingDownvotes, int ratingUpvotes,
                          String title) {
        this.artist = artist;
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
        this.ratingDownvotes = ratingDownvotes;
        this.ratingUpvotes = ratingUpvotes;
        this.title = title;
        this.createdAt = new Date().getTime();
    }

    /**
     * The creation time of the art.
     * @return the creation time
     */
    public long getCreatedAt() {
        return createdAt;
    }

    public Date creationDate() {
        return new Date(createdAt);
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
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get the longitude of the art
     * @return the longitude
     */
    public double getLongitude() {
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
        return ratingUpvotes - ratingDownvotes;
    }

    /**
     * The downvotes of the art.
     * @return the number of downvotes
     */
    public int getRatingDownvotes() {
        return ratingDownvotes;
    }

    /**
     * The upvotes of the art.
     * @return the number of upvotes
     */
    public int getRatingUpvotes() {
        return ratingUpvotes;
    }

    /**
     * Add one additional upvote to the art.
     */
    public void incUpvote() {
        this.ratingUpvotes = ratingUpvotes + 1;
    }

    /**
     * Add one additional downvote to the art.
     */
    public void incDownvote() {
        this.ratingDownvotes = ratingDownvotes + 1;
    }

    /**
     * Remove one upvote to the art.
     */
    public void decUpvote() {
        if (ratingUpvotes > 0) {
            this.ratingUpvotes = ratingUpvotes - 1;
        }
    }

    /**
     * Remove one downvote to the art.
     */
    public void decDownvote() {
        if (ratingDownvotes > 0) {
            this.ratingDownvotes = ratingDownvotes - 1;
        }
    }

    /**
     * Get the distance away from the art
     * @return the distance away
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Set the distance away from the art
     * @param distance the distance away
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }
}
