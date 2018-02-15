package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.snapround.MCIndexSnapRounder;
import org.locationtech.jts.topology.Label;

public class OverlayNoder {

  private PrecisionModel pm;
  List segStrings = new ArrayList();

  public OverlayNoder(PrecisionModel pm) {
    this.pm = pm;
  }

  public Collection node() {
    Noder sr = new MCIndexSnapRounder(pm);
    sr.computeNodes(segStrings);
    
    //TODO: merge duplicate edges
    
    return sr.getNodedSubstrings();
  }
  
  public void add(Geometry g, int index)
  {
    if (g.isEmpty()) return;

    if (g instanceof Polygon)                 addPolygon((Polygon) g, index);
    /*
                        // LineString also handles LinearRings
    else if (g instanceof LineString)         addLineString((LineString) g);
    else if (g instanceof Point)              addPoint((Point) g);
    else if (g instanceof MultiPoint)         addCollection((MultiPoint) g);
    else if (g instanceof MultiLineString)    addCollection((MultiLineString) g);
    else if (g instanceof MultiPolygon)       addCollection((MultiPolygon) g);
    else if (g instanceof GeometryCollection) addCollection((GeometryCollection) g);
    */
    else  throw new UnsupportedOperationException(g.getClass().getName());
  }

  /**
   * Adds a polygon ring to the graph.
   * Empty rings are ignored.
   * 
   * The left and right topological location arguments assume that the ring is oriented CW.
   * If the ring is in the opposite orientation,
   * the left and right locations must be interchanged.
   */
  private void addPolygonRing(LinearRing lr, int cwLeft, int cwRight, int index)
  {
    // don't bother adding empty holes
    if (lr.isEmpty()) return;
    
    Coordinate[] pts = round(lr.getCoordinates());

    int left  = cwLeft;
    int right = cwRight;
    if (Orientation.isCCW(pts)) {
      left = cwRight;
      right = cwLeft;
    }
    Label lbl = new Label(index, Location.BOUNDARY, left, right);
    add(pts, lbl);
  }

  private void add(Coordinate[] pts, Label label) {
    NodedSegmentString ss = new NodedSegmentString(pts, label);
    segStrings.add(ss);
  }


  private void addPolygon(Polygon p, int index)
  {
    addPolygonRing(
            (LinearRing) p.getExteriorRing(),
            Location.EXTERIOR, Location.INTERIOR, index);

    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) p.getInteriorRingN(i);
      
      // Holes are topologically labelled opposite to the shell, since
      // the interior of the polygon lies on their opposite side
      // (on the left, if the hole is oriented CW)
      addPolygonRing(hole,
          Location.INTERIOR, Location.EXTERIOR, index);
    }
  }
  
  private Coordinate[] round(Coordinate[] pts)  {
    // TODO: reduce precision, remove repeated pts, ensure 4 pts
    return pts;
  }
}
