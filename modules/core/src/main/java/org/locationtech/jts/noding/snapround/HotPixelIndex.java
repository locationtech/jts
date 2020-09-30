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

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdNodeVisitor;
import org.locationtech.jts.index.kdtree.KdTree;

/**
 * An index which creates unique {@link HotPixel}s for provided points,
 * and performs range queries on them.
 * The points passed to the index do not needed to be 
 * rounded to the specified scale factor; this is done internally
 * when creating the HotPixels for them.
 *
 * @author mdavis
 *
 */
class HotPixelIndex {
  private PrecisionModel precModel;
  private double scaleFactor;

  /**
   * Use a kd-tree to index the pixel centers for optimum performance.
   * Since HotPixels have an extent, range queries to the
   * index must enlarge the query range by a suitable value
   * (using the pixel width is safest).
   */
  private KdTree index = new KdTree();

  public HotPixelIndex(PrecisionModel pm) {
    this.precModel = pm;
    scaleFactor = pm.getScale();
  }

  /**
   * Utility class to shuffle an array of {@link Coordinate}s using
   * the Fisher-Yates shuffle algorithm
   *
   * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fihser-Yates shuffle</a>
   */
  private static final class CoordinateShuffler implements Iterator<Coordinate> {

    private final Random rnd = new Random(13);
    private final Coordinate[] coordinates;
    private final int[] indices;
    private int index;

    /**
     * Creates an instance of this class
     * @param pts An array of {@link Coordinate}s.
     */
    public CoordinateShuffler(Coordinate[] pts) {
      coordinates = pts;
      indices = new int[pts.length];
      for (int i = 0; i < pts.length; i++)
        indices[i] = i;
      index = pts.length - 1;
    }

    @Override
    public boolean hasNext() {
      return index >= 0;
    }

    @Override
    public Coordinate next() {
      int j = rnd.nextInt(index + 1);
      Coordinate res = coordinates[indices[j]];
      indices[j] = indices[index--];
      return res;
    }
  }

  /**
   * Adds a list of points as non-node pixels.
   *
   * @param pts the points to add
   */
  public void add(Coordinate[] pts) {
    /**
     * Shuffle the points before adding.
     * This avoids having long monontic runs of points
     * causing an unbalanced KD-tree, which would create
     * performance and robustness issues.
     */
    Iterator<Coordinate> it = new CoordinateShuffler(pts);
    while (it.hasNext()) {
      add(it.next());
    }
  }

  /**
   * Adds a list of points as node pixels.
   *
   * @param pts the points to add
   */
  public void addNodes(List<Coordinate> pts) {
    /**
     * Node points are not shuffled, since they are
     * added after the vertex points, and hence the KD-tree should 
     * be reasonably balanced already.
     */
    for (Coordinate pt : pts) {
      HotPixel hp = add(pt);
      hp.setToNode();
    }
  }

  /**
   * Adds a point as a Hot Pixel.
   * If the point has been added already, it is marked as a node.
   *
   * @param p the point to add
   * @return the HotPixel for the point
   */
  public HotPixel add(Coordinate p) {
    // TODO: is there a faster way of doing this?
    Coordinate pRound = round(p);

    HotPixel hp = find(pRound);
    /**
     * Hot Pixels which are added more than once
     * must have more than one vertex in them
     * and thus must be nodes.
     */
    if (hp != null) {
      hp.setToNode();
      return hp;
    }

    /**
     * A pixel containing the point was not found, so create a new one.
     * It is initially set to NOT be a node
     * (but may become one later on).
     */
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

  /**
   * Visits all the hot pixels which may intersect a segment (p0-p1).
   * The visitor must determine whether each hot pixel actually intersects
   * the segment.
   *
   * @param p0 the segment start point
   * @param p1 the segment end point
   * @param visitor the visitor to apply
   */
  public void query(Coordinate p0, Coordinate p1, KdNodeVisitor visitor) {
    Envelope queryEnv = new Envelope(p0, p1);
    // expand query range to account for HotPixel extent
    // expand by full width of one pixel to be safe
    queryEnv.expandBy( 1.0 / scaleFactor );
    index.query(queryEnv, visitor);
  }
}
