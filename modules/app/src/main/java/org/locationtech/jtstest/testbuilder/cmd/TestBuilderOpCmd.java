/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.cmd;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.gml2.GMLWriter;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.command.CommandLine;
import org.locationtech.jtstest.command.Option;
import org.locationtech.jtstest.command.OptionSpec;
import org.locationtech.jtstest.command.ParseException;
import org.locationtech.jtstest.function.DoubleKeyMap;
import org.locationtech.jtstest.geomfunction.BaseGeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunctionRegistry;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.util.io.IOUtil;

public class TestBuilderOpCmd {
  static final String[] helpDoc = new String[] {
  "",
  "Usage: java org.locationtech.jtstest.testrunner.JTSTestRunnerCmd",
  "           [ -geomfunc <classname>]",
  "           [ -verbose]",
  "           [ -help]",
  "           [ -op <op name>]",
  "           [ -afile <filename.ext>]",
  "           [ -bfile <filename.ext>]",
  "           [ -output ( wkt | wkb | geojson | gml ) ]",
  "  -geomfunc       (optional) specifies the class providing the geometry operations",
  "  -op             name of the operation (Category.op)",
  "  -afile          name of the file containing geometry A (extension: WKT, WKB, GeoJSON, GML, SHP)",
  "  -bfile          name of the file containing geometry B (extension: WKT, WKB, GeoJSON, GML, SHP)",
  "  -output         output format to use (WKT, WKB, GML)",
  "  -verbose        display information about execution",
  "  -help           print a list of available operations"
  };

  public static void main(String[] args)
  {    
    TestBuilderOpCmd cmd = new TestBuilderOpCmd();
    try {
      CmdArgs cmdArgs = parseArgs(args);
      
      if (args.length == 0 || isHelp) {
        printHelp(isHelp);
        System.exit(0);
      }

      cmd.execute(cmdArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void printHelp(boolean showFunctions) {
    for (String s : helpDoc) {
      System.out.println(s);
    }
    if (showFunctions) {
      System.out.println();
      System.out.println("Operations:");
      printFunctions();
   }
  }
  
  private static void printFunctions() {
    //TODO: include any loaded functions
    DoubleKeyMap funcMap = funcRegistry.getCategorizedGeometryFunctions();
    Collection categories = funcMap.keySet();
    for (Iterator i = categories.iterator(); i.hasNext();) {
      String category = (String) i.next();

      Collection funcs = funcMap.values(category);
      for (Iterator j = funcs.iterator(); j.hasNext();) {
        GeometryFunction fun = (GeometryFunction) j.next();
        System.out.println(category + "." + functionDesc(fun));
      }
    }
  }

  private static String functionDesc(GeometryFunction fun) {
    // TODO: display this in a command arg style
    BaseGeometryFunction geomFun = (BaseGeometryFunction) fun;
    return geomFun.getSignature();
    //return geomFun.getName();
  }

  private static GeometryFactory geomFactory = new GeometryFactory();
  
  private static GeometryFunctionRegistry funcRegistry = GeometryFunctionRegistry.createTestBuilderRegistry();
  private static CommandLine commandLine = createCmdLine();

  private static boolean isHelp = false;
  private static boolean isVerbose = false;  
  
  private static class CmdArgs {
    String operation;
    String geomAFilename;
    public String geomBFilename;
    public String arg1;
    
    String output = null;
  }

  
  public TestBuilderOpCmd() {
    
  }
  
  private void execute(CmdArgs cmdArgs) throws IOException, Exception {
    
    Geometry geomA = null;
    Geometry geomB = null;

    if (cmdArgs.geomAFilename != null) {
      geomA = IOUtil.readFile(cmdArgs.geomAFilename,geomFactory );
    }
    if (cmdArgs.geomBFilename != null) {
      geomB = IOUtil.readFile(cmdArgs.geomBFilename,geomFactory );
    }
    
    GeometryFunction func = getFunction(cmdArgs.operation);
    if (func == null) {
      System.err.println("Function not found: " + cmdArgs.operation);
      return;
    }
    Object funArgs[] = createFunctionArgs(func, geomB, cmdArgs.arg1);
    
    if (isVerbose) {
      System.out.println(opSummary(cmdArgs, geomA, geomB));
    }

    Stopwatch timer = new Stopwatch();
    Object result = null;
    try {
      result = func.invoke(geomA, funArgs);
    } finally {
      timer.stop();
    }
    if (isVerbose) {
      System.out.println("Time: " + timer.getTimeString());
    }
    if (cmdArgs.output != null) {
      printResult(result, cmdArgs.output);
    }
  }

  private String opSummary(CmdArgs cmdArgs, Geometry geomA, Geometry geomB) {
    StringBuilder sb = new StringBuilder();
    sb.append("Operation: " + cmdArgs.operation);
    if (cmdArgs.geomAFilename != null) {
      sb.append(" A=" + cmdArgs.geomAFilename);
      // TODO: print geometry summary
    }
    if (cmdArgs.geomBFilename != null) {
      sb.append(" B=" + cmdArgs.geomBFilename);
    }
    if (cmdArgs.arg1 != null) {
      sb.append(" arg1=" + cmdArgs.arg1);
    }
    return sb.toString();
  }

  private void printResult(Object result, String outputFormat) {
    if (result == null) return;
    
    if (result instanceof Geometry) {
      printGeometry((Geometry) result, outputFormat);
      return;
    }
    System.out.println(result);
  }
  
  private void printGeometry(Geometry geom, String outputFormat) {
    String txt = null;
    if (outputFormat.equalsIgnoreCase("wkt")) {
      txt = geom.toString();
    }
    if (outputFormat.equalsIgnoreCase("wkb")) {
      txt = WKBWriter.toHex((new WKBWriter().write(geom)));
    }
    if (outputFormat.equalsIgnoreCase("gml")) {
      txt = (new GMLWriter()).write(geom);
    }
    if (txt == null) return;
    System.out.println(txt);
  }

  private Object[] createFunctionArgs(GeometryFunction func, Geometry geomB, String arg1) {
    Class[] paramTypes = func.getParameterTypes();
    Object[] paramVal = new Object[paramTypes.length];
    
    int iparam = 0;
    if (func.isBinary()) {
      paramVal[0] = geomB;
      iparam++;
    }
    if (iparam < paramVal.length) {
      paramVal[iparam] = SwingUtil.coerce(arg1, paramTypes[iparam]);
    }
    return paramVal;
  }

  private GeometryFunction getFunction(String operation) {
    String category = "Geometry";
    String name = operation;
    String[] opCatName = operation.split("\\.");
    if (opCatName.length == 2) {
      category = opCatName[0];
      name = opCatName[1];
    }
    return funcRegistry.find(category, name);
  }

  private static CmdArgs parseArgs(String[] args) throws ParseException, ClassNotFoundException {
    commandLine.parse(args);

    CmdArgs cmdArgs = new CmdArgs();
    
    if (commandLine.hasOption(CommandOptions.GEOMFUNC)) {
      Option opt = commandLine.getOption(CommandOptions.GEOMFUNC);
      for (int i = 0; i < opt.getNumArgs(); i++) {
        String geomFuncClassname = opt.getArg(i);
        try {
          funcRegistry.add(geomFuncClassname);
          System.out.println("Added Geometry Functions from: " + geomFuncClassname);
        } catch (ClassNotFoundException ex) {
          System.out.println("Unable to load function class: " + geomFuncClassname);
        }
      }
    }
    
    cmdArgs.operation = commandLine.getOptionArg(CommandOptions.OP, 0);
/*
    if (commandLine.hasOption(CommandOptions.OP)) {
      cmdArgs.operation = commandLine.getOption(CommandOptions.OP).getArg(0);
    }
    */
    if (commandLine.hasOption(CommandOptions.GEOMAFILE)) {
      cmdArgs.geomAFilename = commandLine.getOption(CommandOptions.GEOMAFILE).getArg(0);
    }
    if (commandLine.hasOption(CommandOptions.GEOMBFILE)) {
      cmdArgs.geomBFilename = commandLine.getOption(CommandOptions.GEOMBFILE).getArg(0);
    }
    if (commandLine.hasOption(CommandOptions.ARG1)) {
      cmdArgs.arg1 = commandLine.getOption(CommandOptions.ARG1).getArg(0);
    }
    cmdArgs.output = commandLine.getOptionArg(CommandOptions.OUTPUT, 0);
    isVerbose = commandLine.hasOption(CommandOptions.VERBOSE);
    isHelp = commandLine.hasOption(CommandOptions.HELP);

    return cmdArgs;
  }
  
  private static CommandLine createCmdLine() {
    commandLine = new CommandLine('-');
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.GEOMFUNC, OptionSpec.NARGS_ONE_OR_MORE));
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.VERBOSE, 0));
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.HELP, 0));
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.OP, 1));
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.GEOMAFILE, 1));
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.GEOMBFILE, 1));
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.ARG1, 1));
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.OUTPUT, 1));
    return commandLine;
  }

}
