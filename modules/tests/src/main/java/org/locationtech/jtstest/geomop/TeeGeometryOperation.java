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
