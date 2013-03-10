package com.nurflugel.gradle.ui.dialog;

import com.nurflugel.util.dependencyvisualizer.domain.Configuration;

import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;

import org.apache.commons.collections.buffer.BoundedFifoBuffer;

import java.util.ArrayList;
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
  protected TextArea                  log;
  protected StackPane                 stackPane;
  private List<String>                logLines                    = new ArrayList<>();
  public static final int             PIXELS_PER_ROW              = 15;

  public ConfigurationChoiceDialog(ConfigurationsDialogBuilder configurationsDialogBuilder)
  {
    this.configurationsDialogBuilder = configurationsDialogBuilder;
  }

  public void addLineToDisplay(String text)
  {
    StringBuilder builder      = new StringBuilder();
    int           prefRowCount = (int) (log.getHeight() / PIXELS_PER_ROW);

    if ((logLines.size() == prefRowCount) && !logLines.isEmpty())
    {
      logLines.remove(0);
    }

    logLines.add(text);

    for (Object logLine : logLines)
    {
      builder.append(logLine).append('\n');
    }

    log.setText(builder.toString());
  }

  public ProgressIndicator getProgressIndicator()
  {
    return progressIndicator;
  }

  public ConfigurationsDialogBuilder getConfigurationsDialogBuilder()
  {
    return configurationsDialogBuilder;
  }
}
