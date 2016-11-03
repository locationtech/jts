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

package org.locationtech.jtstest.testbuilder.model;

import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.*;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.testbuilder.geom.*;


/**
 * Holds the current {@link TestCaseEdit}.
 * 
 * @author Martin Davis
 *
 */
public class GeometryEditModel 
{
  private static WKTWriter wktWriter = new WKTWriter();

  private boolean readOnly = true;

  private int editGeomIndex = 0; // the index of the currently selected geometry
  
  private int geomType = GeometryType.POLYGON;  // from GeometryType

  private TestCaseEdit testCase;
  
  private transient Vector geometryListeners;

  public GeometryEditModel()
  {
    
  }
  
  public Envelope getEnvelope()
  {
    Envelope env = new Envelope();

    if (getGeometry(0) != null) {
      env.expandToInclude(getGeometry(0).getEnvelopeInternal());
    }
    if (getGeometry(1) != null) {
      env.expandToInclude(getGeometry(1).getEnvelopeInternal());
    }
    return env;
  }
  
  public Envelope getEnvelopeAll()
  {
    Envelope env = new Envelope();

    if (getGeometry(0) != null) {
      env.expandToInclude(getGeometry(0).getEnvelopeInternal());
    }
    if (getGeometry(1) != null) {
      env.expandToInclude(getGeometry(1).getEnvelopeInternal());
    }
    if (getResult() != null) {
      env.expandToInclude(getResult().getEnvelopeInternal());     
    }
    return env;
  }
  
  public Envelope getEnvelopeResult()
  {
    Envelope env = new Envelope();

    if (getResult() != null) {
      env.expandToInclude(getResult().getEnvelopeInternal());     
    }
    return env;
  }
  
  public int getGeomIndex() {
    return editGeomIndex;
  }
  
  public void setEditGeomIndex(int index) {
    editGeomIndex = index;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void clear()
  {
    setGeometry(null);
    geomChanged();
  }

  public int getGeometryType() {
    return geomType;
  }

  public void setGeometryType(int geomType) { this.geomType = geomType; }
  
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }
  
  public String getText(int textType) {
    String str = "";
    if (getGeometry(0) != null) {
      str += getText(getGeometry(0), textType);
      str += "\n\n";
    }
    if (getGeometry(1) != null) {
      str += getText(getGeometry(1), textType);
      str += "\n\n";
    }
    return str;
  }

  public static String getText(Geometry geom, int textType) {
    switch (textType) {
      case GeometryType.WELLKNOWNTEXT:
        String wkt = wktWriter.writeFormatted(geom);
        return wkt;
    }
    Assert.shouldNeverReachHere();
    return "";
  }

  public static String toStringVeryLarge(Geometry g)
  {
    return "[[ " + GeometryUtil.structureSummary(g) + " ]]";
  }
  

  //====================================

  public Geometry getResult() {
//    return result;
    return testCase.getResult();
  }

  public Geometry getGeometry()
  {
    return getGeometry(editGeomIndex);
  }
  
  public Geometry getGeometry(int i)
  {
    return testCase.getGeometry(i);
  }
  
  public void setTestCase(TestCaseEdit testCase)
  {
    this.testCase = testCase;
    geomChanged();
  }
  
  public void setGeometry(Geometry g)
  {
    setGeometry(editGeomIndex, g);
    geomChanged();
  }
  
  public void setGeometry(int i, Geometry g)
  {
    testCase.setGeometry(i, g);
    geomChanged();
  }
  
  public void clear(int i)
  {
    setGeometry(i, null);
    geomChanged();
  }
  
  /**
   * Adds a geometry component of the currently selected type,
   * to the currently selected geometry.
   * 
   * @param coordList
   */
  public void addComponent(List coordList)
  {
    GeometryCombiner creator = new GeometryCombiner(JTSTestBuilder.getGeometryFactory());
    
    Geometry newGeom = null;
    switch(getGeometryType()) {
    case GeometryType.POLYGON:
      newGeom = creator.addPolygonRing(getGeometry(), getRing(coordList));
      break;
    case GeometryType.LINESTRING:
      Coordinate[] pts = CoordinateArrays.toCoordinateArray(coordList);
      newGeom = creator.addLineString(getGeometry(), pts);      
      break;
    case GeometryType.POINT:
      newGeom = creator.addPoint(getGeometry(), (Coordinate) coordList.get(0));      
      break;
    }
    setGeometry(newGeom);
  }
  
  private static Coordinate[] getRing(List coordList)
  {
    List closedPts = coordList;
    Coordinate p0 = (Coordinate) coordList.get(0);
    Coordinate pn = (Coordinate) coordList.get(coordList.size() - 1);
    if (! p0.equals2D(pn)) {
      closedPts = new ArrayList(coordList);
      closedPts.add(p0.clone()); 
    }
    Coordinate[] pts = CoordinateArrays.toCoordinateArray(closedPts);
    return pts;
  }
  
  public Coordinate[] findAdjacentVertices(Coordinate vertex)
  {
    Geometry geom = getGeometry();
    if (geom == null) return null;
    return AdjacentVertexFinder.findVertices(getGeometry(), vertex);	
  }
  
  /**
   * Locates a non-vertex point on a line segment of the current geometry
   * within the given tolerance, if any.
   * 
   * Returns the closest point on the segment.
   * 
   * @param testPt
   * @param tolerance
   * @return the location found, or
   * null if no non-vertex point was within tolerance
   */
  public GeometryLocation locateNonVertexPoint(Coordinate testPt, double tolerance)
  {
    Geometry geom = getGeometry();
    if (geom == null) return null;
    return GeometryPointLocater.locateNonVertexPoint(getGeometry(), testPt, tolerance);
  }
  
  /**
   * Locates a vertex of the current geometry
   * within the given tolerance, if any.
   * Returns the closest point on the segment.
   * 
   * @param testPt
   * @param tolerance
   * @return the location of the vertex found, or
   * null if no vertex was within tolerance
   */
  public GeometryLocation locateVertex(Coordinate testPt, double tolerance)
  {
    Geometry geom = getGeometry();
    if (geom == null) return null;
    return GeometryPointLocater.locateVertex(getGeometry(), testPt, tolerance);
  }
  
  public Coordinate locateVertexPt(Coordinate testPt, double tolerance)
  {
    Geometry geom = getGeometry();
    if (geom == null) return null;
    GeometryLocation loc = locateVertex(testPt, tolerance);
    if (loc == null) return null;
    return loc.getCoordinate();
  }
  
  public void moveVertex(Coordinate fromLoc, Coordinate toLoc)
  {
    Geometry modGeom = GeometryVertexMover.move(getGeometry(), fromLoc, toLoc);
    setGeometry(modGeom);
  }
  
  public void geomChanged()
  {
    fireGeometryChanged(new GeometryEvent(this));
  }

  //============================================
  
  public synchronized void removeGeometryListener(GeometryListener l) {
    if (geometryListeners != null && geometryListeners.contains(l)) {
      Vector v = (Vector) geometryListeners.clone();
      v.removeElement(l);
      geometryListeners = v;
    }
  }

  public synchronized void addGeometryListener(GeometryListener l) {
    Vector v = geometryListeners == null ? new Vector(2)
        : (Vector) geometryListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      geometryListeners = v;
    }
  }

  public void fireGeometryChanged(GeometryEvent e) {
    if (geometryListeners != null) {
      Vector listeners = geometryListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((GeometryListener) listeners.elementAt(i)).geometryChanged(e);
      }
    }
  }


}
