
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;


/**
 * @version 1.7
 */
public class GeometryTestFactory {

  public static Coordinate[] createBox(
                        double minx, double miny,
                        int nSide,
                        double segLen)
  {
    int i;
    int ipt = 0;
    Coordinate[] pts = new Coordinate[4 * nSide + 1];

    double maxx = minx + nSide * segLen;
    double maxy = miny + nSide * segLen;

    for (i = 0; i < nSide; i++) {
      double x = minx + i * segLen;
      double y = miny;
      pts[ipt++] = new Coordinate(x, y);
    }
    for (i = 0; i < nSide; i++) {
      double x = maxx;
      double y = miny + i * segLen;
      pts[ipt++] = new Coordinate(x, y);
    }
    for (i = 0; i < nSide; i++) {
      double x = maxx - i * segLen;
      double y = maxy;
      pts[ipt++] = new Coordinate(x, y);
    }
    for (i = 0; i < nSide; i++) {
      double x = minx;
      double y = maxy - i * segLen;
      pts[ipt++] = new Coordinate(x, y);
    }
    pts[ipt++] = new Coordinate(pts[0]);

    return pts;
  }
  public static Polygon createCircle(
                        GeometryFactory fact,
                        double basex,
                        double basey,
                        double size,
                        int nPts)
  {
    Coordinate[] pts = createCircle(basex, basey, size, nPts);
    LinearRing ring = fact.createLinearRing(pts);
    Polygon poly = fact.createPolygon(ring, null);
    return poly;
  }

  /**
   * Creates a circle
   * @param basex the centre x coord
   * @param basey the centre y coord
   * @param size the size of the envelope of the star
   * @param nPts the number of points in the star
   */
  public static Coordinate[] createCircle(
                        double basex,
                        double basey,
                        double size,
                        int nPts)
  {
    Coordinate[] pts = new Coordinate[nPts + 1];

    int iPt = 0;
    double len = size / 2.0;

    for (int i = 0; i < nPts; i++) {
        double ang = i * (2 * Math.PI / nPts);
        double x = len * Math.cos(ang) + basex;
        double y = len * Math.sin(ang) + basey;
        Coordinate pt = new Coordinate(x, y);
        pts[iPt++] = pt;
    }
    pts[iPt] = pts[0];
    return pts;
  }

  public static Polygon createBox(
      GeometryFactory fact,
                        double minx, double miny,
                        int nSide,
                        double segLen)
  {
    Coordinate[] pts = createBox(minx, minx, nSide, segLen);
    LinearRing ring = fact.createLinearRing(pts);
    Polygon poly = fact.createPolygon(ring, null);
    return poly;
  }

  /**
   * Creates a star from a "circular" sine wave
   * @param basex the centre x coord
   * @param basey the centre y coord
   * @param size the size of the envelope of the star
   * @param armLen the length of an arm of the star
   * @param nArms the number of arms of the star
   * @param nPts the number of points in the star
   */
  public static Coordinate[] createSineStar(
                        double basex,
                        double basey,
                        double size,
                        double armLen,
                        int nArms,
                        int nPts)
  {
    double armBaseLen = size / 2 - armLen;
    if (armBaseLen < 0) armBaseLen = 0.5;

    double angInc = 2 * Math.PI / nArms;
    int nArmPt = nPts / nArms;
    if (nArmPt < 5) nArmPt = 5;

    int nPts2 = nArmPt * nArms;
    Coordinate[] pts = new Coordinate[nPts2 + 1];

    int iPt = 0;
    double starAng = 0.0;

    for (int iArm = 0; iArm < nArms; iArm++) {
      for (int iArmPt = 0; iArmPt < nArmPt; iArmPt++) {
        double ang = iArmPt * (2 * Math.PI / nArmPt);
        double len = armLen * (1 - Math.cos(ang) / 2) + armBaseLen;
        double x = len * Math.cos(starAng + iArmPt * angInc / nArmPt) + basex;
        double y = len * Math.sin(starAng + iArmPt * angInc / nArmPt) + basey;
        Coordinate pt = new Coordinate(x, y);
        pts[iPt++] = pt;
      }
      starAng += angInc;
    }
    pts[iPt] = pts[0];
    return pts;
  }

  public static Polygon createSineStar(
                        GeometryFactory fact,
                        double basex,
                        double basey,
                        double size,
                        double armLen,
                        int nArms,
                        int nPts)
  {
    Coordinate[] pts = createSineStar(basex, basey, size, armLen, nArms, nPts);
    LinearRing ring = fact.createLinearRing(pts);
    Polygon poly = fact.createPolygon(ring, null);
    return poly;
  }

}
