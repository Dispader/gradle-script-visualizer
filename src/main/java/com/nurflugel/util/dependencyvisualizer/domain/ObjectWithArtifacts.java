package com.nurflugel.util.dependencyvisualizer.domain;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.parseKey;
import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.parseNestingLevel;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 13:16 To change this template use File | Settings | File Templates. */
@SuppressWarnings("ClassReferencesSubclass")
public abstract class ObjectWithArtifacts
{
  protected String                name;
  protected Map<String, Artifact> masterArtifactList;
  private List<String>            artifactKeys = new ArrayList<String>();
  private ObjectWithArtifacts     parent;

  protected ObjectWithArtifacts()
  {
    System.out.println("ObjectWithArtifacts.ObjectWithArtifacts");
  }

  protected ObjectWithArtifacts(String name, Map<String, Artifact> masterArtifactList)
  {
    this.name               = name;
    this.masterArtifactList = masterArtifactList;
  }

  // -------------------------- OTHER METHODS --------------------------
  public Artifact getArtifact(String key)
  {
    return masterArtifactList.get(key);
  }

  protected void parseArtifacts(int parentNestingLevel, ObjectWithArtifacts parentArtifact, String... parentArtifactLines)
  {
    // find all lines for "this" artifact
    for (int i = 1; i < parentArtifactLines.length; i++)
    {
      String line = parentArtifactLines[i];

      if (line.equals("No dependencies"))
      {
        return;
      }

      int lineNestingLevel = parseNestingLevel(line);

      // we're adding a sibling to the current level
      if (lineNestingLevel == parentNestingLevel)
      {
        // todo should I even ever get here?
        // todo this doesn't work as it adds nested child to parent
        Artifact artifact = addChildArtifact(parentArtifact, line);

        addArtifact(artifact);
      }

      // dependencies of the current artifact
      else if (lineNestingLevel > parentNestingLevel)
      {
        String[] newLines = getArtifactLines(new Pointer(i), parentArtifactLines);
        Artifact artifact = addChildArtifact(parentArtifact, line);

        addArtifact(artifact);
        parseArtifacts(lineNestingLevel, artifact, newLines);
      }
      else  // lineNestingLevel < currentNestingLevel
      {
        // currentArtifact = currentArtifact.getParent();
        // addChildArtifact(this, line);
        return;  // pop off the stack
      }
    }
  }

  /** For now, just keep reading lines until you get an empty one. */
  public static String[] getArtifactLines(Pointer startingIndex, String... lines)
  {
    List<String> lineList = new ArrayList<String>();
    int          index    = startingIndex.getIndex();
    String       topLine  = lines[index];

    lineList.add(topLine);

    int nestingLevel = parseNestingLevel(topLine);

    for (int i = index + 1; i < lines.length; i++)
    {
      String line = lines[i];

      // we return if the nesting level of the line in question is the same as the "parent" level
      int childNestingLevel = parseNestingLevel(line);

      if (childNestingLevel == nestingLevel)
      {
        startingIndex.increment(lineList.size());

        // return new array of startIndex to i
        return lineList.toArray(new String[lineList.size()]);
      }
      else
      {
        lineList.add(line);
      }
    }

    startingIndex.increment(lineList.size());

    return lineList.toArray(new String[lineList.size()]);
  }

  private Artifact addChildArtifact(ObjectWithArtifacts parentArtifact, String line)
  {
    // we're going to recurse into the child
    String   key      = parseKey(line);
    Artifact artifact = masterArtifactList.containsKey(key) ? masterArtifactList.get(key)
                                                            : new Artifact(key, masterArtifactList);

    parentArtifact.addArtifact(artifact);

    return artifact;
  }

  public void addArtifact(Artifact artifact)
  {
    artifactKeys.add(artifact.getKey());
    artifact.setParent(this);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public List<String> getArtifactKeys()
  {
    return artifactKeys;
  }

  public List<Artifact> getArtifacts()
  {
    List<Artifact> artifacts = new ArrayList<Artifact>();

    for (String key : artifactKeys)
    {
      Artifact artifact = masterArtifactList.get(key);

      artifacts.add(artifact);
    }

    return artifacts;
  }

  public Map<String, Artifact> getMasterArtifactList()
  {
    return masterArtifactList;
  }

  public String getName()
  {
    return name;
  }

  public ObjectWithArtifacts getParent()
  {
    return parent;
  }

  public void setParent(ObjectWithArtifacts parent)
  {
    this.parent = parent;
  }
}
