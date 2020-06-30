/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.noding.snapround;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdNodeVisitor;
import org.locationtech.jts.index.kdtree.KdTree;

/**
 * An index which creates {@link HotPixel}s for provided points,
 * and allows performing range queries on them.
 * 
 * @author mdavis
 *
 */
class HotPixelIndex {
  private PrecisionModel precModel;
  private double scaleFactor;

  /**
   * Use a kd-tree to index the pixel centers for optimum performance.
   * Since HotPixels have an extent, queries to the
   * index must enlarge the query range by a suitable value 
   * (using the pixel width is safest).
   */
  private KdTree index = new KdTree();
  
  public HotPixelIndex(PrecisionModel pm) {
    this.precModel = pm;
    scaleFactor = pm.getScale();
  }
  
  public void add(Coordinate[] pts) {
    for (Coordinate pt : pts) {
      add(pt);
    }
  }
  
  public void add(List<Coordinate> pts) {
    for (Coordinate pt : pts) {
      add(pt);
    }
  }
  
  public HotPixel add(Coordinate p) {
    // TODO: is there a faster way of doing this?
    Coordinate pRound = round(p);
    
    HotPixel hp = find(p);
    if (hp != null) 
      return hp;
    
    // not found, so create a new one
    hp = new HotPixel(pRound, scaleFactor);
    index.insert(hp.getCoordinate(), hp);
    return hp;
  }

  private HotPixel find(Coordinate pixelPt) {
    KdNode kdNode = index.query(pixelPt);
    if (kdNode == null) 
      return null;
    return (HotPixel) kdNode.getData();
  }
  
  private Coordinate round(Coordinate pt) {
    Coordinate p2 = pt.copy();
    precModel.makePrecise(p2);
    return p2;
  }
  
  public void query(Coordinate p0, Coordinate p1, KdNodeVisitor visitor) {
    Envelope queryEnv = new Envelope(p0, p1);
    // expand query range to account for HotPixel extent
    // expand by full width of one pixel to be safe
    queryEnv.expandBy( 1.0 / scaleFactor );
    index.query(queryEnv, visitor);
  }
}
