package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the response from the trace.moe API.
 */
public class TraceMoeResponse {
    /** Array of results returned from the API. */
    public Result[] result;

    /**
     * Represents a single result from the trace.moe API.
     */
    public static class Result {
        public String filename;
        public int episode;
        public double similarity;
        public String image;
        public String video;
        public int anilist;

        @SerializedName("title_english")
        public String titleEnglish;

        @SerializedName("title_romaji")
        public String titleRomaji;

        @SerializedName("title_native")
        public String titleNative;
    } // Result
} // TraceMoeResponse
