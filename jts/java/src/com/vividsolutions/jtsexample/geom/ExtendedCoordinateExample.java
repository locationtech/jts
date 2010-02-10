
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
package com.vividsolutions.jtsexample.geom;

import com.vividsolutions.jts.geom.*;


/**
 * @version 1.7
 */
public class ExtendedCoordinateExample
{

  public static void main(String args[])
  {
    ExtendedCoordinateSequenceFactory seqFact = ExtendedCoordinateSequenceFactory.instance();

    ExtendedCoordinate[] array1 = new ExtendedCoordinate[] {
      new ExtendedCoordinate(0, 0, 0, 91),
      new ExtendedCoordinate(10, 0, 0, 92),
      new ExtendedCoordinate(10, 10, 0, 93),
      new ExtendedCoordinate(0, 10, 0, 94),
      new ExtendedCoordinate(0, 0, 0, 91),
    };
    CoordinateSequence seq1 = seqFact.create(array1);

    CoordinateSequence seq2 = seqFact.create(
    new ExtendedCoordinate[] {
      new ExtendedCoordinate(5, 5, 0, 91),
      new ExtendedCoordinate(15, 5, 0, 92),
      new ExtendedCoordinate(15, 15, 0, 93),
      new ExtendedCoordinate(5, 15, 0, 94),
      new ExtendedCoordinate(5, 5, 0, 91),
    });

    GeometryFactory fact = new GeometryFactory(
        ExtendedCoordinateSequenceFactory.instance());

    Geometry g1 = fact.createPolygon(fact.createLinearRing(seq1), null);
    Geometry g2 = fact.createPolygon(fact.createLinearRing(seq2), null);

    System.out.println("WKT for g1: " + g1);
    System.out.println("Internal rep for g1: " + ((Polygon) g1).getExteriorRing().getCoordinateSequence());

    System.out.println("WKT for g2: " + g2);
    System.out.println("Internal rep for g2: " + ((Polygon) g2).getExteriorRing().getCoordinateSequence());

    Geometry gInt = g1.intersection(g2);

    System.out.println("WKT for gInt: " + gInt);
    System.out.println("Internal rep for gInt: " + ((Polygon) gInt).getExteriorRing().getCoordinateSequence());
  }

  public ExtendedCoordinateExample() {
  }


}
