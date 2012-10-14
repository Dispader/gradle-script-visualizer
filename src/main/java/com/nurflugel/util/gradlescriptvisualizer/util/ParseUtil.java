package com.nurflugel.util.gradlescriptvisualizer.util;

import java.util.ArrayList;
import java.util.List;
import static com.nurflugel.util.Util.CLOSE_CURLY_BRACE;
import static com.nurflugel.util.Util.OPEN_CURLY_BRACE;
import static org.apache.commons.lang3.StringUtils.countMatches;

/** Util class to help with parsing. */
public class ParseUtil
{
  /**
   * Go through the lines from the given line where the task is declared and get all the lines within the task scope for analysis.
   *
   * @param  line   the line where whatever is declared
   * @param  lines  the lines of the script
   */
  public static String[] findLinesInScope(String line, List<String> lines)
  {
    List<String> scopeLines = new ArrayList<>();
    int          index      = lines.indexOf(line);

    // in case the opening { is on the next (or later) lines, scan ahead until we find it
    while (!line.contains(OPEN_CURLY_BRACE) && (index < lines.size()))
    {
      index++;
    }

    String text = line;

    if (text.contains(OPEN_CURLY_BRACE))
    {
      scopeLines.add(text);

      int levelOfNesting = 0;

      levelOfNesting += countMatches(text, OPEN_CURLY_BRACE);
      levelOfNesting -= countMatches(text, CLOSE_CURLY_BRACE);  // I'm assuming braces will be nicely formatted and not all on one line, bad
                                                                // assumption as that's legal

      while ((levelOfNesting > 0) && (++index < lines.size()))
      {
        text = lines.get(index);
        scopeLines.add(text);
        levelOfNesting += countMatches(text, OPEN_CURLY_BRACE);
        levelOfNesting -= countMatches(text, CLOSE_CURLY_BRACE);
      }
    }

    return scopeLines.toArray(new String[scopeLines.size()]);
  }

  private ParseUtil() {}
}
