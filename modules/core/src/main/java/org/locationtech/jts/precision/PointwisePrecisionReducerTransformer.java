/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.precision;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryTransformer;

/**
 * A transformer to reduce the precision of a geometry pointwise.
 * 
 * @author mdavis
 *
 */
class PointwisePrecisionReducerTransformer extends GeometryTransformer {
  
  public static Geometry reduce(Geometry geom, PrecisionModel targetPM) {
    PointwisePrecisionReducerTransformer trans = new PointwisePrecisionReducerTransformer(targetPM);
    return trans.transform(geom);
  }
  
  private PrecisionModel targetPM;

  PointwisePrecisionReducerTransformer(PrecisionModel targetPM) {
    this.targetPM = targetPM;
  }
  
  protected CoordinateSequence transformCoordinates(
      CoordinateSequence coordinates, Geometry parent) {
    if (coordinates.size() == 0)
      return null;

    Coordinate[] coordsReduce = reducePointwise(coordinates);
    return factory.getCoordinateSequenceFactory().create(coordsReduce);
  }

  private Coordinate[] reducePointwise(CoordinateSequence coordinates) {
    Coordinate[] coordReduce = new Coordinate[coordinates.size()];
    // copy coordinates and reduce
    for (int i = 0; i < coordinates.size(); i++) {
      Coordinate coord = coordinates.getCoordinate(i).copy();
      targetPM.makePrecise(coord);
      coordReduce[i]= coord;
    }
    return coordReduce;
  }

}
