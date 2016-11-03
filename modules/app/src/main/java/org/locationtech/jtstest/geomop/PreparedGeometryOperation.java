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
import org.locationtech.jts.geom.prep.*;
import org.locationtech.jtstest.testrunner.*;


/**
 * A {@link GeometryOperation} which uses {@link PreparedGeometry}s
 * for applicable operations.
 * This allows testing correctness of the <tt>PreparedGeometry</tt> implementation.
 * <p>
 * This class can be used via the <tt>-geomop</tt> command-line option
 * or by the <tt>&lt;geometryOperation&gt;</tt> XML test file setting.
 *
 * @author mbdavis
 *
 */
public class PreparedGeometryOperation 
implements GeometryOperation
{
  private GeometryMethodOperation chainOp = new GeometryMethodOperation();
  
  public PreparedGeometryOperation()
  {
  	
  }
  
  public Class getReturnType(String opName)
  {
  	if (isPreparedOp(opName))
  		return boolean.class;
  	return chainOp.getReturnType(opName);
  }

  /**
   * Creates a new operation which chains to the given {@link GeometryMethodOperation}
   * for non-intercepted methods.
   * 
   * @param chainOp the operation to chain to
   */
  public PreparedGeometryOperation(GeometryMethodOperation chainOp)
  {
  	this.chainOp = chainOp;
  }
  
  private static boolean isPreparedOp(String opName)
  {
  	if (opName.equals("intersects")) return true;
  	if (opName.equals("contains")) return true;
  	if (opName.equals("containsProperly")) return true;
  	if (opName.equals("covers")) return true;
  	return false;
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
	  if (! isPreparedOp(opName)) {
	    return chainOp.invoke(opName, geometry, args);
	  } 
	  return invokePreparedOp(opName, geometry, args);    
	}

	private Result invokePreparedOp(String opName, Geometry geometry, Object[] args)
	{
		Geometry g2 = (Geometry) args[0];
  	if (opName.equals("intersects")) {
  		return new BooleanResult(PreparedGeometryOp.intersects(geometry, g2));
  	}
    if (opName.equals("contains")) {
      return new BooleanResult(PreparedGeometryOp.contains(geometry, g2));
    }
    if (opName.equals("containsProperly")) {
      return new BooleanResult(PreparedGeometryOp.containsProperly(geometry, g2));
    }
    if (opName.equals("covers")) {
      return new BooleanResult(PreparedGeometryOp.covers(geometry, g2));
    }
  	return null;
	}
	
	static class PreparedGeometryOp
	{
		public static boolean intersects(Geometry g1, Geometry g2)
		{
      PreparedGeometry prepGeom = PreparedGeometryFactory.prepare(g1);
	    return prepGeom.intersects(g2);
		}
    public static boolean contains(Geometry g1, Geometry g2)
    {
      PreparedGeometry prepGeom = PreparedGeometryFactory.prepare(g1);
      return prepGeom.contains(g2);
    }
    public static boolean containsProperly(Geometry g1, Geometry g2)
    {
      PreparedGeometry prepGeom = PreparedGeometryFactory.prepare(g1);
      return prepGeom.containsProperly(g2);
    }
    public static boolean covers(Geometry g1, Geometry g2)
    {
      PreparedGeometry prepGeom = PreparedGeometryFactory.prepare(g1);
      return prepGeom.covers(g2);
    }
	}
}
