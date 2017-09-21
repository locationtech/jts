package org.locationtech.jtslab;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.offsetcurve.OffsetCurve;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurveBuilder;

public class OffsetCurveFunctions {
  
  public static Geometry offsetCurve(Geometry line, double distance) {
    return OffsetCurve.offsetCurve(line, distance);
  }
  
  public static Geometry offsetCurveFamily(Geometry line, double distance, int n) {
    List<Geometry> curves = new ArrayList<Geometry>();
    double d = distance;
    for (int i = 0; i < n; i++) {
      Geometry geom = OffsetCurve.offsetCurve(line, d);
      d += distance;
      curves.add(geom);
    }
    return line.getFactory().buildGeometry(curves);
  }
  
  public static Geometry offsetCurveRaw(Geometry line, double distance) {
    BufferParameters bufParams = new BufferParameters();
    OffsetCurveBuilder ocb = new OffsetCurveBuilder(
        line.getFactory().getPrecisionModel(), bufParams
        );
    Coordinate[] pts = ocb.getOffsetCurve(line.getCoordinates(), distance);
    Geometry curve = line.getFactory().createLineString(pts);
    return curve;
  }
}
