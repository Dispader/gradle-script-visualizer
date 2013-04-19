package com.nurflugel.util;

import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Os.OS_X;

import java.io.File;

@Deprecated
public class GraphicFileCreator
{
  // -------------------------- OTHER METHODS --------------------------

  // public static final Logger logger = LogFactory.getLogger(GraphicFileCreator.class);
  /** Convert the .dot file into png, pdf, svg, whatever. */
  @SuppressWarnings({ "OverlyLongMethod" })
  public void processDotFile(File dotFile, Os os)
  {
    try
    {                    // just open the file with the OS call
      os.openFile(dotFile.getAbsolutePath());
    }
    catch (Exception e)  // todo handle error
    {                    // logger.error(e);
    }
  }
}
