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
package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.geom.util.GeometryMapper.MapOp;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.operation.buffer.BufferInputLineSimplifier;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurveBuilder;
import org.locationtech.jts.operation.buffer.OffsetCurveSetBuilder;
import org.locationtech.jts.operation.buffer.validate.BufferResultValidator;


public class BufferFunctions {
	
	public static String bufferDescription = "Buffers a geometry by a distance";
	
	public static Geometry buffer(Geometry g, double distance)		{		return g.buffer(distance);	}
	
	public static Geometry bufferWithParams(Geometry g, Double distance, 
			Integer quadrantSegments, Integer capStyle, Integer joinStyle, Double mitreLimit)	
	{
	    double dist = 0;
	    if (distance != null) dist = distance.doubleValue();
	    
	    BufferParameters bufParams = new BufferParameters();
	    if (quadrantSegments != null)	bufParams.setQuadrantSegments(quadrantSegments.intValue());
	    if (capStyle != null)	bufParams.setEndCapStyle(capStyle.intValue());
	    if (joinStyle != null) 	bufParams.setJoinStyle(joinStyle.intValue());
	    if (mitreLimit != null) 	bufParams.setMitreLimit(mitreLimit.doubleValue());
	    
	    return BufferOp.bufferOp(g, dist, bufParams);
	}
	
	public static Geometry bufferWithSimplify(Geometry g, Double distance, 
			Double simplifyFactor)	
	{
	    double dist = 0;
	    if (distance != null) dist = distance.doubleValue();
	    
	    BufferParameters bufParams = new BufferParameters();
	    if (simplifyFactor != null)	bufParams.setSimplifyFactor(simplifyFactor.doubleValue());
	    
	    return BufferOp.bufferOp(g, dist, bufParams);
	}
	
	public static Geometry bufferCurve(Geometry g, double distance)	
	{		
    return buildCurveSet(g, distance, new BufferParameters());
	}
	
	public static Geometry bufferCurveWithParams(Geometry g, Double distance, 
			Integer quadrantSegments, Integer capStyle, Integer joinStyle, Double mitreLimit)	
	{
    double dist = 0;
    if (distance != null) dist = distance.doubleValue();
    
    BufferParameters bufParams = new BufferParameters();
    if (quadrantSegments != null)	bufParams.setQuadrantSegments(quadrantSegments.intValue());
    if (capStyle != null)	bufParams.setEndCapStyle(capStyle.intValue());
    if (joinStyle != null) 	bufParams.setJoinStyle(joinStyle.intValue());
    if (mitreLimit != null) 	bufParams.setMitreLimit(mitreLimit.doubleValue());
    
    return buildCurveSet(g, dist, bufParams);
	}
	
  private static Geometry buildCurveSet(Geometry g, double dist, BufferParameters bufParams)
  {
    // --- now construct curve
    OffsetCurveBuilder ocb = new OffsetCurveBuilder(
        g.getFactory().getPrecisionModel(),
        bufParams);
    OffsetCurveSetBuilder ocsb = new OffsetCurveSetBuilder(g, dist, ocb);
    List curves = ocsb.getCurves();
    
    List lines = new ArrayList();
    for (Iterator i = curves.iterator(); i.hasNext(); ) {
    	SegmentString ss = (SegmentString) i.next();
    	Coordinate[] pts = ss.getCoordinates();
    	lines.add(g.getFactory().createLineString(pts));
    }
    Geometry curve = g.getFactory().buildGeometry(lines);
    return curve;
  }

	public static Geometry bufferLineSimplifier(Geometry g, double distance)	
	{   
    return buildBufferLineSimplifiedSet(g, distance);
	}

  private static Geometry buildBufferLineSimplifiedSet(Geometry g, double distance)
  {
    List simpLines = new ArrayList();

    List lines = new ArrayList();
    LinearComponentExtracter.getLines(g, lines);
    for (Iterator i = lines.iterator(); i.hasNext(); ) {
    	LineString line = (LineString) i.next();
    	Coordinate[] pts = line.getCoordinates();
    	simpLines.add(g.getFactory().createLineString(BufferInputLineSimplifier.simplify(pts, distance)));
    }
    Geometry simpGeom = g.getFactory().buildGeometry(simpLines);
    return simpGeom;
  }

  public static Geometry bufferValidated(Geometry g, double distance)
  {
    Geometry buf = g.buffer(distance);
    String errMsg = BufferResultValidator.isValidMsg(g, distance, buf);
    if (errMsg != null)
      throw new IllegalStateException("Buffer Validation error: " + errMsg);
    return buf;
  }

  public static Geometry bufferValidatedGeom(Geometry g, double distance)
  {
    Geometry buf = g.buffer(distance);
    BufferResultValidator validator = new BufferResultValidator(g, distance, buf);    
    boolean isValid = validator.isValid();
    return validator.getErrorIndicator();
  }

  public static Geometry singleSidedBufferCurve(Geometry geom, double distance) {
    BufferParameters bufParam = new BufferParameters();
    bufParam.setSingleSided(true);
    OffsetCurveBuilder ocb = new OffsetCurveBuilder(
        geom.getFactory().getPrecisionModel(), bufParam
        );
    Coordinate[] pts = ocb.getLineCurve(geom.getCoordinates(), distance);
    Geometry curve = geom.getFactory().createLineString(pts);
    return curve;
  }
  
  public static Geometry singleSidedBuffer(Geometry geom, double distance) {
    BufferParameters bufParams = new BufferParameters();
    bufParams.setSingleSided(true);
    return BufferOp.bufferOp(geom, distance, bufParams);
  }
  
  public static Geometry bufferEach(Geometry g, final double distance)
  {
    return GeometryMapper.map(g, new MapOp() {

      public Geometry map(Geometry g)
      {
        return g.buffer(distance);
      }
      
    });
  }

}
