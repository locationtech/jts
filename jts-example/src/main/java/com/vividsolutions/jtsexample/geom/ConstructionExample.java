package com.vividsolutions.jtsexample.geom;

import com.vividsolutions.jts.geom.*;

/**
 * Examples of constructing Geometries programmatically.
 * <p>
 * The Best Practice moral here is:
 * <quote>
 * Use the GeometryFactory to construct Geometries whenever possible.
 * </quote>
 * This has several advantages:
 * <ol>
 * <li>Simplifies your code
 * <li>allows you to take advantage of convenience methods provided by GeometryFactory
 * <li>Insulates your code from changes in the signature of JTS constructors
 * </ol>
 *
 * @version 1.7
 */
public class ConstructionExample
{

  public static void main(String[] args)
      throws Exception
  {
    // create a factory using default values (e.g. floating precision)
    GeometryFactory fact = new GeometryFactory();

    Point p1 = fact.createPoint(new Coordinate(0,0));
    System.out.println(p1);

    Point p2 = fact.createPoint(new Coordinate(1,1));
    System.out.println(p2);

    MultiPoint mpt = fact.createMultiPoint(new Coordinate[] { new Coordinate(0,0), new Coordinate(1,1) } );
    System.out.println(mpt);

  }
}