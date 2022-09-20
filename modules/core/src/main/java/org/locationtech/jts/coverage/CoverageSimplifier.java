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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

/**
 * Simplifies the boundaries of the polygons in a polygonal coverage
 * while preserving the original coverage topology.
 * An area-based simplification algorithm is used to provide
 * high-quality results.
 * 
 * @author Martin Davis
 *
 */
public class CoverageSimplifier {
  
  public interface LineSimplifier {
    MultiLineString simplify(MultiLineString lines, double tolerance);
  }
  public static Geometry[] simplify(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplify(tolerance);
  }
  
  private Geometry[] input;
  private GeometryFactory geomFactory;
  private LineSimplifier simplifier;
  
  public CoverageSimplifier(Geometry[] coverage) {
    input = coverage;
    geomFactory = coverage[0].getFactory();
    simplifier = new LineSimplifier() {

      @Override
      public MultiLineString simplify(MultiLineString lines, double tolerance) {
        return (MultiLineString) TPVWSimplifier.simplify(lines, tolerance);
        //return (MultiLineString) TopologyPreservingSimplifier.simplify(lines, tolerance);
      }
      
    };
  }
  
  public Geometry[] simplify(double tolerance) {
    CoverageRingEdges covEdges = CoverageRingEdges.create(input);
    simplifyEdges(covEdges.getEdges(), tolerance);
    Geometry[] result = covEdges.buildCoverage();
    return result;
  }
  
  private void simplifyEdges(List<CoverageEdge> edges, double tolerance) {
    LineString lines[] = new LineString[edges.size()];
    for (int i = 0; i < lines.length; i++) {
      lines[i] = geomFactory.createLineString(edges.get(i).getCoordinates());
    }
    MultiLineString mls = geomFactory.createMultiLineString(lines);
    MultiLineString mlsSimp = simplifier.simplify(mls, tolerance);
    //Assert: mlsSimp.getNumGeometries = lines.length
    
    for (int i = 0; i < lines.length; i++) {
      edges.get(i).setCoordinates(mlsSimp.getGeometryN(i).getCoordinates());
    }
  }
  
}
