package com.vividsolutions.jts.operation.buffer;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

/**
 * Creates a buffer polygon with varying width along a line.
 * <p>
 * Only single lines are supported as input, since buffer widths 
 * generally need to be specified specifically for each line.
 * 
 * @author Martin Davis
 *
 */
public class VariableWidthBuffer {

  /**
   * Creates a buffer polygon around a line with the width interpolated
   * between a start width and an end width.
   *  
   * @param line the line to buffer
   * @param startWidth the buffer width at the start of the line
   * @param endWidth the buffer width at the end of the line
   * @return the varying-width buffer polygon
   */
  public static Geometry buffer(LineString line, double startWidth,
      double endWidth) {
    double[] width = VariableWidthBuffer.interpolate((LineString) line,
        startWidth, endWidth);
    VariableWidthBuffer vb = new VariableWidthBuffer(line, width);
    return vb.getResult();
  }

  /**
   * Computes a list of values for the points along a line by
   * interpolating between values for the start and end point.
   * The interpolation is
   * based on the distance of each point along the line
   * relative to the total line length.
   * 
   * @param line the line to interpolate along
   * @param start the start value 
   * @param end the end value
   * @return
   */
  public static double[] interpolate(LineString line, double start,
      double end) {
    start = Math.abs(start);
    end = Math.abs(end);
    double[] widths = new double[line.getNumPoints()];
    widths[0] = start;
    widths[widths.length - 1] = end;

    double totalLen = line.getLength();
    Coordinate[] pts = line.getCoordinates();
    double currLen = 0;
    for (int i = 1; i < widths.length; i++) {
      double segLen = pts[i].distance(pts[i - 1]);
      currLen += segLen;
      double lenFrac = currLen / totalLen;
      double delta = lenFrac * (end - start);
      widths[i] = start + delta;
    }
    return widths;
  }

  private LineString line;
  private double[] width;
  private GeometryFactory geomFactory;

  public VariableWidthBuffer(LineString line, double[] width) {
    this.line = (LineString) line;
    this.width = abs(width);
    geomFactory = line.getFactory();
  }

  private static double[] abs(double[] v) {
    double[] a = new double[v.length];
    for (int i = 0; i < v.length; i++) {
      a[i] = Math.abs(v[i]);
    }
    return a;
  }
  
  private Geometry getResult() {
    List parts = new ArrayList();

    Coordinate[] pts = line.getCoordinates();
    for (int i = 0; i < line.getNumPoints(); i++) {
      double dist = width[i] / 2;
      Geometry ptBuf = line.getPointN(i).buffer(dist);
      parts.add(ptBuf);

      if (i >= 1) {
        Coordinate[] curvePts = generateSegmentCurve(pts[i - 1], pts[i],
            width[i - 1], width[i]);
        Geometry segBuf = geomFactory.createPolygon(curvePts);
        parts.add(segBuf);
      }
    }

    GeometryCollection partsGeom = geomFactory
        .createGeometryCollection(GeometryFactory.toGeometryArray(parts));
    Geometry buffer = partsGeom.union();
    return buffer;
  }

  private Coordinate[] generateSegmentCurve(Coordinate p0, Coordinate p1,
      double width0, double width1) {
    LineSegment seg = new LineSegment(p0, p1);

    double dist0 = width0 / 2;
    double dist1 = width1 / 2;
    Coordinate s0 = seg.pointAlongOffset(0, dist0);
    Coordinate s1 = seg.pointAlongOffset(1, dist1);
    Coordinate s2 = seg.pointAlongOffset(1, -dist1);
    Coordinate s3 = seg.pointAlongOffset(0, -dist0);

    Coordinate[] pts = new Coordinate[] { s0, s1, s2, s3, s0 };

    return pts;

  }

}
