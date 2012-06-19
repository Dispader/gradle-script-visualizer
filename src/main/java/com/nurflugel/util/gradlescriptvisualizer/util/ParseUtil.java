package com.nurflugel.util.gradlescriptvisualizer.util;

import com.nurflugel.util.gradlescriptvisualizer.domain.Line;
import java.util.ArrayList;
import java.util.List;
import static org.apache.commons.lang.StringUtils.countMatches;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 6/10/12 Time: 18:34 To change this template use File | Settings | File Templates. */
public class ParseUtil
{
  /**
   * Go through the lines from the given line where the task is declared and get all the lines within the task scope for analysis.
   *
   * @param  line   the line where whatever is declared
   * @param  lines  the lines of the script
   */
  public static String[] findLinesInScope(Line line, List<Line> lines)
  {
    List<String> scopeLines = new ArrayList<String>();
    int          index      = lines.indexOf(line);

    // in case the opening { is on the next (or later) lines, scan ahead until we find it
    while (!line.getText().contains("{") && (index < lines.size()))
    {
      index++;
    }

    String text = line.getText();

    if (text.contains("{"))
    {
      scopeLines.add(text);

      int levelOfNesting = 0;

      levelOfNesting += countMatches(text, "{");
      levelOfNesting -= countMatches(text, "}");  // I'm assuming braces will be nicely formatted and not all on one line, bad assumption as that's
                                                  // legal

      while ((levelOfNesting > 0) && (++index < lines.size()))
      {
        text = lines.get(index).getText();
        scopeLines.add(text);
        levelOfNesting += countMatches(text, "{");
        levelOfNesting -= countMatches(text, "}");
      }
    }

    return scopeLines.toArray(new String[scopeLines.size()]);
  }
}
