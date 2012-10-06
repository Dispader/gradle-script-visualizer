package com.nurflugel.util.dependencyvisualizer.output;

import com.nurflugel.util.ScriptPreferences;
import com.nurflugel.util.dependencyvisualizer.domain.Artifact;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import com.nurflugel.util.dependencyvisualizer.domain.ObjectWithArtifacts;
import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static com.nurflugel.util.Util.*;
import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.getMasterArtifactMap;
import static org.apache.commons.io.FileUtils.writeLines;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.lang3.StringUtils.replace;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/3/12 Time: 11:27 To change this template use File | Settings | File Templates. */
public class DependencyDotFileGenerator
{
  public static final String SPACE       = " ";
  private ScriptPreferences  preferences;

  /**
   * For the list of tasks, create the lines for output based on the given preferences.
   *
   * @param   masterArtifactMap
   * @param   scriptPreferences  preferences read in from disk or modified by user
   *
   * @return  list of text lines to be written to disk
   */
  public List<String> createOutput(List<Configuration> configurations, Map<String, Artifact> masterArtifactMap,
                                   GradleScriptPreferences scriptPreferences)
  {
    preferences = scriptPreferences;

    List<String> output = new ArrayList<String>();

    output.add("digraph G {");
    output.add("node [shape=box,fontname=\"Arial\",fontsize=\"10\"];");
    output.add("edge [fontname=\"Arial\",fontsize=\"8\"];");
    output.add("rankdir=BT;");
    output.add("");
    output.add("concentrate=" + (scriptPreferences.shouldConcentrate() ? "true"
                                                                       : "false") + ';');

    // build up a map of build files and their tasks - if a task has null, add it to "no build file"
    List<Configuration> selectedConfigurations = new ArrayList<Configuration>();

    // declare tasks
    for (Configuration configuration : configurations)
    {
      // if (configuration.getName().equals("javac2"))  // todo figure out how to figure based on user input
      // if (configuration.getName().equals("runtime"))  // todo figure out how to figure based on user input
      if (configuration.getName().equals("compile"))  // todo figure out how to figure based on user input
      {
        output.add(configuration.getDotDeclaration());
        selectedConfigurations.add(configuration);
      }
    }

    Set<Artifact> usedArtifacts = new TreeSet<Artifact>();

    buildMapOfUsedArtifacts(selectedConfigurations, usedArtifacts);

    for (Artifact usedArtifact : usedArtifacts)
    {
      output.add(usedArtifact.getDotDeclaration());
    }

    output.add("\n\n");

    // list their dependencies
    for (Configuration configuration : selectedConfigurations)
    {
      configuration.outputDependencies(output);
    }

    // if desired, group the tasks
    // if (scriptPreferences.shouldGroupByBuildfiles())
    // {
    // addGrouping(output, buildFileTaskMap);
    // }
    output.add("}");

    return output;
  }

  private void buildMapOfUsedArtifacts(List<Configuration> configurations, Set<Artifact> usedArtifacts)
  {
    for (ObjectWithArtifacts configuration : configurations)
    {
      List<Artifact> artifactList = configuration.getArtifacts();

      for (Artifact artifact : artifactList)
      {
        usedArtifacts.add(artifact);
      }

      buildMapOfUsedArtifactsInArtifact(artifactList, usedArtifacts);
    }
  }

  private void buildMapOfUsedArtifactsInArtifact(List<Artifact> artifacts, Set<Artifact> usedArtifacts)
  {
    for (ObjectWithArtifacts artifactToExamine : artifacts)
    {
      List<Artifact> artifactList = artifactToExamine.getArtifacts();

      for (Artifact artifact : artifactList)
      {
        usedArtifacts.add(artifact);
      }

      buildMapOfUsedArtifactsInArtifact(artifactList, usedArtifacts);
    }
  }

  /** Add grouping of build files. */
  private static void addGrouping(List<String> output, Map<String, List<Task>> buildFileTaskMap)
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

      output.add("subgraph cluster_" + replaceBadChars(scriptName) + " { label=\"" + scriptName + "\"; " + builder + '}');
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

  public static File createOutputForFile(File selectedFile, GradleDependencyParser parser, GradleScriptPreferences preferences, String outputFileName)
                                  throws IOException
  {
    preferences.setLastDir(selectedFile.getParent());

    try
    {
      String[] lines = parser.runGradleExec(selectedFile);

      parser.parseText(lines);

      List<Configuration> configurations = parser.getConfigurations();

      // todo filter output by configuration
      DependencyDotFileGenerator dotFileGenerator = new DependencyDotFileGenerator();
      List<String>               output           = dotFileGenerator.createOutput(configurations, getMasterArtifactMap(), preferences);
      File                       file             = dotFileGenerator.writeOutput(output, outputFileName);

      return file;
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }

    return null;
  }
}
