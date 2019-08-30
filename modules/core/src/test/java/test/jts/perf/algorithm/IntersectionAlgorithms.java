package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.HCoordinate;
import org.locationtech.jts.algorithm.NotRepresentableException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.DD;
import org.locationtech.jts.precision.CommonBits;

public class IntersectionAlgorithms {
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
    DD pw = new DD(p1.x).selfMultiply(p2.y).selfSubtract(new DD(p2.x).selfMultiply(p1.y));

    DD qx = new DD(q1.y).selfSubtract(q2.y);
    DD qy = new DD(q2.x).selfSubtract(q1.x);
    DD qw = new DD(q1.x).selfMultiply(q2.y).selfSubtract(new DD(q2.x).selfMultiply(q1.y));

    DD x = py.multiply(qw).selfSubtract(qy.multiply(pw));
    DD y = qx.multiply(pw).selfSubtract(px.multiply(qw));
    DD w = px.multiply(qy).selfSubtract(qx.multiply(py));

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
  
  public static Coordinate intersectionCB(
      Coordinate p1, Coordinate p2,
      Coordinate q1, Coordinate q2)
      throws NotRepresentableException
    {
      Coordinate common = computeCommonCoord(p1, p2, q1, q2);
      p1 = subtractCoord(p1, common);
      p2 = subtractCoord(p2, common);
      q1 = subtractCoord(q1, common);
      q2 = subtractCoord(q2, common);

      // unrolled computation
      double px = p1.y - p2.y;
      double py = p2.x - p1.x;
      double pw = p1.x * p2.y - p2.x * p1.y;

      double qx = q1.y - q2.y;
      double qy = q2.x - q1.x;
      double qw = q1.x * q2.y - q2.x * q1.y;

      double x = py * qw - qy * pw;
      double y = qx * pw - px * qw;
      double w = px * qy - qx * py;

      double xInt = x/w;
      double yInt = y/w;

      if ((Double.isNaN(xInt)) || (Double.isInfinite(xInt)
        || Double.isNaN(yInt)) || (Double.isInfinite(yInt))) {
        throw new NotRepresentableException();
      }

      return new Coordinate(xInt + common.x, yInt + common.y);
    }

    private static Coordinate subtractCoord(Coordinate c0, Coordinate c1) {
      Coordinate res = c0.copy();
      res.x -= c1.x;
      res.y -= c1.y;
      return res;
    }

    private static Coordinate computeCommonCoord(Coordinate c0, Coordinate c1, Coordinate c2, Coordinate c3) {
      return new Coordinate(
        getCommonBits(c0.x, c1.x, c2.x, c3.x),
        getCommonBits(c0.y, c1.y, c2.y, c3.y));
    }

    private static double getCommonBits(double v0, double v1, double v2, double v3) {
      CommonBits cb = new CommonBits();
      cb.add(v0);
      cb.add(v1);
      cb.add(v2);
      cb.add(v3);
      return cb.getCommon();
    }
    
  public static Coordinate intersectionNorm(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) throws NotRepresentableException {
    Coordinate n1 = new Coordinate(p1);
    Coordinate n2 = new Coordinate(p2);
    Coordinate n3 = new Coordinate(q1);
    Coordinate n4 = new Coordinate(q2);
    Coordinate normPt = new Coordinate();
    normalizeToEnvCentre(n1, n2, n3, n4, normPt);

    Coordinate intPt = HCoordinate.intersection(n1, n2, n3, n4);

    intPt.x += normPt.x;
    intPt.y += normPt.y;

    return intPt;
  }

  /**
   * Normalize the supplied coordinates to so that the midpoint of their
   * intersection envelope lies at the origin.
   *
   * @param n00
   * @param n01
   * @param n10
   * @param n11
   * @param normPt
   */
  private static void normalizeToEnvCentre(Coordinate n00, Coordinate n01, Coordinate n10, Coordinate n11, Coordinate normPt) {
    double minX0 = n00.x < n01.x ? n00.x : n01.x;
    double minY0 = n00.y < n01.y ? n00.y : n01.y;
    double maxX0 = n00.x > n01.x ? n00.x : n01.x;
    double maxY0 = n00.y > n01.y ? n00.y : n01.y;

    double minX1 = n10.x < n11.x ? n10.x : n11.x;
    double minY1 = n10.y < n11.y ? n10.y : n11.y;
    double maxX1 = n10.x > n11.x ? n10.x : n11.x;
    double maxY1 = n10.y > n11.y ? n10.y : n11.y;

    double intMinX = minX0 > minX1 ? minX0 : minX1;
    double intMaxX = maxX0 < maxX1 ? maxX0 : maxX1;
    double intMinY = minY0 > minY1 ? minY0 : minY1;
    double intMaxY = maxY0 < maxY1 ? maxY0 : maxY1;

    double intMidX = (intMinX + intMaxX) / 2.0;
    double intMidY = (intMinY + intMaxY) / 2.0;
    normPt.x = intMidX;
    normPt.y = intMidY;

    /*
     * // equilavalent code using more modular but slower method Envelope env0 =
     * new Envelope(n00, n01); Envelope env1 = new Envelope(n10, n11); Envelope
     * intEnv = env0.intersection(env1); Coordinate intMidPt = intEnv.centre();
     * 
     * normPt.x = intMidPt.x; normPt.y = intMidPt.y;
     */

    n00.x -= normPt.x;
    n00.y -= normPt.y;
    n01.x -= normPt.x;
    n01.y -= normPt.y;
    n10.x -= normPt.x;
    n10.y -= normPt.y;
    n11.x -= normPt.x;
    n11.y -= normPt.y;
  }
}
