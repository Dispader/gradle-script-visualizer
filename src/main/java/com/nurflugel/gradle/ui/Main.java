package com.nurflugel.gradle.ui;

import com.nurflugel.gradle.ui.dialog.ConfigurationChoiceDialog;
import com.nurflugel.gradle.ui.dialog.ConfigurationsDialogBuilder;

import static com.nurflugel.util.Util.VERSION;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;

import javafx.application.Application;

import static javafx.fxml.FXMLLoader.load;

import javafx.scene.Scene;

import javafx.scene.layout.AnchorPane;

import javafx.stage.Stage;

import java.net.URL;

import java.util.ArrayList;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;

public class Main extends Application
{
  public static final String TITLE_TEXT = "Gradle Script Visualizer v";

  /** @param  args  the command line arguments */
  public static void main(String... args)
  {
    System.out.println("Main.main");

    try
    {
      launch(Main.class, (String[]) null);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void start(Stage primaryStage)
  {
    System.out.println("Main.start");

    try
    {  // UI related stuff

      URL resource = Main.class.getResource("GradleVisualizerUi.fxml");

      System.out.println("Main.start2");

      AnchorPane page = (AnchorPane) load(resource);

      System.out.println("Main.start3");

      Scene scene = new Scene(page);

      System.out.println("Main.start4");
      primaryStage.setScene(scene);
      primaryStage.setTitle(TITLE_TEXT + VERSION);
      System.out.println("Main.start5");
      primaryStage.show();
      System.out.println("Main.start6");
    }
    catch (Exception ex)
    {
      getLogger(Main.class.getName()).log(SEVERE, null, ex);
    }
  }
}
