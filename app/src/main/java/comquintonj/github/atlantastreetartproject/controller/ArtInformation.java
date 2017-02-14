package comquintonj.github.atlantastreetartproject.controller;

/**
 * Created by alisha on 2/8/2017.
 */

public class ArtInformation {

//might set these to private and make getters and setters in the future as needed
        public String title;
        public String address;
        public String artist;
        public String tag;

        public ArtInformation(){

        }

        public ArtInformation(String title, String address, String artist, String tag) {
            this.title = title;
            this.address = address;
            this.artist = artist;
            this.tag = tag;
        }

}
