package com.nurflugel.gradle.ui.dialog;

import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import com.nurflugel.util.dependencyvisualizer.output.DependencyDotFileGenerator;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.collections.ObservableList;

import javafx.concurrent.Task;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Insets;

import static javafx.geometry.Pos.BOTTOM_CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;

import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.*;

import static javafx.stage.Modality.APPLICATION_MODAL;

import static javafx.stage.StageStyle.UTILITY;

import javafx.stage.Window;

import java.net.URL;

import java.util.List;

@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "ReturnOfThis" })
/** Dialog builder.
 * todo - this is nice for dynamic dialogs, but move it to SceneBuilder for heavy lifting, it's a pain to get the UI right this way
 *
 */
public class ConfigurationsDialogBuilder
{
  private static final int           BUTTON_WIDTH               = 60;
  private static final double        MARGIN                     = 10;
  private ConfigurationChoiceDialog  dialog;
  private DependencyDotFileGenerator dependencyDotFileGenerator;
  private Os                         os;
  private String                     outputFileName;
  private GradleScriptPreferences    preferences;

  public ConfigurationsDialogBuilder create(DependencyDotFileGenerator dependencyDotFileGenerator, GradleScriptPreferences preferences, Os os,
                                            String outputFileName)
  {
    this.outputFileName             = outputFileName;
    this.dependencyDotFileGenerator = dependencyDotFileGenerator;
    this.preferences                = preferences;
    this.os                         = os;
    dialog                          = new ConfigurationChoiceDialog(this);
    dialog.setResizable(true);
    dialog.initStyle(UTILITY);
    dialog.initModality(APPLICATION_MODAL);
    dialog.setIconified(false);
    dialog.centerOnScreen();
    dialog.borderPanel = BorderPaneBuilder.create().styleClass("dialog").build();
    dialog.stackPane   = new StackPane();

    StackPane stackPane = dialog.stackPane;

    dialog.log = new TextArea();

    TextArea   log         = dialog.log;
    BorderPane borderPanel = dialog.borderPanel;

    // message
    dialog.configurationsBox = new VBox();

    VBox configurationsBox = dialog.configurationsBox;

    dialog.configurationsBox = configurationsBox;
    dialog.progressIndicator = new ProgressIndicator();

    ProgressIndicator progressIndicator = dialog.progressIndicator;

    stackPane.getChildren().add(log);
    stackPane.getChildren().add(progressIndicator);
    progressIndicator.setPrefSize(50, 50);
    progressIndicator.setMaxSize(50, 50);
    configurationsBox.setSpacing(15);
    configurationsBox.setAlignment(CENTER_LEFT);
    dialog.scrollPane = new ScrollPane();

    ScrollPane scrollPane = dialog.scrollPane;

    scrollPane.setContent(configurationsBox);
    dialog.borderPanel.setCenter(stackPane);
    BorderPane.setAlignment(configurationsBox, CENTER_LEFT);
    BorderPane.setMargin(configurationsBox, new Insets(MARGIN, MARGIN, MARGIN, 2 * MARGIN));

    // buttons
    dialog.buttonsPanel = new HBox();

    final HBox buttonsPanel = dialog.buttonsPanel;

    buttonsPanel.setSpacing(MARGIN);
    buttonsPanel.setAlignment(BOTTOM_CENTER);
    BorderPane.setMargin(buttonsPanel, new Insets(0, 0, 1.5 * MARGIN, 0));
    borderPanel.setBottom(buttonsPanel);
    borderPanel.widthProperty().addListener(new ChangeListener<Number>()
      {
        public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
        {
          buttonsPanel.layout();
        }
      });
    dialog.scene = new Scene(borderPanel);
    dialog.setScene(dialog.scene);

    URL    resource     = ConfigurationsDialogBuilder.class.getResource("/com/nurflugel/gradle/ui/dialogservice/dialog.css");
    String externalForm = resource.toExternalForm();

    // dialog.borderPanel.styleClass("dialog");
    dialog.getScene().getStylesheets().add(externalForm);

    return this;
  }

  public ConfigurationsDialogBuilder setOwner(Window owner)
  {
    if (owner != null)
    {
      dialog.initOwner(owner);
      dialog.borderPanel.setMaxWidth(owner.getWidth());
      dialog.borderPanel.setMaxHeight(owner.getHeight());
    }
    else
    {
      dialog.setWidth(1000);
      dialog.setHeight(700);
    }

    return this;
  }

  public ConfigurationsDialogBuilder setTitle(String title)
  {
    dialog.setTitle(title);

    return this;
  }

  public ConfigurationsDialogBuilder addOkButton()
  {
    dialog.okButton = new Button("OK");
    dialog.okButton.setPrefWidth(BUTTON_WIDTH * 2);
    dialog.okButton.setOnAction(new EventHandler<ActionEvent>()
      {
        public void handle(ActionEvent t)
        {
          dialog.chosenConfiguration = getChosenConfiguration();
          dependencyDotFileGenerator.generateOutputForConfigurations(preferences, dialog.chosenConfiguration, outputFileName, os);
        }
      });
    dialog.buttonsPanel.getChildren().add(dialog.okButton);

    return this;
  }

  private Configuration getChosenConfiguration()
  {
    ObservableList<Node> children = dialog.configurationsBox.getChildren();

    for (Node child : children)
    {
      if (child instanceof RadioButton)
      {
        RadioButton radioButton = (RadioButton) child;

        if (radioButton.isSelected())
        {
          String              text           = radioButton.getText();
          List<Configuration> configurations = dialog.configurations;

          for (Configuration configuration : configurations)
          {
            String name = configuration.getName();

            if (name.equals(text))
            {
              return configuration;
            }
          }
        }
      }
    }

    return null;
  }

  protected ConfigurationsDialogBuilder addConfirmationButton(String buttonCaption, final EventHandler actionHandler)
  {
    Button confirmationButton = new Button(buttonCaption);

    confirmationButton.setMinWidth(BUTTON_WIDTH);
    confirmationButton.setOnAction(new EventHandler<ActionEvent>()
      {
        public void handle(ActionEvent t)
        {
          dialog.close();

          if (actionHandler != null)
          {
            actionHandler.handle(t);
          }
        }
      });
    dialog.buttonsPanel.getChildren().add(confirmationButton);

    return this;
  }

  /**
   * Add Cancel button to confirmation dialog.
   *
   * @param  actionHandler  action handler
   */
  public ConfigurationsDialogBuilder addCancelButton(EventHandler actionHandler)
  {
    EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>()
    {
      public void handle(ActionEvent t)
      {
        dialog.close();
      }
    };

    return addConfirmationButton("Close/Cancel", eventHandler);
  }

  /**
   * Build dialog.
   *
   * @return  dialog instance
   */
  public ConfigurationChoiceDialog build()
  {
    dialog.buttonsPanel.getChildren().get(0).requestFocus();

    return dialog;
  }

  public ConfigurationsDialogBuilder addConfigurations(List<Configuration> configurations)
  {
    dialog.setTitle("Choose a configuration to graph");
    dialog.configurations = configurations;
    dialog.borderPanel.setCenter(dialog.scrollPane);

    ObservableList<Node> children    = dialog.configurationsBox.getChildren();
    ToggleGroup          toggleGroup = new ToggleGroup();

    for (Configuration configuration : configurations)
    {
      String      name                     = configuration.getName();
      RadioButton configurationRadioButton = new RadioButton(name);

      configurationRadioButton.setToggleGroup(toggleGroup);

      if (name.equals("compile"))
      {
        configurationRadioButton.setSelected(true);  // todo select default from prefs
      }

      children.add(configurationRadioButton);
    }

    return this;
  }
}
