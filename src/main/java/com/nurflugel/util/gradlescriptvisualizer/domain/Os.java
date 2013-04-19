package com.nurflugel.util.gradlescriptvisualizer.domain;

// import org.apache.commons.lang3.SystemUtils;
import static com.nurflugel.util.gradlescriptvisualizer.domain.OutputFormat.PDF;
import static com.nurflugel.util.gradlescriptvisualizer.domain.OutputFormat.PNG;

import org.apache.commons.lang3.SystemUtils;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import static java.io.File.separator;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

/** Enum of operating systems, and methods to deal with differences between them. */
@SuppressWarnings({ "EnumeratedClassNamingConvention", "EnumeratedConstantNamingConvention" })
public enum Os
{
  // todo remove command args for dot
  OS_X   ("Mac OS X", "build.sh", new String[] {}, "javax.swing.plaf.mac.MacLookAndFeel", PDF),
  WINDOWS("Windows", "build.cmd", new String[] { "cmd.exe", "/C" }, "com.sun.java.swing.plaf.windows.WindowsLookAndFeel", PNG);

  private String name;
  // private String       buildCommand;
  // private String[]     baseCommandArgs;
  // private String       lookAndFeel;
  // private OutputFormat outputFormat;

  // --------------------------- CONSTRUCTORS ---------------------------
  Os(String name, String buildCommand, String[] baseCommandArgs, String lookAndFeel, OutputFormat outputFormat)
  {
    this.name = name;
    // this.buildCommand    = buildCommand;
    // this.baseCommandArgs = baseCommandArgs;
    // this.lookAndFeel     = lookAndFeel;
    // this.outputFormat    = outputFormat;
  }

  // ------------------------ STATIC METHODS ------------------------
  public static Os findOs()
  {
    return IS_OS_WINDOWS ? WINDOWS
                         : OS_X;
  }

  /** Open the file in the OS's default application. */
  @SuppressWarnings("CallToRuntimeExec")
  public void openFile(String filePath) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException,
                                               ClassNotFoundException
  {
    if (this == WINDOWS)
    {
      List<String> commandList = new ArrayList<String>();

      commandList.add("cmd.exe");
      commandList.add("/c");
      commandList.add(filePath);

      String[] command = commandList.toArray(new String[commandList.size()]);

      // todo move to ProcessBuilder
      // logger.debug("Command to run: " + concatenate(command));
      Runtime runtime = Runtime.getRuntime();

      runtime.exec(command);
    }
    else
    {  // calling FileManager to open the URL works, if we replace spaces with %20

      String   outputFilePath = filePath.replace(" ", "%20");
      String   fileUrl        = "file://" + outputFilePath;
      Class<?> aClass         = Class.forName("com.apple.eio.FileManager");
      Method   method         = aClass.getMethod("openURL", String.class);

      method.invoke(null, fileUrl);
    }
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getName()
  {
    return name;
  }
}
