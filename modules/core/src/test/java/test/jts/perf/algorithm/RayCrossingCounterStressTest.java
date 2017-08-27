package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.algorithm.RayCrossingCounterDD;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Stress test of {@link RayCrossingCounter} Point-In-Ring algorithm.
 * The input to each test is a triangle with a slanted side, 
 * and a point interpolated along the side. 
 * Almost always the point will not lie exactly on the side.
 * The test consists of comparing the result of computing Point-In-Ring and the result of
 * determining the orientation of the point relative to the side.
 * The test fails if these are not consistent.
 * <p>
 * The stress test reveals that the original RayCrossingCounter
 * has inconsistencies with the DD orientation index computation
 * (which is now the standard in JTS, due to improved robustness).
 * The {@link RayCrossingCounterDD} implementation is consistent,
 * as expected.
 * <p>
 * Note that the inconsistency does not indicate which algorithm is 
 * "more correct" - just that they produce different results.
 * However, it is highly likely that the original RCC algorithm
 * is not robust, since it involves significant arithmetic calcuation.
 * 
 * @author Martin Davis
 *
 */
public class RayCrossingCounterStressTest extends GeometryTestCase {
  

  public static void main(String args[]) {
    TestRunner.run(RayCrossingCounterStressTest.class);
  }

  PrecisionModel pmFixed_1 = new PrecisionModel(1.0);
  private boolean isAllConsistent = true;
  private int testCount;
  private int failureCount;
  
  public RayCrossingCounterStressTest(String name) {
    super(name);
  }

  public void testTriangles() {
    checkTriangles(500, 100, 1000);
    System.out.println("Tests: " + testCount + "   Failures: " + failureCount);
    assertTrue(isAllConsistent);
  }

  private void checkTriangles(double maxHeight, double width, int numPts) {
    for (int i = 0; i < maxHeight; i++) {
      checkTriangleEdge(i, width, numPts);
    }
  }
  
  public void checkTriangleEdge(double height, double width, int numPts) {
    for (int i = 0; i < numPts; i++) {
      double lenFrac = i / (double) (numPts + 1);
      checkTriangle(height, width, lenFrac);
    }
  }
  
  private boolean checkTriangle(double height, double width, double lenFraction) {
    Coordinate[] triPts = new Coordinate[] {
        new Coordinate(0,0),
        new Coordinate(0, height),
        new Coordinate(width, 0),
        new Coordinate(0,0)
    };
    LineSegment seg = new LineSegment(0, height, width, 0);
    Coordinate pt = seg.pointAlong(lenFraction);
    
    return checkTriangleConsistent(triPts, pt);
  }

  boolean checkTriangleConsistent(Coordinate[] triPts, Coordinate pt) {
    testCount++;
    //boolean isPointInRing = CGAlgorithms.isPointInRing(pt, triPts);
    boolean isPointInRing = Location.INTERIOR == RayCrossingCounter.locatePointInRing(pt, triPts);
    //boolean isPointInRing = Location.INTERIOR == RayCrossingCounterDD.locatePointInRing(pt, triPts);
    
    int orientation = CGAlgorithms.orientationIndex(triPts[1], triPts[2], pt);
    
    // if collinear can't determine a failure
    if (orientation == CGAlgorithms.COLLINEAR) return true;
    
    boolean bothOutside = ! isPointInRing && orientation == CGAlgorithms.COUNTERCLOCKWISE;
    boolean bothInside = isPointInRing && orientation == CGAlgorithms.CLOCKWISE;
    boolean isConsistent = bothOutside || bothInside;
    
    if (! isConsistent) {
      isAllConsistent  = false;
      failureCount++;
      System.out.println("Inconsistent: "
          + "PIR=" + isPointInRing + " Orient=" + orientation
          + "  Pt: " + WKTWriter.toPoint(pt) 
          + "  seg: " + WKTWriter.toLineString(triPts[1], triPts[2]) 
          + "  tri: " + toPolygon(triPts) );
    }
    return isConsistent;
    
  }
  
  public static String toPolygon(Coordinate[] coord)
  {
    StringBuffer buf = new StringBuffer();
    buf.append("POLYGON ");
    if (coord.length == 0)
      buf.append(" EMPTY");
    else {
      buf.append("((");
      for (int i = 0; i < coord.length; i++) {
        if (i > 0)
          buf.append(", ");
        buf.append(coord[i].x + " " + coord[i].y );
      }
      buf.append("))");
    }
    return buf.toString();
  }

}
