package com.nurflugel.util.dependencyvisualizer.parser;

import com.nurflugel.util.dependencyvisualizer.domain.Artifact;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;
import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.*;
import static org.testng.Assert.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/29/12 Time: 10:36 To change this template use File | Settings | File Templates. */
@Test(groups = "unit")
public class GradleDependencyParserTest
{
  public void testParseArtifact()
  {
    assertEquals("org.jdom:jdom:1.0", parseKey("+--- org.jdom:jdom:1.0"));
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
    assertEquals(0, parseNestingLevel("+--- org.tmatesoft.svnkit:svnkit:1.7.4-v1"));
  }

  public void testNestingLevel1()
  {
    assertEquals(1, parseNestingLevel(" |    +--- org.tmatesoft.sqljet:sqljet:1.1.1"));
  }

  public void testNestingLevel2()
  {
    assertEquals(2, parseNestingLevel("|    |    \\--- org.antlr:antlr-runtime:3.4"));
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

  // archives - Configuration for archive artifacts.
  // No dependencies
  //
  // or
  // compile - Classpath for compiling the main sources.
  // +--- org.jdom:jdom:1.0
  public void testIsConfigurationLine()
  {
    assertFalse(Configuration.isConfigurationLine(0, "dibble", "dabble"));
    assertFalse(Configuration.isConfigurationLine(0, "dibble", ""));
    assertTrue(Configuration.isConfigurationLine(0, "dibble", "No dependencies"));
    assertTrue(Configuration.isConfigurationLine(0, "dibble", "+--- org.jdom:jdom:1.0"));
  }

  public void testGetConfigurationBlockNoDependencies()
  {
    String[] lines =
    {
      "archives - Configuration for archive artifacts.",      //
      "No dependencies",                                      //
      "",                                                     //
      "compile - Classpath for compiling the main sources.",  //
      "+--- org.jdom:jdom:1.0",                               //
      "+--- org.tmatesoft.svnkit:svnkit:1.7.4-v1"
    };                                                        //
    String[] configurationLines = Configuration.getConfigurationLines(0, lines);

    assertEquals(configurationLines.length, 2);
    assertEquals(configurationLines[0], lines[0]);
    assertEquals(configurationLines[1], lines[1]);
  }

  public void testGetConfigurationBlockWithDependencies()
  {
    String[] lines =
    {
      "archives - Configuration for archive artifacts.",      //
      "No dependencies",                                      //
      "",                                                     //
      "compile - Classpath for compiling the main sources.",  //
      "+--- org.jdom:jdom:1.0",                               //
      "+--- org.tmatesoft.svnkit:svnkit:1.7.4-v1", "\\--- javax.help:jhall:2.0.6", "", "default - Configuration for default artifacts."
    };                                                        //
    String[] configurationLines = Configuration.getConfigurationLines(3, lines);

    assertEquals(configurationLines.length, 4);
    assertEquals(configurationLines[0], lines[3]);
    assertEquals(configurationLines[1], lines[4]);
    assertEquals(configurationLines[2], lines[5]);
    assertEquals(configurationLines[3], lines[6]);
  }

  public void testReadConfiguration()
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
      "\\--- javax.help:jhall:2.0.6"
    };
    Configuration configuration = readConfiguration(0, lines);

    assertEquals(configuration.getName(), "compile");

    Map<String, Artifact> masterArtifactList = configuration.getMasterArtifactList();

    assertNotNull(masterArtifactList.get("org.jdom:jdom:1.0"));

    Artifact       artifact  = masterArtifactList.get("org.tmatesoft.svnkit:svnkit:1.7.4-v1");
    List<Artifact> artifacts = artifact.getArtifacts();
    // check to see we got sqljet, and that got antlr
  }
}
