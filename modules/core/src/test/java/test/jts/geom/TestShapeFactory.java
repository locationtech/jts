/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.util.GeometricShapeFactory;

public class TestShapeFactory {

  public static Polygon createSquare(Coordinate origin, double size) {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(4);
    Polygon g = gsf.createRectangle();
    // Polygon gRect = gsf.createRectangle();
    // Geometry g = gRect.getExteriorRing();
    return g;
  }

  public static Geometry createSineStar(Coordinate origin, double size, int nPts) {
    SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(2);
    gsf.setNumArms(20);
    Geometry poly = gsf.createSineStar();
    return poly;
  }

  public static Polygon createCircle(Coordinate origin, double size, int nPts) {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    Polygon circle = gsf.createCircle();
    return circle;
  }

  private static double HOLE_SIZE_FACTOR = 0.8;

  public static Geometry createSquareWithCircleHoles(Coordinate origin, double size, int nHoles, int nPtsHole) {
    Polygon square = createSquare(origin, size);

    int gridSide = (int) Math.sqrt(nHoles);
    if ( gridSide * gridSide < nHoles )
      gridSide++;

    double gridSideLen = size / gridSide;
    double holeSize = HOLE_SIZE_FACTOR * gridSideLen;

    LinearRing[] holes = new LinearRing[nHoles];

    double baseX = origin.getX() - (size / 2) + gridSideLen / 2;
    double baseY = origin.getY() - (size / 2) + gridSideLen / 2;

    int index = 0;
    for (int i = 0; i < gridSide; i++) {
      for (int j = 0; j < gridSide; j++) {
        double x = baseX + i * gridSideLen;
        double y = baseY + j * gridSideLen;
        Polygon circle = createCircle(new Coordinate(x, y), holeSize, nPtsHole);
        holes[index++] = circle.getExteriorRing();
      }
    }
    return square.getFactory().createPolygon(square.getExteriorRing(), holes);
  }

  public static Geometry createSlantedEllipses(Coordinate origin, double size, double scaleFactor, int nGeom,
      int nPts) {
    Geometry circles = createCircleRow(origin, size, nGeom, nPts);
    Coordinate centre = circles.getEnvelopeInternal().centre();

    AffineTransformation scaleTrans = AffineTransformation.scaleInstance(1, scaleFactor, centre.getX(), centre.getY());
    circles.apply(scaleTrans);

    Coordinate centreScaled = circles.getEnvelopeInternal().centre();

    AffineTransformation rotateTrans = AffineTransformation.rotationInstance(Math.PI / 4, centreScaled.getX(),
        centreScaled.getY());
    circles.apply(rotateTrans);

    return circles;
  }

  private static Geometry createCircleRow(Coordinate origin, double size, int nGeom, int nPts) {
    Polygon[] circles = new Polygon[nGeom];

    int nPtsGeom = nPts / nGeom;

    double baseX = origin.getX();
    double y = origin.getY();
    for (int i = 0; i < nGeom; i++) {

      Coordinate originGeom = new Coordinate(baseX + i * 2 * size, y);
      circles[i] = createCircle(originGeom, size, nPtsGeom);
    }
    return circles[0].getFactory().createMultiPolygon(circles);
  }

  public static Geometry createExtentWithHoles(Geometry polygons) {
    Envelope env = polygons.getEnvelopeInternal().copy();
    env.expandBy(env.getDiameter());
    GeometryFactory factory = polygons.getFactory();
    LinearRing shell = ((Polygon) factory.toGeometry(env)).getExteriorRing();
    LinearRing[] holes = extractShells(polygons);
    return factory.createPolygon(shell, holes);
  }

  private static LinearRing[] extractShells(Geometry polygons) {
    int n = polygons.getNumGeometries();
    LinearRing[] shells = new LinearRing[n];
    for (int i = 0; i < n; i++) {
      shells[i] = ((Polygon) polygons.getGeometryN(i)).getExteriorRing();
    }
    return shells;
  }
}
