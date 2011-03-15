package com.vividsolutions.jts.precision;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

/**
 * Computes the robustness parameter of a geometry or 
 * set of geometries.
 * <p>
 * The <b>Robustness Parameter</b> is a measure of
 * what magnitude of perturbation of its vertices can be tolerated
 * by a geometry before it becomes topologically invalid.
 * The concept was introduced by Thompson and Van Oosterom
 * [TV06], based on earlier work by Milenkovic [Mi88].
 * <p>
 * The Robustness Parameter of a geometry G 
 * is defined to be the value <i>r</i>
 * such that "the movement of all points by a distance
 * of <i>r</i> in any direction will 
 * guarantee to leave the geometry valid" [TV06].
 * An equivalent constructive definition [Mi88] is that
 * <i>r</i> is the largest value such:
 * <ol>
 * <li>No two distinct vertices of G are closer than <i>r</i>
 * <li>No vertex of G is closer than <i>r</i> to an edge of G
 * of which the vertex is not an endpoint
 * </ol>
 * If G has only a single vertex (i.e. is a
 * {@link Point}), the value of the parameter 
 * is {@link Double.MAX_VALUE}.
 * If G is a {@link Lineal} geometry, in fact no amount of perturbation
 * will render the geometry invalid.  However, 
 * in this case the Robustness Parameter is still computed
 * according to the constructive definition.
 * 
 * <h3>References</h3>
 * <ul>
 * <li>[Mi88] Milenkovic, V. J., 
 * <i>Verifiable implementations of geometric algorithms 
 * using finite precision arithmetic</i>.
 * in Artificial Intelligence, 377-401. 1988
 * <li>[TV06] Thompson, Rod and van Oosterom, Peter,
 * <i>Interchange of Spatial Data – Inhibiting Factors</i>,
 * Agile 2006, Visegrad, Hungary. 2006
 * </ul>
 * 
 * @author Martin Davis
 *
 */
public class RobustnessParameter 
{
  public static double getParameter(Geometry g)
  {
    RobustnessParameter rp = new RobustnessParameter(g);
    return rp.getParameter();
  }
  
  public static Geometry getGeometry(Geometry g)
  {
    RobustnessParameter rp = new RobustnessParameter(g);
    return rp.getGeometry();
  }
  
  private Geometry inputGeom;
  private double robustnessParam;
  private Coordinate[] robustnessPts;
  
  public RobustnessParameter(Geometry geom)
  {
    inputGeom = geom;
  }
  
  public double getParameter()
  {
    compute();
    return robustnessParam;
  }
  
  public LineString getGeometry()
  {
    compute();
    return inputGeom.getFactory().createLineString(robustnessPts);
  }
  
  private void compute()
  {
    if (robustnessPts != null) return;
    robustnessPts = new Coordinate[2];
    robustnessParam = Double.MAX_VALUE;
    inputGeom.apply(new VertexCoordinateFilter());
  }
  
  private void updateParameter(double candidateValue, Coordinate p0, Coordinate p1)
  {
    if (candidateValue < robustnessParam) {
      robustnessParam = candidateValue;
      robustnessPts[0] = new Coordinate(p0);
      robustnessPts[1] = new Coordinate(p1);
    }
  }
  
  private void updateParameter(double candidateValue, Coordinate p, 
      Coordinate seg0, Coordinate seg1)
  {
    if (candidateValue < robustnessParam) {
      robustnessParam = candidateValue;
      robustnessPts[0] = new Coordinate(p);
      LineSegment seg = new LineSegment(seg0, seg1);
      robustnessPts[1] = new Coordinate(seg.closestPoint(p));
    }
  }
  
  private class VertexCoordinateFilter 
  implements CoordinateFilter
  {
    public VertexCoordinateFilter()
    {
      
    }
    
    public void filter(Coordinate coord) {
      inputGeom.apply(new ComputeRPCoordinateSequenceFilter(coord));
    }
  }
  
  private class ComputeRPCoordinateSequenceFilter 
  implements CoordinateSequenceFilter 
  {
    private Coordinate queryPt;
    
    public ComputeRPCoordinateSequenceFilter(Coordinate queryPt)
    {
      this.queryPt = queryPt;
    }
    public void filter(CoordinateSequence seq, int i) {
      // compare to vertex
      checkVertexDistance(seq.getCoordinate(i));
      
      // compare to segment, if this is one
      if (i > 0) {
        checkSegmentDistance(seq.getCoordinate(i - 1), seq.getCoordinate(i));
      }
    }
    
    private void checkVertexDistance(Coordinate vertex)
    {
      double vertexDist = vertex.distance(queryPt);
      if (vertexDist > 0) {
        
        updateParameter(vertexDist, queryPt, vertex);
      }
    }
    
    private void checkSegmentDistance(Coordinate seg0, Coordinate seg1)
    {
        if (queryPt.equals2D(seg0) || queryPt.equals2D(seg1))
          return;
        double segDist = CGAlgorithms.distancePointLine(queryPt, seg1, seg0);
        if (segDist > 0) 
          updateParameter(segDist, queryPt, seg1, seg0);
    }
    
    public boolean isDone() {
      return false;
    }
    
    public boolean isGeometryChanged() {
      return false;
    }
    
  }
}
