package test.jts.junit.noding.snapround;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import java.util.*;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.noding.*;
import com.vividsolutions.jts.noding.snapround.GeometryNoder;

/**
 * Test Snap Rounding
 *
 * @version 1.7
 */
public class SnapRoundingTest  extends TestCase {

  WKTReader rdr = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(SnapRoundingTest.class);
  }

  public SnapRoundingTest(String name) { super(name); }

  public void testPolyWithCloseNode() {
    String[] polyWithCloseNode = {
      "POLYGON ((20 0, 20 160, 140 1, 160 160, 160 1, 20 0))"
    };
    runRounding(polyWithCloseNode);
  }

  public void testBadLines1() {
    String[] badLines1 = {
      "LINESTRING ( 171 157, 175 154, 170 154, 170 155, 170 156, 170 157, 171 158, 171 159, 172 160, 176 156, 171 156, 171 159, 176 159, 172 155, 170 157, 174 161, 174 156, 173 156, 172 156 )"
    };
    runRounding(badLines1);
  }
  public void testBadLines2() {
    String[] badLines2 = {
      "LINESTRING ( 175 222, 176 222, 176 219, 174 221, 175 222, 177 220, 174 220, 174 222, 177 222, 175 220, 174 221 )"
    };
    runRounding(badLines2);
  }
  public void testCollapse1() {
    String[] collapse1 = {
      "LINESTRING ( 362 177, 375 164, 374 164, 372 161, 373 163, 372 165, 373 164, 442 58 )"
    };
    runRounding(collapse1);
  }

  public void testBadNoding1() {
    String[] badNoding1 = {
      "LINESTRING ( 76 47, 81 52, 81 53, 85 57, 88 62, 89 64, 57 80, 82 55, 101 74, 76 99, 92 67, 94 68, 99 71, 103 75, 139 111 )"
    };
    runRounding(badNoding1);
  }

  public void testBadNoding1Extract() {
    String[] badNoding1Extract = {
      "LINESTRING ( 82 55, 101 74 )",
      "LINESTRING ( 94 68, 99 71 )",
      "LINESTRING ( 85 57, 88 62 )"
    };
    runRounding(badNoding1Extract);
  }
  public void testBadNoding1ExtractShift() {
    String[] badNoding1ExtractShift = {
      "LINESTRING ( 0 0, 19 19 )",
      "LINESTRING ( 12 13, 17 16 )",
      "LINESTRING ( 3 2, 6 7 )"
    };
    runRounding(badNoding1ExtractShift);
  }

  void runRounding(String[] wkt)
  {
    List geoms = fromWKT(wkt);
    PrecisionModel pm = new PrecisionModel(1.0);
    GeometryNoder noder = new GeometryNoder(pm);
    List nodedLines = noder.node(geoms);
/*
    for (Iterator it = nodedLines.iterator(); it.hasNext(); ) {
      System.out.println(it.next());
    }
    */
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