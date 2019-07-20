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
package org.locationtech.jtstest.cmd;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
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
import org.locationtech.jtstest.testbuilder.geom.GeometryUtil;
import org.locationtech.jtstest.testbuilder.io.SVGTestWriter;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.util.io.IOUtil;
import org.locationtech.jtstest.util.io.MultiFormatReader;

/**
 * A simple CLI to run TestBuilder operations.
 * Allows easier execution of JTS functions on test data for debugging purposes.
 * <p>
 * Examples:
 * 
 * <pre>
 * --- Compute the area of a WKT geometry, output it
 * jtsop -a some-file-with-geom.wkt -f txt area 
 * 
 * --- Compute the unary union of a WKT geometry, output as WKB
 * jtsop -a some-file-with-geom.wkt -f wkb Overlay.unaryUnion 
 * 
 * --- Compute the union of two geometries in WKT and WKB, output as WKT
 * jtsop -a some-file-with-geom.wkt -b some-other-geom.wkb -f wkt Overlay.Union
 * 
 * --- Compute the buffer of distance 10 of a WKT geometry, output as GeoJSON
 * jtsop -a some-file-with-geom.wkt -f geojson Buffer.buffer 10
 * 
 * --- Compute the buffer of a literal geometry, output as WKT
 * jtsop -a "POINT (10 10)" -f wkt Buffer.buffer 10
 * 
 * --- Output a literal geometry as GeoJSON
 * jtsop -a "POINT (10 10)" -f geojson
 * </pre>
 * 
 * @author Martin Davis
 *
 */
public class JTSOpCmd {

  // TODO: add option -ab to read both geoms from a file
  // TODO: allow -a stdin  to indicate reading from stdin.  
  
  static final String[] helpDoc = new String[] {
  "",
  "Usage: jtsop - CLI for JTS operations",
  "           [ -a <wkt> | <wkb> | <filename.ext>]",
  "           [ -b <wkt> | <wkb> | <filename.ext>]",
  "           [ -f ( txt | wkt | wkb | geojson | gml | svg ) ]",
  "           [ -geomfunc <classname>]",
  "           [ -v, -verbose]",
  "           [ -help]",
  "           [ op [ args... ]]",
  "  op              name of the operation (Category.op)",
  "  args            one or more scalar arguments to the operation",
  "",
  "  -a              A geometry or name of file containing it (extension: WKT, WKB, GeoJSON, GML, SHP)",
  "  -b              B geometry or name of file containing it (extension: WKT, WKB, GeoJSON, GML, SHP)",
  "  -f              output format to use.  If omitted output is silent",
  "  -geomfunc       (optional) specifies the class providing the geometry operations",
  "  -v, -verbose    display information about execution",
  "  -help           print a list of available operations"
  };

  private static final String FORMAT_GML = "gml";
  private static final String FORMAT_WKB = "wkb";
  private static final String FORMAT_TXT = "txt";
  private static final String FORMAT_WKT = "wkt";
  private static final String FORMAT_GEOJSON = "geojson";
  private static final String FORMAT_SVG = "svg";

  public static void main(String[] args)
  {    
    JTSOpCmd cmd = new JTSOpCmd();
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
    String geomA;
    public String geomB;
    public String arg1;
    
    String format = null;
  }

  
  public JTSOpCmd() {
    
  }
  
  private void execute(CmdArgs cmdArgs) throws IOException, Exception {
    
    Geometry geomA = null;
    Geometry geomB = null;

    if (cmdArgs.geomA != null) {
      geomA = readGeometry(cmdArgs.geomA);
      printGeometrySummary("A", geomA, cmdArgs.geomA);
    }
    if (cmdArgs.geomB != null) {
      geomB = readGeometry(cmdArgs.geomB);
      printGeometrySummary("B", geomB, cmdArgs.geomB);
    }
    // TODO: Handle option -ab
    
    
    Object result = null;
    if (cmdArgs.operation != null) {
      result = executeFunction(cmdArgs, geomA, geomB);
    }
    else {
      // no op specified, so just output A (allows format conversion)
      result = geomA;
    }
    if (result == null) return;
    
    if (cmdArgs.format != null) {
      printResult(result, cmdArgs.format);
    }
  }

  private Object executeFunction(CmdArgs cmdArgs, Geometry geomA, Geometry geomB) {
    GeometryFunction func = getFunction(cmdArgs.operation);
    if (func == null) {
      System.err.println("Function not found: " + cmdArgs.operation);
      return null;
    }
    Object funArgs[] = createFunctionArgs(func, geomB, cmdArgs.arg1);
    
    if (isVerbose) {
      System.out.println(writeOpSummary(cmdArgs));
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
    return result;
  }

  private Geometry readGeometry(String arg) throws Exception, IOException {
    if (isFilename(arg)) {
      return IOUtil.readFile(arg ,geomFactory );
    }
    MultiFormatReader rdr = new MultiFormatReader(geomFactory);
    return rdr.read(arg);
  }

  private boolean isFilename(String arg) {
    if (arg == null) return false;
    if (MultiFormatReader.isWKB(arg)) return false;
    if (isWKT(arg)) return false;
    /*
    if (arg.indexOf("/") > 0
        || arg.indexOf("\\") > 0
        || arg.indexOf(":") > 0)
      return true;
      */
    return true;
  }

  private static boolean isWKT(String arg) {
    // TODO: make this smarter
    boolean hasParen = (arg.indexOf("(") > 0) && arg.indexOf(")") > 0;
    if (hasParen) return true;
    return false;
  }

  private String writeOpSummary(CmdArgs cmdArgs) {
    StringBuilder sb = new StringBuilder();
    sb.append("Op: " + cmdArgs.operation);
    if (cmdArgs.arg1 != null) {
      sb.append(" " + cmdArgs.arg1);
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
    if (outputFormat.equalsIgnoreCase(FORMAT_WKT)
        || outputFormat.equalsIgnoreCase(FORMAT_TXT)) {
      txt = geom.toString();
    }
    else if (outputFormat.equalsIgnoreCase(FORMAT_WKB)) {
      txt = WKBWriter.toHex((new WKBWriter().write(geom)));
    }
    else if (outputFormat.equalsIgnoreCase(FORMAT_GML)) {
      txt = (new GMLWriter()).write(geom);
    }
    else if (outputFormat.equalsIgnoreCase(FORMAT_GEOJSON)) {
      txt = writeGeoJSON(geom);
    }
    else if (outputFormat.equalsIgnoreCase(FORMAT_SVG)) {
      txt = SVGTestWriter.getSVG(geom, null);
    }
    
    if (txt == null) return;
    System.out.println(txt);
  }

  private String writeGeoJSON(Geometry geom) {
    GeoJsonWriter writer = new GeoJsonWriter();
    writer.setEncodeCRS(false);
    return writer.write(geom);
  }

  private void printGeometrySummary(String label, Geometry geom, String arg) {
    if (! isVerbose) return;
    String filename = "";
    if (arg != null & isFilename(arg)) filename = " -- " + arg;
    System.out.println( writeGeometrySummary(label, geom) + filename);
  }
  
  private String writeGeometrySummary(String label,
      Geometry g)
  {
    if (g == null) return "";
    StringBuilder buf = new StringBuilder();
    buf.append(label + " : ");
    buf.append(GeometryUtil.structureSummary(g));
    buf.append("    " + GeometryUtil.metricsSummary(g));
    return buf.toString();
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
    cmdArgs.geomA = commandLine.getOptionArg(CommandOptions.GEOMA, 0);
    cmdArgs.geomB = commandLine.getOptionArg(CommandOptions.GEOMB, 0);
    cmdArgs.arg1 = commandLine.getOptionArg(CommandOptions.ARG1, 0);
    cmdArgs.format = commandLine.getOptionArg(CommandOptions.FORMAT, 0);
    isVerbose = commandLine.hasOption(CommandOptions.VERBOSE)
                  || commandLine.hasOption(CommandOptions.V);
    isHelp = commandLine.hasOption(CommandOptions.HELP);

    String[] freeArgs = commandLine.getOptionArgs(OptionSpec.OPTION_FREE_ARGS);
    if (freeArgs != null) {
      if (freeArgs.length >= 1) {
        cmdArgs.operation = freeArgs[0];
      }
      if (freeArgs.length >= 2) {
        cmdArgs.arg1 = freeArgs[1];
      }
    }
    
    return cmdArgs;
  }
  
  private static CommandLine createCmdLine() {
    commandLine = new CommandLine('-');
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.GEOMFUNC, OptionSpec.NARGS_ONE_OR_MORE))
    .addOptionSpec(new OptionSpec(CommandOptions.VERBOSE, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.V, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.HELP, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.OP, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.GEOMA, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.GEOMB, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.ARG1, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.FORMAT, 1))
    .addOptionSpec(new OptionSpec(OptionSpec.OPTION_FREE_ARGS, OptionSpec.NARGS_ONE_OR_MORE));
    return commandLine;
  }

}
