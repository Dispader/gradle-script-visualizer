package com.nurflugel.util.gradlescriptvisualizer.output;

import com.nurflugel.util.ScriptPreferences;
import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import org.apache.commons.lang.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.apache.commons.io.FileUtils.isFileNewer;
import static org.apache.commons.io.FileUtils.writeLines;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.lang.StringUtils.replace;

public class DotFileGenerator
{
  private ScriptPreferences preferences;

  public List<String> createOutput(List<Task> tasks, GradleScriptPreferences preferences)
  {
    List<String>            output           = new ArrayList<String>();
    Map<String, List<Task>> buildFileTaskMap = new HashMap<String, List<Task>>();

    output.add("digraph G {\n"                                      //
                 + "node [shape=box,fontname=\"Arial\",fontsize=\"10\"];\n"  //
                 + "edge [fontname=\"Arial\",fontsize=\"8\"];\n"    //
                 + "rankdir=BT;\n"                                  //
                 + '\n'                                             //
                 + "concentrate=" + (preferences.shouldConcentrate() ? "true"
                                                                     : "false") + "true"
                 + ";");

    // build up a map of build files and their tasks - if a task has null, add it to "no build file"
    for (Task task : tasks)
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
    if (preferences.shouldGroupByBuildfiles())
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

        output.add("subgraph cluster_" + replaceBadChars(scriptName) + " { label=\"" + scriptName + "\"; " + builder + "}");
      }
    }

    output.add("}");

    return output;
  }

  public File writeOutput(List<String> lines, String gradleFileName) throws IOException
  {
    String name       = getBaseName(gradleFileName);
    String path       = getFullPath(gradleFileName);
    String outputName = path + name + ".dot";
    File   file       = new File(outputName);

    System.out.println("writing output file = " + file.getAbsolutePath());
    writeLines(file, lines);

    return file;
  }

  /**  */
  public static String replaceBadChars(String oldValue)
  {
    String newValue = replace(oldValue, "-", "_");

    newValue = replace(newValue, " ", "_");
    newValue = replace(newValue, "'", "_");
    newValue = replace(newValue, ":", "_");
    newValue = replace(newValue, ".", "_");
    newValue = replace(newValue, "/", "_");
    newValue = replace(newValue, "\\", "_");

    return newValue;
  }
}
