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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.GeometryCombiner;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurve;
import org.locationtech.jtstest.geomfunction.Metadata;

public class OffsetCurveFunctions {

  public static Geometry offsetCurve(Geometry geom, double distance)
  {
    return OffsetCurve.getCurve(geom, distance);
  }

  public static Geometry offsetCurveWithParams(Geometry geom,       
      Double distance,
      @Metadata(title="Quadrant Segs")
      Integer quadrantSegments, 
      @Metadata(title="NOT USED")
      Integer capStyle, 
      @Metadata(title="Join style")
      Integer joinStyle, 
      @Metadata(title="Mitre limit")
      Double mitreLimit)
  {
    return OffsetCurve.getCurve(geom, distance, quadrantSegments, joinStyle, mitreLimit);
  }

  public static Geometry offsetCurveJoined(Geometry geom, double distance)
  {
    return OffsetCurve.getCurveJoined(geom, distance);
  }

  public static Geometry offsetCurveBoth(Geometry geom, double distance)
  {
    Geometry curve1 = OffsetCurve.getCurve(geom, distance);
    Geometry curve2 = OffsetCurve.getCurve(geom, -distance);
    return GeometryCombiner.combine(curve1, curve2);
  }

  public static Geometry offsetCurveBothWithParams(Geometry geom,       
      Double distance,
      @Metadata(title="Quadrant Segs")
      Integer quadrantSegments, 
      @Metadata(title="NOT USED")
      Integer capStyle, 
      @Metadata(title="Join style")
      Integer joinStyle, 
      @Metadata(title="Mitre limit")
      Double mitreLimit)
  {
    Geometry curve1 = OffsetCurve.getCurve(geom, distance, quadrantSegments, joinStyle, mitreLimit);
    Geometry curve2 = OffsetCurve.getCurve(geom, -distance, quadrantSegments, joinStyle, mitreLimit);
    return GeometryCombiner.combine(curve1, curve2);
  }

  public static Geometry offsetCurveSimplify(Geometry geom, double distance, double simplifyFactor)
  {
    BufferParameters params = new BufferParameters();
    params.setSimplifyFactor(simplifyFactor);
    OffsetCurve oc = new OffsetCurve(geom, distance, params);
    return oc.getCurve();
  }
  
  public static Geometry rawCurve(Geometry geom, double distance)
  {
    Coordinate[] pts = OffsetCurve.rawOffset((LineString) geom, distance);
    Geometry curve = geom.getFactory().createLineString(pts);
    return curve;
  }

  public static Geometry rawCurveWithParams(Geometry geom,       
      Double distance,
      @Metadata(title="Quadrant Segs")
      Integer quadrantSegments, 
      @Metadata(title="NOT USED")
      Integer capStyle, 
      @Metadata(title="Join style")
      Integer joinStyle, 
      @Metadata(title="Mitre limit")
      Double mitreLimit)
  {
    BufferParameters bufferParams = new BufferParameters();
    if (quadrantSegments >= 0) bufferParams.setQuadrantSegments(quadrantSegments);
    if (joinStyle >= 0) bufferParams.setJoinStyle(joinStyle);
    if (mitreLimit >= 0) bufferParams.setMitreLimit(mitreLimit);     
    Coordinate[] pts = OffsetCurve.rawOffset((LineString) geom, distance, bufferParams);
    Geometry curve = geom.getFactory().createLineString(pts);
    return curve;
  }


}
