/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
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

public class PolygonBuilder {

  private GeometryFactory geometryFactory;
  private List<EdgeRing> shellList = new ArrayList<EdgeRing>();
  private List<EdgeRing> freeHoleList = new ArrayList<EdgeRing>();

  public PolygonBuilder(List<OverlayEdge> resultAreaEdges, GeometryFactory geomFact) {
    this.geometryFactory = geomFact;
    buildRings(resultAreaEdges);
  }

  public List<Polygon> getPolygons() {
    return computePolygons(shellList);  
  }

  private void buildRings(List<OverlayEdge> resultAreaEdges)
  {
    linkResultAreaEdgesMax(resultAreaEdges);
    // assumes that minimal edge rings have been linked
    List<MaximalEdgeRing> maxRings = buildMaximalRings(resultAreaEdges);
    buildMinimalRings(maxRings);
    placeFreeHoles(shellList, freeHoleList);
    //Assert: every hole on freeHoleList has a shell assigned to it
  }
  
  private void linkResultAreaEdgesMax(List<OverlayEdge> resultEdges) {
    for (OverlayEdge edge : resultEdges ) {
      //Assert.isTrue(edge.isInResult());
      // TODO: find some way to skip nodes which are already linked
      OverlayNode.linkResultAreaEdgesMax(edge);
    }    
  }
  
  /**
   * For all OverlayEdges in result, form them into MaximalEdgeRings
   */
  private static List<MaximalEdgeRing> buildMaximalRings(Collection<OverlayEdge> edges)
  {
    List<MaximalEdgeRing> edgeRings = new ArrayList<MaximalEdgeRing>();
    for (OverlayEdge e : edges) {
      if (e.isInResult() && e.getLabel().isArea() ) {
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
      List<EdgeRing> minRings = erMax.buildMinimalRings(geometryFactory);
      assignShellsAndHoles(minRings);
    }
  }

  private void assignShellsAndHoles(List<EdgeRing> minRings) {
    /**
     * Two situations may occur:
     * - the rings are a shell and some holes
     * - rings are a set of holes
     * This code identifies the situation
     * and places the rings appropriately 
     */
    EdgeRing shell = findSingleShell(minRings);
    if (shell != null) {
      placeHoles(shell, minRings);
      shellList.add(shell);
    }
    else {
      // rings must all be holes
      freeHoleList.addAll(minRings);
    }
  }
  
  /**
   * This method takes a list of EdgeRings derived from a MaximalEdgeRing,
   * and tests whether they form a Polygon.  This is the case if there is a single shell
   * in the list.  In this case the shell is returned.
   * The other possibility is that they are a series of connected holes, in which case
   * no shell is returned.
   *
   * @return the shell EdgeRing, if there is one
   * or null, if all the rings are holes
   */
  private EdgeRing findSingleShell(List<EdgeRing> edgeRings)
  {
    int shellCount = 0;
    EdgeRing shell = null;
    for ( EdgeRing er : edgeRings ) {
      if (! er.isHole()) {
        shell = er;
        shellCount++;
      }
    }
    Assert.isTrue(shellCount <= 1, "found two shells in EdgeRing list");
    return shell;
  }
  
  /**
   * This method assigns the holes for a Polygon (formed from a list of
   * EdgeRings) to its shell.
   * Determining the holes for a EdgeRing polygon serves two purposes:
   * <ul>
   * <li>it is faster than using a point-in-polygon check later on.
   * <li>it ensures correctness, since if the PIP test was used the point
   * chosen might lie on the shell, which might return an incorrect result from the
   * PIP test
   * </ul>
   */
  private void placeHoles(EdgeRing shell, List<EdgeRing> minEdgeRings)
  {
    for (EdgeRing er : minEdgeRings) {
      if (er.isHole()) {
        er.setShell(shell);
      }
    }
  }

  /**
   * This method determines finds a containing shell for all holes
   * which have not yet been assigned to a shell.
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
  private void placeFreeHoles(List<EdgeRing> shellList, List<EdgeRing> freeHoleList)
  {
    for (EdgeRing hole : freeHoleList ) {
      // only place this hole if it doesn't yet have a shell
      if (hole.getShell() == null) {
        EdgeRing shell = hole.findEdgeRingContaining(shellList);
        if (shell == null)
          throw new TopologyException("unable to assign free hole to a shell", hole.getCoordinate());
//        Assert.isTrue(shell != null, "unable to assign hole to a shell");
        hole.setShell(shell);
      }
    }
  }

  private List<Polygon> computePolygons(List<EdgeRing> shellList)
  {
    List<Polygon> resultPolyList = new ArrayList<Polygon>();
    // add Polygons for all shells
    for (EdgeRing er : shellList ) {
      Polygon poly = er.toPolygon(geometryFactory);
      resultPolyList.add(poly);
    }
    return resultPolyList;
  }

}
