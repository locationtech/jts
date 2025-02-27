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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainBuilder;
import org.locationtech.jts.index.hprtree.HPRtree;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.AbstractNode;
import org.locationtech.jts.index.strtree.Boundable;
import org.locationtech.jts.index.strtree.GeometryItemDistance;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.math.MathUtil;


public class SpatialIndexFunctions
{
  public static Geometry kdTreeQuery(Geometry pts, Geometry queryEnv, double tolerance)
  {
    KdTree index = buildKdTree(pts, tolerance);
    // if no query env provided query everything inserted 
    if (queryEnv == null) queryEnv = pts;
    List result = index.query(queryEnv.getEnvelopeInternal());
    Coordinate[] resultCoords = KdTree.toCoordinates(result);
    return pts.getFactory().createMultiPoint(resultCoords);
  }

  private static KdTree indexKDcache = null;
  private static Geometry indexKDGeom = null;
  
  public static Geometry kdTreeQueryCached(Geometry pts, Geometry queryEnv, double tolerance)
  {
    if (indexKDGeom != pts || indexKDcache == null) {
      indexKDcache = buildKdTree(pts, tolerance);
      indexKDGeom = pts;
    }
    // if no query env provided query everything inserted 
    if (queryEnv == null) queryEnv = pts;
    List result = indexKDcache.query(queryEnv.getEnvelopeInternal());
    Coordinate[] resultCoords = KdTree.toCoordinates(result);
    return pts.getFactory().createMultiPoint(resultCoords);
  }

  public static Geometry kdTreeQueryRepeated(Geometry pts, Geometry queryEnv, double tolerance)
  {
    KdTree index = buildKdTree(pts, tolerance);
    // if no query env provided query everything inserted 
    if (queryEnv == null) queryEnv = pts;
    List result = index.query(queryEnv.getEnvelopeInternal());
    Coordinate[] resultCoords = KdTree.toCoordinates(result, true);
    return pts.getFactory().createMultiPoint(resultCoords);
  }

  public static Geometry kdTreeGraphSeed(Geometry geom) {
    return kdTreeGraph(geom, buildKdTreeSeed(geom, 0));
  }

  public static Geometry kdTreeGraph(Geometry geom) {
    return kdTreeGraph(geom, buildKdTree(geom, 0));
  }
  
  private static Geometry kdTreeGraph(Geometry geom, KdTree index) {
    KdNode root = index.getRoot();
    List<Geometry> edges = new ArrayList<Geometry>();
    
    double x = geom.getEnvelopeInternal().centre().getX();
    double xInc = geom.getEnvelopeInternal().getWidth() / 2;
    addGraphEdges(root, true, 0, x, xInc, edges, geom.getFactory());
    return geom.getFactory().buildGeometry(edges);
  }
  
  private static void addGraphEdges(KdNode node, 
      boolean isXLevel, int depth, double x, double xInc,
      List<Geometry> edges, GeometryFactory factory) {
    double xInc2 = xInc / 2;
    KdNode left = node.getLeft();
    if (left != null) {
      double xLeft = x - xInc2;
      Geometry edgeLeft = factory.createLineString(new Coordinate[] {
          new Coordinate(x, -depth), new Coordinate(xLeft, -depth-1)
      });
      edges.add(edgeLeft);
      addGraphEdges(left, ! isXLevel, depth+1, xLeft, xInc2, edges, factory);
    }
    KdNode right = node.getRight();
    if (right != null) {
      double xRight = x + xInc2;
      Geometry edgeRight = factory.createLineString(new Coordinate[] {
          new Coordinate(x, -depth), new Coordinate(xRight, -depth-1)
      });
      edges.add(edgeRight);
      addGraphEdges(right, ! isXLevel, depth+1, xRight, xInc2, edges, factory);
    }
  }

  public static Geometry kdTreeSplits(Geometry geom) {
    return kdTreeSplits(geom, buildKdTree(geom, 0));
  }
  
  public static Geometry kdTreeSplitsSeed(Geometry geom) {
    return kdTreeSplits(geom, buildKdTreeSeed(geom, 0));
  }

  private static Geometry kdTreeSplits(Geometry geom, KdTree index) {
    Envelope extent = geom.getEnvelopeInternal();
    KdNode root = index.getRoot();
    List<Geometry> splits = new ArrayList<Geometry>();
    
    addSplits(root, true, extent, splits, geom.getFactory());
    return geom.getFactory().buildGeometry(splits);
  }
  
  private static void addSplits(KdNode node, boolean isXLevel, Envelope extent, List<Geometry> splits,
      GeometryFactory factory) {
    double splitVal = node.splitValue(isXLevel);
    Geometry splitLine = createSplitLine(extent, splitVal, isXLevel, factory);
    splits.add(splitLine);
    
    KdNode left = node.getLeft();
    if (left != null) {
      addSplits(left, ! isXLevel, splitExtent(extent, splitVal, isXLevel, true), splits, factory);
    }
    KdNode right = node.getRight();
    if (right != null) {
      addSplits(right, ! isXLevel, splitExtent(extent, splitVal, isXLevel, false), splits, factory);
    }
  }

  private static Envelope splitExtent(Envelope extent, double splitVal, boolean isXLevel, boolean isLeft) {
    double xMin = extent.getMinX();
    double yMin = extent.getMinY();
    double xMax = extent.getMaxX();
    double yMax = extent.getMaxY();
    if (isXLevel) {
      if (isLeft) {
        xMax = splitVal;
      }
      else {
        xMin = splitVal;
      }
    }
    else {
      if (isLeft) {
        yMax = splitVal;
      }
      else {
        yMin = splitVal;
      }
    }
    return new Envelope(xMin, xMax, yMin, yMax);
  }

  private static Geometry createSplitLine(Envelope extent, double splitVal, 
      boolean isXLevel, GeometryFactory factory) {
    
    double x1 = isXLevel ? splitVal : extent.getMinX();
    double y1 = isXLevel ? extent.getMinY() : splitVal;
    double x2 = isXLevel ? splitVal : extent.getMaxX();
    double y2 = isXLevel ? extent.getMaxY() : splitVal;
    
    Coordinate[] pts = { new Coordinate(x1, y1), new Coordinate(x2, y2) };
    return factory.createLineString(pts);
  }

  private static KdTree buildKdTree(Geometry geom, double tolerance) {
    final KdTree index = new KdTree(tolerance);
    Coordinate[] pt = geom.getCoordinates();
    for (int i = 0; i < pt.length; i++) {
      index.insert(pt[i]);
    }
    return index;
  }
  
  private static KdTree buildKdTreeSeed(Geometry geom, double tolerance) {
    final KdTree tree = new KdTree(tolerance);
    Coordinate[] pt = geom.getCoordinates();
    
    //-- seed the tree with some randomly selected points
    int numSeed = pt.length / 100;
    double rand = 0;
    for (int i = 0; i < numSeed; i++) {
      rand = MathUtil.quasirandom(rand);
      int index = (int) (pt.length * rand);
      tree.insert(pt[index]);
    }
    //-- insert all the points
    for (int i = 0; i < pt.length; i++) {
      tree.insert(pt[i]);
    }
    return tree;
  }
  
  public static Geometry strTreeBounds(Geometry geoms)
  {
    STRtree index = new STRtree();
    loadIndex(geoms, index);
    List bounds = new ArrayList();
    addBounds(index.getRoot(), bounds, geoms.getFactory());
    return geoms.getFactory().buildGeometry(bounds);
  }

  private static void addBounds(Boundable bnd, List bounds,
      GeometryFactory factory) {
    // don't include bounds of leaf nodes
    if (! (bnd instanceof AbstractNode)) return;
    
    Envelope env = (Envelope) bnd.getBounds();
    bounds.add(factory.toGeometry(env));
    if (bnd instanceof AbstractNode) {
      AbstractNode node = (AbstractNode) bnd;
      List children = node.getChildBoundables();
      for (Iterator i = children.iterator(); i.hasNext(); ) {
        Boundable child = (Boundable) i.next();
        addBounds(child, bounds, factory);
      }
    }
  }

  public static Geometry hprTreeQuery(Geometry geoms, Geometry queryEnv)
  {
    HPRtree index = new HPRtree();
    loadIndex(geoms, index);
    // if no query env provided query everything inserted 
    if (queryEnv == null) queryEnv = geoms;
    List result = index.query(queryEnv.getEnvelopeInternal());
    return geoms.getFactory().buildGeometry(result);
  }

  private static HPRtree indexHPRcache = null;
  private static Geometry indexHPRGeom = null;
  
  public static Geometry hprTreeQueryCached(Geometry geoms, Geometry queryEnv)
  {
    if (indexHPRGeom != geoms || indexHPRcache == null) {
      indexHPRcache = new HPRtree();
      loadIndex(geoms, indexHPRcache);
      indexHPRGeom = geoms;
    }
    // if no query env provided query everything inserted 
    if (queryEnv == null) queryEnv = geoms;
    List result = indexHPRcache.query(queryEnv.getEnvelopeInternal());
    return geoms.getFactory().buildGeometry(result);
  }

  private static void loadIndex(Geometry geom, SpatialIndex index) {
    geom.apply(new GeometryFilter() {

      @Override
      public void filter(Geometry geom) {
        // only insert atomic geometries
        if (geom instanceof GeometryCollection) return;
        index.insert(geom.getEnvelopeInternal(), geom);
      }
      
    });
  }

  public static Geometry hprTreeBounds(Geometry geoms)
  {
    HPRtree index = new HPRtree();
    loadIndex(geoms, index);
    index.build();
    Envelope[] bounds = index.getBounds();
    Geometry[] polys = new Geometry[bounds.length];
    int i = 0;
    for (Envelope env : bounds) {
      polys[i++] = geoms.getFactory().toGeometry(env);
    }
    return geoms.getFactory().createGeometryCollection(polys);
  }
  
  private static STRtree indexSTRcache = null;
  private static Geometry indexSTRGeom = null;
  
  public static Geometry strTreeQueryCached(Geometry geoms, Geometry queryEnv)
  {
    if (indexSTRGeom != geoms || indexSTRcache == null) {
      indexSTRcache = new STRtree();
      loadIndex(geoms, indexSTRcache);
      indexSTRGeom = geoms;
    }
    // if no query env provided query everything inserted 
    if (queryEnv == null) queryEnv = geoms;
    List result = indexSTRcache.query(queryEnv.getEnvelopeInternal());
    return geoms.getFactory().buildGeometry(result);
  }
  
  public static Geometry strTreeQuery(Geometry geoms, Geometry queryEnv)
  {
    STRtree index = new STRtree();
    loadIndex(geoms, index);
    // if no query env provided query everything inserted 
    if (queryEnv == null) queryEnv = geoms;
    List result = index.query(queryEnv.getEnvelopeInternal());
    return geoms.getFactory().buildGeometry(result);
  }
  
  public static Geometry strTreeNN(Geometry geoms, Geometry geom)
  {
    STRtree index = new STRtree();
    loadIndex(geoms, index);
    Object result = index.nearestNeighbour(geom.getEnvelopeInternal(), geom, new GeometryItemDistance());
    return (Geometry) result;
  }

  public static Geometry strTreeNNInSet(Geometry geoms)
  {
    STRtree index = new STRtree();
    loadIndex(geoms, index);
    Object[] result = index.nearestNeighbour(new GeometryItemDistance());
    Geometry[] resultGeoms = new Geometry[] { (Geometry) result[0], (Geometry) result[1] };
    return geoms.getFactory().createGeometryCollection(resultGeoms);
  }

  public static Geometry strTreeNNk(Geometry geoms, Geometry geom, int k)
  {
    STRtree index = new STRtree();
    loadIndex(geoms, index);
    Object[] knnObjects = index.nearestNeighbour(geom.getEnvelopeInternal(), geom, new GeometryItemDistance(), k);
    List knnGeoms = new ArrayList(Arrays.asList(knnObjects));
    Geometry geometryCollection = geoms.getFactory().buildGeometry(knnGeoms);
    return geometryCollection;
  }
  
  public static Geometry quadTreeQuery(Geometry geoms, Geometry queryEnv)
  {
    Quadtree index = buildQuadtree(geoms);
    // if no query env provided query everything inserted 
    if (queryEnv == null) queryEnv = geoms;
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
  
  public static Geometry monotoneChains(Geometry geom) {
    Coordinate[] pts = geom.getCoordinates();
    List<MonotoneChain> chains = MonotoneChainBuilder.getChains(pts);
    List<LineString> lines = new ArrayList<LineString>();
    for (MonotoneChain mc : chains) {
      Coordinate[] mcPts = mc.getCoordinates();
      LineString line = geom.getFactory().createLineString(mcPts);
      lines.add(line);
    }
    return geom.getFactory().buildGeometry(lines);
  }
  
  /*
  public static Geometry sprTreeBounds(Geometry geom)
  {
    Coordinate[] pts = geom.getCoordinates();
    VertexSequencePackedRtree index = new VertexSequencePackedRtree(pts);
    Envelope[] bounds = index.getBounds();
    Geometry[] polys = new Geometry[bounds.length];
    int i = 0;
    for (Envelope env : bounds) {
      polys[i++] = geom.getFactory().toGeometry(env);
    }
    return geom.getFactory().createGeometryCollection(polys);
  }
  */

}
