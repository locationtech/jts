/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainBuilder;
import org.locationtech.jts.index.chain.MonotoneChainOverlapAction;
import org.locationtech.jts.index.hprtree.HPRtree;
import org.locationtech.jts.noding.SegmentString;

class EdgeSetIntersector {

  private HPRtree index = new HPRtree();
  private Envelope envelope;
  private List<MonotoneChain> monoChains = new ArrayList<MonotoneChain>();
  private int idCounter = 0;
	private boolean hasAEdges;
	private boolean hasBEdges;
  
  public EdgeSetIntersector(List<RelateSegmentString> edgesA, List<RelateSegmentString> edgesB, Envelope env) {
    this.envelope = env;
    hasAEdges = addEdges(edgesA);
    hasBEdges = addEdges(edgesB);
    // build index to ensure thread-safety
    index.build();
  }

  private boolean addEdges(Collection<RelateSegmentString> segStrings)
  {
  	int itemCount = 0;
    for (SegmentString ss : segStrings) {
    	itemCount += addToIndex(ss);
    }
    return itemCount > 0;
  }

  private int addToIndex(SegmentString segStr)
  {
  	int itemCount = 0;
    List<MonotoneChain> segChains = MonotoneChainBuilder.getChains(segStr.getCoordinates(), segStr);
    for (MonotoneChain mc : segChains ) {
      if (envelope == null || envelope.intersects(mc.getEnvelope())) {
        mc.setId(idCounter ++);
        index.insert(mc.getEnvelope(), mc);
        monoChains.add(mc);
        itemCount++;
      }
    }
    return itemCount;
  }
  
  public void process(EdgeSegmentIntersector intersector) {
  	//-- linework from both inputs must be in tree to do processing
  	if (! (hasAEdges && hasBEdges))
  		return;
  	
    MonotoneChainOverlapAction overlapAction = new EdgeSegmentOverlapAction(intersector);

    for (MonotoneChain queryChain : monoChains) {
      List<MonotoneChain> overlapChains = index.query(queryChain.getEnvelope());
      for (MonotoneChain testChain : overlapChains) {
         /**
         * following test makes sure we only compare each pair of chains once
         * and that we don't compare a chain to itself
         */
        if (testChain.getId() <= queryChain.getId())
          continue;
      
        testChain.computeOverlaps(queryChain, overlapAction);
        if (intersector.isDone()) 
          return;
      }
    }  
  }

}
