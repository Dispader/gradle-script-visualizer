package com.nurflugel.gradle.ui;

import com.nurflugel.gradle.ui.dialog.Dialog;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Os.findOs;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/10/12 Time: 19:28 To change this template use File | Settings | File Templates. */
public class GradleVisualizerUiController implements Initializable
{
  private GradleScriptPreferences preferences;
  private Os                      os;
  private GradleFileParser        parser;
  @FXML
  private Button                  selectScriptButton;
  @FXML
  private Button                  quitButton;
  @FXML
  private Button                  generateScriptGraphButton;
  @FXML
  private Button                  generateDependencyGraphButton;
  @FXML
  private CheckBox                deleteDotFilesCheckbox;
  @FXML
  private CheckBox                shouldIncludeImportedFilesCheckbox;
  @FXML
  private CheckBox                watchFilesCheckbox;
  @FXML
  private CheckBox                groupByFilesCheckbox;
  private File                    gradleFile;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
    preferences = new GradleScriptPreferences();
    os          = findOs();
    parser      = new GradleFileParser(preferences, os);
  }

  public void groupByBuildFileClicked(ActionEvent event)
  {
    System.out.println("GradleVisualizerUiController.groupByBuildFileClicked");
    saveCheckboxSettings();
    // reparse scripts
  }

  public void generateScriptGraphButtonClicked(ActionEvent event)
  {
    System.out.println("GradleVisualizerUiController.selectGenerateScriptGraphButton");
  }

  public void generateDependencyGraphButtonClicked(ActionEvent event)
  {
    System.out.println("GradleVisualizerUiController.generateDependencyGraphButtonClicked");
  }

  public void saveCheckboxSettings()
  {
    System.out.println("GradleVisualizerUiController.saveCheckboxSettings");
    preferences.setWatchFilesForChanges(watchFilesCheckbox.isSelected());
    preferences.setShouldDeleteDotFilesOnExit(deleteDotFilesCheckbox.isSelected());
    preferences.setShouldGroupByBuildFiles(groupByFilesCheckbox.isSelected());
    preferences.setShouldIncludeImportedFiles(shouldIncludeImportedFilesCheckbox.isSelected());
    // preferences.setUseHttpProxy();
    // preferences.setUseProxyAuthentication();
    // preferences.setProxyServerName();
    // preferences.setProxyServerPort();
    // preferences.setProxyUserName();
    // preferences.setProxyPassword();
  }

  public void selectScriptsClickedAction(ActionEvent event)
  {
    FileChooser fileChooser = new FileChooser();
    File        lastDir     = new File(preferences.getLastDir());

    if ((lastDir != null) && lastDir.exists())
    {
      fileChooser.setInitialDirectory(lastDir);
    }

    File file = fileChooser.showOpenDialog(null);

    if (file != null)
    {
      gradleFile = file;
      preferences.setLastDir(file.getParent());
      generateDependencyGraphButton.setDisable(false);
      generateScriptGraphButton.setDisable(false);
      selectScriptButton.setDefaultButton(false);
    }
    else
    {
      Dialog.showError("No file selected", "Nothing will happen until you select a file first");
    }
    // examples of dialogs - one with a handler, one without Dialog.showError("You're fucked", "Bend over and smile"); EventHandler actionHandler =
    // new EventHandler() { @Override public void handle(Event event) { System.out.println("GradleVisualizerUiController.handle"); } };
    // Dialog.buildConfirmation("you need to OK this", "Think about what you're
    // doing").addYesButton(actionHandler).addNoButton(actionHandler).build().show();
  }

  public void quitClickedAction(ActionEvent event)
  {
    System.exit(0);
  }

  public static void main(String[] args)
  {
    GradleVisualizerUiController controller = new GradleVisualizerUiController();

    controller.initialize(null, null);
  }
}
