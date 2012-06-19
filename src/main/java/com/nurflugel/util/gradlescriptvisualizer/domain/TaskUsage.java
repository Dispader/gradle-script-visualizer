package com.nurflugel.util.gradlescriptvisualizer.domain;

public enum TaskUsage
{
  GRADLE ("box", "black"),
  EXECUTE("ellipse", "red");

  public String getShape()
  {
    return shape;
  }

  public String getColor()
  {
    return color;
  }

  private final String shape;
  private final String color;

  TaskUsage(String shape, String color)
  {
    this.shape = shape;
    this.color = color;
  }
}
