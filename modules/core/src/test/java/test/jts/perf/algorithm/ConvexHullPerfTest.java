package test.jts.perf.algorithm;

import java.util.ArrayList;
import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.util.GeometricShapeFactory;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class ConvexHullPerfTest extends PerformanceTestCase {
  public static void main(String args[]) {
    PerformanceTestRunner.run(ConvexHullPerfTest.class);
  }

  private MultiPoint geom;
  
  public ConvexHullPerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 1000, 10_000, 100_000, 1_000_000 });
    setRunIterations(100);
  }
 
  @Override
  public void startRun(int num)
  {
    System.out.println("Running with size " + num);
    geom = createRandomMultiPoint(num);
  }

  private MultiPoint createRandomMultiPoint(int num) {
    Coordinate[] pts = new Coordinate[num];
    Random rand = new Random(1324);
    for (int i = 0; i < num; i++) {
      pts[i] = new Coordinate(rand.nextDouble()*100, rand.nextDouble()*100);
    }
    GeometryFactory fact = new GeometryFactory();
    return fact.createMultiPointFromCoords(pts);
  }
  
  public void runConvexHull() {
    Geometry convextHull = geom.convexHull();
  }
}
