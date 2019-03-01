package org.locationtech.jts.index.strtree;

import org.locationtech.jts.geom.Envelope;

/**
 * Utility functions for working with {@link Envelope}s.
 * 
 * @author mdavis
 *
 */
public class EnvelopeUtil 
{
  /**
   * Computes the maximum distance between the points defining two envelopes.
   * This is the distance between the two corners which are farthest apart.
   * 
   * Note that this is NOT the MinMax distance, which is a tighter bound on 
   * the distance between the points in the envelopes.
   * 
   * @param env1 an envelope
   * @param env2 an envelope
   * @return the maximum distance between the points defining the envelopes
   */
  static double maximumDistance(Envelope env1, Envelope env2)
  {
    double minx = Math.min(env1.getMinX(), env2.getMinX());
    double miny = Math.min(env1.getMinY(), env2.getMinY());
    double maxx = Math.max(env1.getMaxX(), env2.getMaxX());
    double maxy = Math.max(env1.getMaxY(), env2.getMaxY());
    return distance(minx, miny, maxx, maxy);
  }
  
  private static double distance(double x1, double y1, double x2, double y2) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    return Math.sqrt(dx * dx + dy * dy);    
  }
}
