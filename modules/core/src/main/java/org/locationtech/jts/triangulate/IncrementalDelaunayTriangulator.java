/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.triangulate;

import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.triangulate.quadedge.LocateFailureException;
import org.locationtech.jts.triangulate.quadedge.QuadEdge;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.Vertex;


/**
 * Computes a Delaunay Triangulation of a set of {@link Vertex}es, using an
 * incremental insertion algorithm.
 * 
 * @author Martin Davis
 * @version 1.0
 */
public class IncrementalDelaunayTriangulator 
{
	private QuadEdgeSubdivision subdiv;
	private boolean isUsingTolerance = false;

	/**
	 * Creates a new triangulator using the given {@link QuadEdgeSubdivision}.
	 * The triangulator uses the tolerance of the supplied subdivision.
	 * 
	 * @param subdiv
	 *          a subdivision in which to build the TIN
	 */
	public IncrementalDelaunayTriangulator(QuadEdgeSubdivision subdiv) {
		this.subdiv = subdiv;
		isUsingTolerance = subdiv.getTolerance() > 0.0;
		
	}

	/**
	 * Inserts all sites in a collection. The inserted vertices <b>MUST</b> be
	 * unique up to the provided tolerance value. (i.e. no two vertices should be
	 * closer than the provided tolerance value). They do not have to be rounded
	 * to the tolerance grid, however.
	 * 
	 * @param vertices a Collection of Vertex
	 * 
   * @throws LocateFailureException if the location algorithm fails to converge in a reasonable number of iterations
	 */
	public void insertSites(Collection vertices) {
		for (Iterator i = vertices.iterator(); i.hasNext();) {
			Vertex v = (Vertex) i.next();
			insertSite(v);
		}
	}

	/**
	 * Inserts a new point into a subdivision representing a Delaunay
	 * triangulation, and fixes the affected edges so that the result is still a
	 * Delaunay triangulation.
	 * <p>
	 * 
	 * @return a quadedge containing the inserted vertex
	 */
	public QuadEdge insertSite(Vertex v) {

		/**
		 * This code is based on Guibas and Stolfi (1985), with minor modifications
		 * and a bug fix from Dani Lischinski (Graphic Gems 1993). (The modification
		 * I believe is the test for the inserted site falling exactly on an
		 * existing edge. Without this test zero-width triangles have been observed
		 * to be created)
		 */
		QuadEdge e = subdiv.locate(v);

		if (subdiv.isVertexOfEdge(e, v)) {
			// point is already in subdivision.
			return e; 
		} 
		else if (subdiv.isOnEdge(e, v.getCoordinate())) {
			// the point lies exactly on an edge, so delete the edge 
			// (it will be replaced by a pair of edges which have the point as a vertex)
			e = e.oPrev();
			subdiv.delete(e.oNext());
		}

		/**
		 * Connect the new point to the vertices of the containing triangle 
		 * (or quadrilateral, if the new point fell on an existing edge.)
		 */
		QuadEdge base = subdiv.makeEdge(e.orig(), v);
		QuadEdge.splice(base, e);
		QuadEdge startEdge = base;
		do {
			base = subdiv.connect(e, base.sym());
			e = base.oPrev();
		} while (e.lNext() != startEdge);

		/**
		 * Examine suspect edges to ensure that the Delaunay condition is satisfied.
		 * If it is not, flip the edge and continue testing.
		 * 
		 * Since the frame is not infinitely far away,
		 * edges which touch the frame or are adjacent to it require logic
		 * to ensure the inner triangulation maintains a convex boundary.
		 */
		do {
System.out.println(e);
if (e.dest().getCoordinate().equals2D(new Coordinate(2, 204))) {
  System.out.println("---> " + e);
  System.out.println(subdiv.getTriangles(true, new GeometryFactory()));
}
System.out.println(subdiv.getTriangles(true, new GeometryFactory()));

      boolean doFlip = false;
      if (subdiv.isFrameVertex(e.dest())) {
        doFlip = doFlipAtFrameVertex(e);
      }
      else if (subdiv.isFrameVertex(e.orig())) {
        doFlip = doFlipAtFrameVertex(e.sym());
      }
      /*
      else if (isBetweenFrameAndInserted(e, v)) {
        //-- don't flip if edge lies between the inserted vertex and a frame vertex
        doFlip = false;
      }
      */
      else {
        //-- general case - flip if vertex is in circumcircle
        QuadEdge t = e.oPrev();
        doFlip = t.dest().rightOf(e) && v.isInCircle(e.orig(), t.dest(), e.dest());
      }
      
      if (doFlip) {
        //-- flip the edge within its quadrilateral
        QuadEdge.swap(e);
        e = e.oPrev();
        continue;
      }
      
      if (e.oNext() == startEdge) {
        return base; // no more suspect edges.
      } else {
        e = e.oNext().lPrev();
      }
    } while (true);
	}

	/**
	 * Tests if a edge terminating in a frame vertex should be flipped.
	 * The edge is flipped if it creates a concavity in the triangulation boundary.
	 * 
	 * @param e an edge whose dest is a frame vertex
	 * @return true if the edge should be flipped
	 */
  private boolean doFlipAtFrameVertex(QuadEdge e) {
    //-- if an edge adjacent to e is a frame edge, don't flip
    if (subdiv.isFrameTriangleEdge(e.dNext()) || subdiv.isFrameTriangleEdge(e.dPrev())) {
      return false;
    }
    //-- flip if boundary is concave
    return isConcaveAtOrigin(e);
  }

  private static boolean isConcaveAtOrigin(QuadEdge e) {
    Coordinate p = e.orig().getCoordinate();
    Coordinate pp = e.oPrev().dest().getCoordinate();
    Coordinate pn = e.oNext().dest().getCoordinate();
    boolean isConcave = Orientation.COUNTERCLOCKWISE == Orientation.index(pp, pn, p);
    // DEBUG
    //if (isConcave) {
      //System.out.println(WKTWriter.toLineString(new Coordinate[] { pn, pp, p}));
    //}
    return isConcave;
  }

  /**
   * Edges whose adjacent triangles contain
   * a frame vertex and the inserted vertex must not be flipped.
   *
   * @param e the edge to test
   * @param vInsert the inserted vertex
   * @return true if the edge is between the frame and inserted vertex
   */
  private boolean isBetweenFrameAndInserted(QuadEdge e, Vertex vInsert) {
    Vertex v1 = e.oNext().dest();
    Vertex v2 = e.oPrev().dest();
    return (v1 == vInsert && subdiv.isFrameVertex(v2))
        || (v2 == vInsert && subdiv.isFrameVertex(v1));
  }
}
