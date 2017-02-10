package comquintonj.github.atlantastreetartproject.controller;

/**
 * Created by alisha on 2/8/2017.
 */

public class ArtInformation {

//will set these private and getters and setters in the future
        public String title;
        public String address;
        public String artist;
        public String description;
        public String tag;

        public ArtInformation(){

        }

        public ArtInformation(String title, String address, String artist, String description, String tag) {
            this.title = title;
            this.address = address;
            this.artist = artist;
            this.description = description;
            this.tag = tag;
        }

}
