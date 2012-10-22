package com.nurflugel.util.dependencyvisualizer.output;

import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 10/19/12 Time: 22:10 To change this template use File | Settings | File Templates. */
public class MockDependencyDotFileGenerator extends DependencyDotFileGenerator
{
  /** for tests we want the "compile" dependency. */
  protected void getConfigurationFromDialog(List<Configuration> configurations, DependencyDotFileGenerator dependencyDotFileGenerator,
                                            GradleScriptPreferences preferences, Os os, String outputFileName)
  {
    for (Configuration configuration : configurations)
    {
      if (configuration.getName().equals("compile"))
      {
        generateOutputForConfigurations(preferences, configuration, outputFileName, os);
      }
    }
  }

  protected void createAndOpenFile(List<String> output, String outputFileName, DependencyDotFileGenerator dotFileGenerator, Os os)
                            throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException
  {
    File file = dotFileGenerator.writeOutput(output, outputFileName);
  }
}
