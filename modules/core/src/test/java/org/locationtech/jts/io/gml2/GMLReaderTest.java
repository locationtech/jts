package org.locationtech.jts.io.gml2;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.xml.sax.SAXException;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class GMLReaderTest extends GeometryTestCase {
  private static final int DEFAULT_SRID = 9876;
  private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), DEFAULT_SRID);

  public static void main(String args[]) {
    TestRunner.run(GMLReaderTest.class);
  }

  public GMLReaderTest(String name) { super(name); }

  public void testPoint() {
    checkRead("<gml:Point>"
        + "    <gml:coordinates>45.67,88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)");
  }
  
  public void testPointNoNamespace() {
    checkRead("<Point>"
        + "    <coordinates>45.67,88.56</coordinates>"
        + " </Point>", 
        "POINT (45.67 88.56)");
  }
  
  public void testPointWithCoordSepSpace() {
    checkRead("<gml:Point>"
        + "    <gml:coordinates>45.67, 88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)");
  }

  public void testPointWithCoordSepMultiSpaceAfter() {
    checkRead("<gml:Point>"
        + "    <gml:coordinates>45.67,     88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)");
  }

  public void testPointWithCoordSepMultiSpaceBefore() {
    checkRead("<gml:Point>"
        + "    <gml:coordinates>45.67   ,88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)");
  }

  public void testPointWithCoordSepMultiSpaceBoth() {
    checkRead("<gml:Point>"
        + "    <gml:coordinates>45.67   ,   88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)");
  }

  public void testPointSRIDInt() {
    checkRead("<gml:Point srsName='1234'>"
        + "    <gml:coordinates>45.67,     88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)", 1234);
  }

  public void testPointSRIDHash() {
    checkRead("<gml:Point srsName='some.prefix#4326'>"
        + "    <gml:coordinates>45.67,     88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)",
        4326);
  }

  public void testPointSRIDSlash() {
    checkRead("<gml:Point srsName='http://www.opengis.net/def/crs/EPSG/0/4326'>"
        + "    <gml:coordinates>45.67,     88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)",
        4326);
  }

  public void testPointSRIDColon() {
    checkRead("<gml:Point srsName='urn:ogc:def:crs:EPSG::4326'>"
        + "    <gml:coordinates>45.67,     88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)",
        4326);
  }

  public void testLineStringWithCoordSepSpace() {
    checkRead( "<gml:LineString>"
        + "    <gml:coordinates>45.67, 88.56 55.56,89.44</gml:coordinates>"
        + " </gml:LineString >",
        "LINESTRING (45.67 88.56, 55.56 89.44)");
  }

  public void testLineStringWithManySpaces() {
    checkRead( "<gml:LineString>"
        + "    <gml:coordinates>45.67,   88.56    55.56,89.44</gml:coordinates>"
        + " </gml:LineString >",
        "LINESTRING (45.67 88.56, 55.56 89.44)");
  }

  private void checkRead(String gml, String wktExpected) {
    checkRead(gml, wktExpected, DEFAULT_SRID);
  } 
  
  private void checkRead(String gml, String wktExpected, int srid) {
    GMLReader gr = new GMLReader();
    Geometry g = null;
    try {
      g = gr.read(gml, geometryFactory);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      //e.printStackTrace();
      fail(e.getMessage());
    }
    Geometry expected = read(wktExpected);
    checkEqual(expected, g);
    assertEquals("SRID incorrect - ", srid, g.getSRID());
  }
}
