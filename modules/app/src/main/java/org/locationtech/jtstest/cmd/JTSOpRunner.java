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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.geomfunction.GeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunctionRegistry;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.util.io.IOUtil;
import org.locationtech.jtstest.util.io.MultiFormatBufferedReader;
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
  
  static class OpParams {
    String operation;
    public String fileA;
    String geomA;
    public String fileB;
    public String geomB;
    //public String[] arg1;
    String format = null;
    public Integer repeat;
    public boolean eachA = false;
    public boolean eachB = false;
    public boolean eachAA = false;
    public String[] argList;
    public boolean validate = false;
    public boolean isIndexed;
  }

  public JTSOpRunner() {
    
  }
  
  public void setRegistry(GeometryFunctionRegistry funcRegistry) {
    this.funcRegistry = funcRegistry;
  }
  
  public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
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
  void execute(OpParams cmdArgs) {
    
    Geometry geomA = null;
    Geometry geomB = null;

    geomA = readGeometry(cmdArgs.fileA, cmdArgs.geomA);
    if (geomA != null) {
      if (isVerbose) geomOut.printGeometrySummary("A", geomA, cmdArgs.fileA);
    }
    geomB = readGeometry(cmdArgs.fileB, cmdArgs.geomB);
    if (geomB != null) {
      if (isVerbose) geomOut.printGeometrySummary("B", geomB, cmdArgs.fileB);
    }
    // TODO: Handle option -ab
    
    //--- If -each aa specified, use A for B
    if (cmdArgs.eachAA) {
      geomB = geomA;
      symGeom2  = SYM_A;
    }
    
    // index B if requested
    if (cmdArgs.eachB) {
      geomIndexB = new IndexedGeometry(geomB, cmdArgs.isIndexed);
    }
    
    if (cmdArgs.operation != null) {
      executeFunction(cmdArgs, geomA, geomB);
    }
    else {
      // no op specified, so just output A (allows format conversion)
      printResult(geomA, cmdArgs.format);
    }
  }

  private void executeFunction(OpParams cmdArgs, Geometry geomA, Geometry geomB) {
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

  private void executeFunctionWithArg(OpParams cmdArgs, GeometryFunction func, Geometry geomA, Geometry geomB, String arg) {
    checkFunctionArgs(func, geomB, arg);
    Object funArgs[] = createFunctionArgs(func, geomB, arg);
    String header = "-- " + opSummary(func.getName(), arg) + "  ------------------------";
    executeFunctionSpreadA(cmdArgs, geomA, func, funArgs, header);
  }

  private void executeFunctionSpreadA(OpParams cmdArgs, Geometry geomA, GeometryFunction func, Object[] funArgs, String header) {
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
      String hdr = header + "\n" + GeometryOutput.writeGeometrySummary(SYM_A + "[" + i + "]", comp);
      executeFunctionSpreadB(cmdArgs, comp, func, funArgs, hdr);
    }
  }
  
  private void executeFunctionSpreadB(OpParams cmdArgs, Geometry geomA, GeometryFunction func, Object[] funArgs, String header) {
    Geometry geomB = null;
    int num = 1;
    if (funArgs.length > 0 && funArgs[0] instanceof Geometry) {
      geomB = (Geometry) funArgs[0];
      num = geomB.getNumGeometries();
    }
    boolean isSpread = geomB != null 
        && (cmdArgs.eachB || cmdArgs.eachAA )
        && num > 1;
        
    if (! isSpread) {
      executeFunctionRepeat(cmdArgs, geomA, func, funArgs, header);
      return;
    }
    
    // spread over B
    // copy args array since it will be modified to set B components
    Object[] funArgsB = funArgs.clone();
    List<Integer> targetB = geomIndexB.query(geomA);
    for (int index : targetB) {
      Geometry comp = geomB.getGeometryN(index);
      String hdr = header + "\n" + GeometryOutput.writeGeometrySummary(symGeom2 + "[" + index + "]", comp);
      funArgsB[0] = comp;
      executeFunctionRepeat(cmdArgs, geomA, func, funArgsB, hdr);
    }
  }

  private Object executeFunctionRepeat(OpParams cmdArgs, Geometry geomA, GeometryFunction func, Object[] funArgs, String hdr) {
    if (isVerbose) {
      out.println(hdr);
    }
    Object result = null;
    for (int i = 0; i < cmdArgs.repeat; i++) {
      if (cmdArgs.repeat > 1) {
        out.print("Run: " + (i+1) + " of " + cmdArgs.repeat + "   ");
      }
      result = executeFunctionOnce(cmdArgs, geomA, func, funArgs);
    }
    return result;
  }

  private Object executeFunctionOnce(OpParams cmdArgs, Geometry geomA, GeometryFunction func, Object[] funArgs) {
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
      if (result instanceof Geometry) {
        geomOut.printGeometrySummary("Result", (Geometry) result, null);
      }
    }
    if (cmdArgs.validate) {
      validate(result);
    }
    printResult(result, cmdArgs.format);
    return result;
  }

  private void validate(Object result) {
    if (! ( result instanceof Geometry)) return;
    Geometry resGeom = (Geometry) result;
    
    // TODO: print invalidity reason
    if (! resGeom.isValid()) {
      throw new CommandError(ERR_INVALID_RESULT);
    }
  }

  /**
   * Reads a geometry from a literal or a filename.
   * If neither are provided this geometry is not present.'
   * 
   * @param filename the filename to read from, if present
   * @param geom the geometry literal, if present
   * @return the geometry read, or null
   * @throws Exception
   */
  private Geometry readGeometry(String filename, String geom) {
    if (geom != null) {
      // read a literal from the argument
      MultiFormatReader rdr = new MultiFormatReader(geomFactory);
      try {
        return rdr.read(geom);
      } catch (Exception e) {
        throw new CommandError(ERR_INPUT, filename);
      }
    }
    // no parameter supplied
    if (filename == null) return null;
    
    // must be a filename
    if (filename.equalsIgnoreCase(CommandOptions.STDIN)){
      return readStdin();     
    }
    
    try {
      return IOUtil.readFile(filename ,geomFactory );
    }
    catch (FileNotFoundException ex) {
      throw new CommandError(ERR_FILE_NOT_FOUND, filename);
    } catch (Exception e) {
      throw new CommandError(ERR_INPUT, filename);
    }
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
    if (index != null)
      return index.query(geom.getEnvelopeInternal());
    
    return allIndexes;
  }
}
