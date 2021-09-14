package org.locationtech.jts.triangulate.tri;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class TriTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(TriTest.class);
  }
  
  private static Tri triCentre = createSimpleTriangulation();
  private static Tri tri0;
  private static Tri tri1;
  private static Tri tri2;
  
  public TriTest(String name) {
    super(name);
  }

  public void testAdjacent() {
    assertTrue(tri0 == triCentre.getAdjacent(0));
    assertTrue(tri1 == triCentre.getAdjacent(1));
    assertTrue(tri2 == triCentre.getAdjacent(2));
  }

  public void testMidpoint() {
    Tri tri = tri(0,0,  0,10, 10,0  );
    checkEqualXY(new Coordinate(0, 5), tri.midpoint(0));
    checkEqualXY(new Coordinate(5, 5), tri.midpoint(1));
    checkEqualXY(new Coordinate(5, 0), tri.midpoint(2));
  }

  public void testCoordinateIndex() {
    Tri tri = tri(0,0,  0,10, 10,0  );
    assertEquals(0, tri.getIndex(new Coordinate(0,0)));
    assertEquals(1, tri.getIndex(new Coordinate(0,10)));
    assertEquals(2, tri.getIndex(new Coordinate(10,0)));
  }

  private static Tri tri(double x0, double y0, double x1, double y1, double x2, double y2) {
    Tri tri = Tri.create(
        new Coordinate(x0, y0),
        new Coordinate(x1, y1),
        new Coordinate(x2, y2));
    assertTrue( Orientation.CLOCKWISE == Orientation.index(
        tri.getCoordinate(0), tri.getCoordinate(1), tri.getCoordinate(2) ));
    return tri;
  }
  
  private static Tri createSimpleTriangulation() {
    Tri tri = tri(10,10,  10,20, 20,10  );
    tri0 = tri(10,20,  10,10, 0,10  );
    tri1 = tri(20,10,  10,20, 20,20  );
    tri2 = tri(10,10,  20,10, 10,0  );
    build(tri, tri0, tri1, tri2);
    return tri;
  }

  private static void build(Tri... tri) {
    List<Tri> triList = new ArrayList<Tri>();
    for (int i = 0; i < tri.length; i++) {
      triList.add(tri[i]);
    }
    TriangulationBuilder.build(triList);
  }
}
