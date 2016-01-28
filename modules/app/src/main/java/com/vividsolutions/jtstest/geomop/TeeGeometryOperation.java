/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.geomop;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.*;

import com.vividsolutions.jtstest.testrunner.*;

/**
 * A {@link GeometryOperation} which executes the original operation 
 * and returns that result,
 * but also executes a separate operation (which could be multiple operations).
 * The side operations can throw exceptions if they do not compute 
 * correct results.  This relies on the availability of 
 * another reliable implementation to provide the expected result.
 * <p>
 * This class can be used via the <tt>-geomop</tt> command-line option
 * or by the <tt>&lt;geometryOperation&gt;</tt> XML test file setting.
 *
 * @author mbdavis
 *
 */
public abstract class TeeGeometryOperation 
implements GeometryOperation
{
  private GeometryMethodOperation chainOp = new GeometryMethodOperation();
  
  public TeeGeometryOperation()
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
  public TeeGeometryOperation(GeometryMethodOperation chainOp)
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
	  runTeeOp(opName, geometry, args);  
	  
	  return chainOp.invoke(opName, geometry, args);
	}

	protected abstract void runTeeOp(String opName, Geometry geometry, Object[] args);

	
}
