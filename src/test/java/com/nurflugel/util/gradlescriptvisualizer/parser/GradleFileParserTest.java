package com.nurflugel.util.gradlescriptvisualizer.parser;

import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser.*;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import static com.nurflugel.util.test.TestResources.getFilePath;
import static com.nurflugel.util.test.TestResources.getLinesFromArray;

import static org.apache.commons.lang3.ArrayUtils.contains;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Test(groups = "gradle")
public class GradleFileParserTest
{
  private static final String   PARSE_FILE_NAME = "gradle/parsetest.gradle";
  private static final String[] TASK_NAMES      =
  {
    "copyHelp",                            //
    "copyLibs",                            //
    "copyResources",                       //
    "formatTestResults",                   //
    "installApp",                          //
    "listRuntimeJars",                     //
    "publishWebstart",                     //
    "signJars",                            //
    "test",                                //
    "transform",                           //
    "wrapper"                              //
  };

  @Test
  public void testCanary()
  {
    assertTrue(true);
  }

  @Test(groups = "failed")
  public void testFailHere()
  {
    File there = new File(getFilePath(PARSE_FILE_NAME));

    ///Users/douglas_bullard/Documents/JavaStuff/Google_Code/AntScriptVisualizer_Google/gradleTrunk/build/.
    ///Users/douglas_bullard/Documents/JavaStuff/Google_Code/AntScriptVisualizer_Google/gradleTrunk/build/build/resources/test/gradle/parsetest.gradle
    if (false)
    {
      File here = new File(".");

      assertEquals(here.getAbsolutePath(), "dibble",
                   "Should have been some sort of path here.... \ndot path is: " + here.getAbsolutePath() + " parse file path is :"
                     + there.getAbsolutePath());
    }
  }

  @Test
  public void testReadLinesFromFile() throws IOException
  {
    List<String> lines = GradleFileParser.readLinesInFile(new File(getFilePath(PARSE_FILE_NAME)));

    assertFalse(lines.isEmpty());
  }

  @Test
  public void testFindTaskLines() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath(PARSE_FILE_NAME));

    List<Task> tasks = parser.getTasks();

    assertEquals(tasks.size(), TASK_NAMES.length, "Should have got a different size " + Arrays.toString(tasks.toArray()));
  }

  @Test
  public void testFindTaskNames() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath(PARSE_FILE_NAME));

    List<Task> tasks = parser.getTasks();

    for (Task task : tasks)
    {
      assertTrue(contains(TASK_NAMES, task.getName()), "Couldn't find task " + task);
    }
  }

  @Test
  public void testFindTaskNamesInMap() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath(PARSE_FILE_NAME));

    Map<String, Task> tasks = parser.getTasksMap();

    for (String taskName : TASK_NAMES)
    {
      assertTrue(tasks.containsKey(taskName));
    }
  }

  @Test
  public void testFindTasksWithDependencies() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath(PARSE_FILE_NAME));

    Map<String, Task> tasks = parser.getTasksMap();

    validateTaskDependencies(tasks, "formatTestResults", "test", 1);
  }

  @Test
  public void testFindFileImports() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath("gradle/importTasks.gradle"));

    Map<String, Task> tasks = parser.getTasksMap();

    assertTrue(tasks.containsKey("publishWebstart"));
  }

  @Test(groups = "failed")
  public void testFindUrlImports() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath("gradle/importTasksFromUrl.gradle"));

    Map<String, Task> tasks = parser.getTasksMap();

    assertTrue(tasks.containsKey("publishWebstart"));
  }

  @Test(groups = "failed")
  public void testFindUrlImportsWithAuthentication() throws IOException
  {
    // read saved values for resetting after test
    GradleScriptPreferences preferences       = new GradleScriptPreferences();
    boolean                 useProxy          = preferences.shouldUseHttpProxy();
    boolean                 useAuthentication = preferences.shouldUseProxyAuthentication();
    String                  proxyUserName     = preferences.getProxyUserName();
    String                  proxyPassword     = preferences.getProxyPassword();

    // store new values
    preferences.setUseHttpProxy(true);
    preferences.setUseProxyAuthentication(true);

    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath("gradle/importTasksFromUrl.gradle"));

    Map<String, Task> tasks = parser.getTasksMap();

    // restore the old values
    preferences.setUseHttpProxy(useProxy);
    preferences.setUseProxyAuthentication(useAuthentication);
    preferences.setProxyUserName(proxyUserName);
    preferences.setProxyPassword(proxyPassword);
    assertTrue(tasks.containsKey("publishWebstart"));
  }

  @Test
  public void testFindImportsWithChildImports() throws IOException
  {
    // Imports not using URLs are not being resolved relative to the top level script, but to each child.
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath("gradle/build-import-from-master.gradle"));

    Map<String, Task> tasks = parser.getTasksMap();

    assertTrue(tasks.containsKey("publishWebstart"));
    assertTrue(tasks.containsKey("generateCoverageReport"));
  }

  private static void validateTaskDependencies(Map<String, Task> tasks, String taskName, String dependsOnTaskName, int expectedSize)
  {
    Task task = tasks.get(taskName);

    assertNotNull(task);

    Task       dependsOnTask = tasks.get(dependsOnTaskName);
    List<Task> taskList      = task.getDependsOn();

    assertEquals(taskList.size(), expectedSize, " got back task list: " + Arrays.toString(taskList.toArray()));

    Task foundTask = taskList.get(expectedSize - 1);

    assertEquals(foundTask.getName(), dependsOnTask.getName());  // validate that the tasks are the same task name
    assertEquals(foundTask, dependsOnTask);                      // validate that the tasks are the same task object
  }

  // just doing this to get a printout of the tasks...
  @Test
  public void testBigFile() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.parseFile(getFilePath("gradle/master-build.gradle"));

    List<Task> tasks = parser.getTasks();

    for (Task task : tasks)
    {
      task.printTask(0);
    }
  }

  // test case like check.dependsOn integrationTest
  @Test
  public void testImplicitDeclarationTask() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());
    List<String>     list   = getLinesFromArray(new String[] { "task dibble", "check.dependsOn integrationTest" });

    parser.findTasksInLines(list, null);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("dibble"));  // no brainer, should already work
    assertTrue(tasksMap.containsKey("check"));
  }

  // test case like check.dependsOn integrationTest
  @Test
  public void testImplicitDeclarationTask2() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());
    List<String>     list   = getLinesFromArray(new String[] { "task dibble", "check.dependsOn integrationTest" });

    parser.findTasksInLines(list, null);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("integrationTest"));
  }

  // test case like check.dependsOn integrationTest
  @Test
  public void testImplicitDeclarationDependsOnTask() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());
    List<String>     list   = getLinesFromArray(new String[] { "task dibble", "check.dependsOn integrationTest" });

    parser.findTasksInLines(list, null);

    Map<String, Task> tasksMap = parser.getTasksMap();
    Task              task     = tasksMap.get("check");

    assertEquals(task.getDependsOn().get(0).getName(), "integrationTest");
  }

  // test cases like [funcTest, bddTest]*.dependsOn daemonModeTomcat
  @Test
  public void testListOfImplicitTaskDeclaration() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());
    List<String>     list   = getLinesFromArray(new String[] { "[funcTest, bddTest]*.dependsOn daemonModeTomcat" });

    parser.findTasksInLines(list, null);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("funcTest"));
    assertTrue(tasksMap.containsKey("bddTest"));
    assertTrue(tasksMap.containsKey("daemonModeTomcat"));
  }

  // test cases like [funcTest, bddTest]*.dependsOn daemonModeTomcat
  @Test
  public void testListOfImplicitTaskDeclarationDepends() throws IOException
  {
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());
    List<String>     list   = getLinesFromArray(new String[] { "[funcTest, bddTest]*.dependsOn daemonModeTomcat" });

    parser.findTasksInLines(list, null);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("funcTest"));
    assertTrue(tasksMap.containsKey("bddTest"));

    String daemonModeTomcat = "daemonModeTomcat";

    assertTrue(tasksMap.containsKey(daemonModeTomcat));
    assertEquals(tasksMap.get("funcTest").getDependsOn().get(0).getName(), daemonModeTomcat);
    assertEquals(tasksMap.get("bddTest").getDependsOn().get(0).getName(), daemonModeTomcat);
  }

  // idea for doing this - after task declaration, keep parsing lines keeping track of { and } - anything within
  // the matching {} pair can be claimed as a dependency.  So, take all those lines and put them into the task for
  // future reference as well.
  @Test
  public void testExecuteInDoFirst()
  {
    String[] lines =
    {
      "task tomcatRunMock(dependsOn: war, description: 'Runs Webapp using Mock resources (DB, LDAP)') {",  //
      "    doFirst {",                                                                                     //
      "        System.setProperty(\"spring.profiles.active\", \"InMemoryAuth,MockDB\")",                   //
      "        tomcatRun.execute()",                                                                       //
      "    }",                                                                                             //
      "    doLast {",                                                                                      //
      "        System.setProperty(\"spring.profiles.active\", \"InMemoryAuth,MockDB\")",                   //
      "        tomcatStop.execute()",                                                                      //
      "    }",                                                                                             // "}"
                                                                                                           //
                                                                                                           // //
    };
    List<String> list   = getLinesFromArray(lines);
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.findTasksInLines(list, null);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("tomcatRunMock"));
    assertTrue(tasksMap.containsKey("tomcatRun"));

    Task task = tasksMap.get("tomcatRunMock");

    assertEquals(task.getDependsOn().size(), 5);
  }

  // after task declaration, keep parsing lines keeping track of { and } - anything within
  // the matching {} pair can be claimed as a dependency.  So, take all those lines and put them into the task for
  // future reference as well.
  @Test
  public void testFindAllTaskLines()
  {
    String[] lines =
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
      "}"                                                                                                  //
    };
    List<String> list   = getLinesFromArray(lines);
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.findTasksInLines(list, null);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("tomcatRunMock"));

    Task     task      = tasksMap.get("tomcatRunMock");
    String[] taskLines = task.getScopeLines();

    assertEquals(taskLines, lines, "Should have all the lines for the task in the task");
  }

  // If multiple executes are specified, then execution order is presumed.  That means cleanFour depends on cleanThree being done, etc.
  @Test
  public void testExecutesOrder()
  {
    String[] lines =
    {
      "task tomcatRunMock(dependsOn: war, description: 'Runs Webapp using Mock resources (DB, LDAP)') {",  //
      "    cleanOne.execute()",                                                                            //
      "    cleanTwo.execute()",                                                                            //
      "    cleanThree.execute()",                                                                          //
      "    cleanFour.execute()",                                                                           //
      "}"                                                                                                  //
    };
    List<String> list   = getLinesFromArray(lines);
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.findTasksInLines(list, null);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("cleanOne"));
    assertTrue(tasksMap.containsKey("cleanTwo"));
    assertTrue(tasksMap.containsKey("cleanThree"));
    assertTrue(tasksMap.containsKey("cleanFour"));
    assertDependency(tasksMap, "cleanFour", "cleanThree");
    assertDependency(tasksMap, "cleanThree", "cleanTwo");
    assertDependency(tasksMap, "cleanTwo", "cleanOne");
  }

  private void assertDependency(Map<String, Task> tasksMap, String firstTaskName, String secondTaskName)
  {
    Task       task     = tasksMap.get(firstTaskName);
    List<Task> taskList = task.getDependsOn();

    assertTrue(taskList.contains(new Task(secondTaskName)));
  }

  @Test
  public void testPostDeclarationExecutes()
  {
    String[] lines =
    {
      "task tRun1 {",               //
      "  }"                         //
        + "task tRun2 {",           //
      "  }",                        //
      "",                           //
      "[tRun1, tRun2].each {",      //
      "   it.dependsOn('dibble')",  //
      "}"                           //
    };
    List<String> list   = getLinesFromArray(lines);
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.findTasksInLines(list, null);
    parser.findPostDeclarationTaskModifications(list);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("tRun1"));

    Task       task      = tasksMap.get("tRun1");
    List<Task> dependsOn = task.getDependsOn();

    assertEquals(dependsOn.size(), 1);

    Task task1 = dependsOn.get(0);

    assertEquals(task1.getName(), "dibble");
  }

  @Test
  public void testFindForEachTasks2()
  {
    String[] lines =
    {
      "task unitTest {",                                                                        //
      "  }"                                                                                     //
        + "check tRun2 {",                                                                      //
      "  }",                                                                                    //
      "[unitTest, check].each{",                                                                //
      "    it.doLast {",                                                                        //
      "        tomcatStop.execute()",                                                           //
      "    }",                                                                                  //
      "    it.doLast {",                                                                        //
      "        if (new File(srcCopy).exists()) {",                                              //
      "            // replace instrumented classes with backup copy again",                     //
      "            ant {",                                                                      //
      "                delete(file: srcOriginal)",                                              //
      "                move(file: srcCopy, tofile: srcOriginal)",                               //
      "            }",                                                                          //
      "            // end cobertura cleanup",                                                   //
      "",                                                                                       //
      "            // create cobertura reports",                                                //
      "            ant.'cobertura-report'(destdir:\"${project.buildDir}/reports/cobertura\",",  //
      "                    format:'html', srcdir:\"src/main/java\", datafile:cobSerFile)",      //
      "            ant.'cobertura-report'(destdir:\"${project.buildDir}/reports/cobertura\",",  //
      "                    format:'xml', srcdir:\"src/main/java\", datafile:cobSerFile)",       //
      "        }",                                                                              //
      "    }",                                                                                  //
      "}"                                                                                       //
    };
    List<String> list   = getLinesFromArray(lines);
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.findTasksInLines(list, null);
    parser.findPostDeclarationTaskModifications(list);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("unitTest"));

    Task       task      = tasksMap.get("check");
    List<Task> dependsOn = task.getDependsOn();

    assertEquals(dependsOn.size(), 1);

    Task task1 = dependsOn.get(0);

    assertEquals(task1.getName(), "tomcatStop");
  }

  @Test
  public void testFindForEachTasksUndeclaredTasks()
  {
    String[] lines =
    {
      "[unitTest, check].each{",                                                                //
      "    it.doLast {",                                                                        //
      "        tomcatStop.execute()",                                                           //
      "    }",                                                                                  //
      "    it.doLast {",                                                                        //
      "        if (new File(srcCopy).exists()) {",                                              //
      "            // replace instrumented classes with backup copy again",                     //
      "            ant {",                                                                      //
      "                delete(file: srcOriginal)",                                              //
      "                move(file: srcCopy, tofile: srcOriginal)",                               //
      "            }",                                                                          //
      "            // end cobertura cleanup",                                                   //
      "",                                                                                       //
      "            // create cobertura reports",                                                //
      "            ant.'cobertura-report'(destdir:\"${project.buildDir}/reports/cobertura\",",  //
      "                    format:'html', srcdir:\"src/main/java\", datafile:cobSerFile)",      //
      "            ant.'cobertura-report'(destdir:\"${project.buildDir}/reports/cobertura\",",  //
      "                    format:'xml', srcdir:\"src/main/java\", datafile:cobSerFile)",       //
      "        }",                                                                              //
      "    }",                                                                                  //
      "}"                                                                                       //
    };
    List<String> list   = getLinesFromArray(lines);
    GradleFileParser parser = new GradleFileParser(new GradleScriptPreferences());

    parser.findTasksInLines(list, null);
    parser.findPostDeclarationTaskModifications(list);

    Map<String, Task> tasksMap = parser.getTasksMap();

    assertTrue(tasksMap.containsKey("unitTest"));

    Task       task      = tasksMap.get("check");
    List<Task> dependsOn = task.getDependsOn();

    assertEquals(dependsOn.size(), 1);

    Task task1 = dependsOn.get(0);

    assertEquals(task1.getName(), "tomcatStop");
  }

  public void testFilterLine()
  {
    assertEquals(filterText("dibble${projectDir}/"), "dibble");
    assertEquals(filterText("dibble${project.projectDir}/"), "dibble");
    assertEquals(filterText("${project.projectDir}/dibble"), "dibble");
  }

  public void testFilterTripleQuotes()
  {
    assertEquals(filterTripleQuotes("\"\"\"${mongoCmd}\"\"\""), "${mongoCmd}");
  }

  public void testFilterEqualsDeclaration()
  {
    assertEquals(filterEqualsDeclaration("tomcatRun.execute()"), "tomcatRun.execute()");
    assertEquals(filterEqualsDeclaration("mongoExec = \"\"\"${mongoCmd}\"\"\".execute()"), "${mongoCmd}.execute()");
    assertEquals(filterEqualsDeclaration("def mongoExec = \"\"\"${mongoCmd}\"\"\".execute()"), "${mongoCmd}.execute()");
  }

  // test imported scripts recursively
  // ==>test find task dependsOn if task exists elsewhere in build script
  // test find dependsOn in task modification
  // test find dependsOn in iterative task modification
  // determine type of task
}
