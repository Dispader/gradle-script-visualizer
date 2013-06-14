package com.nurflugel.util.gradlescriptvisualizer.domain;

import static com.nurflugel.util.gradlescriptvisualizer.domain.Task.*;
import static com.nurflugel.util.test.TestResources.getLinesFromArray;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import java.util.*;

public class TaskTest
{
  @Test(groups = "gradle")
  public void testFindTaskType()
  {
    Task task = new Task(new HashMap<String, Task>(), "task copyHelp(type: Copy) {");

    assertEquals(task.getType(), "Copy");
  }

  @Test(groups = "gradle")
  public void testFindTaskTypeWithDepends()
  {
    Task task = new Task(new HashMap<String, Task>(), "task copyHelp(type: Copy, dependsOn: dibble) {");

    assertEquals(task.getType(), "Copy");
  }

  @Test(groups = "gradle")
  public void testFindTaskTypeWithQualifiedName()
  {
    Task task = new Task(new HashMap<String, Task>(), "task copyHelp(type: org.dibble.Copy, dependsOn: dibble) {");

    assertEquals(task.getType(), "Copy");
  }

  @Test(groups = "gradle")
  public void testFindTaskTypeNoTypeDeclared()
  {
    Task task = new Task(new HashMap<String, Task>(), "task copyHelp() {");

    assertEquals(task.getType(), "noType");
  }

  @Test(groups = "gradle")
  public void testFindDependsOn()
  {
    Task                 task           = new Task(new HashMap<String, Task>(), "task signJars(dependsOn: 'installApp') << {");
    Map<Task, TaskUsage> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 1);
    assertEquals(dependsOnTasks.keySet().iterator().next().getName(), "installApp");
  }

  @Test(groups = "gradle")
  public void testFindDependsOnDoubleQuotes()
  {
    Task                 task           = new Task(new HashMap<String, Task>(), "task signJars(dependsOn: \"installApp\") << {");
    Map<Task, TaskUsage> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 1);
    assertEquals(dependsOnTasks.keySet().iterator().next().getName(), "installApp");
  }

  @Test(groups = "gradle")
  public void testFindDependsOnNoQuotes()
  {
    Task                 task           = new Task(new HashMap<String, Task>(), "task signJars(dependsOn: installApp) << {");
    Map<Task, TaskUsage> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 1);
    assertEquals(dependsOnTasks.keySet().iterator().next().getName(), "installApp");
  }

  @Test(groups = "gradle")
  public void testFindDependsOnWithComma()
  {
    Task                 task           = new Task(new HashMap<String, Task>(), "task jettyRunMock(dependsOn: war, description:");
    Map<Task, TaskUsage> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 1);
    assertEquals(dependsOnTasks.keySet().iterator().next().getName(), "war");
  }

  @Test(groups = "gradle")
  public void testFindMultipleDependsOn()
  {
    Task                 task           = new Task(new HashMap<String, Task>(), "task signJars(dependsOn: [installApp,dibble, dabble]) << {");
    Map<Task, TaskUsage> dependsOnTasks = task.getDependsOn();

    assertEquals(dependsOnTasks.size(), 3);

    Set<Task> tasks = dependsOnTasks.keySet();

    assertTrue(tasks.contains(new Task("installApp")));
    assertTrue(tasks.contains(new Task("dibble")));
    assertTrue(tasks.contains(new Task("dabble")));
  }

  @Test(groups = "gradle")
  public void testDotDeclaration()
  {
    Task task = new Task("simpleTask");

    assertEquals(task.getDotDeclaration(), "simpleTask [label=\"simpleTask\" ];");
  }

  @Test(groups = "gradle")
  public void testDotDependencies()
  {
    Task         task  = new Task(new HashMap<String, Task>(), "task signJars(dependsOn: [installApp,dibble, dabble]) << {");
    List<String> lines = task.getDotDependencies();

    assertTrue(lines.contains("signJars -> installApp  [color=black] ;"));
    assertTrue(lines.contains("signJars -> dibble  [color=black] ;"));
    assertTrue(lines.contains("signJars -> dabble  [color=black] ;"));
  }

  @Test(groups = "gradle")
  public void testImplicitTask1()
  {
    // check.dependsOn integrationTest
    List<Task> task = findOrCreateImplicitTasksByLine(new HashMap<String, Task>(), "check.dependsOn integrationTest");

    assertTrue(task.get(0).getName().equals("check"));
  }

  @Test(groups = "gradle")
  public void testImplicitTaskDepends()
  {
    // check.dependsOn integrationTest
    List<Task>           task      = findOrCreateImplicitTasksByLine(new HashMap<String, Task>(), "check.dependsOn integrationTest");
    Map<Task, TaskUsage> dependsOn = task.get(0).getDependsOn();

    assertFalse(dependsOn.isEmpty());
    assertTrue(dependsOn.keySet().iterator().next().getName().equals("integrationTest"));
  }

  @Test(groups = "gradle")
  public void testImplicitTaskDepends2()
  {
    // check.dependsOn integrationTest
    List<Task>           task      = findOrCreateImplicitTasksByLine(new HashMap<String, Task>(), "check.dependsOn [integrationTest,'dibble']");
    Map<Task, TaskUsage> dependsOn = task.get(0).getDependsOn();
    Iterator<Task>       iterator  = dependsOn.keySet().iterator();

    assertFalse(dependsOn.isEmpty());
    assertTrue(iterator.next().getName().equals("integrationTest"));
    assertTrue(iterator.next().getName().equals("dibble"));
  }

  @Test(groups = "gradle")
  public void testImplicitTask2()
  {
    // check.dependsOn integrationTest
    Map<String, Task> taskMap = new HashMap<>();

    findOrCreateImplicitTasksByLine(taskMap, "check.dependsOn integrationTest");
    assertTrue(taskMap.containsKey("integrationTest"));
  }

  @Test(groups = "gradle")
  // find things like tomcatRun.execute()
  public void testFindExecutes()
  {
    HashMap<String, Task> map           = new HashMap<>();
    Task                  taskInContext = new Task("dibble");
    String                tomcatRun     = "tomcatRun";

    findOrCreateImplicitTasksByExecute(map, tomcatRun + ".execute()", taskInContext, new ArrayList<Task>());
    assertTrue(map.containsKey(tomcatRun));

    Task task = map.get(tomcatRun);

    assertEquals(task.getName(), tomcatRun);

    Map<Task, TaskUsage> dependsOn  = taskInContext.getDependsOn();
    Set<Task>            tasks      = dependsOn.keySet();
    Task                 actualTask = tasks.iterator().next();

    assertEquals(actualTask, task);
  }

  @Test(groups = "gradle")
  public void testFindExecutesDisplaysRight()
  {
    Map<String, Task> map         = new HashMap<>();
    Task              task        = findOrCreateImplicitTasksByExecute(map, "tomcatRun.execute()", new Task("dibble"), new ArrayList<Task>());
    String            declaration = task.getDotDeclaration();

    assertEquals(declaration, "tomcatRun [label=\"tomcatRun\" ];");
  }

  @Test(groups = "gradle")
  // show the task that depends on an execute displays right
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
    List<String> list            = getLinesFromArray(taskLines);
    String       declarationLine = "task tomcatRunMock(dependsOn: war, description: 'Runs Webapp using Mock resources (DB, LDAP)') {";
    Map<String, Task> taskMap    = new HashMap<>();
    Task         task            = findOrCreateTaskByLine(taskMap, declarationLine, list, null);

    assertTrue(taskMap.containsKey("tomcatRun"));
    assertTrue(taskMap.containsKey("tomcatStop"));

    Map<Task, TaskUsage> dependsOn = task.getDependsOn();

    assertTrue(dependsOn.containsKey(new Task("war")));
    assertTrue(dependsOn.containsKey(new Task("tomcatRun")));
    assertTrue(dependsOn.containsKey(new Task("tomcatStop")));
  }

  // after task declaration, keep parsing lines keeping track of { and } - anything within
  // the matching {} pair can be claimed as a dependency.  So, take all those lines and put them into the task for
  // future reference as well.
  @Test(groups = "gradle")
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
    List<String> list            = getLinesFromArray(lines, taskLines, lines);
    String       declarationLine = "task tomcatRunMock(dependsOn: war, description: 'Runs Webapp using Mock resources (DB, LDAP)') {";
    Task         task            = findOrCreateTaskByLine(new HashMap<String, Task>(), declarationLine, list, null);
    String[]     scopeLines      = task.getScopeLines();

    assertEquals(scopeLines, taskLines, "Should have all the lines for the task in the task");
  }

  @Test(groups = "gradle")
  public void testFindExecutesTask()
  {
    String text = "        tomcatRun.execute()";
    String name = findExecuteDependency(text);

    assertEquals(name, "tomcatRun");
  }

  @Test(groups = "gradle")
  public void testFindExecutesTaskWithOtherWords()
  {
    String text = "  dibble      tomcatRun.execute()";
    String name = findExecuteDependency(text);

    assertEquals(name, "tomcatRun");
  }

  @Test(groups = "gradle")
  public void testFindExecutesTaskNoExecutes()
  {
    String text = "  dibble      tomcatRun.dibble()";
    String name = findExecuteDependency(text);

    assertNull(name);
  }

  @Test(groups = "gradle")
  public void testFindForEachTasks()
  {
    String     line  = "[tRun1, tRun2].each {";
    List<Task> tasks = findOrCreateTaskInForEach(line, new HashMap<String, Task>());

    assertEquals(tasks.size(), 2);
    assertTrue(tasks.contains(new Task("tRun1")));  // since .equals only checks name, this works
    assertTrue(tasks.contains(new Task("tRun2")));
  }

  @Test(groups = "gradle")
  public void testSimpleBuildFile()
  {
    Task task = new Task("taskName");

    task.setBuildScript("dibble.gradle");
    assertEquals(task.getBuildScript(), "dibble.gradle");
  }

  @Test(groups = "gradle")
  public void testGetTextBeforeIfExists()
  {
    String baseText = "dibble@dabble";
    String text     = getTextBeforeIfExists(baseText, "@");

    assertEquals(text, "dibble");
    text = getTextBeforeIfExists(baseText, "{");
    assertEquals(text, baseText);
  }

  @Test(groups = "gradle")
  public void testSafeNames()
  {
    assertEquals(makeSafeName("${mongo.exe}"), "__mongo_exe_");
    assertEquals(makeSafeName("-:.mongo"), "___mongo");
  }
}
