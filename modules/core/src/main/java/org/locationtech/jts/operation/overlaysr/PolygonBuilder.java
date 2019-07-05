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
package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;

public class PolygonBuilder {

  private List<OverlayEdge> resultAreaEdges;
  private GeometryFactory geometryFactory;
  private List shellList        = new ArrayList();

  public PolygonBuilder(List<OverlayEdge> resultAreaEdges, GeometryFactory geomFact) {
    this.resultAreaEdges = resultAreaEdges;
    this.geometryFactory = geomFact;
  }

  public List<Polygon> getPolygons() {
    buildRings();
    return computePolygons(shellList);  
  }

  private void buildRings()
  {
    // assumes that minimal edge rings have been linked
    List edgeRings = buildEdgeRings(resultAreaEdges);
    List freeHoleList = new ArrayList();
    sortShellsAndHoles(edgeRings, shellList, freeHoleList);
    placeFreeHoles(shellList, freeHoleList);
    //Assert: every hole on freeHoleList has a shell assigned to it
  }
  
  /**
   * for all DirectedEdges in result, form them into MaximalEdgeRings
   */
  private List buildEdgeRings(Collection<OverlayEdge> edges)
  {
    List edgeRings = new ArrayList();
    for (OverlayEdge e : edges) {
      if (e.isInResult() && e.getLabel().isArea() ) {
        // if this edge has not yet been processed
        if (e.getEdgeRing() == null) {
          EdgeRing er = new EdgeRing(e, geometryFactory);
          edgeRings.add(er);
          //er.setInResult();
//System.out.println("max node degree = " + er.getMaxDegree());
        }
      }
    }
    return edgeRings;
  }

  /**
   * For all rings in the input list,
   * determine whether the ring is a shell or a hole
   * and add it to the appropriate list.
   * Due to the way the DirectedEdges were linked,
   * a ring is a shell if it is oriented CW, a hole otherwise.
   */
  private void sortShellsAndHoles(List edgeRings, List shellList, List freeHoleList)
  {
    for (Iterator it = edgeRings.iterator(); it.hasNext(); ) {
      EdgeRing er = (EdgeRing) it.next();
//      er.setInResult();
      if (er.isHole() ) {
        freeHoleList.add(er);
      }
      else {
        shellList.add(er);
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
  private void placeFreeHoles(List shellList, List freeHoleList)
  {
    for (Iterator it = freeHoleList.iterator(); it.hasNext(); ) {
      EdgeRing hole = (EdgeRing) it.next();
      // only place this hole if it doesn't yet have a shell
      if (hole.getShell() == null) {
        EdgeRing shell = hole.findEdgeRingContaining(shellList);
        if (shell == null)
          throw new TopologyException("unable to assign hole to a shell", hole.getCoordinate());
//        Assert.isTrue(shell != null, "unable to assign hole to a shell");
        hole.setShell(shell);
      }
    }
  }

  private List computePolygons(List shellList)
  {
    List resultPolyList   = new ArrayList();
    // add Polygons for all shells
    for (Iterator it = shellList.iterator(); it.hasNext(); ) {
      EdgeRing er = (EdgeRing) it.next();
      Polygon poly = er.toPolygon(geometryFactory);
      resultPolyList.add(poly);
    }
    return resultPolyList;
  }

}
