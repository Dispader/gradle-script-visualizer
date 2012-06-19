package com.nurflugel.util.gradlescriptvisualizer;

import com.nurflugel.util.OutputFormat;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptMainFrame;
import static com.nurflugel.util.OutputFormat.PDF;
import static com.nurflugel.util.OutputFormat.PNG;
import static java.util.prefs.Preferences.userNodeForPackage;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Nov 24, 2009 Time: 5:04:47 PM To change this template use File | Settings | File Templates.
 */
public class Preferences
{
  private static final String         CONCENTRATE_LINES          = "concentrateLines";
  private static final String         DELETE_DOT_FILES_ON_EXIT   = "deleteDotFilesOnExit";
  private static final String         DOT_EXECUTABLE             = "dotExecutablePath";
  private static final String         GROUP_NODES_BY_BUILDFILE   = "groupNodesByBuildfile";
  private static final String         INCLUDE_IMPORTED_ANT_FILES = "includeImportedAntFiles";
  private static final String         OUTPUT_FORMAT              = "outputFormat";
  private static final String         SHOW_ABSOLUTE_FILE_PATHS   = "show_absolute_file_paths";
  private static final String         LAST_DIR                   = "LAST_DIR";
  private static final String         PREVIOUS_VERSION           = "version";
  private static final String         SHOW_LEGEND                = "showLegend";
  private java.util.prefs.Preferences preferencesStore;
  private boolean                     shouldGroupByBuildfiles    = true;
  private boolean                     shouldIncludeImportedFiles = true;
  private boolean                     shouldConcentrate          = true;
  private boolean                     shouldDeleteDotFilesOnExit = true;
  private OutputFormat                outputFormat               = PDF;
  private boolean                     shouldUseAbsolutePaths     = false;
  private String                      lastDir;
  private String                      dotExecutablePath;
  private String                      previousVersion;
  private boolean                     shouldShowLegend           = true;

  public Preferences()
  {
    preferencesStore           = userNodeForPackage(GradleScriptMainFrame.class);
    dotExecutablePath          = preferencesStore.get(DOT_EXECUTABLE, "");
    shouldGroupByBuildfiles    = preferencesStore.getBoolean(GROUP_NODES_BY_BUILDFILE, true);
    shouldIncludeImportedFiles = preferencesStore.getBoolean(INCLUDE_IMPORTED_ANT_FILES, true);
    shouldConcentrate          = preferencesStore.getBoolean(CONCENTRATE_LINES, true);
    shouldDeleteDotFilesOnExit = preferencesStore.getBoolean(DELETE_DOT_FILES_ON_EXIT, true);
    shouldDeleteDotFilesOnExit = preferencesStore.getBoolean(SHOW_LEGEND, true);
    shouldUseAbsolutePaths     = preferencesStore.getBoolean(SHOW_ABSOLUTE_FILE_PATHS, false);
    lastDir                    = preferencesStore.get(LAST_DIR, "");
    previousVersion            = preferencesStore.get(PREVIOUS_VERSION, "");
    outputFormat               = OutputFormat.valueOf(preferencesStore.get(OUTPUT_FORMAT, PNG.getDisplayLabel()));
  }

  public Preferences(boolean dummyFlag)
  {
    preferencesStore  = userNodeForPackage(GradleScriptMainFrame.class);
    dotExecutablePath = preferencesStore.get(DOT_EXECUTABLE, "");
  }

  // -------------------------- OTHER METHODS --------------------------
  public void setDotExecutablePath(String dotExecutablePath)
  {
    this.dotExecutablePath = dotExecutablePath;
    save();
  }

  public void setLastDir(String lastDir)
  {
    this.lastDir = lastDir;
    save();
  }

  public void save()
  {
    preferencesStore.putBoolean(GROUP_NODES_BY_BUILDFILE, shouldGroupByBuildfiles);
    preferencesStore.putBoolean(INCLUDE_IMPORTED_ANT_FILES, shouldIncludeImportedFiles);
    preferencesStore.putBoolean(CONCENTRATE_LINES, shouldConcentrate);
    preferencesStore.putBoolean(DELETE_DOT_FILES_ON_EXIT, shouldDeleteDotFilesOnExit);
    preferencesStore.put(OUTPUT_FORMAT, outputFormat.getDisplayLabel());
    preferencesStore.putBoolean(SHOW_ABSOLUTE_FILE_PATHS, shouldUseAbsolutePaths);
    preferencesStore.put(DOT_EXECUTABLE, dotExecutablePath);
    preferencesStore.put(LAST_DIR, lastDir);
    preferencesStore.putBoolean(SHOW_LEGEND, shouldShowLegend);
    preferencesStore.put(PREVIOUS_VERSION, previousVersion);
  }

  public void setOutputFormat(OutputFormat outputFormat)
  {
    this.outputFormat = outputFormat;
    save();
  }

  public void setPreferencesStore(java.util.prefs.Preferences preferencesStore)
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

  public void setShouldGroupByBuildfiles(boolean shouldGroupByBuildfiles)
  {
    this.shouldGroupByBuildfiles = shouldGroupByBuildfiles;
    save();
  }

  public void setShouldIncludeImportedFiles(boolean shouldIncludeImportedFiles)
  {
    this.shouldIncludeImportedFiles = shouldIncludeImportedFiles;
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
    return shouldGroupByBuildfiles;
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
}
