package com.nurflugel.gradle.ui.dialogservice;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.net.URL;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.TRANSPARENT;

/** {@linkplain DialogService} Demo. */
public class DialogServiceTest extends Application
{
  private DialogService dialogService;
  // --------------------------- main() method ---------------------------

  /**
   * Main {@linkplain Application} entry point.
   *
   * @param  args  passed arguments
   */
  public static void main(String[] args)
  {
    try
    {
      Application.launch(DialogServiceTest.class, args);
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }
  // -------------------------- OTHER METHODS --------------------------

  /**
   * Shows an example usage of a {@linkplain DialogService} that displays a login screen.
   *
   * @param   primaryStage  the primary application {@linkplain Stage}
   *
   * @throws  Exception  when something goes wrong
   */
  @Override
  public void start(final Stage primaryStage) throws Exception
  {
    // setup the primary stage with a simple button that will open
    VBox rootNode = new VBox();

    rootNode.setAlignment(CENTER);

    Button button = new Button("Launch Login Dialog Window");

    button.setOnMouseClicked(new EventHandler<MouseEvent>()
      {
        @Override
        public void handle(MouseEvent event)
        {
          if (dialogService != null)
          {
            dialogService.hide();
          }

          dialogService = createLoginDialog(primaryStage);
          dialogService.start();
        }
      });
    rootNode.getChildren().add(button);
    primaryStage.setTitle("Dialog Service Demo");
    primaryStage.setScene(new Scene(rootNode, 800, 500, BLACK));

    URL    resource     = DialogServiceTest.class.getResource("/com/nurflugel/gradle/ui/dialogservice/dialog.css");
    String externalForm = resource.toExternalForm();

    primaryStage.getScene().getStylesheets().add(externalForm);
    primaryStage.show();
  }

  /**
   * Creates a {@linkplain DialogService} that displays a login screen.
   *
   * @param  primaryStage  the primary application {@linkplain Stage}
   */
  public DialogService createLoginDialog(Stage primaryStage)
  {
    final TextField     username      = TextFieldBuilder.create().promptText("Username").build();
    final PasswordField password      = PasswordFieldBuilder.create().promptText("Password").build();
    Button              closeBtn      = ButtonBuilder.create().text("Close").build();
    Service<Void>       submitService = new Service<Void>()
    {
      @Override
      protected Task<Void> createTask()
      {
        return new Task<Void>()
        {
          @Override
          protected Void call() throws Exception
          {
            boolean hasUsername       = !username.getText().isEmpty();
            boolean hasPassword       = !password.getText().isEmpty();

            if (hasUsername && hasPassword)
            {
              // TODO : perform some sort of authentication here
              // or you can throw an exception to see the error
              // message in the dialog window
            }
            else
            {
              String invalidFields = ((hasUsername) ? ""
                                                    : username.getPromptText()) + ' ' + ((hasPassword) ? ""
                                                                                                       : password.getPromptText());

              throw new RuntimeException("Invalid " + invalidFields);
            }

            return null;
          }
        };
      }
    };

    final DialogService dialogService = dialog(primaryStage, "Test Dialog Window", "Please provide a username and password to access the application",
                                               null, "Login", 550d, 300d, submitService, closeBtn, username, password);

    if (closeBtn != null)
    {
      closeBtn.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
          @Override
          public void handle(MouseEvent event)
          {
            dialogService.hide();
          }
        });
    }

    return dialogService;
  }

  /**
   * Creates a dialog window {@linkplain Stage} that is shown when the {@linkplain DialogService#start()} is called and hidden when the submit
   * {@linkplain Service#restart()} returns {@linkplain State#SUCCEEDED}. When a {@linkplain Task} throws an {@linkplain Exception} the
   * {@linkplain Exception#getMessage()} will be used to update the messageHeader of the dialog.
   *
   * @param   parent         the parent {@linkplain Stage}
   * @param   title          the text for the {@linkplain Stage#setTitle(String)}
   * @param   headerText     the text for the {@linkplain Text#setText(String)} header
   * @param   icon           the icon of the {@linkplain Stage}
   * @param   submitLabel    the text for the submit {@linkplain Button#setText(String)}
   * @param   width          the width of the {@linkplain Stage}
   * @param   height         the height of the {@linkplain Stage}
   * @param   submitService  the {@linkplain Service} ran whenever the submit {@linkplain Button} is clicked
   * @param   children       the child {@linkplain Node}s that will be added between the messageHeader and submit {@linkplain Button} (if any). If any
   *                         of the {@linkplain Node}s are {@linkplain Button}s they will be added to the internal {@linkplain Button}
   *                         {@linkplain FlowPane} added to the bottom of the dialog.
   *
   * @return  the {@linkplain DialogService}
   */
  public static DialogService dialog(Stage parent, String title, String headerText, Image icon, String submitLabel, double width, double height,
                                     final Service<Void> submitService, Node... children)
  {
    Stage         window        = new Stage();
    Text          header        = TextBuilder.create().text(headerText).styleClass("dialog-title").wrappingWidth(width / 1.2d).build();
    Text          messageHeader = TextBuilder.create().styleClass("dialog-message").wrappingWidth(width / 1.2d).build();
    DialogService service       = new DialogService(parent, window, messageHeader, submitService);

    window.initModality(Modality.APPLICATION_MODAL);
    window.initStyle(StageStyle.TRANSPARENT);

    if (icon != null)
    {
      window.getIcons().add(icon);
    }

    if (title != null)
    {
      window.setTitle(title);
    }

    VBox content = VBoxBuilder.create().styleClass("dialog").build();

    content.setMaxSize(width, height);
    window.setScene(new Scene(content, width, height, TRANSPARENT));

    if (parent != null)
    {
      window.getScene().getStylesheets().setAll(parent.getScene().getStylesheets());
    }

    Button submitBtn = ButtonBuilder.create().text(submitLabel).defaultButton(true).onAction(new EventHandler<ActionEvent>()
      {
        @Override
        public void handle(ActionEvent actionEvent)
        {
          submitService.restart();
        }
      }).build();
    FlowPane flowPane = new FlowPane();

    flowPane.setAlignment(CENTER);
    flowPane.setVgap(20d);
    flowPane.setHgap(10d);
    flowPane.setPrefWrapLength(width);
    flowPane.getChildren().add(submitBtn);
    content.getChildren().addAll(header, messageHeader);

    if ((children != null) && (children.length > 0))
    {
      for (Node node : children)
      {
        if (node == null)
        {
          continue;
        }

        if (node instanceof Button)
        {
          flowPane.getChildren().add(node);
        }
        else
        {
          content.getChildren().add(node);
        }
      }
    }

    content.getChildren().addAll(flowPane);

    return service;
  }
  // -------------------------- INNER CLASSES --------------------------
}
