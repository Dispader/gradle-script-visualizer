package com.nurflugel.util.gradlescriptvisualizer.parser;

import com.nurflugel.util.GraphicFileCreator;
import static com.nurflugel.util.Util.*;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Task.*;
import com.nurflugel.util.gradlescriptvisualizer.output.FileWatcher;
import com.nurflugel.util.gradlescriptvisualizer.output.ScriptDotFileGenerator;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleTaskOutputParser.mergeGradleTasksIntoExistingTasks;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleTaskOutputParser.parse;
import static com.nurflugel.util.gradlescriptvisualizer.parser.GradleTaskOutputParser.runGradleToGetTaskLines;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;
import static com.nurflugel.util.gradlescriptvisualizer.util.ParseUtil.findLinesInScope;

import static org.apache.commons.io.FileUtils.checksumCRC32;
import static org.apache.commons.io.FileUtils.readLines;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

import java.util.*;

/** Class with parses the Gradle scripts. */
public class GradleFileParser
{
  /** map of all the tasks found. */
  private Map<String, Task> taskMap = new HashMap<>();

  /** map of checksums for all the build files. If any of these change, the graph is regenerated. Presto! */
  private Map<File, Long>         fileChecksums = new HashMap<>();
  private GradleScriptPreferences preferences;
  private Os                      os;
  private final Set<File>         filesToRender = new HashSet<>();

  /** The starting point - the entry script. */
  private File    baseFile;
  private boolean showInternalGradleDependencies;

  public GradleFileParser(GradleScriptPreferences preferences)
  {
    this.preferences = preferences;
  }

  /** if the task doesn't exist in the map, add it. */
  public static void addToTaskMap(Map<String, Task> taskMap, String name, Task task)
  {
    if (!isEmpty(name))
    {
      taskMap.put(name, task);
    }
  }

  public GradleFileParser(GradleScriptPreferences preferences, Os os)
  {
    this.preferences = preferences;
    this.os          = os;
  }

  // -------------------------- OTHER METHODS --------------------------
  /** Gets a list of the tasks in the task map. */
  public List<Task> getTasks() throws IOException
  {
    return new ArrayList<>(taskMap.values());
  }

  public Map<String, Task> getTasksMap()
  {
    return taskMap;
  }

  /** Parse the given file. */
  public void parseFile(File file) throws IOException
  {
    System.out.println("file = " + file.getAbsolutePath());

    if (file.exists())
    {
      List<String> lines = readLinesInFile(file);

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
   *
   * @param  file  The file to read
   */
  static List<String> readLinesInFile(File file) throws IOException
  {
    List<String> lines     = new ArrayList<>();
    List<String> textLines = readLines(file);

    for (String textLine : textLines)
    {
      textLine = filterText(textLine);

      if (!textLine.startsWith("//"))
      {
        lines.add(textLine);
      }
    }

    return lines;
  }

  /** Remove any unwanted text from the line before processing further. */
  public static String filterText(String textLine)
  {
    String results = filterProjectDirs(textLine);

    results = filterTripleQuotes(results);
    results = filterEqualsDeclaration(results);

    return results;
  }

  // mongoExec = ${mongoCmd}.execute() into   ${mongoCmd}.execute()
  public static String filterEqualsDeclaration(String text)
  {
    if (!text.contains(".execute()"))
    {
      return text;
    }

    String results = filterTripleQuotes(text);

    results = substringAfterLast(results, "=").trim();

    return results;
  }

  public static String filterTripleQuotes(String results)
  {
    return remove(results, "\"\"\"");
  }

  public static String filterProjectDirs(String textLine)
  {
    String results = remove(textLine, "${project.projectDir}/");

    results = remove(results, "${projectDir}/");

    return results;
  }

  /**
   * Process the lines in the source file.
   *
   * @param  sourceFile  the file that was read in
   * @param  lines       the lines from that file
   */
  private void processLines(File sourceFile, List<String> lines) throws IOException
  {
    String baseName = getBaseName(sourceFile.getAbsolutePath());

    findTasksInLines(lines, baseName);
    findImportsInFile(lines);
    findPostDeclarationTaskModifications(lines);
  }

  /** find any tasks in the list of lines. Simple, right? */
  void findTasksInLines(List<String> lines, String sourceFile)
  {
    Task       taskInContext = null;
    List<Task> executeTasks  = new ArrayList<>();

    for (String line : lines)
    {
      String trimmedLine = line.trim();

      if (trimmedLine.startsWith("task "))
      {
        Task task = findOrCreateTaskByLine(taskMap, line, lines, sourceFile);

        taskInContext = task;
        executeTasks  = new ArrayList<>();
        taskMap.put(task.getName(), task);
      }

      if (trimmedLine.contains(".dependsOn"))
      {
        findOrCreateImplicitTasksByLine(taskMap, trimmedLine);
      }

      if (trimmedLine.contains(".execute()"))
      {
        findOrCreateImplicitTasksByExecute(taskMap, trimmedLine, taskInContext, executeTasks);
      }
    }
  }

  /** find any imports and process them, too. */
  void findImportsInFile(List<String> lines) throws IOException
  {
    for (String line : lines)
    {
      String text = line.trim();

      if (text.startsWith(APPLY_FROM))
      {
        text = substringAfter(text, APPLY_FROM);
        text = remove(text, SINGLE_QUOTE);
        text = remove(text, DOUBLE_QUOTE);

        if (text.startsWith(HTTP))
        {
          try
          {
            findUrlImport(text);
          }
          catch (IOException e)
          {  // e.printStackTrace();//todo something where we tell the user we can't go through the firewall or the file doesn't exist
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
            fileChecksums.put(newFile, checksumCRC32(newFile));
          }

          parseFile(newFile);
        }
      }
    }
  }

  /** find URL import and process that. Note we have to deal with proxy servers. */
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

    URL          url       = new URL(location);
    String       bigString = IOUtils.toString(url);
    String[]     tokens    = bigString.split("\n");
    List<String> lines     = new ArrayList<>();

    Collections.addAll(lines, tokens);
    processLines(url, lines);
  }

  /** Process the lines in the URL. */
  private void processLines(URL sourceUrl, List<String> lines) throws IOException
  {
    String fileName = sourceUrl.toString();

    findTasksInLines(lines, fileName);
    findImportsFromUrl(lines);
    findPostDeclarationTaskModifications(lines);
  }

  /** The lines imported from a URL may have imports themselves. Use the internet as your file system! */
  void findImportsFromUrl(List<String> lines) throws IOException
  {
    for (String line : lines)
    {
      String text = line.trim();

      if (text.startsWith(APPLY_FROM))
      {
        text = substringAfter(text, APPLY_FROM);
        text = remove(text, SINGLE_QUOTE);
        text = remove(text, DOUBLE_QUOTE);

        if (text.startsWith(HTTP))
        {
          findUrlImport(text);
        }
        else
        {  // todo we can't parse a file import in a remote URL - throw some sort of exception
        }
      }
    }
  }

  /** Find any modifications done after the task is declared. */
  public void findPostDeclarationTaskModifications(List<String> lines)
  {
    for (String line : lines)
    {
      if (!line.startsWith("//"))
      {
        if (line.contains(EACH))
        {
          List<Task> tasks = findOrCreateTaskInForEach(line, taskMap);

          if (!tasks.isEmpty())
          {
            String[] linesInScope = findLinesInScope(line, lines);

            for (String lineInScope : linesInScope)
            {
              if (lineInScope.contains(DEPENDS_ON))
              {
                for (Task task : tasks)
                {
                  task.findTaskDependsOn(taskMap, lineInScope, DEPENDS_ON);
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
  }

  /** parse the given file. */
  public void parseFile(String fileName)
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
        e.printStackTrace();  // todo log to user
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

  /** Generate the output. */
  public void handleFileGeneration() throws IOException
  {
    for (File file : filesToRender)
    {
      purgeAll();
      parseFile(file);
      System.out.println("selectedFile = " + file);

      if (showInternalGradleDependencies)
      {
        String[]   lines       = runGradleToGetTaskLines(file);
        List<Task> gradleTasks = parse(lines);

        // take the list of tasks generated from parsing Gradle and see if they exist in the establish set of tasks. If so, augment the dependency
        // information with what's give from the list.
        mergeGradleTasksIntoExistingTasks(taskMap, gradleTasks);
      }

      createTasks(file);
    }
  }

  private void createTasks(File file) throws IOException
  {
    List<Task>             tasks            = getTasks();
    ScriptDotFileGenerator dotFileGenerator = new ScriptDotFileGenerator();
    List<String>           lines            = dotFileGenerator.createOutput(tasks, preferences);
    File                   dotFile          = dotFileGenerator.writeOutput(lines, file.getAbsolutePath());
    GraphicFileCreator     fileCreator      = new GraphicFileCreator();

    fileCreator.processDotFile(dotFile, preferences, os);
  }

  public void beginOnFile(boolean watchFileForChanges, boolean showInternalGradleDependencies, File... gradleFiles) throws IOException
  {
    this.showInternalGradleDependencies = showInternalGradleDependencies;
    filesToRender.addAll(Arrays.asList(gradleFiles));

    // chooser.hide();
    if (gradleFiles.length > 0)
    {
      File selectedFile = gradleFiles[0];

      baseFile = selectedFile;
      preferences.setLastDir(selectedFile.getParent());
    }

    for (File selectedFile : gradleFiles)
    {  // put the file checksum into a map so we can check it later if need be...

      long checksum = checksumCRC32(selectedFile);

      fileChecksums.put(selectedFile, checksum);
      System.out.println("adding selectedFile = " + selectedFile + " to list with checksum = " + checksum);
    }

    handleFileGeneration();

    if (watchFileForChanges)
    {  // set a thread timer, pass it the maps, and have it call handleFileGeneration if any file in the map changes

      FileWatcher fileWatcher = new FileWatcher(fileChecksums, this);

      fileWatcher.execute();
    }
  }
}
