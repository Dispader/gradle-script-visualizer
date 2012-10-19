package com.nurflugel.util.dependencyvisualizer.domain;

import org.testng.annotations.Test;
import java.util.HashMap;
import static org.testng.Assert.assertEquals;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/18/12 Time: 19:35 To change this template use File | Settings | File Templates. */
@Test(groups = "unit")
public class ArtifactTest
{
  public void testParseIgnoreStar()
  {
    Artifact artifact = new Artifact("|    |    \\--- org.seleniumhq.selenium:selenium-remote-driver:2.20.0 (*)", new HashMap<String, Artifact>());

    assertEquals(artifact.getKey(), "org.seleniumhq.selenium:selenium-remote-driver:2.20.0");
    assertEquals(artifact.getRequestedRevision(), "2.20.0");
  }

  public void testParseVersionConflict()
  {
    Artifact artifact = new Artifact("|    |    |    |    +--- org.apache.httpcomponents:httpcore:4.1.2 -> 4.2 (*)", new HashMap<String, Artifact>());

    assertEquals(artifact.getRequestedRevision(), "4.1.2");
    assertEquals(artifact.getRevision(), "4.2");
  }
}
