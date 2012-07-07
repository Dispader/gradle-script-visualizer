package com.nurflugel.util.gradlescriptvisualizer.domain;

import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Task.findOrCreateImplicitTasksByExecute;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Task.findOrCreateImplicitTasksByLine;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Task.findOrCreateTaskByLine;
import static com.nurflugel.util.gradlescriptvisualizer.domain.TaskUsage.EXECUTE;
import static com.nurflugel.util.test.TestResources.getLinesFromArray;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

@Test(groups = "gradle")
public class TaskTest
{
  @Test
  public void testFindTaskType()
  {
    Task task = new Task(new HashMap<String, Task>(), new Line("task copyHelp(type: Copy) {"));

    assertEquals(task.getType(), "Copy");
  }

  @Test
  public void testFindTaskTypeWithDepends()
  {
    Task task = new Task(new HashMap<String, Task>(), new Line("task copyHelp(type: Copy, dependsOn: dibble) {"));

    assertEquals(task.getType(), "Copy");
  }

  @Test
  public void testFindTaskTypeWithQualifiedName()
  {
    Task task = new Task(new HashMap<String, Task>(), new Line("task copyHelp(type: org.dibble.Copy, dependsOn: dibble) {"));

    assertEquals(task.getType(), "Copy");
  }

  @Test
  public void testFindTaskTypeNoTypeDeclared()
  {
    Task task = new Task(new HashMap<String, Task>(), new Line("task copyHelp() {"));

    assertEquals(task.getType(), "noType");
  }

  @Test
  public void testFindDependsOn()
  {
    Task       task           = new Task(new HashMap<String, Task>(), new Line("task signJars(dependsOn: 'installApp') << {"));
    List<Task> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 1);
    assertEquals(dependsOnTasks.get(0).getName(), "installApp");
  }

  @Test
  public void testFindDependsOnDoubleQuotes()
  {
    Task       task           = new Task(new HashMap<String, Task>(), new Line("task signJars(dependsOn: \"installApp\") << {"));
    List<Task> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 1);
    assertEquals(dependsOnTasks.get(0).getName(), "installApp");
  }

  @Test
  public void testFindDependsOnNoQuotes()
  {
    Task       task           = new Task(new HashMap<String, Task>(), new Line("task signJars(dependsOn: installApp) << {"));
    List<Task> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 1);
    assertEquals(dependsOnTasks.get(0).getName(), "installApp");
  }

  @Test
  public void testFindDependsOnWithComma()
  {
    Task       task           = new Task(new HashMap<String, Task>(), new Line("task jettyRunMock(dependsOn: war, description:"));
    List<Task> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 1);
    assertEquals(dependsOnTasks.get(0).getName(), "war");
  }

  @Test
  public void testFindMultipleDependsOn()
  {
    Task       task           = new Task(new HashMap<String, Task>(), new Line("task signJars(dependsOn: [installApp,dibble, dabble]) << {"));
    List<Task> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 3);
    assertEquals(dependsOnTasks.get(0).getName(), "installApp");
    assertEquals(dependsOnTasks.get(1).getName(), "dibble");
    assertEquals(dependsOnTasks.get(2).getName(), "dabble");
  }

  @Test
  public void testDotDeclaration()
  {
    Task task = new Task("simpleTask");

    assertEquals(task.getDotDeclaration(), "simpleTask [label=\"simpleTask\" shape=box color=black ];");
  }

  @Test
  public void testDotDependencies()
  {
    Task         task  = new Task(new HashMap<String, Task>(), new Line("task signJars(dependsOn: [installApp,dibble, dabble]) << {"));
    List<String> lines = task.getDotDependencies();

    assertEquals(lines.get(0), "signJars -> installApp;");
    assertEquals(lines.get(1), "signJars -> dibble;");
    assertEquals(lines.get(2), "signJars -> dabble;");
  }

  @Test
  public void testImplicitTask1()
  {
    // check.dependsOn integrationTest
    List<Task> task = findOrCreateImplicitTasksByLine(new HashMap<String, Task>(), "check.dependsOn integrationTest");

    assertTrue(task.get(0).getName().equals("check"));
  }

  @Test
  public void testImplicitTaskDepends()
  {
    // check.dependsOn integrationTest
    List<Task> task      = findOrCreateImplicitTasksByLine(new HashMap<String, Task>(), "check.dependsOn integrationTest");
    List<Task> dependsOn = task.get(0).getDependsOn();

    assertFalse(dependsOn.isEmpty());
    assertTrue(dependsOn.get(0).getName().equals("integrationTest"));
  }

  @Test
  public void testImplicitTaskDepends2()
  {
    // check.dependsOn integrationTest
    List<Task> task      = findOrCreateImplicitTasksByLine(new HashMap<String, Task>(), "check.dependsOn [integrationTest,'dibble']");
    List<Task> dependsOn = task.get(0).getDependsOn();

    assertFalse(dependsOn.isEmpty());
    assertTrue(dependsOn.get(0).getName().equals("integrationTest"));
    assertTrue(dependsOn.get(1).getName().equals("dibble"));
  }

  @Test
  public void testImplicitTask2()
  {
    // check.dependsOn integrationTest
    Map<String, Task> taskMap = new HashMap<String, Task>();

    findOrCreateImplicitTasksByLine(taskMap, "check.dependsOn integrationTest");
    assertTrue(taskMap.containsKey("integrationTest"));
  }

  // find things like tomcatRun.execute()
  @Test
  public void testFindExecutes()
  {
    HashMap<String, Task> map           = new HashMap<String, Task>();
    Task                  taskInContext = new Task("dibble");

    findOrCreateImplicitTasksByExecute(map, "tomcatRun.execute()", taskInContext, new ArrayList<Task>());

    String tomcatRun = "tomcatRun";

    assertTrue(map.containsKey(tomcatRun));

    Task task = map.get(tomcatRun);

    assertEquals(task.getName(), tomcatRun);
    assertEquals(task.getUsage(), EXECUTE);
    assertEquals(taskInContext.getDependsOn().get(0), task);
  }

  @Test
  public void testFindExecutesDisplaysRight()
  {
    Map<String, Task> map         = new HashMap<String, Task>();
    Task              task        = findOrCreateImplicitTasksByExecute(map, "tomcatRun.execute()", new Task("dibble"), new ArrayList<Task>());
    String            declaration = task.getDotDeclaration();

    assertEquals(declaration, "tomcatRun [label=\"tomcatRun\" shape=ellipse color=red ];");
  }

  // show the task that depends on an execute displays right
  @Test
  public void testTaskDependsOnExecute()
  {
    String[] taskLines =
    {
      "task tomcatRunMock(dependsOn: war, description: 'Runs Webapp using Mock resources (DB, LDAP)') {",  //
      "    doFirst {",                                                                                     //
      "        System.setProperty(\"spring.profiles.active\", \"InMemoryAuth,MockDB\")",                   //
      "        tomcatRun.execute()",                                                                       //
      "    }",                                                                                             //
      "    doLast {",                                                                                      //
      "        System.setProperty(\"spring.profiles.active\", \"InMemoryAuth,MockDB\")",                   //
      "        tomcatStop.execute()",                                                                      //
      "    }",                                                                                             //
      "}",                                                                                                 //
    };

    // build up a list of what we want surrounded by junk - we should get just what we want back
    List<Line> list            = getLinesFromArray(taskLines);
    Line       declarationLine = new Line("task tomcatRunMock(dependsOn: war, description: 'Runs Webapp using Mock resources (DB, LDAP)') {");
    Map<String, Task> taskMap  = new HashMap<String, Task>();
    Task       task            = findOrCreateTaskByLine(taskMap, declarationLine, list, null);

    assertTrue(taskMap.containsKey("tomcatRun"));
    assertTrue(taskMap.containsKey("tomcatStop"));

    List<Task> dependsOn = task.getDependsOn();

    assertTrue(dependsOn.contains(new Task("war")));
    assertTrue(dependsOn.contains(new Task("tomcatRun")));
    assertTrue(dependsOn.contains(new Task("tomcatStop")));
  }

  // after task declaration, keep parsing lines keeping track of { and } - anything within
  // the matching {} pair can be claimed as a dependency.  So, take all those lines and put them into the task for
  // future reference as well.
  @Test
  public void testFindAllTaskLines()
  {
    String[] lines = {
                       "dibble",                                                                           //
                       "dibble",                                                                           //
                       "dibble",                                                                           //
                       "dibble",                                                                           //
                       "dibble",                                                                           //
                       "dibble",                                                                           //
                     };
    String[] taskLines =
    {
      "task tomcatRunMock(dependsOn: war, description: 'Runs Webapp using Mock resources (DB, LDAP)') {",  //
      "    doFirst {",                                                                                     //
      "        System.setProperty(\"spring.profiles.active\", \"InMemoryAuth,MockDB\")",                   //
      "        tomcatRun.execute()",                                                                       //
      "    }",                                                                                             //
      "    doLast {",                                                                                      //
      "        System.setProperty(\"spring.profiles.active\", \"InMemoryAuth,MockDB\")",                   //
      "        tomcatStop.execute()",                                                                      //
      "    }",                                                                                             //
      "}",                                                                                                 //
    };

    // build up a list of what we want surrounded by junk - we should get just what we want back
    List<Line> list            = getLinesFromArray(lines, taskLines, lines);
    Line       declarationLine = new Line("task tomcatRunMock(dependsOn: war, description: 'Runs Webapp using Mock resources (DB, LDAP)') {");
    Task       task            = findOrCreateTaskByLine(new HashMap<String, Task>(), declarationLine, list, null);
    String[]   scopeLines      = task.getScopeLines();

    assertEquals(scopeLines, taskLines, "Should have all the lines for the task in the task");
  }

  @Test
  public void testFindExecutesTask()
  {
    String text = "        tomcatRun.execute()";
    String name = Task.findExecuteDependency(text);

    assertEquals(name, "tomcatRun");
  }

  @Test
  public void testFindExecutesTaskWithOtherWords()
  {
    String text = "  dibble      tomcatRun.execute()";
    String name = Task.findExecuteDependency(text);

    assertEquals(name, "tomcatRun");
  }

  @Test
  public void testFindExecutesTaskNoExecutes()
  {
    String text = "  dibble      tomcatRun.dibble()";
    String name = Task.findExecuteDependency(text);

    assertNull(name);
  }

  @Test
  public void testFindForEachTasks()
  {
    Line       line  = new Line("[tRun1, tRun2].each {");
    List<Task> tasks = Task.findOrCreateTaskInForEach(line, new HashMap<String, Task>());

    assertEquals(tasks.size(), 2);
    assertTrue(tasks.contains(new Task("tRun1")));  // since .equals only checks name, this works
    assertTrue(tasks.contains(new Task("tRun2")));
  }

  @Test
  public void testSimpleBuildFile()
  {
    Task task = new Task("taskName");

    task.setBuildScript("dibble.gradle");
    assertEquals(task.getBuildScript(), "dibble.gradle");
  }

  // ==>test find task dependsOn if task exists elsewhere in build script
  // test find dependsOn in task modification
  // test find dependsOn in iterative task modification
  // determine type of task
}
