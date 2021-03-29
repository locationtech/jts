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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.operation.union.DisjointSets.Subsets;

/**
 * Computes a partition of a set of geometries into disjoint subsets, 
 * based on a provided equivalence {@link EquivalenceRelation}.
 * Uses a spatial index for efficient processing.
 * 
 * @author mdavis
 *
 */
public class SpatialPartition {
  
  /**
   * An interface for a function to compute an equivalence relation.
   * An equivalence relation must be symmetric, reflexive and transitive.
   * Examples are <code>intersects</code> or <code>withinDistance</code>.
   *
   */
  public interface EquivalenceRelation {
    /**
     * Tests whether two geometry items are equivalent to each other under the relation.
     * 
     * @param i the index of a geometry
     * @param j the index of another geometry
     * @return true if the geometry items are equivalent
     */
    boolean isEquivalent(int i, int j);
  }
  
  private Subsets sets;
  private Geometry[] geoms;

  public SpatialPartition(Geometry[] geoms, EquivalenceRelation rel) {
    this.geoms = geoms;
    sets = build(geoms, rel);
  }

  /**
   * Gets the number of partitions
   * @return the number of partitions
   */
  public int getCount() {
    return sets.getCount();
  }
  
  /**
   * Gets the number of geometries in a given partition.
   * 
   * @param s the partition index
   * @return the size of the partition
   */
  public int getSize(int s) {
    return sets.getSize(s);
  }
  
  /**
   * Gets the index of a geometry in a partition
   * @param s the partition index
   * @param i the item index
   * @return the item in the partition
   */
  public int getItem(int s, int i) {
    return sets.getItem(s, i);
  }
  
  /**
   * Gets a geometry in a given partition
   * @param s the partition index
   * @param i the item index
   * @return the geometry for the given partition and item index
   */
  public Geometry getGeometry(int s, int i) {
    return geoms[ getItem(s, i) ];
  }
  
  private Subsets build(Geometry[] geoms, EquivalenceRelation rel) {
    STRtree index = createIndex(geoms);
    
    DisjointSets dset = new DisjointSets(geoms.length);
    //--- partition the geometries
    for (int i = 0; i < geoms.length; i++) {
      
      final int queryIndex = i;
      Geometry queryGeom = geoms[i];
      // TODO: allow expanding query env to account for distance-based relations
      index.query(queryGeom.getEnvelopeInternal(), new ItemVisitor() {

        @Override
        public void visitItem(Object item) {
          int itemIndex = (Integer) item;
          
          // avoid reflexive and symmetric comparisons by comparing only lower to higher
          if (itemIndex <= queryIndex) return;
          
          // already in same partition
          if (dset.isInSameSubset(queryIndex,  itemIndex)) return;
          
          if (rel.isEquivalent(queryIndex, itemIndex)) {
            // geometries are in same partition
            dset.merge(queryIndex, itemIndex);            
          }
        }
        
      });
    }
    return dset.subsets();
  }
  
  private STRtree createIndex(Geometry[] geoms) {
    STRtree index = new STRtree();
    for (int i = 0; i < geoms.length; i++) {
      index.insert(geoms[i].getEnvelopeInternal(), new Integer(i));
    }
    return index;
  }
  
}
