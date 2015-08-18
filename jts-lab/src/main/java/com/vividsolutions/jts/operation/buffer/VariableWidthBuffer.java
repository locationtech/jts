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
 * Creates a buffer polygon with variable width along a line.
 * <p>
 * Only single lines are supported as input, since buffer widths 
 * generally need to be specified specifically for each line.
 * 
 * @author Martin Davis
 *
 */
public class VariableWidthBuffer {

  /**
   * Creates a buffer polygon along a line with the width interpolated
   * between a start width and an end width.
   *  
   * @param line the line to buffer
   * @param startWidth the buffer width at the start of the line
   * @param endWidth the buffer width at the end of the line
   * @return the variable-width buffer polygon
   */
  public static Geometry buffer(Geometry line, double startWidth,
      double endWidth) {
    double[] width = VariableWidthBuffer.interpolate((LineString) line,
        startWidth, endWidth);
    VariableWidthBuffer vb = new VariableWidthBuffer(line, width);
    return vb.getResult();
  }
  
  public static Geometry bufferAternating(Geometry line, double width1,
      double width2) {
    int nPts = line.getNumPoints();
    double[] width = new double[nPts];
    for (int i = 0; i < width.length; i++) {
      width[i] = (i % 2) == 0 ? width1 : width2;
    }
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
   * @return the array of interpolated values
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

  private static double[] abs(double[] v) {
    double[] a = new double[v.length];
    for (int i = 0; i < v.length; i++) {
      a[i] = Math.abs(v[i]);
    }
    return a;
  }
  
  private LineString line;
  private double[] width;
  private GeometryFactory geomFactory;

  /**
   * Creates a generator for a variable-width line buffer.
   * 
   * @param line
   * @param width
   */
  public VariableWidthBuffer(Geometry line, double[] width) {
    this.line = (LineString) line;
    this.width = abs(width);
    geomFactory = line.getFactory();
  }
  
  /**
   * Gets the computed variable-width line buffer.
   * 
   * @return a polygon
   */
  public Geometry getResult() {
    List parts = new ArrayList();

    Coordinate[] pts = line.getCoordinates();
    for (int i = 0; i < line.getNumPoints(); i++) {
      double dist = width[i] / 2;
      // skip zero-width fillets
      if (dist > 0) {
        Geometry ptBuf = line.getPointN(i).buffer(dist);
        parts.add(ptBuf);
      }

      if (i == 0) continue;
      
      double width0 = width[i - 1];
      double width1 = width[i];
      if (width0 > 0 || width1 > 0) {
        Coordinate[] curvePts = generateSegmentCurve(pts[i - 1], pts[i],
            width0, width1);
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
