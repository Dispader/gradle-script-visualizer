package com.nurflugel.gradle.ui.dialogservice;

import javafx.application.Platform;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import static javafx.concurrent.Worker.State.*;

import javafx.scene.Scene;

import javafx.scene.effect.ColorAdjustBuilder;
import javafx.scene.effect.Effect;

import javafx.scene.text.Text;

import javafx.stage.Stage;

/**
 * A {@linkplain Service} for showing and hiding a {@linkplain Stage}.
 *
 * <p>Not used in the app, here for reference and future use</p>
 */
public class DialogService extends Service<Void>
{
  private final Stage         window;
  private final Stage         parent;
  private final Effect        origEffect;
  private final Service<Void> submitService;

  /**
   * Creates a dialog service for showing and hiding a {@linkplain Stage}.
   *
   * @param  parent         the parent {@linkplain Stage}
   * @param  window         the window {@linkplain Stage} that will be shown/hidden
   * @param  messageHeader  the messageHeader {@linkplain Text} used for the service that will be updated with exception information as the
   *                        submitService informs the DialogService of
   * @param  submitService  the {@linkplain Service} that will be listened to for {@linkplain State#SUCCEEDED} at which point the DialogService window
   *                        {@linkplain Stage} will be hidden
   */
  protected DialogService(Stage parent, final Stage window, final Text messageHeader, final Service<Void> submitService)
  {
    this.window = window;
    this.parent = parent;
    origEffect  = hasParentSceneRoot() ? this.parent.getScene().getRoot().getEffect()
                                       : null;
    this.submitService = submitService;
    this.submitService.stateProperty().addListener(new ChangeListener<State>()
      {
        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue)
        {
          if (submitService.getException() != null)
          {  // service indicated that an error occurred
            messageHeader.setText(submitService.getException().getMessage());
          }
          else if (newValue == SUCCEEDED)
          {
            window.getScene().getRoot().setEffect(ColorAdjustBuilder.create().brightness(-0.5d).build());
            Platform.runLater(createHideTask());
          }
        }
      });
  }

  /** {@inheritDoc} */
  @Override
  protected Task<Void> createTask()
  {
    return window.isShowing() ? createHideTask()
                              : createShowTask();
  }

  /** @return  a task that will show the service {@linkplain Stage} */
  protected Task<Void> createShowTask()
  {
    Task<Void> showTask = new Task<Void>()
    {
      @Override
      protected Void call() throws Exception
      {
        Platform.runLater(new Runnable()
          {
            public void run()
            {
              if (hasParentSceneRoot())
              {
                parent.getScene().getRoot().setEffect(ColorAdjustBuilder.create().brightness(-0.5d).build());
              }

              window.show();
              window.centerOnScreen();
            }
          });

        return null;
      }
    };
    showTask.stateProperty().addListener(new ChangeListener<State>()
      {
        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue)
        {
          if ((newValue == FAILED) || (newValue == CANCELLED))
          {
            Platform.runLater(createHideTask());
          }
        }
      });

    return showTask;
  }

  /** @return  a task that will hide the service {@linkplain Stage} */
  protected Task<Void> createHideTask()
  {
    Task<Void> closeTask = new Task<Void>()
    {
      @Override
      protected Void call() throws Exception
      {
        window.hide();

        if (hasParentSceneRoot())
        {
          parent.getScene().getRoot().setEffect(origEffect);
        }

        window.getScene().getRoot().setDisable(false);

        return null;
      }
    };

    return closeTask;
  }

  /** @return  true when the parent {@linkplain Stage#getScene()} has a valid {@linkplain Scene#getRoot()} */
  private boolean hasParentSceneRoot()
  {
    return (parent != null) && (parent.getScene() != null) && (parent.getScene().getRoot() != null);
  }

  /** Hides the dialog used in the {@linkplain Service}. */
  public void hide()
  {
    Platform.runLater(createHideTask());
  }
}
