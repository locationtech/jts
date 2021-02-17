/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.union;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.strtree.STRtree;

/**
 * Unions a set of polygonal geometries by partitioning them
 * into connected sets of polygons.
 * This works best for a <i>sparse</i> set of polygons.
 * Sparse means that if the geometries are partioned
 * into connected sets, the number of sets
 * is a significant fraction of the total number of geometries.
 * The algorithm used provides performance and memory advantages
 * over the {@link CascadedPolygonUnion} algorithm.
 * It also has the advantage that it does not alter input geometries
 * which do not intersect any other input geometry.
 * <p>
 * Non-sparse sets will work, but may be slower than using cascaded union.
 * 
 * @author mdavis
 *
 */
public class SparsePolygonUnion {
  public static Geometry union(Collection geoms)
  {
    SparsePolygonUnion op = new SparsePolygonUnion(geoms);
    return op.union();
  }

  public static Geometry union(Geometry geoms)
  {
    List polys = PolygonExtracter.getPolygons(geoms);
    SparsePolygonUnion op = new SparsePolygonUnion(polys);
    return op.union();
  }

  private Collection<Geometry> inputPolys;
  private STRtree index;
  private int count;
  private List<PolygonNode> nodes = new ArrayList<PolygonNode>();
  private GeometryFactory geomFactory;

  public SparsePolygonUnion(Collection<Geometry> polys)
  {
    this.inputPolys = polys;
    // guard against null input
    if (inputPolys == null)
      inputPolys = new ArrayList();
  }
  
  public Geometry union()
  {
    if (inputPolys.isEmpty())
      return null;
    geomFactory = ((Geometry) inputPolys.iterator().next()).getFactory();
    
    loadIndex(inputPolys);
    
    //--- cluster the geometries
    for (PolygonNode queryNode : nodes) {
      index.query(queryNode.getEnvelope(), new ItemVisitor() {

        @Override
        public void visitItem(Object item) {
          PolygonNode node = (PolygonNode) item;
          if (item == queryNode) return;
          // avoid duplicate intersections
          if (node.id() > queryNode.id()) return;
          if (queryNode.isInSameCluster(node)) return;
          if (! queryNode.intersects(node)) return;
          queryNode.merge((PolygonNode) item);
        }
        
      });
    }
    
    //--- compute union of each cluster
    List<Geometry> clusterGeom = new ArrayList<Geometry>();
    for (PolygonNode node : nodes) {
      Geometry geom = node.union();
      if (geom == null) continue;
      clusterGeom.add(geom);
    }
    return geomFactory.buildGeometry(clusterGeom);
  }

  private void loadIndex(Collection<Geometry> inputPolys) {
    index = new STRtree();
    for (Geometry geom : inputPolys) {
      add(geom);
    }
  }

  private void add(Geometry poly) {
    PolygonNode node = new PolygonNode(count++, poly);
    nodes.add(node);
    index.insert(poly.getEnvelopeInternal(), node);
  }
  
  static class PolygonNode {

    private int id;
    private boolean isFree = true;
    private Geometry poly;
    private PolygonNode root;
    private List<PolygonNode> nodes = null;

    public PolygonNode(int id, Geometry poly) {
      this.id = id;
      this.poly = poly;
    }

    public int id() {
      return id;
    }

    public Envelope getEnvelope() {
      return poly.getEnvelopeInternal();
    }
    
    public boolean intersects(PolygonNode node) {
      // this would benefit from having a short-circuiting intersects 
      PreparedGeometry pg = PreparedGeometryFactory.prepare(poly);
      return pg.intersects(node.poly);
      //return poly.intersects(node.poly);
    }

     public boolean isInSameCluster(PolygonNode node) {
      if (isFree || node.isFree) return false;
      return root == node.root;
    }

    public void merge(PolygonNode node) {
      if (this == node) 
        throw new IllegalArgumentException("Can't merge node with itself");
      
      if (this.id < node.id) {
        this.add(node);
      }
      else {
        node.add(this);
      }
    }
    
    private void initCluster() {
      isFree = false;
      root = this;
      nodes = new ArrayList<PolygonNode>();
      nodes.add(this);
   }
    
    private void add(PolygonNode node) {
      if (isFree) initCluster();
      
      if (node.isFree) {
        node.isFree = false;
        node.root = root;
        root.nodes.add(node);
      }
      else {
        root.mergeRoot(node.getRoot());
      }
    }

    /**
     * Add the other root's nodes to this root's list.
     * Set the other nodes to have this as root.
     * Free the other root's node list.
     * 
     * @param root the other root node
     */
    private void mergeRoot(PolygonNode root) {
      if (nodes == root.nodes)
        throw new IllegalStateException("Attempt to merge same cluster");
      
      for (PolygonNode node : root.nodes) {
        nodes.add(node);
        node.root = this;
      }
      root.nodes = null;
    }

    private PolygonNode getRoot() {
      if (isFree) throw new IllegalStateException("free node has no root");
      if (root != null) return root;
      return this;
    }
    
    public Geometry union() {
      // free polys are returned unchanged
      if (isFree) return poly;
      // only root nodes can compute a union
      if (root != this) return null;
      return CascadedPolygonUnion.union(toPolygons(nodes));
    }

    private static List<Geometry> toPolygons(List<PolygonNode> nodes) {
      List<Geometry> polys = new ArrayList<Geometry>();
      for (PolygonNode node : nodes) {
        polys.add(node.poly);
      }
      return polys;
    }
    
  }

}
