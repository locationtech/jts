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
 * Nodes the linework in a list of {@link Geometry}s using Snap-Rounding
 * to a given {@link PrecisionModel}.
 * <p>
 * The input coordinates are expected to be rounded
 * to the given precision model.
 * This class does not perform that function.
 * <code>GeometryPrecisionReducer</code> may be used to do this.
 * <p>
 * This class does <b>not</b> dissolve the output linework,
 * so there may be duplicate linestrings in the output.  
 * Subsequent processing (e.g. polygonization) may require
 * the linework to be unique.  Using <code>UnaryUnion</code> is one way
 * to do this (although this is an inefficient approach).
 * 
 * 
 */
public class GeometrySnapRounder
{
  private GeometryFactory geomFact;
  private PrecisionModel pm;

  /**
   * Creates a new noder which snap-rounds to a grid specified
   * by the given {@link PrecisionModel}.
   * 
   * @param pm the precision model for the grid to snap-round to
   */
  public GeometrySnapRounder(PrecisionModel pm) {
    this.pm = pm;
  }
  
  /**
   * Nodes the linework of a set of Geometrys using SnapRounding. 
   * 
   * @param geoms a Collection of Geometrys of any type
   * @return a List of LineStrings representing the noded linework of the input
   */
  public List node(Collection geoms)
  {
    // get geometry factory
    Geometry geom0 = (Geometry) geoms.iterator().next();
    geomFact = geom0.getFactory();

    List segStrings = extractSegmentStrings(geoms);
    //Noder sr = new SimpleSnapRounder(pm);
    Noder sr = new MCIndexSnapRounder(pm);
    sr.computeNodes(segStrings);
    
    List nodedLines = getNodedLines(segStrings);
    return nodedLines;
    
  }
  
  public Geometry node(Geometry geom) {
    
    List geomList = new ArrayList();
    geomList.add(geom);
    List segStrings = extractSegmentStrings(geomList);
    //Noder sr = new SimpleSnapRounder(pm);
    Noder sr = new MCIndexSnapRounder(pm);
    sr.computeNodes(segStrings);

    
    Map nodedPtsMap = getNodedPtsMap(segStrings);
    GeometryLineReplacer lineReplacer = new GeometryLineReplacer(nodedPtsMap);
    GeometryEditor geomEditor = new GeometryEditor();
    Geometry snapped = geomEditor.edit(geom,  lineReplacer);
    return snapped;
    
  }

  private List extractSegmentStrings(Collection geoms)
  {
    final List segStrings = new ArrayList();
    GeometryComponentFilter filter = new GeometryComponentFilter() {

      public void filter(Geometry geom) {
        if (geom.getDimension() != 1) return;
        segStrings.add(new NodedSegmentString(geom.getCoordinates(), geom));
      }
      
    };
    for (Iterator it = geoms.iterator(); it.hasNext(); ) {
      Geometry geom = (Geometry) it.next();
      geom.apply(filter);
    }
    return segStrings;
  }
  
  private List getNodedLines(Collection segStrings) {
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
    return lines;
  }
  private HashMap getNodedPtsMap(Collection segStrings) {
    HashMap ptsMap = new HashMap();
    for (Iterator it = segStrings.iterator(); it.hasNext(); ) {
      NodedSegmentString nss = (NodedSegmentString) it.next();
      // skip collapsed lines
      if (nss.size() < 2)
        continue;
      //Coordinate[] pts = getCoords(nss);
      Coordinate[] pts = nss.getNodeList().getSplitCoordinates();
      
      ptsMap.put(nss.getData(), pts);
    }
    return ptsMap;
  }

  /*
  private Coordinate[] getCoords(NodedSegmentString nss) {
    List edges = new ArrayList();
    nss.getNodeList().addSplitEdges(edges);
    CoordinateList coordList = new CoordinateList();
    for (Iterator it = edges.iterator(); it.hasNext(); ) {
      SegmentString ss = (SegmentString) it.next();
      Coordinate[] coords = ss.getCoordinates();
      coordList.add(coords, false);
    }    
    
    Coordinate[] pts = coordList.toCoordinateArray();
    return pts;
  }
*/

}

class GeometryLineReplacer extends CoordinateSequenceOperation {

  private Map geometryPtsMap;

  public GeometryLineReplacer(Map geometryPtsMap) {
    this.geometryPtsMap = geometryPtsMap;
  }
  
  public CoordinateSequence edit(CoordinateSequence coordSeq, Geometry geometry) {
    if (geometryPtsMap.containsKey(geometry)) {
      Coordinate[] pts = (Coordinate[]) geometryPtsMap.get(geometry);
      return geometry.getFactory().getCoordinateSequenceFactory().create(pts);
    }
    return coordSeq;
  }
  
}
