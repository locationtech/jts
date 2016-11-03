

/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testrunner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.locationtech.jts.*;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.command.CommandLine;
import org.locationtech.jtstest.command.Option;
import org.locationtech.jtstest.command.OptionSpec;
import org.locationtech.jtstest.command.ParseException;
import org.locationtech.jtstest.function.*;
import org.locationtech.jtstest.geomop.*;
import org.locationtech.jtstest.util.StringUtil;


/**
 * Executes tests specified in XML files. Displays errors encountered.
 * <p>
 * <b>Command Line Options</b>
 * 
 * <table border='1'>
 * <tr>
 * <td><tt>-files { <i>&lt;fileOrDirectoryName&gt;</i></tt> }  </td>
 * <td>req</td>
 * <td>Specifies the XML test files to run</td>
 * </tr>
 * <tr>
 * <td><tt>-geomop <i>&lt;classname&gt;</i></tt> </td>
 * <td>opt</td>
 * <td>Specifies a custom {@link GeometryOperation} to be used</td>
 * </tr>
 * <tr>
 * <tr>
 * <td><tt>-testCaseIndex <i>&lt;num&gt;</i></tt> </td>
 * <td>opt</td>
 * <td>Specifies the index of a single test to run</td>
 * </tr>
 * <tr>
 * <td><tt>-verbose</tt> </td>
 * <td>opt</td>
 * <td>Provides verbose output</td>
 * </tr>
 * </table>
 *
 * @version 1.7
 */
public class TopologyTestApp extends JFrame {

  private static final String OPT_GEOMFUNC = "geomfunc";
  private static final String OPT_GEOMOP = "geomop";
  private static final String OPT_TESTCASEINDEX = "testCaseIndex";
  private static final String OPT_VERBOSE = "verbose";

  private static CommandLine commandLine;

  private static GeometryFunctionRegistry funcRegistry 
  	= new GeometryFunctionRegistry(TestCaseGeometryFunctions.class);
  private static GeometryOperation defaultOp = new GeometryFunctionOperation(funcRegistry);
  private static GeometryOperation geometryOp = defaultOp;

  public static GeometryOperation getGeometryOperation()
  {
    return geometryOp;
  }

  /**
   * Tests whether a GeometryOperation was specified on the command line
   * @return true if a geometry operation was specified
   */
  public static boolean isGeometryOperationSpecified()
  {
  	return geometryOp != defaultOp;
  }
  
  private static ResultMatcher defaultResultMatcher = new EqualityResultMatcher();
  private static ResultMatcher resultMatcher = defaultResultMatcher;

  public static ResultMatcher getResultMatcher()
  {
    return resultMatcher;
  }
  /**
   * Tests whether a {@link ResultMatcher} was specified on the command line
   * @return true if a matcher was specified
   */
  public static boolean isResultMatcherSpecified()
  {
  	return resultMatcher != defaultResultMatcher;
  }

  private TestEngine engine = new TestEngine();

  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JPanel jPanel5 = new JPanel();
  GridBagLayout gridBagLayout4 = new GridBagLayout();
  JScrollPane jScrollPane2 = new JScrollPane();
  JTextArea logTextArea = new JTextArea();
  JPanel jPanel3 = new JPanel();
  JLabel statusLabel = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();
  JList fileList;
  JScrollPane jScrollPane1 = new JScrollPane();
  JButton runSelectedButton = new JButton();
  JButton addFileButton = new JButton();
  JPanel jPanel2 = new JPanel();
  FlowLayout flowLayout2 = new FlowLayout();
  JButton runAllButton = new JButton();
  JButton removeFileButton = new JButton();
  private DefaultListModel fileListModel = new DefaultListModel();
  private JFileChooser chooser = new JFileChooser();
  private Timer timer = new Timer(2000,
    new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        updateEnabled();
        if (!engine.isRunning()) {
          timer.stop();
          logTextArea.setText(report() + "\n\n" + summary());
          statusLabel.setText(oneLineSummary());
        }
      }

    });

  public TopologyTestApp(java.util.List testFiles) {
    this();
    try {
      add(testFiles);
    }
    catch (Exception e) {
      reportException(e);
    }
  }

  /**
   *  Creates a TopologyTestApp. Do not use this constructor; instead, use
   *  #TopologyTestApp(Properties). This constructor is for use by JBuilder's
   *  Designers.
   */
  public TopologyTestApp() {
    try {
      jbInit();
      chooser.setDialogTitle("Select topology test files");
      chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setMultiSelectionEnabled(true);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   *  Opens a TopologyTestApp.
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      parseCommandLine(args);
      
      System.out.println("=====  Test Runner  -  JTS Topology Suite (Version " 
      		+ JTSVersion.CURRENT_VERSION
      		+ ")  =====");
      
      TopologyTestApp topologyTestApp = new TopologyTestApp(testFiles());
      if (args.length == 0) {
        displayHelp();
        System.exit(0);
      }
      
      if (commandLine.hasOption(OPT_GEOMOP)) {
        String geomOpClassname = commandLine.getOption(OPT_GEOMOP).getArg(0);
        geometryOp = GeometryOperationLoader.createGeometryOperation(TopologyTestApp.class.getClassLoader(), geomOpClassname);
        // loading must have failed - abort
        if (geometryOp == null) {
          System.exit(0);
        }
        System.out.println("Using Geometry Operation: " + geomOpClassname);
      }
      
      if (commandLine.hasOption(OPT_GEOMFUNC)) {
        String geomFuncClassname = commandLine.getOption(OPT_GEOMFUNC).getArg(0);
        System.out.println("Adding Geometry Functions from: " + geomFuncClassname);
        funcRegistry.add(geomFuncClassname);
      }
      
      if (commandLine.hasOption(OPT_TESTCASEINDEX)) 
      {
      	int testCaseIndexToRun = commandLine.getOption(OPT_TESTCASEINDEX).getArgAsInt(0);
        topologyTestApp.engine.setTestCaseIndexToRun(testCaseIndexToRun);  
        System.out.println("Running test case # " + testCaseIndexToRun);
      }
      if (!commandLine.hasOption("GUI")) {
        topologyTestApp.engine.setTestFiles(topologyTestApp.getAllTestFiles());
        topologyTestApp.engine.run();
        System.out.println(
            topologyTestApp.report());
        System.exit(0);
      }
      else {
        topologyTestApp.setVisible(true);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }


  private static void parseCommandLine(String[] args) throws ParseException {
    commandLine = new CommandLine('-');
    OptionSpec os;

    os = new OptionSpec("files", OptionSpec.NARGS_ONE_OR_MORE);
    commandLine.addOptionSpec(os);

    os = new OptionSpec("properties", 1);
    commandLine.addOptionSpec(os);

    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMOP, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMFUNC, 1));

    os = new OptionSpec("gui", 0);
    commandLine.addOptionSpec(os);

    commandLine.addOptionSpec(new OptionSpec(OPT_TESTCASEINDEX, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_VERBOSE, 0));

    commandLine.parse(args);
  }

  private static void displayHelp() {
    System.out.println("");
    System.out.println("Usage: java com.vividsolutions.jtstest.testrunner.TopologyTestApp ");
    System.out.println("           [-files <.xml files>] [-gui] ");
    System.out.println("           [-geomfunc <classname>]");
    System.out.println("           [-geomop <GeometryOperation classname>]");
    System.out.println("           [-testIndex <number>]");
    System.out.println("           [-verbose]");
    System.out.println("           [-properties <file.properties>]");
    System.out.println("");
    System.out.println("  -files          run a list of .xml files or directories");
    System.out.println("                  containing .xml files");
    System.out.println("  -properties     load/save .xml filenames in a .properties file");
    System.out.println("  -geomfunc         specifies the class providing the geometry operations");
    System.out.println("  -geomop         specifies the class providing the geometry operations");
    System.out.println("  -gui            use the graphical user interface");
    System.out.println("  -testIndex      specfies the index of a single test to run");
    System.out.println("  -verbose        display the results of successful tests");
  }

  private static Collection filenames(Collection fileAndDirectoryNames) {
    Collection filenames = new ArrayList();
    for (Iterator i = fileAndDirectoryNames.iterator(); i.hasNext(); ) {
      String name = (String) i.next();
      File file = new File(name);
      if (file.isDirectory()) {
        filenames.addAll(filenames(file));
      }
      else if (file.isFile()) {
        filenames.add(name);
      }
    }
    return filenames;
  }

  private static Collection filenames(File directory) {
    Collection filenames = new ArrayList();
    Assert.isTrue(directory.isDirectory());
    File[] files = directory.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        filenames.add(files[i].getPath());
      }
    }
    return filenames;
  }

  private static Collection filenamesDeep(File directory) {
    Collection filenames = new ArrayList();
    Assert.isTrue(directory.isDirectory());
    File[] files = directory.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        filenames.add(files[i].getPath());
      }
      else if (files[i].isDirectory()) {
      	filenames.add(filenamesDeep(files[i]));
      }
    }
    return filenames;
  }

  private static java.util.List arguments(String optionName) {
    Option option = commandLine.getOption(optionName);
    ArrayList arguments = new ArrayList();
    for (int i = 0; i < option.getNumArgs(); i++) {
      arguments.add(option.getArg(i));
    }
    return arguments;
  }

  private static java.util.List testFiles() throws FileNotFoundException, IOException {
    java.util.List testFiles = new ArrayList();
    if (commandLine.hasOption("Files")) {
      testFiles.addAll(filenames(arguments("Files")));
    }
    if (commandLine.hasOption("Properties")) {
      Properties properties = new Properties();
      File file = new File(commandLine.getOption("Properties").getArg(0));
      if (!file.exists()) {
        file.createNewFile();
      }
      properties.load(new FileInputStream(commandLine.getOption("Properties").getArg(0)));
      String testFilesString = properties.getProperty("TestFiles");
      if (testFilesString != null) {
        testFiles.addAll(StringUtil.fromCommaDelimitedString(testFilesString));
      }
    }
    return testFiles;
  }

  public void add(java.util.List testFiles) {
    for (Iterator i = testFiles.iterator(); i.hasNext(); ) {
      File file = new File(i.next().toString());
      fileListModel.addElement(file);
    }
  }

  public String summary() {
    String summary = "";
    if (engine.getParseExceptionCount() > 0) {
      summary += engine.getParseExceptionCount() + " parsing exceptions\n";
    }
    summary += engine.getTestCaseCount() + " cases, " + engine.getTestCount() + " tests\n";
    summary += engine.getPassedCount()
         + " passed, " 
         + engine.getFailedCount() + " failed, " 
         + engine.getExceptionCount()
         + " threw exceptions";
    if (engine.getParseExceptionCount() + engine.getFailedCount() + engine.getExceptionCount() > 0) {
      summary += "*******  ERRORS ENCOUNTERED  ********";
    }
    return summary;
  }

  public String oneLineSummary() {
    return StringUtil.replaceAll(summary(), "\n", "; ");
  }

  void this_windowClosing(WindowEvent e) {
    try {
      saveProperties();
    }
    catch (Exception x) {
      reportException(x);
    }
    System.exit(0);
  }

  void this_windowOpened(WindowEvent e) {
    GuiUtil.centerOnScreen(this);
  }

  void addFileButton_actionPerformed(ActionEvent e) {
    try {
      if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File[] files = chooser.getSelectedFiles();
        for (int i = 0; i < files.length; i++) {
          fileListModel.addElement(files[i]);
        }
      }
      saveProperties();
    }
    catch (Exception x) {
      reportException(x);
    }
  }

  void removeFileButton_actionPerformed(ActionEvent e) {
    try {
      for (Iterator i = getSelectedTestFiles().iterator(); i.hasNext(); ) {
        fileListModel.removeElement(i.next());
      }
      saveProperties();
      updateEnabled();
    }
    catch (Exception x) {
      reportException(x);
    }
  }

  void runSelectedButton_actionPerformed(ActionEvent e) {
    run(getSelectedTestFiles());
  }

  void runAllButton_actionPerformed(ActionEvent e) {
    updateEnabled();
    logTextArea.setText("");
    run(getAllTestFiles());
  }

  void fileList_valueChanged(ListSelectionEvent e) { }

  /**
   *  Returns the selected test files.
   */
  private java.util.List getSelectedTestFiles() {
    return Arrays.asList(fileList.getSelectedValues());
  }

  /**
   *  Returns all the test files.
   */
  private java.util.List getAllTestFiles() {
    return Arrays.asList(fileListModel.toArray());
  }

  /**
   *  Reports the exception to the user.
   */
  private void reportException(Exception e) {
    e.printStackTrace(System.out);
    JOptionPane.showMessageDialog(this, StringUtil.getStackTrace(e), "Exception",
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   *  Initializes the Component's.
   */
  private void jbInit() throws Exception {
    fileList = new JList(fileListModel);
    this.getContentPane().setLayout(gridBagLayout1);
    setSize(450, 500);
    this.addWindowListener(
      new java.awt.event.WindowAdapter() {

        public void windowClosing(WindowEvent e) {
          this_windowClosing(e);
        }

        public void windowOpened(WindowEvent e) {
          this_windowOpened(e);
        }
      });
    jPanel5.setLayout(gridBagLayout4);
    this.setTitle("Topology Test App");
    jPanel3.setLayout(borderLayout1);
    statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    statusLabel.setText(" ");
    fileList.addListSelectionListener(
      new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent e) {
          updateEnabled();
        }
      });
    fileList.setBackground(SystemColor.control);
    fileList.setForeground(SystemColor.controlText);
    jScrollPane1.setMaximumSize(new Dimension(32767, 100));
    jScrollPane1.setMinimumSize(new Dimension(24, 100));
    jScrollPane1.setPreferredSize(new Dimension(260, 100));
    runSelectedButton.setEnabled(false);
    runSelectedButton.setText("Run Selected");
    runSelectedButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          runSelectedButton_actionPerformed(e);
        }
      });
    addFileButton.setText("Add...");
    addFileButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          addFileButton_actionPerformed(e);
        }
      });
    jPanel2.setLayout(flowLayout2);
    runAllButton.setText("Run All");
    runAllButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          runAllButton_actionPerformed(e);
        }
      });
    removeFileButton.setEnabled(false);
    removeFileButton.setText("Remove Selected");
    removeFileButton.addActionListener(
      new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          removeFileButton_actionPerformed(e);
        }
      });
    this.getContentPane().add(jPanel5, new GridBagConstraints(50, 83, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    jPanel5.add(jScrollPane2, new GridBagConstraints(50, 60, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2,
        2, 2), 0, 0));
    this.getContentPane().add(jPanel3, new GridBagConstraints(50, 102, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    jPanel3.add(statusLabel, BorderLayout.CENTER);
    this.getContentPane().add(jScrollPane1, new GridBagConstraints(50, 45, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    this.getContentPane().add(jPanel2, new GridBagConstraints(50, 54, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    jPanel2.add(addFileButton, null);
    jPanel2.add(removeFileButton, null);
    jPanel2.add(runSelectedButton, null);
    jPanel2.add(runAllButton, null);
    jScrollPane1.getViewport().add(fileList, null);
    jScrollPane2.getViewport().add(logTextArea, null);
  }

  /**
   *  Saves the app settings to the properties file.
   */
  private void saveProperties() throws FileNotFoundException, IOException {
    if (!commandLine.hasOption("Properties")) {
      return;
    }
    java.util.List testFiles = getAllTestFiles();
    String testFilesString = testFiles.isEmpty() ? "" : StringUtil.toCommaDelimitedString(testFiles);
    Properties properties = new Properties();
    properties.setProperty("TestFiles", testFilesString);
    properties.store(
        new FileOutputStream(commandLine.getOption("Properties").getArg(0)),
        "Properties file for " + getClass());
  }

  private void run(java.util.List testFiles) {
    engine.setTestFiles(testFiles);
    Thread t = new Thread(engine);
    t.start();
    timer.start();
    updateEnabled();
  }

  /**
   *  Sets the enabled state of the buttons.
   */
  private void updateEnabled() {
    removeFileButton.setEnabled((!engine.isRunning()) && fileList.getSelectedIndices().length > 0);
    runSelectedButton.setEnabled((!engine.isRunning()) && fileList.getSelectedIndices().length > 0);
    runAllButton.setEnabled(!engine.isRunning());
    addFileButton.setEnabled(!engine.isRunning());
  }

  private String report() {
    SimpleReportWriter reportWriter = new SimpleReportWriter(
        commandLine.hasOption("Verbose"));
    return reportWriter.writeReport(engine);
  }
}
