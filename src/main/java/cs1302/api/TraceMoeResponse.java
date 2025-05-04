package cs1302.api;

public class TraceMoeResponse {
    public Result[] result;

    public static class Result {
      public String filename;
      public int episode;
      public double similarity;
      public String image;
      public String video;
      public int anilist;
      public String title_english;
      public String title_romaji;
      public String title_native;
    } //Result
} //TraceMoeResponse
