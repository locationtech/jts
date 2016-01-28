/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jtsexample.geom;

import org.locationtech.jts.geom.*;

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