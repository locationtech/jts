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
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.jts.operation.overlayng.PrecisionReducer;

/**
 * A transformer to reduce the precision of geometry in a 
 * topologically valid way.
 * Repeated points are removed.
 * If geometry elements collapse below their valid length, 
 * they may be removed 
 * by specifying <code>isRemoveCollapsed</code as <code>true</code>.
 * 
 * @author mdavis
 *
 */
class PrecisionReducerTransformer extends GeometryTransformer {
  
  public static Geometry reduce(Geometry geom, PrecisionModel targetPM, boolean isRemoveCollapsed) {
    PrecisionReducerTransformer trans = new PrecisionReducerTransformer(targetPM, isRemoveCollapsed);
    return trans.transform(geom);
  }
  
  private PrecisionModel targetPM;
  private boolean isRemoveCollapsed = false;
  
  PrecisionReducerTransformer(PrecisionModel targetPM, boolean isRemoveCollapsed) {
    this.targetPM = targetPM;
    this.isRemoveCollapsed  = isRemoveCollapsed;
  }
  
  protected CoordinateSequence transformCoordinates(
      CoordinateSequence coordinates, Geometry parent) {
    if (coordinates.size() == 0)
      return null;

    Coordinate[] coordsReduce = reduceCompress(coordinates);

    /**
     * Check if the removal of repeated points collapsed the coordinate
     * list to an invalid length for the type of the parent geometry. It is not
     * necessary to check for Point collapses, since the coordinate list can
     * never collapse to less than one point. If the length is invalid, return
     * the full-length coordinate array first computed, or null if collapses are
     * being removed. (This may create an invalid geometry - the client must
     * handle this.)
     */
    int minLength = 0;
    if (parent instanceof LineString)
      minLength = 2;
    if (parent instanceof LinearRing)
      minLength = LinearRing.MINIMUM_VALID_SIZE;

    /**
     * Handle collapse. If specified return null so parent geometry is removed or empty,
     * otherwise extend to required length.
     */
    if (coordsReduce.length < minLength) {
      if (isRemoveCollapsed) {
        return null;
      }
      coordsReduce = extend(coordsReduce, minLength);
    }
    return factory.getCoordinateSequenceFactory().create(coordsReduce);
  }

  private Coordinate[] extend(Coordinate[] coords, int minLength) {
    if (coords.length >= minLength)
      return coords;
    Coordinate[] exCoords = new Coordinate[minLength];
    for (int i = 0; i < exCoords.length; i++) {
      int iSrc = i < coords.length ? i : coords.length - 1;
      exCoords[i] = coords[iSrc].copy();
    }
    return exCoords;
  }

  private Coordinate[] reduceCompress(CoordinateSequence coordinates) {
    CoordinateList noRepeatCoordList = new CoordinateList();
    // copy coordinates and reduce
    for (int i = 0; i < coordinates.size(); i++) {
      Coordinate coord = coordinates.getCoordinate(i).copy();
      targetPM.makePrecise(coord);
      noRepeatCoordList.add(coord, false);
    }
    // remove repeated points, to simplify geometry as much as possible
    Coordinate[] noRepeatCoords = noRepeatCoordList.toCoordinateArray();
    return noRepeatCoords;
  }

  protected Geometry transformPolygon(Polygon geom, Geometry parent) {
    return reduceArea(geom);
  }

  protected Geometry transformMultiPolygon(MultiPolygon geom, Geometry parent) {
    return reduceArea(geom);
  }

  private Geometry reduceArea(Geometry geom) {
    Geometry reduced = PrecisionReducer.reducePrecision(geom, targetPM);
    return reduced;
  }
}
