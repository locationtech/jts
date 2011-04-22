package com.vividsolutions.jts.index.strtree;


/**
 * A function method which computes the distance
 * between two {@link ItemBoundable}s in an {@link STRtree}.
 * Used for Nearest Neighbour searches.
 * 
 * @author Martin Davis
 *
 */
public interface ItemDistance 
{
  /**
   * Computes the distance between two items.
   * 
   * @param item1
   * @param item2
   * @return the distance between the items
   * 
   * @throws IllegalArgumentException if the metric is not applicable to the arguments
   */
  double distance(ItemBoundable item1, ItemBoundable item2);

}
