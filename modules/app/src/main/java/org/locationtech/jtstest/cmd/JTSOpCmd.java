/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.cmd;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.JTSVersion;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTConstants;
import org.locationtech.jtstest.cmd.JTSOpRunner.OpParams;
import org.locationtech.jtstest.command.CommandLine;
import org.locationtech.jtstest.command.Option;
import org.locationtech.jtstest.command.OptionSpec;
import org.locationtech.jtstest.command.ParseException;
import org.locationtech.jtstest.function.DoubleKeyMap;
import org.locationtech.jtstest.geomfunction.BaseGeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunctionRegistry;
import org.locationtech.jtstest.util.io.MultiFormatReader;

/**
 * A CLI to run JTS TestBuilder operations.
 * Allows easier execution of JTS functions on test data for debugging purposes.
 * <p>
 * Examples:
 * 
 * <pre>
 * --- Compute the area of a WKT geometry, output it
 * jtsop -a some-file-with-geom.wkt -f txt area 
 * 
 * --- Validate geometries from a WKT file using limit and offset
 * jtsop -a some-file-with-geom.wkt -limit 100 -offset 40 -f txt isValid 
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
 * --- Compute buffers of multiple sizes
 * jtsop -a "POINT (10 10)" -f wkt Buffer.buffer 1,10,100
 * 
 * --- Run op for each A 
 * jtsop -a "MULTIPOINT ((10 10), (20 20))" -eacha -f wkt Buffer.buffer
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
  public static final String ERR_INVALID_ARG_PARAM = "Invalid argument parameter";

  private static final String MACRO_VAL = "val";

  public static void main(String[] args)
  {    
    JTSOpCmd cmd = new JTSOpCmd();
    int rc = 1;
    try {
      OpParams cmdArgs = cmd.parseArgs(args);
      cmd.execute(cmdArgs);
      rc = 0;
    } 
    catch (CommandError e) {
      // for command errors, just print the message
      System.err.println(e.getMessage() );
    }
    catch (ParseException e) {
      System.err.println(e.getMessage() );
    }
    catch (Exception e) {
      // unexpected errors get a stack track to help debugging
      e.printStackTrace();
    }
    System.exit(rc);  // err code
  }

  private static CommandLine createCmdLine() {
    CommandLine commandLine = new CommandLine('-');
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.GEOMFUNC, OptionSpec.NARGS_ONE_OR_MORE))
    .addOptionSpec(new OptionSpec(CommandOptions.TIME, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.VERBOSE, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.V, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.HELP, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.OP, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.GEOMA, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.GEOMB, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.GEOMAB, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.COLLECT, 0))
    //.addOptionSpec(new OptionSpec(CommandOptions.EACH, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.EACHA, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.EACHB, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.INDEX, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.EXPLODE, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.FORMAT, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.LIMIT, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.OFFSET, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.REPEAT, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.SRID, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.WHERE, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.VALIDATE, 0))
    .addOptionSpec(new OptionSpec(OptionSpec.OPTION_FREE_ARGS, OptionSpec.NARGS_ONE_OR_MORE));
    return commandLine;
  }

  static final String[] helpDoc = new String[] {
  "",
  "Usage: jtsop - CLI for JTS operations",
  "           [ -a  <wkt> | <wkb> | stdin | <filename.ext> ]",
  "           [ -b  <wkt> | <wkb> | stdin | <filename.ext> ]",
  "           [ -ab <wkt> | <wkb> | stdin | <filename.ext> ]",
  "           [ -limit N ]",
  "           [ -offset N ]",
  "           [ -collect ]",
  "           [ -eacha ]",
  "           [ -eachb ]",
  "           [ -index ]",
  "           [ -repeat N ]",
  "           [ -where D ]",
  "           [ -validate ]",
  "           [ -explode",
  "           [ -srid SRID ]",
  "           [ -f ( txt | wkt | wkb | geojson | gml | svg ) ]",
  "           [ -time ]",
  "           [ -v, -verbose ]",
  "           [ -help ]",
  "           [ -geomfunc classname ]",
  "           [ -op ]",
  "           [ op [ args... ]]",
  "           op       name of the operation (in format Category.op)",
  "           args     one or more scalar arguments to the operation",
  "           - To run over multiple arguments use v1,v2,v3 OR val(v1,v2,v3,..)",
  "",
  "===== Input options:",
  "  -a              Geometry A: literal, stdin (WKT or WKB), or filename (extension: WKT, WKB, GeoJSON, GML, SHP)",
  "  -b              Geometry A: literal, stdin (WKT or WKB), or filename (extension: WKT, WKB, GeoJSON, GML, SHP)",
  "  -limit          Limits the number of geometries read from A, or B if specified",
  "  -offset         Uses an offset to read geometries  from A, or B if specified",
  "===== Operation options:",
  "  -collect        execute op on collection of A geometries",
  "  -eacha          execute op on each element of A",
  "  -eachb          execute op on each element of B",
  "  -index          index the B geometries",
  "  -repeat         repeat the operation N times",
  "  -where          output geometry where operation result equals the value D (1=true, 0=false)",
  "  -validate       validate the result of each operation",
  "  -geomfunc       specifies class providing geometry operations",
  "  -op             separator to delineate operation arguments",
  "===== Output options:",
  "  -srid           Sets the SRID on output geometries",
  "  -explode        output atomic geometries",
  "  -f              output format to use.  If omitted output is silent",
  "===== Logging options:",
  "  -time           display execution time",
  "  -v, -verbose    display information about execution",
  "  -help           print a list of available operations"
  };
  
  private void printHelp(boolean showFunctions) {
    System.out.println("JTSOp  --  Version " + JTSVersion.CURRENT_VERSION);
    for (String s : helpDoc) {
      System.out.println(s);
    }
    if (showFunctions) {
      System.out.println();
      System.out.println("Operations:");
      printFunctions();
   }
  }
  
  private void printFunctions() {
    //TODO: include any loaded functions
    DoubleKeyMap funcMap = funcRegistry.getCategorizedGeometryFunctions();
    @SuppressWarnings("unchecked")
    Collection<String> categories = funcMap.keySet();
    for (String category : categories) {
      @SuppressWarnings("unchecked")
      Collection<GeometryFunction> funcs = funcMap.values(category);
      for (GeometryFunction fun : funcs) {
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

  private GeometryFunctionRegistry funcRegistry = GeometryFunctionRegistry.createTestBuilderRegistry();
  private CommandLine commandLine = createCmdLine();

  private boolean isHelp = false;
  private boolean isHelpWithFunctions = false;

  private JTSOpRunner opRunner;

  public JTSOpCmd() {
    opRunner = new JTSOpRunner();
  }
  
  public void captureOutput() {
    opRunner.captureOutput();
  }
  
  public void captureResult() {
    opRunner.captureResult();
  }
  
  public List<Geometry> getResultGeometry() {
    return opRunner.getResultGeometry();
  }
  
  public void replaceStdIn(InputStream inStream) {
    opRunner.replaceStdIn(inStream);
  }
  
  public String getOutput() {
    return opRunner.getOutput();
  }
  
  public String[] getOutputLines() {
    String outAll = opRunner.getOutput();
    return outAll.split("\n");
  }
  
  public static boolean isFilename(String arg) {
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
    // TODO: make this smarter?
    boolean hasParen = (arg.indexOf("(") > 0) && arg.indexOf(")") > 0;
    if (hasParen) return true;
    if (arg.toUpperCase().endsWith(" " + WKTConstants.EMPTY)) return true;
    return false;
  }
  
  void execute(JTSOpRunner.OpParams cmdArgs) {
    if (isHelp || isHelpWithFunctions) {
      printHelp(isHelpWithFunctions);
      return;
    }
    opRunner.setRegistry(funcRegistry);
    opRunner.execute(cmdArgs);
  }

  JTSOpRunner.OpParams parseArgs(String[] args) throws ParseException, ClassNotFoundException {
    
    if (args.length == 0) {
      isHelp = true;
      return null;
    }
    commandLine.parse(args);

    OpParams cmdArgs = new JTSOpRunner.OpParams();
    
    String argA = commandLine.getOptionArg(CommandOptions.GEOMA, 0);
    if (argA != null) {
      if (isFilename(argA)) {
        cmdArgs.fileA = argA;
      }
      else {
        cmdArgs.geomA = argA;
      }
    }
    String argB = commandLine.getOptionArg(CommandOptions.GEOMB, 0);
    if (argB != null) {
      if (isFilename(argB)) {
        cmdArgs.fileB = argB;
      }
      else {
        cmdArgs.geomB = argB;
      }
    }
    String argAB = commandLine.getOptionArg(CommandOptions.GEOMAB, 0);
    if (argAB != null) {
      cmdArgs.isGeomAB = true;
      if (isFilename(argAB)) {
        cmdArgs.fileA = argAB;
      }
      else {
        cmdArgs.geomA = argAB;
      }
    }
    
    cmdArgs.isCollect = commandLine.hasOption(CommandOptions.COLLECT);
    cmdArgs.isExplode = commandLine.hasOption(CommandOptions.EXPLODE);
    
    int paramLimit = commandLine.hasOption(CommandOptions.LIMIT)
        ? commandLine.getOptionArgAsInt(CommandOptions.LIMIT, 0)
            : -1; 
    
    int paramOffset = commandLine.hasOption(CommandOptions.OFFSET)
        ? commandLine.getOptionArgAsInt(CommandOptions.OFFSET, 0)
            : 0; 
        
    cmdArgs.format = commandLine.getOptionArg(CommandOptions.FORMAT, 0);
    
    cmdArgs.srid = commandLine.hasOption(CommandOptions.SRID)
        ? commandLine.getOptionArgAsInt(CommandOptions.SRID, 0)
            : -1;
    
    cmdArgs.isIndexed = commandLine.hasOption(CommandOptions.INDEX);
    
    cmdArgs.repeat = commandLine.hasOption(CommandOptions.REPEAT)
        ? commandLine.getOptionArgAsInt(CommandOptions.REPEAT, 0)
            : 1;
    cmdArgs.validate  = commandLine.hasOption(CommandOptions.VALIDATE);
    cmdArgs.isSelect  = commandLine.hasOption(CommandOptions.WHERE);
    cmdArgs.selectVal =  cmdArgs.isSelect ?
        commandLine.getOptionArgAsNum(CommandOptions.WHERE, 0)
        : 1;
     

    cmdArgs.eachA = commandLine.hasOption(CommandOptions.EACHA);
    cmdArgs.eachB = commandLine.hasOption(CommandOptions.EACHB);
    
    boolean isVerbose = commandLine.hasOption(CommandOptions.VERBOSE)
        || commandLine.hasOption(CommandOptions.V);
    opRunner.setVerbose(isVerbose);
    opRunner.setTime(commandLine.hasOption(CommandOptions.TIME));
    
    isHelpWithFunctions = commandLine.hasOption(CommandOptions.HELP);

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
    
    String[] freeArgs = commandLine.getOptionArgs(OptionSpec.OPTION_FREE_ARGS);
    if (freeArgs != null) {
      if (freeArgs.length >= 1) {
        cmdArgs.operation = freeArgs[0];
      }
      if (freeArgs.length >= 2) {
        cmdArgs.argList = parseOpArg(freeArgs[1]);
      }
    }
    
    /** 
     * ======  Apply extra parameter logic
     */
    //--- apply limit to A if no B, or else to B
    // This allows applying a binary op with a fixed LHS to a limited set of RHS geoms
    if (OpParams.isGeometryInput(cmdArgs.fileB, cmdArgs.geomB)) {
      cmdArgs.limitB = paramLimit;
      cmdArgs.offsetB = paramOffset;
    }
    else {
      cmdArgs.limitA = paramLimit;
      cmdArgs.offsetA = paramOffset;
    }
    
    return cmdArgs;
  }
  
  private String[] parseOpArg(String arg) {
    if (isArgMultiValues(arg)) {
      return parseValues(arg);
    }
    
    // no other macros, for now
    if (arg.contains("(")) 
        throw new CommandError(ERR_INVALID_ARG_PARAM, arg); 
    
    // default is a single arg value
    return new String[] { arg };
  }

  /**
   * Detects various syntaxes for values:
   * - (1,2,3)
   * - val(1,2,3)
   * - 1,2,3
   * 
   * @param arg
   * @return
   */
  private boolean isArgMultiValues(String arg) {
    
    if (arg.startsWith("(")) return true;
    if (arg.startsWith(MACRO_VAL + "(")) return true;
    
    boolean hasParen = arg.indexOf('(') >= 0;
    boolean hasComma = arg.indexOf(',') >= 0;
    
    if (hasComma && ! hasParen) return true;
    
    return false;
  }

  private String[] parseValues(String valuesExpr) {
    boolean hasParenL = valuesExpr.indexOf('(') >= 0;
    boolean hasParenR = valuesExpr.indexOf(')') >= 0;
    boolean hasParen = hasParenL || hasParenR;

    if (hasParen) {
      return parseMacroArgs(valuesExpr);
    }
    // assume expr is an arg list
    return valuesExpr.split(",");
  }

  private String[] parseMacroArgs(String macroTerm) {
    int indexLeft = macroTerm.indexOf('(');
    int indexRight = macroTerm.indexOf(')');
    
    // check for missing L or R parent
    if (indexLeft < 0 || indexRight <= 0) 
      throw new CommandError(ERR_INVALID_ARG_PARAM, macroTerm);  
    
    // TODO: error if no R paren
    String args = macroTerm.substring(indexLeft + 1, indexRight);
    return args.split(",");
  }
}
