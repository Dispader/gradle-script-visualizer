package com.nurflugel.util.dependencyvisualizer.domain;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.nurflugel.util.dependencyvisualizer.domain.ObjectWithArtifacts.getArtifactLines;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/4/12 Time: 12:39 To change this template use File | Settings | File Templates. */
public class ObjectWithArtifactsTest
{
  private final String[] sampleLines =
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
    "",
  };

  @Test
  public void testGetArtifactLines() throws Exception
  {
    Pointer  pointer = new Pointer(0);
    String[] lines   = getArtifactLines(pointer, sampleLines);

    assertEquals(pointer.getIndex(), 23);
    assertEquals(lines.length, 23);

    // now read in an artifact witih dependencies
    pointer = new Pointer(2);
    lines   = getArtifactLines(pointer, sampleLines);
    assertEquals(pointer.getIndex(), 8);
    assertEquals(lines.length, 6);
    pointer = new Pointer(5);
    lines   = getArtifactLines(pointer, sampleLines);
    assertEquals(pointer.getIndex(), 7);
    assertEquals(lines.length, 2);
  }

  @Test
  public void testGetArtifacts() throws Exception
  {
    Map<String, Artifact> masterArtifactList = new HashMap<String, Artifact>();
    Configuration         configuration      = new Configuration("dibble", masterArtifactList);
    String[]              artifactLines      = getArtifactLines(new Pointer(0), sampleLines);
    List<Artifact>        artifacts          = configuration.parseArtifacts(0, artifactLines);

    assertNotNull(configuration.getArtifact("org.jdom:jdom:1.0"));
    assertNotNull(configuration.getArtifact("org.tmatesoft.svnkit:svnkit:1.7.4-v1"));
    assertNotNull(configuration.getArtifact("com.ryangrier.ant:version_tool:1.1.4_fixed"));
    assertNotNull(configuration.getArtifact("com.nurflugel:buildtasks:1.0-SNAPSHOT"));
    assertNotNull(configuration.getArtifact("org.apache:commons-logging:1.0.4"));
    assertNotNull(configuration.getArtifact("org.apache:commons-lang:2.4"));
    assertNotNull(configuration.getArtifact("org.apache:commons-collections:3.2.1"));
    assertNotNull(configuration.getArtifact("org.apache:commons-io:2.2"));
    assertNotNull(configuration.getArtifact("com.intellij:forms_rt:11.0.3"));
    assertNotNull(configuration.getArtifact("org.apache:log4j:1.2.15"));
    assertNotNull(configuration.getArtifact("org.testng:testng:6.4"));
    assertNotNull(configuration.getArtifact("org.objectweb.asm:asm:3.1"));
    assertNotNull(configuration.getArtifact("javax.help:jhall:2.0.6"));
    assertEquals(artifacts.size(), 13);
  }

  @Test
  public void testGetSubArtifacts() throws Exception
  {
    Map<String, Artifact> masterArtifactList = new HashMap<String, Artifact>();
    Artifact              artifact           = new Artifact("org.tmatesoft.svnkit:svnkit:1.7.4-v1", masterArtifactList);
    String[]              artifactLines      = getArtifactLines(new Pointer(2), sampleLines);
    List<Artifact>        artifacts          = artifact.parseArtifacts(1, artifactLines);

    assertNotNull(artifact.getArtifact("de.regnis.q.sequence:sequence-library:1.0.2"));
    assertNotNull(artifact.getArtifact("net.java.dev.jna:jna:3.4.0"));

    Artifact subArtifact = artifact.getArtifact("corg.tmatesoft.sqljet:sqljet:1.1.1");

    assertTrue(artifacts.contains(subArtifact));
    assertNotNull(subArtifact.getArtifact("org.antlr:antlr-runtime:3.4"));
    assertNotNull(artifact.getArtifact("om.trilead:trilead-ssh2:1.0.0-build215"));
    assertEquals(artifacts.size(), 4);
  }
}
