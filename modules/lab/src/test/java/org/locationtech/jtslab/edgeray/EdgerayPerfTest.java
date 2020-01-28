package org.locationtech.jtslab.edgeray;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.SineStarFactory;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class EdgerayPerfTest extends PerformanceTestCase
{
  public static void main(String args[]) {
    PerformanceTestRunner.run(EdgerayPerfTest.class);
  }
  boolean verbose = true;
  private Geometry geom1;
  private Geometry geom2;
  
  public EdgerayPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 100, 1000, 2000 });
    setRunIterations(1);
  }

  public void startRun(int size)
  {
    System.out.println("\n---  Running with size " + size + "  -----------");
    iter = 0;
    geom1 = createSineStar(size, 0);
    geom2 = createSineStar(size, 10);

  }
  
  private int iter = 0;
  
  public void runEdgeRayArea()
  {
    //System.out.println("Test 1 : Iter # " + iter++);
    double area = EdgeRayIntersectionArea.area(geom1, geom2);
    System.out.println("EdgeRay area = " + area);
  }
  
  public void runIntersectionArea()
  {
    double area = geom1.intersection(geom2).getArea();
    System.out.println("Overlay area = " + area);
  }
  
  Geometry createSineStar(int nPts, double offset)
  {
    SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(new Coordinate(0, offset));
    gsf.setSize(100);
    gsf.setNumPoints(nPts);
    
    Geometry g = gsf.createSineStar();
    
    return g;
    
  }
}
