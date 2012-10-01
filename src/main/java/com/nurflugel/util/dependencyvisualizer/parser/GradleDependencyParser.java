package com.nurflugel.util.dependencyvisualizer.parser;

import com.nurflugel.util.dependencyvisualizer.domain.Artifact;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.apache.commons.lang3.StringUtils.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 18:36 To change this template use File | Settings | File Templates. */
public class GradleDependencyParser
{
  public static final String           DOTTED_LINE       = "------------------------------------------------------------";
  private static Map<String, Artifact> masterArtifactMap = new HashMap<String, Artifact>();
  private List<Configuration>          configurations    = new ArrayList<Configuration>();

  public static String parseKey(String line)
  {
    return substringAfterLast(line, " ");
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
    return countMatches(line, "|");
  }

  // -------------------------- OTHER METHODS --------------------------
  public void parseFile(File file) throws IOException
  {
    List<String> strings = FileUtils.readLines(file);
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
    {
      // skip any blank lines
      if (isBlank(lines[i]))
      {
        continue;
      }

      if (!pastHeaders)
      {
        pastHeaders = isAtLastLineOfHeaders(i, lines);
      }

      if (pastHeaders)
      {
        readConfigurations(i, lines);
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

    return lines[i].trim().equals(DOTTED_LINE) && lines[i - 1].trim().equals("Root project") && lines[i - 2].trim().equals(DOTTED_LINE);
  }

  private void readConfigurations(int i, String[] lines)
  {
    // now we're past the list line of headers - we can start picking up configurations.  A configuration is just a name with or without
    // dependencies afterwards, like so:
    // archives - Configuration for archive artifacts.
    // No dependencies
    //
    // or
    // compile - Classpath for compiling the main sources.
    // +--- org.jdom:jdom:1.0
    if (Configuration.isConfigurationLine(i, lines))
    {
      readConfiguration(i, lines);
    }
  }

  public static Configuration readConfiguration(int i, String[] lines)
  {
    String[]      tokens        = split(lines[i], "-");
    Configuration configuration = new Configuration(tokens[0].trim(), masterArtifactMap);

    if (tokens.length > 1)
    {
      configuration.setDescription(tokens[1].trim());
    }

    String[] configurationLines = Configuration.getConfigurationLines(i, lines);

    configuration.setLines(configurationLines);
    configuration.parseLines();

    return configuration;
  }
}
