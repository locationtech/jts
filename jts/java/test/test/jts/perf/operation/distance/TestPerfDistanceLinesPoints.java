package test.jts.perf.operation.distance;

import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.distance.IndexedFacetDistance;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.util.Stopwatch;
import mdjts.operation.distance.*;

public class TestPerfDistanceLinesPoints 
{
  static GeometryFactory geomFact = new GeometryFactory();
  
  static final int MAX_ITER = 1;
  static final int NUM_TARGET_ITEMS = 4000;
  static final double EXTENT = 1000;
  static final int NUM_PTS_SIDE = 100;


  public static void main(String[] args) {
    TestPerfDistanceLinesPoints test = new TestPerfDistanceLinesPoints();
    test.test();
  }

  boolean verbose = true;

  public TestPerfDistanceLinesPoints() {
  }

  public void test()
  {
    
    
//    test(5000);
//    test(8001);

    test(50);
    test(100);
    test(200);
    test(500);
    test(1000);
    test(5000);
    test(10000);
    test(50000);
    test(100000);
  }
  
  public void test(int num)
  {
    //Geometry lines = createLine(EXTENT, num);
    Geometry target = createDiagonalCircles(EXTENT, NUM_TARGET_ITEMS);
    Geometry[] pts = createPoints(EXTENT, num);
    
    if (verbose) System.out.println("Running with " + num * num + " points; target size = " + target.getNumPoints());
    if (! verbose) System.out.print(num + ", ");
    test(pts, target);
  }
  
  public void test(Geometry[] pts, Geometry geom)
  {
    Stopwatch sw = new Stopwatch();
    double dist = 0.0;
    for (int i = 0; i < MAX_ITER; i++) {
      computeDistance(pts, geom);
    }
    if (! verbose) System.out.println(sw.getTimeString());
    if (verbose) {
      System.out.println("Finished in " + sw.getTimeString());
      //System.out.println("       (Distance = " + dist + ")\n");
      System.out.println();
    }
  }

  void computeDistance(Geometry[] pts, Geometry geom)
  {
  IndexedFacetDistance bbd = new IndexedFacetDistance(geom);
    for (int i = 0; i < pts.length; i++ ) {
//       double dist = geom.distance(pts[i]);
    double dist = bbd.getDistance(pts[i].getCoordinate());
    }
  }
  
  Geometry createDiagonalCircles(double extent, int nSegs)
  {
    Polygon[] circles = new Polygon[nSegs];
    double inc = extent / nSegs;
    for (int i = 0; i < nSegs; i++) {
      double ord = i * inc;
      Coordinate p = new Coordinate(ord, ord);
      Geometry pt = geomFact.createPoint(p);
      circles[i] = (Polygon) pt.buffer(inc/2);
    }
    return geomFact.createMultiPolygon(circles);

  }
  
  Geometry createLine(double extent, int nSegs)
  {
    Coordinate[] pts = 
      new Coordinate[] {
        new Coordinate(0,0),
        new Coordinate(0, extent),
        new Coordinate(extent, extent),
        new Coordinate(extent, 0)
        
                                      };
    Geometry outline = geomFact.createLineString(pts);
    double inc = extent / nSegs;
    return Densifier.densify(outline, inc);    

  }
  
  Geometry createDiagonalLine(double extent, int nSegs)
  {
    Coordinate[] pts = new Coordinate[nSegs + 1];
    pts[0] = new Coordinate(0,0);
    double inc = extent / nSegs;
    for (int i = 1; i <= nSegs; i++) {
      double ord = i * inc;
      pts[i] = new Coordinate(ord, ord); 
    }
    return geomFact.createLineString(pts);
  }
  
  Geometry[] createPoints(double extent, int nPtsSide)
  {
    Geometry[] pts = new Geometry[nPtsSide * nPtsSide];
    int index = 0;
    double inc = extent / nPtsSide;
    for (int i = 0; i < nPtsSide; i++) {
      for (int j = 0; j < nPtsSide; j++) {
        pts[index++] = geomFact.createPoint(new Coordinate(i * inc, j * inc));
      }
    }
    return pts;
  }
}
  
  
