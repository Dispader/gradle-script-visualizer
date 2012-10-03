package com.nurflugel.util.dependencyvisualizer.domain;

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

    if (!masterArtifactList.containsKey(key))
    {
      masterArtifactList.put(key, this);
    }

    String[] strings = key.split(COLON);

    if (strings.length != 3)
    {
      // do something ot tell user they've got bad input
    }

    org      = strings[0];
    name     = strings[1];
    revision = strings[2];
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

  /** Something like "org.apache:commons-lang:2.2.1". */
  public String getKey()
  {
    return org + COLON + name + COLON + revision;
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
