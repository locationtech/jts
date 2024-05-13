/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;

/**
 * Determines the location for a point which is known to lie
 * on at least one edge of a set of polygons.
 * This provides the union-semantics for determining
 * point location in a GeometryCollection, which may
 * have polygons with adjacent edges which are effectively 
 * in the interior of the geometry.
 * Note that it is also possible to have adjacent edges which
 * lie on the boundary of the geometry 
 * (e.g. a polygon contained within another polygon with adjacent edges). 
 * 
 * @author mdavis
 *
 */
class AdjacentEdgeLocator {

  private List<Coordinate[]> ringList;;

  public AdjacentEdgeLocator(Geometry geom) {
    init(geom);
  }

  public int locate(Coordinate p) {
    NodeSections sections = new NodeSections(p);
    for (Coordinate[] ring : ringList) {
      addSections(p, ring, sections);
    }
    RelateNode node = sections.createNode();
    //node.finish(false, false);
    return node.hasExteriorEdge(true) ? Location.BOUNDARY : Location.INTERIOR;
  }

  private void addSections(Coordinate p, Coordinate[] ring, NodeSections sections) {
    for (int i = 0; i < ring.length - 1; i++) {
      Coordinate p0 = ring[i];
      Coordinate pnext = ring[i + 1];
      
      if (p.equals2D(pnext)) {
        //-- segment final point is assigned to next segment
        continue;
      }
      else if (p.equals2D(p0)) {
        int iprev = i > 0 ? i - 1 : ring.length - 2; 
        Coordinate pprev = ring[iprev];
        sections.addNodeSection(createSection(p, pprev, pnext));
      }
      else if (PointLocation.isOnSegment(p, p0, pnext)) {
        sections.addNodeSection(createSection(p, p0, pnext));
      }
    }
  }

  private NodeSection createSection(Coordinate p, Coordinate prev, Coordinate next) {
    if (prev.distance(p) == 0 || next.distance(p) == 0) {
      System.out.println("Found zero-length section segment");
    };
    NodeSection ns = new NodeSection(true, Dimension.A, 1, 0, null, false, prev, p, next);
    return ns;
  }

  private void init(Geometry geom) {
    if (geom.isEmpty())
      return;
    ringList = new ArrayList<Coordinate[]>();
    addRings(geom, ringList);
  }

  private void addRings(Geometry geom, List<Coordinate[]> ringList2) {
    if (geom instanceof Polygon) {
      Polygon poly = (Polygon) geom;
      LinearRing shell = poly.getExteriorRing();
      addRing(shell, true);
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        LinearRing hole = poly.getInteriorRingN(i);
        addRing(hole, false);
      }
    }
    else if (geom instanceof GeometryCollection) {
      //-- recurse through collections
      for (int i = 0; i < geom.getNumGeometries(); i++) {
        addRings(geom.getGeometryN(i), ringList);
      }
    }
  }

  private void addRing(LinearRing ring, boolean requireCW) {
    //TODO: remove repeated points?
    Coordinate[] pts = RelateGeometry.orient(ring.getCoordinates(), requireCW);  
    ringList.add(pts);
  }

}
