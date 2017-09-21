package org.locationtech.jts.offsetcurve;

import org.locationtech.jts.edgegraph.HalfEdge;
import org.locationtech.jts.geom.Coordinate;

public class Node {

  private HalfEdge edge;
  private double distance;
  private boolean isMarked;
  private Node nearestOnPath;

  public Node(HalfEdge e) {
    this.edge = e;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public Coordinate getCoordinate() {
    return edge.orig();
  }

  public HalfEdge edge() {
    return edge;
  }

  public String toString() {
    Coordinate p = edge.orig();
    return "[ " + p.x + ", " + p.y + " d= " + distance + " ]";
  }

  public void setMark(boolean b) {
    isMarked = b;
  }
  public boolean isMarked() {
    return isMarked;
  }

  public void setNearestOnPath(Node n) {
    nearestOnPath = n;
  }

  public Node getNearestOnPath() {
    return nearestOnPath;
  }

}
