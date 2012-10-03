package com.nurflugel.util.dependencyvisualizer.domain;

import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.nurflugel.util.dependencyvisualizer.domain.Configuration.isConfigurationLine;
import static com.nurflugel.util.dependencyvisualizer.domain.Configuration.readConfiguration;
import static com.nurflugel.util.dependencyvisualizer.domain.ObjectWithArtifacts.getArtifactLines;
import static org.testng.Assert.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 13:27 To change this template use File | Settings | File Templates. */
@Test(groups = "unit")
public class ConfigurationTest
{
  public void testAddFindArtifact()
  {
    Map<String, Artifact> artifacts     = populateMap();
    Configuration         configuration = new Configuration("dibble", artifacts);
    Artifact              artifact      = configuration.getArtifact("org.dibble:drabble:666");

    assertEquals(artifact.getOrg(), "org.dibble");
    assertEquals(artifact.getName(), "drabble");
    assertEquals(artifact.getRevision(), "666");

    List<Artifact> artifactKeys = artifact.getArtifacts();
    Artifact       artifact1    = artifactKeys.get(0);

    assertNotNull(artifact1);
    assertEquals(artifact1.getOrg(), "org.silly");
    assertEquals(artifact1.getName(), "sally");
    assertEquals(artifact1.getRevision(), "999");
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
    String[] configurationLines = getArtifactLines(new Pointer(0), lines);

    assertEquals(configurationLines.length, 1);

    // assertEquals(configurationLines[0], lines[1]);
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
    String[] configurationLines = getArtifactLines(new Pointer(3), lines);

    assertEquals(configurationLines.length, 4);
    assertEquals(configurationLines[0], "compile - Classpath for compiling the main sources.");
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
      "|    +--- org.tmatesoft.sqljet:sqljet:1.1.1",            //
      "|    |    \\--- org.antlr:antlr-runtime:3.4",            //
      "|    \\--- com.trilead:trilead-ssh2:1.0.0-build215",     //
      "+--- com.ryangrier.ant:version_tool:1.1.4_fixed"         //
    };
    Map<String, Artifact> map           = new HashMap<String, Artifact>();
    Configuration         configuration = readConfiguration(new Pointer(0), lines, map);

    assertEquals(configuration.getName(), "compile");

    Map<String, Artifact> masterArtifactList = configuration.getMasterArtifactList();

    assertNotNull(masterArtifactList.get("org.jdom:jdom:1.0"), "jdom should have been found");

    Artifact     artifact     = masterArtifactList.get("org.tmatesoft.svnkit:svnkit:1.7.4-v1");
    List<String> artifactKeys = artifact.getArtifactKeys();
    String       regnis       = "de.regnis.q.sequence:sequence-library:1.0.2";

    assertTrue(artifactKeys.contains(regnis), "regnis should have been found");
    assertTrue(artifactKeys.contains("com.trilead:trilead-ssh2:1.0.0-build215"), "trilead should have been found");

    String sqljet = "org.tmatesoft.sqljet:sqljet:1.1.1";

    assertTrue(artifactKeys.contains(sqljet), "sqljet should have been found");
    artifact     = masterArtifactList.get(sqljet);
    artifactKeys = artifact.getArtifactKeys();
    assertTrue(artifactKeys.contains("org.antlr:antlr-runtime:3.4"), "antlr should have been found");
  }

  // archives - Configuration for archive artifacts.
  // No dependencies
  //
  // or
  // compile - Classpath for compiling the main sources.
  // +--- org.jdom:jdom:1.0
  public void testIsConfigurationLine()
  {
    assertFalse(isConfigurationLine(new Pointer(0), "dibble", "dabble"));
    assertFalse(isConfigurationLine(new Pointer(0), "dibble", ""));
    assertTrue(isConfigurationLine(new Pointer(0), "dibble", "No dependencies"));
    assertTrue(isConfigurationLine(new Pointer(0), "dibble", "+--- org.jdom:jdom:1.0"));
  }

  private Map<String, Artifact> populateMap()
  {
    HashMap<String, Artifact> map      = new HashMap<String, Artifact>();
    Artifact                  artifact = new Artifact("org.dibble:drabble:666", map);

    map.put(artifact.getKey(), artifact);

    Artifact artifact2 = new Artifact("org.silly:sally:999", map);

    map.put(artifact2.getKey(), artifact2);
    artifact.addArtifact(artifact2);

    return map;
  }
}
