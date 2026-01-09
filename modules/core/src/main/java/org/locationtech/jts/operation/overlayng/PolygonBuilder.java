/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.hprtree.HPRtree;

class PolygonBuilder {

  private GeometryFactory geometryFactory;
  private List<OverlayEdgeRing> shellList = new ArrayList<OverlayEdgeRing>();
  private List<OverlayEdgeRing> freeHoleList = new ArrayList<OverlayEdgeRing>();
  private boolean isEnforcePolygonal = true;

  public PolygonBuilder(List<OverlayEdge> resultAreaEdges, GeometryFactory geomFact) {
    this(resultAreaEdges, geomFact, true);
  }
  
  public PolygonBuilder(List<OverlayEdge> resultAreaEdges, GeometryFactory geomFact, boolean isEnforcePolygonal) {
    this.geometryFactory = geomFact;
    this.isEnforcePolygonal = isEnforcePolygonal;
    buildRings(resultAreaEdges);
  }

  public List<Polygon> getPolygons() {
    return computePolygons(shellList);  
  }

  public List<OverlayEdgeRing> getShellRings() {
    return shellList;  
  }

  private List<Polygon> computePolygons(List<OverlayEdgeRing> shellList)
  {
    List<Polygon> resultPolyList = new ArrayList<Polygon>();
    // add Polygons for all shells
    for (OverlayEdgeRing er : shellList ) {
      Polygon poly = er.toPolygon(geometryFactory);
      resultPolyList.add(poly);
    }
    return resultPolyList;
  }
  
  private void buildRings(List<OverlayEdge> resultAreaEdges)
  {
    linkResultAreaEdgesMax(resultAreaEdges);
    List<MaximalEdgeRing> maxRings = buildMaximalRings(resultAreaEdges);
    buildMinimalRings(maxRings);
    placeFreeHoles(shellList, freeHoleList);
    //Assert: every hole on freeHoleList has a shell assigned to it
  }
  
  private void linkResultAreaEdgesMax(List<OverlayEdge> resultEdges) {
    for (OverlayEdge edge : resultEdges ) {
      //Assert.isTrue(edge.isInResult());
      // TODO: find some way to skip nodes which are already linked
      MaximalEdgeRing.linkResultAreaMaxRingAtNode(edge);
    }    
  }
  
  /**
   * For all OverlayEdges in result, form them into MaximalEdgeRings
   */
  private static List<MaximalEdgeRing> buildMaximalRings(Collection<OverlayEdge> edges)
  {
    List<MaximalEdgeRing> edgeRings = new ArrayList<MaximalEdgeRing>();
    for (OverlayEdge e : edges) {
      if (e.isInResultArea() && e.getLabel().isBoundaryEither() ) {
        // if this edge has not yet been processed
        if (e.getEdgeRingMax() == null) {
          MaximalEdgeRing er = new MaximalEdgeRing(e);
          edgeRings.add(er);
        }
      }
    }
    return edgeRings;
  }

  private void buildMinimalRings(List<MaximalEdgeRing> maxRings)
  {
    for (MaximalEdgeRing erMax : maxRings) {
      List<OverlayEdgeRing> minRings = erMax.buildMinimalRings(geometryFactory);
      assignShellsAndHoles(minRings);
    }
  }

  private void assignShellsAndHoles(List<OverlayEdgeRing> minRings) {
    /**
     * Two situations may occur:
     * - the rings are a shell and some holes
     * - rings are a set of holes
     * This code identifies the situation
     * and places the rings appropriately 
     */
    OverlayEdgeRing shell = findSingleShell(minRings);
    if (shell != null) {
      assignHoles(shell, minRings);
      shellList.add(shell);
    }
    else {
      // all rings are holes; their shell will be found later
      freeHoleList.addAll(minRings);
    }
  }
  
  /**
   * Finds the single shell, if any, out of 
   * a list of minimal rings derived from a maximal ring.
   * The other possibility is that they are a set of (connected) holes, 
   * in which case no shell will be found.
   *
   * @return the shell ring, if there is one
   * or null, if all rings are holes
   */
  private OverlayEdgeRing findSingleShell(List<OverlayEdgeRing> edgeRings)
  {
    int shellCount = 0;
    OverlayEdgeRing shell = null;
    for ( OverlayEdgeRing er : edgeRings ) {
      if (! er.isHole()) {
        shell = er;
        shellCount++;
      }
    }
    Assert.isTrue(shellCount <= 1, "found two shells in EdgeRing list");
    return shell;
  }
  
  /**
   * For the set of minimal rings comprising a maximal ring, 
   * assigns the holes to the shell known to contain them.
   * Assigning the holes directly to the shell serves two purposes:
   * <ul>
   * <li>it is faster than using a point-in-polygon check later on.
   * <li>it ensures correctness, since if the PIP test was used the point
   * chosen might lie on the shell, which might return an incorrect result from the
   * PIP test
   * </ul>
   */
  private static void assignHoles(OverlayEdgeRing shell, List<OverlayEdgeRing> edgeRings)
  {
    for (OverlayEdgeRing er : edgeRings) {
      if (er.isHole()) {
        er.setShell(shell);
      }
    }
  }

  /**
   * Place holes have not yet been assigned to a shell.
   * These "free" holes should
   * all be <b>properly</b> contained in their parent shells, so it is safe to use the
   * <code>findEdgeRingContaining</code> method.
   * (This is the case because any holes which are NOT
   * properly contained (i.e. are connected to their
   * parent shell) would have formed part of a MaximalEdgeRing
   * and been handled in a previous step).
   *
   * @throws TopologyException if a hole cannot be assigned to a shell
   */
  private void placeFreeHoles(List<OverlayEdgeRing> shellList, List<OverlayEdgeRing> freeHoleList)
  {
    SpatialIndex index = new HPRtree();
    for (OverlayEdgeRing shell : shellList) {
      index.insert(shell.getRing().getEnvelopeInternal(), shell);
    }

    for (OverlayEdgeRing hole : freeHoleList ) {
      // only place this hole if it doesn't yet have a shell
      if (hole.getShell() == null) {
        List<OverlayEdgeRing> shellListOverlaps = index.query(hole.getRing().getEnvelopeInternal());
        OverlayEdgeRing shell = hole.findEdgeRingContaining(shellListOverlaps);
        // only when building a polygon-valid result
        if (isEnforcePolygonal  && shell == null) {
          throw new TopologyException("unable to assign free hole to a shell", hole.getCoordinate());
        }
        hole.setShell(shell);
      }
    }
  }

}
