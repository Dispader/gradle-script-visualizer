package com.nurflugel.util.dependencyvisualizer.output;

import com.nurflugel.gradle.ui.dialog.ConfigurationChoiceDialog;
import com.nurflugel.gradle.ui.dialog.ConfigurationsDialogBuilder;
import static com.nurflugel.gradle.ui.dialog.Dialog.showThrowable;

import static com.nurflugel.util.Util.*;
import com.nurflugel.util.dependencyvisualizer.domain.Artifact;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import static com.nurflugel.util.dependencyvisualizer.domain.Configuration.COMPILE;
import com.nurflugel.util.dependencyvisualizer.domain.ObjectWithArtifacts;
import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

import static org.apache.commons.io.FileUtils.writeLines;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.lang3.StringUtils.replace;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/3/12 Time: 11:27 To change this template use File | Settings | File Templates. */
public class DependencyDotFileGenerator
{
  public static final String SPACE        = " ";
  private static final File  previousFile = null;

  /**
   * For the list of tasks, create the lines for output based on the given preferences.
   *
   * @param   preferences     preferences read in from disk or modified by user
   * @param   outputFileName
   * @param   os
   * @param   dialog
   *
   * @return  list of text lines to be written to disk
   */
  public void createOutput(List<Configuration> configurations, GradleScriptPreferences preferences, String outputFileName, Os os,
                           ConfigurationChoiceDialog dialog) throws NoConfigurationsFoundException
  {
    // build up a map of build files and their tasks - if a task has null, add it to "no build file"
    // for now, just allow one config to be graphed
    if (configurations.size() > 1)
    {
      if (preferences.shouldJustUseCompileConfig())
      {
        int index = configurations.indexOf(COMPILE);

        hideDialog(dialog);
        generateOutputForConfigurations(preferences, configurations.get(index), outputFileName, os);
      }
      else
      {
        dialog.getConfigurationsDialogBuilder().addConfigurations(configurations);
      }
    }
    else if (configurations.isEmpty())
    {
      throw new NoConfigurationsFoundException();
    }
    else
    {
      generateOutputForConfigurations(preferences, configurations.get(0), outputFileName, os);
    }
  }

  void hideDialog(ConfigurationChoiceDialog dialog)
  {
    if (dialog != null)  // null for unit tests
    {
      dialog.hide();
    }
  }

  public void generateOutputForConfigurations(GradleScriptPreferences preferences, Configuration configuration, String outputFileName, Os os)
  {
    List<String> output = new ArrayList<>();

    output.add("digraph G {");
    output.add("node [shape=box,fontname=\"Arial\",fontsize=\"10\"];");
    output.add("edge [fontname=\"Arial\",fontsize=\"8\"];");
    output.add("rankdir=BT;");
    output.add("");
    output.add("concentrate=" + (preferences.shouldConcentrateDependencyLines() ? "true"
                                                                                : "false") + ';');

    List<Configuration> selectedConfigurations = new ArrayList<>();
    boolean             showConfigurationLabel = false;

    if (showConfigurationLabel)
    {
      output.add(configuration.getDotDeclaration());
    }

    selectedConfigurations.add(configuration);

    Set<Artifact> usedArtifacts = new TreeSet<>();

    buildMapOfUsedArtifacts(selectedConfigurations, usedArtifacts);

    for (Artifact usedArtifact : usedArtifacts)
    {
      output.add(usedArtifact.getDotDeclaration());
    }

    output.add("\n\n");

    Set<String> dependencyLines = new TreeSet<>();  // cheesy way to avoid dups and also sort

    configuration.outputDependencies(dependencyLines);
    output.addAll(dependencyLines);

    // if desired, group the tasks
    // if (scriptPreferences.shouldGroupByBuildfiles())
    // {
    // addGrouping(output, buildFileTaskMap);
    // }
    output.add("}");

    try
    {
      String absolutePath = new File(preferences.getLastDir(), outputFileName).getAbsolutePath();

      createAndOpenFile(output, absolutePath, this, os);
    }
    catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e)
    {
      showThrowable("Something bad happened", "Unexpected error", e);
    }
  }

  private void buildMapOfUsedArtifacts(List<Configuration> configurations, Set<Artifact> usedArtifacts)
  {
    for (ObjectWithArtifacts configuration : configurations)
    {
      List<Artifact> artifactList = configuration.getArtifacts();

      for (Artifact artifact : artifactList)
      {
        if (artifact == null)
        {
          System.out.println("Got a null artifact due to dependency conflict!");
        }
        else
        {
          usedArtifacts.add(artifact);
        }
      }

      buildMapOfUsedArtifactsInArtifact(artifactList, usedArtifacts);
    }
  }

  private void buildMapOfUsedArtifactsInArtifact(List<Artifact> artifacts, Set<Artifact> usedArtifacts)
  {
    for (ObjectWithArtifacts artifactToExamine : artifacts)
    {
      if (artifactToExamine != null)  // todo shouldn't be null, fix this later
      {
        List<Artifact> artifactList = artifactToExamine.getArtifacts();

        for (Artifact artifact : artifactList)
        {
          if (artifact == null)
          {
            System.out.println("DependencyDotFileGenerator.buildMapOfUsedArtifactsInArtifact - got a null artifact");
          }
          else
          {
            usedArtifacts.add(artifact);
          }
        }

        buildMapOfUsedArtifactsInArtifact(artifactList, usedArtifacts);
      }
    }
  }

  /** Add grouping of build files. */
  private void addGrouping(List<String> output, Map<String, List<Task>> buildFileTaskMap)
  {
    for (Map.Entry<String, List<Task>> stringListEntry : buildFileTaskMap.entrySet())
    {
      String        scriptName = stringListEntry.getKey();
      StringBuilder builder    = new StringBuilder();
      List<Task>    taskList   = stringListEntry.getValue();

      for (Task task : taskList)
      {
        builder.append(task.getDotDeclaration()).append("; ");
      }

      // todo this line is optional - if they show the configuration, don't have it, if they don't, then show it
      output.add("subgraph cluster_" + replaceBadChars(scriptName) + " { label=\"" + scriptName + "\"; " + "" + builder + '}');
    }
  }

  /**
   * Write the output to disk.
   *
   * @param   lines     the lines to write
   * @param   fileName  the file to write to
   *
   * @return  the File that was created
   *
   * @throws  IOException  if something bad happens
   */
  public File writeOutput(Collection<String> lines, String fileName) throws IOException
  {
    String name       = getBaseName(fileName);
    String path       = getFullPath(fileName);
    String outputName = path + name + ".dot";
    File   file       = new File(outputName);

    System.out.println("writing output file = " + file.getAbsolutePath());
    writeLines(file, lines);

    // if (preferences.shouldDeleteDotFilesOnExit())
    // {
    // file.deleteOnExit();
    // }
    return file;
  }

  /** Replace any characters offensive to the DOT language with harmless underscores. */
  public static String replaceBadChars(String oldValue)
  {
    String newValue = replace(oldValue, HYPHEN, UNDERSCORE);

    newValue = replace(newValue, SPACE, UNDERSCORE);
    newValue = replace(newValue, SINGLE_QUOTE, UNDERSCORE);
    newValue = replace(newValue, COLON, UNDERSCORE);
    newValue = replace(newValue, PERIOD, UNDERSCORE);
    newValue = replace(newValue, SLASH, UNDERSCORE);
    newValue = replace(newValue, BACKSLASH, UNDERSCORE);

    return newValue;
  }

  public File createOutputForFile(File selectedFile, GradleDependencyParser parser, GradleScriptPreferences preferences, String outputFileName,
                                  Os os) throws Exception, NoConfigurationsFoundException
  {
    if (!selectedFile.equals(previousFile))
    {
      ConfigurationChoiceDialog dialog = createAndShowDialog(preferences, outputFileName, os);

      preferences.setLastDir(selectedFile.getParent());

      GradleExecTask gradleExecTask = new GradleExecTask(dialog, selectedFile, this, parser, preferences, outputFileName, os);

      bindProgressBar(dialog, gradleExecTask);
      runTask(gradleExecTask);
    }

    return new File(outputFileName);
  }

  void runTask(GradleExecTask gradleExecTask) throws Exception
  {
    new Thread(gradleExecTask).start();
  }

  void bindProgressBar(ConfigurationChoiceDialog dialog, GradleExecTask gradleExecTask)
  {
    ProgressIndicator progressBar = dialog.getProgressIndicator();

    progressBar.progressProperty().bind(gradleExecTask.progressProperty());
  }

  ConfigurationChoiceDialog createAndShowDialog(GradleScriptPreferences preferences, String outputFileName, Os os)
  {
    ConfigurationChoiceDialog dialog = new ConfigurationsDialogBuilder().create(null, preferences, os, outputFileName).setOwner(null)
                                                                        .setTitle("Processing build file").addOkButton().addCancelButton(null)
                                                                        .build();

    dialog.show();

    return dialog;
  }

  void createDotFileFromLines(GradleDependencyParser parser, GradleScriptPreferences preferences, String outputFileName, String[] lines, Os os,
                              ConfigurationChoiceDialog dialog) throws IOException, NoConfigurationsFoundException
  {
    parser.parseText(lines);

    List<Configuration> configurations = parser.getConfigurations();

    createOutput(configurations, preferences, outputFileName, os, dialog);
  }

  protected void createAndOpenFile(List<String> output, String outputFileName, DependencyDotFileGenerator dotFileGenerator, Os os)
                            throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException
  {
    File file = dotFileGenerator.writeOutput(output, outputFileName);

    os.openFile(file.getAbsolutePath());
  }
}
