package com.nurflugel.util.dependencyvisualizer.ui;

import com.nurflugel.util.dependencyvisualizer.parser.GradleDependencyParser;
import com.nurflugel.util.gradlescriptvisualizer.domain.Os;
import com.nurflugel.util.gradlescriptvisualizer.ui.GradleScriptPreferences;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import static com.nurflugel.util.Util.*;
import static com.nurflugel.util.dependencyvisualizer.output.DependencyDotFileGenerator.createOutputForFile;
import static com.nurflugel.util.gradlescriptvisualizer.domain.Os.findOs;
import static javax.swing.JFileChooser.APPROVE_OPTION;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 6/3/12 Time: 14:32 To change this template use File | Settings | File Templates. */
@SuppressWarnings("MethodOnlyUsedFromInnerClass")
public class GradleDependencyMainFrame
{
  public static final String           TITLE_TEXT                      = "Gradle Dependency Visualizer v";
  private JButton                      selectGradleScriptButton;
  private JRadioButton                 generateJustDOTFilesRadioButton;
  private JRadioButton                 generatePNGPDFFilesRadioButton;
  private JPanel                       mainPanel;
  private JButton                      quitButton;
  private JCheckBox                    deleteDOTFilesOnCheckBox;
  private JCheckBox                    useHttpProxyCheckBox;
  private JPanel                       proxySettingsPanel;
  private JFormattedTextField          serverNameField;
  private JCheckBox                    useAuthenticationCheckBox;
  private JPanel                       authenticationPanel;
  private JTextField                   portNumberField;
  private JTextField                   userNameField;
  private JPasswordField               passwordField;
  private JTabbedPane tabbedPane1;
  private JPanel mainUiPanel;
  private JPanel resultsUiPanel;
  private JButton graphButton;
  private JButton quitButton1;
  private JRadioButton radioButton1;
  private JRadioButton radioButton2;
  private JFrame                       frame;
  private GradleScriptPreferences      preferences;
  private Os                           os;
  private final Set<File>              filesToRender                   = new HashSet<File>();
  private final GradleDependencyParser parser;

  public GradleDependencyMainFrame()
  {
    preferences = new GradleScriptPreferences();
    os          = findOs();
    frame       = new JFrame();
    frame.setContentPane(mainPanel);
    initializeUi();
    addActionListeners();
    parser = new GradleDependencyParser();
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

    chooser.setFileFilter(new FileNameExtensionFilter("Gradle scripts", "gradle"));
    chooser.setMultiSelectionEnabled(false);

    int returnVal = chooser.showOpenDialog(frame);

    if (returnVal == APPROVE_OPTION)
    {
      File selectedFile = chooser.getSelectedFile();chooser.hide();

      File outputForFile = createOutputForFile(selectedFile, parser, preferences, "dibble.dot");
      if (outputForFile != null)
      {
        try
        {
          os.openFile(outputForFile.getAbsolutePath());
        }
        catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException e)
        {
          e.printStackTrace();
        }
      }
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
    GradleDependencyMainFrame ui = new GradleDependencyMainFrame();
  }
}
