package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.HCoordinate;
import org.locationtech.jts.algorithm.NotRepresentableException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.DD;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class IntersectionPerfTest extends PerformanceTestCase {
  private static final int N_ITER = 1000000;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(IntersectionPerfTest.class);
  }
  
  public IntersectionPerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 1 });
    setRunIterations(N_ITER);
  }
  
  Coordinate a0 = new Coordinate(0, 0);
  Coordinate a1 = new Coordinate(10, 0);
  Coordinate b0 = new Coordinate(20, 10);
  Coordinate b1 = new Coordinate(20, 20);
  
  Coordinate p0;
  Coordinate p1;
  Coordinate q0;
  Coordinate q1;
  
  public void startRun(int npts)
  {
    p0 = new Coordinate(35613471.6165017, 4257145.3061322933);
    p1 = new Coordinate(35613477.7705378, 4257160.5282227108);
    q0 = new Coordinate(35613477.775057241, 4257160.5396535359);
    q1 = new Coordinate(35613479.856073894, 4257165.9236917039);
  }
  
  public void runDP() throws NotRepresentableException
  {
    Coordinate intPt = HCoordinate.intersection(p0, p1, q0, q1);
  }
  
  public void runDD() 
  {
    Coordinate intPt = CGAlgorithmsDD.intersection(p0, p1, q0, q1);
  }
  
  public void runDDFast() throws NotRepresentableException 
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionDD(p0, p1, q0, q1);
  }
  
  public void runDDWithFilter() throws NotRepresentableException 
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionDDWithFilter(p0, p1, q0, q1);
  }
  
  public void runCB() throws NotRepresentableException 
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionCB(p0, p1, q0, q1);
  }
  
  public void runNorm() throws NotRepresentableException 
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionNorm(p0, p1, q0, q1);
  }
  
  
  
  public void runDP_easy() throws NotRepresentableException
  {
    Coordinate intPt = HCoordinate.intersection(a0, a1, b0, b1);
  }
  
  public void runDDFast_easy() throws NotRepresentableException
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionDD(a0, a1, b0, b1);
  }
  
  public void runDDWithFilter_easy() throws NotRepresentableException 
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionDDWithFilter(a0, a1, b0, b1);
  }
  

}
