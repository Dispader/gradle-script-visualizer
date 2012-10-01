package com.nurflugel.util.dependencyvisualizer.domain;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 13:08 To change this template use File | Settings | File Templates. */
public class Configuration extends ObjectWithArtifacts
{
  private String[] lines;
  private String   description;

  public Configuration(String name, Map<String, Artifact> masterArtifactList)
  {
    super(name, masterArtifactList);
  }

  // is this a configuration line?
  public static boolean isConfigurationLine(int i, String... lines)
  {
    if (i < lines.length)
    {
      // todo nice to use Java 7 switch with strings here...
      if (lines[i + 1].equalsIgnoreCase("No dependencies"))
      {
        return true;
      }

      if (lines[i + 1].startsWith("+---"))
      {
        return true;
      }

      if (lines[i + 1].startsWith("| "))
      {
        return true;
      }

      if (lines[i + 1].startsWith("\\-"))
      {
        return true;
      }
    }

    return false;
  }

  /** For now, just keep reading lines until you get an empty one. */
  public static String[] getConfigurationLines(int startingIndex, String... lines)
  {
    List<String> configurationLineList = new ArrayList<String>();

    for (int i = startingIndex + 1; i < lines.length; i++)
    {
      String line = lines[i];

      if (isEmpty(line))
      {
        // return new array of startIndex to i
        return configurationLineList.toArray(new String[configurationLineList.size()]);
      }
      else
      {
        configurationLineList.add(line);
      }
    }

    return configurationLineList.toArray(new String[configurationLineList.size()]);
  }
  // -------------------------- OTHER METHODS --------------------------

  /** Go through the configuration and parse the lines. */
  // todo put this into parent class and make it recursive
  public void parseLines()
  {
    int                 currentNestingLevel  = 0;
    ObjectWithArtifacts currentArtifact      = this;
    ObjectWithArtifacts currentChildArtifact = null;

    for (String line : lines)
    {
      if (line.equals("No dependencies"))
      {
        return;
      }
      else
      {
        // todo I really need to do is figure out a recursive algorithm for this
        int lineNestingLevel = GradleDependencyParser.parseNestingLevel(line);

        // we're adding a sibling to the current level
        if (lineNestingLevel == currentNestingLevel)
        {
          currentChildArtifact = addChildArtifact(line);
        }

        // dependencies of the current artifact
        else if (lineNestingLevel > currentNestingLevel)
        {
          currentChildArtifact = addChildToChild(currentChildArtifact, line);
          currentNestingLevel  = lineNestingLevel;
        }
        else  // lineNestingLevel < currentNestingLevel
        {
          currentArtifact = currentArtifact.getParent();
          addChildArtifact(line);
        }
      }
    }
  }

  private ObjectWithArtifacts addChildToChild(ObjectWithArtifacts currentChildArtifact, String line)
  {
    // we're going to recurse into the child
    String   key      = GradleDependencyParser.parseKey(line);
    Artifact artifact = new Artifact(key, getMasterArtifactList());

    currentChildArtifact.addArtifact(artifact);

    return artifact;
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

  public void setLines(String[] lines)
  {
    this.lines = lines;
  }
}
