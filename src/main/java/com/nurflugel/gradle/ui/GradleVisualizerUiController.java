package com.nurflugel.gradle.ui;

import com.nurflugel.gradle.ui.dialog.Dialog;
import static com.nurflugel.gradle.ui.dialog.Dialog.showError;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Os.findOs;
import com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.net.URL;

import java.util.ResourceBundle;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/10/12 Time: 19:28 To change this template use File | Settings | File Templates. */
public class GradleVisualizerUiController implements Initializable
{
  private GradleScriptPreferences preferences;
  private Os                      os;
  private GradleFileParser        scriptParser;
  private GradleDependencyParser  dependencyParser;
  @FXML
  private Button                  quitButton;
  @FXML
  private Button                  quitButton2;
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
  @FXML
  private CheckBox                showGradleTaskDependenciesCheckbox;
  @FXML
  private CheckBox                useHttpProxyCheckbox;
  @FXML
  private CheckBox                useHttpProxyAuthenticationCheckbox;
  @FXML
  private TextField               proxyServerNameField;
  @FXML
  private TextField               proxyServerPortField;
  @FXML
  private TextField               proxyUserNameField;
  @FXML
  private PasswordField           proxyPasswordField;
  @FXML
  private GridPane                proxyServerPane;
  @FXML
  private HBox                    proxyBox;
  @FXML
  private VBox                    userBox;
  @FXML
  private TabPane                 tabPane;
  @FXML
  private CheckBox                concentrateScriptLinesCheckbox;
  @FXML
  private CheckBox                concentrateDependencyLinesCheckbox;
  @FXML
  private CheckBox                justUseCompileConfigCheckbox;

  // --------------------------- main() method ---------------------------
  public static void main(String[] args)
  {
    GradleVisualizerUiController controller = new GradleVisualizerUiController();

    controller.initialize(null, null);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
    preferences      = new GradleScriptPreferences();
    os               = findOs();
    scriptParser     = new GradleFileParser(preferences, os);
    dependencyParser = new GradleDependencyParser();
    initializeUiFromSettings();

    boolean                   selectSecondTab = preferences.getSelectSecondTab();
    SingleSelectionModel<Tab> selectionModel  = tabPane.getSelectionModel();
    int                       tabToSelect     = selectSecondTab ? 1
                                                                : 0;

    selectionModel.select(tabToSelect);
    setDefaultButton();
  }

  // -------------------------- OTHER METHODS --------------------------
  public void generateDependencyGraphButtonClicked()
  {
    System.out.println("GradleVisualizerUiController.generateDependencyGraphButtonClicked");

    File gradleFile = selectScriptsClickedAction();

    try
    {
      dependencyParser.parseDependencies(os, gradleFile, preferences);
    }
    catch (Exception e)
    {
      Dialog.showThrowable("Error parsing dependencies", "Something bad happened, here's the stack trace", e);
    }
  }

  public void generateScriptGraphButtonClicked()
  {
    File gradleFile = selectScriptsClickedAction();

    scriptParser.purgeAll();

    try
    {
      saveSettings();
      scriptParser.beginOnFile(watchFilesCheckbox.isSelected(), showGradleTaskDependenciesCheckbox.isSelected(), gradleFile);
    }
    catch (IOException e)
    {
      Dialog.showThrowable("Error parsing files", "Something bad happened, here's the stack trace", e);
    }
  }

  // todo as tab selection changes, let the file chooser select multiple or single Gradle files
  public void groupByBuildFileClicked(ActionEvent event)
  {
    System.out.println("GradleVisualizerUiController.groupByBuildFileClicked");
    saveSettings();
    // reparse scripts
  }

  public void saveSettings()
  {
    System.out.println("GradleVisualizerUiController.saveSettings");

    if (areAllNotNull(preferences, watchFilesCheckbox, deleteDotFilesCheckbox, groupByFilesCheckbox, shouldIncludeImportedFilesCheckbox,
                        useHttpProxyAuthenticationCheckbox, useHttpProxyCheckbox, proxyServerNameField, proxyServerPortField, proxyUserNameField,
                        proxyPasswordField, tabPane, justUseCompileConfigCheckbox, showGradleTaskDependenciesCheckbox))
    {
      preferences.setShowGradleTaskDependencies(showGradleTaskDependenciesCheckbox.isSelected());
      preferences.setWatchFilesForChanges(watchFilesCheckbox.isSelected());
      preferences.setShouldDeleteDotFilesOnExit(deleteDotFilesCheckbox.isSelected());
      preferences.setShouldGroupByBuildFiles(groupByFilesCheckbox.isSelected());
      preferences.setShouldIncludeImportedFiles(shouldIncludeImportedFilesCheckbox.isSelected());
      preferences.setUseHttpProxy(useHttpProxyCheckbox.isSelected());
      preferences.setUseProxyAuthentication(useHttpProxyAuthenticationCheckbox.isSelected());
      preferences.setProxyServerName(proxyServerNameField.getText());
      preferences.setShouldConcentrateScriptLines(concentrateScriptLinesCheckbox.isSelected());
      preferences.setShouldConcentrateDependencyLines(concentrateDependencyLinesCheckbox.isSelected());
      preferences.setShouldJustUseCompileConfig(justUseCompileConfigCheckbox.isSelected());

      String text = proxyServerPortField.getText();

      try
      {
        preferences.setProxyServerPort(Integer.parseInt(text));
      }
      catch (NumberFormatException e)
      {
        System.out.println("GradleVisualizerUiController.saveSettings - couldn't parse a port number out of \"" + text + '"');
      }

      preferences.setProxyUserName(proxyUserNameField.getText());
      preferences.setProxyPassword(proxyPasswordField.getText());
      preferences.setSelectSecondTab(tabPane.getTabs().get(1).isSelected());
    }
  }

  /** check to see if everything's been instantiated yet, as it seems that events are being called before everything's instantiated. */
  static boolean areAllNotNull(Object... components)
  {
    for (Object component : components)
    {
      if (component == null)
      {
        return false;
      }
    }

    return true;
  }

  public void saveAndInitializeUi()
  {
    saveSettings();
    initializeUiFromSettings();
  }

  private void initializeUiFromSettings()
  {
    if (preferences != null)
    {
      CheckBox checkbox = watchFilesCheckbox;
      boolean  value    = preferences.watchFilesForChanges();

      setCheckboxFromSettings(checkbox, value);
      setCheckboxFromSettings(deleteDotFilesCheckbox, preferences.shouldDeleteDotFilesOnExit());
      setCheckboxFromSettings(groupByFilesCheckbox, preferences.shouldGroupByBuildfiles());
      setCheckboxFromSettings(shouldIncludeImportedFilesCheckbox, preferences.shouldIncludeImportedFiles());
      setCheckboxFromSettings(useHttpProxyCheckbox, preferences.shouldUseHttpProxy());
      setCheckboxFromSettings(useHttpProxyAuthenticationCheckbox, preferences.shouldUseProxyAuthentication());
      setCheckboxFromSettings(concentrateDependencyLinesCheckbox, preferences.shouldConcentrateDependencyLines());
      setCheckboxFromSettings(concentrateScriptLinesCheckbox, preferences.shouldConcentrateScriptLines());
      setCheckboxFromSettings(justUseCompileConfigCheckbox, preferences.shouldJustUseCompileConfig());
      setCheckboxFromSettings(showGradleTaskDependenciesCheckbox, preferences.showGradleTaskDependencies());
      proxyServerNameField.setText(preferences.getProxyServerName());  // these override the helpful text if not empty or null
      proxyServerPortField.setText(preferences.getProxyServerPort() + "");
      proxyUserNameField.setText(preferences.getProxyUserName());
    }
  }

  /** Helper method to deal with null values upon initialization. */
  private void setCheckboxFromSettings(CheckBox checkbox, boolean value)
  {
    if (checkbox != null)
    {
      checkbox.setSelected(value);
    }
  }

  public void quitClickedAction()
  {
    saveSettings();
    System.exit(0);
  }

  public File selectScriptsClickedAction()
  {
    FileChooser fileChooser = new FileChooser();
    File        lastDir     = new File(preferences.getLastDir());

    if (lastDir.exists())
    {
      fileChooser.setInitialDirectory(lastDir);
    }

    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Gradle files (*.gradle)", "*.gradle");

    fileChooser.getExtensionFilters().add(extFilter);

    File file = fileChooser.showOpenDialog(null);

    if (file != null)
    {  // gradleFile = file;
      preferences.setLastDir(file.getParent());
      generateDependencyGraphButton.setDisable(false);
      generateScriptGraphButton.setDisable(false);

      // setDefaultButton();
      return file;
    }

    showError("No file selected", "Nothing will happen until you select a file first");

    // J- examples of dialogs - one with a handler, one without Dialog.showError("You're fucked", "Bend over and smile");
    //
    // EventHandler actionHandler = new EventHandler() { @Override public void handle(Event event) {
    // System.out.println("GradleVisualizerUiController.handle"); } }; Dialog.buildConfirmation("you need to OK this", "Think about what you're
    // doing").addYesButton(actionHandler).addNoButton(actionHandler).build() .show();  J+
    return null;
  }

  private void setDefaultButton()
  {
    generateScriptGraphButton.setDefaultButton(true);
    generateDependencyGraphButton.setDefaultButton(true);
  }

  public void useHttpAuthenticationBoxClicked()
  {
    System.out.println("GradleVisualizerUiController.useHttpAuthenticationBoxClicked");
    userBox.setVisible(useHttpProxyAuthenticationCheckbox.isSelected());
    saveSettings();
  }

  public void useHttpProxyBoxClicked()
  {
    System.out.println("GradleVisualizerUiController.useHttpProxyBoxClicked");

    boolean selected = useHttpProxyCheckbox.isSelected();

    proxyBox.setVisible(selected);
    useHttpProxyAuthenticationCheckbox.setVisible(selected);

    if (!selected)
    {
      setCheckboxFromSettings(useHttpProxyAuthenticationCheckbox, false);
      userBox.setVisible(false);
    }

    saveSettings();
  }
}
