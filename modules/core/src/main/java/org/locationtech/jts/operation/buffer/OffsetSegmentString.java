/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.buffer;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * A dynamic list of the vertices in a constructed offset curve.
 * Automatically removes adjacent vertices
 * which are closer than a given tolerance.
 * 
 * @author Martin Davis
 *
 */
class OffsetSegmentString 
{
  private static final Coordinate[] COORDINATE_ARRAY_TYPE = new Coordinate[0];

  private ArrayList ptList;
  private PrecisionModel precisionModel = null;
  
  /**
   * The distance below which two adjacent points on the curve 
   * are considered to be coincident.
   * This is chosen to be a small fraction of the offset distance.
   */
  private double minimimVertexDistance = 0.0;

  public OffsetSegmentString()
  {
  	ptList = new ArrayList();
  }
  
  public void setPrecisionModel(PrecisionModel precisionModel)
  {
  	this.precisionModel = precisionModel;
  }
  
  public void setMinimumVertexDistance(double minimimVertexDistance)
  {
  	this.minimimVertexDistance = minimimVertexDistance;
  }
  
  public void addPt(Coordinate pt)
  {
    Coordinate bufPt = new Coordinate(pt);
    precisionModel.makePrecise(bufPt);
    // don't add duplicate (or near-duplicate) points
    if (isRedundant(bufPt))
        return;
    ptList.add(bufPt);
//System.out.println(bufPt);
  }
  
  public void addPts(Coordinate[] pt, boolean isForward)
  {
    if (isForward) {
      for (int i = 0; i < pt.length; i++) {
        addPt(pt[i]);
      }
    }
    else {
      for (int i = pt.length - 1; i >= 0; i--) {
        addPt(pt[i]);
      }     
    }
  }
  
  /**
   * Tests whether the given point is redundant
   * relative to the previous
   * point in the list (up to tolerance).
   * 
   * @param pt
   * @return true if the point is redundant
   */
  private boolean isRedundant(Coordinate pt)
  {
    if (ptList.size() < 1)
    	return false;
    Coordinate lastPt = (Coordinate) ptList.get(ptList.size() - 1);
    double ptDist = pt.distance(lastPt);
    if (ptDist < minimimVertexDistance)
    	return true;
    return false;
  }
  
  public void closeRing()
  {
    if (ptList.size() < 1) return;
    Coordinate startPt = new Coordinate((Coordinate) ptList.get(0));
    Coordinate lastPt = (Coordinate) ptList.get(ptList.size() - 1);
    Coordinate last2Pt = null;
    if (ptList.size() >= 2)
      last2Pt = (Coordinate) ptList.get(ptList.size() - 2);
    if (startPt.equals(lastPt)) return;
    ptList.add(startPt);
  }

  public void reverse()
  {
    
  }
  
  public Coordinate[] getCoordinates()
  {
    /*
     // check that points are a ring - add the startpoint again if they are not
   if (ptList.size() > 1) {
      Coordinate start  = (Coordinate) ptList.get(0);
      Coordinate end    = (Coordinate) ptList.get(ptList.size() - 1);
      if (! start.equals(end) ) addPt(start);
    }
    */
    Coordinate[] coord = (Coordinate[]) ptList.toArray(COORDINATE_ARRAY_TYPE);
    return coord;
  }

  public String toString()
  {
  	GeometryFactory fact = new GeometryFactory();
  	LineString line = fact.createLineString(getCoordinates());
  	return line.toString();
  }
}
