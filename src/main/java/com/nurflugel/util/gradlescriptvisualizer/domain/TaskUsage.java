package com.nurflugel.util.gradlescriptvisualizer.domain;

/** Enum to show the different types of task usage. Tasks invoked using execute() are shown in red (bad programmer, no donut). */
public enum TaskUsage
{
  GRADLE     ("box", "black"),
  GRADLE_TASK("box", "grey"),
  EXECUTE    ("ellipse", "red");

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
