package com.nurflugel.util.dependencyvisualizer.parser;

import com.nurflugel.util.dependencyvisualizer.domain.Artifact;
import com.nurflugel.util.dependencyvisualizer.domain.Configuration;
import com.nurflugel.util.dependencyvisualizer.domain.Pointer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import static com.nurflugel.util.dependencyvisualizer.domain.Configuration.isConfigurationLine;
import static com.nurflugel.util.dependencyvisualizer.domain.Configuration.readConfiguration;
import static org.apache.commons.io.FileUtils.readLines;
import static org.apache.commons.lang3.StringUtils.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 9/28/12 Time: 18:36 To change this template use File | Settings | File Templates. */
public class GradleDependencyParser
{
  public static final String           DOTTED_LINE       = "------------------------------------------------------------";
  private static Map<String, Artifact> masterArtifactMap = new HashMap<String, Artifact>();
  private List<Configuration>          configurations    = new ArrayList<Configuration>();

  public static Map<String, Artifact> getMasterArtifactMap()
  {
    return masterArtifactMap;
  }

  public static String parseKey(String line)
  {
    line = remove(line, " (*)");  // remove the characters that tell you this line has dependencies listed elsewhere
    line = substringBefore(line, " [").trim();

    return substringAfterLast(line, " ");
  }

  /*
   *
   * +--- org.tmatesoft.svnkit:svnkit:1.7.4-v1      0
   * |    +--- org.tmatesoft.sqljet:sqljet:1.1.1    1
   * |    |    \--- org.antlr:antlr-runtime:3.4     2, etc.
   *
   */
  public static int parseNestingLevel(String line)
  {
    return countMatches(line, "|") + countMatches(line, "+---") + countMatches(line, "\\---");
  }

  // -------------------------- OTHER METHODS --------------------------
  public void parseFile(File file) throws IOException
  {
    List<String> strings = readLines(file);
    String[]     lines   = strings.toArray(new String[strings.size()]);

    parseText(lines);
  }

  public void parseText(String... lines)
  {
    // do something here... read in each line - is it a key?
    // if so, if it's more deeply nested than the previous one, it's a dependency.  If it's at the same nesting level, it's a peer, if it's
    // less nested, you've moved out of a dependency.
    boolean pastHeaders = false;

    for (int i = 0; i < lines.length; i++)
    {
      // skip any blank lines
      if (isBlank(lines[i]))
      {
        continue;
      }

      if (!pastHeaders)
      {
        pastHeaders = isAtLastLineOfHeaders(i, lines);
      }

      if (pastHeaders)
      {
        configurations = readConfigurations(i, lines);

        break;
      }
    }
  }

  /**
   * Determine if we're at the last line of the header.
   *
   * <p>------------------------------------------------------------ Root project ------------------------------------------------------------</p>
   *
   * <p>We do this by looking at the current line and past two lines</p>
   */
  public static boolean isAtLastLineOfHeaders(int i, String... lines)
  {
    if (i < 3)
    {
      return false;
    }

    return lines[i].trim().equals(DOTTED_LINE) && lines[i - 1].trim().equals("Root project") && lines[i - 2].trim().equals(DOTTED_LINE);
  }

  protected static List<Configuration> readConfigurations(int i, String[] lines)
  {
    // now we're past the list line of headers - we can start picking up configurations.  A configuration is just a name with or without
    // dependencies afterwards, like so:
    // archives - Configuration for archive artifacts.
    // No dependencies
    //
    // or
    // compile - Classpath for compiling the main sources.
    // +--- org.jdom:jdom:1.0
    Pointer             pointer           = new Pointer(i);
    List<Configuration> configurationList = new ArrayList<Configuration>();

    while (pointer.getIndex() < lines.length)
    {
      if (isConfigurationLine(pointer, lines))
      {
        Configuration configuration = readConfiguration(pointer, lines, masterArtifactMap);

        configurationList.add(configuration);
      }
      else
      {
        pointer.increment();
      }
    }

    return configurationList;
  }

  public String[] runGradleExec(File gradleFile) throws IOException, InterruptedException
  {
    ProcessBuilder pb = new ProcessBuilder(gradleFile.getParent() + File.separator + "gradlew", "dependencies");

    pb.directory(gradleFile.getParentFile());
    pb.redirectErrorStream(true);

    Process shell = pb.start();

    // To capture output from the shell
    InputStream shellIn = shell.getInputStream();

    // Wait for the shell to finish and get the return code
    int shellExitStatus = shell.waitFor();

    System.out.println("Exit status" + shellExitStatus);

    String response = convertStreamToStr(shellIn);

    System.out.println("response = " + response);
    shellIn.close();

    String[] lines = response.split("\n");

    return lines;
  }

  /*
   * To convert the InputStream to String we use the Reader.read(char[]
   * buffer) method. We iterate until the Reader return -1 which means
   * there's no more data to read. We use the StringWriter class to
   * produce the string.
   */
  public static String convertStreamToStr(InputStream is) throws IOException
  {
    if (is != null)
    {
      Writer writer = new StringWriter();

      try
      {
        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        int    n;
        char[] buffer = new char[1024];

        while ((n = reader.read(buffer)) != -1)
        {
          writer.write(buffer, 0, n);
        }
      }
      finally
      {
        is.close();
      }

      return writer.toString();
    }
    else
    {
      return "";
    }
  }

  public List<Configuration> getConfigurations()
  {
    return configurations;
  }
}
