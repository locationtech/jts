package org.locationtech.jts.index.strtree;

import java.io.Serializable;
import java.util.Comparator;


/**
 * The Class BoundablePairDistanceComparator. It implements Java comparator and is used 
 * as a parameter to sort the BoundablePair list.
 */
public class BoundablePairDistanceComparator implements Comparator<BoundablePair>, Serializable{
	
	/** The normal order. */
	boolean normalOrder;

	/**
	 * Instantiates a new boundable pair distance comparator.
	 *
	 * @param normalOrder The true means puts the least record at the head of this queue.
	 * This is the natural order. PriorityQueue peek() will get the least element. Vice versa.
	 */
	public BoundablePairDistanceComparator(boolean normalOrder)
	{
		this.normalOrder = normalOrder;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(BoundablePair p1, BoundablePair p2) {
		double distance1 = p1.getDistance();
		double distance2 = p2.getDistance();
		if(this.normalOrder)
		{
			if (distance1 > distance2) {
				return 1;
			} else if (distance1 == distance2) {
				return 0;
			}
			return -1;
		}
		else
		{
			if (distance1 > distance2) {
				return -1;
			} else if (distance1 == distance2) {
				return 0;
			}
			return 1;
		}

	}
}
