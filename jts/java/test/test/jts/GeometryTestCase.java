package test.jts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.Assert;

import junit.framework.TestCase;

/**
 * A base class for Geometry tests which provides various utility methods.
 * 
 * @author mbdavis
 *
 */
public class GeometryTestCase extends TestCase{

  GeometryFactory geomFactory = new GeometryFactory();
  
  WKTReader reader = new WKTReader(geomFactory);

  public GeometryTestCase(String name) {
    super(name);
  }

  protected void checkEqual(Geometry expected, Geometry actual) {
    Geometry actualNorm = actual.norm();
    Geometry expectedNorm = expected.norm();
    boolean equal = actualNorm.equalsExact(expectedNorm);
    if (! equal) {
      System.out.println("FAIL - Expected = " + expectedNorm
          + " actual = " + actualNorm );
    }
    assertTrue(equal);
  }

  protected void checkEqual(Collection expected, Collection actual) {
    checkEqual(toGeometryCollection(expected),toGeometryCollection(actual) );
  }

  GeometryCollection toGeometryCollection(Collection geoms) {
    return geomFactory.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
  }
  
  /**
   * Reads a {@link Geometry} from a WKT string using a custom {@link GeometryFactory}.
   *  
   * @param geomFactory the custom factory to use
   * @param wkt the WKT string
   * @return the geometry read
   */
  protected Geometry read(GeometryFactory geomFactory, String wkt) {
    WKTReader reader = new WKTReader(geomFactory);
    try {
       return reader.read(wkt);
    } catch (ParseException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  protected Geometry read(String wkt) {
    try {
       return reader.read(wkt);
    } catch (ParseException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  protected List readList(String[] wkt) {
    ArrayList geometries = new ArrayList();
    for (int i = 0; i < wkt.length; i++) {
      geometries.add(read(wkt[i]));
    }
    return geometries;
  }
}
