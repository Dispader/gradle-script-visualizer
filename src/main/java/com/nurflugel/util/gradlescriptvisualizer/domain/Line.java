package com.nurflugel.util.gradlescriptvisualizer.domain;

/** Wrapper class for a line of text. */
public class Line
{
  private String text;

  public Line(String text)
  {
    this.text = text;
  }

  public String getText()
  {
    return text;
  }

  @Override
  public String toString()
  {
    return "Line{"
             + "text='" + text + '\'' + '}';
  }

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

    Line line = (Line) o;

    if (!text.equals(line.text))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return text.hashCode();
  }
}
