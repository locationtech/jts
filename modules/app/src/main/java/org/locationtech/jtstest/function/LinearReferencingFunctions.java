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

package org.locationtech.jtstest.function;


import org.locationtech.jts.geom.*;
import org.locationtech.jts.linearref.LengthIndexedLine;

public class LinearReferencingFunctions
{
  public static Geometry extractPoint(Geometry g, double index)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    Coordinate p = ll.extractPoint(index);
    return g.getFactory().createPoint(p);
  }
  public static Geometry extractLine(Geometry g, double start, double end)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    return ll.extractLine(start, end);
  }
  public static Geometry project(Geometry g, Geometry g2)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    double index = ll.project(g2.getCoordinate());
    Coordinate p = ll.extractPoint(index);
    return g.getFactory().createPoint(p);
  }
  public static double projectIndex(Geometry g, Geometry g2)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    return ll.project(g2.getCoordinate());
  }

}
