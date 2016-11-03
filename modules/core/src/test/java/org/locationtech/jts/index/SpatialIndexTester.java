
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
package org.locationtech.jts.index;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;



/**
 * @version 1.7
 */
public class SpatialIndexTester 
{
  private static boolean VERBOSE = false;
  
  private SpatialIndex index;
  private ArrayList sourceData;
  private boolean isSuccess = true;
  
  public SpatialIndexTester() {
  }

  public boolean isSuccess()
  {
    return isSuccess;
  }
  
  public void setSpatialIndex(SpatialIndex index)
  {
    this.index = index;
  }

  public SpatialIndex getSpatialIndex()
  {
    return index;
  }

  public void init() {
    sourceData = new ArrayList();
    addSourceData(0, sourceData);
    addSourceData(OFFSET, sourceData);
    if (VERBOSE) {
      //System.out.println("===============================");
      //System.out.println("Grid Extent: " + (CELL_EXTENT * CELLS_PER_GRID_SIDE));
      //System.out.println("Cell Extent: " + CELL_EXTENT);
      //System.out.println("Feature Extent: " + FEATURE_EXTENT);
      //System.out.println("Cells Per Grid Side: " + CELLS_PER_GRID_SIDE);
      //System.out.println("Offset For 2nd Set Of Features: " + OFFSET);
      //System.out.println("Feature Count: " + sourceData.size());
    }
    insert(sourceData, index);
  }
  
  public void run()
  {
    doTest(index, QUERY_ENVELOPE_EXTENT_1, sourceData);
    doTest(index, QUERY_ENVELOPE_EXTENT_2, sourceData);
  }

  private void insert(List sourceData, SpatialIndex index) {
    for (Iterator i = sourceData.iterator(); i.hasNext(); ) {
      Envelope envelope = (Envelope) i.next();
      index.insert(envelope, envelope);
    }
  }

  private static final double CELL_EXTENT = 20.31;
  private static final int CELLS_PER_GRID_SIDE = 10;
  private static final double FEATURE_EXTENT = 10.1;
  private static final double OFFSET = 5.03;
  private static final double QUERY_ENVELOPE_EXTENT_1 = 1.009;
  private static final double QUERY_ENVELOPE_EXTENT_2 = 11.7;

  private void addSourceData(double offset, List sourceData) {
    for (int i = 0; i < CELLS_PER_GRID_SIDE; i++) {
      double minx = (i * CELL_EXTENT) + offset;
      double maxx = minx + FEATURE_EXTENT;
      for (int j = 0; j < CELLS_PER_GRID_SIDE; j++) {
        double miny = (j * CELL_EXTENT) + offset;
        double maxy = miny + FEATURE_EXTENT;
        Envelope e = new Envelope(minx, maxx, miny, maxy);
        sourceData.add(e);
      }
    }
  }

  private void doTest(SpatialIndex index, double queryEnvelopeExtent, List sourceData) {
   int extraMatchCount = 0;
    int expectedMatchCount = 0;
    int actualMatchCount = 0;
    int queryCount = 0;
    for (int x = 0; x < CELL_EXTENT * CELLS_PER_GRID_SIDE; x+= queryEnvelopeExtent) {
      for (int y = 0; y < CELL_EXTENT * CELLS_PER_GRID_SIDE; y+= queryEnvelopeExtent) {
        Envelope queryEnvelope = new Envelope(x, x+queryEnvelopeExtent, y, y+queryEnvelopeExtent);
        List expectedMatches = intersectingEnvelopes(queryEnvelope, sourceData);
        List actualMatches = index.query(queryEnvelope);
        // since index returns candidates only, it may return more than the expected value
        if (expectedMatches.size() > actualMatches.size()) {
          isSuccess = false;
        }
        extraMatchCount += (actualMatches.size() - expectedMatches.size());
        expectedMatchCount += expectedMatches.size();
        actualMatchCount += actualMatches.size();
        compare(expectedMatches, actualMatches);
        queryCount++;
      }
    }
    if (VERBOSE) {
      //System.out.println("---------------");
      //System.out.println("Envelope Extent: " + queryEnvelopeExtent);
      //System.out.println("Expected Matches: " + expectedMatchCount);
      //System.out.println("Actual Matches: " + actualMatchCount);
      //System.out.println("Extra Matches: " + extraMatchCount);
      //System.out.println("Query Count: " + queryCount);
      //System.out.println("Average Expected Matches: " + (expectedMatchCount/(double)queryCount));
      //System.out.println("Average Actual Matches: " + (actualMatchCount/(double)queryCount));
      //System.out.println("Average Extra Matches: " + (extraMatchCount/(double)queryCount));
    }
  }

  private void compare(List expectedEnvelopes, List actualEnvelopes) {
    //Don't use #containsAll because we want to check using
    //==, not #equals. [Jon Aquino]
    for (Iterator i = expectedEnvelopes.iterator(); i.hasNext(); ) {
      Envelope expected = (Envelope) i.next();
      boolean found = false;
      for (Iterator j = actualEnvelopes.iterator(); j.hasNext(); ) {
        Envelope actual = (Envelope) j.next();
        if (actual.equals(expected)) {
          found = true;
          break;
        }
      }
      if (! found)
        isSuccess = false;
    }
  }

  private List intersectingEnvelopes(Envelope queryEnvelope, List envelopes) {
    ArrayList intersectingEnvelopes = new ArrayList();
    for (Iterator i = envelopes.iterator(); i.hasNext(); ) {
      Envelope candidate = (Envelope) i.next();
      if (candidate.intersects(queryEnvelope)) { intersectingEnvelopes.add(candidate); }
    }
    return intersectingEnvelopes;
  }
}
