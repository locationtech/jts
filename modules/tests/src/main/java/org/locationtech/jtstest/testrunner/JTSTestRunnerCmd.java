
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.locationtech.jts.JTSVersion;
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

  private static final String PROPERTY_TESTFILES = "TestFiles";
  private static final String OPT_FILES = "files";
  private static final String OPT_GEOMFUNC = "geomfunc";
  private static final String OPT_GEOMOP = "geomop";
  private static final String OPT_PROPERTIES = "properties";
  private static final String OPT_TESTCASEINDEX = "testCaseIndex";
  private static final String OPT_VERBOSE = "verbose";

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

  private void run(TestRunnerOptions options) throws FileNotFoundException, IOException {
    List<File> files = FilesUtil.toFile(options.filenames);
    
    if (options.testCaseIndex >= 0) {
      engine.setTestCaseIndexToRun(options.testCaseIndex);
      System.out.println("Running test case # " + options.testCaseIndex);
    }
    
    engine.setTestFiles(files);
    engine.run();
    System.out.println(report(options.isVerbose));
  }

  private String report(boolean isVerbose) {
    SimpleReportWriter reportWriter = new SimpleReportWriter(isVerbose);
    return reportWriter.writeReport(engine);
  }

  private static class TestRunnerOptions {
    List<String> filenames;
    boolean isVerbose = false;
    int testCaseIndex = -1;
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

  private static TestRunnerOptions readOptions(String[] args) throws ParseException, FileNotFoundException, IOException {
    CommandLine commandLine = createCommandLine();
    commandLine.parse(args);
    
    TestRunnerOptions opts = new TestRunnerOptions();
    if (commandLine.hasOption(OPT_GEOMOP)) {
      String geomOpClassname = commandLine.getOption(OPT_GEOMOP).getArg(0);
      geometryOp = GeometryOperationLoader.createGeometryOperation(JTSTestRunnerCmd.class.getClassLoader(),
          geomOpClassname);
      // loading must have failed - abort
      if (geometryOp == null) {
        System.exit(0);
      }
      System.out.println("Using Geometry Operation: " + geomOpClassname);
    }

    /*
     * if (commandLine.hasOption(OPT_GEOMFUNC)) { String geomFuncClassname =
     * commandLine.getOption(OPT_GEOMFUNC).getArg(0); System.out.println(
     * "Adding Geometry Functions from: " + geomFuncClassname);
     * funcRegistry.add(geomFuncClassname); }
     */

    if (commandLine.hasOption(OPT_TESTCASEINDEX)) {
      opts.testCaseIndex = commandLine.getOption(OPT_TESTCASEINDEX).getArgAsInt(0);
    }
    opts.isVerbose = commandLine.hasOption(OPT_VERBOSE);
    opts.filenames = extractTestFilenames(commandLine);
    return opts;
  }
  
  private static List<String> extractTestFilenames(CommandLine commandLine) throws FileNotFoundException, IOException {
    List<String> testFiles = new ArrayList<String>();
    if (commandLine.hasOption(OPT_FILES)) {
      testFiles.addAll(FilesUtil.expand(cmdOptionArgList(commandLine, OPT_FILES)));
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

    os = new OptionSpec(OPT_FILES, OptionSpec.NARGS_ONE_OR_MORE);
    commandLine.addOptionSpec(os);

    os = new OptionSpec(OPT_PROPERTIES, 1);
    commandLine.addOptionSpec(os);

    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMOP, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_GEOMFUNC, 1));

    commandLine.addOptionSpec(new OptionSpec(OPT_TESTCASEINDEX, 1));
    commandLine.addOptionSpec(new OptionSpec(OPT_VERBOSE, 0));

    return commandLine;
  }

  private static void printHelp() {
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
    System.out.println("  -properties     load .xml filenames from a .properties file");
    System.out.println("  -geomfunc       specifies the class providing the geometry operations");
    System.out.println("  -geomop         specifies the class providing the geometry operations");
    System.out.println("  -testIndex      specfies the index of a single test to run");
    System.out.println("  -verbose        display the results of successful tests");
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
