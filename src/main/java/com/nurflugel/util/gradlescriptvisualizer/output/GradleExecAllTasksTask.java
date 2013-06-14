package com.nurflugel.util.gradlescriptvisualizer.output;

import com.nurflugel.gradle.ui.dialog.ConfigurationChoiceDialog;

import com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser;

import javafx.application.Platform;

import javafx.concurrent.Task;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import static java.io.File.separator;

import java.util.ArrayList;
import java.util.List;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/21/12 Time: 13:25 To change this template use File | Settings | File Templates. */
@SuppressWarnings({ "UseOfProcessBuilder", "UseOfSystemOutOrSystemErr" })
public class GradleExecAllTasksTask extends Task
{
  private ConfigurationChoiceDialog dialog;
  private final File                gradleFile;
  private String[]                  lines;
  private GradleFileParser          fileParser;

  public GradleExecAllTasksTask(File gradleFile, ConfigurationChoiceDialog dialog, GradleFileParser fileParser)
  {
    this.dialog     = dialog;
    this.gradleFile = gradleFile;
    this.fileParser = fileParser;
  }

  @Override
  protected Object call() throws Exception
  {
    String   parent    = gradleFile.getParent();
    String   command   = parent + separator + "gradlew";
    String   path      = gradleFile.getAbsolutePath();
    String[] arguments = { command, "-b", path, "tasks", "--all", "--no-daemon" };

    System.out.println("GradleDependencyParser.runGradleExec - calling ProcessBuilder command " + command + ' ' + ArrayUtils.toString(arguments));

    ProcessBuilder pb = new ProcessBuilder(arguments);

    pb.directory(gradleFile.getParentFile());
    pb.redirectErrorStream(true);

    List<String> outputLines = new ArrayList<>();
    Process      proc        = pb.start();

    try(PrintWriter out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream())))
    {  // feed in the program
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

    lines = outputLines.toArray(new String[outputLines.size()]);
    System.out.println("GradleExecAllTasksTask.call 1");

    // dialog.hide();
    System.out.println("GradleExecAllTasksTask.call 2");

    // dialog.close();
    System.out.println("GradleExecAllTasksTask.call 3");
    proc.destroy();
    System.out.println("GradleExecAllTasksTask.call 4");
    Platform.runLater(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {  // this is where we redirect the output
            fileParser.processTaskLines(dialog, gradleFile, lines);
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      });

    return null;
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

  public String[] getLines()
  {
    return lines;
  }
}
