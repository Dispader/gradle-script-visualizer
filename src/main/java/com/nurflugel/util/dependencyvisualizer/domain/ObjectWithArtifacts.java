package com.nurflugel.util.dependencyvisualizer.domain;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 13:16 To change this template use File | Settings | File Templates. */
@SuppressWarnings("ClassReferencesSubclass")
public abstract class ObjectWithArtifacts
{
  protected String                name;
  private List<String>            artifactKeys       = new ArrayList<String>();
  protected Map<String, Artifact> masterArtifactList;
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
  public void addArtifact(Artifact artifact)
  {
    artifactKeys.add(artifact.getKey());
    artifact.setParent(this);
  }

  public Artifact getArtifact(String key)
  {
    return masterArtifactList.get(key);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
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

  protected ObjectWithArtifacts addChildArtifact(String line)
  {
    String   key      = GradleDependencyParser.parseKey(line);
    Artifact artifact = new Artifact(key, getMasterArtifactList());

    addArtifact(artifact);

    return artifact;
  }
}
