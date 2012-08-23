package com.nurflugel.util.gradlescriptvisualizer.output;

import com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptMainFrame;
import org.apache.commons.io.FileUtils;
import javax.swing.*;
import java.io.File;
import java.util.Map;

/** Watches the files in the map every second, and puts the current checkums into the map. */
public class FileWatcher extends SwingWorker<Object, Object>
{
  private final Map<File, Long>       fileChecksums;
  private final GradleScriptMainFrame gradleScriptMainFrame;
  private GradleFileParser            parser;

  public FileWatcher(Map<File, Long> fileChecksums, GradleScriptMainFrame gradleScriptMainFrame, GradleFileParser parser)
  {
    this.fileChecksums         = fileChecksums;
    this.gradleScriptMainFrame = gradleScriptMainFrame;
    this.parser                = parser;
  }

  @Override
  protected Object doInBackground() throws Exception
  {
    // noinspection InfiniteLoopStatement
    while (true)
    {
      // wake up every second to check the files
      Thread.sleep(1000);

      for (Map.Entry<File, Long> fileLongEntry : fileChecksums.entrySet())
      {
        File file            = fileLongEntry.getKey();
        long currentChecksum = FileUtils.checksumCRC32(file);
        Long oldChecksum     = fileLongEntry.getValue();

        if (oldChecksum == currentChecksum)
        {
          // System.out.println("File " + file + " didn't change");
        }
        else
        {
          System.out.println("File " + file + " changed!");

          // update the checksum
          fileChecksums.put(file, currentChecksum);
          parser.purgeAll();
          gradleScriptMainFrame.handleFileGeneration(parser);
        }
      }
    }
  }
}
