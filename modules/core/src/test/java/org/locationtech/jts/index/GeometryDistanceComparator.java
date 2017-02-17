package org.locationtech.jts.index;

import java.io.Serializable;
import java.util.Comparator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * The Class GeometryDistanceComparator.
 */
public class GeometryDistanceComparator implements Comparator<Geometry>, Serializable{
	
	/** The normal order. */
	boolean normalOrder;

	/** The query center. */
	Point queryCenter;
	
	/**
	 * Instantiates a new Geometry distance comparator.
	 *
	 * @param queryCenter the query center
	 * @param normalOrder The true means puts the least record at the head of this queue. peek() will get the least element. Vice versa.
	 */
	public GeometryDistanceComparator(Point queryCenter, boolean normalOrder)
	{
		this.queryCenter = queryCenter;
		this.normalOrder = normalOrder;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Geometry g1, Geometry g2) {
		double distance1 = g1.getEnvelopeInternal().distance(this.queryCenter.getEnvelopeInternal());
		double distance2 = g2.getEnvelopeInternal().distance(this.queryCenter.getEnvelopeInternal());
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
