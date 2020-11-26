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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.geomfunction.GeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunctionRegistry;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.util.io.MultiFormatBufferedReader;
import org.locationtech.jtstest.util.io.MultiFormatFileReader;
import org.locationtech.jtstest.util.io.MultiFormatReader;

/**
 * Runs an operation according to suppliec parameters.
 * 
 * @author Martin Davis
 *
 */
public class JTSOpRunner {

  // TODO: add option -ab to read both geoms from a file
  // TODO: allow -a stdin  to indicate reading from stdin.  
  
  public static final String ERR_FILE_NOT_FOUND = "File not found";
  public static final String ERR_INPUT = "Unable to read input";
  public static final String ERR_PARSE_GEOM = "Unable to parse geometry";
  public static final String ERR_FUNCTION_NOT_FOUND = "Function not found";
  public static final String ERR_REQUIRED_A = "Geometry A may be required";
  public static final String ERR_REQUIRED_B = "Geometry B is required";
  public static final String ERR_WRONG_ARG_COUNT = "Function arguments and parameters do not match";
  public static final String ERR_FUNCTION_ERR = "Error executing function";
  public static final String ERR_INVALID_RESULT = "Result is invalid";
  
  private static final String SYM_A = "A";
  private static final String SYM_B = "B";

  private GeometryFactory geomFactory = new GeometryFactory();
  
  private GeometryFunctionRegistry funcRegistry;

  private boolean isVerbose = false;

  private InputStream stdIn = System.in;
  private boolean captureGeometry = false;
  private List<Geometry> resultGeoms = new ArrayList<Geometry>();
  
  private CommandOutput out = new CommandOutput();
  private GeometryOutput geomOut = new GeometryOutput(out);
  private String symGeom2 = SYM_B;

  private IndexedGeometry geomIndexB;
  private Geometry geomA;
  private Geometry geomB;
  private OpParams param;
  private String hdrSave;
  private long totalTime;
  private int opCount = 0;
  private boolean isTime;
  
  static class OpParams {
    static final int OFFSET_DEFAULT = 0;
    static final int LIMIT_DEFAULT = -1;
    
    public String fileA;
    String geomA;
    public int limitA = LIMIT_DEFAULT;
    public int offsetA = OFFSET_DEFAULT;
    
    public String fileB;
    public String geomB;
    public int limitB = LIMIT_DEFAULT;
    public int offsetB = OFFSET_DEFAULT;
    
    public boolean isGeomAB = false;
    String format = null;
    public Integer repeat;
    public boolean eachA = false;
    public boolean eachB = false;
    public boolean eachAA = false;
    public boolean validate = false;
    public boolean isIndexed = false;
    public boolean isExplode = false;
    public int srid;
    
    String operation;
    public String[] argList;
    
    /**
     * Tests whether an input geometry has been supplied.
     * 
     * @param file
     * @param geom
     * @return true if an input geometry is present
     */
    static boolean isGeometryInput(String file, String geom) {
      return file != null || geom != null;
    }
  }

  public JTSOpRunner() {
    
  }
  
  public void setRegistry(GeometryFunctionRegistry funcRegistry) {
    this.funcRegistry = funcRegistry;
  }
  
  public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
  }
  public void setTime(boolean isTime) {
    this.isTime = isTime;
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
  void execute(OpParams param) {
    this.param = param;
    
    geomFactory = createGeometryFactory(param.srid);
    geomA = null;
    geomB = null;

    loadGeometry();
    if (geomA != null) {
      printGeometrySummary("A", geomA, fileInfo(param.fileA, param.limitA, param.offsetA) );
    }
    if (geomB != null) {
      printGeometrySummary("B", geomB, fileInfo(param.fileB, param.limitB, param.offsetB) );
    }
    
    //--- If -each aa specified, use A for B
    if (param.eachAA) {
      geomB = geomA;
      symGeom2  = SYM_A;
    }

    // index B if requested
    if (param.eachB) {
      geomIndexB = new IndexedGeometry(geomB, param.isIndexed);
    }
    
    if (param.operation != null) {
      executeFunction();
    }
    else {
      // no op specified, so just output A (allows format conversion)
      outputResult(geomA, param.isExplode, param.format);
    }
  }

  private GeometryFactory createGeometryFactory(int srid) {
    if (srid > 0) {
      return new GeometryFactory(new PrecisionModel(), srid);
    }
    return new GeometryFactory();
  }

  private void loadGeometry() {
    if (param.isGeomAB) {
      //--- limiting is not used for AB reading
      loadGeometryAB();
    }
    else {
      geomA = readGeometry("A", param.fileA, param.geomA, param.limitA, param.offsetA);
      geomB = readGeometry("B", param.fileB, param.geomB, param.limitB, param.offsetB);
    }
  }

  private void loadGeometryAB() {
    Geometry geomAB = readGeometry("AB", param.fileA, param.geomA, OpParams.LIMIT_DEFAULT, OpParams.OFFSET_DEFAULT);
    if (geomAB.getNumGeometries() < 2) {
      throw new CommandError(ERR_REQUIRED_B);
    }
    geomA = geomAB.getGeometryN(0);
    geomB = geomAB.getGeometryN(1);
  }

  private void executeFunction() {
    GeometryFunction func = getFunction(param.operation);
    
    if (func == null) {
      throw new CommandError(ERR_FUNCTION_NOT_FOUND, param.operation);
    }
    String[] argList = param.argList;
    checkFunctionArgs(func, geomB, argList);

    FunctionInvoker fun = new FunctionInvoker(func, argList);
    executeFunctionSpreadA(fun);
    
    if (isVerbose || isTime) {
      out.println("\nOperations: " + opCount
        + "  Total Time: " + Stopwatch.getTimeString( totalTime ));
    }
  }
  
  private void executeFunctionSpreadA(FunctionInvoker fun) {
    int numGeom = 1;
    if (geomA != null) {
      numGeom = geomA.getNumGeometries(); 
    }
    String header = "\n";
    boolean isSpread = param.eachA && numGeom > 1;
    if (! isSpread) {
      executeFunctionSpreadB(geomA, fun, header);
      return;
    }
    
    // spread over A
    for (int i = 0; i < numGeom; i++) {
      Geometry comp = geomA.getGeometryN(i);
      String hdr =  "\n" + GeometryOutput.writeGeometrySummary(SYM_A + "[" + i + "]", comp);
      executeFunctionSpreadB(comp, fun, hdr);
    }
  }
  
  private void executeFunctionSpreadB(Geometry geomA, FunctionInvoker fun, String header) {
    int numGeom = 1;
    if ( fun.isBinaryGeom() && geomB != null ) {
      numGeom = geomB.getNumGeometries();
    }
    boolean isSpread = geomB != null 
        && (param.eachB || param.eachAA )
        && numGeom > 1;
        
    if (! isSpread) {
      fun.setB(geomB);
      executeFunctionArgs(geomA, fun, header);
      return;
    }
    
    // spread over B
    List<Integer> targetB = geomIndexB.query(geomA);
    for (int index : targetB) {
      Geometry comp = geomB.getGeometryN(index);
      String hdr = header + "\n" + GeometryOutput.writeGeometrySummary(symGeom2 + "[" + index + "]", comp);
      fun.setB(comp);
      executeFunctionArgs(geomA, fun, hdr);
    }
  }
  
  private void executeFunctionArgs(Geometry geomA, FunctionInvoker fun, String hdr) {
    // Set saved hdr to blank in case verbose is on
    hdrSave = "";
    printlnInfo(hdr);
    for (int i = 0; i < fun.getNumInvocations(); i++) {
      Object funArgs[] = fun.getArgs(i);
      GeometryFunction func = fun.getFunction();
      String arg = fun.getValue(i);
      
      String opDesc = "-- " + opSummary(func, arg) + "  ------------------------";
      if (isVerbose) {
        out.println(opDesc);
      }
      else {
        hdrSave = hdr + "\n" + opDesc;
      }
      executeFunctionRepeat(geomA, func, funArgs);
    }
  }
  
  private Object executeFunctionRepeat(Geometry geomA, GeometryFunction func, Object[] funArgs) {
    Object result = null;
    for (int i = 0; i < param.repeat; i++) {
      if (param.repeat > 1) {
        printlnInfo("Run: " + (i+1) + " of " + param.repeat + "   ");
      }
      result = executeFunctionOnce(geomA, func, funArgs);
    }
    return result;
  }
  
  private Object executeFunctionOnce(Geometry geomA, GeometryFunction func, Object[] funArgs) {
    Stopwatch timer = new Stopwatch();
    Object result = null;
    try {
      result = func.invoke(geomA, funArgs);
    } 
    catch (NullPointerException ex) {
      if (geomA == null)
        throw new CommandError(ERR_REQUIRED_A, param.operation); 
      // if A is present then must be something else
      logError( errorMsg(ex) );
    }
    catch (Exception ex) {
      logError( errorMsg(ex) );
    }
    finally {
      timer.stop();
    }
    totalTime += timer.getTime();
    printlnInfo("Time: " + timer.getTimeString());
    opCount++;
    
    if (result instanceof Geometry) {
      printGeometrySummary("Result", (Geometry) result, null);
    }
    if (param.validate) {
      validate(result);
    }
    outputResult(result, param.isExplode, param.format);
    return result;
  }

  private String errorMsg(Throwable ex) {
    String msg = "ERROR excuting function: " + ex.getMessage() + "\n";
    msg += toStrackString(ex);
    return msg;
  }

  private String toStrackString(Throwable ex) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    String stack = sw.toString();
    return stack;
  }
  
  private void logError(String msg) {
    // this will be blank if already printed in verbose mode
    out.println(hdrSave);
    out.println(msg);
  }

  private void validate(Object result) {
    if (! ( result instanceof Geometry)) return;
    Geometry resGeom = (Geometry) result;
    
    // TODO: print invalidity reason
    if (! resGeom.isValid()) {
      logError("Result is invalid");
    }
  }
  
  /**
   * Reads a geometry from a literal or a filename.
   * If neither are provided this geometry is not present.
   * 
   * @param geomLabel label for geometry being read
   * @param filename the filename to read from, if present, or <code>null</code>
   * @param geom the geometry literal, if present, or <code>null</code>
   * @param geomA2 
   * @return the geometry read, or null
   * @throws Exception
   */
  private Geometry readGeometry(String geomLabel, String filename, String geom, int limit, int offset) {
    String geomDesc = " " + geomLabel + " ";
    if (geom != null) {
      // read a literal from the argument
      MultiFormatReader rdr = new MultiFormatReader(geomFactory);
      try {
        return rdr.read(geom);
      } 
      catch (org.locationtech.jts.io.ParseException ex) {
        throw new CommandError(ERR_PARSE_GEOM + geomDesc + " - " + ex.getMessage());
      }
      catch (Exception e) {
        throw new CommandError(ERR_PARSE_GEOM + geomDesc, limitLength(geom, 50));
      }
    }
    // no parameter supplied
    if (filename == null) return null;
    
    // must be a filename
    if (filename.equalsIgnoreCase(CommandOptions.STDIN)){
      return readStdin();     
    }
    
    try {
      return MultiFormatFileReader.readFile(filename, limit, offset, geomFactory );
    }
    catch (FileNotFoundException ex) {
      throw new CommandError(ERR_FILE_NOT_FOUND, filename);
    } catch (Exception e) {
      throw new CommandError(ERR_PARSE_GEOM + geomDesc, filename);
    }
  }

  private Geometry readStdin() {
    try {
      MultiFormatBufferedReader rdr = new MultiFormatBufferedReader(geomFactory);
      return rdr.read(new InputStreamReader(stdIn));
    }
    catch (org.locationtech.jts.io.ParseException ex) {
      throw new CommandError(ERR_PARSE_GEOM + " - " + ex.getMessage());
    }
    catch (Exception ex) {
      throw new CommandError(ERR_INPUT);
    }
  }

  private static String limitLength(String s, int n) {
    if (s.length() <= n) return s;
    return s.substring(0, n) + "...";
  }
  
  private static String opSummary(GeometryFunction func, String arg) {
    StringBuilder sb = new StringBuilder();
    sb.append("Op: " + func.getCategory() + "." + func.getName() );
    if (arg != null) {
      sb.append(" " + arg);
    }
    return sb.toString();
  }

  private void outputResult(Object result, boolean isExplode, String outputFormat) {
    if (result == null) return;
    if (outputFormat == null) return;
    
    if (! (result instanceof Geometry)) {
      out.println(result);
      return;
    }
    Geometry geom = (Geometry) result;
    if (isExplode && geom instanceof GeometryCollection) {
      for (int i = 0; i < geom.getNumGeometries(); i++) {
        printGeometry(geom.getGeometryN(i), param.srid, outputFormat);
      }
    }
    else {
      printGeometry(geom, param.srid, outputFormat);
    }
  }
  
  private void printGeometry(Geometry geom, int srid, String outputFormat) {
    if (geom == null) return;
    if (outputFormat == null) return;
    
    if (captureGeometry) {
      resultGeoms.add((Geometry) geom);
    }
    geomOut.printGeometry((Geometry) geom, srid, outputFormat);
  }
  
  private void printlnInfo(String s) {
    if (! isVerbose) return;
    out.println(s);
  }
  
  private void printGeometrySummary(String label, Geometry geom, String source) {
    // short-circuit to avoid cost
    if (! isVerbose) return;
    
    String srcname = "";
    if (source != null) srcname = " -- " + source;
    printlnInfo( GeometryOutput.writeGeometrySummary(label, geom) + srcname);
  }
  
  private static String fileInfo(String filename, int limit, int offset) {
    if (filename == null) return null;
    String info = filename;
    if (limit > OpParams.LIMIT_DEFAULT) info += " LIMIT " + limit;
    if (offset > OpParams.OFFSET_DEFAULT) info += " OFFSET " + offset;
    return info;
  }
  
  private void checkFunctionArgs(GeometryFunction func, Geometry geomB, String[] argList) {
    Class[] paramTypes = func.getParameterTypes();
    int nParam = paramTypes.length;
    
    /*
    // disable this check for now, since it does not handle functions where B is optional
    if (func.isBinary() && geomB == null)
      throw new CommandError(ERR_REQUIRED_B);
     */
    
    /*
     * check count of supplied args.
     * Assumes B has been checked.
     */
    int argCount = 0;
    if (func.isBinary()
      // disable B check for now
      //  && geomB != null
        ) {
      argCount++;
    }
    if (argList != null) argCount++;
    if (nParam != argCount) {
      throw new CommandError(ERR_WRONG_ARG_COUNT, func.getName());
    }
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

  public static boolean isCustomSRID(int srid) {
    return srid > 0;
  }

}

class FunctionInvoker {
  private GeometryFunction func;
  private Geometry b;
  private String[] args;

  public FunctionInvoker(GeometryFunction fun, String[] args) {
    this.func = fun;
    this.args = args;
  }
  
  public void setB(Geometry geom) {
    this.b = geom;
  }

  public boolean isBinaryGeom() {
    return func.isBinary();
  }

  public GeometryFunction getFunction() {
    return func;
  }

  public int getNumInvocations() {
    if (args == null) return 1;
    return args.length;
  }
  
  public String getValue(int i) {
    if (args == null) {
      return null;
    }
    return args[i];
  }
  public Object[] getArgs(int i) {
    String arg = args == null ? null : args[i];
    return createFunctionArgs(func, b, arg);
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
}
class IndexedGeometry
{
  private SpatialIndex index = null;
  private List<Integer> allIndexes = null;
  
  public IndexedGeometry(Geometry geom, boolean isIndexed)
  {
    if (isIndexed) {
      initIndex(geom);
    }
    else {
      initList(geom);
    }
  }
  
  private void initList(Geometry geom) {
    allIndexes = new ArrayList<Integer>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      allIndexes.add(i);
    }
  }

  private void initIndex(Geometry geom)
  {
    index = new STRtree();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Geometry comp = geom.getGeometryN(i);
      index.insert(comp.getEnvelopeInternal(), new Integer(i));
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<Integer> query(Geometry geom)
  {
    if (index != null) {
      List<Integer> vals = index.query(geom.getEnvelopeInternal());
      // sort indices in ascending order for readability
      Collections.sort(vals);
      return vals;
    }
    return allIndexes;
  }
}
