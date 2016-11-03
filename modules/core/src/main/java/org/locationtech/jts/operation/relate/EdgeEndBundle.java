


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
package org.locationtech.jts.operation.relate;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geomgraph.Edge;
import org.locationtech.jts.geomgraph.EdgeEnd;
import org.locationtech.jts.geomgraph.GeometryGraph;
import org.locationtech.jts.geomgraph.Label;
import org.locationtech.jts.geomgraph.Position;

/**
 * A collection of {@link EdgeEnd}s which obey the following invariant:
 * They originate at the same node and have the same direction.
 *
 * @version 1.7
 */
public class EdgeEndBundle
  extends EdgeEnd
{
//  private BoundaryNodeRule boundaryNodeRule;
  private List edgeEnds = new ArrayList();

  public EdgeEndBundle(BoundaryNodeRule boundaryNodeRule, EdgeEnd e)
  {
    super(e.getEdge(), e.getCoordinate(), e.getDirectedCoordinate(), new Label(e.getLabel()));
    insert(e);
    /*
    if (boundaryNodeRule != null)
      this.boundaryNodeRule = boundaryNodeRule;
    else
      boundaryNodeRule = BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE;
    */
  }

  public EdgeEndBundle(EdgeEnd e)
  {
    this(null, e);
  }

  public Label getLabel() { return label; }
  public Iterator iterator() { return edgeEnds.iterator(); }
  public List getEdgeEnds() { return edgeEnds; }

  public void insert(EdgeEnd e)
  {
    // Assert: start point is the same
    // Assert: direction is the same
    edgeEnds.add(e);
  }
  /**
   * This computes the overall edge label for the set of
   * edges in this EdgeStubBundle.  It essentially merges
   * the ON and side labels for each edge.  These labels must be compatible
   */
  public void computeLabel(BoundaryNodeRule boundaryNodeRule)
  {
    // create the label.  If any of the edges belong to areas,
    // the label must be an area label
    boolean isArea = false;
    for (Iterator it = iterator(); it.hasNext(); ) {
      EdgeEnd e = (EdgeEnd) it.next();
      if (e.getLabel().isArea()) isArea = true;
    }
    if (isArea)
      label = new Label(Location.NONE, Location.NONE, Location.NONE);
    else
      label = new Label(Location.NONE);

    // compute the On label, and the side labels if present
    for (int i = 0; i < 2; i++) {
      computeLabelOn(i, boundaryNodeRule);
      if (isArea)
        computeLabelSides(i);
    }
  }

  /**
   * Compute the overall ON location for the list of EdgeStubs.
   * (This is essentially equivalent to computing the self-overlay of a single Geometry)
   * edgeStubs can be either on the boundary (eg Polygon edge)
   * OR in the interior (e.g. segment of a LineString)
   * of their parent Geometry.
   * In addition, GeometryCollections use a {@link BoundaryNodeRule} to determine
   * whether a segment is on the boundary or not.
   * Finally, in GeometryCollections it can occur that an edge is both
   * on the boundary and in the interior (e.g. a LineString segment lying on
   * top of a Polygon edge.) In this case the Boundary is given precendence.
   * <br>
   * These observations result in the following rules for computing the ON location:
   * <ul>
   * <li> if there are an odd number of Bdy edges, the attribute is Bdy
   * <li> if there are an even number >= 2 of Bdy edges, the attribute is Int
   * <li> if there are any Int edges, the attribute is Int
   * <li> otherwise, the attribute is NULL.
   * </ul>
   */
  private void computeLabelOn(int geomIndex, BoundaryNodeRule boundaryNodeRule)
  {
    // compute the ON location value
    int boundaryCount = 0;
    boolean foundInterior = false;

    for (Iterator it = iterator(); it.hasNext(); ) {
      EdgeEnd e = (EdgeEnd) it.next();
      int loc = e.getLabel().getLocation(geomIndex);
      if (loc == Location.BOUNDARY) boundaryCount++;
      if (loc == Location.INTERIOR) foundInterior = true;
    }
    int loc = Location.NONE;
    if (foundInterior)  loc = Location.INTERIOR;
    if (boundaryCount > 0) {
      loc = GeometryGraph.determineBoundary(boundaryNodeRule, boundaryCount);
    }
    label.setLocation(geomIndex, loc);

  }
  /**
   * Compute the labelling for each side
   */
  private void computeLabelSides(int geomIndex)
  {
    computeLabelSide(geomIndex, Position.LEFT);
    computeLabelSide(geomIndex, Position.RIGHT);
  }

  /**
   * To compute the summary label for a side, the algorithm is:
   *   FOR all edges
   *     IF any edge's location is INTERIOR for the side, side location = INTERIOR
   *     ELSE IF there is at least one EXTERIOR attribute, side location = EXTERIOR
   *     ELSE  side location = NULL
   *  <br>
   *  Note that it is possible for two sides to have apparently contradictory information
   *  i.e. one edge side may indicate that it is in the interior of a geometry, while
   *  another edge side may indicate the exterior of the same geometry.  This is
   *  not an incompatibility - GeometryCollections may contain two Polygons that touch
   *  along an edge.  This is the reason for Interior-primacy rule above - it
   *  results in the summary label having the Geometry interior on <b>both</b> sides.
   */
  private void computeLabelSide(int geomIndex, int side)
  {
    for (Iterator it = iterator(); it.hasNext(); ) {
      EdgeEnd e = (EdgeEnd) it.next();
      if (e.getLabel().isArea()) {
        int loc = e.getLabel().getLocation(geomIndex, side);
        if (loc == Location.INTERIOR) {
            label.setLocation(geomIndex, side, Location.INTERIOR);
            return;
        }
        else if (loc == Location.EXTERIOR)
              label.setLocation(geomIndex, side, Location.EXTERIOR);
      }
    }
  }

  /**
   * Update the IM with the contribution for the computed label for the EdgeStubs.
   */
  void updateIM(IntersectionMatrix im)
  {
    Edge.updateIM(label, im);
  }
  public void print(PrintStream out)
  {
    out.println("EdgeEndBundle--> Label: " + label);
    for (Iterator it = iterator(); it.hasNext(); ) {
      EdgeEnd ee = (EdgeEnd) it.next();
      ee.print(out);
      out.println();
    }
  }
}
