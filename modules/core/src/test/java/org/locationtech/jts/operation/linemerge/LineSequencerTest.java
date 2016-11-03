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

package org.locationtech.jts.operation.linemerge;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;


/**
 * Test LineSequencer
 *
 * @version 1.7
 */
public class LineSequencerTest
    extends TestCase
{
  private static WKTReader rdr = new WKTReader();

  public LineSequencerTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(LineSequencerTest.class);
  }

  public void testSimple()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 0 20, 0 30 )",
      "LINESTRING ( 0 10, 0 20 )"
    };
    String result =
        "MULTILINESTRING ((0 0, 0 10), (0 10, 0 20), (0 20, 0 30))";
    runLineSequencer(wkt, result);
  }

  public void testSimpleLoop()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 0 10, 0 0 )",
    };
    String result =
        "MULTILINESTRING ((0 0, 0 10), (0 10, 0 0))";
    runLineSequencer(wkt, result);
  }

  public void testSimpleBigLoop()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 0 20, 0 30 )",
      "LINESTRING ( 0 30, 0 00 )",
      "LINESTRING ( 0 10, 0 20 )",
    };
    String result =
        "MULTILINESTRING ((0 0, 0 10), (0 10, 0 20), (0 20, 0 30), (0 30, 0 0))";
    runLineSequencer(wkt, result);
  }

  public void test2SimpleLoops()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 0 10, 0 0 )",
      "LINESTRING ( 0 0, 0 20 )",
      "LINESTRING ( 0 20, 0 0 )",
    };
    String result =
        "MULTILINESTRING ((0 10, 0 0), (0 0, 0 20), (0 20, 0 0), (0 0, 0 10))";
    runLineSequencer(wkt, result);
  }

  public void testWide8WithTail()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 10 0, 10 10 )",
      "LINESTRING ( 0 0, 10 0 )",
      "LINESTRING ( 0 10, 10 10 )",
      "LINESTRING ( 0 10, 0 20 )",
      "LINESTRING ( 10 10, 10 20 )",
      "LINESTRING ( 0 20, 10 20 )",

      "LINESTRING ( 10 20, 30 30 )",
    };
    String result = null;
    runLineSequencer(wkt, result);
  }

  public void testSimpleLoopWithTail()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 0 10, 10 10 )",
      "LINESTRING ( 10 10, 10 20, 0 10 )",
    };
    String result =
        "MULTILINESTRING ((0 0, 0 10), (0 10, 10 10), (10 10, 10 20, 0 10))";
    runLineSequencer(wkt, result);
  }

  public void testLineWithRing()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 0 10, 10 10, 10 20, 0 10 )",
      "LINESTRING ( 0 30, 0 20 )",
      "LINESTRING ( 0 20, 0 10 )",
    };
    String result =
        "MULTILINESTRING ((0 0, 0 10), (0 10, 10 10, 10 20, 0 10), (0 10, 0 20), (0 20, 0 30))";
    runLineSequencer(wkt, result);
  }

  public void testMultipleGraphsWithRing()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 0 10, 10 10, 10 20, 0 10 )",
      "LINESTRING ( 0 30, 0 20 )",
      "LINESTRING ( 0 20, 0 10 )",
      "LINESTRING ( 0 60, 0 50 )",
      "LINESTRING ( 0 40, 0 50 )",
    };
    String result =
        "MULTILINESTRING ((0 0, 0 10), (0 10, 10 10, 10 20, 0 10), (0 10, 0 20), (0 20, 0 30), (0 40, 0 50), (0 50, 0 60))";
    runLineSequencer(wkt, result);
  }

  public void testMultipleGraphsWithMultipeRings()
      throws Exception
  {
    String[] wkt = {
      "LINESTRING ( 0 0, 0 10 )",
      "LINESTRING ( 0 10, 10 10, 10 20, 0 10 )",
      "LINESTRING ( 0 10, 40 40, 40 20, 0 10 )",
      "LINESTRING ( 0 30, 0 20 )",
      "LINESTRING ( 0 20, 0 10 )",
      "LINESTRING ( 0 60, 0 50 )",
      "LINESTRING ( 0 40, 0 50 )",
    };
    String result =
        "MULTILINESTRING ((0 0, 0 10), (0 10, 40 40, 40 20, 0 10), (0 10, 10 10, 10 20, 0 10), (0 10, 0 20), (0 20, 0 30), (0 40, 0 50), (0 50, 0 60))";
    runLineSequencer(wkt, result);
  }

// isSequenced tests ==========================================================

  public void testLineSequence()
      throws Exception
  {
    String wkt =
        "LINESTRING ( 0 0, 0 10 )";
    runIsSequenced(wkt, true);
  }

  public void testSplitLineSequence()
      throws Exception
  {
    String wkt =
        "MULTILINESTRING ((0 0, 0 1), (0 2, 0 3), (0 3, 0 4) )";
    runIsSequenced(wkt, true);
  }

  public void testBadLineSequence()
      throws Exception
  {
    String wkt =
        "MULTILINESTRING ((0 0, 0 1), (0 2, 0 3), (0 1, 0 4) )";
    runIsSequenced(wkt, false);
  }

//==========================================================

  private void runLineSequencer(String[] inputWKT, String expectedWKT)
      throws ParseException
  {
    List inputGeoms = fromWKT(inputWKT);
    LineSequencer sequencer = new LineSequencer();
    sequencer.add(inputGeoms);

    boolean isCorrect = false;
    if (! sequencer.isSequenceable()) {
      assertTrue(expectedWKT == null);
    }
    else {
      Geometry expected = rdr.read(expectedWKT);
      Geometry result = sequencer.getSequencedLineStrings();
      boolean isOK = expected.equalsNorm(result);
      if (! isOK) {
        System.out.println("ERROR - Expected: " + expected);
        System.out.println("          Actual: " + result);
      }
      assertTrue(isOK);

      boolean isSequenced = LineSequencer.isSequenced(result);
      assertTrue(isSequenced);
    }
  }

  private void runIsSequenced(String inputWKT, boolean expected)
      throws ParseException
  {
    Geometry g = rdr.read(inputWKT);
    boolean isSequenced = LineSequencer.isSequenced(g);
    assertTrue(isSequenced == expected);
  }

  List fromWKT(String[] wkts)
  {
    List geomList = new ArrayList();
    for (int i = 0; i < wkts.length; i++) {
      try {
        geomList.add(rdr.read(wkts[i]));
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return geomList;
  }

}