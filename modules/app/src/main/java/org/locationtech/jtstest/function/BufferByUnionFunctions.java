/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jtstest.geomfunction.Metadata;


public class BufferByUnionFunctions {
	
	public static Geometry componentBuffers(Geometry g, double distance)	
	{		
		List bufs = new ArrayList();
		for (Iterator it = new GeometryCollectionIterator(g); it.hasNext(); ) {
			Geometry comp = (Geometry) it.next();
			if (comp instanceof GeometryCollection) continue;
			bufs.add(comp.buffer(distance));
		}
    return FunctionsUtil.getFactoryOrDefault(g)
    				.createGeometryCollection(GeometryFactory.toGeometryArray(bufs));
	}
	
	public static Geometry bufferByComponents(Geometry g, double distance)	
	{
		return componentBuffers(g, distance).union();
	}
	
	/**
	 * Buffer polygons by buffering the individual boundary segments and
	 * either unioning or differencing them.
	 * 
	 * @param g
	 * @param distance
	 * @return the buffer geometry
	 */
  public static Geometry bufferBySegments(Geometry g, double distance)
  {
    Geometry segs = LineHandlingFunctions.extractSegments(g);
    double posDist = Math.abs(distance);
    Geometry segBuf = bufferByComponents(segs, posDist);
    if (distance < 0.0) 
      return g.difference(segBuf);
    return g.union(segBuf);
  }
  
  public static Geometry bufferBySections(Geometry g, double distance,
      @Metadata(title="Section Size")
      int maxChainSize)
  {
    if (maxChainSize <= 0)
      throw new IllegalArgumentException("Section Size must be specified as an input parameter");
    Geometry segs = LineHandlingFunctions.extractChains(g, maxChainSize);
    double posDist = Math.abs(distance);
    Geometry segBuf = bufferByComponents(segs, posDist);
    if (distance < 0.0) 
      return OverlayNGRobust.overlay(g, segBuf, OverlayNG.DIFFERENCE);
    return OverlayNGRobust.overlay(g, segBuf, OverlayNG.UNION);
  }
  
  public static Geometry sectionBuffers(Geometry g, double distance,
      @Metadata(title="Section Size")
      int maxChainSize)
  {
    if (maxChainSize <= 0)
      throw new IllegalArgumentException("Section Size must be specified as an input parameter");
    Geometry segs = LineHandlingFunctions.extractChains(g, maxChainSize);
    double posDist = Math.abs(distance);
    Geometry segBuf = componentBuffers(segs, posDist);
    return segBuf;
  }
}
