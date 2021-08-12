/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testrunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.locationtech.jts.JTSVersion;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.command.CommandLine;
import org.locationtech.jtstest.command.Option;
import org.locationtech.jtstest.command.OptionSpec;
import org.locationtech.jtstest.command.ParseException;
import org.locationtech.jtstest.geomop.GeometryFunctionOperation;
import org.locationtech.jtstest.geomop.GeometryFunctionRegistry;
import org.locationtech.jtstest.geomop.GeometryOperation;
import org.locationtech.jtstest.geomop.TestCaseGeometryFunctions;
import org.locationtech.jtstest.util.FilesUtil;
import org.locationtech.jtstest.util.StringUtil;

/**
 * A command-line utility to execute tests specified in JTS Test XML files.
 * Displays status and any errors encountered.
 * <p>
 * <b>Command Line Options</b>
 * 
 * <table border='1'> <tr> <td><tt>-files {
 * <i>&lt;fileOrDirectoryName&gt;</i></tt> } </td> <td>req</td> <td>Specifies
 * the XML test files to run</td> </tr> <tr> <td><tt>-geomop
 * <i>&lt;classname&gt;</i></tt> </td> <td>opt</td> <td>Specifies a custom
 * {@link GeometryOperation} to be used</td> </tr> <tr> <tr>
 * <td><tt>-testCaseIndex <i>&lt;num&gt;</i></tt> </td> <td>opt</td>
 * <td>Specifies the index of a single test to run</td> </tr> <tr>
 * <td><tt>-verbose</tt> </td> <td>opt</td> <td>Provides verbose output</td>
 * </tr> </table>
 *
 * @version 1.7
 */
public class JTSTestRunnerCmd {

  static final String[] help = new String[] {
  "",
  "Usage: java org.locationtech.jtstest.testrunner.JTSTestRunnerCmd",
  "           [ -geomfunc <classpath>...]",
  "           [ -geomop <GeometryOperation classname>]",
  "           [ -testIndex <number>]",
  "           [ -verbose]",
  "           [ -op <op name>]",
  "           [ -afile <filename>]",
  "           [ -bfile <filename>]",
  "           [ -properties <file.properties>]",
  "           [ -files <.xml file or dir> ...]",
  "           [ <.xml file or dir> ... ]",
  "  -files          run a list of .xml files or directories containing .xml files",
  "  -properties     load .xml filenames from a .properties file",
  "  -geomfunc       specifies class(es) with static methods overriding or adding geometry functions",
  "  -geomop         specifies the class providing the geometry operations",
  "  -testIndex      specfies the index of a single test to run",
  "  -verbose        display the results of successful tests"
  };
  
  private static final String PROPERTY_TESTFILES = "TestFiles";
  private static final String OPT_FILES = "files";
  private static final String OPT_GEOMFUNC = "geomfunc";
  private static final String OPT_GEOMOP = "geomop";
  private static final String OPT_PROPERTIES = "properties";
  private static final String OPT_TESTCASEINDEX = "testCaseIndex";
  private static final String OPT_VERBOSE = "verbose";
  
  private static final String OPT_OP = "op";
  private static final String OPT_GEOMAFILE = "afile";
  private static final String OPT_GEOMBFILE = "bfile";
  private static final String OPT_ARG1 = "arg1";

  private static final String FILENAME_EXTENSION = "xml";
  
  

  private static GeometryFunctionRegistry funcRegistry = new GeometryFunctionRegistry(TestCaseGeometryFunctions.class);
  private static GeometryOperation defaultOp = new GeometryFunctionOperation(funcRegistry);
  private static GeometryOperation geometryOp = defaultOp;

  public static GeometryOperation getGeometryOperation() {
    return geometryOp;
  }

  /**
   * Tests whether a GeometryOperation was specified on the command line
   * 
   * @return true if a geometry operation was specified
   */
  public static boolean isGeometryOperationSpecified() {
    return geometryOp != defaultOp;
  }

  private static ResultMatcher defaultResultMatcher = new EqualityResultMatcher();
  private static ResultMatcher resultMatcher = defaultResultMatcher;

  public static ResultMatcher getResultMatcher() {
    return resultMatcher;
  }

  /**
   * Tests whether a {@link ResultMatcher} was specified on the command line
   * 
   * @return true if a matcher was specified
   */
  public static boolean isResultMatcherSpecified() {
    return resultMatcher != defaultResultMatcher;
  }

  private TestEngine engine = new TestEngine();

  public JTSTestRunnerCmd() {

  }

  private void run(TestRunnerOptions options) throws FileNotFoundException, IOException, ParseException, org.locationtech.jts.io.ParseException {
    List<File> files = FilesUtil.toFile(options.filenames);
    
    if (options.testCaseIndex >= 0) {
      engine.setTestCaseIndexToRun(options.testCaseIndex);
      System.out.println("Running test case # " + options.testCaseIndex);
    }
    
    boolean hasCmdLineTest = options.operation != null;
    if (hasCmdLineTest) {
      runOperation(options);
    }
    else {
      engine.setTestFiles(files);
      engine.run();
      System.out.println(report(options.isVerbose));
    }
  }

  private TestRun createTestRun(TestRunnerOptions options) throws IOException, ParseException, org.locationtech.jts.io.ParseException {
    TestRunBuilder trb = new TestRunBuilder();
    trb.setOperation(options.operation);
    trb.readGeometryAFromFile(options.geomAFilename);
    if (options.geomBFilename != null) {
      trb.readGeometryBFromFile(options.geomBFilename);
    }
    trb.setArguments(getArguments(options));
    return trb.build();
  }
  
  private void runOperation(TestRunnerOptions options) throws IOException, ParseException, org.locationtech.jts.io.ParseException {
    TestRun testRun = createTestRun(options);
    Stopwatch sw = new Stopwatch();
    testRun.run();
    System.out.println("Run time: " + sw.getTimeString());
  }

  private List<String> getArguments(TestRunnerOptions options) {
    List<String> args = new ArrayList<String>();
    if (options.arg1 != null) {
      args.add(options.arg1);
    }
    return args;
  }

  private String report(boolean isVerbose) {
    SimpleReportWriter reportWriter = new SimpleReportWriter(isVerbose);
    return reportWriter.writeReport(engine);
  }

  private static class TestRunnerOptions {
    String geomAFilename;
    String operation;
    List<String> filenames;
    boolean isVerbose = false;
    int testCaseIndex = -1;
    public String geomBFilename;
    public String arg1;
  }

  public static void main(String[] args) {
    try {

      System.out
          .println("=====  Test Runner  -  JTS Topology Suite (Version " + JTSVersion.CURRENT_VERSION + ")  =====");

      JTSTestRunnerCmd testRunner = new JTSTestRunnerCmd();
      if (args.length == 0) {
        printHelp();
        System.exit(0);
      }

      TestRunnerOptions options = readOptions(args);
      testRunner.run(options);
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static TestRunnerOptions readOptions(String[] args) throws ParseException, FileNotFoundException, IOException, ClassNotFoundException {
    CommandLine commandLine = createCommandLine();
    commandLine.parse(args);
    
    TestRunnerOptions opts = new TestRunnerOptions();
    if (commandLine.hasOption(OPT_GEOMOP)) {
      loadGeomOp( commandLine.getOption(OPT_GEOMOP).getArg(0) );
    }

    if (commandLine.hasOption(OPT_GEOMFUNC)) {
      String[] geomFuncClassnames = commandLine.getOption(OPT_GEOMFUNC).getArgs();
      for (String cls : geomFuncClassnames) {
        System.out.println("Adding Geometry Functions from: " + cls);
        funcRegistry.add(cls);
      }
    }

    if (commandLine.hasOption(OPT_TESTCASEINDEX)) {
      opts.testCaseIndex = commandLine.getOption(OPT_TESTCASEINDEX).getArgAsInt(0);
    }
    if (commandLine.hasOption(OPT_OP)) {
      opts.operation = commandLine.getOption(OPT_OP).getArg(0);
    }
    if (commandLine.hasOption(OPT_GEOMAFILE)) {
      opts.geomAFilename = commandLine.getOption(OPT_GEOMAFILE).getArg(0);
    }
    if (commandLine.hasOption(OPT_GEOMBFILE)) {
      opts.geomBFilename = commandLine.getOption(OPT_GEOMBFILE).getArg(0);
    }
    if (commandLine.hasOption(OPT_ARG1)) {
      opts.arg1 = commandLine.getOption(OPT_ARG1).getArg(0);
    }
    opts.isVerbose = commandLine.hasOption(OPT_VERBOSE);
    opts.filenames = extractTestFilenames(commandLine);
    return opts;
  }

  private static void loadGeomOp(String geomOpClassname) {
    geometryOp = GeometryOperationLoader.createGeometryOperation(JTSTestRunnerCmd.class.getClassLoader(),
        geomOpClassname);
    // loading must have failed - abort
    if (geometryOp == null) {
      System.out.println("Unable to load Geometry Operation: " + geomOpClassname);
      System.exit(0);
    }
    System.out.println("Using Geometry Operation: " + geomOpClassname);
  }
  
  private static List<String> extractTestFilenames(CommandLine commandLine) throws FileNotFoundException, IOException {
    List<String> testFiles = new ArrayList<String>();
    
    if (commandLine.hasOption(OptionSpec.OPTION_FREE_ARGS)) {
      testFiles.addAll(FilesUtil.expand(cmdOptionArgList(commandLine, OptionSpec.OPTION_FREE_ARGS), FILENAME_EXTENSION));
    }
    
    if (commandLine.hasOption(OPT_FILES)) {
      testFiles.addAll(FilesUtil.expand(cmdOptionArgList(commandLine, OPT_FILES), FILENAME_EXTENSION));
    }
    
    if (commandLine.hasOption(OPT_PROPERTIES)) {
      Properties properties = new Properties();
      File file = new File(commandLine.getOption(OPT_PROPERTIES).getArg(0));
      properties.load(new FileInputStream(commandLine.getOption(OPT_PROPERTIES).getArg(0)));
      String testFilesString = properties.getProperty(PROPERTY_TESTFILES);
      if (testFilesString != null) {
        testFiles.addAll(StringUtil.fromCommaDelimitedString(testFilesString));
      }
    }
    return testFiles;
  }
  
  private static CommandLine createCommandLine() throws ParseException {
    CommandLine commandLine = new CommandLine('-');
    OptionSpec os;

    os = new OptionSpec(OptionSpec.OPTION_FREE_ARGS, OptionSpec.NARGS_ONE_OR_MORE);
    commandLine.addOptionSpec(os);

    os = new OptionSpec(OPT_FILES, OptionSpec.NARGS_ONE_OR_MORE);
    commandLine.addOptionSpec(os);

    commandLine.addOptionSpec(new OptionSpec(OPT_PROPERTIES, 1));

    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMOP, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMFUNC, OptionSpec.NARGS_ONE_OR_MORE));

    commandLine.addOptionSpec(new OptionSpec(OPT_TESTCASEINDEX, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_VERBOSE, 0));
    
    commandLine.addOptionSpec(new OptionSpec(OPT_OP, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMAFILE, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMBFILE, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_ARG1, 1));

    return commandLine;
  }
  
  private static void printHelp() {
    for (String s : help) {
      System.out.println(s);
    }
  }

  public static List<String> cmdOptionArgList(CommandLine commandLine, String optionName) {
    Option option = commandLine.getOption(optionName);
    ArrayList<String> arguments = new ArrayList<String>();
    for (int i = 0; i < option.getNumArgs(); i++) {
      arguments.add(option.getArg(i));
    }
    return arguments;
  }


}
