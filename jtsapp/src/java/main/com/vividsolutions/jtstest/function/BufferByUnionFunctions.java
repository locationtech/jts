/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryCollectionIterator;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.noding.SegmentString;
import com.vividsolutions.jts.operation.buffer.BufferInputLineSimplifier;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.operation.buffer.OffsetCurveSetBuilder;
import com.vividsolutions.jts.operation.buffer.validate.BufferResultValidator;

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
  
  public static Geometry bufferByChains(Geometry g, double distance, int maxChainSize)
  {
    if (maxChainSize <= 0)
      throw new IllegalArgumentException("Maximum Chain Size must be specified as an input parameter");
    Geometry segs = LineHandlingFunctions.extractChains(g, maxChainSize);
    double posDist = Math.abs(distance);
    Geometry segBuf = bufferByComponents(segs, posDist);
    if (distance < 0.0) 
      return g.difference(segBuf);
    return g.union(segBuf);
  }
}
