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

class PrecisionReducerTransformer extends GeometryTransformer {
  
  public static Geometry reduce(Geometry geom, PrecisionModel targetPM) {
     return reduce(geom, targetPM, false);
  }
  
  public static Geometry reduce(Geometry geom, PrecisionModel targetPM, boolean isPointwise) {
    PrecisionReducerTransformer trans = new PrecisionReducerTransformer(targetPM, isPointwise);
    return trans.transform(geom);
  }
  
  private PrecisionModel targetPM;
  private boolean isPointwise = false;

  PrecisionReducerTransformer(PrecisionModel targetPM) {
    this(targetPM, false);
  }
  
  PrecisionReducerTransformer(PrecisionModel targetPM, boolean isPointwise) {
    this.targetPM = targetPM;
    this.isPointwise  = isPointwise;
  }
  
  protected CoordinateSequence transformCoordinates(
      CoordinateSequence coordinates, Geometry parent) {
    if (coordinates.size() == 0)
      return null;

    Coordinate[] coordsReduce;
    if (isPointwise) {
      coordsReduce = reducePointwise(coordinates);
    }
    else {
      coordsReduce = reduceCompress(coordinates);
    }

    /**
     * Check to see if the removal of repeated points collapsed the coordinate
     * List to an invalid length for the type of the parent geometry. It is not
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
      minLength = 4;

    // collapse - return null so parent is removed or empty
    if (coordsReduce.length < minLength) {
      return null;
    }

    return factory.getCoordinateSequenceFactory().create(coordsReduce);
  }

  private Coordinate[] reduceCompress(CoordinateSequence coordinates) {
    CoordinateList noRepeatCoordList = new CoordinateList();
    // copy coordinates and reduce
    for (int i = 0; i < coordinates.size(); i++) {
      Coordinate coord = coordinates.getCoordinate(i).copy();
      targetPM.makePrecise(coord);
      noRepeatCoordList.add(coord, false);
    }
    // remove repeated points, to simplify returned geometry as much as possible
    Coordinate[] noRepeatCoords = noRepeatCoordList.toCoordinateArray();
    return noRepeatCoords;
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

  protected Geometry transformPolygon(Polygon geom, Geometry parent) {
    if (isPointwise) {
      Geometry trans = super.transformPolygon(geom, parent);
      /**
       * For some reason the base transformer may return non-polygonal geoms here.
       * Check this and return an empty polygon instead.
       */
      if (trans instanceof Polygon)
        return trans;
      return factory.createPolygon();
    }
    return reduceArea(geom);
  }

  protected Geometry transformMultiPolygon(MultiPolygon geom, Geometry parent) {
    if (isPointwise) {
      return super.transformMultiPolygon(geom, parent);
    }
    return reduceArea(geom);
  }

  private Geometry reduceArea(Geometry geom) {
    Geometry reduced = PrecisionReducer.reducePrecision(geom, targetPM);
    return reduced;
  }
}
