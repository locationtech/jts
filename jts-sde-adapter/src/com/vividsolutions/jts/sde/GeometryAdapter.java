package com.vividsolutions.jts.sde;
import com.esri.sde.sdk.client.*;
import com.vividsolutions.jts.geom.*;
/**
 *  <p>
 *  Title: JTS - SDE Adaptation Project</p> <p>
 *  Description: Converts JTS Geometries to SDE Shapes</p> <p>
 *  Copyright: Copyright (c) 2002</p> <p>
 *  Company: Vivid Solutions Inc.</p>
 *@author     Georgi Kostadinov
 *@version    1.0
 */

public class GeometryAdapter extends SeShape {

  private SeCoordinateReference DEFAULT_COORD_REF = sdeBCAlbersCoordRef();

  /**
   *  Adapts a JTS Geometry to a SDE Shape. <p>
   *
   *  <b>Note:</b> The coordinate reference defaults to <i>BCAlbers</i> !
   *
   *@param  geometry  A JTS Geometry to adapt
   */
  public GeometryAdapter(Geometry geometry) throws SeException {
    setCoordRef(DEFAULT_COORD_REF);
    adapt(geometry);
  }

  /**
   *  Adapts a JTS Geometry to a SDE Shape. Adaptation will use the specified
   *  <code>CoordinateReference</code>.
   *
   *@param  geometry  A JTS Geometry to adapt
   *@param  coordRef  Adaptee's <code>CoordinateReference</code>
   */
  public GeometryAdapter(Geometry geometry, SeCoordinateReference coordRef) throws SeException {
    setCoordRef(coordRef);
    adapt(geometry);
  }

  /**
   *  A Factory method to create a BC Albers Coordinate Reference.
   *
   *@return    SDE Coordinate Reference
   */
  public static SeCoordinateReference sdeBCAlbersCoordRef() {
    SeCoordinateReference coordRef82 = new SeCoordinateReference();
    coordRef82.setCoordSysByDescription(
        "PROJCS[\"PCS_Albers\"," +
        "GEOGCS[\"GCS_North_American_1983\"," +
        "DATUM[\"D_North_American_1983\"," +
        "SPHEROID[\"GRS_1980\",6378137,298.257222101]]," +
        "PRIMEM[\"Greenwich\",0]," +
        "UNIT[\"Degree\",0.017453292519943295]]," +
        "PROJECTION[\"Albers\"]," +
        "PARAMETER[\"False_Easting\",1000000]," +
        "PARAMETER[\"False_Northing\",0]," +
        "PARAMETER[\"Central_Meridian\",-126]," +
        "PARAMETER[\"Standard_Parallel_1\",50]," +
        "PARAMETER[\"Standard_Parallel_2\",58.5]," +
        "PARAMETER[\"Latitude_Of_Origin\",45]," +
        "UNIT[\"Meter\",1]]");
    coordRef82.setM(0, 1000);
    coordRef82.setXY(0, 0, 1000);
    coordRef82.setZ(-1000000, 1000);
    return coordRef82;
  }

  /**
   *  Adapt a JTS Geometry to a SDE Shape. <p>
   *
   *  The following geometries are supported:
   *  <ul>
   *    <li> Polygon</li>
   *    <li> MultiPolygon</li>
   *    <li> Point</li>
   *    <li> MultiPoint</li>
   *    <li> LineString</li>
   *    <li> MultiLineString</li>
   *    <li> LinearRing</li>
   *  </ul>
   *  <b>Note: </b> The GeometryCollection geometry is supported throught the
   *  <code>ShapeFactory</code> class. This is because the SDE Shape class does
   *  not support this type. <p>
   *
   *  If the client passes none of the above mentioned geometries or a NULL
   *  geometry, then a NIL (empty) SDE Shape is created!
   *
   *@param  geometry  A JTS Geometry to adapt.
   */
  protected void adapt(Geometry geometry) throws SeException {
    if (geometry == null || geometry.isEmpty()) {
      //Do nothing, thus creating a nil Shape [Jon Aquino]
      return;
    }
    String geomType = geometry.getGeometryType();
    //Use #getClass rather than instanceof, becuase LinearRing is a subclass
    //of LineString. [Jon Aquino]
    if (geometry.getClass() == Polygon.class) {
      adapt((Polygon) geometry);
    }
    else if (geometry.getClass() == MultiPolygon.class) {
      adapt((MultiPolygon) geometry);
    }
    else if (geometry.getClass() == com.vividsolutions.jts.geom.Point.class) {
      adapt((com.vividsolutions.jts.geom.Point) geometry);
    }
    else if (geometry.getClass() == MultiPoint.class) {
      adapt((MultiPoint) geometry);
    }
    else if (geometry.getClass() == LineString.class) {
      adapt((LineString) geometry);
    }
    else if (geometry.getClass() == MultiLineString.class) {
      adapt((MultiLineString) geometry);
    }
    else if (geometry.getClass() == LinearRing.class) {
      adapt((LinearRing) geometry);
    }
    else {// geometry type not supported by this adapter!
      throw new UnsupportedOperationException("Unsupported type: " + geometry.getClass().getName());
    }
  }

  /**
   *  Adapts a JTS <code>Polygon</code> to a SDE POLYGON. This method generate a
   *  POLYGON based on the Polygon's shell. It then loops through its holes
   *  (islands) and they are not empty, then a new island is added to the
   *  POLYGON shape.
   *
   *@param  polygon  A JTS <code>Polygon</code> to adapt
   */
  protected void adapt(Polygon polygon) throws SeException {
        LinearRing shell = (LinearRing) polygon.getExteriorRing();
        SDEPoint[] points = adapt(shell.getCoordinates());
        //Eric Wright was finding that something inside toString was causing
        //JRun to spit out a NoSuchMethodError. Tomcat did not, however.
        //The problem vanished when I commented out these System.out.println's.
        //Anyway, we shouldn't be calling System.out.println. Something to
        //investigate in the future, however. [Jon Aquino]
//        System.out.println("shell: " + toString(points));
        SeShape shellShape = new SeShape();
        shellShape.setCoordRef(getCoordRef());
        shellShape.generatePolygon(points.length, 1, new int[] {0}, points);
        //With the 8.2 API, must use #addPart, not #generatePolygon, before adding
        //islands. Else get Model Integrity error. [Jon Aquino]
        addPart(shellShape);
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
          LinearRing hole = (LinearRing) polygon.getInteriorRingN(i);
          Coordinate[] holeCoordinates = hole.getCoordinates();
          if (holeCoordinates.length > 0) {
//            System.out.println("hole: " + toString(adapt(hole.getCoordinates())));
            addIsland(adapt(hole.getCoordinates()));
          }
        }
  }

  /**
   *  Adapts a <code>MultiPolygon</code> to a SDE MULTI_POLYGON.
   *
   *@param  multiPolygon  A JTS <code>MultiPolygon</code> to adapt
   */
  protected void adapt(MultiPolygon multiPolygon) throws SeException {
    adapt((GeometryCollection) multiPolygon);
  }

  /**
   *  Adapts a <code>Point</code> to a SDE POINT.
   *
   *@param  point  A JTS <code>Point</code> to adapt
   */
  protected void adapt(com.vividsolutions.jts.geom.Point point) throws SeException {
        generatePoint(1, new SDEPoint[] {
            new CoordinateAdapter(point.getCoordinate())});
  }

  /**
   *  Adapts a <code>MultiPoint</code> to a SDE MULTI_POINT.
   *
   *@param  multiPoint  A JTS <code>MultiPoint</code> to adapt
   */
  protected void adapt(MultiPoint multiPoint) throws SeException {
    adapt((GeometryCollection) multiPoint);
  }

  /**
   *  Adapts a <code>LineString</code> to a SDE LINE.
   *
   *@param  lineString  A JTS <code>LineString</code> to adapt
   */
  protected void adapt(LineString lineString) throws SeException {
      SDEPoint[] points = adapt(lineString.getCoordinates());
      generateLine(points.length, 1, new int[] {0}, points);
  }

  /**
   *  Adapts a <code>MultiLineString</code> to a SDE MULTI_LINE.
   *
   *@param  multiLineString  A JTS <code>MultiLineString</code> to adapt
   */
  protected void adapt(MultiLineString multiLineString) throws SeException {
    adapt((GeometryCollection) multiLineString);
  }

  /**
   *  Adapts a <code>LinearRing</code> to a SDE LINE or POLYGON.
   *
   *@param  linearRing  A JTS <code>LinearRing</code> to adapt
   */
  protected void adapt(LinearRing linearRing) throws SeException {
    adapt((LineString) linearRing);
  }

  private String toString(SDEPoint[] sdePoints) {
    String s = "new Point[] {";
    for (int i = 0; i < sdePoints.length; i++) {
      if (i != 0) {
        s += ", ";
      }
      s += "new Point(" + sdePoints[i].getX() + ", " + sdePoints[i].getY() + ")";
    }
    return s + "}";
  }

  /**
   *  Adapts a <code>MultiLineString</code>, <code>MultiPoint</code>, or <code>MultiPolygon</code>
   *  respectively to a SDE MULTI_LINE, MULTI_POINT, or MULTI_POLYGON.
   *
   *@param  geometryCollection  A JTS <code>GeometryCollection</code> to adapt
   */
  private void adapt(GeometryCollection geometryCollection) throws SeException {
    for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
      Geometry geometry = geometryCollection.getGeometryN(i);
      SeShape shape = new GeometryAdapter(geometry);
      shape.setCoordRef(getCoordRef());
      addPart(shape);
    }
  }

  /**
   *  This class adapts a JTS Coordinate to a SDE Point
   */
  protected static class CoordinateAdapter extends SDEPoint {
    public CoordinateAdapter(Coordinate coordinate) {
      super(coordinate.x, coordinate.y);
    }
  }

  private SDEPoint[] adapt(Coordinate[] coordinates) {
    int numberOfPoints = coordinates.length;
    SDEPoint[] points = new SDEPoint[numberOfPoints];
    for (int i = 0; i < numberOfPoints; i++) {
      points[i] = new CoordinateAdapter(coordinates[i]);
    }
    return points;
  }
}
