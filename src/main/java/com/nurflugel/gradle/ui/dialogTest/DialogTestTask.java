package com.nurflugel.gradle.ui.dialogTest;

import com.nurflugel.gradle.ui.dialog.ConfigurationChoiceDialog;

import javafx.application.Platform;

import javafx.concurrent.Task;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 4/17/13 Time: 09:46 To change this template use File | Settings | File Templates. */
public class DialogTestTask extends Task
{
  private ConfigurationChoiceDialog dialog;

  public DialogTestTask(ConfigurationChoiceDialog dialog)
  {
    this.dialog = dialog;
  }

  @Override
  protected Object call() throws Exception
  {
    String   command   = "find .";
    String[] arguments = { command };

    System.out.println("GradleDependencyParser.runGradleExec - calling ProcessBuilder command " + command + ' ' + ArrayUtils.toString(arguments));

    ProcessBuilder pb = new ProcessBuilder(arguments);

    // pb.directory(gradleFile.getParentFile());
    pb.redirectErrorStream(true);

    List<String> outputLines = new ArrayList<>();
    Process      proc        = pb.start();

    try(PrintWriter out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream())))
    {        // feed in the program
      out.println("Some line here");
      out.flush();

      String resultLine = in.readLine();

      while (resultLine != null)
      {
        try
        {
          Thread.sleep(1);
        }
        catch (InterruptedException interrupted)
        {
          if (isCancelled())
          {
            updateMessage("Cancelled");

            break;
          }
        }

        System.out.println(resultLine);
        log(resultLine);
        outputLines.add(resultLine);
        resultLine = in.readLine();
      }
    }

    String[] lines = outputLines.toArray(new String[outputLines.size()]);

    proc.destroy();
    Platform.runLater(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {  // this is where we redirect the output

            // dependencyDotFileGenerator.createDotFileFromLines(parser, preferences, outputFileName, lines, os, dialog);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      });

    return lines;
  }

  private void log(final String text)
  {
    // we can access fx objects only from fx thread so we need to wrap log access into Platform#runLater
    Platform.runLater(new Runnable()
      {
        @Override
        public void run()
        {
          dialog.addLineToDisplay(text);
        }
      });
  }
}
