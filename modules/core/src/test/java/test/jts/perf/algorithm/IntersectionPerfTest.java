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
    Coordinate intPt = intersectionDD(p0, p1, q0, q1);
  }
  
  public void runDDWithFilter() throws NotRepresentableException 
  {
    Coordinate intPt = intersectionDDWithFilter(p0, p1, q0, q1);
  }
  
  
  
  public void runDP_easy() throws NotRepresentableException
  {
    Coordinate intPt = HCoordinate.intersection(a0, a1, b0, b1);
  }
  
  public void runDDFast_easy() throws NotRepresentableException
  {
    Coordinate intPt = intersectionDD(a0, a1, b0, b1);
  }
  
  public void runDDWithFilter_easy() throws NotRepresentableException 
  {
    Coordinate intPt = intersectionDDWithFilter(a0, a1, b0, b1);
  }
  
  /**
   * DD version of {@link HCoordinate#intersection(Coordinate, Coordinate, Coordinate, Coordinate)}.
   * Slightly simpler and faster (25%) than {@link CGAlgorithmsDD#intersection(Coordinate, Coordinate, Coordinate, Coordinate)}.
   * 
   * @param p1
   * @param p2
   * @param q1
   * @param q2
   * @return
   * @throws NotRepresentableException
   */
  public static Coordinate intersectionDD(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2)
      throws NotRepresentableException {
    // unrolled computation
    DD px = new DD(p1.y).selfSubtract(p2.y);
    DD py = new DD(p2.x).selfSubtract(p1.x);
    DD pw = new DD(p1.x).selfMultiply(p2.y).subtract(new DD(p2.x).selfMultiply(p1.y));

    DD qx = new DD(q1.y).selfSubtract(q2.y);
    DD qy = new DD(q2.x).selfSubtract(q1.x);
    DD qw = new DD(q1.x).selfMultiply(q2.y).subtract(new DD(q2.x).selfMultiply(q1.y));

    DD x = py.multiply(qw).subtract(qy.multiply(pw));
    DD y = qx.multiply(pw).subtract(px.multiply(qw));
    DD w = px.multiply(qy).subtract(qx.multiply(py));

    double xInt = x.selfDivide(w).doubleValue();
    double yInt = y.selfDivide(w).doubleValue();

    if ((Double.isNaN(xInt)) || (Double.isInfinite(xInt) || Double.isNaN(yInt)) || (Double.isInfinite(yInt))) {
      throw new NotRepresentableException();
    }

    return new Coordinate(xInt, yInt);
  }
  
  public static Coordinate intersectionDDWithFilter(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2)
  {
    Coordinate intPt = intersectionDDFilter(p1, p2, q1, q2);
    if (intPt != null) 
      return intPt;
    try {
      return intersectionDD(p1, p2, q1, q2);
    } catch (NotRepresentableException e) {
      return null;
    }
  }
  
  private static final double FILTER_TOL = 1.0E-6;
  
  public static Coordinate intersectionDDFilter(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2)
  {
    // Compute using DP math
    Coordinate intPt = null;
    try {
      intPt = HCoordinate.intersection(p1, p2, q1, q2);
    } catch (NotRepresentableException e) {
      return null;
    }
    if (Distance.pointToLinePerpendicular(intPt, p1, p2) > FILTER_TOL) return null;
    if (Distance.pointToLinePerpendicular(intPt, q1, q2) > FILTER_TOL) return null;
    return intPt;
  }
}
