
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
package test.jts.index;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.AbstractNode;
import com.vividsolutions.jts.index.strtree.Boundable;
import com.vividsolutions.jts.index.strtree.STRtree;


/**
 * @version 1.7
 */
public class STRtreeDemo {

  public STRtreeDemo() {
  }

  public static class TestTree extends STRtree {
    public TestTree(int nodeCapacity) {
      super(nodeCapacity);
    }
    public List boundablesAtLevel(int level) { return super.boundablesAtLevel(level); }
    public AbstractNode getRoot() { return root; }
    public List createParentBoundables(List verticalSlice, int newLevel) {
      return super.createParentBoundables(verticalSlice, newLevel);
    }
    public List[] verticalSlices(List childBoundables, int size) {
      return super.verticalSlices(childBoundables, size);
    }
    public List createParentBoundablesFromVerticalSlice(List childBoundables, int newLevel) {
      return super.createParentBoundablesFromVerticalSlice(childBoundables, newLevel);
    }
  }

  private static void initTree(TestTree t, List sourceEnvelopes) {
    for (Iterator i = sourceEnvelopes.iterator(); i.hasNext(); ) {
      Envelope sourceEnvelope = (Envelope) i.next();
      t.insert(sourceEnvelope, sourceEnvelope);
    }
    t.build();
  }

  public static void main(String[] args) throws Exception {
    List envelopes = sourceData();
    TestTree t = new TestTree(NODE_CAPACITY);
    initTree(t, envelopes);
    PrintStream printStream = System.out;
    printSourceData(envelopes, printStream);
    printLevels(t, printStream);
  }

  public static void printSourceData(List sourceEnvelopes, PrintStream out) {
    out.println("============ Source Data ============\n");
    out.print("GEOMETRYCOLLECTION(");
    boolean first = true;
    for (Iterator i = sourceEnvelopes.iterator(); i.hasNext(); ) {
      Envelope e = (Envelope) i.next();
      Geometry g = factory.createPolygon(factory.createLinearRing(new Coordinate[] {
        new Coordinate(e.getMinX(), e.getMinY()), new Coordinate(e.getMinX(), e.getMaxY()),
        new Coordinate(e.getMaxX(), e.getMaxY()), new Coordinate(e.getMaxX(), e.getMinY()),
        new Coordinate(e.getMinX(), e.getMinY()) }), null);
      if (first) {
        first = false;
      }
      else {
        out.print(",");
      }
      out.print(g);
    }
    out.println(")\n");
  }

  private static List sourceData() {
    ArrayList envelopes = new ArrayList();
    for (int i = 0; i < ITEM_COUNT; i++) {
      envelopes.add(randomRectangle().getEnvelopeInternal());
    }
    return envelopes;
  }

  private static final double EXTENT = 100;
  private static final double MAX_ITEM_EXTENT = 15;
  private static final double MIN_ITEM_EXTENT = 3;
  private static final int ITEM_COUNT = 20;
  private static final int NODE_CAPACITY = 4;
  private static GeometryFactory factory = new GeometryFactory();

  private static Polygon randomRectangle() {
    double width = MIN_ITEM_EXTENT + ((MAX_ITEM_EXTENT-MIN_ITEM_EXTENT) * Math.random());
    double height = MIN_ITEM_EXTENT + ((MAX_ITEM_EXTENT-MIN_ITEM_EXTENT) * Math.random());
    double bottom = EXTENT * Math.random();
    double left = EXTENT * Math.random();
    double top = bottom + height;
    double right = left + width;
    return factory.createPolygon(factory.createLinearRing(new Coordinate[]{
          new Coordinate(left, bottom), new Coordinate(right, bottom),
          new Coordinate(right, top), new Coordinate(left, top),
          new Coordinate(left, bottom) }), null);
  }

  public static void printLevels(TestTree t, PrintStream out) {
    for (int i = 0; i <= t.getRoot().getLevel(); i++) {
      printBoundables(t.boundablesAtLevel(i), "Level " + i, out);
    }
  }

  public static void printBoundables(List boundables, String title, PrintStream out) {
    out.println("============ " + title + " ============\n");
    out.print("GEOMETRYCOLLECTION(");
    boolean first = true;
    for (Iterator i = boundables.iterator(); i.hasNext(); ) {
      Boundable boundable = (Boundable) i.next();
      if (first) {
        first = false;
      }
      else {
        out.print(",");
      }
      out.print(toString(boundable));
    }
    out.println(")\n");
  }

  private static String toString(Boundable b) {
    return "POLYGON(("
         + envelope(b).getMinX() + " "
         + envelope(b).getMinY() + ", "
         + envelope(b).getMinX() + " "
         + envelope(b).getMaxY() + ", "
         + envelope(b).getMaxX() + " "
         + envelope(b).getMaxY() + ", "
         + envelope(b).getMaxX() + " "
         + envelope(b).getMinY() + ","
         + envelope(b).getMinX() + " "
         + envelope(b).getMinY() + "))";
  }

  private static Envelope envelope(Boundable b) {
    return (Envelope)b.getBounds();
  }

}
