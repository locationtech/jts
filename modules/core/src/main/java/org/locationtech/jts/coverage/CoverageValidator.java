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

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;

public class CoverageValidator {
  
  public static Geometry validate(Geometry coverage) {
    return validate(coverage, 0);
  }
  
  public static Geometry validate(Geometry coverage, double distanceTolerance) {
    CoverageValidator v = new CoverageValidator(coverage, distanceTolerance);
    return v.validate();
  }
  
  private Geometry coverage;
  private double distanceTolerance;
  private GeometryFactory geomFactory;

  public CoverageValidator(Geometry coverage, double distanceTolerance) {
    this.coverage = coverage;
    geomFactory = coverage.getFactory();
    this.distanceTolerance = distanceTolerance;
  }
  
  public Geometry validate() {
    STRtree index = new STRtree();
    for (int i = 0; i < coverage.getNumGeometries(); i++) {
      Geometry item = coverage.getGeometryN(i);
      index.insert(item.getEnvelopeInternal(), item);
    }
    
    List<Geometry> resultLines = new ArrayList<Geometry>();
    for (int i = 0; i < coverage.getNumGeometries(); i++) {
      Geometry geom = coverage.getGeometryN(i);
      addValidation(geom, index, resultLines);
    }
    return geomFactory.createGeometryCollection(GeometryFactory.toGeometryArray(resultLines));
  }

  private void addValidation(Geometry geom, STRtree index, List<Geometry> resultLines) {
    Envelope queryEnv = geom.getEnvelopeInternal();
    queryEnv.expandBy(distanceTolerance);
    List<Geometry> nearGeomList = index.query(queryEnv);
    //-- the base geometry is returned in the query, but does not need to be checked
    nearGeomList.remove(geom);
    
    Geometry[] nearGeoms = GeometryFactory.toGeometryArray(nearGeomList);
    Geometry nearGeomColl = geomFactory.createGeometryCollection(nearGeoms);
    //TODO: add to array, to preserve linkage to source polygon
    Geometry result = CoveragePolygonValidator.validate(geom, nearGeomColl, distanceTolerance);
    if (! result.isEmpty()) {
      resultLines.add(result);
    }
  }
}
