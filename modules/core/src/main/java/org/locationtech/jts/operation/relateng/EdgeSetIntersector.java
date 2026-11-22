/*
 * Copyright (c) 2026 Martin Davis.
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
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainBuilder;
import org.locationtech.jts.index.chain.MonotoneChainOverlapAction;
import org.locationtech.jts.index.hprtree.HPRtree;
import org.locationtech.jts.noding.SegmentString;

/**
 * An edge intersector which extracts monotone chains from two geometries,
 * and intersects all segments they contain 
 * (except pairs of identical segments from a single geometry).
 * In order to detect self-intersections,
 * segments are evaluated for intersection even if they are in the same geometry,
 * 
 * The primary geometry chains are cached to allow them to be reused
 * for multiple intersection processes.
 * This improves performance for large primary geometries. 
 */
class EdgeSetIntersector {

	private List<MonotoneChain> monoChainsA;
  
  public EdgeSetIntersector(List<RelateSegmentString> edgesA) {
  	monoChainsA = createMonotoneChains(edgesA);
  }

  private static List<MonotoneChain> createMonotoneChains(List<RelateSegmentString> segStrings)
  {
  	return createMonotoneChains(segStrings, 0, null);
  }
  
  private static List<MonotoneChain> createMonotoneChains(List<RelateSegmentString> segStrings, int chainId, Envelope envelope)
  {
  	List<MonotoneChain> chains = new ArrayList<MonotoneChain>();
    for (SegmentString ss : segStrings) {
    	chainId = addMonotoneChains(ss, chainId + 1, envelope, chains);
    }
    return chains;
  }

  private static int addMonotoneChains(SegmentString segStr, int chainId, Envelope envelope, List<MonotoneChain> chains)
  {
    List<MonotoneChain> segChains = MonotoneChainBuilder.getChains(segStr.getCoordinates(), segStr);
    for (MonotoneChain mc : segChains ) {
      if (envelope == null || envelope.intersects(mc.getEnvelope())) {
        mc.setId(chainId);
        chains.add(mc);
        chainId++;
      }
    }
    return chainId;
  }
  
	private List<MonotoneChain> extractChains(List<MonotoneChain> chains, Envelope envelope) {
		List<MonotoneChain> chainsInEnv = new ArrayList<MonotoneChain>();
	  for (MonotoneChain mc : chains) {
	  	if (envelope.intersects(mc.getEnvelope())) {
	  		chainsInEnv.add(mc);
	  	}
	  }
	  return chainsInEnv;
	}
	
	/**
	 * Intersects all the segments in the primary geometry
	 * and the segments in a secondary geometry.
	 * Segments are evaluated for intersection even if they are in the same geometry,
	 * in order to detect self-intersections.
	 * 
	 * @param edgesB the segment strings for the secondary geometry
	 * @param constraintEnv the envelope of intersection between the two geometries, or null
	 * @param intersector the segment intersector used to report intersections
	 */
  public void process(List<RelateSegmentString> edgesB, Envelope constraintEnv, EdgeSegmentIntersector intersector) {
  	/**
  	 * It can happen that no chains from a geometry intersect the constraint envelope. 
  	 * If so there is no interaction between the geometry edges,
  	 * and processing can be skipped.
  	 */
  	List<MonotoneChain> chainsAExtract = extractChains(monoChainsA, constraintEnv);
    if (chainsAExtract.size() <= 0)
    	return;
    
    /**
     * Ensure B chain ids are unique by sequentially ordering them above all A chains.
     */
    int chainIdBase = monoChainsA.size() + 1;
    List<MonotoneChain> chainsB = createMonotoneChains(edgesB, chainIdBase, constraintEnv);
    if (chainsB.size() <= 0)
    	return;
  	
  	List<MonotoneChain> chainsBoth = new ArrayList<MonotoneChain>();
  	chainsBoth.addAll(chainsAExtract);
  	chainsBoth.addAll(chainsB);
  	
    process(chainsBoth, intersector);  
  }

	private void process(List<MonotoneChain> monoChains, EdgeSegmentIntersector intersector) {
		HPRtree index = createIndex(monoChains);
		
		MonotoneChainOverlapAction overlapAction = new EdgeSegmentOverlapAction(intersector);

    for (MonotoneChain queryChain : monoChains) {
      List<MonotoneChain> overlapChains = index.query(queryChain.getEnvelope());
      for (MonotoneChain testChain : overlapChains) {
         /**
         * Ensure each pair of chains is compared only once
         * and that a chain is not compared to itself
         */
        if (testChain.getId() <= queryChain.getId())
          continue;
      
        testChain.computeOverlaps(queryChain, overlapAction);
        if (intersector.isDone()) 
          return;
      }
    }
	}

	private static HPRtree createIndex(List<MonotoneChain> monoChains) {
		HPRtree index = new HPRtree();
		for (MonotoneChain mc : monoChains) {
			index.insert(mc.getEnvelope(), mc);
		}
		index.build();
		return index;
	}

}
