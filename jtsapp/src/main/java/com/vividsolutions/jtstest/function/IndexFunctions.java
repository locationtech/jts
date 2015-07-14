package com.vividsolutions.jtstest.function;


import java.util.List;

import test.jts.util.IOUtil;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.kdtree.KdNode;
import com.vividsolutions.jts.index.kdtree.KdTree;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

public class IndexFunctions
{
  public static Geometry kdTree(Geometry pts, Geometry query, double tolerance)
  {
    KdTree index = build(pts, tolerance);
    List result = index.query(query.getEnvelopeInternal());
    Coordinate[] resultCoords = nodeCoords(result);
    return pts.getFactory().createMultiPoint(resultCoords);
  }

  private static Coordinate[] nodeCoords(List result) {
   Coordinate[] coord = new Coordinate[result.size()];
   for (int i = 0; i < result.size(); i++) {
     coord[i] = ((KdNode) result.get(i)).getCoordinate();
   }
   return coord;
  }

  private static KdTree build(Geometry geom, double tolerance) {
    final KdTree index = new KdTree(tolerance);
    Coordinate[] pt = geom.getCoordinates();
    for (int i = 0; i < pt.length; i++) {
      index.insert(pt[i]);
    }
    return index;
  }
}
