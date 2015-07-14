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
    Coordinate[] resultCoords = KdTree.toCoordinates(result);
    return pts.getFactory().createMultiPoint(resultCoords);
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
