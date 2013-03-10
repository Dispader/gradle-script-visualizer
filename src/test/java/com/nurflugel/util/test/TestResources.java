package com.nurflugel.util.test;

import org.apache.commons.lang3.BooleanUtils;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

import java.util.ArrayList;
import java.util.List;

/** Util class to help with test resources. */
public class TestResources
{
  private static final String SOURCE_PATH_IDEA   = "build/resources/test/";
  private static final String SOURCE_PATH_GRADLE = "resources/test/";

  private TestResources() {}

  /** We do this because when unit tests run in the IDE the base file path is different than when running under Gradle, so we have to adjust it. */
  public static String getFilePath(String fileName)
  {
    if (fileName.startsWith("/"))
    {
      return fileName;
    }

    String  property            = System.getProperty("running.in.gradle");
    boolean isGradleEnvironment = toBooleanObject(property, "yes", null, "dibble");

    return isGradleEnvironment ? (SOURCE_PATH_GRADLE + fileName)
                               : (SOURCE_PATH_IDEA + fileName);
  }

  public static List<String> getLinesFromArray(String[]... lineArrays)
  {
    List<String> results = new ArrayList<String>();

    for (String[] lineArray : lineArrays)
    {
      for (String line : lineArray)
      {
        results.add(new String(line));
      }
    }

    return results;
  }
}
