package com.nurflugel.util.dependencyvisualizer.parser;

import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import com.nurflugel.util.test.TestResources;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.*;
import static org.testng.Assert.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/29/12 Time: 10:36 To change this template use File | Settings | File Templates. */
@Test(groups = "unit")
public class GradleDependencyParserTest
{
  public void testParseArtifact()
  {
    assertEquals(parseKey("+--- org.jdom:jdom:1.0"), "org.jdom:jdom:1.0");
  }

  public void testParseArtifactWithStar()
  {
    assertEquals(parseKey("+--- org.jdom:jdom:1.0 (*)"), "org.jdom:jdom:1.0");
  }

  public void testParseKeyWithRevisionConflict()
  {
    assertEquals(parseKey("|    |    |    |    +--- org.apache.httpcomponents:httpcore:4.1.2 -> 4.2 (*)"), "org.apache.httpcomponents:httpcore:4.2");
  }

  public void testParseKeyWithNoRevisionConflict()
  {
    assertEquals(parseKey("|    |    |    |    +--- org.apache.httpcomponents:httpcore:4.1.2"), "org.apache.httpcomponents:httpcore:4.1.2");
  }

  public void testRequestedRevision()
  {
    assertEquals(parseRequestedRevision("|    |    |    |    +--- org.apache.httpcomponents:httpcore:4.1.2 -> 4.2 (*)"), "4.1.2");
    assertEquals(parseRequestedRevision("|    |    |    |    +--- org.apache.httpcomponents:httpcore:4.1.2 (*)"), "4.1.2");
    assertEquals(parseRequestedRevision("|    |    |    |    +--- org.apache.httpcomponents:httpcore:4.1.2 "), "4.1.2");
  }

  public void testResolvedRevision()
  {
    assertEquals(parseResolvedRevision("|    |    |    |    +--- org.apache.httpcomponents:httpcore:4.1.2 -> 4.2 (*)"), "4.2");
  }

  /*
   *
   * +--- org.tmatesoft.svnkit:svnkit:1.7.4-v1      0
   * |    +--- org.tmatesoft.sqljet:sqljet:1.1.1    1
   * |    |    \--- org.antlr:antlr-runtime:3.4     2, etc.
   *
   */
  public void testNestingLevel0()
  {
    assertEquals(1, parseNestingLevel("+--- org.tmatesoft.svnkit:svnkit:1.7.4-v1"));
  }

  public void testNestingLevel1()
  {
    assertEquals(2, parseNestingLevel(" |    +--- org.tmatesoft.sqljet:sqljet:1.1.1"));
  }

  public void testNestingLevel2()
  {
    assertEquals(3, parseNestingLevel("|    |    \\--- org.antlr:antlr-runtime:3.4"));
  }

  public void testAtLastLineOfHeaders()
  {
    String[] lines =
    {
      "Dynamic properties are deprecated: http://gradle.org/docs/current/dsl/org.gradle.api.plugins.ExtraPropertiesExtension.html",                                                      //
      "Deprecated dynamic property: \"transtive\" on \"DefaultExternalModuleDependency{group='velocity', name='velocity', version='1.4', configuration='default'}\", value: \"true\".",  //
      "File to look for is /Users/douglas_bullard/Documents/JavaStuff/Google_Code/gradle-script-visualizer/trunk/src/main/java/overview.html",                                           //
      "Deprecated dynamic property: \"versionClassName\" on \"root project 'GradleScriptVisualizer'\", value: \"com.nurflugel.util.Util\".",                                             //
      ":dependencies",                                                                                                                                                                   //
      "",                                                                                                                                                                                //
      "------------------------------------------------------------",                                                                                                                    //
      "Root project",                                                                                                                                                                    //
      "------------------------------------------------------------",                                                                                                                    //
      ""                                                                                                                                                                                 //
    };

    assertFalse(isAtLastLineOfHeaders(0, lines));
    assertFalse(isAtLastLineOfHeaders(5, lines));
    assertTrue(isAtLastLineOfHeaders(8, lines));
  }

  public void testReadConfigurations()
  {
    String[] lines =
    {
      "compile - Classpath for compiling the main sources.",    //
      "+--- org.jdom:jdom:1.0",                                 //
      "+--- org.tmatesoft.svnkit:svnkit:1.7.4-v1",              //
      "|    +--- de.regnis.q.sequence:sequence-library:1.0.2",  //
      "|    +--- net.java.dev.jna:jna:3.4.0",                   //
      "|    +--- org.tmatesoft.sqljet:sqljet:1.1.1",            //
      "|    |    \\--- org.antlr:antlr-runtime:3.4",            //
      "|    \\--- com.trilead:trilead-ssh2:1.0.0-build215",     //
      "+--- com.ryangrier.ant:version_tool:1.1.4_fixed",        //
      "+--- com.nurflugel:buildtasks:1.0-SNAPSHOT",             //
      "+--- org.apache:commons-logging:1.0.4",                  //
      "+--- org.apache:commons-lang:2.4",                       //
      "+--- org.apache:commons-collections:3.2.1",              //
      "+--- org.apache:commons-io:2.2",                         //
      "+--- com.intellij:forms_rt:11.0.3",                      //
      "+--- org.apache:log4j:1.2.15",                           //
      "+--- org.testng:testng:6.4",                             //
      "|    +--- junit:junit:3.8.1",                            //
      "|    +--- org.beanshell:bsh:2.0b4",                      //
      "|    +--- com.beust:jcommander:1.12",                    //
      "|    \\--- org.yaml:snakeyaml:1.6",                      //
      "+--- org.objectweb.asm:asm:3.1",                         //
      "\\--- javax.help:jhall:2.0.6",                           //
      "",                                                       //
      "default - Configuration for default artifacts.",         //
      "+--- org.jdom:jdom:1.0",                                 //
      "+--- org.tmatesoft.svnkit:svnkit:1.7.4-v1",              //
      "|    +--- de.regnis.q.sequence:sequence-library:1.0.2",  //
      "|    +--- net.java.dev.jna:jna:3.4.0",                   //
      "|    +--- org.tmatesoft.sqljet:sqljet:1.1.1",            //
      "|    |    \\--- org.antlr:antlr-runtime:3.4",            //
      "|    \\--- com.trilead:trilead-ssh2:1.0.0-build215",     //
      "+--- com.ryangrier.ant:version_tool:1.1.4_fixed",        //
      "+--- com.nurflugel:buildtasks:1.0-SNAPSHOT",             //
      "+--- org.apache:commons-logging:1.0.4",                  //
      "+--- org.apache:commons-lang:2.4",                       //
      "+--- org.apache:commons-collections:3.2.1",              //
      "+--- org.apache:commons-io:2.2",                         //
      "+--- com.intellij:forms_rt:11.0.3",                      //
      "+--- org.apache:log4j:1.2.15",                           //
      "+--- org.testng:testng:6.4",                             //
      "|    +--- junit:junit:3.8.1",                            //
      "|    +--- org.beanshell:bsh:2.0b4",                      //
      "|    +--- com.beust:jcommander:1.12",                    //
      "|    \\--- org.yaml:snakeyaml:1.6",                      //
      "+--- org.objectweb.asm:asm:3.1",                         //
      "\\--- javax.help:jhall:2.0.6",                           //
      "",                                                       //
      "dist",                                                   //
      "No dependencies",                                        //
      "",                                                       //
      "javac2",                                                 //
      "+--- com.intellij:javac2:11.0.3",                        //
      "+--- com.intellij:annotations:11.0.3",                   //
      "+--- asm:asm-commons:3.3.1",                             //
      "|    \\--- asm:asm-tree:3.3.1",                          //
      "|         \\--- asm:asm:3.3.1",                          //
      "+--- asm:asm-parent:3.3.1",                              //
      "\\--- org.jdom:jdom:1.0\n"
    };                                                          //
    List<Configuration> configurations = readConfigurations(0, lines);

    assertEquals(configurations.size(), 3);
  }

  public void testReadFile() throws IOException
  {
    String                 filePath = TestResources.getFilePath("gradle/dependencies/dependencies1.txt");
    GradleDependencyParser parser   = new GradleDependencyParser();

    parser.parseFile(new File(filePath));

    List<Configuration> configurations = parser.getConfigurations();

    assertEquals(configurations.size(), 8);
  }

  @Test(groups = "long")
  public void testRunGradlew() throws IOException, InterruptedException
  {
    GradleDependencyParser parser = new GradleDependencyParser();
    // String[]               lines  = parser.runGradleExec(new
    // File("/Users/douglas_bullard/Documents/JavaStuff/Google_Code/gradle-script-visualizer/trunk/build.gradle"));

    // parser.parseText(lines);
    fail("Uncomment and fix");

    List<Configuration> configurations = parser.getConfigurations();

    assertEquals(configurations.size(), 13);
  }
}
