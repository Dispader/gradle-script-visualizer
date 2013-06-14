package com.nurflugel.util.gradlescriptvisualizer.parser;

import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.domain.TaskUsage;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleTaskOutputParser.getSubTask;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleTaskOutputParser.getTaskFromLine;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleTaskOutputParser.isSubTask;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import java.io.File;

import java.util.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 3/7/13 Time: 21:24 To change this template use File | Settings | File Templates. */
@SuppressWarnings("MethodMayBeStatic")
@Test(groups = "unit")
public class GradleTaskOutputParserTest
{
  @Test(groups = "unit")
  public void testSingleWorkTaskLine() throws Exception
  {
    assertNotNull(getTaskFromLine("assemble  "));
    assertNull(getTaskFromLine("ass emble  "));
  }

  @Test(groups = "unit")
  public void testValidTaskLine() throws Exception
  {
    validateTasks(getTaskFromLine("assemble - Assembles the outputs of this project. [war]"), "war");
  }

  private void validateTasks(Task task, String... names)
  {
    assertNotNull(task);

    Map<Task, TaskUsage> taskList = task.getDependsOn();

    assertEquals(taskList.size(), names.length);

    Set<Task>    tasks         = taskList.keySet();
    List<String> requiredNames = Arrays.asList(names);

    for (Task dependentTask : tasks)
    {
      String name = dependentTask.getName();

      assertTrue(requiredNames.contains(name));
    }
  }

  @Test(groups = "unit")
  public void testValidTaskLineMultipleTasks() throws Exception
  {
    validateTasks(getTaskFromLine("assemble - Assembles the outputs of this project. [war,dibble]"), "war", "dibble");
    validateTasks(getTaskFromLine("assemble - Assembles the outputs of this project. [war, dibble]"), "war", "dibble");
  }

  @Test(groups = "unit")
  public void testNoTasks() throws Exception
  {
    assertNull(getTaskFromLine("assemble  Assembles the outputs of this project. "));
    assertNull(getTaskFromLine("ass emble - Assembles the outputs of this project. "));
    assertNull(getTaskFromLine("ass emble - Assembles the outputs of this project. [dibble] "));
    assertNoSubtasks(getTaskFromLine("assemble - Assembles the outputs of this project. "));
    assertNoSubtasks(getTaskFromLine("assemble - Assembles the outputs of this project. []"));
    assertNoSubtasks(getTaskFromLine("assemble - Assembles the outputs of this project. [ ]"));
    assertNoSubtasks(getTaskFromLine("assemble - Assembles the outputs of this project. ["));
    assertNoSubtasks(getTaskFromLine("assemble - Assembles the outputs of this project. ]"));
  }

  private void assertNoSubtasks(Task task)
  {
    assertNotNull(task);
    assertTrue(task.getDependsOn().isEmpty());
  }

  @Test(groups = "unit")
  public void testRealFile() throws Exception
  {
    File realFile = new File("/Users/douglas_bullard/Documents/JavaStuff/Google_Code/gradle-script-visualizer/trunk/build/resources/test/gradle/gradleTasks.out");
    List<Task> tasks = GradleTaskOutputParser.parseLines(realFile);

    for (Task task : tasks)
    {
      System.out.println("task = " + task);

      Map<Task, TaskUsage> dependsOn = task.getDependsOn();

      for (Task subTask : dependsOn.keySet())
      {
        System.out.println("    subTask = " + subTask);
      }
    }
  }

  @Test(groups = "unit")
  public void testGetSubTask()
  {
    assertEquals(getSubTask("   compileGroovy - Compiles the main Groovy source.").getName(), "compileGroovy");
    assertEquals(getSubTask("   compileGroovy").getName(), "compileGroovy");
    assertNull(getSubTask(""));
    assertNull(getSubTask("   "));
    assertNull(getSubTask("dibble"));
  }

  @Test(groups = "unit")
  public void testIsSubTask()
  {
    assertTrue(isSubTask("   compileGroovy - Compiles the main Groovy source."));
    assertTrue(isSubTask("   compileGroovy"));
    assertFalse(isSubTask(""));
    assertFalse(isSubTask("   "));
    assertFalse(isSubTask("dibble"));
  }
}
