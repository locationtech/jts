package test.jts.perf.operation.distance;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class PointPointDistancePerfTest extends PerformanceTestCase {


  public static void main(String args[]) {
    PerformanceTestRunner.run(PointPointDistancePerfTest.class);
  }

  private Point[] grid;

  public PointPointDistancePerfTest(String name) {
    super(name);
    setRunSize(new int[] {10000});
    setRunIterations(1);
  }
  
  public void startRun(int npts)
  {
    System.out.println("\n-------  Running with # pts = " + npts);
    grid = createPointGrid(new Envelope(0, 10., 0, 10), npts);
  }

  private Point[] createPointGrid(Envelope envelope, int npts) {
    List<Point> geoms = new ArrayList<Point>();
    GeometryFactory fact = new GeometryFactory();
    int nSide = (int) Math.sqrt(npts);
    double xInc = envelope.getWidth() / nSide;
    double yInc = envelope.getHeight() / nSide;
    for (int i = 0; i < nSide; i++) {
      for (int j = 0; j < nSide; j++) {
        double x = envelope.getMinX() + i * xInc;
        double y = envelope.getMinY() + i * yInc;
        Point p = fact.createPoint(new Coordinate(x, y));
        geoms.add(p);
      }
    }
    return GeometryFactory.toPointArray(geoms);
  }
  
  public void runPoints() {
    for (Geometry p1 : grid) {
      for (Geometry p2 : grid) {
        double dist = p1.distance(p2);
      }
    }
  }

}
