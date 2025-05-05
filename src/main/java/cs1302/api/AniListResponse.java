package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the response from the AniList GraphQL API.
 */
public class AniListResponse {
    public Data data;

    /**
     * Represents the actual anime object in the AniList response.
     */
    public static class Data {
        @SerializedName("Media")
        public Media media;
    } //Data

    /**
     * Represents the malID from anilist which we'll use for our RESTful Jikan response.
     */
    public static class Media {
        public int idMal;
    }
} //AniListResponse


/**
 * ta note: I goofed up with naming in both this class and the 
 * trace moe class but didn't realize till I was about to compile.
 * instead of going back I just decided to use SeralizedName to help
 * fix the name here so I didn't have to rewrite 40 lines of code
 */
