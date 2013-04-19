package com.nurflugel.util.gradlescriptvisualizer.ui;

import com.nurflugel.util.ScriptPreferences;

/** Preferences controlling class - saves user's output between sessions, reads it back in at startup. */
public class GradleScriptPreferences extends ScriptPreferences
{
  public static final int     DEFAULT_PROXY_PORT            = 8080;
  public static final String  SELECT_SECOND_TAB             = "select second tab";
  private static final String WATCH_FILES_FOR_CHANGES       = "watch files for changes";
  private static final String USE_HTTP_PROXY                = "useHttpProxy";
  private static final String PROXY_SERVER_NAME             = "proxyServerName";
  private static final String PROXY_SERVER_PORT             = "proxyServerPort";
  private static final String USE_PROXY_AUTHENTICATION      = "useProxyAuthentication";
  private static final String PROXY_USER_NAME               = "proxyUserName";
  public static final String  CONCENTRATE_DEPENDENCY_LINES  = "concentrateDependencyLines";
  public static final String  CONCENTRATE_SCRIPT_LINES      = "concentrateScriptLines";
  public static final String  SHOW_GRADLE_TASK_DEPENDENCIES = "showGradleTaskDependencies";
  private boolean             watchFilesForChanges;
  private boolean             useHttpProxy;
  private String              proxyServerName;
  private int                 proxyServerPort;
  private boolean             useProxyAuthentication;
  private String              proxyUserName;
  private String              proxyPassword;
  private boolean             selectSecondTab;
  private boolean             concentrateScriptLines;
  private boolean             concentrateDependencyLines;
  private boolean             shouldJustUseCompileConfig;
  public static final String  JUST_USE_COMPILE_CONFIG       = "justUseCompileConfig";
  private boolean             showGradleTaskDependencies;

  public GradleScriptPreferences(Class theClass)
  {
    super(theClass);
    get();

    // proxyPassword=preferencesStore.get(PROXY_PASSWORD,"");
  }

  public GradleScriptPreferences()
  {
    super(GradleScriptPreferences.class);
    get();

    // proxyPassword=preferencesStore.get(PROXY_PASSWORD,"");
  }

  /** Get the preferences from disk at app startup. */
  private void get()
  {
    showGradleTaskDependencies = preferencesStore.getBoolean(SHOW_GRADLE_TASK_DEPENDENCIES, false);
    watchFilesForChanges       = preferencesStore.getBoolean(WATCH_FILES_FOR_CHANGES, false);
    selectSecondTab            = preferencesStore.getBoolean(SELECT_SECOND_TAB, false);
    useHttpProxy               = preferencesStore.getBoolean(USE_HTTP_PROXY, false);
    concentrateDependencyLines = preferencesStore.getBoolean(CONCENTRATE_DEPENDENCY_LINES, false);
    concentrateScriptLines     = preferencesStore.getBoolean(CONCENTRATE_SCRIPT_LINES, false);
    proxyServerName            = preferencesStore.get(PROXY_SERVER_NAME, "");
    proxyServerPort            = preferencesStore.getInt(PROXY_SERVER_PORT, DEFAULT_PROXY_PORT);
    useProxyAuthentication     = preferencesStore.getBoolean(USE_PROXY_AUTHENTICATION, false);
    proxyUserName              = preferencesStore.get(PROXY_USER_NAME, "");
    shouldJustUseCompileConfig = preferencesStore.getBoolean(JUST_USE_COMPILE_CONFIG, false);
  }

  // -------------------------- OTHER METHODS --------------------------
  public void setProxyPassword(String proxyPassword)
  {
    this.proxyPassword = proxyPassword;
    save();
  }

  public void setProxyServerName(String proxyServerName)
  {
    this.proxyServerName = proxyServerName;
    save();
  }

  public void setProxyServerPort(int proxyServerPort)
  {
    this.proxyServerPort = proxyServerPort;
    save();
  }

  public void setProxyUserName(String proxyUserName)
  {
    this.proxyUserName = proxyUserName;
    save();
  }

  public void setSelectSecondTab(boolean selectSecondTab)
  {
    this.selectSecondTab = selectSecondTab;
    save();
  }

  public void setUseHttpProxy(boolean useHttpProxy)
  {
    this.useHttpProxy = useHttpProxy;
    save();
  }

  public void setUseProxyAuthentication(boolean useProxyAuthentication)
  {
    this.useProxyAuthentication = useProxyAuthentication;
    save();
  }

  public void setWatchFilesForChanges(boolean watchFilesForChanges)
  {
    this.watchFilesForChanges = watchFilesForChanges;
    save();
  }

  /** Save the preferences to disk. */
  @Override
  public void save()
  {
    super.save();
    preferencesStore.putBoolean(SHOW_GRADLE_TASK_DEPENDENCIES, showGradleTaskDependencies);
    preferencesStore.putBoolean(WATCH_FILES_FOR_CHANGES, watchFilesForChanges);
    preferencesStore.putBoolean(USE_HTTP_PROXY, useHttpProxy);
    preferencesStore.put(PROXY_SERVER_NAME, proxyServerName);
    preferencesStore.putInt(PROXY_SERVER_PORT, proxyServerPort);
    preferencesStore.putBoolean(USE_PROXY_AUTHENTICATION, useProxyAuthentication);
    preferencesStore.put(PROXY_USER_NAME, proxyUserName);
    preferencesStore.putBoolean(SELECT_SECOND_TAB, selectSecondTab);
    preferencesStore.putBoolean(CONCENTRATE_DEPENDENCY_LINES, concentrateDependencyLines);
    preferencesStore.putBoolean(CONCENTRATE_SCRIPT_LINES, concentrateScriptLines);
    preferencesStore.putBoolean(JUST_USE_COMPILE_CONFIG, shouldJustUseCompileConfig);
  }

  public boolean shouldUseHttpProxy()
  {
    return useHttpProxy;
  }

  public boolean shouldUseProxyAuthentication()
  {
    return useProxyAuthentication;
  }

  public boolean watchFilesForChanges()
  {
    return watchFilesForChanges;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getProxyPassword()
  {
    return proxyPassword;
  }

  public String getProxyServerName()
  {
    return proxyServerName;
  }

  public int getProxyServerPort()
  {
    return proxyServerPort;
  }

  public String getProxyUserName()
  {
    return proxyUserName;
  }

  public boolean getSelectSecondTab()
  {
    return selectSecondTab;
  }

  public boolean shouldConcentrateScriptLines()
  {
    return concentrateScriptLines;
  }

  public void setShouldConcentrateScriptLines(boolean concentrateScriptLines)
  {
    this.concentrateScriptLines = concentrateScriptLines;
  }

  public boolean shouldConcentrateDependencyLines()
  {
    return concentrateDependencyLines;
  }

  public void setShouldConcentrateDependencyLines(boolean concentrateDependencyLines)
  {
    this.concentrateDependencyLines = concentrateDependencyLines;
  }

  public void setShouldJustUseCompileConfig(boolean shouldJustUseCompileConfig)
  {
    this.shouldJustUseCompileConfig = shouldJustUseCompileConfig;
  }

  public boolean shouldJustUseCompileConfig()
  {
    return shouldJustUseCompileConfig;
  }

  public void setShowGradleTaskDependencies(boolean showGradleTaskDependencies)
  {
    this.showGradleTaskDependencies = showGradleTaskDependencies;
  }

  public boolean showGradleTaskDependencies()
  {
    return showGradleTaskDependencies;
  }
}
