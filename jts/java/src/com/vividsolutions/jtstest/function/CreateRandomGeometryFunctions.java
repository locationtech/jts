package com.vividsolutions.jtstest.function;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class CreateRandomGeometryFunctions {

  public static Geometry randomPointsInGrid(Geometry g, int nPts) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

    int nCell = (int) Math.sqrt(nPts) + 1;

    double xLen = env.getWidth() / nCell;
    double yLen = env.getHeight() / nCell;

    List pts = new ArrayList();

    for (int i = 0; i < nCell; i++) {
      for (int j = 0; j < nCell; j++) {
        double x = env.getMinX() + i * xLen + xLen * Math.random();
        double y = env.getMinY() + j * yLen + yLen * Math.random();
        pts.add(geomFact.createPoint(new Coordinate(x, y)));
      }
    }
    return geomFact.buildGeometry(pts);
  }

  public static Geometry randomPoints(Geometry g, int nPts) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    double xLen = env.getWidth();
    double yLen = env.getHeight();

    List pts = new ArrayList();

    for (int i = 0; i < nPts; i++) {
      double x = env.getMinX() + xLen * Math.random();
      double y = env.getMinY() + yLen * Math.random();
      pts.add(geomFact.createPoint(new Coordinate(x, y)));
    }
    return geomFact.buildGeometry(pts);
  }

  public static Geometry randomRadialPoints(Geometry g, int nPts) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    double xLen = env.getWidth();
    double yLen = env.getHeight();
    double rMax = Math.min(xLen, yLen) / 2.0;
    
    double centreX = env.getMinX() + xLen/2;
    double centreY = env.getMinY() + yLen/2;
    
    List pts = new ArrayList();

    for (int i = 0; i < nPts; i++) {
      double rand = Math.random();
      double r = rMax * rand * rand;
      double ang = 2 * Math.PI * Math.random();
      double x = centreX + r * Math.cos(ang);
      double y = centreY + r * Math.sin(ang);
      pts.add(geomFact.createPoint(new Coordinate(x, y)));
    }
    return geomFact.buildGeometry(pts);
  }

  public static Geometry randomSegments(Geometry g, int nPts) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    double xLen = env.getWidth();
    double yLen = env.getHeight();

    List lines = new ArrayList();

    for (int i = 0; i < nPts; i++) {
      double x0 = env.getMinX() + xLen * Math.random();
      double y0 = env.getMinY() + yLen * Math.random();
      double x1 = env.getMinX() + xLen * Math.random();
      double y1 = env.getMinY() + yLen * Math.random();
      lines.add(geomFact.createLineString(new Coordinate[] {
          new Coordinate(x0, y0), new Coordinate(x1, y1) }));
    }
    return geomFact.buildGeometry(lines);
  }

  public static Geometry randomSegmentsInGrid(Geometry g, int nPts) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

    int nCell = (int) Math.sqrt(nPts) + 1;

    double xLen = env.getWidth() / nCell;
    double yLen = env.getHeight() / nCell;

    List lines = new ArrayList();

    for (int i = 0; i < nCell; i++) {
      for (int j = 0; j < nCell; j++) {
        double x0 = env.getMinX() + i * xLen + xLen * Math.random();
        double y0 = env.getMinY() + j * yLen + yLen * Math.random();
        double x1 = env.getMinX() + i * xLen + xLen * Math.random();
        double y1 = env.getMinY() + j * yLen + yLen * Math.random();
        lines.add(geomFact.createLineString(new Coordinate[] {
            new Coordinate(x0, y0), new Coordinate(x1, y1) }));
      }
    }
    return geomFact.buildGeometry(lines);
  }

  public static Geometry randomLineString(Geometry g, int nPts) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    double width = env.getWidth();
    double hgt = env.getHeight();

    Coordinate[] pts = new Coordinate[nPts];

    for (int i = 0; i < nPts; i++) {
      double xLen = width * Math.random();
      double yLen = hgt * Math.random();
      pts[i] = randomPtAround(env.centre(), xLen, yLen);
    }
    return geomFact.createLineString(pts);
  }

  public static Geometry randomRectilinearWalk(Geometry g, int nPts) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    double xLen = env.getWidth();
    double yLen = env.getHeight();

    Coordinate[] pts = new Coordinate[nPts];

    boolean xory = true;
    for (int i = 0; i < nPts; i++) {
      Coordinate pt = null;
      if (i == 0) {
       pt = randomPtAround(env.centre(), xLen, yLen);
      }
      else {
        double dist = xLen * (Math.random() - 0.5);
        double x = pts[i-1].x;
        double y = pts[i-1].y;
        if (xory) {
          x += dist;
        }
        else {
          y += dist;
        }
        // switch orientation
        xory = ! xory;
        pt = new Coordinate(x, y);
      }
      pts[i] = pt;
    }
    return geomFact.createLineString(pts);
  }

  private static int randomQuadrant(int exclude)
  {
    while (true) { 
      int quad = (int) (Math.random() * 4);
      if (quad > 3) quad = 3;
      if (quad != exclude) return quad;
    }
  }
  
  private static Coordinate randomPtAround(Coordinate base, double xLen, double yLen)
  {
    double x0 = base.x + xLen * (Math.random() - 0.5);
    double y0 = base.y + yLen * (Math.random() - 0.5);
    return new Coordinate(x0, y0);    
  }

}
