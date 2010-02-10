
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts.junit.index;
import junit.framework.TestCase;

import com.vividsolutions.jts.index.strtree.Interval;


/**
 * @version 1.7
 */
public class IntervalTest extends TestCase {

  public IntervalTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {IntervalTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  public void testIntersectsBasic() {
    assertTrue(new Interval(5, 10).intersects(new Interval(7, 12)));
    assertTrue(new Interval(7, 12).intersects(new Interval(5, 10)));
    assertTrue(! new Interval(5, 10).intersects(new Interval(11, 12)));
    assertTrue(! new Interval(11, 12).intersects(new Interval(5, 10)));
    assertTrue(new Interval(5, 10).intersects(new Interval(10, 12)));
    assertTrue(new Interval(10, 12).intersects(new Interval(5, 10)));
  }

  public void testIntersectsZeroWidthInterval() {
    assertTrue(new Interval(10, 10).intersects(new Interval(7, 12)));
    assertTrue(new Interval(7, 12).intersects(new Interval(10, 10)));
    assertTrue(! new Interval(10, 10).intersects(new Interval(11, 12)));
    assertTrue(! new Interval(11, 12).intersects(new Interval(10, 10)));
    assertTrue(new Interval(10, 10).intersects(new Interval(10, 12)));
    assertTrue(new Interval(10, 12).intersects(new Interval(10, 10)));
  }

  public void testCopyConstructor() {
    assertEquals(new Interval(3, 4), new Interval(3, 4));
    assertEquals(new Interval(3, 4), new Interval(new Interval(3, 4)));
  }

  public void testGetCentre() {
    assertEquals(6.5, new Interval(4, 9).getCentre(), 1E-10);
  }

  public void testExpandToInclude() {
    assertEquals(new Interval(3, 8), new Interval(3, 4)
                 .expandToInclude(new Interval(7, 8)));
    assertEquals(new Interval(3, 7), new Interval(3, 7)
                 .expandToInclude(new Interval(4, 5)));
    assertEquals(new Interval(3, 8), new Interval(3, 7)
                 .expandToInclude(new Interval(4, 8)));
  }

}
