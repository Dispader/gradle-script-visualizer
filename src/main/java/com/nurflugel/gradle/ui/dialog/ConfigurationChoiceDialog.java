package com.nurflugel.gradle.ui.dialog;

import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

@SuppressWarnings("ProtectedField")
public class ConfigurationChoiceDialog extends Stage
{
  protected double                    originalWidth               = 400;
  protected double                    originalHeight              = 600;
  protected Scene                     scene;
  protected VBox                      configurationsBox;
  protected ScrollPane                scrollPane;
  protected HBox                      buttonsPanel;
  protected Button                    okButton;
  protected BorderPane                borderPanel;
  protected List<Configuration>       configurations;
  protected Configuration             chosenConfiguration;
  protected ProgressIndicator         progressIndicator;
  private ConfigurationsDialogBuilder configurationsDialogBuilder;

  public ConfigurationChoiceDialog(ConfigurationsDialogBuilder configurationsDialogBuilder)
  {
    this.configurationsDialogBuilder = configurationsDialogBuilder;
  }

  public void addLineToDisplay(String resultLine) {}

  public ProgressIndicator getProgressIndicator()
  {
    return progressIndicator;
  }

  public ConfigurationsDialogBuilder getConfigurationsDialogBuilder()
  {
    return configurationsDialogBuilder;
  }
}
