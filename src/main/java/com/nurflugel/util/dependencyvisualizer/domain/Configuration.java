package com.nurflugel.util.dependencyvisualizer.domain;

import java.util.Map;
import static org.apache.commons.lang3.StringUtils.split;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 13:08 To change this template use File | Settings | File Templates. */
public class Configuration extends ObjectWithArtifacts
{
  private String[] lines;
  private String   description;

  // is this a configuration line?
  public static boolean isConfigurationLine(Pointer pointer, String... lines)
  {
    int index = pointer.getIndex();

    if (index < lines.length)
    {
      // todo nice to use Java 7 switch with strings here...
      if (lines[index + 1].equalsIgnoreCase("No dependencies"))
      {
        return true;
      }

      if (lines[index + 1].startsWith("+---"))
      {
        return true;
      }

      if (lines[index + 1].startsWith("| "))
      {
        return true;
      }

      if (lines[index + 1].startsWith("\\-"))
      {
        return true;
      }
    }

    return false;
  }

  public static Configuration readConfiguration(Pointer pointer, String[] lines, Map<String, Artifact> masterArtifactMap)
  {
    int           i             = pointer.getIndex();
    String[]      tokens        = split(lines[i], "-");
    Configuration configuration = new Configuration(tokens[0].trim(), masterArtifactMap);

    if (tokens.length > 1)
    {
      configuration.setDescription(tokens[1].trim());
    }

    String[] configurationLines = getArtifactLines(new Pointer(i), lines);     // todo can I just pass in the pointer to this?  Do I need to create a
                                                                               // new one?

    configuration.setLines(configurationLines);
    configuration.parseLines();
    pointer.increment(configurationLines.length);

    return configuration;
  }

  public void setLines(String[] lines)
  {
    this.lines = lines;
  }

  /** Go through the configuration and parse the lines. */
  public void parseLines()
  {
    parseArtifacts(0, this, lines);
  }

  public Configuration(String name, Map<String, Artifact> masterArtifactList)
  {
    super(name, masterArtifactList);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }
}
