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
  public static double maximumDistance(Envelope env1, Envelope env2)
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
  
  public static double minMaxDistance(Envelope a, Envelope b)
  {
    double aminx = a.getMinX();
    double aminy = a.getMinY();
    double amaxx = a.getMaxX();
    double amaxy = a.getMaxY();
    double bminx = b.getMinX();
    double bminy = b.getMinY();
    double bmaxx = b.getMaxX();
    double bmaxy = b.getMaxY();
    
    double dist =         maxDistance(aminx, aminy, aminx, amaxy, bminx, bminy, bminx, bmaxy);
    dist = Math.min(dist, maxDistance(aminx, aminy, aminx, amaxy, bminx, bminy, bmaxx, bminy));
    dist = Math.min(dist, maxDistance(aminx, aminy, aminx, amaxy, bmaxx, bmaxy, bminx, bmaxy));
    dist = Math.min(dist, maxDistance(aminx, aminy, aminx, amaxy, bmaxx, bmaxy, bmaxx, bminy));
  
    dist = Math.min(dist, maxDistance(aminx, aminy, amaxx, aminy, bminx, bminy, bminx, bmaxy));
    dist = Math.min(dist, maxDistance(aminx, aminy, amaxx, aminy, bminx, bminy, bmaxx, bminy));
    dist = Math.min(dist, maxDistance(aminx, aminy, amaxx, aminy, bmaxx, bmaxy, bminx, bmaxy));
    dist = Math.min(dist, maxDistance(aminx, aminy, amaxx, aminy, bmaxx, bmaxy, bmaxx, bminy));
    
    dist = Math.min(dist, maxDistance(amaxx, amaxy, aminx, amaxy, bminx, bminy, bminx, bmaxy));
    dist = Math.min(dist, maxDistance(amaxx, amaxy, aminx, amaxy, bminx, bminy, bmaxx, bminy));
    dist = Math.min(dist, maxDistance(amaxx, amaxy, aminx, amaxy, bmaxx, bmaxy, bminx, bmaxy));
    dist = Math.min(dist, maxDistance(amaxx, amaxy, aminx, amaxy, bmaxx, bmaxy, bmaxx, bminy));
    
    dist = Math.min(dist, maxDistance(amaxx, amaxy, amaxx, aminy, bminx, bminy, bminx, bmaxy));
    dist = Math.min(dist, maxDistance(amaxx, amaxy, amaxx, aminy, bminx, bminy, bmaxx, bminy));
    dist = Math.min(dist, maxDistance(amaxx, amaxy, amaxx, aminy, bmaxx, bmaxy, bminx, bmaxy));
    dist = Math.min(dist, maxDistance(amaxx, amaxy, amaxx, aminy, bmaxx, bmaxy, bmaxx, bminy));
    
    return dist;
}

  private static double maxDistance(double ax1, double ay1, double ax2, double ay2, 
      double bx1, double by1, double bx2, double by2) {
    double dist = distance(ax1, ay1, bx1, by1);
    dist = Math.max(dist, distance(ax1, ay1, bx2, by2));
    dist = Math.max(dist, distance(ax2, ay2, bx1, by1));
    dist = Math.max(dist, distance(ax2, ay2, bx2, by2));
    return dist;
  }
}
