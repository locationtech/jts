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

package org.locationtech.jtsexample.technique;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

/**
 * Shows a technique for using a zero-width buffer to compute
 * the union of a collection of <b>polygonal</b> geometrys.
 * The advantages of this technique are:
 * <ul>
 * <li>can avoid robustness issues
 * <li>faster for large numbers of input geometries
 * <li>handles GeometryCollections as input (although only the polygons will be buffered)
 * </ul>
 * Disadvantages are:
 * <ul>
 * <li>may not preserve input coordinate precision in some cases
 * <li>only works for polygons
 * </ul>
 * 
 * @deprecated It is now recommended to use Geometry.union() (unary union) instead of this technique.
 *
 * @version 1.7
 */
public class PolygonUnionUsingBuffer {

  public static void main(String[] args)
      throws Exception
  {
    WKTReader rdr = new WKTReader();

    Geometry[] geom = new Geometry[3];
    geom[0] = rdr.read("POLYGON (( 100 180, 100 260, 180 260, 180 180, 100 180 ))");
    geom[1] = rdr.read("POLYGON (( 80 140, 80 200, 200 200, 200 140, 80 140 ))");
    geom[2] = rdr.read("POLYGON (( 160 160, 160 240, 240 240, 240 160, 160 160 ))");
    unionUsingBuffer(geom);

  }

  public static void unionUsingBuffer(Geometry[] geom)
  {
    GeometryFactory fact = geom[0].getFactory();
    Geometry geomColl = fact.createGeometryCollection(geom);
    Geometry union = geomColl.buffer(0.0);
    System.out.println(union);
  }



}