
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
package org.locationtech.jts.operation.buffer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.operation.buffer.validate.BufferResultValidator;
import org.locationtech.jts.util.StringUtil;

import junit.framework.Assert;


/**
 * @version 1.7
 */
public class BufferValidator 
{

  
  public static void main(String[] args) throws Exception {
    Geometry g =
      new WKTReader().read(
        "MULTILINESTRING (( 635074.5418406526 6184832.4888257105, 635074.5681951842 6184832.571842485, 635074.6472587794 6184832.575795664 ), ( 635074.6657069515 6184832.53889932, 635074.6933792098 6184832.451929366, 635074.5642420045 6184832.474330718 ))");
    //System.out.println(g);
    //System.out.println(g.buffer(0.01, 100));
    //System.out.println("END");
  }


  private static abstract class Test implements Comparable {
    private String name;
    public Test(String name) {
      this(name, 2);
    }
    public Test(String name, int priority) {
      this.name = name;
      this.priority = priority;
    }
    public String getName() {
      return name;
    }
    public String toString() {
      return getName();
    }
    public abstract void test() throws Exception;
    private int priority;
    public int compareTo(Object o) {
      return priority - ((Test) o).priority;
    }
  }

  private Geometry original;
  private double bufferDistance;
  private Map nameToTestMap = new HashMap();
  private Geometry buffer;
  private static final int QUADRANT_SEGMENTS_1 = 100;
  private static final int QUADRANT_SEGMENTS_2 = 50;
  private String wkt;
  private GeometryFactory geomFact = new GeometryFactory();
  private WKTWriter wktWriter = new WKTWriter();
  private WKTReader wktReader;


  public BufferValidator(double bufferDistance, String wkt)
  throws ParseException {
    this(bufferDistance, wkt, true);
  }

  public BufferValidator(double bufferDistance, String wkt, boolean addContainsTest)
  throws ParseException {
    // SRID = 888 is to test that SRID is preserved in computed buffers
    setFactory(new PrecisionModel(), 888);
    this.bufferDistance = bufferDistance;
    this.wkt = wkt;
    if (addContainsTest) addContainsTest();
    //addBufferResultValidatorTest();
  }


  public void test() throws Exception {
    try {
      Collection tests = nameToTestMap.values();
      for (Iterator i = tests.iterator();
        i.hasNext();
        ) {
        Test test = (Test) i.next();
        test.test();
      }
    } catch (Exception e) {
      throw new Exception(
        supplement(e.toString()) + StringUtil.getStackTrace(e));
    }
  }
  private String supplement(String message) throws ParseException {
    String newMessage = "\n" + message + "\n";
    newMessage += "Original: " + wktWriter.writeFormatted(getOriginal()) + "\n";
    newMessage += "Buffer Distance: " + bufferDistance + "\n";
    newMessage += "Buffer: " + wktWriter.writeFormatted(getBuffer()) + "\n";
    return newMessage.substring(0, newMessage.length() - 1);
  }

  private BufferValidator addTest(Test test) {
    nameToTestMap.put(test.getName(), test);
    return this;
  }
  public BufferValidator setExpectedArea(final double expectedArea) {
    return addTest(new Test("Area Test") {
      public void test() throws Exception {
        double tolerance =
          Math.abs(
            getBuffer().getArea()
              - getOriginal()
                .buffer(
                  bufferDistance,
                  QUADRANT_SEGMENTS_1 - QUADRANT_SEGMENTS_2)
                .getArea());
        Assert.assertEquals(
          getName(),
          expectedArea,
          getBuffer().getArea(),
          tolerance);
      }
    });
  }

  public BufferValidator setEmptyBufferExpected(final boolean emptyBufferExpected) {
    return addTest(new Test("Empty Buffer Test", 1) {
      public void test() throws Exception {
        Assert.assertTrue(
          supplement(
            "Expected buffer "
              + (emptyBufferExpected ? "" : "not ")
              + "to be empty"),
          emptyBufferExpected == getBuffer().isEmpty());
      }
    });
  }

  public BufferValidator setBufferHolesExpected(final boolean bufferHolesExpected) {
    return addTest(new Test("Buffer Holes Test") {
      public void test() throws Exception {
        Assert.assertTrue(
          supplement(
            "Expected buffer "
              + (bufferHolesExpected ? "" : "not ")
              + "to have holes"),
          hasHoles(getBuffer()) == bufferHolesExpected);
      }
      private boolean hasHoles(Geometry buffer) {
        if (buffer.isEmpty()) {
          return false;
        }
        if (buffer instanceof Polygon) {
          return ((Polygon) buffer).getNumInteriorRing() > 0;
        }
        MultiPolygon multiPolygon = (MultiPolygon) buffer;
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
          if (hasHoles(multiPolygon.getGeometryN(i))) {
            return true;
          }
        }
        return false;
      }
    });
  }

  private Geometry getOriginal() throws ParseException {
    if (original == null) {
      original = wktReader.read(wkt);
    }
    return original;
  }


  public BufferValidator setPrecisionModel(PrecisionModel precisionModel) {
    wktReader = new WKTReader(new GeometryFactory(precisionModel));
    return this;
  }

  public BufferValidator setFactory(PrecisionModel precisionModel, int srid) {
    wktReader = new WKTReader(new GeometryFactory(precisionModel, srid));
    return this;
  }

  private Geometry getBuffer() throws ParseException {
    if (buffer == null) {
      buffer = getOriginal().buffer(bufferDistance, QUADRANT_SEGMENTS_1);
      if (getBuffer().getClass() == GeometryCollection.class && getBuffer().isEmpty()) {
        try {
          //#contains doesn't work with GeometryCollections [Jon Aquino
          // 10/29/2003]
          buffer = wktReader.read("POINT EMPTY");
        } catch (ParseException e) {
          org.locationtech.jts.util.Assert.shouldNeverReachHere();
        }
      }
    }
    return buffer;
  }

  private void addContainsTest() {
    addTest(new Test("Contains Test") {
      public void test() throws Exception {
        if (getOriginal().getClass() == GeometryCollection.class) {
          return;
        }
        org.locationtech.jts.util.Assert.isTrue(getOriginal().isValid());
        if (bufferDistance > 0) {
          Assert.assertTrue(
            supplement("Expected buffer to contain original"),
            contains(getBuffer(), getOriginal()));
        } else {
          Assert.assertTrue(
            supplement("Expected original to contain buffer"),
            contains(getOriginal(), getBuffer()));
        }
      }
      private boolean contains(Geometry a, Geometry b) {
        //JTS doesn't currently handle empty geometries correctly [Jon Aquino
        // 10/29/2003]
        if (b.isEmpty()) {
          return true;
        }
        boolean isContained = a.contains(b);
        return isContained;
      }
    });
  }

  private void addBufferResultValidatorTest() {
    addTest(new Test("BufferResultValidator Test") {
      public void test() throws Exception {
        if (getOriginal().getClass() == GeometryCollection.class) {
          return;
        }
        
          Assert.assertTrue(
            supplement("BufferResultValidator failure"),
            BufferResultValidator.isValid(getOriginal(), bufferDistance, getBuffer()));
      }
    });
  }

}
