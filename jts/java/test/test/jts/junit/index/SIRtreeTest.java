
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
import java.util.List;

import junit.framework.TestCase;

import com.vividsolutions.jts.index.strtree.AbstractNode;
import com.vividsolutions.jts.index.strtree.SIRtree;


/**
 * @version 1.7
 */
public class SIRtreeTest extends TestCase {
  public SIRtreeTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {SIRtreeTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  private static class TestTree extends SIRtree {
    public TestTree(int nodeCapacity) { super(nodeCapacity); }
    public AbstractNode getRoot() { return super.getRoot(); }
    protected List boundablesAtLevel(int level) { return super.boundablesAtLevel(level); }
  }

  public void test() {
    TestTree t = new TestTree(2);
    t.insert(2, 6, "A");
    t.insert(2, 4, "B");
    t.insert(2, 3, "C");
    t.insert(2, 4, "D");
    t.insert(0, 1, "E");
    t.insert(2, 4, "F");
    t.insert(5, 6, "G");
    t.build();
    assertEquals(2, t.getRoot().getLevel());
    assertEquals(4, t.boundablesAtLevel(0).size());
    assertEquals(2, t.boundablesAtLevel(1).size());
    assertEquals(1, t.boundablesAtLevel(2).size());
    assertEquals(1, t.query(0.5, 0.5).size());
    assertEquals(0, t.query(1.5, 1.5).size());
    assertEquals(2, t.query(4.5, 5.5).size());
  }

  public void testEmptyTree() {
    TestTree t = new TestTree(2);
    t.build();
    assertEquals(0, t.getRoot().getLevel());
    assertEquals(1, t.boundablesAtLevel(0).size());
    assertEquals(0, t.boundablesAtLevel(1).size());
    assertEquals(0, t.boundablesAtLevel(-1).size());
    assertEquals(0, t.query(0.5, 0.5).size());
  }
}
