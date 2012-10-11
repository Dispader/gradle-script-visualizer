package com.nurflugel.gradle.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA. User: douglas_bullard Date: 10/10/12 Time: 19:28 To change this template use File | Settings | File
 * Templates.
 */
public class GradleVisualizerUiController implements Initializable
{
  @FXML
  private Button selectScriptButton;
  @FXML
  private Button quitButton;

  @FXML
  private Button generateGraphButton;
  @FXML
  private CheckBox deleteDotFilesCheckbox;
  @FXML
  private CheckBox watchFilesCheckbox;
  @FXML
  private CheckBox groupByFilesCheckbox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
  }

  public void selectScriptsClickedAction(ActionEvent event)
  {
    System.out.println("GradleVisualizerUiController.selectScriptsClickedAction");
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
