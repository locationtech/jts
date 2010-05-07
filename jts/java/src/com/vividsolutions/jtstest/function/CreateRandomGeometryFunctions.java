package com.vividsolutions.jtstest.function;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.util.RandomShapeFactory;

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

  public static Geometry randomPointsInGridCircles(Geometry g, int nPts) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

    int nCell = (int) Math.sqrt(nPts) + 1;

    double xLen = env.getWidth() / nCell;
    double yLen = env.getHeight() / nCell;

    List pts = new ArrayList();

    for (int i = 0; i < nCell; i++) {
      for (int j = 0; j < nCell; j++) {
      	Coordinate centre = new Coordinate(
      			env.getMinX() + i * xLen + xLen / 2, 
      			env.getMinY() + j * yLen + yLen / 2);
      	Coordinate pt = randomPtInEllipseAround(centre, xLen, yLen);
        pts.add(geomFact.createPoint(pt));
      }
    }
    return geomFact.buildGeometry(pts);
  }

  public static Geometry randomPointsInGridWithGutter(Geometry g, int nPts, double gutterFraction) {
    Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

    int nCell = (int) Math.sqrt(nPts) + 1;

    double cellWidth = env.getWidth() / nCell;
    double cellHeight = env.getHeight() / nCell;

    double gutterSize = gutterFraction * Math.min(cellWidth, cellHeight);
    double gutterOffset = gutterSize / 2;
    double areaWidth = cellWidth - gutterSize;
    if (areaWidth < 0) areaWidth = 0;
    double areaHeight = cellHeight - gutterSize;
    if (areaHeight < 0) areaHeight = 0;

    List pts = new ArrayList();

    for (int i = 0; i < nCell; i++) {
      for (int j = 0; j < nCell; j++) {
        double x = env.getMinX() + i * cellWidth + gutterOffset + areaWidth * Math.random();
        double y = env.getMinY() + j * cellHeight + gutterOffset + areaHeight * Math.random();
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
      // use rans^2 to accentuate radial distribution
      double r = rMax * rand * rand;
      // produces even distribution
      //double r = rMax * Math.sqrt(rand);
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
      pts[i] = randomPtInRectangleAround(env.centre(), xLen, yLen);
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
       pt = randomPtInRectangleAround(env.centre(), xLen, yLen);
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

  public static Geometry randomPointsInPolygon(Geometry g, int nPts) {
  	RandomShapeFactory shapeFact = new RandomShapeFactory(FunctionsUtil.getFactoryOrDefault(g));
  	shapeFact.setExtent(g);
    return shapeFact.createPoints(nPts);
  }

  private static int randomQuadrant(int exclude)
  {
    while (true) { 
      int quad = (int) (Math.random() * 4);
      if (quad > 3) quad = 3;
      if (quad != exclude) return quad;
    }
  }
  
  private static Coordinate randomPtInRectangleAround(Coordinate centre, double width, double height)
  {
    double x0 = centre.x + width * (Math.random() - 0.5);
    double y0 = centre.y + height * (Math.random() - 0.5);
    return new Coordinate(x0, y0);    
  }
  private static Coordinate randomPtInEllipseAround(Coordinate centre, double width, double height)
  {
  	double rndAng = 2 * Math.PI * Math.random();
  	double rndRadius = Math.random();
    // use square root of radius, since area is proportional to square of radius
    double rndRadius2 = Math.sqrt(rndRadius);
  	double rndX = width/2 * rndRadius2 * Math.cos(rndAng); 
  	double rndY = height/2 * rndRadius2 * Math.sin(rndAng); 
  	
    double x0 = centre.x + rndX;
    double y0 = centre.y + rndY;
    return new Coordinate(x0, y0);    
  }

}
