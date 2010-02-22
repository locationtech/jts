package com.vividsolutions.jtstest.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LineStringExtracter;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

public class PolygonizeFunctions {

  public static Geometry polygonize(Geometry g)
  {
    List lines = LineStringExtracter.getLines(g);
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    Collection polys = polygonizer.getPolygons();
    Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
    return g.getFactory().createGeometryCollection(polyArray);
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
