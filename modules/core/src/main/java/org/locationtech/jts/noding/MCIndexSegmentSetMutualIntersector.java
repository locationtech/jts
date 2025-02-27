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
package org.locationtech.jts.noding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainBuilder;
import org.locationtech.jts.index.chain.MonotoneChainOverlapAction;
import org.locationtech.jts.index.strtree.STRtree;


/**
 * Intersects two sets of {@link SegmentString}s using a index based
 * on {@link MonotoneChain}s and a {@link SpatialIndex}.
 *
 * Thread-safe and immutable.
 * 
 * @version 1.7
 */
public class MCIndexSegmentSetMutualIntersector implements SegmentSetMutualIntersector
{
  /**
  * The {@link SpatialIndex} used should be something that supports
  * envelope (range) queries efficiently (such as a 
  * {@link org.locationtech.jts.index.quadtree.Quadtree}
  * or {@link STRtree}.
  */
  private STRtree index = new STRtree();
  private double overlapTolerance = 0.0;
  private Envelope envelope = null;

  /**
   * Constructs a new intersector for a given set of {@link SegmentString}s.
   * 
   * @param baseSegStrings the base segment strings to intersect
   */
  public MCIndexSegmentSetMutualIntersector(Collection baseSegStrings)
  {
    initBaseSegments(baseSegStrings);
  }

  public MCIndexSegmentSetMutualIntersector(Collection baseSegStrings, Envelope env)
  {
    this.envelope  = env;
    initBaseSegments(baseSegStrings);
  }

  public MCIndexSegmentSetMutualIntersector(Collection baseSegStrings, double overlapTolerance)
  {
    initBaseSegments(baseSegStrings);
    this.overlapTolerance  = overlapTolerance;
  }

  /** 
   * Gets the index constructed over the base segment strings.
   * 
   * NOTE: To retain thread-safety, treat returned value as immutable!
   * 
   * @return the constructed index
   */
  public SpatialIndex getIndex() { return index; }

  private void initBaseSegments(Collection<SegmentString> segStrings)
  {
    for (SegmentString ss : segStrings) {
      if (ss.size() == 0)
        continue;
      addToIndex(ss);
    }
    // build index to ensure thread-safety
    index.build();
  }
  
  private void addToIndex(SegmentString segStr)
  {
    List segChains = MonotoneChainBuilder.getChains(segStr.getCoordinates(), segStr);
    for (Iterator i = segChains.iterator(); i.hasNext(); ) {
      MonotoneChain mc = (MonotoneChain) i.next();
      if (envelope == null || envelope.intersects(mc.getEnvelope())) {
        index.insert(mc.getEnvelope(overlapTolerance), mc);
      }
    }
  }

  /**
   * Calls {@link SegmentIntersector#processIntersections(SegmentString, int, SegmentString, int)} 
   * for all <i>candidate</i> intersections between
   * the given collection of SegmentStrings and the set of indexed segments. 
   * 
   * @param segStrings set of segments to intersect
   * @param segInt segment intersector to use
   */
  @Override
  public void process(Collection segStrings, SegmentIntersector segInt)
  {
  	List monoChains = new ArrayList();
    for (Iterator i = segStrings.iterator(); i.hasNext(); ) {
      addToMonoChains((SegmentString) i.next(), monoChains);
    }
    intersectChains(monoChains, segInt);
//    System.out.println("MCIndexBichromaticIntersector: # chain overlaps = " + nOverlaps);
//    System.out.println("MCIndexBichromaticIntersector: # oct chain overlaps = " + nOctOverlaps);
  }

  private void addToMonoChains(SegmentString segStr, List monoChains)
  {
    if (segStr.size() == 0)
      return;
    List segChains = MonotoneChainBuilder.getChains(segStr.getCoordinates(), segStr);
    for (Iterator i = segChains.iterator(); i.hasNext(); ) {
      MonotoneChain mc = (MonotoneChain) i.next();
      if (envelope == null || envelope.intersects(mc.getEnvelope())) {
        monoChains.add(mc);
      }
    }
  }

  private void intersectChains(List monoChains, SegmentIntersector segInt)
  {
    MonotoneChainOverlapAction overlapAction = new SegmentOverlapAction(segInt);

    for (Iterator i = monoChains.iterator(); i.hasNext(); ) {
      MonotoneChain queryChain = (MonotoneChain) i.next();
      Envelope queryEnv = queryChain.getEnvelope(overlapTolerance);
      List overlapChains = index.query(queryEnv);
      for (Iterator j = overlapChains.iterator(); j.hasNext(); ) {
        MonotoneChain testChain = (MonotoneChain) j.next();
        queryChain.computeOverlaps(testChain, overlapTolerance, overlapAction);
        if (segInt.isDone()) return;
      }
    }
  }

  public static class SegmentOverlapAction
      extends MonotoneChainOverlapAction
  {
    private SegmentIntersector si = null;

    public SegmentOverlapAction(SegmentIntersector si)
    {
      this.si = si;
    }

    @Override
    public void overlap(MonotoneChain mc1, int start1, MonotoneChain mc2, int start2)
    {
      SegmentString ss1 = (SegmentString) mc1.getContext();
      SegmentString ss2 = (SegmentString) mc2.getContext();
      si.processIntersections(ss1, start1, ss2, start2);
    }

  }
}
