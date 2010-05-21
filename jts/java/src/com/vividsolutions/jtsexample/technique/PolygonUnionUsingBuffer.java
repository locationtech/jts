package com.vividsolutions.jtsexample.technique;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;

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