package com.nurflugel.util.dependencyvisualizer.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/6/12 Time: 18:05 To change this template use File | Settings | File Templates. */
public class DependencyUiMain extends Application
{
  /** @param  args  the command line arguments */
  public static void main(String[] args)
  {
    Application.launch(DependencyUiMain.class, (String[]) null);
  }

  @Override
  public void start(Stage primaryStage)
  {
    try
    {
      AnchorPane page  = (AnchorPane) FXMLLoader.load(DependencyUiMain.class.getResource("DependencyUi.fxml"));
      Scene      scene = new Scene(page);

      primaryStage.setScene(scene);
      primaryStage.setTitle("Gradle Dependency Visualizer");
      primaryStage.show();
    }
    catch (Exception ex)
    {
      getLogger(DependencyUiMain.class.getName()).log(SEVERE, null, ex);
    }
  }
}
