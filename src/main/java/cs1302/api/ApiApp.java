package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import com.google.gson.Gson;
import cs1302.api.TraceMoeResponse;
import cs1302.api.AniListResponse;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {
    
    Stage stage;
    Scene scene;
    VBox root;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

      this.stage = stage;

      Label imageUrlLabel = new Label("Image URL:");
      TextField imageUrlField = new TextField("https://..."); // placeholder URL
      

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
                System.out.println("Failed to load image: " + imgFail.getMessage());
              }
            }).start();
            
              traceMoe(imageUrl, animeTitleLabel, scoreLabel, verdictLabel, threshold, coverImageView);
                
                } catch (Exception invalidInput) {
              System.out.println("Invalid Image URL: " + invalidInput.getMessage());
             }//try-catch
            });
       
      root.setSpacing(10);
      root.getChildren().addAll(
          
          imageInputBox,
          selectedImageView,
          coverImageView,
          thresholdBox,
          submitButton,
          resultBox
      );

      scene = new Scene(root, 700, 700);
      stage.setTitle("Is This Anime Worth Your Time?");
      stage.setScene(scene);
      stage.setOnCloseRequest(event -> Platform.exit());
      stage.sizeToScene();
      stage.show();
    } //start

    public void aniList(int anilistId, Label titleLabel, Label scoreLabel, Label verdictLabel, double threshold, ImageView coverImageView) {
    new Thread(() -> {
        try {
            String query = "{ \"query\": \"query ($id: Int) { Media(id: $id, type: ANIME) { title { romaji english native } averageScore coverImage {                                 large } } }\", " +
                          "\"variables\": { \"id\": " + anilistId + " } }";


            java.net.URL url = new java.net.URL("https://graphql.anilist.co");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            java.io.OutputStream os = conn.getOutputStream();
            byte[] input = query.getBytes("utf-8");
            os.write(input, 0, input.length);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line.trim());
            }
            in.close();

            Gson gson = new Gson();
            AniListResponse data = gson.fromJson(response.toString(), AniListResponse.class);
            String title = data.data.Media.title.english != null ? data.data.Media.title.english : data.data.Media.title.romaji;
            double score = data.data.Media.averageScore / 10.0;
            
            String coverUrl = data.data.Media.coverImage.large;


            Platform.runLater(() -> {
                titleLabel.setText("Anime Title: " + title);
                scoreLabel.setText("Score: " + score);
                if (score >= threshold) {
                    verdictLabel.setText("Verdict: Kino!!");
                } else {
                    verdictLabel.setText("Verdict: Slop :(");
                }
                coverImageView.setImage(new Image(coverUrl, true));
            });

        } catch (Exception eee) {
            eee.printStackTrace(); //this is a surprise tool that'll help us later
        }
    }).start();
}

    
    public void traceMoe(String imageUrl, Label animeTitleLabel, Label scoreLabel, Label verdictLabel, double threshold, ImageView coverImageView) {
      new Thread(() -> {
          try {
              String apiUrl = "https://api.trace.moe/search?url=" + java.net.URLEncoder.encode(imageUrl, "UTF-8");

              java.net.URL url = new java.net.URL(apiUrl);
              java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
              conn.setRequestMethod("GET");
  
              java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
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

              aniList(anilistId, animeTitleLabel, scoreLabel, verdictLabel, threshold, coverImageView);
            
          } catch (Exception e) {
              e.printStackTrace();
          }
      }).start();
    } //traceMoe
    
    
} // ApiApp
