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
package com.vividsolutions.jts.io.geojson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;

/**
 * Writes {@link Geometry}s as JSON fragments in GeoJson format.
 * 
 * @author Martin Davis
 * @author Paul Howells, Vivid Solutions
 */
public class GeoJsonWriter {
  
  public static final String EPSG_PREFIX = "EPSG:";
  
  private double scale;
  private boolean isEncodeCRS = true;

  /**
   * Constructs a GeoJsonWriter instance.
   */
  public GeoJsonWriter() {
    this(8);
  }

  /**
   * Constructs a GeoJsonWriter instance specifying the number of decimals to
   * use when encoding floating point numbers.
   */
  public GeoJsonWriter(int decimals) {
    this.scale = Math.pow(10, decimals);
  }

  public void setEncodeCRS(boolean isEncodeCRS) {
    this.isEncodeCRS  = isEncodeCRS;
  }
  
  /**
   * Writes a {@link Geometry} in GeoJson format to a String.
   * 
   * @param geometry
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

  private Map<String, Object> create(Geometry geometry, boolean encodeCRS) {

    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(GeoJsonConstants.NAME_TYPE, geometry.getGeometryType());

    if (geometry instanceof Point) {
      Point point = (Point) geometry;

      final String jsonString = getJsonString(point.getCoordinateSequence());

      result.put(GeoJsonConstants.NAME_COORDINATES, new JSONAware() {

        public String toJSONString() {
          return jsonString;
        }
      });

    } else if (geometry instanceof LineString) {
      LineString lineString = (LineString) geometry;

      final String jsonString = getJsonString(lineString
          .getCoordinateSequence());

      result.put(GeoJsonConstants.NAME_COORDINATES, new JSONAware() {

        public String toJSONString() {
          return jsonString;
        }
      });

    } else if (geometry instanceof Polygon) {
      Polygon polygon = (Polygon) geometry;

      result.put(GeoJsonConstants.NAME_COORDINATES, makeJsonAware(polygon));

    } else if (geometry instanceof MultiPoint) {
      MultiPoint multiPoint = (MultiPoint) geometry;

      result.put(GeoJsonConstants.NAME_COORDINATES, makeJsonAware(multiPoint));

    } else if (geometry instanceof MultiLineString) {
      MultiLineString multiLineString = (MultiLineString) geometry;

      result.put(GeoJsonConstants.NAME_COORDINATES, makeJsonAware(multiLineString));

    } else if (geometry instanceof MultiPolygon) {
      MultiPolygon multiPolygon = (MultiPolygon) geometry;

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
