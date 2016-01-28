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

import org.locationtech.jts.geom.Geometry;

import com.vividsolutions.jtstest.testrunner.Result;

/**
 * A {@link GeometryOperation} which logs
 * the input and output from another 
 * {@link GeometryOperation}.
 * The log is sent to {@link System#out}.
 * 
 * @author mbdavis
 *
 */
public class LoggingGeometryOperation 
 implements GeometryOperation
{
	private GeometryOperation geomOp = new GeometryMethodOperation();
	
	public LoggingGeometryOperation()
	{
		
	}
	
  public Class getReturnType(String opName)
  {
  	return GeometryMethodOperation.getGeometryReturnType(opName);
  }
  
	public LoggingGeometryOperation(GeometryOperation geomOp)
	{
		this.geomOp = geomOp;
	}
	
  public Result invoke(String opName, Geometry geometry, Object[] args)
  	throws Exception
  {
  	System.out.println("Operation <" + opName + ">");
  	System.out.println("Geometry: " + geometry);
  	for (int i = 0; i < args.length; i++) {
  		System.out.println("Arg[" + i + "]: " + args[i]);
  	}
  	Result result = geomOp.invoke(opName, geometry, args);
  	System.out.println("Result==> " + result.toFormattedString());
  	return result;
  }
}
