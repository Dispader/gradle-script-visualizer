package com.nurflugel.util.dependencyvisualizer.domain;

import com.nurflugel.util.gradlescriptvisualizer.output.ScriptDotFileGenerator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import java.util.Map;
import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.parseKey;
import static com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser.parseRequestedRevision;

/** Representation of a dependency artifact. */
public class Artifact extends ObjectWithArtifacts
{
  public static final String COLON             = ":";
  private String             org;
  private String             revision;
  private String             requestedRevision;

  public Artifact(String line, Map<String, Artifact> masterArtifactList)
  {
    super(parseKey(line), masterArtifactList);
    requestedRevision = parseRequestedRevision(line);

    String key = name;

    if (!masterArtifactList.containsKey(key))
    {
      masterArtifactList.put(key, this);
    }

    String[] strings = key.split(COLON);

    if (strings.length == 3)
    {
      org      = strings[0];
      name     = strings[1];
      revision = strings[2];
    }
    else
    {
      System.out.println("Artifact.Artifact - expecting 3 args, got " + strings.length + " in " + key);
      // do something to tell user they've got bad input
    }
  }
  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface Comparable ---------------------
  @Override
  public int compareTo(Object o)
  {
    return getKey().compareTo(((Artifact) o).getKey());
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  public String getDotDeclaration()
  {
    String shape = "ellipse";
    String color = revision.equals(requestedRevision) ? "black"
                                                      : "red";

    return getNiceDotName() + " [label=\"" + org + "\\n" + name + "\\n" + revision + "\" shape=" + shape + " color=" + color + " ]; ";
  }

  @Override
  public String getNiceDotName()
  {
    return ScriptDotFileGenerator.replaceBadChars(getKey());
  }

  /** Something like "org.apache:commons-lang:2.2.1". */
  public String getKey()
  {
    return org + COLON + name + COLON + revision;
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    Artifact other = (Artifact) obj;

    return new EqualsBuilder().append(org, other.org).append(name, other.name).append(revision, other.revision).isEquals();
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder().append(org).append(name).append(revision).toHashCode();
  }

  @Override
  public String toString()
  {
    return "Artifact{"
             + "key='" + getKey() + '\'' + '}';
  }
  // --------------------- GETTER / SETTER METHODS ---------------------

  // public String getName()
  // {
  // return name;
  // }
  //
  // public void setName(String name)
  // {
  // this.name = name;
  // }
  public String getOrg()
  {
    return org;
  }

  public void setOrg(String org)
  {
    this.org = org;
  }

  public String getRevision()
  {
    return revision;
  }

  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  public String getRequestedRevision()
  {
    return requestedRevision;
  }
}
