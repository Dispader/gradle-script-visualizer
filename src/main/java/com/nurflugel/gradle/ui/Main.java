package com.nurflugel.gradle.ui;

import com.nurflugel.gradle.ui.dialog.ConfigurationChoiceDialog;
import com.nurflugel.gradle.ui.dialog.ConfigurationsDialogBuilder;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ArrayList;
import static com.nurflugel.util.Util.VERSION;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Os.findOs;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static javafx.fxml.FXMLLoader.load;

public class Main extends Application
{
  public static final String TITLE_TEXT = "Gradle Script Visualizer v";

  /** @param  args  the command line arguments */
  public static void main(String... args)
  {
    System.out.println("Main.main");
    launch(Main.class, (String[]) null);
  }

  @Override
  public void start(Stage primaryStage)
  {
    System.out.println("Main.start");

    try
    {
      // UI related stuff
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

      ConfigurationChoiceDialog dialog = new ConfigurationsDialogBuilder().create().setOwner(primaryStage).setTitle("Select a configuration to graph")
                                                                          .addOkButton().addConfigurations(new ArrayList<Configuration>()).build();

      dialog.show();
      System.out.println("Main.start7");
    }
    catch (Exception ex)
    {
      getLogger(Main.class.getName()).log(SEVERE, null, ex);
    }
  }
}
