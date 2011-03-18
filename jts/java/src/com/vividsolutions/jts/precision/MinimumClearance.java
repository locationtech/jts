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
 * Computes the minimum clearance of a geometry or 
 * set of geometries.
 * <p>
 * The <b>Minimum Clearance</b> is a measure of
 * what magnitude of perturbation of its vertices can be tolerated
 * by a geometry before it becomes topologically invalid.
 * The concept was introduced by Thompson and Van Oosterom
 * [TV06], based on earlier work by Milenkovic [Mi88].
 * <p>
 * The Minimum Clearance of a geometry G 
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
 * {@link Point}), the value of the minimum clearance 
 * is {@link Double.MAX_VALUE}.
 * If G is a {@link Lineal} geometry, 
 * then in fact no amount of perturbation
 * will render the geometry invalid.  However, 
 * in this case the Minimum Clearance is still computed
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
public class MinimumClearance 
{
  public static double getDistance(Geometry g)
  {
    MinimumClearance rp = new MinimumClearance(g);
    return rp.getDistance();
  }
  
  public static Geometry getLine(Geometry g)
  {
    MinimumClearance rp = new MinimumClearance(g);
    return rp.getLine();
  }
  
  private Geometry inputGeom;
  private double minClearance;
  private Coordinate[] minClearancePts;
  
  public MinimumClearance(Geometry geom)
  {
    inputGeom = geom;
  }
  
  public double getDistance()
  {
    compute();
    return minClearance;
  }
  
  public LineString getLine()
  {
    compute();
    return inputGeom.getFactory().createLineString(minClearancePts);
  }
  
  private void compute()
  {
    if (minClearancePts != null) return;
    minClearancePts = new Coordinate[2];
    minClearance = Double.MAX_VALUE;
    inputGeom.apply(new VertexCoordinateFilter());
  }
  
  private void updateParameter(double candidateValue, Coordinate p0, Coordinate p1)
  {
    if (candidateValue < minClearance) {
      minClearance = candidateValue;
      minClearancePts[0] = new Coordinate(p0);
      minClearancePts[1] = new Coordinate(p1);
    }
  }
  
  private void updateParameter(double candidateValue, Coordinate p, 
      Coordinate seg0, Coordinate seg1)
  {
    if (candidateValue < minClearance) {
      minClearance = candidateValue;
      minClearancePts[0] = new Coordinate(p);
      LineSegment seg = new LineSegment(seg0, seg1);
      minClearancePts[1] = new Coordinate(seg.closestPoint(p));
    }
  }
  
  private class VertexCoordinateFilter 
  implements CoordinateFilter
  {
    public VertexCoordinateFilter()
    {
      
    }
    
    public void filter(Coordinate coord) {
      inputGeom.apply(new ComputeMCCoordinateSequenceFilter(coord));
    }
  }
  
  private class ComputeMCCoordinateSequenceFilter 
  implements CoordinateSequenceFilter 
  {
    private Coordinate queryPt;
    
    public ComputeMCCoordinateSequenceFilter(Coordinate queryPt)
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
