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
package org.locationtech.jts.geom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Extracts the components of a given type from a {@link Geometry}.
 *
 * @version 1.7
 */
public class GeometryExtracter
  implements GeometryFilter
{

	/**
	 * Extracts the components of type {@code clz} from a {@link Geometry}, returns
	 * them in a new {@link List}, and optionally also adds them to {@code out}.
	 * <p>
	 * This method is intentionally NOT named {@code extract(...)} to avoid overload
	 * ambiguity with the legacy JTS {@code extract(Geometry, String, List)} /
	 * {@code extract(Geometry, Class, List)} methods.
	 *
	 * @param geom the geometry from which to extract (may be {@code null})
	 * @param clz  the class of the components to extract (must not be {@code null})
	 * @param out  optional collection to also write extracted components into (may
	 *             be {@code null})
	 * @param <T>  the component type
	 * @return a new modifiable list of extracted components
	 */
	public static <T extends Geometry> List<T> extractByClass(Geometry geom, Class<T> clz, Collection<? super T> out) {
		List<T> result = new ArrayList<T>();
		if (geom == null)
			return result;

		geom.apply(new GeometryExtracter(clz, result));
		if (out != null)
			out.addAll(result);

		return result;
	}

	/**
	 * Extracts the components of type {@code clz} from a {@link Geometry} and
	 * returns them in a new {@link List}.
	 *
	 * @param geom the geometry from which to extract (may be {@code null})
	 * @param clz  the class of the components to extract (must not be {@code null})
	 * @param <T>  the component type
	 * @return a new modifiable list of extracted components
	 */
	public static <T extends Geometry> List<T> extractByClass(Geometry geom, Class<T> clz) {
		return extractByClass(geom, clz, null);
	}

	/**
	 * Extracts the components of type <tt>clz</tt> from a {@link Geometry} and adds
	 * them to the provided {@link List}.
	 * 
	 * @param geom the geometry from which to extract
	 * @param list the list to add the extracted elements to
	 * @deprecated Use {@link GeometryExtracter#extract(Geometry, String, List)}
	 */
  public static List extract(Geometry geom, Class clz, List list)
  {
  	return extract(geom, toGeometryType(clz), list);
  }
  
  /**
   * @deprecated
   */
  private static String toGeometryType(Class clz) {
	if (clz == null)
	  return null;
	else if (clz.isAssignableFrom(Point.class))
	  return Geometry.TYPENAME_POINT;
	else if (clz.isAssignableFrom(LineString.class))
	  return Geometry.TYPENAME_LINESTRING;
	else if (clz.isAssignableFrom(LinearRing.class))
	  return Geometry.TYPENAME_LINEARRING;
	else if (clz.isAssignableFrom(Polygon.class))
	  return Geometry.TYPENAME_POLYGON;
	else if (clz.isAssignableFrom(MultiPoint.class))
	  return Geometry.TYPENAME_MULTIPOINT;
	else if (clz.isAssignableFrom(MultiLineString.class))
	  return Geometry.TYPENAME_MULTILINESTRING;
	else if (clz.isAssignableFrom(MultiPolygon.class))
	  return Geometry.TYPENAME_MULTIPOLYGON;
	else if (clz.isAssignableFrom(GeometryCollection.class))
	  return Geometry.TYPENAME_GEOMETRYCOLLECTION;
	throw new RuntimeException("Unsupported class");
  }
  
  /**
   * Extracts the components of <tt>geometryType</tt> from a {@link Geometry}
   * and adds them to the provided {@link List}.
   * 
   * @param geom the geometry from which to extract
   * @param geometryType Geometry type to extract (null means all types)
   * @param list the list to add the extracted elements to
   */
  public static List extract(Geometry geom, String geometryType, List list)
  {
  	if (geom.getGeometryType() == geometryType) {
  		list.add(geom);
  	}
  	else if (geom instanceof GeometryCollection) {
  		geom.apply(new GeometryExtracter(geometryType, list));
  	}
  	// skip non-LineString elemental geometries
  	
    return list;
  }

  /**
   * Extracts the components of type <tt>clz</tt> from a {@link Geometry}
   * and returns them in a {@link List}.
   * 
   * @param geom the geometry from which to extract
   * @deprecated Use {@link GeometryExtracter#extract(Geometry, String)}
   */
  public static List extract(Geometry geom, Class clz)
  {
    return extract(geom, clz, new ArrayList());
  }
  
  public static List extract(Geometry geom, String geometryType)
  {
    return extract(geom, geometryType, new ArrayList());
  }

  private final String geometryType; // legacy string-based matching mode
  private final Class<?> componentClass; // class-based matching mode
  private final Collection comps;

  public GeometryExtracter(String geometryType, List comps)
  {
    this.geometryType = geometryType;
    this.componentClass = null;
    this.comps = comps;
  }

  /**
   * Constructs a filter matching by component class.
   *
   * @param componentClass the class of the components to extract (null means all types)
   * @param comps the list to extract into
   */
  public GeometryExtracter(Class componentClass, List comps)
  {
    this.geometryType = null;
    this.componentClass = componentClass;
    this.comps = comps;
  }

  @Override
  public void filter(Geometry geom)
  {
    if (geom == null) return;

    if (componentClass != null) {
      if (componentClass.isInstance(geom)) comps.add(geom);
      return;
    }

    // legacy string-based behaviour
    if (geometryType == null || isOfType(geom, geometryType)) {
      comps.add(geom);
    }
  }

  protected static boolean isOfType(Geometry geom, String geometryType) {
    if (geom.getGeometryType() == geometryType) return true;
    if (geometryType == Geometry.TYPENAME_LINESTRING
        && geom.getGeometryType() == Geometry.TYPENAME_LINEARRING) return true;
    return false;
  }
}
