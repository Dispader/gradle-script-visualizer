package com.nurflugel.util.gradlescriptvisualizer.output;

import com.nurflugel.util.ScriptPreferences;
import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import org.apache.commons.lang.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static com.nurflugel.util.Util.*;
import static org.apache.commons.io.FileUtils.writeLines;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.lang.StringUtils.replace;

/**
 * class responsible for generating the DOT output. Since Dot monitors the files it's showing in realtime, this can be called multiple times for the
 * same file during a session.
 */
public class DotFileGenerator
{
  public static final String SPACE       = " ";
  private ScriptPreferences  preferences;

  /**
   * For the list of tasks, create the lines for output based on the given preferences.
   *
   * @param   tasks              the list of tasks
   * @param   scriptPreferences  preferences read in from disk or modified by user
   *
   * @return  list of text lines to be written to disk
   */
  public List<String> createOutput(List<Task> tasks, GradleScriptPreferences scriptPreferences)
  {
    preferences = scriptPreferences;

    List<String>            output           = new ArrayList<String>();
    Map<String, List<Task>> buildFileTaskMap = new HashMap<String, List<Task>>();

    output.add("digraph G {");
    output.add("node [shape=box,fontname=\"Arial\",fontsize=\"10\"];");
    output.add("edge [fontname=\"Arial\",fontsize=\"8\"];");
    output.add("rankdir=BT;");
    output.add("");
    output.add("concentrate=" + (scriptPreferences.shouldConcentrate() ? "true"
                                                                       : "false") + ';');

    // build up a map of build files and their tasks - if a task has null, add it to "no build file"
    for (Task task : tasks)
    {
      buildMapOfTasks(buildFileTaskMap, task);
    }

    // declare tasks
    for (Task task : tasks)
    {
      output.add(task.getDotDeclaration());
    }

    output.add("\n\n");

    // list their dependencies
    for (Task task : tasks)
    {
      output.addAll(task.getDotDependencies());
    }

    // if desired, group the tasks
    if (scriptPreferences.shouldGroupByBuildfiles())
    {
      addGrouping(output, buildFileTaskMap);
    }

    output.add("}");

    return output;
  }

  /** Build up the map of tasks. */
  private static void buildMapOfTasks(Map<String, List<Task>> buildFileTaskMap, Task task)
  {
    String     buildScript = StringUtils.defaultIfEmpty(task.getBuildScript(), "no build script");
    List<Task> tasks1      = buildFileTaskMap.get(buildScript);

    if (tasks1 == null)
    {
      tasks1 = new ArrayList<Task>();
      buildFileTaskMap.put(buildScript, tasks1);
    }

    tasks1.add(task);
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
   * @param   lines           the lines to write
   * @param   gradleFileName  the file to write to
   *
   * @return  the File that was created
   *
   * @throws  IOException  if something bad happens
   */
  public File writeOutput(Collection<String> lines, String gradleFileName) throws IOException
  {
    String name       = getBaseName(gradleFileName);
    String path       = getFullPath(gradleFileName);
    String outputName = path + name + ".dot";
    File   file       = new File(outputName);

    System.out.println("writing output file = " + file.getAbsolutePath());
    writeLines(file, lines);

    if (preferences.shouldDeleteDotFilesOnExit())
    {
      file.deleteOnExit();
    }

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
}
