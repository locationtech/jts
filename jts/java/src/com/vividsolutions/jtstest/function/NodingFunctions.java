package com.vividsolutions.jtstest.function;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.noding.snapround.GeometryNoder;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

public class NodingFunctions 
{

  /**
   * Reduces precision pointwise, then snap-rounds.
   * Note that output set may not contain non-unique linework
   * (and thus cannot be used as input to Polygonizer directly).
   * UnaryUnion is one way to make the linework unique.
   * 
   * 
   * @param geom
   * @param scaleFactor
   * @return
   */
	public static Geometry snapRoundWithPointwisePrecisionReduction(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);

		Geometry roundedGeom = GeometryPrecisionReducer.reducePointwise(geom, pm);

		List geomList = new ArrayList();
		geomList.add(roundedGeom);
		
		GeometryNoder noder = new GeometryNoder(pm);
		List lines = noder.node(geomList);
		
    return FunctionsUtil.getFactoryOrDefault(geom).buildGeometry(lines);
	}
	
}
