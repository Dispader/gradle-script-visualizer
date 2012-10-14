package com.nurflugel.gradle.ui.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Extracts stack trace from Throwable. */
public class StacktraceExtractor
{
  private StacktraceExtractor() {}

  public static String extract(Throwable t)
  {
    StringWriter sw = new StringWriter();

    try(

        PrintWriter pw = new PrintWriter(sw))
    {
      t.printStackTrace(pw);
    }

    return sw.toString();
  }
}
