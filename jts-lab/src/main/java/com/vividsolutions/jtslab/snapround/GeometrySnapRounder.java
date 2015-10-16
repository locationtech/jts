package com.vividsolutions.jtslab.snapround;

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

import java.util.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.geom.util.GeometryEditor.CoordinateSequenceOperation;
import com.vividsolutions.jts.noding.*;
import com.vividsolutions.jts.noding.snapround.*;

/**
 * Nodes a {@link Geometry} using Snap-Rounding
 * to a given {@link PrecisionModel}.
 * <ul>
 * <li>Point geometries are not handled.  They are skipped if present in the input.
 * <li>Linestrings which collapse to a point due to snapping are removed.
 * <li>Polygonal output may not be valid.  
 * Invalid output is due to the introduction of topology collapses.
 * This should be straightforward to clean using standard heuristics (e.g. buffer(0) ).
 * </ul>
 * The input geometry coordinates are expected to be rounded
 * to the given precision model.
 * This class does not perform that function.
 * <code>GeometryPrecisionReducer</code> may be used to do this.
 */
public class GeometrySnapRounder
{
  private PrecisionModel pm;
  private boolean isLineworkOnly = false;

  /**
   * Creates a new snap-rounder which snap-rounds to a grid specified
   * by the given {@link PrecisionModel}.
   * 
   * @param pm the precision model for the grid to snap-round to
   */
  public GeometrySnapRounder(PrecisionModel pm) {
    this.pm = pm;
  }
  
  public void setLineworkOnly(boolean isLineworkOnly) {
    this.isLineworkOnly = isLineworkOnly;
  }
  
  /**
   * Snap-rounds the given geometry.
   *  
   * 
   * @param geom
   * @return
   */
  public Geometry execute(Geometry geom) {
    
    // TODO: reduce precision of input automatically
    // TODO: add switch to GeometryPrecisionReducer to NOT check & clean invalid polygonal geometry (not needed here)
    // TODO: OR just do precision reduction with custom code here 
    
    List segStrings = extractTaggedSegmentStrings(geom, pm);
    snapRound(segStrings);
    
    if (isLineworkOnly) {
      return toNodedLines(segStrings, geom.getFactory());
    }
    
    Geometry geomSnapped = replaceLines(geom, segStrings);
    Geometry geomClean = ensureValid(geomSnapped);
    return geomClean;
  }

  private Geometry toNodedLines(Collection segStrings, GeometryFactory geomFact) {
    List lines = new ArrayList();
    for (Iterator it = segStrings.iterator(); it.hasNext(); ) {
      NodedSegmentString nss = (NodedSegmentString) it.next();
      // skip collapsed lines
      if (nss.size() < 2)
        continue;
      //Coordinate[] pts = getCoords(nss);
      Coordinate[] pts = nss.getNodeList().getSplitCoordinates();
      
      lines.add(geomFact.createLineString(pts));
    }
    return geomFact.buildGeometry(lines);
  }
  
  private Geometry replaceLines(Geometry geom, List segStrings) {
    Map nodedLinesMap = nodedLinesMap(segStrings);
    GeometryCoordinateReplacer lineReplacer = new GeometryCoordinateReplacer(nodedLinesMap);
    GeometryEditor geomEditor = new GeometryEditor();
    Geometry snapped = geomEditor.edit(geom,  lineReplacer);
    return snapped;
  }

  private void snapRound(List segStrings) {
    //Noder sr = new SimpleSnapRounder(pm);
    Noder sr = new MCIndexSnapRounder(pm);
    sr.computeNodes(segStrings);
  }

  private HashMap nodedLinesMap(Collection segStrings) {
    HashMap ptsMap = new HashMap();
    for (Iterator it = segStrings.iterator(); it.hasNext(); ) {
      NodedSegmentString nss = (NodedSegmentString) it.next();
      // skip collapsed lines
      if (nss.size() < 2)
        continue;
      Coordinate[] pts = nss.getNodeList().getSplitCoordinates();
      ptsMap.put(nss.getData(), pts);
    }
    return ptsMap;
  }
  
  static List extractTaggedSegmentStrings(Geometry geom, final PrecisionModel pm)
  {
    final List segStrings = new ArrayList();
    GeometryComponentFilter filter = new GeometryComponentFilter() {
      public void filter(Geometry geom) {
        // Extract linework for lineal components only
        if (! (geom instanceof LineString) ) return;
        // skip empty lines
        if (geom.getNumPoints() <= 0) return;
        Coordinate[] roundPts = round( ((LineString)geom).getCoordinateSequence(), pm);
        segStrings.add(new NodedSegmentString(roundPts, geom));
      }
    };
    geom.apply(filter);
    return segStrings;
  }
  
  static Coordinate[] round(CoordinateSequence seq, PrecisionModel pm) {
    if (seq.size() == 0) return new Coordinate[0];

    CoordinateList coordList = new CoordinateList();  
    // copy coordinates and reduce
    for (int i = 0; i < seq.size(); i++) {
      Coordinate coord = new Coordinate(
          seq.getOrdinate(i,  Coordinate.X),
          seq.getOrdinate(i,  Coordinate.Y) );
      pm.makePrecise(coord);
      coordList.add(coord, false);
    }
    Coordinate[] coord = coordList.toCoordinateArray();
    
    //TODO: what if seq is too short?
    return coord;
  }
  
  private static Geometry ensureValid(Geometry geom) {
    // TODO: need to ensure all polygonal components are valid
    if (! (geom instanceof Polygonal) ) return geom;
    if (geom.isValid()) return geom;
    
    return cleanPolygonal(geom);
  }

  private static Geometry cleanPolygonal(Geometry geom) {
    // TODO: use a better method of removing collapsed topology 
    return geom.buffer(0);
  }
  
  private static class GeometryCoordinateReplacer extends CoordinateSequenceOperation {
  
    private Map geometryLinesMap;
  
    public GeometryCoordinateReplacer(Map linesMap) {
      this.geometryLinesMap = linesMap;
    }
    
    /**
     * Gets the snapped coordinate array for an atomic geometry,
     * or null if it has collapsed.
     * 
     * @return the snapped coordinate array for this geometry
     * @return null if the snapped coordinates have collapsed, or are missing
     */
    public CoordinateSequence edit(CoordinateSequence coordSeq, Geometry geometry) {
      if (geometryLinesMap.containsKey(geometry)) {
        Coordinate[] pts = (Coordinate[]) geometryLinesMap.get(geometry);
        // Assert: pts should always have length > 0
        boolean isValidPts = isValidSize(pts, geometry);
        if (! isValidPts) return null;
        return geometry.getFactory().getCoordinateSequenceFactory().create(pts);
      }
      //TODO: should this return null if no matching snapped line is found
      // probably should never reach here?
      return coordSeq;
    }

    /**
     * Tests if a coordinate array has a size which is 
     * valid for the containing geometry.
     * 
     * @param pts the point list to validate
     * @param geom the atomic geometry containing the point list
     * @return true if the coordinate array is a valid size
     */
    private static boolean isValidSize(Coordinate[] pts, Geometry geom) {
      if (pts.length == 0) return true;
      int minSize = minimumNonEmptyCoordinatesSize(geom);
      if (pts.length < minSize) {
        return false;
      }
      return true;
    }

    private static int minimumNonEmptyCoordinatesSize(Geometry geom) {
      if (geom instanceof LinearRing)
        return 4;
      if (geom instanceof LineString)
        return 2;
      return 0;
    }
  }
}


