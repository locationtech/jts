package org.locationtech.jts.noding.snapround;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.index.strtree.STRtree;

class HotPixelIndex {
  private PrecisionModel precModel;
  private LineIntersector li;
  private double scaleFactor;

  /**
   * HotPixels have an extent, so a suitable index must be used here 
   * (i.e. a KD-tree won't work)
   */
  private STRtree index = new STRtree();
  
  private Map<Coordinate, HotPixel> hotPixelMap = new HashMap<Coordinate, HotPixel>();

  public HotPixelIndex(PrecisionModel pm) {
    this.precModel = pm;
    li = new RobustLineIntersector();
    li.setPrecisionModel(pm);
    scaleFactor = pm.getScale();
  }
  
  public HotPixel add(Coordinate p) {
    // TODO: is there a faster way of doing this?
    Coordinate pRound = round(p);
    
    HotPixel hp = hotPixelMap.get(pRound);
    if (hp != null) 
      return hp;
    
    // not found, so create a new one
    hp = new HotPixel(pRound, scaleFactor, li);
    hotPixelMap.put(pRound,  hp);
    Envelope hpEnv = hp.getSafeEnvelope();
    index.insert(hpEnv, hp);
    return hp;
  }

  private Coordinate round(Coordinate pt) {
    Coordinate p2 = pt.copy();
    precModel.makePrecise(p2);
    return p2;
  }
 
  public List<HotPixel> query(Coordinate p0, Coordinate p1) {
    // TODO: is it possible to avoid envelope creation?
    Envelope queryEnv = new Envelope(p0, p1);
    List<HotPixel> pixels = index.query(queryEnv);
    return pixels;
  }
  
}
