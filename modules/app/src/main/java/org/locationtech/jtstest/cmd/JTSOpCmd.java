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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.locationtech.jtstest.geomfunction.SpreaderGeometryFunction;
import org.locationtech.jtstest.testbuilder.geom.GeometryUtil;
import org.locationtech.jtstest.testbuilder.io.SVGTestWriter;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.util.io.IOUtil;
import org.locationtech.jtstest.util.io.MultiFormatBufferedReader;
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
 * --- Compute multiple buffers
 * jtsop -a "POINT (10 10)" -f wkt Buffer.buffer val(1,10,100)
 * 
 * --- Run op for each A 
 * jtsop -a "MULTIPOINT ((10 10), (20 20))" -each A -f wkt Buffer.buffer
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
  
  public static final String ERR_FILE_NOT_FOUND = "File not found";
  public static final String ERR_INVALID_PARAMETER = "Invalid Parameter";
  public static final String ERR_INPUT = "Unable to read input";
  public static final String ERR_FUNCTION_NOT_FOUND = "Function not found";
  public static final String ERR_REQUIRED_A = "Geometry A may be required";
  public static final String ERR_REQUIRED_B = "Geometry B is required";
  public static final String ERR_WRONG_ARG_COUNT = "Function arguments and parameters do not match";
  public static final String ERR_FUNCTION_ERR = "Error executing function";
  public static final String ERR_INVALID_RESULT = "Result is invalid";

  private static final String MACRO_VAL = "val";

  public static void main(String[] args)
  {    
    JTSOpCmd cmd = new JTSOpCmd();
    try {
      CmdArgs cmdArgs = cmd.parseArgs(args);
      cmd.execute(cmdArgs);
    } 
    catch (CommandError e) {
      // for command errors, just print the message
      System.err.println(e.getMessage() );
    }
    catch (Exception e) {
      // unexpected errors get a stack track to help debugging
      e.printStackTrace();
    }
  }

  private void printHelp(boolean showFunctions) {
    for (String s : helpDoc) {
      out.println(s);
    }
    if (showFunctions) {
      out.println();
      out.println("Operations:");
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
        out.println(category + "." + functionDesc(fun));
      }
    }
  }

  private static String functionDesc(GeometryFunction fun) {
    // TODO: display this in a command arg style
    BaseGeometryFunction geomFun = (BaseGeometryFunction) fun;
    return geomFun.getSignature();
    //return geomFun.getName();
  }

  private GeometryFactory geomFactory = new GeometryFactory();
  
  private GeometryFunctionRegistry funcRegistry = GeometryFunctionRegistry.createTestBuilderRegistry();
  private CommandLine commandLine = createCmdLine();

  private boolean isHelp = false;
  private boolean isHelpWithFunctions = false;
  private boolean isVerbose = false;

  private InputStream stdIn = System.in;
  private boolean captureGeometry = false;
  private List<Geometry> resultGeoms = new ArrayList<Geometry>();
  
  private CommandOutput out = new CommandOutput();
  private GeometryOutput geomOut = new GeometryOutput(out);
  
  static class CmdArgs {
    String operation;
    String geomA;
    public String geomB;
    //public String[] arg1;
    String format = null;
    public Integer repeat;
    public boolean eachA = false;
    public boolean eachB = false;
    public String[] argList;
    public boolean validate = false;
  }

  public JTSOpCmd() {
    
  }
  
  public void captureOutput() {
    out = new CommandOutput(true);
    geomOut = new GeometryOutput(out);
  }
  
  public void captureResult() {
    captureGeometry  = true;
  }
  
  public List<Geometry> getResultGeometry() {
    return resultGeoms;
  }
  
  public void replaceStdIn(InputStream inStream) {
    stdIn  = inStream;
  }
  
  public String getOutput() {
    return out.getOutput();
  }
  void execute(CmdArgs cmdArgs) throws IOException, Exception {
    if (isHelp || isHelpWithFunctions) {
      printHelp(isHelpWithFunctions);
      return;
    }
    
    Geometry geomA = null;
    Geometry geomB = null;

    if (cmdArgs.geomA != null) {
      geomA = readGeometry(cmdArgs.geomA);
      if (isVerbose) geomOut.printGeometrySummary("A", geomA, getFilename( cmdArgs.geomA));
    }
    if (cmdArgs.geomB != null) {
      geomB = readGeometry(cmdArgs.geomB);
      if (isVerbose) geomOut.printGeometrySummary("B", geomB, getFilename( cmdArgs.geomB));
    }
    // TODO: Handle option -ab
    
    
    if (cmdArgs.operation != null) {
      executeFunction(cmdArgs, geomA, geomB);
    }
    else {
      // no op specified, so just output A (allows format conversion)
      printResult(geomA, cmdArgs.format);
    }
  }

  private String getFilename(String arg) {
    if (isFilename(arg)) return arg;
    return null;
  }

  private void executeFunction(CmdArgs cmdArgs, Geometry geomA, Geometry geomB) {
    GeometryFunction func = getFunction(cmdArgs.operation);
    //func = decorateFunctionEach(cmdArgs, func);
    
    if (func == null) {
      throw new CommandError(ERR_FUNCTION_NOT_FOUND, cmdArgs.operation);
    }
    
    if (cmdArgs.argList != null) {
      for (String arg : cmdArgs.argList) {
        executeFunctionWithArg(cmdArgs, func, geomA, geomB, arg);
      }
    }
    else {
      executeFunctionWithArg(cmdArgs, func, geomA, geomB, null);
    }
  }

  private void executeFunctionWithArg(CmdArgs cmdArgs, GeometryFunction func, Geometry geomA, Geometry geomB, String arg) {
    checkFunctionArgs(func, geomB, arg);
    Object funArgs[] = createFunctionArgs(func, geomB, arg);
    
    String header = "-- " + opSummary(func.getName(), arg) + "  ------------------------";
    
    for (int i = 0; i < cmdArgs.repeat; i++) {
      if (cmdArgs.repeat > 1) {
        out.print("Run: " + (i+1) + " of " + cmdArgs.repeat + "   ");
      }
      executeFunctionSpreadA(cmdArgs, geomA, func, funArgs, header);
    }
  }

  private void executeFunctionSpreadA(CmdArgs cmdArgs, Geometry geomA, GeometryFunction func, Object[] funArgs, String header) {
    int num = 1;
    if (geomA != null) {
      num = geomA.getNumGeometries(); 
    }
    boolean isSpread = cmdArgs.eachA && num > 1;
    if (! isSpread) {
      executeFunctionSpreadB(cmdArgs, geomA, func, funArgs, header);
      return;
    }
    
    // spread over A
    for (int i = 0; i < num; i++) {
      Geometry comp = geomA.getGeometryN(i);
      String hdr = header + "\n" + GeometryOutput.writeGeometrySummary("A[" + i + "]", comp);
      executeFunctionSpreadB(cmdArgs, comp, func, funArgs, hdr);
    }
  }
  
  private void executeFunctionSpreadB(CmdArgs cmdArgs, Geometry geomA, GeometryFunction func, Object[] funArgs, String header) {
    Geometry geomB = null;
    int num = 1;
    if (funArgs.length > 0 && funArgs[0] instanceof Geometry) {
      geomB = (Geometry) funArgs[0];
      num = geomB.getNumGeometries();
    }
    boolean isSpread = geomB != null && cmdArgs.eachB && num > 1;
    if (! isSpread) {
      executeFunctionOnce(cmdArgs, geomA, func, funArgs);
      return;
    }
    
    // spread over B
    // copy args array since it will be modified to set B components
    Object[] funArgsB = funArgs.clone();
    for (int i = 0; i < num; i++) {
      Geometry comp = geomB.getGeometryN(i);
      if (isVerbose) {
        String hdr = header + "\n" + GeometryOutput.writeGeometrySummary("B[" + i + "]", comp);
        out.println(hdr);
      }
      funArgsB[0] = comp;
      executeFunctionOnce(cmdArgs, geomA, func, funArgsB);
    }
  }

  private Object executeFunctionOnce(CmdArgs cmdArgs, Geometry geomA, GeometryFunction func, Object[] funArgs) {
    Stopwatch timer = new Stopwatch();
    Object result = null;
    try {
      result = func.invoke(geomA, funArgs);
    } 
    catch (NullPointerException ex) {
      if (geomA == null)
        throw new CommandError(ERR_REQUIRED_A, cmdArgs.operation); 
      // if A is present then must be something else
      throw new CommandError(ERR_FUNCTION_ERR, ex.getMessage());
    }
    finally {
      timer.stop();
    }
    if (isVerbose) {
      out.println("Time: " + timer.getTimeString());
    }
    if (cmdArgs.validate && result instanceof Geometry) {
      Geometry resGeom = (Geometry) result;
      if (! resGeom.isValid()) {
        throw new CommandError(ERR_INVALID_RESULT);
      }
    }
    printResult(result, cmdArgs.format);
    return result;
  }

  private Geometry readGeometry(String arg) throws Exception, IOException {
    if (arg.equalsIgnoreCase(CommandOptions.STDIN)){
      return readStdin();     
    }
    else if (isFilename(arg)) {
      try {
        return IOUtil.readFile(arg ,geomFactory );
      }
      catch (FileNotFoundException ex) {
        throw new CommandError(ERR_FILE_NOT_FOUND, arg);
      }
    }
    // read a literal from the argument
    MultiFormatReader rdr = new MultiFormatReader(geomFactory);
    return rdr.read(arg);
  }

  private Geometry readStdin() {
    try {
      MultiFormatBufferedReader rdr = new MultiFormatBufferedReader();
      return rdr.read(new InputStreamReader(stdIn));
    }
    catch (org.locationtech.jts.io.ParseException ex) {
      throw new CommandError(ERR_INPUT + " - " + ex.getMessage());
    }
    catch (Exception ex) {
      throw new CommandError(ERR_INPUT);
    }
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
    // TODO: make this smarter
    boolean hasParen = (arg.indexOf("(") > 0) && arg.indexOf(")") > 0;
    if (hasParen) return true;
    return false;
  }

  private static String opSummary(String funcName, String arg) {
    StringBuilder sb = new StringBuilder();
    sb.append("Op: " + funcName );
    if (arg != null) {
      sb.append(" " + arg);
    }
    return sb.toString();
  }

  private void printResult(Object result, String outputFormat) {
    if (result == null) return;
    if (outputFormat == null) return;
    
    if (result instanceof Geometry) {
      if (captureGeometry) {
        resultGeoms.add((Geometry) result);
      }
      if (isVerbose) {
        geomOut.printGeometrySummary("Result", (Geometry) result, null);
      }
      geomOut.printGeometry((Geometry) result, outputFormat);
    }
    else {
      out.println(result);
    }
  }
  
  private void checkFunctionArgs(GeometryFunction func, Geometry geomB, String arg1) {
    Class[] paramTypes = func.getParameterTypes();
    int nParam = paramTypes.length;
    
    if (func.isBinary() && geomB == null)
      throw new CommandError(ERR_REQUIRED_B);
    /**
    // MD not sure whether to check this?
    if (! func.isBinary() && geomB != null)
      throw new CommandError(ERR_REQUIRED_B);
      */
    
    /*
     * check count of supplied args.
     * Assumes B has been checked.
     */
    int argCount = 0;
    if (func.isBinary() && geomB != null) argCount++;
    if (arg1 != null) argCount++;
    if (nParam != argCount) {
      throw new CommandError(ERR_WRONG_ARG_COUNT, func.getName());
    }
  }
  
  private Object[] createFunctionArgs(GeometryFunction func, Geometry geomB, String arg1) {
    Class[] paramTypes = func.getParameterTypes();
    Object[] paramVal = new Object[paramTypes.length];
    
    int iparam = 0;
    if (func.isBinary()) {
      paramVal[0] = geomB;
      iparam++;
    }
    // just handling one scalar arg for now
    if (iparam < paramVal.length) {
      paramVal[iparam] = SwingUtil.coerce(arg1, paramTypes[iparam]);
    }
    return paramVal;
  }


  private GeometryFunction decorateFunctionEach(CmdArgs cmdArgs, GeometryFunction func) {
    if (! (cmdArgs.eachA || cmdArgs.eachB)) return func;
    return new SpreaderGeometryFunction(func, cmdArgs.eachA, cmdArgs.eachB );
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

  CmdArgs parseArgs(String[] args) throws ParseException, ClassNotFoundException {
    CmdArgs cmdArgs = new CmdArgs();
    
    if (args.length == 0) {
      isHelp = true;
      return cmdArgs;
    }
    
    commandLine.parse(args);

    cmdArgs.operation = commandLine.getOptionArg(CommandOptions.OP, 0);
    cmdArgs.geomA = commandLine.getOptionArg(CommandOptions.GEOMA, 0);
    cmdArgs.geomB = commandLine.getOptionArg(CommandOptions.GEOMB, 0);
    cmdArgs.format = commandLine.getOptionArg(CommandOptions.FORMAT, 0);
    
    cmdArgs.repeat = commandLine.hasOption(CommandOptions.REPEAT)
        ? commandLine.getOptionArgAsInt(CommandOptions.REPEAT, 0)
            : 1;
    cmdArgs.validate  = commandLine.hasOption(CommandOptions.VALIDATE);

    if (commandLine.hasOption(CommandOptions.EACH)) {
      String each = commandLine.getOptionArg(CommandOptions.EACH, 0);

      if (each.equalsIgnoreCase("a")) {
        cmdArgs.eachA = true;
      }
      else if (each.equalsIgnoreCase("b")) {
        cmdArgs.eachB = true;
      }
      else if (each.equalsIgnoreCase("ab")) {
        cmdArgs.eachA = true;
        cmdArgs.eachB = true;
      }
      else {
        throw new CommandError(ERR_INVALID_PARAMETER, "-each " + each);
      }
    }
    isVerbose = commandLine.hasOption(CommandOptions.VERBOSE)
                  || commandLine.hasOption(CommandOptions.V);
    isHelpWithFunctions = commandLine.hasOption(CommandOptions.HELP);

    if (commandLine.hasOption(CommandOptions.GEOMFUNC)) {
      Option opt = commandLine.getOption(CommandOptions.GEOMFUNC);
      for (int i = 0; i < opt.getNumArgs(); i++) {
        String geomFuncClassname = opt.getArg(i);
        try {
          funcRegistry.add(geomFuncClassname);
          out.println("Added Geometry Functions from: " + geomFuncClassname);
        } catch (ClassNotFoundException ex) {
          out.println("Unable to load function class: " + geomFuncClassname);
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
    return cmdArgs;
  }
  
  private String[] parseOpArg(String arg) {
    if (arg.startsWith(MACRO_VAL + "(")) 
      return parseValues(arg);
    // no other macros, for now
    if (arg.contains("(")) 
        throw new CommandError(ERR_INVALID_PARAMETER, arg); 
    return new String[] { arg };
  }

  private String[] parseValues(String arg) {
    int indexLeft = arg.indexOf('(');
    int indexRight = arg.indexOf(')');
    if (indexRight <= 0) 
      throw new CommandError(ERR_INVALID_PARAMETER, arg);  
    // TODO: error if no R paren
    String content = arg.substring(indexLeft + 1, indexRight);
    return content.split(",");
  }

  private CommandLine createCmdLine() {
    commandLine = new CommandLine('-');
    commandLine.addOptionSpec(new OptionSpec(CommandOptions.GEOMFUNC, OptionSpec.NARGS_ONE_OR_MORE))
    .addOptionSpec(new OptionSpec(CommandOptions.VERBOSE, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.V, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.HELP, 0))
    .addOptionSpec(new OptionSpec(CommandOptions.OP, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.GEOMA, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.GEOMB, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.EACH, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.FORMAT, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.REPEAT, 1))
    .addOptionSpec(new OptionSpec(CommandOptions.VALIDATE, 0))
    .addOptionSpec(new OptionSpec(OptionSpec.OPTION_FREE_ARGS, OptionSpec.NARGS_ONE_OR_MORE));
    return commandLine;
  }

  static final String[] helpDoc = new String[] {
  "",
  "Usage: jtsop - CLI for JTS operations",
  "           [ -a <wkt> | <wkb> | stdin | <filename.ext> ]",
  "           [ -b <wkt> | <wkb> | stdin | <filename.ext> ]",
  "           [ -each ( a | b | ab ) ]",
  "           [ -f ( txt | wkt | wkb | geojson | gml | svg ) ]",
  "           [ -repeat <num> ]",
  "           [ -validate ]",
  "           [ -geomfunc <classname> ]",
  "           [ -v, -verbose ]",
  "           [ -help]",
  "           [ op [ args... ]]",
  "  op              name of the operation (Category.op)",
  "  args            one or more scalar arguments to the operation",
  "                  - Use val(v1,v2,v3,..) for multiple arguments",
  "",
  "  -a              Geometry A: literal, stdin (WKT or WKB), or filename (extension: WKT, WKB, GeoJSON, GML, SHP)",
  "  -b              Geometry A: literal, stdin (WKT or WKB), or filename (extension: WKT, WKB, GeoJSON, GML, SHP)",
  "  -each           execute op on each component of A and/or B",
  "  -f              output format to use.  If omitted output is silent",
  "  -repeat         repeat the operation N times",
  "  -validate       validate the result of each operation",
  "  -geomfunc       specifies class providing geometry operations",
  "  -v, -verbose    display information about execution",
  "  -help           print a list of available operations"
  };

}
