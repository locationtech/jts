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
package org.locationtech.jts.io.gml2;

/**
 * Various constant strings associated with GML format.
 */
final public class GMLConstants{
	
	  // Namespace constants
	  public static final String GML_NAMESPACE = "http://www.opengis.net/gml";
	  public static final String GML_PREFIX = "gml";

	  // Source Coordinate System
	  public static final String GML_ATTR_SRSNAME = "srsName";

	  // GML associative types
	  public static final String GML_GEOMETRY_MEMBER = "geometryMember";
	  public static final String GML_POINT_MEMBER = "pointMember";
	  public static final String GML_POLYGON_MEMBER = "polygonMember";
	  public static final String GML_LINESTRING_MEMBER = "lineStringMember";
	  public static final String GML_OUTER_BOUNDARY_IS = "outerBoundaryIs";
	  public static final String GML_INNER_BOUNDARY_IS = "innerBoundaryIs";

	  // Primitive Geometries
	  public static final String GML_POINT = "Point";
	  public static final String GML_LINESTRING = "LineString";
	  public static final String GML_LINEARRING = "LinearRing";
	  public static final String GML_POLYGON = "Polygon";
	  public static final String GML_BOX = "Box";

	  // Aggregate Geometries
	  public static final String GML_MULTI_GEOMETRY = "MultiGeometry";
	  public static final String GML_MULTI_POINT = "MultiPoint";
	  public static final String GML_MULTI_LINESTRING = "MultiLineString";
	  public static final String GML_MULTI_POLYGON = "MultiPolygon";

	  // Coordinates
	  public static final String GML_COORDINATES = "coordinates";
	  public static final String GML_COORD = "coord";
	  public static final String GML_COORD_X = "X";
	  public static final String GML_COORD_Y = "Y";
	  public static final String GML_COORD_Z = "Z";
}