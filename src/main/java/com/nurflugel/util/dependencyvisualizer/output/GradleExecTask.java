package com.nurflugel.util.dependencyvisualizer.output;

import com.nurflugel.gradle.ui.dialog.ConfigurationChoiceDialog;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;

import javafx.application.Platform;

import javafx.concurrent.Task;

import javafx.scene.control.TextArea;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import static java.io.File.separator;

import java.util.ArrayList;
import java.util.List;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/21/12 Time: 13:25 To change this template use File | Settings | File Templates. */
@SuppressWarnings({ "UseOfProcessBuilder", "UseOfSystemOutOrSystemErr" })
public class GradleExecTask extends Task
{
  private final ConfigurationChoiceDialog dialog;
  private final File                      gradleFile;
  private DependencyDotFileGenerator      dependencyDotFileGenerator;
  private final GradleDependencyParser    parser;
  private final GradleScriptPreferences   preferences;
  private final String                    outputFileName;
  private final Os                        os;

  public GradleExecTask(ConfigurationChoiceDialog dialog, File gradleFile, DependencyDotFileGenerator dependencyDotFileGenerator,
                        GradleDependencyParser parser, GradleScriptPreferences preferences, String outputFileName, Os os)
  {
    this.dialog                     = dialog;
    this.gradleFile                 = gradleFile;
    this.dependencyDotFileGenerator = dependencyDotFileGenerator;
    this.parser                     = parser;
    this.preferences                = preferences;
    this.outputFileName             = outputFileName;
    this.os                         = os;
  }

  @Override
  protected Object call() throws Exception
  {
    String   parent    = gradleFile.getParent();
    String   command   = parent + separator + "gradlew";
    String   path      = gradleFile.getAbsolutePath();
    String[] arguments = { command, "-b", path, "dependencies", "--no-daemon" };

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

    final String[] lines = outputLines.toArray(new String[outputLines.size()]);

    proc.destroy();
    Platform.runLater(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            dependencyDotFileGenerator.createDotFileFromLines(parser, preferences, outputFileName, lines, os, dialog);
          }
          catch (IOException | NoConfigurationsFoundException e)
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
