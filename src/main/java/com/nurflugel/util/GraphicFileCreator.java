package com.nurflugel.util;

// import com.nurflugel.util.LogFactory;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import java.io.File;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Os.OS_X;

/** TODO - I'm thinking of just removing all this - calling DOT directly seems to work, and the user can output graphics if they feel like it. */
@Deprecated
public class GraphicFileCreator
{
  // -------------------------- OTHER METHODS --------------------------

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
      Runtime  runtime = Runtime.getRuntime();

      runtime.exec(command).waitFor();
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
}
