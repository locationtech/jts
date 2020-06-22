package org.locationtech.jts.io.gml2;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.xml.sax.SAXException;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class GMLReaderTest extends GeometryTestCase {
  private static final GeometryFactory geometryFactory = new GeometryFactory();

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

  public void testPointWithCoordSepMultiSpace() {
    checkRead("<gml:Point>"
        + "    <gml:coordinates>45.67,     88.56</gml:coordinates>"
        + " </gml:Point>", 
        "POINT (45.67 88.56)");
  }

  public void testLineStringWithCoordSepSpace() {
    checkRead( "<gml:LineString>"
        + "    <gml:coordinates>45.67, 88.56 55.56,89.44</gml:coordinates>"
        + " </gml:LineString >",
        "LINESTRING (45.67 88.56, 55.56 89.44)");
  }

  private void checkRead(String gml, String wktExpected) {
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
  }
}
