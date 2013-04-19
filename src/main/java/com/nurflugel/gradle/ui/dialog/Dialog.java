package com.nurflugel.gradle.ui.dialog;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;

import javafx.scene.image.ImageView;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import javafx.stage.Window;

import static java.lang.Math.max;
import static java.lang.Math.min;

/** @author  Anton Smirnov (dev@antonsmirnov.name) */
@SuppressWarnings("ProtectedField")
public class Dialog extends Stage
{
  protected String       stacktrace;
  protected double       originalWidth;
  protected double       originalHeight;
  protected Scene        scene;
  protected BorderPane   borderPanel;
  protected ImageView    icon;
  protected VBox         messageBox;
  protected Label        messageLabel;
  protected boolean      stacktraceVisible;
  protected HBox         stacktraceButtonsPanel;
  protected ToggleButton viewStacktraceButton;
  protected Button       copyStacktraceButton;
  protected ScrollPane   scrollPane;
  protected Label        stackTraceLabel;
  protected HBox         buttonsPanel;
  protected Button       okButton;

  /**
   * Show information dialog box as parentWindow child.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   * @param  owner    parent window
   */
  public static void showInfo(String title, String message, Window owner)
  {
    new DialogBuilder().create().setOwner(owner).setTitle(title).setInfoIcon().setMessage(message).addOkButton().build().show();
  }

  /**
   * Show information dialog box as parentStage child.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   */
  public static void showInfo(String title, String message)
  {
    showInfo(title, message, null);
  }

  /**
   * Show warning dialog box as parentStage child.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   * @param  owner    parent window
   */
  public static void showWarning(String title, String message, Window owner)
  {
    new DialogBuilder().create().setOwner(owner).setTitle(title).setWarningIcon().setMessage(message).addOkButton().build().show();
  }

  /**
   * Show warning dialog box.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   */
  public static void showWarning(String title, String message)
  {
    showWarning(title, message, null);
  }

  /**
   * Show error dialog box.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   * @param  owner    parent window
   */
  public static void showError(String title, String message, Window owner)
  {
    new DialogBuilder().create().setOwner(owner).setTitle(title).setErrorIcon().setMessage(message).addOkButton().build().show();
  }

  /**
   * Show error dialog box.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   */
  public static void showErrorDialog(String title, String message)
  {
    showError(title, message, null);
  }

  /**
   * Show error dialog box with stacktrace.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   * @param  t        throwable
   * @param  owner    parent window
   */
  public static void showThrowable(String title, String message, Throwable t, Window owner)
  {
    new DialogBuilder().create().setOwner(owner).setTitle(title).setThrowableIcon().setMessage(message).setStackTrace(t).addOkButton().build().show();
  }

  /**
   * Show error dialog box with stacktrace.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   * @param  t        throwable
   */
  public static void showThrowableDialog(String title, String message, Throwable t)
  {
    showThrowable(title, message, t, null);
  }

  /**
   * Build confirmation dialog builder.
   *
   * @param  title    dialog title
   * @param  message  dialog message
   * @param  owner    parent window
   */
  public static DialogBuilder buildConfirmation(String title, String message, Window owner)
  {
    return new DialogBuilder().create().setOwner(owner).setTitle(title).setConfirmationIcon().setMessage(message);
  }

  public static DialogBuilder buildConfirmation(String title, String message)
  {
    return buildConfirmation(title, message, null);
  }
}
