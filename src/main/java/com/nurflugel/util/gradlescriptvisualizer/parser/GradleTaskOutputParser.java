package com.nurflugel.util.gradlescriptvisualizer.parser;

import com.nurflugel.gradle.ui.dialog.ConfigurationChoiceDialog;
import com.nurflugel.gradle.ui.dialog.ConfigurationsDialogBuilder;

import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.domain.TaskUsage;
import static com.nurflugel.util.gradlescriptvisualizer.domain.TaskUsage.GRADLE;
import com.nurflugel.util.gradlescriptvisualizer.output.GradleExecAllTasksTask;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;

import static org.apache.commons.io.FileUtils.readLines;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is designed to take the output from calling "gradlew tasks --all" and parsing it to see tasks that we can't see from the actual
 * build.gradle itself.
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public class GradleTaskOutputParser
{
  public static void runGradleToGetTaskLines(File fileToParse, GradleScriptPreferences preferences, GradleFileParser gradleFileParser)
  {
    Os                          os                           = Os.findOs();
    ConfigurationsDialogBuilder configurationsDialogBuilder  = new ConfigurationsDialogBuilder().create(null, null, os, null);
    ConfigurationsDialogBuilder configurationsDialogBuilder1 = configurationsDialogBuilder.setOwner(null);
    ConfigurationsDialogBuilder configurationsDialogBuilder2 = configurationsDialogBuilder1.setTitle("Processing build file");
    ConfigurationChoiceDialog   dialog                       = configurationsDialogBuilder2.addCancelButton(null).build();
    GradleExecAllTasksTask      task                         = new GradleExecAllTasksTask(fileToParse, dialog, gradleFileParser);

    System.out.println("GradleTaskOutputParser.runGradleToGetTaskLines 1");

    // run "gradlew tasks --all" with a spinner, but show output when output is done, close window
    dialog.show();
    runTask(task);
    System.out.println("GradleTaskOutputParser.runGradleToGetTaskLines 2");
  }

  private static void runTask(GradleExecAllTasksTask task)
  {
    new Thread(task).start();
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static void mergeGradleTasksIntoExistingTasks(Map<String, Task> taskMap, List<Task> gradleTasks)
  {
    // go through each task in the list
    for (Task gradleTask : gradleTasks)
    {                                           // see if the name of that task exists in the map

      String gradleTaskName = gradleTask.getName();

      if (taskMap.containsKey(gradleTaskName))  // if so, get the old version of the task
      {
        Task                 existingTask          = taskMap.get(gradleTaskName);
        Map<Task, TaskUsage> existingTaskDependsOn = existingTask.getDependsOn();
        Map<Task, TaskUsage> gradleTaskDependsOn   = gradleTask.getDependsOn();

        // make sure it has all the dependent tasks in the task from the list
        for (Task task : gradleTaskDependsOn.keySet())
        {
          if (!existingTaskDependsOn.containsKey(task))
          {
            existingTask.addDependsOn(task, GRADLE);
          }
        }
      }
      else
      {  // if not, create a new task and add it to the map
        taskMap.put(gradleTaskName, gradleTask);
      }
    }
  }

  private GradleTaskOutputParser() {}

  public static List<Task> parseLines(File fileToParse) throws IOException
  {
    List<String> lines = readLines(fileToParse);

    return parse(lines.toArray(new String[lines.size()]));
  }

  // ========================================================================================================
  // Welcome to Gradle version 1.4
  //
  // Gradle user directory is set to: /Users/Shared/Development/NikeBuild/gradleHome
  // Base directory:                  /Users/Shared/Development/NikeBuild/nikeDev/das/trunk
  // Running build script             build.gradle
  // ========================================================================================================
  //
  // Das location is null/src/main/DAS/web
  // Should edit build/tmp/war/app/settings.js, line 15:
  // servicesBaseURL: "https://shop-dev.nike.net/das", to use localhost
  // :tasks
  //
  // ------------------------------------------------------------
  // All tasks runnable from root project - DAS webapp
  // ------------------------------------------------------------
  //
  // Build tasks
  // -----------
  // assemble - Assembles the outputs of this project. [war]
  // build - Assembles and tests this project. [assemble, check]
  static List<Task> parse(String... lines)
  {
    int        size  = lines.length;
    List<Task> tasks = new ArrayList<>(size);

    for (int i = 0; i < size; i++)
    {
      String line = lines[i];

      if (i < (size - 2))
      {
        if (line.endsWith(" tasks"))
        {
          String nextLine = lines[++i];

          if (containsOnly(nextLine, "-"))  // contains only dashes - next line will be a possible task with dependencies did
          {                                 // we're reading lines, keep reading until i is too big or we hit a blank line

            Task parentTask = null;

            while ((i < lines.length) && isNotEmpty(nextLine.trim()))
            {
              nextLine = lines[++i];

              // todo do a test for ifTask, then get task
              Task task = getTaskFromLine(nextLine);

              if (task == null)           // it's not a task
              {                           // figure out how to add to other task lists

                if (isSubTask(nextLine))  // is it a subtask?
                {
                  Task subtask = getSubTask(nextLine);

                  parentTask.addDependsOn(subtask, GRADLE);
                }
              }
              else                        // add the task to the list
              {
                tasks.add(task);
                parentTask = task;
              }
            }
          }
        }
      }
    }

    return tasks;
  }

  public static Task getTaskFromLine(String possibleTaskLine)
  {
    if (possibleTaskLine.startsWith("   "))
    {
      return null;
    }

    String taskName = possibleTaskLine.trim();

    if (containsOnly(taskName, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890_"))
    {
      Task task = new Task(taskName);

      return task;
    }

    // should just have one word before first hyphen.
    if (!contains(possibleTaskLine, '-'))
    {
      return null;
    }

    taskName = substringBefore(possibleTaskLine, "-").trim();

    if (taskName.contains(" "))
    {
      return null;
    }

    // if (!contains(possibleTaskLine, '[') || !contains(possibleTaskLine, ']'))
    // {
    // return null;
    // }
    String possibleTasks = StringUtils.substringAfterLast(possibleTaskLine, "[").trim();

    possibleTasks = substringBefore(possibleTasks, "]");

    // if (StringUtils.isEmpty(possibleTasks))
    // {
    // return null;
    // }
    String[] tasks = possibleTasks.split(",");

    // should have at least one token between []
    // if (tasks.length == 0)
    // {
    // return null;
    // }
    Task task = new Task(taskName);

    for (String subtaskname : tasks)
    {
      if (isNotEmpty(subtaskname))
      {
        task.addDependsOn(new Task(subtaskname.trim()), GRADLE);
      }
    }

    return task;
  }

  static boolean isSubTask(String nextLine)
  {
    if (!nextLine.startsWith("   "))
    {
      return false;
    }

    if (isBlank(nextLine))
    {
      return false;
    }

    return true;
  }

  static Task getSubTask(String line)
  {
    if (isSubTask(line))
    {
      String taskName = substringBefore(line, "-").trim();

      return new Task(taskName);
    }
    else
    {
      return null;
    }
  }
}
