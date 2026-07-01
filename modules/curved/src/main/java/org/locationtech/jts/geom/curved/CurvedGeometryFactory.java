/*
 * Copyright (c) 2026 grootstebozewolf
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.curved;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * A {@link GeometryFactory} subclass with creation methods for the
 * extended OGC SFA / ISO 19125-2 geometry types implemented in
 * {@code jts-curved}: {@link CircularString}, {@link CompoundCurve},
 * {@link CurvePolygon}, {@link MultiCurve}, {@link MultiSurface},
 * {@link Triangle}, {@link PolyhedralSurface}, and {@link Tin}.
 * <p>
 * Behaves identically to {@link GeometryFactory} for all standard
 * (non-curved) types. Use this factory when constructing curved
 * geometries programmatically; pair it with {@link
 * org.locationtech.jts.io.curved.CurvedWKTReader} when reading WKT.
 */
public class CurvedGeometryFactory extends GeometryFactory {

  public CurvedGeometryFactory() {
    super();
  }

  public CurvedGeometryFactory(PrecisionModel pm) {
    super(pm);
  }

  public CurvedGeometryFactory(PrecisionModel pm, int srid) {
    super(pm, srid);
  }

  public CurvedGeometryFactory(PrecisionModel pm, int srid, CoordinateSequenceFactory csf) {
    super(pm, srid, csf);
  }

  public CurvedGeometryFactory(CoordinateSequenceFactory csf) {
    super(csf);
  }

  public CircularString createCircularString(CoordinateSequence points) {
    return new CircularString(points, this);
  }

  public CompoundCurve createCompoundCurve(CoordinateSequence points) {
    return new CompoundCurve(points, this);
  }

  public CurvePolygon createCurvePolygon() {
    return new CurvePolygon(this);
  }

  public CurvePolygon createCurvePolygon(LinearRing shell) {
    return new CurvePolygon(shell, null, this);
  }

  public CurvePolygon createCurvePolygon(LinearRing shell, LinearRing[] holes) {
    return new CurvePolygon(shell, holes, this);
  }

  /** Structural creation: shell/holes may be curved (CircularString, CompoundCurve, etc). */
  public CurvePolygon createCurvePolygon(LineString shell, LineString[] holes) {
    return new CurvePolygon(shell, holes, this);
  }

  /** Structural creation with single shell (no holes). */
  public CurvePolygon createCurvePolygon(LineString shell) {
    return new CurvePolygon(shell, new LineString[0], this);
  }

  public MultiCurve createMultiCurve(LineString[] members) {
    return new MultiCurve(members, this);
  }

  public MultiSurface createMultiSurface(Polygon[] members) {
    return new MultiSurface(members, this);
  }

  public Triangle createTriangle() {
    return new Triangle(this);
  }

  public Triangle createTriangle(LinearRing shell) {
    return new Triangle(shell, this);
  }

  public PolyhedralSurface createPolyhedralSurface(Polygon[] patches) {
    return new PolyhedralSurface(patches, this);
  }

  public Tin createTin(Polygon[] patches) {
    return new Tin(patches, this);
  }
}
