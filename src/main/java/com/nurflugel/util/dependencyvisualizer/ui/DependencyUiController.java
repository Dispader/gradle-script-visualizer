package com.nurflugel.util.dependencyvisualizer.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.ResourceBundle;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/6/12 Time: 18:04 To change this template use File | Settings | File Templates. */
public class DependencyUiController implements Initializable
{
  @FXML
  private Button       selectScriptButton;
  @FXML
  private Button       quitButton;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {}

  public void selectClickedAction(ActionEvent event) {}

  public void quitClickedAction(ActionEvent event) {}
}
