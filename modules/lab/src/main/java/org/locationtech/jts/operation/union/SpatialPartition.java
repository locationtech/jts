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

public class SpatialPartition {
  public interface Relation {
    boolean isEquivalent(int i1, int i2);
  }
  
  private STRtree index;
  private Geometry[] geoms;
  private DisjointSets dset;

  public SpatialPartition(Geometry[] geoms, Relation rel) {
    this.geoms = geoms;
    build(rel);
  }

  private void build(Relation rel) {
    loadIndex(geoms);
    
    dset = new DisjointSets(geoms.length);
    //--- cluster the geometries
    for (int i = 0; i < geoms.length; i++) {
      
      final int queryIndex = i;
      Geometry queryGeom = geoms[i];
      index.query(queryGeom.getEnvelopeInternal(), new ItemVisitor() {

        @Override
        public void visitItem(Object item) {
          int itemIndex = (Integer) item;
          if (itemIndex == queryIndex) return;
          // avoid duplicate intersections
          if (itemIndex < queryIndex) return;
          if (dset.inInSameSet(queryIndex,  itemIndex)) return;
          
          if (rel.isEquivalent(queryIndex, itemIndex)) {
            // geometries are in same partition
            dset.merge(queryIndex, itemIndex);            
          }
        }
        
      });
    }
  }
  
  private void loadIndex(Geometry[] geoms) {
    index = new STRtree();
    for (int i = 0; i < geoms.length; i++) {
      index.insert(geoms[i].getEnvelopeInternal(), new Integer(i));
    }
  }
  
  public int getNumSets() {
    return dset.getNumSets();
  }
  public int getSetSize(int s) {
    return dset.getSetSize(s);
  }
  public int getSetItem(int s, int i) {
    return dset.getSetItem(s, i);
  }
}
