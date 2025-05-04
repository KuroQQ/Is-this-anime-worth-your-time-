package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import com.google.gson.Gson;
import cs1302.api.TraceMoeResponse;
import cs1302.api.AniListResponse;

/**
 * This app that checks if an anime screenshot is from a show that's worth your time.
 * The user is prompted to put in an anime screenshot (jpeg, png, gif) and then it will
 * use trace moe to help track down where the original anime is from. Once it has the anime.
 * It will use Anilist's api to see if the anime is worth the user's time based on whatever the
 * user deems a score to be. if the Anime's score is above the user's minimum score then the anime
 * is rated "Kino" if it's below then it is rated "Slop".
 */
public class ApiApp extends Application {

    Stage stage;
    Scene scene;
    VBox root;

    /**
     * Constructs an apiApp Object.
     */
    public ApiApp() {
        root = new VBox();
    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Label imageUrlLabel = new Label("Image URL:");
        TextField imageUrlField = new TextField("https://...");
        Label thresholdLabel = new Label("Minimum Score:");
        TextField thresholdField = new TextField("7.5");
        HBox thresholdBox = new HBox(10, thresholdLabel, thresholdField);
        HBox imageInputBox = new HBox(10, imageUrlLabel, imageUrlField);
        ImageView selectedImageView = new ImageView();
        selectedImageView.setFitWidth(300);
        selectedImageView.setPreserveRatio(true);
        ImageView coverImageView = new ImageView();
        coverImageView.setFitWidth(200);
        coverImageView.setPreserveRatio(true);
        Label animeTitleLabel = new Label("Anime Title: ");
        Label scoreLabel = new Label("Score: ");
        Label verdictLabel = new Label("Verdict: ");
        VBox resultBox = new VBox(5, animeTitleLabel, scoreLabel, verdictLabel);
        Button submitButton = new Button("Check if it's worth your time: ");
        submitButton.setOnAction(e -> {
            try {
                String imageUrl = imageUrlField.getText().trim();
                double threshold = Double.parseDouble(thresholdField.getText().trim());

                new Thread(() -> {
                    try {
                        Image img = new Image(imageUrl, true);
                        Platform.runLater(() -> selectedImageView.setImage(img));
                    } catch (Exception imgFail) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setTitle("Image Error");
                            alert.setHeaderText("Kyaa~ Image loading failed!");
                            alert.setContentText("Onii-chan, I couldn't load that image... " +
                                imgFail.getMessage());
                            alert.showAndWait();
                        });
                    }
                }).start();
                traceMoe(imageUrl, animeTitleLabel,
                    scoreLabel, verdictLabel, threshold, coverImageView);
            } catch (Exception invalidInput) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Kyaa~ Something's wrong!");
                alert.setContentText("Senpai, that doesn't look like a valid image URL... " +
                    invalidInput.getMessage());
                alert.showAndWait();
            }
        });
        setupLayout(root, imageInputBox, selectedImageView, coverImageView,
            thresholdBox, submitButton, resultBox);

        scene = new Scene(root, 700, 700);
        stage.setTitle("Is This Anime Worth Your Time? ");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
    } //start

    /**
     * Uses AniList GraphQL API to get anime metadata.
     * @param anilistId       the AniList ID of the anime
     * @param titleLabel      label to display the anime title
     * @param scoreLabel      label to display the average score from aniList
     * @param verdictLabel    label to display the verdict ("Kino" or "Slop")
     * @param threshold       minimum score threshold for "Kino" or an anime worth your time
     * @param coverImageView  image view to display the official anime cover image
     */
    public void aniList(int anilistId, Label titleLabel, Label scoreLabel,
                        Label verdictLabel, double threshold, ImageView coverImageView) {

        new Thread(() -> {
            try {
                String query =
                    "{ \"query\": \"query ($id: Int) { Media(id: $id, type: ANIME) " +
                    "{ title { romaji english native } averageScore coverImage { large } } }\", " +
                    "\"variables\": { \"id\": " + anilistId + " } }";

                java.net.URL url = new java.net.URL("https://graphql.anilist.co");
                java.net.HttpURLConnection conn =
                    (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                java.io.OutputStream os = conn.getOutputStream();
                byte[] input = query.getBytes("utf-8");
                os.write(input, 0, input.length);

                java.io.BufferedReader in =
                    new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(),
                        "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line.trim());
                }
                in.close();

                Gson gson = new Gson();
                AniListResponse data = gson.fromJson(response.toString(), AniListResponse.class);
                String title = data.data.media.title.english != null
                    ? data.data.media.title.english
                    : data.data.media.title.romaji;
                double score = data.data.media.averageScore / 10.0;
                String coverUrl = data.data.media.coverImage.large;

                updateVerdictUi(title, score, threshold, titleLabel, scoreLabel,
                    verdictLabel, coverImageView, coverUrl);

            } catch (Exception eee) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("AniList Error");
                    alert.setHeaderText("Uwaah~ Couldn't get anime info!");
                    alert.setContentText("Senpai, AniList didn't respond properly... " +
                        eee.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    /**
     * Calls Trace.moe API to identify anime from image URL.
     * @param imageUrl         the URL of the image to identify
     * @param animeTitleLabel  label to display the anime title
     * @param scoreLabel       label to display the average score
     * @param verdictLabel     label to display the verdict ("Kino" or "Slop")
     * @param threshold        minimum score threshold to consider the anime worth watching
     * @param coverImageView   image view to display the anime's official cover art
     */
    public void traceMoe(String imageUrl, Label animeTitleLabel, Label scoreLabel,
                         Label verdictLabel, double threshold, ImageView coverImageView) {

        new Thread(() -> {
            try {
                String apiUrl = "https://api.trace.moe/search?url=" +
                    java.net.URLEncoder.encode(imageUrl, "UTF-8");

                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream())
                );

                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Gson gson = new Gson();
                TraceMoeResponse data = gson.fromJson(response.toString(), TraceMoeResponse.class);
                TraceMoeResponse.Result topResult = data.result[0];

                int anilistId = topResult.anilist;

                aniList(anilistId, animeTitleLabel, scoreLabel,
                    verdictLabel, threshold, coverImageView);

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("TraceMoe Error");
                    alert.setHeaderText("Nyaa~ TraceMoe had trouble!");
                    alert.setContentText("Ehh!? I couldn't identify the anime... " +
                        e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    } //traceMoe

    /**
     * Adds UI elements to the root layout.
     *
     * @param root              the main vertical layout container for the scene
     * @param imageInputBox     the HBox containing the image URL input field
     * @param selectedImageView the ImageView showing the user-provided screenshot
     * @param coverImageView    the ImageView showing the anime's official cover art
     * @param thresholdBox      the HBox containing the threshold input
     * @param submitButton      the button to trigger the anime analysis
     * @param resultBox         the VBox displaying the title, score, and verdict
     */
    private void setupLayout(VBox root, HBox imageInputBox, ImageView selectedImageView,
                             ImageView coverImageView, HBox thresholdBox,
                             Button submitButton, VBox resultBox) {

        root.setSpacing(10);
        root.getChildren().addAll(
            imageInputBox,
            selectedImageView,
            coverImageView,
            thresholdBox,
            submitButton,
            resultBox
        );
    } //setupLayout

    /**
     * Updates the GUI with anime title, score, verdict, and cover image.
     *
     * @param title            the title of the anime
     * @param score            the average score of the anime
     * @param threshold        the minimum score threshold for determining the verdict
     * @param titleLabel       label to display the anime title
     * @param scoreLabel       label to display the score
     * @param verdictLabel     label to display the verdict ("Kino" or "Slop")
     * @param coverImageView   image view to display the anime's official cover art
     * @param coverUrl         URL of the anime's cover image
     */
    private void updateVerdictUi(String title, double score, double threshold,
                                 Label titleLabel, Label scoreLabel,
                                 Label verdictLabel, ImageView coverImageView,
                                 String coverUrl) {

        Platform.runLater(() -> {
            titleLabel.setText("Anime Title: " + title);
            scoreLabel.setText("Score: " + score);
            if (score >= threshold) {
                verdictLabel.setText("Verdict: Senpai, It's Kino!!");
            } else {
                verdictLabel.setText("Verdict: Ehh?! It's Slop Senpai");
            }
            coverImageView.setImage(new Image(coverUrl, true));
        });
    }

} // ApiApp
