package org.locationtech.jts.offsetcurve;

import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.operation.buffer.BufferInputLineSimplifier;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurveBuilder;


public class OffsetCurve {

  public static Geometry offsetCurve(Geometry line, double distance) {
    Geometry curveRaw = offsetCurveRaw(line, distance);
    Coordinate[] pts = curveRaw.getCoordinates();
    Coordinate start = pts[0];
    Coordinate end = pts[pts.length-1];
    Geometry noded = node(curveRaw);
    //TODO: ensure start and end are nodes in noded geometry
    Geometry path = ShortestPath.findPath(noded, start, end);
    return path;
  }
  
  private static Geometry offsetCurveRaw(Geometry line, double distance) {
    BufferParameters bufParams = new BufferParameters();
    OffsetCurveBuilder ocb = new OffsetCurveBuilder(
        line.getFactory().getPrecisionModel(), bufParams
        );
    Coordinate[] pts = ocb.getOffsetCurve(line.getCoordinates(), distance);
    
    Coordinate[] ptsSimp = BufferInputLineSimplifier.simplify(pts, distance);
    
    Geometry curve = line.getFactory().createLineString(ptsSimp);
    return curve;
  }
  
  private static Geometry node(Geometry geom)
  {
    Noder noder = new MCIndexNoder(new IntersectionAdder(new RobustLineIntersector()));
    noder.computeNodes( SegmentStringUtil.extractNodedSegmentStrings(geom) );
    return SegmentStringUtil.toGeometry(noder.getNodedSubstrings(), geom.getFactory());
  }
}
