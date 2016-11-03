
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.noding;

import java.util.Collection;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;

/**
 * Nodes a set of {@link NodedSegmentString}s completely.
 * The set of segment strings is fully noded;
 * i.e. noding is repeated until no further
 * intersections are detected.
 * <p>
 * Iterated noding using a FLOATING precision model is not guaranteed to converge,
 * due to roundoff error.   
 * This problem is detected and an exception is thrown.
 * Clients can choose to rerun the noding using a lower precision model.
 *
 * @version 1.7
 */
public class IteratedNoder
    implements Noder
{
  public static final int MAX_ITER = 5;

  private PrecisionModel pm;
  private LineIntersector li;
  private Collection nodedSegStrings;
  private int maxIter = MAX_ITER;

  public IteratedNoder(PrecisionModel pm)
  {
    li = new RobustLineIntersector();
    this.pm = pm;
    li.setPrecisionModel(pm);
  }

  /**
   * Sets the maximum number of noding iterations performed before
   * the noding is aborted.
   * Experience suggests that this should rarely need to be changed
   * from the default.
   * The default is MAX_ITER.
   *
   * @param maxIter the maximum number of iterations to perform
   */
  public void setMaximumIterations(int maxIter)
  {
    this.maxIter = maxIter;
  }

  public Collection getNodedSubstrings()  {    return nodedSegStrings;  }

  /**
   * Fully nodes a list of {@link SegmentString}s, i.e. peforms noding iteratively
   * until no intersections are found between segments.
   * Maintains labelling of edges correctly through
   * the noding.
   *
   * @param segStrings a collection of SegmentStrings to be noded
   * @throws TopologyException if the iterated noding fails to converge.
   */
  public void computeNodes(Collection segStrings)
    throws TopologyException
  {
    int[] numInteriorIntersections = new int[1];
    nodedSegStrings = segStrings;
    int nodingIterationCount = 0;
    int lastNodesCreated = -1;
    do {
      node(nodedSegStrings, numInteriorIntersections);
      nodingIterationCount++;
      int nodesCreated = numInteriorIntersections[0];

      /**
       * Fail if the number of nodes created is not declining.
       * However, allow a few iterations at least before doing this
       */
//System.out.println("# nodes created: " + nodesCreated);
      if (lastNodesCreated > 0
          && nodesCreated >= lastNodesCreated
          && nodingIterationCount > maxIter) {
        throw new TopologyException("Iterated noding failed to converge after "
                                    + nodingIterationCount + " iterations");
      }
      lastNodesCreated = nodesCreated;

    } while (lastNodesCreated > 0);
//System.out.println("# nodings = " + nodingIterationCount);
  }


/**
 * Node the input segment strings once
 * and create the split edges between the nodes
 */
  private void node(Collection segStrings, int[] numInteriorIntersections)
  {
    IntersectionAdder si = new IntersectionAdder(li);
    MCIndexNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(si);
    noder.computeNodes(segStrings);
    nodedSegStrings = noder.getNodedSubstrings();
    numInteriorIntersections[0] = si.numInteriorIntersections;
//System.out.println("# intersection tests: " + si.numTests);
  }

}
