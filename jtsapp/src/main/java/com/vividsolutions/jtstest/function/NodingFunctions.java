package com.vividsolutions.jtstest.function;

import java.util.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.noding.BasicSegmentString;
import com.vividsolutions.jts.noding.FastNodingValidator;
import com.vividsolutions.jts.noding.IntersectionAdder;
import com.vividsolutions.jts.noding.MCIndexNoder;
import com.vividsolutions.jts.noding.NodedSegmentString;
import com.vividsolutions.jts.noding.Noder;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.noding.ScaledNoder;
import com.vividsolutions.jts.noding.SegmentString;
import com.vividsolutions.jts.noding.snapround.GeometryNoder;
import com.vividsolutions.jts.noding.snapround.MCIndexSnapRounder;
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
  
  public static Geometry MCIndexNoding(Geometry geom, double scaleFactor)
  {
    List segs = createNodedSegmentStrings(geom);
    PrecisionModel fixedPM = new PrecisionModel(scaleFactor);
    Noder noder = new MCIndexNoder(new IntersectionAdder(new RobustLineIntersector()));
    noder.computeNodes(segs);
    Collection nodedSegStrings = noder.getNodedSubstrings();
    return fromSegmentStrings(nodedSegStrings);
  }

  /**
   * Runs a ScaledNoder on input.
   * Input vertices should be rounded to precision model.
   * 
   * @param geom
   * @param scaleFactor
   * @return the noded geometry
   */
  public static Geometry scaledNoding(Geometry geom, double scaleFactor)
  {
    List segs = createSegmentStrings(geom);
    PrecisionModel fixedPM = new PrecisionModel(scaleFactor);
    Noder noder = new ScaledNoder(new MCIndexSnapRounder(new PrecisionModel(1.0)),
        fixedPM.getScale());
    noder.computeNodes(segs);
    Collection nodedSegStrings = noder.getNodedSubstrings();
    return fromSegmentStrings(nodedSegStrings);
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
  
  private static List createNodedSegmentStrings(Geometry geom)
  {
    List segs = new ArrayList();
    List lines = LinearComponentExtracter.getLines(geom);
    for (Iterator i = lines.iterator(); i.hasNext(); ) {
      LineString line = (LineString) i.next();
      segs.add(new NodedSegmentString(line.getCoordinates(), null));
    }
    return segs;
  }
  
  private static Geometry fromSegmentStrings(Collection segStrings)
  {
    LineString[] lines = new LineString[segStrings.size()];
    int index = 0;
    for (Iterator i = segStrings.iterator(); i.hasNext(); ) {
      SegmentString ss = (SegmentString) i.next();
      LineString line = FunctionsUtil.getFactoryOrDefault(null).createLineString(ss.getCoordinates());
      lines[index++] = line;
    }
    return FunctionsUtil.getFactoryOrDefault(null).createMultiLineString(lines);
  }
  
  
}
