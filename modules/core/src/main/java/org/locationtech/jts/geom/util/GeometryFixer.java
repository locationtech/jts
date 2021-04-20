/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.util;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

/**
 * Fixes a geometry to be a valid geometry, while preserving as much as 
 * possible of the shape and location of the input.
 * Validity is determined according to {@link Geometry#isValid()}.
 * <p>
 * Input geometries are always processed, so even valid inputs may 
 * have some minor alterations.  The output is always a new geometry object.
 * <h2>Semantic Rules</h2>
 * <ol>
 * <li>Vertices with non-finite X or Y ordinates are removed 
 * (as per {@link Coordinate#isValid()}.</li>
 * <li>Repeated points are reduced to a single point</li>
 * <li>Empty atomic geometries are valid and are returned unchanged</li>
 * <li>Empty elements are removed from collections</li>
 * <li><code>Point</code>: keep valid coordinate, or EMPTY</li>
 * <li><code>LineString</code>: fix coordinate list</li>
 * <li><code>LinearRing</code>: fix coordinate list, return as valid ring or else <code>LineString</code></li>
 * <li><code>Polygon</code>: transform into a valid polygon, 
 * preserving as much of the extent and vertices as possible</li>
 * <li><code>MultiPolygon</code>: fix each polygon, 
 * then ensure result is non-overlapping (via union)</li>
 * <li><code>GeometryCollection</code>: fix each element</li>
 * <li>Collapsed lines and polygons are handled as follows,
 * depending on the <code>keepCollapsed</code> setting:
 * <ul>
 * <li><code>false</code>: (default) collapses are converted to empty geometries</li>
 * <li><code>true</code>: collapses are converted to a valid geometry of lower dimension</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @author Martin Davis
 * 
 * @see Geometry#isValid()
 */
public class GeometryFixer {

  /**
   * Fixes a geometry to be valid.
   * 
   * @param geom the geometry to be fixed
   * @return the valid fixed geometry
   */
  public static Geometry fix(Geometry geom) {
    GeometryFixer fix = new GeometryFixer(geom);
    return fix.getResult();
  }
  
  private Geometry geom;
  private GeometryFactory factory;
  private boolean isKeepCollapsed = false;

  /**
   * Creates a new instance to fix a given geometry.
   * 
   * @param geom the geometry to be fixed
   */
  public GeometryFixer(Geometry geom) {
    this.geom = geom;
    this.factory = geom.getFactory();
  }
  
  /**
   * Sets whether collapsed geometries are converted to empty,
   * (which will be removed from collections),
   * or to a valid geometry of lower dimension.
   * The default is to convert collapses to empty geometries.
   * 
   * @param isKeepCollapsed whether collapses should be converted to a lower dimension geometry
   */
  public void setKeepCollapsed(boolean isKeepCollapsed) {
    this.isKeepCollapsed  = isKeepCollapsed;
  }
  
  /**
   * Gets the fixed geometry.
   * 
   * @return the fixed geometry
   */
  public Geometry getResult() {
    /**
     *  Truly empty geometries are simply copied.
     *  Geometry collections with elements are evaluated on a per-element basis.
     */
    if (geom.getNumGeometries() == 0) {
      return geom.copy();
    }
    
    if (geom instanceof Point)              return fixPoint((Point) geom);
    //  LinearRing must come before LineString
    if (geom instanceof LinearRing)         return fixLinearRing((LinearRing) geom);
    if (geom instanceof LineString)         return fixLineString((LineString) geom);
    if (geom instanceof Polygon)            return fixPolygon((Polygon) geom);
    if (geom instanceof MultiPoint)         return fixMultiPoint((MultiPoint) geom);
    if (geom instanceof MultiLineString)    return fixMultiLineString((MultiLineString) geom);
    if (geom instanceof MultiPolygon)       return fixMultiPolygon((MultiPolygon) geom);
    if (geom instanceof GeometryCollection) return fixCollection((GeometryCollection) geom);
    throw new UnsupportedOperationException(geom.getClass().getName());
  }

  private Point fixPoint(Point geom) {
    Geometry pt = fixPointElement(geom);
    if (pt == null)
      return factory.createPoint();
    return (Point) pt;
  }

  private Point fixPointElement(Point geom) {
    if (geom.isEmpty() || ! isValidPoint(geom)) {
      return null;
    }
    return (Point) geom.copy();
  }

  private static boolean isValidPoint(Point pt) {
    Coordinate p = pt.getCoordinate();
    return p.isValid();
  }

  private Geometry fixMultiPoint(MultiPoint geom) {
    List<Point> pts = new ArrayList<Point>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Point pt = (Point) geom.getGeometryN(i);
      if (pt.isEmpty()) continue;
      Point fixPt = fixPointElement(pt);
      if (fixPt != null) {
        pts.add(fixPt);
      }
    }
    return factory.createMultiPoint(GeometryFactory.toPointArray(pts));
  }
  
  private Geometry fixLinearRing(LinearRing geom) {
    Geometry fix = fixLinearRingElement(geom);
    if (fix == null)
      return factory.createLinearRing();
    return fix;
  }
  
  private Geometry fixLinearRingElement(LinearRing geom) {
    if (geom.isEmpty()) return null;
    Coordinate[] pts = geom.getCoordinates();
    Coordinate[] ptsFix = fixCoordinates(pts);
    if (isKeepCollapsed) {
      if (ptsFix.length == 1) {
        return factory.createPoint(ptsFix[0]);
      }
      if (ptsFix.length > 1 && ptsFix.length <= 3) {
        return factory.createLineString(ptsFix);
      }
    }
    //--- too short to be a valid ring
    if (ptsFix.length <= 3) {
      return null;
    }

    LinearRing ring = factory.createLinearRing(ptsFix);
    //--- convert invalid ring to LineString
    if (! ring.isValid()) {
      return factory.createLineString(ptsFix);
    }
    return ring;
  }

  private Geometry fixLineString(LineString geom) {
    Geometry fix = fixLineStringElement(geom);
    if (fix == null)
      return factory.createLineString();
    return fix;
  }
  
  private Geometry fixLineStringElement(LineString geom) {
    if (geom.isEmpty()) return null;
    Coordinate[] pts = geom.getCoordinates();
    Coordinate[] ptsFix = fixCoordinates(pts);
    if (isKeepCollapsed && ptsFix.length == 1) {
      return factory.createPoint(ptsFix[0]);
    }
    if (ptsFix.length <= 1) {
      return null;
    }
    return factory.createLineString(ptsFix);
  }

  /**
   * Returns a clean copy of the input coordinate array.
   * 
   * @param pts coordinates to clean
   * @return an array of clean coordinates
   */
  private static Coordinate[] fixCoordinates(Coordinate[] pts) {
    Coordinate[] ptsClean = CoordinateArrays.removeRepeatedOrInvalidPoints(pts);
    return CoordinateArrays.copyDeep(ptsClean);
  }
  
  private Geometry fixMultiLineString(MultiLineString geom) {
    List<Geometry> fixed = new ArrayList<Geometry>();
    boolean isMixed = false;
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      LineString line = (LineString) geom.getGeometryN(i);
      if (line.isEmpty()) continue;
      
      Geometry fix = fixLineStringElement(line);
      if (fix == null) continue;
      
      if (! (fix instanceof LineString)) {
        isMixed = true;
      }
      fixed.add(fix);
    }
    if (fixed.size() == 1) {
      return fixed.get(0);
    }
    if (isMixed) {
      return factory.createGeometryCollection(GeometryFactory.toGeometryArray(fixed));
    }
    return factory.createMultiLineString(GeometryFactory.toLineStringArray(fixed));
  }

  private Geometry fixPolygon(Polygon geom) {
    Geometry fix = fixPolygonElement(geom);
    if (fix == null)
      return factory.createPolygon();
    return fix;
  }
  
  private Geometry fixPolygonElement(Polygon geom) {
    LinearRing shell = geom.getExteriorRing();
    Geometry fixShell = fixRing(shell);
    if (fixShell.isEmpty()) {
      if (isKeepCollapsed) {
        return fixLineString(shell);
      }
      //-- if not allowing collapses then return empty polygon
      return null;      
    }
    // if no holes then done
    if (geom.getNumInteriorRing() == 0) {
      return fixShell;
    }
    Geometry fixHoles = fixHoles(geom);
    Geometry result = removeHoles(fixShell, fixHoles);
    return result;
  }

  private Geometry removeHoles(Geometry shell, Geometry holes) {
    if (holes == null) 
      return shell;
    return OverlayNGRobust.overlay(shell, holes, OverlayNG.DIFFERENCE);
  }

  private Geometry fixHoles(Polygon geom) {
    List<Geometry> holes = new ArrayList<Geometry>();
    for (int i = 0; i < geom.getNumInteriorRing(); i++) {
      Geometry holeRep = fixRing(geom.getInteriorRingN(i));
      if (holeRep != null) {
        holes.add(holeRep);
      }
    }
    if (holes.size() == 0) return null;
    if (holes.size() == 1) {
      return holes.get(0);
    }
    // TODO: replace with holes.union() once OverlayNG is the default
    Geometry holesUnion = OverlayNGRobust.union(holes);
    return holesUnion;
  }

  private Geometry fixRing(LinearRing ring) {
    //-- always execute fix, since it may remove repeated coords etc
    Geometry poly = factory.createPolygon(ring);
    // TOD: check if buffer removes invalid coordinates
    return BufferOp.bufferByZero(poly, true);
  }

  private Geometry fixMultiPolygon(MultiPolygon geom) {
    List<Geometry> polys = new ArrayList<Geometry>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Polygon poly = (Polygon) geom.getGeometryN(i);
      Geometry polyFix = fixPolygonElement(poly);
      if (polyFix != null && ! polyFix.isEmpty()) {
        polys.add(polyFix);
      }
    }
    if (polys.size() == 0) {
      return factory.createMultiPolygon();
    }
    // TODO: replace with polys.union() once OverlayNG is the default
    Geometry result = OverlayNGRobust.union(polys);
    return result;    
  }

  private Geometry fixCollection(GeometryCollection geom) {
    Geometry[] geomRep = new Geometry[geom.getNumGeometries()];
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      geomRep[i] = fix(geom.getGeometryN(i));
    }
    return factory.createGeometryCollection(geomRep);
  }
}
