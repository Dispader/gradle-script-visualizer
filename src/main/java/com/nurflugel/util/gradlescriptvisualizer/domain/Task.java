package com.nurflugel.util.gradlescriptvisualizer.domain;

import static com.nurflugel.util.Util.*;
import static com.nurflugel.util.gradlescriptvisualizer.domain.TaskUsage.EXECUTE;
import static com.nurflugel.util.gradlescriptvisualizer.domain.TaskUsage.GRADLE;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser.addToTaskMap;
import static com.nurflugel.util.gradlescriptvisualizer.util.ParseUtil.findLinesInScope;

import org.apache.commons.collections.CollectionUtils;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Representation of a Gradle task.
 *
 * <p>A Task can depend on other tasks, it can be a "Gradle" task, or, if an execute method is called on it, an "execute" task - we do that because
 * you're not supposed to call "execute" on tasks.</p>
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class Task
{
  private static boolean showFullyQualifiedTaskType = false;
  private String         name;
  private String         type;
  private List<Task>     dependsOnTasks             = new ArrayList<>();

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
   * Find tasks referenced but not declared in this script. For example:
   *
   * <p>check.dependsOn integrationTest</p>
   *
   * @param   taskMap  map of tasks
   * @param   line     the line of text we're parsing
   *
   * @return  a list of tasks found
   */
  public static List<Task> findOrCreateImplicitTasksByLine(Map<String, Task> taskMap, String line)
  {
    List<Task> tasks       = new ArrayList<>();
    String     dependsText = ".dependsOn";
    String     text        = substringBefore(line, dependsText);

    if (text.contains(OPEN_SQUARE_BRACKET))  // it's a list
    {
      text = substringAfter(text, OPEN_SQUARE_BRACKET);
      text = substringBefore(text, CLOSE_SQUARE_BRACKET);

      String[] tokens = split(text, COMMA);

      for (String token : tokens)
      {
        Task task = extractTaskByName(taskMap, line, dependsText, token);

        tasks.add(task);
      }
    }
    else
    {
      Task task = extractTaskByName(taskMap, line, dependsText, text);

      tasks.add(task);
    }

    return tasks;
  }

  /**
   * Find a task in the line, and finds any dependsOn explicitly declared.
   *
   * @param   taskMap      map of tasks
   * @param   line         the line of text we're parsing
   * @param   dependsText  the text pattern used to parse for dependsOn text.
   * @param   taskName     raw name of the task
   *
   * @return  the task found or created
   */
  private static Task extractTaskByName(Map<String, Task> taskMap, String line, String dependsText, String taskName)
  {
    String name = taskName.trim();
    Task   task = findOrCreateTaskByName(taskMap, name);

    task.findTaskDependsOn(taskMap, line, dependsText);

    return task;
  }

  /**
   * Finds the task in the map, or creates it if it's not already there.
   *
   * @param  taskMap   map of existing tasks
   * @param  taskName  the task name to look for
   */
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

  /**
   * Find any dependsOn tasks explicitly declared in this task. For example:
   *
   * <p>task dibble (dependsOn: ['alpha', beta])</p>
   *
   * @param  taskMap      the map of existing tasks
   * @param  line         the line of text to parse
   * @param  dependsText  text to parse dependsOn for
   */
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

  /**
   * Add a single dependency to the task.
   *
   * @param  taskMap  map of tasks
   * @param  oldText  text with dependency name
   */
  private void addSingleDependsOnTask(Map<String, Task> taskMap, String oldText)
  {
    // remove any quotes
    String text = substringBefore(oldText, COMMA);

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

      if (isNotEmpty(executeTasks))
      {
        Task previousExecuteTask = executeTasks.get(executeTasks.size() - 1);

        executeTask.addDependsOn(previousExecuteTask);
      }

      executeTasks.add(executeTask);
      executeTask.setUsage(EXECUTE);

      return executeTask;
    }

    return null;
  }

  /** Helper method to prevent .getDependsOn().add(xxxx) type of code. I hate those, plus it exposes the collection. */
  public void addDependsOn(Task task)
  {
    dependsOnTasks.add(task);
  }

  /**
   * Go through forEach line and find tasks.
   *
   * @param   line     the line of text to parse
   * @param   taskMap  the map of existing tasks
   *
   * @return  list of tasks found
   */
  public static List<Task> findOrCreateTaskInForEach(String line, Map<String, Task> taskMap)
  {
    List<Task> foundTasks = new ArrayList<>();
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

  /** simple constructor to just set the name. */
  public Task(String name)
  {
    this.name = makeSafeName(name);
  }

  /** Strip any unsafe chars out: -:_.${} */
  public static String makeSafeName(String oldValue)
  {
    String newValue = replace(oldValue, "-", "_");

    newValue = replace(newValue, ".xml", "");
    newValue = replace(newValue, " ", "_");
    newValue = replace(newValue, "'", "_");
    newValue = replace(newValue, ":", "_");
    newValue = replace(newValue, ".", "_");
    newValue = replace(newValue, "/", "_");
    newValue = replace(newValue, "$", "_");
    newValue = replace(newValue, "{", "_");
    newValue = replace(newValue, "}", "_");
    newValue = replace(newValue, "\\", "_");

    return newValue;
  }

  /**
   * Constructor which takes a line of text and parses it for information.
   *
   * @param  taskMap  the map of existing tasks
   * @param  line     the line of text to parse
   */
  Task(Map<String, Task> taskMap, String line)
  {
    String taskName = findTaskName(line);

    name = makeSafeName(taskName);
    type = findTaskType(line);
    findTaskDependsOn(taskMap, line);
  }

  /** Determine the task type, if possible. If not, returns NO_TYPE. */
  private static String findTaskType(String line)
  {
    if (line.contains(TYPE))
    {
      String taskType = substringAfter(line, TYPE);

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

  public Task(String name, TaskUsage taskUsage)
  {
    this.name = name;
    usage     = taskUsage;
  }
  // -------------------------- OTHER METHODS --------------------------

  /**
   * Go through the lines in scope and see if there are any executes hiding in there. If so, those are implicit tasks being used, add them to the map.
   *
   * @param  taskMap       the map of existing tasks
   * @param  linesInScope  the lines in scope
   */
  public void analyzeScopeLinesForExecuteDependencies(Map<String, Task> taskMap, String... linesInScope)
  {
    for (String line : linesInScope)
    {
      String executeDependency = findExecuteDependency(line);

      if (executeDependency != null)
      {
        Task newTask = findOrCreateTaskByName(taskMap, executeDependency);

        newTask.setUsage(EXECUTE);
        dependsOnTasks.add(newTask);
      }
    }
  }

  /** Finds any execute tasks, returns the name of the executed task. */
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
    return Collections.unmodifiableList(dependsOnTasks);
  }

  /** Get the declaration for the DOT language. */
  public String getDotDeclaration()
  {
    return name + " [label=\"" + getDeclarationLabel() + "\" shape=" + usage.getShape() + " color=" + usage.getColor() + " ];";
  }

  /** Get the label used to display the task in DOT. */
  private String getDeclarationLabel()
  {
    boolean shouldShowType = showType;
    boolean isType         = !StringUtils.equals(type, NO_TYPE);

    shouldShowType &= isType;
    shouldShowType &= StringUtils.isNotEmpty(type);

    return shouldShowType ? (name + "\\n" + "Type: " + type)
                          : name;
  }

  /**
   * get the list of dependencies in the DOT language.
   *
   * @return  list of lines for output
   */
  public List<String> getDotDependencies()
  {
    List<String> lines = new ArrayList<>();

    for (Task dependsOnTask : dependsOnTasks)
    {
      lines.add(name + " -> " + dependsOnTask.name + ';');
    }

    return lines;
  }

  /**
   * pretty print the task.
   *
   * @param  nestingLevel  number of tabs to use in output
   */
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

  public void setScopeLines(String... scopeLines)
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
