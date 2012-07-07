package com.nurflugel.util.gradlescriptvisualizer.domain;

import com.nurflugel.util.gradlescriptvisualizer.util.ParseUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.nurflugel.util.gradlescriptvisualizer.domain.TaskUsage.GRADLE;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser.addToTaskMap;
import static org.apache.commons.lang.StringUtils.*;

public class Task
{
  private static final String DEPENDS_ON                 = "dependsOn:";
  private static final String EXECUTE                    = ".execute()";
  private static boolean      showFullyQualifiedTaskType = false;
  private String              name;
  private String              type;
  private List<Task>          dependsOnTasks             = new ArrayList<Task>();
  private TaskUsage           usage                      = GRADLE;
  private String[]            scopeLines;
  private boolean             showType                   = true;
  private String              buildScript;

  public static Task findOrCreateTaskByLine(Map<String, Task> taskMap, Line line, List<Line> lines, String sourceFile)
  {
    String name   = findTaskName(line.getText());
    Task   result;

    if (taskMap.containsKey(name))
    {
      result = taskMap.get(name);
    }
    else
    {
      result = new Task(taskMap, line);
      taskMap.put(name, result);
      addToTaskMap(taskMap, name, result);
    }

    result.setBuildScript(sourceFile);

    // find any dependencies in the task
    result.findTaskDependsOn(taskMap, line);
    result.setScopeLines(ParseUtil.findLinesInScope(line, lines));
    result.analyzeScopeLinesForExecuteDependencies(taskMap);

    return result;
  }

  /** Find the task name in the line. */
  private static String findTaskName(String line)
  {
    String taskName = substringAfter(line, "task ");

    taskName = substringBefore(taskName, " ");
    taskName = getTextBeforeIfExists(taskName, "(");

    return taskName;
  }

  private static String getTextBeforeIfExists(String taskType, String matchingText)
  {
    if (taskType.contains(matchingText))
    {
      taskType = substringBefore(taskType, matchingText);
    }

    return taskType;
  }

  // find the depends on from something like task signJars(dependsOn: 'installApp') << {
  private void findTaskDependsOn(Map<String, Task> taskMap, Line line)
  {
    findTaskDependsOn(taskMap, line.getText(), DEPENDS_ON);
  }

  /** Go through the scope lines, look for any .executes - grab that and mark that task as a dependency. */
  private void analyzeScopeLinesForExecuteDependencies(Map<String, Task> taskMap)
  {
    analyzeScopeLinesForExecuteDependencies(taskMap, scopeLines);
  }

  // check.dependsOn integrationTest
  public static List<Task> findOrCreateImplicitTasksByLine(Map<String, Task> taskMap, String trimmedLine)
  {
    List<Task> tasks       = new ArrayList<Task>();
    String     dependsText = ".dependsOn";
    String     text        = substringBefore(trimmedLine, dependsText);

    if (text.contains("["))  // it's a list
    {
      text = substringAfter(text, "[");
      text = substringBefore(text, "]");

      String[] tokens = split(text, ",");

      for (String token : tokens)
      {
        Task task = extractTaskByName(taskMap, trimmedLine, dependsText, token);

        tasks.add(task);
      }
    }
    else
    {
      Task task = extractTaskByName(taskMap, trimmedLine, dependsText, text);

      tasks.add(task);
    }

    return tasks;
  }

  private static Task extractTaskByName(Map<String, Task> taskMap, String trimmedLine, String dependsText, String taskName)
  {
    String name = taskName.trim();
    Task   task = findOrCreateTaskByName(taskMap, name);

    task.findTaskDependsOn(taskMap, trimmedLine, dependsText);

    return task;
  }

  public static Task findOrCreateTaskByName(Map<String, Task> taskMap, String taskName)
  {
    Task result;

    if (taskMap.containsKey(taskName))
    {
      result = taskMap.get(taskName);
    }
    else
    {
      result = new Task(taskName);
      addToTaskMap(taskMap, taskName, result);
    }

    return result;
  }

  // todo do I still need Line in this???
  public void findTaskDependsOn(Map<String, Task> taskMap, String line, String dependsText)
  {
    String text = substringAfter(line, dependsText);

    text = substringBefore(text, ")");
    text = replace(text, "(", "");

    // test for multiple dependsOn
    if (text.contains("["))
    {
      text = substringAfter(text, "[");
      text = substringBefore(text, "]");

      String[] tokens = split(text, ",");

      for (String token : tokens)
      {
        addSingleDependsOnTask(taskMap, token);
      }
    }
    else
    {
      if (isNotBlank(text))
      {
        addSingleDependsOnTask(taskMap, text);
      }
    }
  }

  private void addSingleDependsOnTask(Map<String, Task> taskMap, String oldText)
  {
    // remove any quotes
    String text = substringBefore(oldText, ",");

    text = remove(text, '\"');
    text = remove(text, '\'');
    text = trim(text);

    Task task = findOrCreateTaskByName(taskMap, text);

    if (!dependsOnTasks.contains(task))
    {
      dependsOnTasks.add(task);
    }
  }

  /**
   * @param   taskMap        map of tasks to add this one to
   * @param   line           line of text to parse
   * @param   taskInContext  the task that's in context - we add this execute task as a dependency to this
   * @param   executeTasks   a list of executes found in this task so far - each new task is a dependency to the previous one, as order is implied
   *
   * @return  the "execute" task
   */
  public static Task findOrCreateImplicitTasksByExecute(Map<String, Task> taskMap, String line, Task taskInContext, List<Task> executeTasks)
  {
    String trim = line.trim();

    if (trim.contains(EXECUTE))
    {
      String taskName    = substringBefore(trim, EXECUTE);
      Task   executeTask = findOrCreateTaskByName(taskMap, taskName);

      if (taskInContext != null)
      {
        taskInContext.addDependsOn(executeTask);
      }
      else
      {
        System.out.println("Task.findOrCreateImplicitTasksByExecute - we need to have a task in context");
      }

      if (CollectionUtils.isNotEmpty(executeTasks))
      {
        Task previousExecuteTask = executeTasks.get(executeTasks.size() - 1);

        executeTask.addDependsOn(previousExecuteTask);
      }

      executeTasks.add(executeTask);
      executeTask.setUsage(TaskUsage.EXECUTE);

      return executeTask;
    }

    return null;
  }

  private void addDependsOn(Task task)
  {
    dependsOnTasks.add(task);
  }

  public static List<Task> findOrCreateTaskInForEach(Line line, Map<String, Task> taskMap)
  {
    List<Task> foundTasks = new ArrayList<Task>();
    String     text       = line.getText();

    text = substringBefore(text, ".each");
    text = substringBefore(text, "]");
    text = substringAfter(text, "[");

    if (text != null)
    {
      String[] tokens = text.split(",");

      for (String token : tokens)
      {
        Task task = findOrCreateTaskByName(taskMap, token.trim());

        foundTasks.add(task);
      }
    }

    return foundTasks;
  }

  public Task(String name)
  {
    this.name = name;
  }

  // todo maybe a better way of doing this would be to have a subclass which takes the other constructor??
  Task(Map<String, Task> taskMap, Line line)
  {
    name = findTaskName(line.getText());
    type = findTaskType(line.getText());
    findTaskDependsOn(taskMap, line);
  }

  private static String findTaskType(String line)
  {
    if (line.contains("type:"))
    {
      String taskType = substringAfter(line, "type:");

      taskType = trim(taskType);
      taskType = getTextBeforeIfExists(taskType, ")");
      taskType = getTextBeforeIfExists(taskType, ",");
      taskType = getTextBeforeIfExists(taskType, " ");

      if (!showFullyQualifiedTaskType && taskType.contains("."))
      {
        taskType = substringAfterLast(taskType, ".");
      }

      taskType = trim(taskType);

      return taskType;
    }
    else
    {
      return "noType";
    }
  }

  // -------------------------- OTHER METHODS --------------------------
  public void analyzeScopeLinesForExecuteDependencies(Map<String, Task> taskMap, String... linesInScope)
  {
    for (String line : linesInScope)
    {
      String executeDependency = findExecuteDependency(line);

      if (executeDependency != null)
      {
        Task newTask = findOrCreateTaskByName(taskMap, executeDependency);

        newTask.setUsage(TaskUsage.EXECUTE);
        dependsOnTasks.add(newTask);
      }
    }
  }

  public static String findExecuteDependency(String text)
  {
    if (contains(text, EXECUTE))
    {
      String beforeText         = substringBefore(text, EXECUTE);
      String afterLastSpaceText = substringAfterLast(beforeText, " ");
      String trimmedText        = afterLastSpaceText.trim();

      return trimmedText;
    }
    else
    {
      return null;
    }
  }

  public List<Task> getDependsOn()
  {
    return dependsOnTasks;
  }

  public String getDotDeclaration()
  {
    return name + " [label=\"" + getDeclarationLabel() + "\" shape=" + usage.getShape() + " color=" + usage.getColor() + " ];";
  }

  private String getDeclarationLabel()
  {
    boolean shouldShowType = showType;
    boolean noType         = StringUtils.equals(type, "noType");

    shouldShowType &= !noType;
    shouldShowType &= isNotEmpty(type);

    return shouldShowType ? (name + "\\n" + "Type: " + type)
                          : name;
  }

  public List<String> getDotDependencies()
  {
    List<String> lines = new ArrayList<String>();

    for (Task dependsOnTask : dependsOnTasks)
    {
      lines.add(name + " -> " + dependsOnTask.name + ';');
    }

    return lines;
  }

  public void printTask(int nestingLevel)
  {
    System.out.println(leftPad("", nestingLevel * 4) + "task = " + this);

    for (Task dependancy : dependsOnTasks)
    {
      dependancy.printTask(nestingLevel + 1);
    }
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    Task other = (Task) obj;

    return new EqualsBuilder().append(name, other.name).isEquals();
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder().append(name).toHashCode();
  }

  public String toString()
  {
    return name;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getBuildScript()
  {
    return buildScript;
  }

  public void setBuildScript(String buildScript)
  {
    this.buildScript = buildScript;
  }

  public String getName()
  {
    return name;
  }

  public String[] getScopeLines()
  {
    return scopeLines;
  }

  public void setScopeLines(String[] scopeLines)
  {
    this.scopeLines = scopeLines;
  }

  public String getType()
  {
    return type;
  }

  public TaskUsage getUsage()
  {
    return usage;
  }

  public void setUsage(TaskUsage usage)
  {
    this.usage = usage;
  }
}
