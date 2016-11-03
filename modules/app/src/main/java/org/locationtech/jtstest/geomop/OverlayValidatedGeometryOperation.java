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
package org.locationtech.jtstest.geomop;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlay.validate.OverlayResultValidator;
import org.locationtech.jtstest.testrunner.*;


/**
 * A {@link GeometryOperation} which validates the result of overlay operations.
 * If an invalid result is found, an exception is thrown (this is the most
 * convenient and noticeable way of flagging the problem when using the TestRunner).
 * All other Geometry methods are executed normally.
 * <p>
 * In order to eliminate the need to specify the precise result of an overlay, 
 * this class forces the final return value to be <tt>GEOMETRYCOLLECTION EMPTY</tt>.
 * <p>
 * This class can be used via the <tt>-geomop</tt> command-line option
 * or by the <tt>&lt;geometryOperation&gt;</tt> XML test file setting.
 * 
 * @author Martin Davis
 *
 */
public class OverlayValidatedGeometryOperation 
	implements GeometryOperation
{
  public static int overlayOpCode(String methodName)
  {
    if (methodName.equals("intersection")) return OverlayOp.INTERSECTION;
    if (methodName.equals("union")) return OverlayOp.UNION;
    if (methodName.equals("difference")) return OverlayOp.DIFFERENCE;
    if (methodName.equals("symDifference")) return OverlayOp.SYMDIFFERENCE;
    return -1;
  }

  private boolean returnEmptyGC = true;
  
  private GeometryMethodOperation chainOp = new GeometryMethodOperation();

  public OverlayValidatedGeometryOperation()
  {
  	
  }
  
  public Class getReturnType(String opName)
  {
  	return chainOp.getReturnType(opName);
  }

  /**
   * Creates a new operation which chains to the given {@link GeometryMethodOperation}
   * for non-intercepted methods.
   * 
   * @param chainOp the operation to chain to
   */
  public OverlayValidatedGeometryOperation(GeometryMethodOperation chainOp)
  {
  	this.chainOp = chainOp;
  }
  
  /**
   * Invokes the named operation
   * 
   * @param opName
   * @param geometry
   * @param args
   * @return the result
   * @throws Exception
   * @see GeometryOperation#invoke
   */
	public Result invoke(String opName, Geometry geometry, Object[] args)
	  throws Exception
	{
	  int opCode = overlayOpCode(opName);
	  
	  // if not an overlay op, do the default
	  if (opCode < 0) {
	    return chainOp.invoke(opName, geometry, args);
	  } 
	  return invokeValidatedOverlayOp(opCode, geometry, args);    
	}

  /**
   * Invokes an overlay op, optionally using snapping,
   * and optionally validating the result.
   * 
   * @param opCode
   * @param g0
   * @param args
   * @return the result
   * @throws Exception
   */
  public Result invokeValidatedOverlayOp(int opCode, Geometry g0, Object[] args)
  	throws Exception
  {
	  Geometry result = null;
	  Geometry g1 = (Geometry) args[0];

	  result = invokeGeometryOverlayMethod(opCode, g0, g1);
	  
    // validate
	  validate(opCode, g0, g1, result);
    areaValidate(g0, g1);
    
    /**
     * Return an empty GeometryCollection as the result.  
     * This allows the test case to avoid specifying an exact result
     */
    if (returnEmptyGC) {
    	result = result.getFactory().createGeometryCollection(null);
    }
    
    return new GeometryResult(result);
  }
  
  private void validate(int opCode, Geometry g0, Geometry g1, Geometry result)
  {
	  OverlayResultValidator validator = new OverlayResultValidator(g0, g1, result);
	  // check if computed result is valid
	  if (! validator.isValid(opCode)) {
	  	Coordinate invalidLoc = validator.getInvalidLocation();
	  	String msg = "Operation result is invalid [OverlayResultValidator] ( " + WKTWriter.toPoint(invalidLoc) + " )";
	  	reportError(msg);
	  } 
  }
  
  private static final double AREA_DIFF_TOL = 5.0;
  
  private void areaValidate(Geometry g0, Geometry g1)
  {
  	double areaDiff = areaDiff(g0, g1);
//  	System.out.println("Area diff = " + areaDiff);
	  if (Math.abs(areaDiff) > AREA_DIFF_TOL) {
	  	String msg = "Operation result is invalid [AreaTest] (" + areaDiff + ")";
	  	reportError(msg);
	  } 
  }
  
  public static double areaDiff(Geometry g0, Geometry g1)
  {
  	double areaA = g0.getArea();
  	double areaAdiffB = g0.difference(g1).getArea();
  	double areaAintB = g0.intersection(g1).getArea();
  	return areaA - areaAdiffB - areaAintB;
  }
  
  private void reportError(String msg)
  {
//  	System.out.println(msg);
    throw new RuntimeException(msg);
  }
  
  public static Geometry invokeGeometryOverlayMethod(int opCode, Geometry g0, Geometry g1)
  {
    switch (opCode) {
      case OverlayOp.INTERSECTION: return g0.intersection(g1);
      case OverlayOp.UNION: return g0.union(g1);
      case OverlayOp.DIFFERENCE: return g0.difference(g1);
      case OverlayOp.SYMDIFFERENCE: return g0.symDifference(g1);
    }
    throw new IllegalArgumentException("Unknown overlay op code");
  }


}
