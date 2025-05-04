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
      Button loadImageButton = new Button("Load Image");

      Label thresholdLabel = new Label("Minimum Score:");
      TextField thresholdField = new TextField("7.5");
      HBox thresholdBox = new HBox(10, thresholdLabel, thresholdField);

      HBox imageInputBox = new HBox(10, imageUrlLabel, imageUrlField, loadImageButton);

      ImageView selectedImageView = new ImageView();
      selectedImageView.setFitWidth(300);
      selectedImageView.setPreserveRatio(true);

      Button submitButton = new Button("Check if it's worth your time");

      Label animeTitleLabel = new Label("Anime Title: ");
      Label scoreLabel = new Label("Score: ");
      Label verdictLabel = new Label("Verdict: ");
      VBox resultBox = new VBox(5, animeTitleLabel, scoreLabel, verdictLabel);

      loadImageButton.setOnAction(e -> {
          try {
              String imageUrl = imageUrlField.getText().trim();
              Image img = new Image(imageUrl, true);
              selectedImageView.setImage(img);
          } catch (Exception ex) {
              System.out.println("Failed to load image: " + ex.getMessage());
          }
      });

      root.setSpacing(10);
      root.getChildren().addAll(
          
          imageInputBox,
          selectedImageView,
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


} // ApiApp
