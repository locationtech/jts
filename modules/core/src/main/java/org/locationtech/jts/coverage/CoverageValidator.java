/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;

/**
 * Validates a polygonal coverage, and returns the locations of
 * invalid polygon boundary segments if found.
 * <p>
 * A polygonal coverage is a set of polygons which may be edge-adjacent but do 
 * not overlap.
 * Coverage algorithms (such as {@link CoverageUnion} or simplification) 
 * generally require the input coverage to be valid to produce correct results.
 * A polygonal coverage is valid if:
 * <ol>
 * <li>The interiors of all polygons do not intersect (are disjoint).
 * This is the case if no polygon has a boundary which intersects the interior of another polygon,
 * and no two polygons are identical.
 * <li>If the boundaries of polygons intersect, the vertices
 * and line segments of the intersection match exactly.
 * </ol> 
 * <p>
 * A valid coverage may contain holes (regions of no coverage).
 * Sometimes it is desired to detect whether coverages contain 
 * narrow gaps between polygons
 * (which can be a result of digitizing error or misaligned data).
 * This class can detect narrow gaps, 
 * by specifying a maximum gap width using {@link #setGapWidth(double)}.
 * Note that this also identifies narrow gaps separating disjoint coverage regions, 
 * and narrow gores.
 * In some situations it may also produce false positives 
 * (linework identified as part of a gap which is actually wider).
 * See {@link CoverageGapFinder} for an alternate way to detect gaps which may be more accurate.
 * 
 * @author Martin Davis
 *
 */
public class CoverageValidator {
  /**
   * Tests whether a polygonal coverage is valid.
   * 
   * @param coverage an array of polygons forming a coverage
   * @return true if the coverage is valid
   */
  public static boolean isValid(Geometry[] coverage) {
    CoverageValidator v = new CoverageValidator(coverage);
    return ! hasInvalidResult(v.validate());     
  }
  
  /**
   * Tests if some element of an array of geometries is a coverage invalidity 
   * indicator.
   * 
   * @param validateResult an array produced by a polygonal coverage validation
   * @return true if the result has at least one invalid indicator
   */
  public static boolean hasInvalidResult(Geometry[] validateResult) {
    for (Geometry geom : validateResult) {
      if (geom != null)
        return true;
    }
    return false;
  }

  /**
   * Validates that a set of polygons forms a valid polygonal coverage,
   * and returns linear geometries indicating the locations of invalidities, if any.
   * 
   * @param coverage an array of polygons forming a coverage
   * @return an array of linear geometries indicating coverage errors, or nulls
   */
  public static Geometry[] validate(Geometry[] coverage) {
    CoverageValidator v = new CoverageValidator(coverage);
    return v.validate();
  }
  
  /**
   * Validates that a set of polygons forms a valid polygonal coverage
   * and contains no gaps narrower than a specified width.
   * The result is an array of linear geometries indicating the locations of invalidities, 
   * or null if the polygon is coverage-valid.
   * 
   * @param coverage an array of polygons forming a coverage
   * @param gapWidth the maximum width of invalid gaps
   * @return an array of linear geometries indicating coverage errors, or nulls
   */
  public static Geometry[] validate(Geometry coverage[], double gapWidth) {
    CoverageValidator v = new CoverageValidator(coverage);
    v.setGapWidth(gapWidth);
    return v.validate();
  }
  
  private Geometry[] coverage;
  private double gapWidth;

  /**
   * Creates a new coverage validator
   * 
   * @param coverage a array of polygons representing a polygonal coverage
   */
  public CoverageValidator(Geometry[] coverage) {
    this.coverage = coverage;
  }
  
  /**
   * Sets the maximum gap width, if narrow gaps are to be detected.
   * 
   * @param gapWidth the maximum width of gaps to detect
   */
  public void setGapWidth(double gapWidth) {
    this.gapWidth = gapWidth;
  }
  
  /**
   * Validates the polygonal coverage.
   * The result is an array of the same size as the input coverage.
   * Each array entry is either null, or if the polygon does not form a valid coverage,
   * a linear geometry containing the boundary segments
   * which intersect polygon interiors, which are mismatched, 
   * or form gaps (if checked).
   * 
   * @return an array of nulls or linear geometries
   */
  public Geometry[] validate() {
    STRtree index = new STRtree();
    for (Geometry geom : coverage) {
      index.insert(geom.getEnvelopeInternal(), geom);
    }
    Geometry[] invalidLines = new Geometry[coverage.length];
    for (int i = 0; i < coverage.length; i++) {
      Geometry geom = coverage[i];
      invalidLines[i] = validate(geom, index);
    }
    return invalidLines;
  }

  private Geometry validate(Geometry targetGeom, STRtree index) {
    Envelope queryEnv = targetGeom.getEnvelopeInternal();
    queryEnv.expandBy(gapWidth);
    List<Geometry> nearGeomList = index.query(queryEnv);
    //-- the target geometry is returned in the query, so must be removed from the set
    nearGeomList.remove(targetGeom);
    
    Geometry[] nearGeoms = GeometryFactory.toGeometryArray(nearGeomList);
    Geometry result = CoveragePolygonValidator.validate(targetGeom, nearGeoms, gapWidth);
    return result.isEmpty() ? null : result;
  }
}
