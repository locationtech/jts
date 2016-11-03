
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.index;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.AbstractNode;
import org.locationtech.jts.index.strtree.Boundable;
import org.locationtech.jts.index.strtree.STRtree;



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
