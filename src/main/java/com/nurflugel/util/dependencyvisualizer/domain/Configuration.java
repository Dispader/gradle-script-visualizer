package com.nurflugel.util.dependencyvisualizer.domain;

import static com.nurflugel.util.gradlescriptvisualizer.output.ScriptDotFileGenerator.replaceBadChars;

import static org.apache.commons.lang3.StringUtils.split;

import java.util.Map;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 13:08 To change this template use File | Settings | File Templates. */
public class Configuration extends ObjectWithArtifacts
{
  public static final Configuration COMPILE     = new Configuration("compile", null);
  private String                    description;

  // is this a configuration line?
  public static boolean isConfigurationLine(Pointer pointer, String... lines)
  {
    int index = pointer.getIndex();

    if (index < (lines.length - 1))
    {
      String line = lines[index + 1];

      if (line != null)
      {
        if (line.equalsIgnoreCase("No dependencies"))
        {
          return true;
        }

        if (line.startsWith("+---"))
        {
          return true;
        }

        if (line.startsWith("| "))
        {
          return true;
        }

        if (line.startsWith("\\-"))
        {
          return true;
        }
      }
    }

    return false;
  }

  /** Read the configuration and it's artififacts from the lines. Increment the pointer as we do so. */
  public static Configuration readConfiguration(Pointer pointer, String[] lines, Map<String, Artifact> masterArtifactMap)
  {
    int           i             = pointer.getIndex();
    String[]      tokens        = split(lines[i], "-");
    Configuration configuration = new Configuration(tokens[0].trim(), masterArtifactMap);

    if (tokens.length > 1)
    {
      configuration.setDescription(tokens[1].trim());
    }

    String[] configurationLines = getArtifactLines(new Pointer(i), lines);  // todo can I just pass in the pointer to this?  Do I need to create a
                                                                            // new one?

    configuration.setLines(configurationLines);
    configuration.parseLines();
    pointer.increment(configurationLines.length);

    return configuration;
  }

  public Configuration(String name, Map<String, Artifact> masterArtifactList)
  {
    super(name, masterArtifactList);
  }
  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface Comparable ---------------------
  @Override
  public int compareTo(Object o)
  {
    return getName().compareTo(((Configuration) o).getName());
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  public String getDotDeclaration()
  {
    return "label=\"" + getName() + "\"\n";
  }

  @Override
  public String getNiceDotName()
  {
    return replaceBadChars(name);
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }

    if ((o == null) || (getClass() != o.getClass()))
    {
      return false;
    }

    Configuration that = (Configuration) o;

    if ((getName() != null) ? (!getName().equals(that.getName()))
                            : (that.getName() != null))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return (getName() != null) ? getName().hashCode()
                               : 0;
  }

  @Override
  public String toString()
  {
    return "Configuration{"
             + "name='" + getName() + '\'' + '}';
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
