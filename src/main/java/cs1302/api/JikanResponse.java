package cs1302.api;

import com.google.gson.annotations.SerializedName;


/**
 * This represents the response from the JikanAPI. 
 */  
public class JikanResponse {
    public Data data;

    /**
     * This is the actual raw metadata response from Jikan.
     * it has the title, score, and cover image for the show.
     */   
    public static class Data {
        public String title;
        public double score;
        public Images images;
    }
    
    /**
     * This is the actual jpg image for the cover image.
     */
    public static class Images {
        public Jpg jpg;
    }
    
    /**
     * This is the image itself from it's file.
     */
    public static class Jpg {
        @SerializedName("image_url")
        public String imageUrl;
    }
}

