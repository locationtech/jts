package com.vividsolutions.jtslab;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtslab.clean.SmallHoleRemover;

public class RemoverFunctions {
  public static Geometry removeSmallHoles(Geometry geom, double areaTolerance)
  {
    return SmallHoleRemover.clean(geom, areaTolerance);
  }
  

}
