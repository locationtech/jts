package test.jts.perf.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.algorithm.locate.YStripesPointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class YStripesPointInAreaPerfTest extends PerformanceTestCase {
  public static void main(String args[]) {
    PerformanceTestRunner.run(YStripesPointInAreaPerfTest.class);
  }
  
  public YStripesPointInAreaPerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 100_000 });
    setRunIterations(1);
  }
  
  List<Coordinate> coords;
  Polygon polygon;
  
  public void startRun(int num)
  {
    System.out.println("Running with size " + num);
    GeometricShapeFactory factory = new GeometricShapeFactory();
    factory.setSize(100);
    polygon = factory.createCircle();
    
    coords = new ArrayList<>();
    Random rand = new Random(1324);
    for (int i = 0; i < num; i++) {
      coords.add(new Coordinate(rand.nextDouble()*100, rand.nextDouble()*100));
    }
  }
  
  public void runParallel() {
    for (int i = 0; i < 1000; i++) {
      PointOnGeometryLocator locator = new YStripesPointInAreaLocator(polygon);
      coords.parallelStream().forEach(c -> isInside(locator, c));
    }
  }
      
  private boolean isInside(PointOnGeometryLocator locator, Coordinate coord) {
    return locator.locate(coord) == Location.INTERIOR;
  }
}
