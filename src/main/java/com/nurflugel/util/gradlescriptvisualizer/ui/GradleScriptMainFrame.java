package com.nurflugel.util.gradlescriptvisualizer.ui;

import com.nurflugel.util.GraphicFileCreator;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.domain.Task;
import com.nurflugel.util.gradlescriptvisualizer.output.DotFileGenerator;
import com.nurflugel.util.gradlescriptvisualizer.output.FileWatcher;
import com.nurflugel.util.gradlescriptvisualizer.parser.GradleFileParser;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static com.nurflugel.util.Util.*;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Os.findOs;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static org.apache.commons.io.FileUtils.checksumCRC32;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 6/3/12 Time: 14:32 To change this template use File | Settings | File Templates. */
public class GradleScriptMainFrame
{
  public static final String      TITLE_TEXT                      = "Gradle Script Visualizer v";
  private JButton                 selectGradleScriptButton;
  private JCheckBox               watchFileForChangesCheckBox;
  private JRadioButton            generateJustDOTFilesRadioButton;
  private JRadioButton            generatePNGPDFFilesRadioButton;
  private JPanel                  mainPanel;
  private JButton                 quitButton;
  private JCheckBox               deleteDOTFilesOnCheckBox;
  private JCheckBox               groupByBuildFileCheckBox;
  private JCheckBox               useHttpProxyCheckBox;
  private JPanel                  proxySettingsPanel;
  private JFormattedTextField     serverNameField;
  private JCheckBox               useAuthenticationCheckBox;
  private JPanel                  authenticationPanel;
  private JTextField              portNumberField;
  private JTextField              userNameField;
  private JPasswordField          passwordField;
  private JFrame                  frame;
  private GradleScriptPreferences preferences;
  private String                  dotExecutablePath;
  private Os                      os;
  private final Map<File, Long>   fileChecksums                   = new HashMap<File, Long>();
  private final Set<File>         filesToRender                   = new HashSet<File>();
  private final GradleFileParser  parser;

  public GradleScriptMainFrame()
  {
    preferences = new GradleScriptPreferences();
    os          = findOs();
    frame       = new JFrame();
    frame.setContentPane(mainPanel);
    initializeUi();
    addActionListeners();
    dotExecutablePath = preferences.getDotExecutablePath();  // todo this is ugly, fix it somehow
    preferences.setDotExecutablePath(dotExecutablePath);
    parser = new GradleFileParser(preferences, os);
  }

  /** I like to put all the listeners in one method. */
  private void addActionListeners()
  {
    serverNameField.addFocusListener(new FocusAdapter()
      {
        @Override
        public void focusLost(FocusEvent e)
        {
          preferences.setProxyServerName(serverNameField.getText());
        }
      });
    userNameField.addFocusListener(new FocusAdapter()
      {
        @Override
        public void focusLost(FocusEvent e)
        {
          preferences.setProxyUserName(userNameField.getText());
        }
      });
    passwordField.addFocusListener(new FocusAdapter()
      {
        @Override
        public void focusLost(FocusEvent e)
        {
          preferences.setProxyPassword(new String(passwordField.getPassword()));
        }
      });
    portNumberField.addFocusListener(new FocusAdapter()
      {
        @Override
        public void focusLost(FocusEvent e)
        {
          preferences.setProxyServerPort(Integer.parseInt(portNumberField.getText()));
        }
      });
    useAuthenticationCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          boolean selected = useAuthenticationCheckBox.isSelected();

          setComponentVisibilityFromSettings();
          preferences.setUseProxyAuthentication(selected);
        }
      });
    useHttpProxyCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          boolean selected = useHttpProxyCheckBox.isSelected();

          setComponentVisibilityFromSettings();
          preferences.setUseHttpProxy(selected);
        }
      });
    groupByBuildFileCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          preferences.setShouldGroupByBuildFiles(groupByBuildFileCheckBox.isSelected());

          try
          {
            handleFileGeneration(parser);
          }
          catch (IOException e)
          {
            e.printStackTrace();  // todo do something...
          }
        }
      });
    deleteDOTFilesOnCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          preferences.setShouldDeleteDotFilesOnExit(deleteDOTFilesOnCheckBox.isSelected());
        }
      });
    generateJustDOTFilesRadioButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          preferences.setGenerateJustDotFiles(generateJustDOTFilesRadioButton.isSelected());
        }
      });
    generatePNGPDFFilesRadioButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          preferences.setGenerateJustDotFiles(generateJustDOTFilesRadioButton.isSelected());
        }
      });
    watchFileForChangesCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          preferences.setWatchFilesForChanges(watchFileForChangesCheckBox.isSelected());
        }
      });
    selectGradleScriptButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          try
          {
            selectGradleScript();
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      });
    quitButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          doQuitAction();
        }
      });
  }

  /** Do any initialization that the IDE doesn't do in it's layout. Look and feel, setting UI attributes from stored preferencer, etc. */
  private void initializeUi()
  {
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", frame);
    frame.pack();
    center(frame);
    deleteDOTFilesOnCheckBox.setSelected(preferences.shouldDeleteDotFilesOnExit());
    generateJustDOTFilesRadioButton.setSelected(preferences.generateJustDotFiles());
    watchFileForChangesCheckBox.setSelected(preferences.watchFilesForChanges());
    groupByBuildFileCheckBox.setSelected(preferences.shouldGroupByBuildfiles());

    if (preferences.shouldUseHttpProxy())
    {
      serverNameField.setText(preferences.getProxyServerName());
      portNumberField.setText(preferences.getProxyServerPort() + "");

      if (preferences.shouldUseProxyAuthentication())
      {
        userNameField.setText(preferences.getProxyUserName());
      }
    }

    useHttpProxyCheckBox.setSelected(preferences.shouldUseHttpProxy());
    useAuthenticationCheckBox.setSelected(preferences.shouldUseProxyAuthentication());
    setComponentVisibilityFromSettings();
    frame.setTitle(TITLE_TEXT + VERSION);
    frame.setVisible(true);
  }

  /** Called at startup and when the user clicks on one of the checkboxes. */
  private void setComponentVisibilityFromSettings()
  {
    proxySettingsPanel.setVisible(useHttpProxyCheckBox.isSelected());
    authenticationPanel.setVisible(useAuthenticationCheckBox.isSelected());
    frame.pack();
  }

  /** Open up a file chooser and select the file(s) to process. */
  private void selectGradleScript() throws IOException
  {
    JFileChooser chooser = new JFileChooser();
    String       lastDir = preferences.getLastDir();

    if (lastDir != null)
    {
      chooser.setCurrentDirectory(new File(lastDir));
    }

    chooser.setFileFilter(new FileNameExtensionFilter("Gradle scripts", "gradle", "groovy"));
    chooser.setMultiSelectionEnabled(true);

    int returnVal = chooser.showOpenDialog(frame);

    parser.purgeAll();

    if (returnVal == APPROVE_OPTION)
    {
      File[] selectedFiles = chooser.getSelectedFiles();

      filesToRender.addAll(Arrays.asList(selectedFiles));

      // chooser.hide();
      if (selectedFiles.length > 0)
      {
        File selectedFile = selectedFiles[0];

        parser.setBaseFile(selectedFile);
        preferences.setLastDir(selectedFile.getParent());
      }

      for (File selectedFile : selectedFiles)
      {
        // put the file checksum into a map so we can check it later if need be...
        long checksum = checksumCRC32(selectedFile);

        fileChecksums.put(selectedFile, checksum);
        System.out.println("adding selectedFile = " + selectedFile + " to list with checksum = " + checksum);
      }

      handleFileGeneration(parser);
    }

    if (watchFileForChangesCheckBox.isSelected())
    {
      // set a thread timer, pass it the maps, and have it call handleFileGeneration if any file in the map changes
      FileWatcher fileWatcher = new FileWatcher(fileChecksums, parser);

      fileWatcher.execute();
    }
  }

  /** Generate the output. */
  public void handleFileGeneration(GradleFileParser parser) throws IOException
  {
    for (File file : filesToRender)
    {
      parser.purgeAll();
      parser.parseFile(file);
      System.out.println("selectedFile = " + file);

      List<Task>         tasks            = parser.getTasks();
      DotFileGenerator   dotFileGenerator = new DotFileGenerator();
      List<String>       lines            = dotFileGenerator.createOutput(tasks, preferences);
      File               dotFile          = dotFileGenerator.writeOutput(lines, file.getAbsolutePath());
      GraphicFileCreator fileCreator      = new GraphicFileCreator();

      fileCreator.processDotFile(dotFile, preferences, os);
    }
  }

  private void doQuitAction()
  {
    getOutputPreferencesFromUi();
    preferences.save();
    System.exit(0);
  }

  private void getOutputPreferencesFromUi() {}

  // --------------------------- main() method ---------------------------
  public static void main(String[] args)
  {
    GradleScriptMainFrame ui = new GradleScriptMainFrame();
  }
}
