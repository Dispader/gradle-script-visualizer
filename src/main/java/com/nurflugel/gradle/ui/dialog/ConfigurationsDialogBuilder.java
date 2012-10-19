package com.nurflugel.gradle.ui.dialog;

import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import com.nurflugel.util.dependencyvisualizer.output.DependencyDotFileGenerator;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import java.net.URL;
import java.util.List;
import static javafx.geometry.Pos.BOTTOM_CENTER;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.stage.Modality.APPLICATION_MODAL;
import static javafx.stage.StageStyle.UTILITY;

@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "ReturnOfThis" })
/** Dialog builder. */
public class ConfigurationsDialogBuilder
{
  private static final int           STACKTRACE_LABEL_MAXHEIGHT = 240;
  private static final int           MESSAGE_MIN_WIDTH          = 180;
  private static final int           MESSAGE_MAX_WIDTH          = 800;
  private static final int           BUTTON_WIDTH               = 60;
  private static final double        MARGIN                     = 10;
  private static final String        ICON_PATH                  = "/com/nurflugel/gradle/ui/dialog/";
  private ConfigurationChoiceDialog  dialog;
  private DependencyDotFileGenerator dependencyDotFileGenerator;
  private List<String>               output;
  private Os                         os;
  private String                     outputFileName;

  public ConfigurationsDialogBuilder create(DependencyDotFileGenerator dependencyDotFileGenerator, List<String> output, Os os, String outputFileName)
  {
    this.outputFileName             = outputFileName;
    this.dependencyDotFileGenerator = dependencyDotFileGenerator;
    this.output                     = output;
    this.os                         = os;
    dialog                          = new ConfigurationChoiceDialog();
    dialog.setResizable(true);
    dialog.initStyle(UTILITY);
    dialog.initModality(APPLICATION_MODAL);
    dialog.setIconified(false);
    dialog.centerOnScreen();
    dialog.borderPanel = BorderPaneBuilder.create().styleClass("dialog").build();

    // message
    VBox configurationsBox = new VBox();

    dialog.configurationsBox = configurationsBox;
    configurationsBox.setSpacing(15);
    configurationsBox.setAlignment(CENTER_LEFT);
    dialog.scrollPane = new ScrollPane();
    dialog.scrollPane.setContent(dialog.configurationsBox);

    // dialog.messageLabel = new Label();
    // dialog.messageLabel.setWrapText(true);
    // dialog.messageLabel.setMinWidth(MESSAGE_MIN_WIDTH);
    // dialog.messageLabel.setMaxWidth(MESSAGE_MAX_WIDTH);
    ObservableList<Node> children = configurationsBox.getChildren();

    children.add(dialog.scrollPane);
    dialog.borderPanel.setCenter(dialog.scrollPane);
    BorderPane.setAlignment(configurationsBox, CENTER);
    BorderPane.setMargin(configurationsBox, new Insets(MARGIN, MARGIN, MARGIN, 2 * MARGIN));

    // buttons
    dialog.buttonsPanel = new HBox();
    dialog.buttonsPanel.setSpacing(MARGIN);
    dialog.buttonsPanel.setAlignment(BOTTOM_CENTER);
    BorderPane.setMargin(dialog.buttonsPanel, new Insets(0, 0, 1.5 * MARGIN, 0));
    dialog.borderPanel.setBottom(dialog.buttonsPanel);
    dialog.borderPanel.widthProperty().addListener(new ChangeListener<Number>()
      {
        public void changed(ObservableValue<? extends Number> ov, Number t, Number t1)
        {
          dialog.buttonsPanel.layout();
        }
      });
    dialog.scene = new Scene(dialog.borderPanel);
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
      dialog.setWidth(400);
      dialog.setHeight(500);
    }

    return this;
  }

  public ConfigurationsDialogBuilder setTitle(String title)
  {
    dialog.setTitle(title);

    return this;
  }

  private void alignScrollPane()
  {
    // dialog.setWidth(dialog.icon.getImage().getWidth()
    // + Math.max(dialog.messageLabel.getWidth(),
    // dialog.stacktraceVisible ? Math.max(dialog.stacktraceButtonsPanel.getWidth(), dialog.stackTraceLabel.getWidth())
    // : dialog.stacktraceButtonsPanel.getWidth()) + (5 * MARGIN));
    // dialog.setHeight(Math.max(dialog.icon.getImage().getHeight(),
    // dialog.messageLabel.getHeight() + dialog.stacktraceButtonsPanel.getHeight()
    // + (dialog.stacktraceVisible ? Math.min(dialog.stackTraceLabel.getHeight(), STACKTRACE_LABEL_MAXHEIGHT)
    // : 0)) + dialog.buttonsPanel.getHeight() + (3 * MARGIN));
    dialog.centerOnScreen();
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
          dependencyDotFileGenerator.generateOutputForConfigurations(output, dialog.chosenConfiguration, outputFileName, os);
          dialog.close();
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
    return addConfirmationButton("Cancel", actionHandler);
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
    dialog.configurations = configurations;

    ToggleGroup toggleGroup = new ToggleGroup();

    for (Configuration configuration : configurations)
    {
      RadioButton configurationRadioButton = new RadioButton(configuration.getName());

      configurationRadioButton.setToggleGroup(toggleGroup);
      dialog.configurationsBox.getChildren().add(configurationRadioButton);  // todo select default
    }

    return this;
  }
}
