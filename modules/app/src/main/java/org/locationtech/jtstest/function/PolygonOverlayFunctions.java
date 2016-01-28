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

package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.noding.snapround.GeometryNoder;
import org.locationtech.jts.operation.polygonize.Polygonizer;


public class PolygonOverlayFunctions 
{

  public static Geometry overlaySnapRounded(Geometry g1, Geometry g2, double precisionTol)
  {
    PrecisionModel pm = new PrecisionModel(precisionTol);
    GeometryFactory geomFact = g1.getFactory();
    
    List lines = LinearComponentExtracter.getLines(g1);
    // add second input's linework, if any
    if (g2 != null)
      LinearComponentExtracter.getLines(g2, lines);
    List nodedLinework = new GeometryNoder(pm).node(lines);
    // union the noded linework to remove duplicates
    Geometry nodedDedupedLinework = geomFact.buildGeometry(nodedLinework).union();
    
    // polygonize the result
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(nodedDedupedLinework);
    Collection polys = polygonizer.getPolygons();
    
    // convert to collection for return
    Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
    return geomFact.createGeometryCollection(polyArray);
  }

}
