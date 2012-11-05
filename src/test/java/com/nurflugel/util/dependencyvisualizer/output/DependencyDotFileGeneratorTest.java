package com.nurflugel.util.dependencyvisualizer.output;

import com.nurflugel.gradle.ui.dialog.ConfigurationChoiceDialog;
import com.nurflugel.gradle.ui.dialog.ConfigurationsDialogBuilder;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import static com.nurflugel.util.test.TestResources.getFilePath;

import static org.apache.commons.io.FileUtils.readLines;

import org.testng.Assert;

import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import java.util.List;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/3/12 Time: 13:03 To change this template use File | Settings | File Templates. */
@Test(groups = "unit")
public class DependencyDotFileGeneratorTest
{
  public void testCreateOutput() throws Exception {}

  public void testWriteOutput() throws Exception {}

  public void testReplaceBadChars() throws Exception {}

  public void testReadFile1() throws IOException, NoConfigurationsFoundException
  {
    String                  filePath    = getFilePath("gradle/dependencies/Das_dependencies.txt");
    List<String>            lines       = readLines(new File(filePath));
    GradleDependencyParser  parser      = new GradleDependencyParser();
    GradleScriptPreferences preferences = new GradleScriptPreferences();

    preferences.setShouldJustUseCompileConfig(true);

    String                     outputFileName = "das_dibble.dot";
    DependencyDotFileGenerator generator      = new MockDependencyDotFileGenerator();

    // generator.createDotFileFromLines(parser, preferences, outputFileName, lines.toArray(new String[lines.size()]), Os.findOs(), null);
  }

  @Test(groups = "long")
  public void testCreateOutputForFile() throws Exception, NoConfigurationsFoundException
  {
    File file = new File("/Users/douglas_bullard/Documents/JavaStuff/Google_Code/gradle-script-visualizer/trunk/build.gradle");  // todo parametrize
    DependencyDotFileGenerator generator = new DependencyDotFileGenerator();
    GradleScriptPreferences    preferences = new GradleScriptPreferences();

    preferences.setShouldJustUseCompileConfig(true);
    // todo fix this so it won't die because of the dialog generator.createOutputForFile(file, new GradleDependencyParser(), preferences,
    // "dibble.dot", Os.findOs());
  }
}
