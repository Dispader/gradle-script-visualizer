package com.nurflugel.util.dependencyvisualizer.domain;

import com.nurflugel.util.gradlescriptvisualizer.output.DotFileGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import java.util.Map;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 13:08 To change this template use File | Settings | File Templates. */
public class Artifact extends ObjectWithArtifacts
{
  public static final String COLON = ":";
  private String             org;

  // private String name;
  private String revision;

  public Artifact(String key, Map<String, Artifact> masterArtifactList)
  {
    super(key, masterArtifactList);
    key = StringUtils.substringBefore(key, " [");

    if (!masterArtifactList.containsKey(key))
    {
      masterArtifactList.put(key, this);
    }

    String[] strings = key.split(COLON);

    if (strings.length != 3)
    {
      System.out.println("Artifact.Artifact - expecting 3 args, got " + strings.length + " in " + key);
      // do something to tell user they've got bad input
    }
    else
    {
      org      = strings[0];
      name     = strings[1];
      revision = strings[2];
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
    String color = "black";

    return getNiceDotName() + " [label=\"" + org + "\\n" + name + "\\n" + revision + "\" shape=" + shape + " color=" + color + " ]; ";
  }

  @Override
  public String getNiceDotName()
  {
    return DotFileGenerator.replaceBadChars(getKey());
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
}
