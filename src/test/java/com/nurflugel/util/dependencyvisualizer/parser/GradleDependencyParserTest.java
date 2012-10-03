package com.nurflugel.util.dependencyvisualizer.parser;

import com.nurflugel.util.dependencyvisualizer.domain.Artifact;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import com.nurflugel.util.dependencyvisualizer.domain.Pointer;
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
}
