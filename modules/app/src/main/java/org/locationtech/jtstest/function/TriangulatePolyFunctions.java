/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.polygon.PolygonHoleJoiner;
import org.locationtech.jts.triangulate.polygon.PolygonTriangulator;


public class TriangulatePolyFunctions 
{
  public static Geometry triangulate(Geometry geom)
  {
    return PolygonTriangulator.triangulate(geom);
  }

  public static Geometry constrainedDelaunay(Geometry geom)
  {
    return ConstrainedDelaunayTriangulator.triangulate(geom);
  }

  public static Geometry joinHoles(Geometry geom)
  {
    return PolygonHoleJoiner.joinAsPolygon((Polygon) geom);
  }

}
