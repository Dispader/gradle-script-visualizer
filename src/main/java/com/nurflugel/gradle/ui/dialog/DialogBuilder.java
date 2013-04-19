package com.nurflugel.gradle.ui.dialog;

import static com.nurflugel.gradle.ui.dialog.StacktraceExtractor.extract;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import static javafx.geometry.Pos.BOTTOM_CENTER;
import static javafx.geometry.Pos.CENTER;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;

import javafx.scene.layout.BorderPane;
import static javafx.scene.layout.BorderPane.setAlignment;
import static javafx.scene.layout.BorderPane.setMargin;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static javafx.stage.Modality.APPLICATION_MODAL;

import static javafx.stage.StageStyle.UTILITY;

import javafx.stage.Window;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject", "ReturnOfThis" })
/** Dialog builder. */
public class DialogBuilder
{
  private static final int    STACKTRACE_LABEL_MAXHEIGHT = 240;
  private static final int    MESSAGE_MIN_WIDTH          = 180;
  private static final int    MESSAGE_MAX_WIDTH          = 800;
  private static final int    BUTTON_WIDTH               = 60;
  private static final double MARGIN                     = 10;
  private static final String ICON_PATH                  = "/com/nurflugel/gradle/ui/dialog/";
  private Dialog              dialog;

  public DialogBuilder create()
  {
    dialog = new Dialog();
    dialog.setResizable(false);
    dialog.initStyle(UTILITY);
    dialog.initModality(APPLICATION_MODAL);
    dialog.setIconified(false);
    dialog.centerOnScreen();
    dialog.borderPanel = BorderPaneBuilder.create().styleClass("dialog").build();

    // icon
    dialog.icon = new ImageView();
    dialog.borderPanel.setLeft(dialog.icon);
    setMargin(dialog.icon, new Insets(MARGIN));

    // message
    dialog.messageBox = new VBox();
    dialog.messageBox.setAlignment(Pos.CENTER_LEFT);
    dialog.messageLabel = new Label();
    dialog.messageLabel.setWrapText(true);
    dialog.messageLabel.setMinWidth(MESSAGE_MIN_WIDTH);
    dialog.messageLabel.setMaxWidth(MESSAGE_MAX_WIDTH);
    dialog.messageBox.getChildren().add(dialog.messageLabel);
    dialog.borderPanel.setCenter(dialog.messageBox);
    setAlignment(dialog.messageBox, CENTER);
    setMargin(dialog.messageBox, new Insets(MARGIN, MARGIN, MARGIN, 2 * MARGIN));

    // buttons
    dialog.buttonsPanel = new HBox();
    dialog.buttonsPanel.setSpacing(MARGIN);
    dialog.buttonsPanel.setAlignment(BOTTOM_CENTER);
    setMargin(dialog.buttonsPanel, new Insets(0, 0, 1.5 * MARGIN, 0));
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

    URL    resource     = DialogBuilder.class.getResource("/com/nurflugel/gradle/ui/dialogservice/dialog.css");
    String externalForm = resource.toExternalForm();

    // dialog.borderPanel.styleClass("dialog");
    dialog.getScene().getStylesheets().add(externalForm);

    return this;
  }

  public DialogBuilder setOwner(Window owner)
  {
    if (owner != null)
    {
      dialog.initOwner(owner);
      dialog.borderPanel.setMaxWidth(owner.getWidth());
      dialog.borderPanel.setMaxHeight(owner.getHeight());
    }

    return this;
  }

  public DialogBuilder setTitle(String title)
  {
    dialog.setTitle(title);

    return this;
  }

  public DialogBuilder setMessage(String message)
  {
    dialog.messageLabel.setText(message);

    return this;
  }

  private void alignScrollPane()
  {
    dialog.setWidth(dialog.icon.getImage().getWidth()
                      + max(dialog.messageLabel.getWidth(),
                              dialog.stacktraceVisible ? max(dialog.stacktraceButtonsPanel.getWidth(), dialog.stackTraceLabel.getWidth())
                                                       : dialog.stacktraceButtonsPanel.getWidth()) + (5 * MARGIN));
    dialog.setHeight(max(dialog.icon.getImage().getHeight(),
                         dialog.messageLabel.getHeight() + dialog.stacktraceButtonsPanel.getHeight()
                           + (dialog.stacktraceVisible ? min(dialog.stackTraceLabel.getHeight(), STACKTRACE_LABEL_MAXHEIGHT)
                                                       : 0)) + dialog.buttonsPanel.getHeight() + (3 * MARGIN));

    if (dialog.stacktraceVisible)
    {
      dialog.scrollPane.setPrefHeight(dialog.getHeight() - dialog.messageLabel.getHeight() - dialog.stacktraceButtonsPanel.getHeight()
                                        - (2 * MARGIN));
    }

    dialog.centerOnScreen();
  }

  // NOTE: invoke once during Dialog creating
  DialogBuilder setStackTrace(Throwable t)
  {
    // view button
    dialog.viewStacktraceButton = new ToggleButton("View stacktrace");

    // copy button
    dialog.copyStacktraceButton = new Button("Copy to clipboard");
    HBox.setMargin(dialog.copyStacktraceButton, new Insets(0, 0, 0, MARGIN));
    dialog.stacktraceButtonsPanel = new HBox();
    dialog.stacktraceButtonsPanel.getChildren().addAll(dialog.viewStacktraceButton, dialog.copyStacktraceButton);
    VBox.setMargin(dialog.stacktraceButtonsPanel, new Insets(MARGIN, MARGIN, MARGIN, 0));
    dialog.messageBox.getChildren().add(dialog.stacktraceButtonsPanel);

    // stacktrace text
    dialog.stackTraceLabel = new Label();
    dialog.stackTraceLabel.widthProperty().addListener(new ChangeListener<Number>()
      {
        public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue)
        {
          alignScrollPane();
        }
      });
    dialog.stackTraceLabel.heightProperty().addListener(new ChangeListener<Number>()
      {
        public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue)
        {
          alignScrollPane();
        }
      });
    dialog.stacktrace = extract(t);
    dialog.scrollPane = new ScrollPane();
    dialog.scrollPane.setContent(dialog.stackTraceLabel);
    dialog.viewStacktraceButton.setOnAction(new EventHandler<ActionEvent>()
      {
        public void handle(ActionEvent t)
        {
          dialog.stacktraceVisible = !dialog.stacktraceVisible;

          if (dialog.stacktraceVisible)
          {
            dialog.messageBox.getChildren().add(dialog.scrollPane);
            dialog.stackTraceLabel.setText(dialog.stacktrace);
            alignScrollPane();
          }
          else
          {
            dialog.messageBox.getChildren().remove(dialog.scrollPane);

            // alignScrollPane();
            dialog.setWidth(dialog.originalWidth);
            dialog.setHeight(dialog.originalHeight);
            dialog.stackTraceLabel.setText(null);
            dialog.centerOnScreen();
          }

          dialog.messageBox.layout();
        }
      });
    dialog.copyStacktraceButton.setOnAction(new EventHandler<ActionEvent>()
      {
        public void handle(ActionEvent t)
        {
          Clipboard               clipboard = Clipboard.getSystemClipboard();
          Map<DataFormat, Object> map       = new HashMap<DataFormat, Object>();

          map.put(DataFormat.PLAIN_TEXT, dialog.stacktrace);
          clipboard.setContent(map);
        }
      });
    dialog.showingProperty().addListener(new ChangeListener<Boolean>()
      {
        public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue)
        {
          if (newValue)
          {
            dialog.originalWidth  = dialog.getWidth();
            dialog.originalHeight = dialog.getHeight();
          }
        }
      });

    return this;
  }

  protected void setIconFromResource(String resourceName)
  {
    try(InputStream resourceAsStream = getClass().getResourceAsStream(resourceName))
    {
      Image image = new Image(resourceAsStream);

      dialog.icon.setImage(image);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected DialogBuilder setWarningIcon()
  {
    setIconFromResource(ICON_PATH + "warningIcon.png");

    return this;
  }

  protected DialogBuilder setErrorIcon()
  {
    setIconFromResource(ICON_PATH + "errorIcon.png");

    return this;
  }

  protected DialogBuilder setThrowableIcon()
  {
    setIconFromResource(ICON_PATH + "bugIcon.png");

    return this;
  }

  protected DialogBuilder setInfoIcon()
  {
    setIconFromResource(ICON_PATH + "infoIcon.png");

    return this;
  }

  protected DialogBuilder setConfirmationIcon()
  {
    setIconFromResource(ICON_PATH + "confirmationIcon.png");

    return this;
  }

  protected DialogBuilder addOkButton()
  {
    dialog.okButton = new Button("OK");
    dialog.okButton.setPrefWidth(BUTTON_WIDTH * 2);
    dialog.okButton.setOnAction(new EventHandler<ActionEvent>()
      {
        public void handle(ActionEvent t)
        {
          dialog.close();
        }
      });
    dialog.buttonsPanel.getChildren().add(dialog.okButton);

    return this;
  }

  protected DialogBuilder addConfirmationButton(String buttonCaption, final EventHandler actionHandler)
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
   * Add Yes button to confirmation dialog.
   *
   * @param  actionHandler  action handler
   */
  public DialogBuilder addYesButton(EventHandler actionHandler)
  {
    return addConfirmationButton("Yes", actionHandler);
  }

  /**
   * Add No button to confirmation dialog.
   *
   * @param  actionHandler  action handler
   */
  public DialogBuilder addNoButton(EventHandler actionHandler)
  {
    return addConfirmationButton("No", actionHandler);
  }

  /**
   * Add Cancel button to confirmation dialog.
   *
   * @param  actionHandler  action handler
   */
  public DialogBuilder addCancelButton(EventHandler actionHandler)
  {
    return addConfirmationButton("Cancel", actionHandler);
  }

  /**
   * Build dialog.
   *
   * @return  dialog instance
   */
  public Dialog build()
  {
    if (dialog.buttonsPanel.getChildren().isEmpty())
    {
      throw new RuntimeException("Add one dialog button at least");
    }

    dialog.buttonsPanel.getChildren().get(0).requestFocus();

    return dialog;
  }
}
