package com.nurflugel.gradle.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static javafx.fxml.FXMLLoader.load;

public class Main extends Application
{
  /** @param  args  the command line arguments */
  public static void main(String... args)
  {
    launch(Main.class, (String[]) null);
  }

  @Override
  public void start(Stage primaryStage)
  {
    try
    {
      URL resource = Main.class.getResource("GradleVisualizerUi.fxml");
      AnchorPane page  = (AnchorPane) load(resource);
      Scene scene = new Scene(page);

      primaryStage.setScene(scene);
      primaryStage.setTitle("Gradle Visualizer");
      primaryStage.show();
    }
    catch (Exception ex)
    {
      getLogger(Main.class.getName()).log(SEVERE, null, ex);
    }
  }
}
