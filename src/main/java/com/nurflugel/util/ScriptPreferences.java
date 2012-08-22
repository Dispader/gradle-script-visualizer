package com.nurflugel.util;

import com.nurflugel.util.gradlescriptvisualizer.domain.OutputFormat;

import java.util.prefs.Preferences;

import static com.nurflugel.util.gradlescriptvisualizer.domain.OutputFormat.PDF;
import static com.nurflugel.util.gradlescriptvisualizer.domain.OutputFormat.PNG;
import static java.util.prefs.Preferences.userNodeForPackage;

/** Base class for my Ant and Gradle script parsers. Need to move this to shared code library. */
public abstract class ScriptPreferences
{
  private static final String   CONCENTRATE_LINES          = "concentrateLines";
  private static final String   DELETE_DOT_FILES_ON_EXIT   = "deleteDotFilesOnExit";
  private static final String   DOT_EXECUTABLE             = "dotExecutablePath";
  private static final String   GROUP_NODES_BY_BUILDFILE   = "groupNodesByBuildfile";
  private static final String   INCLUDE_IMPORTED_ANT_FILES = "includeImportedAntFiles";
  private static final String   OUTPUT_FORMAT              = "outputFormat";
  private static final String   SHOW_ABSOLUTE_FILE_PATHS   = "show_absolute_file_paths";
  private static final String   LAST_DIR                   = "LAST_DIR";
  private static final String   PREVIOUS_VERSION           = "version";
  private static final String   SHOW_LEGEND                = "showLegend";
  protected static final String GENERATE_JUST_DOT_FILES    = "generate just dot files";
  protected Preferences         preferencesStore;
  private boolean               shouldGroupByBuildFiles    = true;
  private boolean               shouldIncludeImportedFiles = true;
  private boolean               shouldConcentrate          = true;
  private boolean               shouldDeleteDotFilesOnExit = true;
  private OutputFormat outputFormat               = PDF;
  private String                lastDir;
  private String                dotExecutablePath;
  private String                previousVersion;
  private boolean               shouldUseAbsolutePaths     = false;
  private boolean               shouldShowLegend           = true;
  protected boolean             generateJustDotFiles;

  protected ScriptPreferences(Class theClass)
  {
    preferencesStore           = userNodeForPackage(theClass);
    dotExecutablePath          = preferencesStore.get(DOT_EXECUTABLE, "");
    shouldGroupByBuildFiles    = preferencesStore.getBoolean(GROUP_NODES_BY_BUILDFILE, true);
    shouldIncludeImportedFiles = preferencesStore.getBoolean(INCLUDE_IMPORTED_ANT_FILES, true);
    shouldConcentrate          = preferencesStore.getBoolean(CONCENTRATE_LINES, true);
    lastDir                    = preferencesStore.get(LAST_DIR, "");
    previousVersion            = preferencesStore.get(PREVIOUS_VERSION, "");

    String defaultOutputFormat = (Os.findOs() == Os.OS_X) ? PDF.getDisplayLabel()
                                                          : PNG.getDisplayLabel();

    outputFormat = OutputFormat.valueOf(preferencesStore.get(OUTPUT_FORMAT, defaultOutputFormat));

    // outputFormat=OutputFormat.PDF;
    // outputFormat=OutputFormat.PNG;
    shouldUseAbsolutePaths     = preferencesStore.getBoolean(SHOW_ABSOLUTE_FILE_PATHS, false);
    shouldDeleteDotFilesOnExit = preferencesStore.getBoolean(DELETE_DOT_FILES_ON_EXIT, true);
    shouldShowLegend           = preferencesStore.getBoolean(SHOW_LEGEND, true);
    generateJustDotFiles       = preferencesStore.getBoolean(GENERATE_JUST_DOT_FILES, false);
  }

  // -------------------------- OTHER METHODS --------------------------
  public void setDotExecutablePath(String dotExecutablePath)
  {
    this.dotExecutablePath = dotExecutablePath;
    save();
  }

  public void save()
  {
    preferencesStore.putBoolean(GROUP_NODES_BY_BUILDFILE, shouldGroupByBuildFiles);
    preferencesStore.putBoolean(INCLUDE_IMPORTED_ANT_FILES, shouldIncludeImportedFiles);
    preferencesStore.putBoolean(CONCENTRATE_LINES, shouldConcentrate);
    preferencesStore.putBoolean(DELETE_DOT_FILES_ON_EXIT, shouldDeleteDotFilesOnExit);
    preferencesStore.put(OUTPUT_FORMAT, outputFormat.getDisplayLabel());
    preferencesStore.put(DOT_EXECUTABLE, dotExecutablePath);
    preferencesStore.put(LAST_DIR, lastDir);
    preferencesStore.putBoolean(SHOW_LEGEND, shouldShowLegend);
    preferencesStore.put(PREVIOUS_VERSION, previousVersion);
    preferencesStore.putBoolean(SHOW_ABSOLUTE_FILE_PATHS, shouldUseAbsolutePaths);
    preferencesStore.putBoolean(GENERATE_JUST_DOT_FILES, generateJustDotFiles());
  }

  // todo this might be a good use of Spring aop - for any set* method, call save
  public void setLastDir(String lastDir)
  {
    this.lastDir = lastDir;
    save();
  }

  public void setOutputFormat(OutputFormat outputFormat)
  {
    this.outputFormat = outputFormat;
    save();
  }

  public void setPreferencesStore(Preferences preferencesStore)
  {
    this.preferencesStore = preferencesStore;
    save();
  }

  public void setPreviousVersion(String previousVersion)
  {
    this.previousVersion = previousVersion;
    save();
  }

  public void setShouldConcentrate(boolean shouldConcentrate)
  {
    this.shouldConcentrate = shouldConcentrate;
    save();
  }

  public void setShouldDeleteDotFilesOnExit(boolean shouldDeleteDotFilesOnExit)
  {
    this.shouldDeleteDotFilesOnExit = shouldDeleteDotFilesOnExit;
    save();
  }

  public void setShouldGroupByBuildFiles(boolean shouldGroupByBuildFiles)
  {
    this.shouldGroupByBuildFiles = shouldGroupByBuildFiles;
    save();
  }

  public void setShouldIncludeImportedFiles(boolean shouldIncludeImportedFiles)
  {
    this.shouldIncludeImportedFiles = shouldIncludeImportedFiles;
    save();
  }

  public void setShouldShowLegend(boolean shouldShowLegend)
  {
    this.shouldShowLegend = shouldShowLegend;
    save();
  }

  public void setShouldUseAbsolutePaths(boolean shouldUseAbsolutePaths)
  {
    this.shouldUseAbsolutePaths = shouldUseAbsolutePaths;
    save();
  }

  public boolean shouldConcentrate()
  {
    return shouldConcentrate;
  }

  public boolean shouldDeleteDotFilesOnExit()
  {
    return shouldDeleteDotFilesOnExit;
  }

  public boolean shouldGroupByBuildfiles()
  {
    return shouldGroupByBuildFiles;
  }

  public boolean shouldIncludeImportedFiles()
  {
    return shouldIncludeImportedFiles;
  }

  public boolean shouldShowLegend()
  {
    return shouldShowLegend;
  }

  public boolean shouldUseAbsolutePaths()
  {
    return shouldUseAbsolutePaths;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getDotExecutablePath()
  {
    return dotExecutablePath;
  }

  public String getLastDir()
  {
    return lastDir;
  }

  public OutputFormat getOutputFormat()
  {
    return outputFormat;
  }

  public String getPreviousVersion()
  {
    return previousVersion;
  }

  // -------------------------- OTHER METHODS --------------------------
  public void setGenerateJustDotFiles(boolean generateJustDotFiles)
  {
    this.generateJustDotFiles = generateJustDotFiles;
    save();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public boolean generateJustDotFiles()
  {
    return generateJustDotFiles;
  }
}
