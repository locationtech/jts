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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.polygonize.Polygonizer;


public class PolygonizeFunctions {

  private static Geometry polygonize(Geometry g, boolean extractOnlyPolygonal) {
    List lines = LineStringExtracter.getLines(g);
    Polygonizer polygonizer = new Polygonizer(extractOnlyPolygonal);
    polygonizer.add(lines);
    return polygonizer.getGeometry();
    /*
    Collection polys = polygonizer.getPolygons();
    Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
    return g.getFactory().createGeometryCollection(polyArray);
    */
  }
  public static Geometry polygonize(Geometry g)
  {
    return polygonize(g, false);
  }
  public static Geometry polygonizePolygonal(Geometry g)
  {
    return polygonize(g, true);
  }
  public static Geometry polygonizeDangles(Geometry g)
  {
    List lines = LineStringExtracter.getLines(g);
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    Collection geom = polygonizer.getDangles();
    return g.getFactory().buildGeometry(geom);
  }
  public static Geometry polygonizeCutEdges(Geometry g)
  {
    List lines = LineStringExtracter.getLines(g);
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    Collection geom = polygonizer.getCutEdges();
    return g.getFactory().buildGeometry(geom);
  }
  public static Geometry polygonizeInvalidRingLines(Geometry g)
  {
    List lines = LineStringExtracter.getLines(g);
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    Collection geom = polygonizer.getInvalidRingLines();
    return g.getFactory().buildGeometry(geom);
  }
  public static Geometry polygonizeAllErrors(Geometry g)
  {
    List lines = LineStringExtracter.getLines(g);
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    List errs = new ArrayList();
    errs.addAll(polygonizer.getDangles());
    errs.addAll(polygonizer.getCutEdges());
    errs.addAll(polygonizer.getInvalidRingLines());
    return g.getFactory().buildGeometry(errs);
  }

}
