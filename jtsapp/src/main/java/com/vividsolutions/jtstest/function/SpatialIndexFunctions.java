package com.vividsolutions.jtstest.function;


import java.util.List;

import test.jts.util.IOUtil;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.kdtree.KdNode;
import com.vividsolutions.jts.index.kdtree.KdTree;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

public class SpatialIndexFunctions
{
  public static Geometry kdTreeQuery(Geometry pts, Geometry queryEnv, double tolerance)
  {
    KdTree index = buildKdTree(pts, tolerance);
    List result = index.query(queryEnv.getEnvelopeInternal());
    Coordinate[] resultCoords = KdTree.toCoordinates(result);
    return pts.getFactory().createMultiPoint(resultCoords);
  }

  public static Geometry kdTreeQueryRepeated(Geometry pts, Geometry queryEnv, double tolerance)
  {
    KdTree index = buildKdTree(pts, tolerance);
    List result = index.query(queryEnv.getEnvelopeInternal());
    Coordinate[] resultCoords = KdTree.toCoordinates(result, true);
    return pts.getFactory().createMultiPoint(resultCoords);
  }

  private static KdTree buildKdTree(Geometry geom, double tolerance) {
    final KdTree index = new KdTree(tolerance);
    Coordinate[] pt = geom.getCoordinates();
    for (int i = 0; i < pt.length; i++) {
      index.insert(pt[i]);
    }
    return index;
  }
  
  public static Geometry strTreeQuery(Geometry geoms, Geometry queryEnv)
  {
    STRtree index = buildSTRtree(geoms);
    List result = index.query(queryEnv.getEnvelopeInternal());
    return geoms.getFactory().buildGeometry(result);
  }

  private static STRtree buildSTRtree(Geometry geom) {
    final STRtree index = new STRtree();
    geom.apply(new GeometryFilter() {

      @Override
      public void filter(Geometry geom) {
        // only insert atomic geometries
        if (geom instanceof GeometryCollection) return;
        index.insert(geom.getEnvelopeInternal(), geom);
      }
      
    });
    return index;
  }
  
  public static Geometry quadTreeQuery(Geometry geoms, Geometry queryEnv)
  {
    Quadtree index = buildQuadtree(geoms);
    List result = index.query(queryEnv.getEnvelopeInternal());
    return geoms.getFactory().buildGeometry(result);
  }

  private static Quadtree buildQuadtree(Geometry geom) {
    final Quadtree index = new Quadtree();
    geom.apply(new GeometryFilter() {

      @Override
      public void filter(Geometry geom) {
        // only insert atomic geometries
        if (geom instanceof GeometryCollection) return;
        index.insert(geom.getEnvelopeInternal(), geom);
      }
      
    });
    return index;
  }
}
