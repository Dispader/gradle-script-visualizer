package com.nurflugel.util.gradlescriptvisualizer.ui;

import com.nurflugel.util.ScriptPreferences;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 6/6/12 Time: 18:41 To change this template use File | Settings | File Templates. */
public class GradleScriptPreferences extends ScriptPreferences
{
  private static final String WATCH_FILES_FOR_CHANGES  = "watch files for changes";
  private static final String USE_HTTP_PROXY           = "useHttpProxy";
  private static final String PROXY_SERVER_NAME        = "proxyServerName";
  private static final String PROXY_SERVER_PORT        = "proxyServerPort";
  private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication";
  private static final String PROXY_USER_NAME          = "proxyUserName";
  private static final String PROXY_PASSWORD           = "proxyPassword";
  private boolean             watchFilesForChanges;
  private boolean             useHttpProxy;
  private String              proxyServerName;
  private int                 proxyServerPort;
  private boolean             useProxyAuthentication;
  private String              proxyUserName;
  private String              proxyPassword;

  public GradleScriptPreferences()
  {
    super(GradleScriptMainFrame.class);
    get();

    // proxyPassword=preferencesStore.get(PROXY_PASSWORD,"");
  }

  private void get()
  {
    watchFilesForChanges   = preferencesStore.getBoolean(WATCH_FILES_FOR_CHANGES, false);
    useHttpProxy           = preferencesStore.getBoolean(USE_HTTP_PROXY, false);
    proxyServerName        = preferencesStore.get(PROXY_SERVER_NAME, "");
    proxyServerPort        = preferencesStore.getInt(PROXY_SERVER_PORT, 8080);
    useProxyAuthentication = preferencesStore.getBoolean(USE_PROXY_AUTHENTICATION, false);
    proxyUserName          = preferencesStore.get(PROXY_USER_NAME, "");
  }

  GradleScriptPreferences(Class theTestClass){
    super(theTestClass);
    get();
  }

  @Override
  public void save()
  {
    super.save();
    preferencesStore.putBoolean(WATCH_FILES_FOR_CHANGES, watchFilesForChanges);
    preferencesStore.putBoolean(USE_HTTP_PROXY, useHttpProxy);
    preferencesStore.put(PROXY_SERVER_NAME, proxyServerName);
    preferencesStore.putInt(PROXY_SERVER_PORT, proxyServerPort);
    preferencesStore.putBoolean(USE_PROXY_AUTHENTICATION, useProxyAuthentication);
    preferencesStore.put(PROXY_USER_NAME, proxyUserName);

    // preferencesStore.put(PROXY_USER_NAME,proxyUserName);
  }

  public void setWatchFilesForChanges(boolean watchFilesForChanges)
  {
    this.watchFilesForChanges = watchFilesForChanges;
    save();
  }

  public String getProxyPassword()
  {
    return proxyPassword;
  }

  public void setProxyPassword(String proxyPassword)
  {
    this.proxyPassword = proxyPassword;
    save();
  }

  public String getProxyServerName()
  {
    return proxyServerName;
  }

  public void setProxyServerName(String proxyServerName)
  {
    this.proxyServerName = proxyServerName;
    save();
  }

  public int getProxyServerPort()
  {
    return proxyServerPort;
  }

  public void setProxyServerPort(int proxyServerPort)
  {
    this.proxyServerPort = proxyServerPort;
    save();
  }

  public String getProxyUserName()
  {
    return proxyUserName;
  }

  public void setProxyUserName(String proxyUserName)
  {
    this.proxyUserName = proxyUserName;
    save();
  }

  public boolean shouldUseProxyAuthentication()
  {
    return useProxyAuthentication;
  }

  public void setUseProxyAuthentication(boolean useProxyAuthentication)
  {
    this.useProxyAuthentication = useProxyAuthentication;
    save();
  }

  public boolean watchFilesForChanges()
  {
    return watchFilesForChanges;
  }

  public void setUseHttpProxy(boolean useHttpProxy)
  {
    this.useHttpProxy = useHttpProxy;
    save();
  }

  public boolean shouldUseHttpProxy()
  {
    return useHttpProxy;
  }
}
