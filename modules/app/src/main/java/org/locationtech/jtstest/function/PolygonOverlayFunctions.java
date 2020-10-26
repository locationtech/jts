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

package org.locationtech.jtstest.function;

import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jts.operation.polygonize.Polygonizer;

public class PolygonOverlayFunctions 
{

  public static Geometry overlaySR(Geometry g1, Geometry g2, double precisionTol)
  {
    PrecisionModel pm = new PrecisionModel(precisionTol);
    return overlay(g1, g2, pm);
  }
  public static Geometry overlay(Geometry g1, Geometry g2)
  {
    return overlay(g1, g2, null);
  }
  
  private static Geometry overlay(Geometry g1, Geometry g2, PrecisionModel pm)
  {
    GeometryFactory geomFact = g1.getFactory();

    List lines = LinearComponentExtracter.getLines(g1);
    // add second input's linework, if any
    if (g2 != null)
      LinearComponentExtracter.getLines(g2, lines);
    Geometry inputLines = g1.getFactory().buildGeometry(lines);
    
    Geometry nodedDedupedLinework = node(inputLines, pm);

    // polygonize the result
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(nodedDedupedLinework);
    Collection polys = polygonizer.getPolygons();

    // TODO: use PIP to remove hole polygons
    
    // convert to collection for return
    Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
    return geomFact.createGeometryCollection(polyArray);
  }
  
  private static Geometry node(Geometry inputLines, PrecisionModel pm) {
    if (pm == null) {
      return OverlayNGRobust.overlay(inputLines, null, OverlayNG.UNION);
    }
    return OverlayNG.overlay(inputLines, null, OverlayNG.UNION, pm);
  }

}
