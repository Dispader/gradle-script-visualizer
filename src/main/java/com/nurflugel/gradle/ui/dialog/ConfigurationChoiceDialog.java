package com.nurflugel.gradle.ui.dialog;

import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.List;

@SuppressWarnings("ProtectedField")
public class ConfigurationChoiceDialog extends Stage
{
  protected double              originalWidth;
  protected double              originalHeight;
  protected Scene               scene;
  protected VBox                configurationsBox;
  protected ScrollPane          scrollPane;
  protected HBox                buttonsPanel;
  protected Button              okButton;
  protected BorderPane          borderPanel;
  protected List<Configuration> configurations;
  protected Configuration       chosenConfiguration;

  public Configuration getChosenConfiguration()
  {
    return chosenConfiguration;
  }
}
