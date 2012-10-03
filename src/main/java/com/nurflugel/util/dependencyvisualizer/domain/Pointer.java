package com.nurflugel.util.dependencyvisualizer.domain;

/** Wrapper class for a pointer in an array. */
public class Pointer
{
  private int index;

  public Pointer(int index)
  {
    this.index = index;
  }

  // -------------------------- OTHER METHODS --------------------------
  public void increment()
  {
    index++;
  }

  public void increment(int amount)
  {
    index += amount;
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  public String toString()
  {
    return "Pointer{"
             + "index=" + index + '}';
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public int getIndex()
  {
    return index;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }
}
