package cs1302.api;

public static class AniListResponse {
    public Data data;

    public static class Data {
        public Media Media;
    }

    public static class Media {
        public Title title;
        public int averageScore;
    }

    public static class Title {
        public String romaji;
        public String english;
        public String native_;
    }
}
