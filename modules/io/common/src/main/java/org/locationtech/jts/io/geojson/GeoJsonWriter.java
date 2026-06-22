/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.geojson;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.Assert;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Writes {@link Geometry}s as JSON fragments in GeoJSON format.
 * <p>
 * The current GeoJSON specification is 
 * <a href='https://tools.ietf.org/html/rfc7946'>https://tools.ietf.org/html/rfc7946</a>.
 * <p>
 * The GeoJSON specification states that polygons should be emitted using
 * the counter-clockwise shell orientation.  This is not enforced by this writer.
 * <p>
 * The GeoJSON specification does not state how to represent empty geometries of specific type.
 * The writer emits empty typed geometries using an empty array for the <code>coordinates</code> property.
 * 
 * @author Martin Davis
 * @author Paul Howells, Vivid Solutions
 */
public class GeoJsonWriter {
  
  private static final String JSON_ARRAY_EMPTY = "[]";

  /**
   * The prefix for EPSG codes in the <code>crs</code> property.
   */
  public static final String EPSG_PREFIX = "EPSG:";
  
  private double scale;
  private boolean isEncodeCRS = true;
  private boolean isForceCCW = false;

  /**
   * Constructs a GeoJsonWriter instance.
   */
  public GeoJsonWriter() {
    this(8);
  }

  /**
   * Constructs a GeoJsonWriter instance specifying the number of decimals to
   * use when encoding floating point numbers.
   * 
   * @param decimals the number of decimal places to output
   */
  public GeoJsonWriter(int decimals) {
    this.scale = Math.pow(10, decimals);
  }

  /**
   * Sets whether the GeoJSON <code>crs</code> property should 
   * be output.
   * The value of the property is taken from geometry SRID.
   * 
   * @param isEncodeCRS true if the crs property should be output
   */
  public void setEncodeCRS(boolean isEncodeCRS) {
    this.isEncodeCRS  = isEncodeCRS;
  }
  
  /**
   * Sets whether the GeoJSON should be output following counter-clockwise orientation aka Right Hand Rule defined in RFC7946
   * See <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">RFC 7946 Specification</a> for more context.
   *
   * @param isForceCCW true if the GeoJSON should be output following the RFC7946 counter-clockwise orientation aka Right Hand Rule
   */
  public void setForceCCW(boolean isForceCCW) {
    this.isForceCCW = isForceCCW;
  }

  /**
   * Writes a {@link Geometry} in GeoJson format to a String.
   * 
   * @param geometry the geometry to write
   * @return String GeoJson Encoded Geometry
   */
  public String write(Geometry geometry) {

    StringWriter writer = new StringWriter();
    try {
      write(geometry, writer);
    } catch (IOException ex) {
      Assert.shouldNeverReachHere();
    }

    return writer.toString();
  }

  /**
   * Writes a {@link Geometry} in GeoJson format into a {@link Writer}.
   * 
   * @param geometry
   *          Geometry to encode
   * @param writer
   *          Stream to encode to.
   * @throws IOException
   *           throws an IOException when unable to write the JSON string
   */
  public void write(Geometry geometry, Writer writer) throws IOException {
    Map<String, Object> map = create(geometry, isEncodeCRS);
    JSONObject.writeJSONString(map, writer);
    writer.flush();
  }

  protected Map<String, Object> create(Geometry geometry, boolean encodeCRS) {

    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(GeoJsonConstants.NAME_TYPE, geometry.getGeometryType());

    if (geometry instanceof Point) {
      Point point = (Point) geometry;

      CoordinateSequence coordinateSequence = point.getCoordinateSequence();
      final String jsonString = coordinateSequence.size() == 0
          ? JSON_ARRAY_EMPTY : getJsonString(coordinateSequence);

      result.put(GeoJsonConstants.NAME_COORDINATES, new JSONAware() {

        public String toJSONString() {
          return jsonString;
        }
      });

    } else if (geometry instanceof LineString) {
      LineString lineString = (LineString) geometry;

      CoordinateSequence coordinateSequence = lineString.getCoordinateSequence();
      final String jsonString = coordinateSequence.size() == 0
          ? JSON_ARRAY_EMPTY : getJsonString(coordinateSequence);

      result.put(GeoJsonConstants.NAME_COORDINATES, new JSONAware() {

        public String toJSONString() {
          return jsonString;
        }
      });

    } else if (geometry instanceof Polygon) {
      Polygon polygon = (Polygon) geometry;

      if (isForceCCW) {
        polygon = (Polygon) OrientationTransformer.transformCCW(polygon);
      }

      result.put(GeoJsonConstants.NAME_COORDINATES, makeJsonAware(polygon));

    } else if (geometry instanceof MultiPoint) {
      MultiPoint multiPoint = (MultiPoint) geometry;

      result.put(GeoJsonConstants.NAME_COORDINATES, makeJsonAware(multiPoint));

    } else if (geometry instanceof MultiLineString) {
      MultiLineString multiLineString = (MultiLineString) geometry;

      result.put(GeoJsonConstants.NAME_COORDINATES, makeJsonAware(multiLineString));

    } else if (geometry instanceof MultiPolygon) {
      MultiPolygon multiPolygon = (MultiPolygon) geometry;

      if (isForceCCW) {
        multiPolygon = (MultiPolygon) OrientationTransformer.transformCCW(multiPolygon);
      }

      result.put(GeoJsonConstants.NAME_COORDINATES, makeJsonAware(multiPolygon));

    } else if (geometry instanceof GeometryCollection) {
      GeometryCollection geometryCollection = (GeometryCollection) geometry;

      ArrayList<Map<String, Object>> geometries = new ArrayList<Map<String, Object>>(
          geometryCollection.getNumGeometries());

      for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
        geometries.add(create(geometryCollection.getGeometryN(i), false));
      }

      result.put(GeoJsonConstants.NAME_GEOMETRIES, geometries);

    } else {
      throw new IllegalArgumentException("Unable to encode geometry " + geometry.getGeometryType() );
    }

    if (encodeCRS) {
      result.put(GeoJsonConstants.NAME_CRS, createCRS(geometry.getSRID()));
    }

    return result;
  }

  private Map<String, Object> createCRS(int srid) {

    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(GeoJsonConstants.NAME_TYPE, GeoJsonConstants.NAME_NAME);

    Map<String, Object> props = new LinkedHashMap<String, Object>();
    props.put(GeoJsonConstants.NAME_NAME, EPSG_PREFIX + srid);

    result.put(GeoJsonConstants.NAME_PROPERTIES, props);

    return result;
  }

  private List<JSONAware> makeJsonAware(Polygon poly) {
    ArrayList<JSONAware> result = new ArrayList<JSONAware>();

    {
      final String jsonString = getJsonString(poly.getExteriorRing()
          .getCoordinateSequence());
      result.add(new JSONAware() {

        public String toJSONString() {
          return jsonString;
        }
      });
    }
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      final String jsonString = getJsonString(poly.getInteriorRingN(i)
          .getCoordinateSequence());
      result.add(new JSONAware() {

        public String toJSONString() {
          return jsonString;
        }
      });
    }

    return result;
  }

  private List<Object> makeJsonAware(GeometryCollection geometryCollection) {

    ArrayList<Object> list = new ArrayList<Object>(
        geometryCollection.getNumGeometries());
    for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
      Geometry geometry = geometryCollection.getGeometryN(i);
      
      if (geometry instanceof Polygon) {
        Polygon polygon = (Polygon) geometry;
        list.add(makeJsonAware(polygon));
      } 
      else if (geometry instanceof LineString) {
        LineString lineString = (LineString) geometry;
        final String jsonString = getJsonString(lineString
            .getCoordinateSequence());
        list.add(new JSONAware() {

          public String toJSONString() {
            return jsonString;
          }
        });
      } 
      else if (geometry instanceof Point) {
        Point point = (Point) geometry;
        final String jsonString = getJsonString(point.getCoordinateSequence());
        list.add(new JSONAware() {

          public String toJSONString() {
            return jsonString;
          }
        });
      }
    }

    return list;
  }

  private String getJsonString(CoordinateSequence coordinateSequence) {
    StringBuffer result = new StringBuffer();

    if (coordinateSequence.size() > 1) {
      result.append("[");
    }
    for (int i = 0; i < coordinateSequence.size(); i++) {
      if (i > 0) {
        result.append(",");
      }
      result.append("[");
      result.append(formatOrdinate(coordinateSequence.getOrdinate(i, CoordinateSequence.X))); 
      result.append(",");
      result.append(formatOrdinate(coordinateSequence.getOrdinate(i, CoordinateSequence.Y)));

      if (coordinateSequence.getDimension() > 2 ) {
        double z = coordinateSequence.getOrdinate(i, CoordinateSequence.Z);
        if (!  Double.isNaN(z)) {
          result.append(",");
          result.append(formatOrdinate(z));
        }
      }

      result.append("]");

    }

    if (coordinateSequence.size() > 1) {
      result.append("]");
    }

    return result.toString();
  }

  private String formatOrdinate(double x) {
    String result = null;

    if (Math.abs(x) >= Math.pow(10, -3) && x < Math.pow(10, 7)) {
      x = Math.floor(x * scale + 0.5) / scale;
      long lx = (long) x;
      if (lx == x) {
        result = Long.toString(lx);
      } else {
        result = Double.toString(x);
      }
    } else {
      result = Double.toString(x);
    }

    return result;
  }

}
