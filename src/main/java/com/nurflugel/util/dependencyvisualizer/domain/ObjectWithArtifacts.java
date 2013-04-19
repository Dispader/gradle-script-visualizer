package com.nurflugel.util.dependencyvisualizer.domain;

import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.parseKey;
import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.parseNestingLevel;

import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.util.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 13:16 To change this template use File | Settings | File Templates. */
@SuppressWarnings({ "ClassReferencesSubclass", "ProtectedField" })
public abstract class ObjectWithArtifacts implements Comparable
{
  protected String                name;
  protected Map<String, Artifact> masterArtifactList = new HashMap<>();
  protected String[]              lines;
  private Set<String>             artifactKeys       = new TreeSet<>();
  // private ObjectWithArtifacts     parent;

  protected ObjectWithArtifacts()
  {
    System.out.println("ObjectWithArtifacts.ObjectWithArtifacts");
  }

  protected ObjectWithArtifacts(String name, Map<String, Artifact> masterArtifactList)
  {
    name                    = substringBefore(name, " [");
    this.name               = name;
    this.masterArtifactList = masterArtifactList;
  }

  // -------------------------- OTHER METHODS --------------------------
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

    // artifact.setParent(this);
  }

  public abstract String getDotDeclaration();

  public void outputDependencies(Set<String> output)
  {
    for (String artifactKey : artifactKeys)
    {
      Artifact artifact = getArtifact(artifactKey);

      if (artifact != null)                   // todo fix null artifacts
      {                                       // todo need a map of the dependsOn tasks, and the revision THIS object asked for

        boolean isSameRevision = artifact.getRequestedRevision().equals(artifact.getRevision());
        String  color          = isSameRevision ? "black"
                                                : "red";
        String edgeLabel = isSameRevision ? ""
                                          : (" label=\"" + artifact.getRequestedRevision() + "\" , ");
        String line = getNiceDotName() + " -> " + artifact.getNiceDotName() + "  [" + edgeLabel + "color=\"" + color + "\"];";

        output.add(line);
        artifact.outputDependencies(output);  // todo will this do duplicates?  Need to do a set to reduce dups?
      }
    }
  }

  public Artifact getArtifact(String key)
  {
    return masterArtifactList.get(key);
  }

  public abstract String getNiceDotName();

  /** Go through and parse the lines. */
  public void parseLines()
  {
    int nestingLevel = parseNestingLevel(lines[0]);

    parseArtifacts(nestingLevel, lines);
  }

  protected List<Artifact> parseArtifacts(int parentNestingLevel, String... lines)
  {
    List<Artifact> foundArtifacts = new ArrayList<>();

    // find all lines for "this" artifact
    for (int i = 1; i < lines.length; i++)
    {
      String line = lines[i];

      if (line.equals("No dependencies"))
      {
        return foundArtifacts;
      }

      int lineNestingLevel = parseNestingLevel(line);

      // dependencies of the current artifact
      if (lineNestingLevel == (parentNestingLevel + 1))
      {
        String[] newLines = getArtifactLines(new Pointer(i), lines);
        Artifact artifact = new Artifact(line, masterArtifactList);

        foundArtifacts.add(artifact);
        addArtifact(artifact);
        artifact.parseArtifacts(lineNestingLevel, newLines);
        i += newLines.length - 1;
      }
      else  // lineNestingLevel < currentNestingLevel
      {
        // currentArtifact = currentArtifact.getParent();
        // addChildArtifact(this, line);
        return foundArtifacts;  // pop off the stack
      }
    }

    return foundArtifacts;
  }

  /** Start at the given nesting level - read lines until you get to line with equal to or greater than the current one. */
  public static String[] getArtifactLines(Pointer startingIndex, String... lines)
  {
    List<String> lineList = new ArrayList<>();
    int          index    = startingIndex.getIndex();
    String       topLine  = lines[index];

    lineList.add(topLine);  // add the line which defines this object - all others, if they exist, are children.

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

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Collection<String> getArtifactKeys()
  {
    return artifactKeys;
  }

  public List<Artifact> getArtifacts()
  {
    List<Artifact> artifacts = new ArrayList<>();

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

  // public ObjectWithArtifacts getParent()
  // {
  // return parent;
  // }
  //
  // public void setParent(ObjectWithArtifacts parent)
  // {
  // this.parent = parent;
  // }
  public void setLines(String... lines)
  {
    this.lines = lines;
  }
}
