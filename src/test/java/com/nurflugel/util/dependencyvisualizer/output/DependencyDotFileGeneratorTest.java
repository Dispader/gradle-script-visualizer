package com.nurflugel.util.dependencyvisualizer.output;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import org.testng.annotations.Test;
import java.io.File;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/3/12 Time: 13:03 To change this template use File | Settings | File Templates. */
@Test(groups = "unit")
public class DependencyDotFileGeneratorTest
{
  public void testCreateOutput() throws Exception {}

  public void testWriteOutput() throws Exception {}

  public void testReplaceBadChars() throws Exception {}

  public void testCreateOutputForFile() throws Exception
  {
    File file = new File("/Users/douglas_bullard/Documents/JavaStuff/Google_Code/gradle-script-visualizer/trunk/build.gradle");  // todo parameterize

    DependencyDotFileGenerator.createOutputForFile(file, new GradleDependencyParser(), new GradleScriptPreferences(), "dibble.dot");
  }
}
