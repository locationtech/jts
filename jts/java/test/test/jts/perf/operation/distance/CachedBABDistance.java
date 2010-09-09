package test.jts.perf.operation.distance;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.distance.*;

public class CachedBABDistance 
{

  private static Geometry cacheGeom = null;
  private static IndexedFacetDistance babDist;
  
  public CachedBABDistance() {
    super();
  }

  static double getDistance(Geometry g1, Geometry g2)
  {
    if (cacheGeom != g1) {
      babDist = new IndexedFacetDistance(g1);
      cacheGeom = g1;
    }
    return babDist.getDistance(g2);
  }
}
