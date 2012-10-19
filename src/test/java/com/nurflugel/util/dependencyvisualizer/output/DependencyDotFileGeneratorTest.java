package com.nurflugel.util.dependencyvisualizer.output;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static com.nurflugel.util.dependencyvisualizer.output.DependencyDotFileGenerator.createDotFileFromLines;
import static com.nurflugel.util.test.TestResources.getFilePath;
import static org.apache.commons.io.FileUtils.readLines;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/3/12 Time: 13:03 To change this template use File | Settings | File Templates. */
@Test(groups = "unit")
public class DependencyDotFileGeneratorTest
{
  public void testCreateOutput() throws Exception {}

  public void testWriteOutput() throws Exception {}

  public void testReplaceBadChars() throws Exception {}

  public void testReadFile1() throws IOException, NoConfigurationsFoundException
  {
    String                  filePath       = getFilePath("gradle/dependencies/Das_dependencies.txt");
    List<String>            lines          = readLines(new File(filePath));
    GradleDependencyParser  parser         = new GradleDependencyParser();
    GradleScriptPreferences preferences    = new GradleScriptPreferences();
    String                  outputFileName = "das_dibble.dot";

    createDotFileFromLines(parser, preferences, outputFileName, lines.toArray(new String[lines.size()]), Os.findOs());
  }

  @Test(groups = "long")
  public void testCreateOutputForFile() throws Exception, NoConfigurationsFoundException
  {
    File file = new File("/Users/douglas_bullard/Documents/JavaStuff/Google_Code/gradle-script-visualizer/trunk/build.gradle");  // todo parameterize
    DependencyDotFileGenerator generator = new DependencyDotFileGenerator();

    generator.createOutputForFile(file, new GradleDependencyParser(), new GradleScriptPreferences(), "dibble.dot", Os.findOs());
  }
}
