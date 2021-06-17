package org.locationtech.jts.operation.valid;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

class InvalidIntersectionFinder 
implements SegmentIntersector
{
  LineIntersector li = new RobustLineIntersector();
  private List<Coordinate> intersectionPts = new ArrayList<Coordinate>();
  private boolean hasProperInt = false;
  private boolean hasIntersection = false;
  private boolean hasCrossing= false;
  private boolean hasDoubleTouch = false;
  private boolean isInvertedRingValid;

  InvalidIntersectionFinder(boolean isInvertedRingValid) {
    this.isInvertedRingValid = isInvertedRingValid;
  }
  
  @Override
  public boolean isDone() {
    return hasIntersection || hasDoubleTouch;
  }
  
  public Coordinate getIntersectionLocation() {
    if (intersectionPts.size() == 0) return null;
    return intersectionPts.get(0);
  }

  public boolean hasDoubleTouch() {
    return hasDoubleTouch;
  }
  
  public boolean hasIntersection() {
    return intersectionPts.size() > 0; 
  }
  
  @Override
  public void processIntersections(SegmentString ss0, int segIndex0, SegmentString ss1, int segIndex1) {
    // don't test a segment with itself
    boolean isSameSegString = ss0 == ss1;
    boolean isSameSegment = isSameSegString && segIndex0 == segIndex1;
    if (isSameSegment) return;
    
    hasIntersection = findInvalidIntersection(ss0, segIndex0, ss1, segIndex1);
    
    if (hasIntersection) {
      // found an intersection!
      intersectionPts.add(li.getIntersection(0));
    }    
  }

  private boolean findInvalidIntersection(SegmentString ss0, int segIndex0, 
      SegmentString ss1, int segIndex1) {
    Coordinate p00 = ss0.getCoordinate(segIndex0);
    Coordinate p01 = ss0.getCoordinate(segIndex0 + 1);
    Coordinate p10 = ss1.getCoordinate(segIndex1);
    Coordinate p11 = ss1.getCoordinate(segIndex1 + 1);

    li.computeIntersection(p00, p01, p10, p11);
    
    if (! li.hasIntersection()) return false;
    
    /**
     * Check for an intersection in the interior of both segments.
     */
    hasProperInt = li.isProper();
    if (hasProperInt) 
      return true;
    
    /**
     * Check for collinear segments (which produces two intersection points).
     * This is invalid - either a zero-width spike or gore,
     * or adjacent rings.
     */
    hasProperInt = li.getIntersectionNum() >= 2;
    if (hasProperInt) return true;
    
    /**
     * Now know there is exactly one intersection, 
     * at a vertex of at least one segment.
     */
    Coordinate intPt = li.getIntersection(0);
    
    /**
     * If segments are adjacent the intersection must be their common endpoint.
     * (since they are not collinear).
     * This is valid.
     */
    boolean isSameSegString = ss0 == ss1;
    boolean isAdjacentSegments = isSameSegString && isAdjacentInRing(ss0, segIndex0, segIndex1);
    // Assert: intersection is an endpoint of both segs
    if (isAdjacentSegments) return false;      

    // TODO: allow ring self-intersection - if NOT using OGC semantics
    
    /**
     * Under OGC semantics, rings cannot self-intersect.
     * So the intersection is invalid.
     */
    if (isSameSegString && ! isInvertedRingValid) {
      return true;
    }
    
    /**
     * Optimization: don't analyze intPts at the endpoint of a segment.
     * This is because they are also start points, so don't need to be
     * evaluated twice.
     * This simplifies following logic, by removing the segment endpoint case.
     */
    if (intPt.equals2D(p01) || intPt.equals2D(p11))
      return false;
    
    /**
     * Check topology of a vertex intersection.
     * The ring(s) must not cross.
     */
    Coordinate e00 = p00;
    Coordinate e01 = p01;
    if (intPt.equals2D(p00)) {
      e00 = prevCoordinateInRing(ss0, segIndex0);
      e01 = p01;
    }
    Coordinate e10 = p10;
    Coordinate e11 = p11;
    if (intPt.equals2D(p10)) {
      e10 = prevCoordinateInRing(ss1, segIndex1);
      e11 = p11;
    }
    hasCrossing = AreaNode.isCrossing(intPt, e00, e01, e10, e11); 
    if (hasCrossing) 
      return true;
    
    /**
     * If allowing inverted rings, record a self-touch to support later checking
     * that it does not disconnect the interior.
     */
    if (isSameSegString && isInvertedRingValid) {
      addSelfTouch(ss0, intPt, e00, e01, e10, e11);
    }
    
    /**
     * If the rings are in the same polygon
     * then record the touch to support connected interior checking.
     * 
     * Also check for an invalid double-touch situation,
     * if the rings are different.
     */
    boolean isDoubleTouch = PolygonRing.addTouch((PolygonRing) ss0.getData(), (PolygonRing) ss1.getData(), intPt);
    if (isDoubleTouch && ! isSameSegString) {
      hasDoubleTouch = true;
      return true;
    }
    
    return false;
  }

  private void addSelfTouch(SegmentString ss, Coordinate intPt, Coordinate e00, Coordinate e01, Coordinate e10,
      Coordinate e11) {
    PolygonRing polyRing = (PolygonRing) ss.getData();
    if (polyRing == null) {
      throw new IllegalStateException("SegmentString missing PolygonRing data when checking valid self-touches");
    }
    polyRing.addSelfTouch(intPt, e00, e01, e10, e11);
  }

  /**
   * For a segment string for a ring, gets the coordinate
   * previous to the given index (wrapping if the index is 0)
   * 
   * @param ringSS the ring segment string
   * @param segIndex the segment index
   * @return the coordinate previous to the given segment
   */
  private static Coordinate prevCoordinateInRing(SegmentString ringSS, int segIndex) {
    int prevIndex = segIndex - 1;
    if (prevIndex < 0) {
      prevIndex = ringSS.size() - 2;
    }
    return ringSS.getCoordinate( prevIndex );
  }

  /**
   * Tests if two segments in a closed {@link SegmentString} are adjacent.
   * This handles determining adjacency across the start/end of the ring.
   * 
   * @param ringSS the segment string
   * @param segIndex0 a segment index
   * @param segIndex1 a segment index
   * @return true if the segments are adjacent
   */
  private static boolean isAdjacentInRing(SegmentString ringSS, int segIndex0, int segIndex1) {
    int delta = Math.abs(segIndex1 - segIndex0);
    if (delta <= 1) return true;
    /**
     * A string with N vertices has maximum segment index of N-2.
     * If the delta is at least N-2, the segments must be
     * at the start and end of the string and thus adjacent.
     */
    if (delta >= ringSS.size() - 2) return true;
    return false;
  }
}