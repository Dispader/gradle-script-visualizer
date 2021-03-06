package com.nurflugel.util.dependencyvisualizer.parser;

import com.nurflugel.gradle.ui.dialog.Dialog;
import static com.nurflugel.gradle.ui.dialog.Dialog.showErrorDialog;

import com.nurflugel.util.dependencyvisualizer.domain.Artifact;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import static com.nurflugel.util.dependencyvisualizer.domain.Configuration.isConfigurationLine;
import static com.nurflugel.util.dependencyvisualizer.domain.Configuration.readConfiguration;
import com.nurflugel.util.dependencyvisualizer.domain.Pointer;
import com.nurflugel.util.dependencyvisualizer.output.DependencyDotFileGenerator;
import com.nurflugel.util.dependencyvisualizer.output.NoConfigurationsFoundException;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;

import static org.apache.commons.io.FileUtils.readLines;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 18:36 To change this template use File | Settings | File Templates. */
public class GradleDependencyParser
{
  public static final String           DOTTED_LINE       = "------------------------------------------------------------";
  private static Map<String, Artifact> masterArtifactMap = new HashMap<>();
  public static final String           CONFLICT_ARROW    = " ->";
  public static final String           COLON             = ":";
  private List<Configuration>          configurations    = new ArrayList<>();
  public static final String           THREE_DASHES      = "---";

  public static Map<String, Artifact> getMasterArtifactMap()
  {
    return masterArtifactMap;
  }

  // org.apache.httpcomponents:httpcore:4.1.2 -> 4.2
  public static String parseKey(String originalLine)
  {
    String keyWithoutNumber = substringBeforeLast(originalLine, COLON);

    if (keyWithoutNumber.contains(THREE_DASHES))
    {
      keyWithoutNumber = substringAfter(keyWithoutNumber, THREE_DASHES).trim();
    }

    String requestedRevision = parseRequestedRevision(originalLine);
    String resolvedRevision  = parseResolvedRevision(originalLine);
    String key               = keyWithoutNumber + COLON + (requestedRevision.equals(resolvedRevision) ? requestedRevision
                                                                                                      : resolvedRevision);

    return key;
  }

  // org.apache.httpcomponents:httpcore:   4.1.2 -> 4.2
  public static String parseRequestedRevision(String originalLine)
  {
    String line = substringAfterLast(originalLine, COLON).trim();

    line = substringBefore(line, " ").trim();

    return line;
  }

  // org.apache.httpcomponents:httpcore:   4.1.2 -> 4.2
  public static String parseResolvedRevision(String originalLine)
  {
    String revision = substringAfter(originalLine, CONFLICT_ARROW).trim();

    if (isEmpty(revision))
    {
      revision = parseRequestedRevision(originalLine);
    }

    revision = substringBefore(revision, " ");

    return revision;
  }

  private static boolean findHasConflict(String line)
  {
    return line.contains(CONFLICT_ARROW);
  }

  /*
   *
   * +--- org.tmatesoft.svnkit:svnkit:1.7.4-v1      0
   * |    +--- org.tmatesoft.sqljet:sqljet:1.1.1    1
   * |    |    \--- org.antlr:antlr-runtime:3.4     2, etc.
   *
   */
  public static int parseNestingLevel(String line)
  {
    return countMatches(line, "|") + countMatches(line, '+' + THREE_DASHES) + countMatches(line, '\\' + THREE_DASHES);
  }

  // -------------------------- OTHER METHODS --------------------------
  // todo this is only used in tests
  public void parseFile(File file) throws IOException
  {
    List<String> strings = readLines(file);
    String[]     lines   = strings.toArray(new String[strings.size()]);

    parseText(lines);
  }

  public void parseText(String... lines)
  {
    // do something here... read in each line - is it a key?
    // if so, if it's more deeply nested than the previous one, it's a dependency.  If it's at the same nesting level, it's a peer, if it's
    // less nested, you've moved out of a dependency.
    boolean pastHeaders = false;

    for (int i = 0; i < lines.length; i++)
    {  // skip any blank lines

      if (isBlank(lines[i]))
      {
        continue;
      }

      if (lines[i].startsWith("Download"))
      {
        continue;
      }

      if (!pastHeaders)
      {
        pastHeaders = isAtLastLineOfHeaders(i, lines);
      }

      if (pastHeaders)
      {
        configurations = readConfigurations(i, lines);

        break;
      }
    }
  }

  /**
   * Determine if we're at the last line of the header.
   *
   * <p>------------------------------------------------------------ Root project ------------------------------------------------------------</p>
   *
   * <p>We do this by looking at the current line and past two lines</p>
   */
  public static boolean isAtLastLineOfHeaders(int i, String... lines)
  {
    if (i < 3)
    {
      return false;
    }

    return lines[i].trim().equals(DOTTED_LINE) && lines[i - 1].trim().startsWith("Root project") && lines[i - 2].trim().equals(DOTTED_LINE);
  }

  protected static List<Configuration> readConfigurations(int i, String... lines)
  {
    // now we're past the list line of headers - we can start picking up configurations.  A configuration is just a name with or without
    // dependencies afterwards, like so:
    // archives - Configuration for archive artifacts.
    // No dependencies
    //
    // or
    // compile - Classpath for compiling the main sources.
    // +--- org.jdom:jdom:1.0
    Pointer             pointer           = new Pointer(i);
    List<Configuration> configurationList = new ArrayList<>();

    while (pointer.getIndex() < lines.length)
    {
      if (isConfigurationLine(pointer, lines))
      {
        Configuration configuration = readConfiguration(pointer, lines, masterArtifactMap);

        if (!configuration.getArtifacts().isEmpty())
        {
          configurationList.add(configuration);
        }
      }
      else
      {
        pointer.increment();
      }
    }

    return configurationList;
  }

  public List<Configuration> getConfigurations()
  {
    return configurations;
  }

  public void parseDependencies(Os os, File gradleFile, GradleScriptPreferences preferences) throws Exception
  {
    try
    {
      DependencyDotFileGenerator generator      = new DependencyDotFileGenerator();
      String                     baseName       = gradleFile.getName();
      String                     outputFileName = replace(baseName, ".gradle", ".dot");

      generator.createOutputForFile(gradleFile, this, preferences, outputFileName, os);
    }
    catch (NoConfigurationsFoundException e)
    {
      showErrorDialog("Unable to create graph", "No configurations were found in the build file");
    }
  }
}
