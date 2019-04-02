package org.locationtech.jtslab.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtslab.noding.anchorpoint.AnchorPointNodingTest;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AnchorPointRobustLineIntersectorTest extends TestCase
{

  public static void main(String[] args) {
    TestRunner.run(AnchorPointNodingTest.class);
  }

  public AnchorPointRobustLineIntersectorTest(String name) { super(name); }

  public void testIntersectionAnchorToVertex() {

    Coordinate p0 = new Coordinate(0, 0);
    Coordinate p1 = new Coordinate(10, 0);
    Coordinate q0 = new Coordinate(8, 5);
    Coordinate q1 = new Coordinate(7, -5);

    AnchorPointRobustLineIntersector li = new AnchorPointRobustLineIntersector(3);
    AddVertexAnchorPoints(li.getAnchorPoints(),new Coordinate[] { p0, p1, q0, q1 });

    li.setPrecisionModel(new PrecisionModel(10));
    Coordinate res = li.intersection(p0, p1, q0, q1);
    assertEquals("equal", p1, res);
    assertTrue("reference equal", p1 == res);

    List apNodes = li.getAnchorPoints().query(new Envelope(res));
    assertEquals(1, apNodes.size());
    assertTrue(((AnchorPoint)((KdNode)apNodes.get(0)).getData()).getFromVertex());

  }

  public void testReuseAnchorPoint() {

    Coordinate p0 = new Coordinate(0, 0);
    Coordinate p1 = new Coordinate(10, 0);
    Coordinate q0 = new Coordinate(8, 5);
    Coordinate q1 = new Coordinate(7, -5);

    Coordinate r0 = new Coordinate(7.5, 0);
    AnchorPointRobustLineIntersector li = new AnchorPointRobustLineIntersector(1);
    AddVertexAnchorPoints(li.getAnchorPoints(),new Coordinate[] { p0, p1, q0, q1 });

    li.setPrecisionModel(new PrecisionModel(10));
    Coordinate res = li.intersection(p0, p1, q0, q1);
    assertEquals("equal", r0, res);

    List apNodes = li.getAnchorPoints().query(new Envelope(res));
    assertEquals(1, apNodes.size());
    assertTrue(!((AnchorPoint)((KdNode)apNodes.get(0)).getData()).getFromVertex());

    Coordinate q2 = new Coordinate(8, -5);
    AddVertexAnchorPoints(li.getAnchorPoints(), new Coordinate[] {q2});
    Coordinate res2 = li.intersection(p0, p1, q0, q2);
    assertTrue("reference equal", res == res2);
  }

  private static void AddVertexAnchorPoints(KdTree res, Coordinate[] coordinates) {
    for (int i = 0; i < coordinates.length; i++)
      res.insert(coordinates[i], new AnchorPoint(coordinates[i], true));
  }

  public void testIntersectionOfLongLinesRotatedAroundCommonCenter()
  {
    LineIntersector rli = new RobustLineIntersector();
    rli.setPrecisionModel(new PrecisionModel());
    int numIntersectionsRli1  = computeIntersectionPoints(rli);
    rli.setPrecisionModel(new PrecisionModel(1E4));
    int numIntersectionsRli2  = computeIntersectionPoints(rli);
    int numIntersectionsApLi = computeIntersectionPoints(new AnchorPointRobustLineIntersector(1E-5));
    assertTrue(numIntersectionsRli1 > numIntersectionsRli2);
    assertEquals(1, numIntersectionsApLi);
    assertEquals(1, numIntersectionsRli2);

  }

  private static final Random rnd = new Random(17);
  private static final int numRotations = 10;

  private static final Coordinate p0 = new Coordinate(-rnd.nextDouble() * 1E10, rnd.nextDouble());
  private static final Coordinate p1 = new Coordinate(rnd.nextDouble()*1E10, p0.y);


  private static int computeIntersectionPoints(LineIntersector li) {

    Set<Coordinate> coordinates = new HashSet<>();

    AffineTransformation af = AffineTransformation.rotationInstance(1d / 18d * Math.PI, 0.5 * (p0.x+p1.x), p0.y);

    Coordinate p0s[] = new Coordinate[numRotations];
    Coordinate p1s[] = new Coordinate[numRotations];

    Coordinate tmpP0 = p0.copy();
    Coordinate tmpP1 = p1.copy();
    for (int i = 0; i < numRotations; i++) {
      p0s[i] = tmpP0.copy();
      p1s[i] = tmpP1.copy();
      af.transform(tmpP0, tmpP0);
      af.transform(tmpP1, tmpP1);
    }

    for (int i = 0; i < numRotations; i++)
      for (int j = 0; j < numRotations; j++) {
        if (i == j) continue;
        li.computeIntersection(p0s[i], p1s[i], p0s[j], p1s[j]);
        assertTrue("proper intersection", li.hasIntersection() && li.isProper());
        Coordinate c = li.getIntersection(0);
        if (!coordinates.contains(c))
          coordinates.add(c);
      }
    return coordinates.size();
  }

  public void testUnionOfLongLinesRotatedAroundCommonCenter() {

    RobustLineIntersector rli = new RobustLineIntersector();
    try { computeUnions(rli, new PrecisionModel()); }
    catch (TopologyException tpe)
    { System.out.println("Union with RobustLineIntersector and floating PrecisionModel failed"); }

    try { computeUnions(rli, new PrecisionModel(1e2)); }
    catch (TopologyException tpe )
    { System.out.println("Union with RobustLineIntersector and PrecisionModel(1E2) failed"); }

    Geometry g = null;
    double minDistanceToAnchorPoint = 1E-10;

    while (true)
    {
      g = null;
      try { g = computeUnions(new AnchorPointRobustLineIntersector(minDistanceToAnchorPoint), new PrecisionModel()); }
      catch (Exception e)
      { System.out.println("Union with AnchorPointRobustLineIntersector (mapd="+ minDistanceToAnchorPoint +") failed"); }

      // check if ok
      if (g != null) {
        System.out.println("Union with AnchorPointRobustLineIntersector (mapd="+ minDistanceToAnchorPoint +"): " + g.getNumGeometries());
        if (g.getNumGeometries() == 20)
          break;
      }

      // increase min. distance to anchor point.
      minDistanceToAnchorPoint *= 10;

      if (minDistanceToAnchorPoint > 1)
        fail("Union with AnchorPointRobustLineIntersector failed");
    }
    assertEquals("number of geometries", 20, g.getNumGeometries());

    System.out.println("Union with AnchorPointRobustLineIntersector (mapd="+ minDistanceToAnchorPoint + ") passed");
  }

  private static Geometry computeUnions(LineIntersector li, PrecisionModel pm)
  {
    li.setPrecisionModel(pm);
    GeometryFactory f = new GeometryFactory(pm);

    Coordinate q0 = p0.copy();
    Coordinate q1 = p1.copy();
    AffineTransformation af = AffineTransformation.rotationInstance(1d / 18d * Math.PI, 0.5 * (p0.x+p1.x), p0.y);

    Geometry g = f.createLineString(new Coordinate[] {q0, q1});
    for (int i = 1; i < numRotations; i++) {
      q0 = q0.copy();
      q1 = q1.copy();
      af.transform(q0, q0);
      af.transform(q1, q1);
      Geometry gtmp = f.createLineString(new Coordinate[] {q0, q1});
      g = OverlayOp.overlayOp(li, g, gtmp, OverlayOp.UNION);
    }
    return g;
  }
}
