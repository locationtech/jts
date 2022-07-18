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
 * invalid linework if found.
 * <p>
 * A polygonal coverage is a set of polygons which may be edge-adjacent but do 
 * not overlap.
 * A polygonal coverage is valid if:
 * <ol>
 * <li>The interiors of all polygons are disjoint
 * This is the case if no polygon has a boundary which intersects the interior of another polygon.
 * <li>If the boundaries of polygons intersect the vertices
 * and line segments of the intersection match exactly.
 * </ol> 
 * Note that this definition allows gaps between the polygons, 
 * as long as the polygons around the gap form a valid coverage according to the above rules.
 * <p>
 * A valid polygonal coverage ensures that coverage algorithms 
 * (such as union or simplification) produce valid results.
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
   * and contains no misaligned (gap) segments according to the 
   * given distance tolerance.
   * The result is an array of linear geometries indicating the locations of invalidities, if any.
   * 
   * @param coverage an array of polygons forming a coverage
   * @param distanceTolerance the distance tolerance for misaligned segments
   * @return an array of linear geometries indicating coverage errors, or nulls
   */
  public static Geometry[] validate(Geometry coverage[], double distanceTolerance) {
    CoverageValidator v = new CoverageValidator(coverage);
    v.setToleranceDistance(distanceTolerance);
    return v.validate();
  }
  
  private Geometry[] coverage;
  private double distanceTolerance = 0.0;

  /**
   * Creates a new coverage validator
   * 
   * @param coverage a array of polygons representing a polygonal coverage
   */
  public CoverageValidator(Geometry[] coverage) {
    this.coverage = coverage;
  }
  
  /**
   * Sets the distance tolerance, if being used for misaligned segment detection.
   * 
   * @param distanceTolerance the distance tolerance
   */
  public void setToleranceDistance(double distanceTolerance) {
    this.distanceTolerance = distanceTolerance;
  }
  
  /**
   * Validates the polygonal coverage.
   * The result is an array of the same size as the input coverage.
   * Each array entry is either null, or if the polygon does not form a valid coverage,
   * a linear geometry containing the boundary segments
   * which intersect polygon interiors, or which are mismatched.
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
    queryEnv.expandBy(distanceTolerance);
    List<Geometry> nearGeomList = index.query(queryEnv);
    //-- the target geometry is returned in the query, so must be removed from the set
    nearGeomList.remove(targetGeom);
    
    Geometry[] nearGeoms = GeometryFactory.toGeometryArray(nearGeomList);
    Geometry result = CoveragePolygonValidator.validate(targetGeom, nearGeoms, distanceTolerance);
    return result.isEmpty() ? null : result;
  }
}
