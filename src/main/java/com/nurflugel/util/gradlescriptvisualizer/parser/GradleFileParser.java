package com.nurflugel.util.gradlescriptvisualizer.parser;

import com.nurflugel.util.gradlescriptvisualizer.domain.Line;
import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Task.*;
import static com.nurflugel.util.gradlescriptvisualizer.util.ParseUtil.findLinesInScope;
import static org.apache.commons.io.FileUtils.checksumCRC32;
import static org.apache.commons.io.FileUtils.readLines;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.lang.StringUtils.*;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 5/30/12 Time: 22:07 To change this template use File | Settings | File Templates. */
public class GradleFileParser
{
  private Map<String, Task>       taskMap       = new HashMap<String, Task>();
  private Map<File, Long>         fileChecksums;
  private GradleScriptPreferences preferences;

  /** The starting point - the entry script. */
  private File baseFile;

  public static void addToTaskMap(Map<String, Task> taskMap, String name, Task task)
  {
    if (!isEmpty(name))
    {
      taskMap.put(name, task);
    }
  }

  public GradleFileParser(Map<File, Long> fileChecksums, GradleScriptPreferences preferences)
  {
    this.fileChecksums = fileChecksums;
    this.preferences   = preferences;
  }

  // -------------------------- OTHER METHODS --------------------------
  public List<Task> getTasks() throws IOException
  {
    return new ArrayList<Task>(taskMap.values());
  }

  public Map<String, Task> getTasksMap()
  {
    return taskMap;
  }

  public void parseFile(File file) throws IOException
  {
    System.out.println("file = " + file.getAbsolutePath());

    if (file.exists())
    {
      List<Line> lines = readLinesInFile(file);

      processLines(file, lines);
    }
    else
    {
      throw new FileNotFoundException("Expected file not found: " + file.getAbsolutePath());
    }
  }

  /**
   * We wrap the text lines into object lines so we can determine parsing strings or lines better. Later on, we may modify the line class to be more
   * broad than a single line of text.
   */
  static List<Line> readLinesInFile(File file) throws IOException
  {
    List<Line>   lines     = new ArrayList<Line>();
    List<String> textLines = readLines(file);

    for (String textLine : textLines)
    {
      lines.add(new Line(textLine));
    }

    return lines;
  }

  private void processLines(File sourceFile, List<Line> lines) throws IOException
  {
    String baseName = getBaseName(sourceFile.getAbsolutePath());

    findTasksInLines(lines, baseName);
    findImportsInFile(lines);
    findPostDeclarationTaskModifications(lines);
  }

  void findTasksInLines(List<Line> lines, String sourceFile)
  {
    for (Line line : lines)
    {
      String trimmedLine = line.getText().trim();

      if (trimmedLine.startsWith("task "))
      {
        Task task = findOrCreateTaskByLine(taskMap, line, lines, sourceFile);

        taskMap.put(task.getName(), task);
      }

      if (trimmedLine.contains(".dependsOn"))
      {
        findOrCreateImplicitTasksByLine(taskMap, trimmedLine);
      }

      if (trimmedLine.contains(".execute()"))
      {
        findOrCreateImplicitTasksByExecute(taskMap, trimmedLine);
      }
    }
  }

  void findImportsInFile(List<Line> lines) throws IOException
  {
    for (Line line : lines)
    {
      String text = line.getText().trim();

      if (text.startsWith("apply from: "))
      {
        text = substringAfter(text, "apply from: ");
        text = remove(text, '\'');
        text = remove(text, '\"');

        if (text.startsWith("http:"))
        {
          try
          {
            findUrlImport(text);
          }
          catch (IOException e)
          {
            // e.printStackTrace();//todo something where we tell the user we can't go through the firewall
          }
        }
        else
        {
          String fileName = trim(text);
          File   newFile  = new File(fileName);

          // non-absolute path must be resolved relative to the current file
          if (!fileName.startsWith("/"))
          {
            String parent = getFullPath(baseFile.getAbsolutePath());

            newFile = new File(parent, fileName);
          }

          parseFile(newFile);
        }
      }
    }
  }

  private void findUrlImport(String location) throws IOException
  {
    if (preferences.shouldUseHttpProxy())
    {
      Properties properties = System.getProperties();

      properties.setProperty("http.proxyHost", preferences.getProxyServerName());
      properties.setProperty("http.proxyPort", preferences.getProxyServerPort() + "");

      if (preferences.shouldUseProxyAuthentication())
      {
        String userName = preferences.getProxyUserName();

        if (userName != null)
        {
          properties.setProperty("http.proxyUser", userName);
        }

        String password = preferences.getProxyPassword();

        if (password != null)
        {
          properties.setProperty("http.proxyPassword", password);
        }
      }
    }

    URL        url       = new URL(location);
    String     bigString = IOUtils.toString(url);
    String[]   tokens    = bigString.split("\n");
    List<Line> lines     = new ArrayList<Line>();

    for (String token : tokens)
    {
      lines.add(new Line(token));
    }

    processLines(url, lines);
  }

  private void processLines(URL sourceUrl, List<Line> lines) throws IOException
  {
    String fileName = sourceUrl.toString();

    findTasksInLines(lines, fileName);
    findImportsFromUrl(lines);
    findPostDeclarationTaskModifications(lines);
  }

  void findImportsFromUrl(List<Line> lines) throws IOException
  {
    for (Line line : lines)
    {
      String text = line.getText().trim();

      if (text.startsWith("apply from: "))
      {
        text = substringAfter(text, "apply from: ");
        text = remove(text, '\'');
        text = remove(text, '\"');

        if (text.startsWith("http:"))
        {
          findUrlImport(text);
        }
        else
        {
          // todo we can't parse a file import in a remote URL - thow some sort of exception
        }
      }
    }
  }

  public void findPostDeclarationTaskModifications(List<Line> list)
  {
    for (Line line : list)
    {
      String text = line.getText();

      if (text.contains(".each"))
      {
        List<Task> tasks = findOrCreateTaskInForEach(line, taskMap);

        if (!tasks.isEmpty())
        {
          String[] linesInScope = findLinesInScope(line, list);

          for (String lineInScope : linesInScope)
          {
            if (lineInScope.contains("dependsOn"))
            {
              for (Task task : tasks)
              {
                task.findTaskDependsOn(taskMap, lineInScope, "dependsOn");
              }
            }

            if (lineInScope.contains("execute("))
            {
              for (Task task : tasks)
              {
                task.analyzeScopeLinesForExecuteDependencies(taskMap, linesInScope);
              }
            }
          }
          // todo work with iteration variable other than it
        }
      }
    }
  }

  public void parseFile(String fileName)  // todo establish base directory from first call?
  {
    File file = new File(fileName);

    if (file.exists())
    {
      baseFile = file;

      try
      {
        fileChecksums.put(file, checksumCRC32(file));
        parseFile(file);
      }
      catch (IOException e)
      {
        e.printStackTrace();              // todo log to user
      }
    }
    else
    {
      System.out.println("GradleFileParser.parseFile - couldn't find file " + fileName);
      // todo notify user file doesn't exist
    }
  }

  /** Keep results from one file corrupting another's output. */
  public void purgeAll()
  {
    taskMap.clear();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public void setBaseFile(File baseFile)
  {
    this.baseFile = baseFile;
  }
}
