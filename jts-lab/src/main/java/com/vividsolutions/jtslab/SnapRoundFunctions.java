package com.vividsolutions.jtslab;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.noding.snapround.GeometryNoder;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import com.vividsolutions.jtslab.clean.SmallHoleRemover;
import com.vividsolutions.jtslab.snapround.GeometrySnapRounder;
import com.vividsolutions.jtstest.function.FunctionsUtil;

public class SnapRoundFunctions {
  /**
   * Reduces precision pointwise, then snap-rounds.
   * Note that output set may not contain non-unique linework
   * (and thus cannot be used as input to Polygonizer directly).
   * UnaryUnion is one way to make the linework unique.
   * 
   * 
   * @param geom a geometry containing linework to node
   * @param scaleFactor the precision model scale factor to use
   * @return the noded, snap-rounded linework
   */
  public static Geometry snapRoundLines(
      Geometry geom, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);

    Geometry roundedGeom = GeometryPrecisionReducer.reducePointwise(geom, pm);

    List geomList = new ArrayList();
    geomList.add(roundedGeom);

    GeometrySnapRounder noder = new GeometrySnapRounder(pm);
    List lines = noder.node(geomList);

    return FunctionsUtil.getFactoryOrDefault(geom).buildGeometry(lines);
  }
  
  public static Geometry snapRound(
      Geometry geomA, Geometry geomB, 
      double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);

    Geometry roundedGeomA = GeometryPrecisionReducer.reducePointwise(geomA, pm);
    Geometry geomRound = roundedGeomA;
    
    if (geomB != null) {
      Geometry roundedGeomB = GeometryPrecisionReducer.reducePointwise(geomB, pm);
      geomRound = geomA.getFactory().createGeometryCollection(new Geometry[] { roundedGeomA, roundedGeomB });
    }
    
    GeometrySnapRounder noder = new GeometrySnapRounder(pm);
    Geometry snapped = noder.node(geomRound);

    return snapped;
  }
  

}
