/*
 * Copyright (c) 2016 Vivid Solutions, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index.chain;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;


/**
 * Monotone Chains are a way of partitioning the segments of a linestring to
 * allow for fast searching of intersections.
 * They have the following properties:
 * <ol>
 * <li>the segments within a monotone chain never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is equal to the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection.
 * <p>
 * Property 2 allows
 * an efficient binary search to be used to find the intersection points of two monotone chains.
 * For many types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * <p>
 * One of the goals of this implementation of MonotoneChains is to be
 * as space and time efficient as possible. One design choice that aids this
 * is that a MonotoneChain is based on a subarray of a list of points.
 * This means that new arrays of points (potentially very large) do not
 * have to be allocated.
 * <p>
 *
 * MonotoneChains support the following kinds of queries:
 * <ul>
 * <li>Envelope select: determine all the segments in the chain which
 * intersect a given envelope
 * <li>Overlap: determine all the pairs of segments in two chains whose
 * envelopes overlap
 * </ul>
 *
 * This implementation of MonotoneChains uses the concept of internal iterators
 * ({@link MonotoneChainSelectAction} and {@link MonotoneChainOverlapAction})
 * to return the results for queries.
 * This has time and space advantages, since it
 * is not necessary to build lists of instantiated objects to represent the segments
 * returned by the query.
 * Queries made in this manner are thread-safe.
 * <p>
 * MonotoneChains support being assigned an integer id value
 * to provide a total ordering for a set of chains.
 * This can be used during some kinds of processing to 
 * avoid redundant comparisons
 * (i.e. by comparing only chains where the first id is less than the second).
 * <p>
 * MonotoneChains support using an tolerance distance for overlap tests.
 * This allows reporting overlap in situations where 
 * intersection snapping is being used.
 * If this is used the chain envelope must be computed
 * providing an expansion distance using {@link #getEnvelope(double)}.
 *
 * @version 1.7
 */
public class MonotoneChain {

  private Coordinate[] pts;
  private int start, end;
  private Envelope env = null;
  private Object context = null;// user-defined information
  private int id;// useful for optimizing chain comparisons
  //private double overlapDistance;

  /**
   * Creates a new MonotoneChain based on the given array of points.
   * @param pts the points containing the chain
   * @param start the index of the first coordinate in the chain
   * @param end the index of the last coordinate in the chain 
   * @param context a user-defined data object
   */
  public MonotoneChain(Coordinate[] pts, int start, int end, Object context)
  {
    this.pts    = pts;
    this.start  = start;
    this.end    = end;
    this.context = context;
  }

  /**
   * Sets the id of this chain.
   * Useful for assigning an ordering to a set of 
   * chains, which can be used to avoid redundant processing.
   * 
   * @param id an id value
   */
  public void setId(int id) { this.id = id; }
  
  /**
   * Sets the overlap distance used in overlap tests
   * with other chains.
   * 
   * @param distance the distance to buffer overlap tests by
   */
  public void setOverlapDistance(double distance) {
    //this.overlapDistance = distance;
  }
  
  /**
   * Gets the id of this chain.
   * 
   * @return the id value
   */
  public int getId() { return id; }

  /**
   * Gets the user-defined context data value.
   * 
   * @return a data value
   */
  public Object getContext() { return context; }

  /**
   * Gets the envelope of the chain.
   * 
   * @return the envelope of the chain
   */
  public Envelope getEnvelope()
  {
    return getEnvelope(0.0);
  }

  /**
   * Gets the envelope for this chain, 
   * expanded by a given distance.
   * 
   * @param expansionDistance distance to expand the envelope by
   * @return the expanded envelope of the chain
   */
  public Envelope getEnvelope(double expansionDistance)
  {
    if (env == null) {
      /**
       * The monotonicity property allows fast envelope determination
       */
      Coordinate p0 = pts[start];
      Coordinate p1 = pts[end];
      env = new Envelope(p0, p1);
      if (expansionDistance > 0.0)
        env.expandBy(expansionDistance);
    }
    return env;
  }
  
  /**
   * Gets the index of the start of the monotone chain
   * in the underlying array of points.
   * 
   * @return the start index of the chain
   */
  public int getStartIndex()  { return start; }
  
  /**
   * Gets the index of the end of the monotone chain
   * in the underlying array of points.
   * 
   * @return the end index of the chain
   */
  public int getEndIndex()    { return end; }

  /**
   * Gets the line segment starting at <code>index</code>
   * 
   * @param index index of segment
   * @param ls line segment to extract into
   */
  public void getLineSegment(int index, LineSegment ls)
  {
    ls.p0 = pts[index];
    ls.p1 = pts[index + 1];
  }
  /**
   * Return the subsequence of coordinates forming this chain.
   * Allocates a new array to hold the Coordinates
   */
  public Coordinate[] getCoordinates()
  {
    Coordinate coord[] = new Coordinate[end - start + 1];
    int index = 0;
    for (int i = start; i <= end; i++) {
      coord[index++] = pts[i];
    }
    return coord;
  }

  /**
   * Determine all the line segments in the chain whose envelopes overlap
   * the searchEnvelope, and process them.
   * <p>
   * The monotone chain search algorithm attempts to optimize 
   * performance by not calling the select action on chain segments
   * which it can determine are not in the search envelope.
   * However, it *may* call the select action on segments
   * which do not intersect the search envelope.
   * This saves on the overhead of checking envelope intersection
   * each time, since clients may be able to do this more efficiently.
   * 
   * @param searchEnv the search envelope
   * @param mcs the select action to execute on selected segments
   */
  public void select(Envelope searchEnv, MonotoneChainSelectAction mcs)
  {
    computeSelect(searchEnv, start, end, mcs);
  }

  private void computeSelect(
    Envelope searchEnv,
    int start0, int end0,
    MonotoneChainSelectAction mcs )
  {
    Coordinate p0 = pts[start0];
    Coordinate p1 = pts[end0];

//Debug.println("trying:" + p0 + p1 + " [ " + start0 + ", " + end0 + " ]");
    // terminating condition for the recursion
    if (end0 - start0 == 1) {
      //Debug.println("computeSelect:" + p0 + p1);
      mcs.select(this, start0);
      return;
    }
    // nothing to do if the envelopes don't overlap
    if (! searchEnv.intersects(p0, p1))
      return;

    // the chains overlap, so split each in half and iterate  (binary search)
    int mid = (start0 + end0) / 2;

    // Assert: mid != start or end (since we checked above for end - start <= 1)
    // check terminating conditions before recursing
    if (start0 < mid) {
      computeSelect(searchEnv, start0, mid, mcs);
    }
    if (mid < end0) {
      computeSelect(searchEnv, mid, end0, mcs);
    }
  }

  /**
   * Determines the line segments in two chains which may overlap, 
   * and passes them to an overlap action.
   * <p>
   * The monotone chain search algorithm attempts to optimize 
   * performance by not calling the overlap action on chain segments
   * which it can determine do not overlap.
   * However, it *may* call the overlap action on segments
   * which do not actually interact.
   * This saves on the overhead of checking intersection
   * each time, since clients may be able to do this more efficiently.
   * 
   * @param mc the chain to compare to
   * @param mco the overlap action to execute on overlapping segments
   */
  public void computeOverlaps(MonotoneChain mc, MonotoneChainOverlapAction mco)
  {
    computeOverlaps(start, end, mc, mc.start, mc.end, 0.0, mco);
  }

  /**
   * Determines the line segments in two chains which may overlap,
   * using an overlap distance tolerance, 
   * and passes them to an overlap action.
   * 
   * @param mc the chain to compare to
   * @param overlapTolerance the distance tolerance for the overlap test
   * @param mco the overlap action to execute on selected segments
   */
  public void computeOverlaps(MonotoneChain mc, double overlapTolerance, MonotoneChainOverlapAction mco)
  {
    computeOverlaps(start, end, mc, mc.start, mc.end, overlapTolerance, mco);
  }
  
  /**
   * Uses an efficient mutual binary search strategy 
   * to determine which pairs of chain segments 
   * may overlap, and calls the given overlap action on them.
   * 
   * @param start0 the start index of this chain section
   * @param end0 the end index of this chain section
   * @param mc the target monotone chain
   * @param start1 the start index of the target chain section
   * @param end1 the end index of the target chain section  
   * @param overlapTolerance the overlap tolerance distance (may be 0)
   * @param mco the overlap action to execute on selected segments
   */
  private void computeOverlaps(
    int start0, int end0,
    MonotoneChain mc,
    int start1, int end1,
    double overlapTolerance,
    MonotoneChainOverlapAction mco)
  {
//Debug.println("computeIntersectsForChain:" + p00 + p01 + p10 + p11);
    // terminating condition for the recursion
    if (end0 - start0 == 1 && end1 - start1 == 1) {
      mco.overlap(this, start0, mc, start1);
      return;
    }
    // nothing to do if the envelopes of these subchains don't overlap
    if (! overlaps(start0, end0, mc, start1, end1, overlapTolerance)) return;

    // the chains overlap, so split each in half and iterate  (binary search)
    int mid0 = (start0 + end0) / 2;
    int mid1 = (start1 + end1) / 2;

    // Assert: mid != start or end (since we checked above for end - start <= 1)
    // check terminating conditions before recursing
    if (start0 < mid0) {
      if (start1 < mid1) computeOverlaps(start0, mid0, mc, start1,  mid1, overlapTolerance, mco);
      if (mid1 < end1)   computeOverlaps(start0, mid0, mc, mid1,    end1, overlapTolerance, mco);
    }
    if (mid0 < end0) {
      if (start1 < mid1) computeOverlaps(mid0,   end0, mc, start1,  mid1, overlapTolerance, mco);
      if (mid1 < end1)   computeOverlaps(mid0,   end0, mc, mid1,    end1, overlapTolerance, mco);
    }
  }
  
  /**
   * Tests whether the envelope of a section of the chain 
   * overlaps (intersects) the envelope of a section of another target chain.
   * This test is efficient due to the monotonicity property 
   * of the sections (i.e. the envelopes can be are determined 
   * from the section endpoints
   * rather than a full scan).
   * 
   * @param start0 the start index of this chain section
   * @param end0 the end index of this chain section
   * @param mc the target monotone chain
   * @param start1 the start index of the target chain section
   * @param end1 the end index of the target chain section
   * @param overlapTolerance 
   * @return true if the section envelopes overlap
   */
  private boolean overlaps(
      int start0, int end0,
      MonotoneChain mc,
      int start1, int end1, 
      double overlapTolerance)
  {
    if (overlapTolerance > 0.0) {
      return overlaps(pts[start0], pts[end0], mc.pts[start1], mc.pts[end1], overlapTolerance);
    }
    return Envelope.intersects(pts[start0], pts[end0], mc.pts[start1], mc.pts[end1]);
  }
  
  private boolean overlaps(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2, double overlapTolerance)
  {
    double minq = Math.min(q1.x, q2.x);
    double maxq = Math.max(q1.x, q2.x);
    double minp = Math.min(p1.x, p2.x);
    double maxp = Math.max(p1.x, p2.x);

    if( minp > maxq + overlapTolerance )
        return false;
    if( maxp < minq - overlapTolerance )
        return false;

    minq = Math.min(q1.y, q2.y);
    maxq = Math.max(q1.y, q2.y);
    minp = Math.min(p1.y, p2.y);
    maxp = Math.max(p1.y, p2.y);

    if( minp > maxq + overlapTolerance )
        return false;
    if( maxp < minq - overlapTolerance )
        return false;
    return true;
  }

}
