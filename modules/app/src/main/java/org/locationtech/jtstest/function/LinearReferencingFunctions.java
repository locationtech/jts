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
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jtstest.geomfunction.Metadata;

public class LinearReferencingFunctions
{
  public static Geometry extractPoint(Geometry g, double index)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    Coordinate p = ll.extractPoint(index);
    return g.getFactory().createPoint(p);
  }
  
  public static Geometry extractLine(Geometry g,
      @Metadata(title="Start length")
      double start, 
      @Metadata(title="End length")
      double end)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    return ll.extractLine(start, end);
  }
  
  public static Geometry project(Geometry line, Geometry geom)
  {
    LengthIndexedLine ll = new LengthIndexedLine(line);
    if (geom.getDimension() == 0) {
      Coordinate[] projPts = new Coordinate[geom.getNumPoints()];
      for (int i = 0; i < geom.getNumPoints(); i++) {
        Coordinate pt = geom.getGeometryN(i).getCoordinate();
        double index = ll.project(pt);
        Coordinate p = ll.extractPoint(index);
      }
      if (projPts.length == 1) {
        return geom.getFactory().createPoint(projPts[0]);
      }
      return geom.getFactory().createMultiPointFromCoords(projPts);
    }
    else {
      return projectOnLine(line, geom);
    }
  }  
  
  private static Geometry projectOnLine(Geometry line, Geometry geom) {
    Coordinate[] bPts = geom.getCoordinates();
    
    LengthIndexedLine aLR = new LengthIndexedLine((LineString) line);
    
    double locStart = -1.0;
    double locEnd = -1.0;
    for (int i = 0; i < bPts.length; i++) {
      Coordinate maskPt = bPts[i];
      double loc = aLR.indexOf(maskPt);
      if (locStart < 0 || loc < locStart) locStart = loc;
      if (loc < 0 || loc > locEnd) locEnd = loc;
    }
    
    return aLR.extractLine(locStart, locEnd);
  }
  
  public static double projectIndex(Geometry line, Geometry geom)
  {
    LengthIndexedLine ll = new LengthIndexedLine(line);
    return ll.project(geom.getCoordinate());
  }

}
