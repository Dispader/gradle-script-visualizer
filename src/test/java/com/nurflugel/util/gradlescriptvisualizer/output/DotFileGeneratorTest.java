package com.nurflugel.util.gradlescriptvisualizer.output;

import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.nurflugel.util.test.TestResources.getFilePath;
import static org.testng.Assert.assertEquals;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 6/2/12 Time: 19:18 To change this template use File | Settings | File Templates. */
@Test(groups = "unit")
public class DotFileGeneratorTest
{
  @Test
  public void testGenerateSimpleDotFile() throws IOException
  {
    String           gradleFileName = getFilePath("gradle/dasbuild.gradle");
    GradleFileParser parser         = new GradleFileParser(new HashMap<File, Long>(), new GradleScriptPreferences());

    parser.parseFile(gradleFileName);

    List<Task>       tasks            = parser.getTasks();
    DotFileGenerator dotFileGenerator = new DotFileGenerator();
    List<String>     lines            = dotFileGenerator.createOutput(tasks, new GradleScriptPreferences());

    dotFileGenerator.writeOutput(lines, gradleFileName);

    for (String line : lines)
    {
      System.out.println(line);
    }

    // Assert.fail("just failed");
  }

  @Test
  public void testBadCharacterReplacement()
  {
    String badPath  = "/svn/trunk/build/master:gradle/master-build.gradle";
    String goodPath = DotFileGenerator.replaceBadChars(badPath);

    assertEquals(goodPath, "_svn_trunk_build_master_gradle_master_build_gradle");
  }
}
