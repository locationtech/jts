
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
package org.locationtech.jts.planargraph;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.Quadrant;

/**
 * Represents a directed edge in a {@link PlanarGraph}. A DirectedEdge may or
 * may not have a reference to a parent {@link Edge} (some applications of
 * planar graphs may not require explicit Edge objects to be created). Usually
 * a client using a <code>PlanarGraph</code> will subclass <code>DirectedEdge</code>
 * to add its own application-specific data and methods.
 *
 * @version 1.7
 */
public class DirectedEdge
    extends GraphComponent
    implements Comparable
{
  /**
   * Returns a List containing the parent Edge (possibly null) for each of the given
   * DirectedEdges.
   */
  public static List toEdges(Collection dirEdges)
  {
    List edges = new ArrayList();
    for (Iterator i = dirEdges.iterator(); i.hasNext(); ) {
      edges.add( ((DirectedEdge) i.next()).parentEdge);
    }
    return edges;
  }

  protected Edge parentEdge;
  protected Node from;
  protected Node to;
  protected Coordinate p0, p1;
  protected DirectedEdge sym = null;  // optional
  protected boolean edgeDirection;
  protected int quadrant;
  protected double angle;

  /**
   * Constructs a DirectedEdge connecting the <code>from</code> node to the
   * <code>to</code> node.
   *
   * @param directionPt
   *   specifies this DirectedEdge's direction vector
   *   (determined by the vector from the <code>from</code> node
   *   to <code>directionPt</code>)
   * @param edgeDirection
   *   whether this DirectedEdge's direction is the same as or
   *   opposite to that of the parent Edge (if any)
   */
  public DirectedEdge(Node from, Node to, Coordinate directionPt, boolean edgeDirection)
  {
    this.from = from;
    this.to = to;
    this.edgeDirection = edgeDirection;
    p0 = from.getCoordinate();
    p1 = directionPt;
    double dx = p1.x - p0.x;
    double dy = p1.y - p0.y;
    quadrant = Quadrant.quadrant(dx, dy);
    angle = Math.atan2(dy, dx);
    //Assert.isTrue(! (dx == 0 && dy == 0), "EdgeEnd with identical endpoints found");
  }

  /**
   * Returns this DirectedEdge's parent Edge, or null if it has none.
   */
  public Edge getEdge() { return parentEdge; }
  /**
   * Associates this DirectedEdge with an Edge (possibly null, indicating no associated
   * Edge).
   */
  public void setEdge(Edge parentEdge) { this.parentEdge = parentEdge; }
  /**
   * Returns 0, 1, 2, or 3, indicating the quadrant in which this DirectedEdge's
   * orientation lies.
   */
  public int getQuadrant() { return quadrant; }
  /**
   * Returns a point to which an imaginary line is drawn from the from-node to
   * specify this DirectedEdge's orientation.
   */
  public Coordinate getDirectionPt() { return p1; }
  /**
   * Returns whether the direction of the parent Edge (if any) is the same as that
   * of this Directed Edge.
   */
  public boolean getEdgeDirection() { return edgeDirection; }
  /**
   * Returns the node from which this DirectedEdge leaves.
   */
  public Node getFromNode() { return from; }
  /**
   * Returns the node to which this DirectedEdge goes.
   */
  public Node getToNode() { return to; }
  /**
   * Returns the coordinate of the from-node.
   */
  public Coordinate getCoordinate() { return from.getCoordinate(); }
  /**
   * Returns the angle that the start of this DirectedEdge makes with the
   * positive x-axis, in radians.
   */
  public double getAngle() { return angle; }
  /**
   * Returns the symmetric DirectedEdge -- the other DirectedEdge associated with
   * this DirectedEdge's parent Edge.
   */
  public DirectedEdge getSym() { return sym; }
  /**
   * Sets this DirectedEdge's symmetric DirectedEdge, which runs in the opposite
   * direction.
   */
  public void setSym(DirectedEdge sym) { this.sym = sym; }

  /**
   * Removes this directed edge from its containing graph.
   */
  void remove() {
    this.sym = null;
    this.parentEdge = null;
  }

  /**
   * Tests whether this directed edge has been removed from its containing graph
   *
   * @return <code>true</code> if this directed edge is removed
   */
  public boolean isRemoved()
  {
    return parentEdge == null;
  }

  /**
   * Returns 1 if this DirectedEdge has a greater angle with the
   * positive x-axis than b", 0 if the DirectedEdges are collinear, and -1 otherwise.
   * <p>
   * Using the obvious algorithm of simply computing the angle is not robust,
   * since the angle calculation is susceptible to roundoff. A robust algorithm
   * is:
   * <ul>
   * <li>first compare the quadrants. If the quadrants are different, it it
   * trivial to determine which vector is "greater".
   * <li>if the vectors lie in the same quadrant, the robust
   * {@link CGAlgorithms#computeOrientation(Coordinate, Coordinate, Coordinate)}
   * function can be used to decide the relative orientation of the vectors.
   * </ul>
   */
  public int compareTo(Object obj)
  {
      DirectedEdge de = (DirectedEdge) obj;
      return compareDirection(de);
  }

  /**
   * Returns 1 if this DirectedEdge has a greater angle with the
   * positive x-axis than b", 0 if the DirectedEdges are collinear, and -1 otherwise.
   * <p>
   * Using the obvious algorithm of simply computing the angle is not robust,
   * since the angle calculation is susceptible to roundoff. A robust algorithm
   * is:
   * <ul>
   * <li>first compare the quadrants. If the quadrants are different, it it
   * trivial to determine which vector is "greater".
   * <li>if the vectors lie in the same quadrant, the robust
   * {@link CGAlgorithms#computeOrientation(Coordinate, Coordinate, Coordinate)}
   * function can be used to decide the relative orientation of the vectors.
   * </ul>
   */
  public int compareDirection(DirectedEdge e)
  {
    // if the rays are in different quadrants, determining the ordering is trivial
    if (quadrant > e.quadrant) return 1;
    if (quadrant < e.quadrant) return -1;
    // vectors are in the same quadrant - check relative orientation of direction vectors
    // this is > e if it is CCW of e
    return CGAlgorithms.computeOrientation(e.p0, e.p1, p1);
  }

  /**
   * Prints a detailed string representation of this DirectedEdge to the given PrintStream.
   */
  public void print(PrintStream out)
  {
    String className = getClass().getName();
    int lastDotPos = className.lastIndexOf('.');
    String name = className.substring(lastDotPos + 1);
    out.print("  " + name + ": " + p0 + " - " + p1 + " " + quadrant + ":" + angle);
  }

}
