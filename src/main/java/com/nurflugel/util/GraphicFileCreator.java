package com.nurflugel.util;

// import com.nurflugel.util.LogFactory;
import org.apache.log4j.Logger;
import java.io.File;
import java.util.Date;
import static com.nurflugel.util.Os.OS_X;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 6/6/12 Time: 18:17 To change this template use File | Settings | File Templates. */
public class GraphicFileCreator
{
  // public static final Logger logger = LogFactory.getLogger(GraphicFileCreator.class);

  /** Convert the .dot file into png, pdf, svg, whatever. */
  @SuppressWarnings({ "OverlyLongMethod" })
  public void processDotFile(File dotFile, ScriptPreferences preferences, Os os)
  {
    try
    {
      if (preferences.generateJustDotFiles)
      {
        // just open the file with the OS call
        os.openFile(dotFile.getAbsolutePath());

        return;
      }

      // else, generate the PNG or PDF...
      String outputFileName = getOutputFileName(dotFile, preferences.getOutputFormat().getExtension());
      File   outputFile     = new File(dotFile.getParent(), outputFileName);
      File   parentFile     = outputFile.getParentFile();
      String dotFilePath    = dotFile.getAbsolutePath();
      String outputFilePath = outputFile.getAbsolutePath();

      if (outputFile.exists())
      {
        // if (logger.isDebugEnabled())
        // {
        // logger.debug("Deleting existing version of " + outputFilePath);
        // }
        outputFile.delete();  // delete the file before generating it if it exists
      }

      String outputFormatName  = preferences.getOutputFormat().getType();
      String dotExecutablePath = preferences.getDotExecutablePath();

      // this is to deal with different versions of Graphviz on OS X - if dot is in applications (old version), preface with an e for epdf.  If it's
      // in /usr/local/bin, leave as pdf
      if ((os == OS_X) && dotExecutablePath.startsWith("/Applications") && !outputFormatName.startsWith("e"))
      {
        outputFormatName = 'e' + outputFormatName;
      }

      String[] command = { dotExecutablePath, "-T" + outputFormatName, "-o" + outputFilePath, dotFilePath };

      // if (logger.isDebugEnabled())
      // {
      // logger.debug("Command to run: " + concatenate(command) + " parent file is " + parentFile.getPath());
      // }
      Runtime runtime = Runtime.getRuntime();
      long    start   = new Date().getTime();

      runtime.exec(command).waitFor();

      long end = new Date().getTime();

      // if (logger.isDebugEnabled())
      // {
      // logger.debug("Took " + (end - start) + " milliseconds to generate graphic");
      // }
      os.openFile(outputFilePath);
    }
    catch (Exception e)  // todo handle error
    {
      // logger.error(e);
    }
  }

  /** Takes something like build.dot and returns build.png. */
  private String getOutputFileName(File dotFile, String outputExtension)
  {
    String results = dotFile.getName();
    int    index   = results.indexOf(".dot");

    results = results.substring(0, index) + outputExtension;

    return results;
  }

  /** Join the array together as one string, with spaces between the elements. */
  private String concatenate(String[] commands)
  {
    StringBuilder stringBuffer = new StringBuilder();

    for (String command : commands)
    {
      stringBuffer.append(' ');
      stringBuffer.append(command);
    }

    return stringBuffer.toString().trim();
  }
}
