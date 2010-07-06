package com.vividsolutions.jtstest.function;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.noding.BasicSegmentString;
import com.vividsolutions.jts.noding.FastNodingValidator;
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
   * @param geom a geometry containing linework to node
   * @param scaleFactor the precision model scale factor to use
   * @return the noded, snap-rounded linework
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
	
  public static Geometry checkNoding(Geometry geom)
  {
    List segs = createSegmentStrings(geom);
    FastNodingValidator nv = new FastNodingValidator(segs);
    nv.setFindAllIntersections(true);
    nv.isValid();
    List intPts = nv.getIntersections();
    Point[] pts = new Point[intPts.size()];
    for (int i = 0; i < intPts.size(); i++) {
      Coordinate coord = (Coordinate) intPts.get(i);
      // use default factory in case intersections are not fixed
      pts[i] = FunctionsUtil.getFactoryOrDefault(null).createPoint(coord);
    }
    return FunctionsUtil.getFactoryOrDefault(null).createMultiPoint(
        pts);
  }
  
  private static List createSegmentStrings(Geometry geom)
  {
    List segs = new ArrayList();
    List lines = LinearComponentExtracter.getLines(geom);
    for (Iterator i = lines.iterator(); i.hasNext(); ) {
      LineString line = (LineString) i.next();
      segs.add(new BasicSegmentString(line.getCoordinates(), null));
    }
    return segs;
  }
}
