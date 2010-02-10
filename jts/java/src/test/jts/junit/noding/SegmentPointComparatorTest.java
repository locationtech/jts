package test.jts.junit.noding;

import junit.framework.TestCase;

import com.vividsolutions.jts.noding.*;
import com.vividsolutions.jts.geom.Coordinate;
/**
 * Test IntersectionSegment#compareNodePosition
 *
 * @version 1.7
 */
public class SegmentPointComparatorTest
 extends TestCase
{

  public SegmentPointComparatorTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(SegmentPointComparatorTest.class);
  }

  public void testOctant0()
  {
    checkNodePosition(0, 1, 1, 2, 2, -1);
    checkNodePosition(0, 1, 0, 1, 1, -1);
  }

  private void checkNodePosition(int octant,
      double x0, double y0,
    double x1, double y1,
    int expectedPositionValue
    )
  {
    int posValue = SegmentPointComparator.compare(octant,
        new Coordinate(x0, y0),
        new Coordinate(x1, y1)
        );
    assertTrue(posValue == expectedPositionValue);
  }
}
