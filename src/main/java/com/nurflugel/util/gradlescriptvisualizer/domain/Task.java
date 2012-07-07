package com.nurflugel.util.gradlescriptvisualizer.domain;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.nurflugel.util.Util.*;
import static com.nurflugel.util.gradlescriptvisualizer.domain.TaskUsage.GRADLE;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser.addToTaskMap;
import static com.nurflugel.util.gradlescriptvisualizer.util.ParseUtil.findLinesInScope;
import static org.apache.commons.lang.StringUtils.*;

/**
 * Representation of a Gradle task.
 *
 * <p>A Task can depend on other tasks, it can be a "Gradle" task, or, if an execute method is called on it, an "execute" task - we do that because
 * you're not supposed to call "execute" on tasks.</p>
 */
public class Task
{
  private static boolean showFullyQualifiedTaskType = false;
  private String         name;
  private String         type;
  private List<Task>     dependsOnTasks             = new ArrayList<Task>();

  /** Default is GRADLE, can be switched to EXECUTE if that method is used. */
  private TaskUsage usage = GRADLE;

  /** lines included in the scope of the task declaration. */
  private String[] scopeLines;
  private boolean  showType    = true;
  private String   buildScript;

  /**
   * Given the line of text, find the task in the map of tasks by name, or else create a new task and add it to the map.
   *
   * @param   taskMap     the map of existing, known tasks
   * @param   line        the line of text to parse
   * @param   lines       The lines of text we're parsing - needed to find all the lines in the scope of a task.
   * @param   sourceFile  the file these lines came from
   *
   * @return  the task that was created or found
   */
  public static Task findOrCreateTaskByLine(Map<String, Task> taskMap, String line, List<String> lines, String sourceFile)
  {
    String name   = findTaskName(line);
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
    result.setScopeLines(findLinesInScope(line, lines));
    result.analyzeScopeLinesForExecuteDependencies(taskMap);

    return result;
  }

  /** Find the task name in the line. */
  private static String findTaskName(String line)
  {
    String taskName = substringAfter(line, "task ");

    taskName = substringBefore(taskName, SPACE);
    taskName = getTextBeforeIfExists(taskName, OPEN_PARENTHESIS);

    return taskName;
  }

  /**
   * Get the text before the matching text. For example, if text is "dibble@dabble", and the delimiter is " @", then you'll get "dibble" back.
   *
   * <p>However, if the delimiter is "&", then you get the whole "dibble@dabble" back.</p>
   *
   * @param   text       the text to parse
   * @param   delimiter  a bit of text to search from.
   *
   * @return  the result text, or else the original
   *
   *          <p>todo - isn't this in StringUtils somewhere???</p>
   */
  static String getTextBeforeIfExists(String text, String delimiter)
  {
    return text.contains(delimiter) ? substringBefore(text, delimiter)
                                    : text;
  }

  /**
   * find the depends on from something like task signJars(dependsOn: 'installApp') << {
   *
   * @param  taskMap  map of tasks
   * @param  line     the line of text we're parsing
   */
  private void findTaskDependsOn(Map<String, Task> taskMap, String line)
  {
    findTaskDependsOn(taskMap, line, DEPENDS_ON_TEXT);
  }

  /** Go through the scope lines, look for any .executes - grab that and mark that task as a dependency. */
  private void analyzeScopeLinesForExecuteDependencies(Map<String, Task> taskMap)
  {
    analyzeScopeLinesForExecuteDependencies(taskMap, scopeLines);
  }

  /**
   * <p>check.dependsOn integrationTest</p>
   *
   * @param   taskMap
   * @param   trimmedLine
   *
   * @return
   */
  public static List<Task> findOrCreateImplicitTasksByLine(Map<String, Task> taskMap, String trimmedLine)
  {
    List<Task> tasks       = new ArrayList<Task>();
    String     dependsText = ".dependsOn";
    String     text        = substringBefore(trimmedLine, dependsText);

    if (text.contains(OPEN_SQUARE_BRACKET))  // it's a list
    {
      text = substringAfter(text, OPEN_SQUARE_BRACKET);
      text = substringBefore(text, CLOSE_SQUARE_BRACKET);

      String[] tokens = split(text, COMMA);

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

  public void findTaskDependsOn(Map<String, Task> taskMap, String line, String dependsText)
  {
    String text = substringAfter(line, dependsText);

    text = substringBefore(text, CLOSE_PARENTHESIS);
    text = replace(text, OPEN_PARENTHESIS, EMPTY_TEXT);
    text = replace(text, SINGLE_QUOTE, EMPTY_TEXT);
    text = replace(text, DOUBLE_QUOTE, EMPTY_TEXT);

    // test for multiple dependsOn
    if (text.contains(OPEN_SQUARE_BRACKET))
    {
      text = substringAfter(text, OPEN_SQUARE_BRACKET);
      text = substringBefore(text, CLOSE_SQUARE_BRACKET);

      String[] tokens = split(text, COMMA);

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

    if (trim.contains(EXECUTE_TEXT))
    {
      String taskName    = substringBefore(trim, EXECUTE_TEXT);
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

  public static List<Task> findOrCreateTaskInForEach(String line, Map<String, Task> taskMap)
  {
    List<Task> foundTasks = new ArrayList<Task>();
    String     text       = line;

    text = substringBefore(text, EACH);
    text = substringBefore(text, CLOSE_SQUARE_BRACKET);
    text = substringAfter(text, OPEN_SQUARE_BRACKET);

    if (text != null)
    {
      String[] tokens = text.split(COMMA);

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
  Task(Map<String, Task> taskMap, String line)
  {
    name = findTaskName(line);
    type = findTaskType(line);
    findTaskDependsOn(taskMap, line);
  }

  private static String findTaskType(String line)
  {
    String type = "type:";

    if (line.contains(type))
    {
      String taskType = substringAfter(line, type);

      taskType = trim(taskType);
      taskType = getTextBeforeIfExists(taskType, CLOSE_PARENTHESIS);
      taskType = getTextBeforeIfExists(taskType, COMMA);
      taskType = getTextBeforeIfExists(taskType, SPACE);

      if (!showFullyQualifiedTaskType && taskType.contains(PERIOD))
      {
        taskType = substringAfterLast(taskType, PERIOD);
      }

      taskType = trim(taskType);

      return taskType;
    }
    else
    {
      return NO_TYPE;
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
    if (contains(text, EXECUTE_TEXT))
    {
      String beforeText         = substringBefore(text, EXECUTE_TEXT);
      String afterLastSpaceText = substringAfterLast(beforeText, SPACE);
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
    boolean noType         = StringUtils.equals(type, NO_TYPE);

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
