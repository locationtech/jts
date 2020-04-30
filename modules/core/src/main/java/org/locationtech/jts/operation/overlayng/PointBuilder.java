package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class PointBuilder {

  private GeometryFactory geometryFactory;
  private OverlayGraph graph;
  private int opCode;
  private int resultAreaIndex;
  private InputGeometry inputGeom;
  private int resultDimension;
  private boolean hasResultArea;
  private List<Point> points = new ArrayList<Point>();
  
  public PointBuilder(InputGeometry inputGeom, OverlayGraph graph, boolean hasResultArea, int opCode,
      GeometryFactory geomFact) {
    this.inputGeom = inputGeom;
    this.graph = graph;
    this.opCode = opCode;
    this.geometryFactory = geomFact;
    //this.resultAreaIndex = resultAreaIndex(opCode);
    this.hasResultArea = hasResultArea;
  }

  public List<Point> getPoints() {
    return points;
  }

}
