package comquintonj.github.atlantastreetartproject.model;

import android.media.ExifInterface;

/**
 * Utilized to calculate the location of art
 */
public class GPSUtility {

    /**
     * Keeps track to see if the location is valid
     */
    private boolean valid = false;

    /**
     * The latitude of the art
     */
    private Double Latitude;

    /**
     * The longitude of the art
     */
    private Double Longitude;

    /**
     * Constructor using an ExifInterface to scrape the coordinates of art
     * @param exif the ExifInterface
     */
    public GPSUtility(ExifInterface exif) {
        String attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

        if((attrLATITUDE !=null)
                && (attrLATITUDE_REF !=null)
                && (attrLONGITUDE != null)
                && (attrLONGITUDE_REF !=null))
        {
            valid = true;

            if(attrLATITUDE_REF.equals("N")){
                Latitude = convertToDegree(attrLATITUDE);
            }
            else{
                Latitude = 0 - convertToDegree(attrLATITUDE);
            }

            if(attrLONGITUDE_REF.equals("E")){
                Longitude = convertToDegree(attrLONGITUDE);
            }
            else{
                Longitude = 0 - convertToDegree(attrLONGITUDE);
            }

        }
    };

    /**
     * Convert the coordinates to a usable form in decimal
     * @param stringDMS take in the coordinates in DMS format
     * @return the coordinates in decimal format
     */
    private Double convertToDegree(String stringDMS){
        Double result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        Double FloatS = S0/S1;

        result = FloatD + (FloatM / 60) + (FloatS / 3600);

        return result;


    };

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return (String.valueOf(Latitude)
                + ", "
                + String.valueOf(Longitude));
    }

    /**
     * Get the latitude of the art
     * @return the latitude
     */
    public double getLatitudeE6(){
        return Latitude;
    }

    /**
     * Get the longitude of the art
     * @return the longitude
     */
    public double getLongitudeE6(){
        return Longitude;
    }

}