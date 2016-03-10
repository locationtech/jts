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

/**
 * Finds if two sets of {@link SegmentString}s intersect.
 * Uses indexing for fast performance and to optimize repeated tests
 * against a target set of lines.
 * Short-circuited to return as soon an intersection is found.
 *
 * Immutable and thread-safe.
 *
 * @version 1.7
 */
public class FastSegmentSetIntersectionFinder 
{
	private final SegmentSetMutualIntersector segSetMutInt; 
	// for testing purposes
	// private SimpleSegmentSetMutualIntersector mci;  

	/**
	 * Creates an intersection finder against a given set of segment strings.
	 * 
	 * @param baseSegStrings the segment strings to search for intersections
	 */
	public FastSegmentSetIntersectionFinder(Collection baseSegStrings)
	{
	    segSetMutInt = new MCIndexSegmentSetMutualIntersector(baseSegStrings);
	}
		
	/**
	 * Gets the segment set intersector used by this class.
	 * This allows other uses of the same underlying indexed structure.
	 * 
	 * @return the segment set intersector used
	 */
	public SegmentSetMutualIntersector getSegmentSetIntersector()
	{
		return segSetMutInt;
	}
	
	/**
	 * Tests for intersections with a given set of target {@link SegmentString}s.
	 * 
	 * @param segStrings the SegmentStrings to test
	 * @return true if an intersection is found
	 */
	public boolean intersects(Collection segStrings)
	{
		SegmentIntersectionDetector intFinder = new SegmentIntersectionDetector();
		return intersects(segStrings, intFinder);
	}
	
	/**
	 * Tests for intersections with a given set of target {@link SegmentString}s.
	 * using a given SegmentIntersectionDetector.
	 * 
	 * @param segStrings the SegmentStrings to test
	 * @param intDetector the intersection detector to use
	 * @return true if the detector reports intersections
	 */
	public boolean intersects(Collection segStrings, SegmentIntersectionDetector intDetector)
	{
		segSetMutInt.process(segStrings, intDetector);
 		return intDetector.hasIntersection();
	}
}
