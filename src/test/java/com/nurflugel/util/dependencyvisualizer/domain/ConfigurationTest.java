package com.nurflugel.util.dependencyvisualizer.domain;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import junit.framework.Assert;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static junit.framework.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

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
