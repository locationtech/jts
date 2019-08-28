package test.jts.perf.algorithm;

import java.util.Random;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.HCoordinate;
import org.locationtech.jts.algorithm.NotRepresentableException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;

public class IntersectionStressTest {
  
  private static final int MAX_ITER = 1000;
  private static final double MAX_ORD = 1000000;
  private static final double SEG_LEN = 100;
  // make results reproducible
  static Random randGen = new Random(123456);

  public static void main(String args[]) {
    IntersectionStressTest test = new IntersectionStressTest();
    test.run();
  }

  private void run() {
    for (int i = 0; i < MAX_ITER; i++) {
      try {
        doIntersectionTest(i);
      } catch (NotRepresentableException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private void doIntersectionTest(int i) throws NotRepresentableException {
    Coordinate basePt = randomCoordinate();
    
    double baseAngle = 2 * Math.PI * randGen.nextDouble();
    
    Coordinate p1 = computeVector(basePt, baseAngle, SEG_LEN);
    Coordinate p2 = computeVector(basePt, baseAngle, 2 * SEG_LEN);
    
    double angleTest = baseAngle + 0.99 * Math.PI; 
    
    Coordinate q1 = computeVector(basePt, angleTest, SEG_LEN);
    Coordinate q2 = computeVector(basePt, angleTest, 2 * SEG_LEN);
    
    System.out.println(i + ":  Lines: "
        + WKTWriter.toLineString(p1, p2) + "  -  "
        + WKTWriter.toLineString(q1, q2 ) );
    
    Coordinate intPt = HCoordinate.intersection(p1, p2, q1, q2);
    Coordinate intPtDD = IntersectionPerfTest.intersectionDD(p1, p2, q1, q2);
    //Coordinate intPtDD = IntersectionPerfTest.intersectionDDWithFilter(p1, p2, q1, q2);
    printStats("DP", intPt, p1, p2, q1, q2);
    printStats("DD", intPtDD, p1, p2, q1, q2);
  }
  
  private void printStats(String tag, Coordinate intPt, Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
    double distP = Distance.pointToLinePerpendicular(intPt, p1, p2);    
    double distQ = Distance.pointToLinePerpendicular(intPt, q1, q2);
    System.out.println(tag + " : Dist P = " + distP + "    Dist Q = " + distQ);
  }

  private Coordinate computeVector(Coordinate basePt, double angle, double len) {
    double x = basePt.getX() + len * Math.cos(angle);
    double y = basePt.getY() + len * Math.sin(angle);
    return new Coordinate(x, y);
  }

  private Coordinate randomCoordinate() {
    double x = MAX_ORD * randGen.nextDouble();
    double y = MAX_ORD * randGen.nextDouble();
    return new Coordinate(x, y);
  }
}
