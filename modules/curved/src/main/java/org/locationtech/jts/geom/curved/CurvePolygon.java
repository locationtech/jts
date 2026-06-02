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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * A polygon whose rings may be straight, circular, or compound curves.
 *
 * <p>Option A (F-CP / FCP-DOVE per SPEC_F_CP.md): legacy {@code getExteriorRing()}
 * and {@code getInteriorRingN(i)} return {@link LinearRing} views obtained by
 * linearising at default tolerance (0.0); curve-aware code uses
 * {@link #getExteriorCurve()} / {@link #getInteriorCurveN(int)} to obtain the
 * structural {@link LineString} (which may be a {@link CircularString} or
 * {@link CompoundCurve}).
 */
public class CurvePolygon extends Polygon implements Linearizable {
  private static final long serialVersionUID = 1L;

  private final LineString structuralShell;
  private final LineString[] structuralHoles;

  public CurvePolygon(LinearRing shell, LinearRing[] holes, GeometryFactory factory) {
    // Note: ternary in super() arg to keep 'super' as the first *statement* for Java 8 source compat.
    // (Flexible constructor bodies / statements-before-super are a newer language feature.)
    super(shell, (holes == null ? new LinearRing[0] : holes), factory);
    this.structuralShell = shell;
    this.structuralHoles = copyAsLineStrings(holes);
  }

  public CurvePolygon(GeometryFactory factory) {
    super(null, null, factory);
    this.structuralShell = null;
    this.structuralHoles = new LineString[0];
  }

  /**
   * Structural constructor (Option A): accepts shell and holes as any LineString
   * (CircularString / CompoundCurve / LineString / LinearRing). Derives
   * LinearRing views at default tolerance for the Polygon supertype so that
   * legacy callers continue to work.
   */
  public CurvePolygon(LineString structuralShell, LineString[] holes, GeometryFactory factory) {
    super(
        deriveLinearRing(structuralShell, factory),
        deriveLinearRings(holes, factory),
        factory);
    this.structuralShell = structuralShell;
    this.structuralHoles = (holes == null) ? new LineString[0] : holes.clone();
  }

  private static LineString[] copyAsLineStrings(LinearRing[] in) {
    if (in == null || in.length == 0) return new LineString[0];
    LineString[] out = new LineString[in.length];
    System.arraycopy(in, 0, out, 0, in.length);
    return out;
  }

  private static LinearRing deriveLinearRing(LineString s, GeometryFactory f) {
    if (s == null) return null;
    if (s instanceof LinearRing) return (LinearRing) s;
    LineString flat = (s instanceof Linearizable)
        ? (LineString) ((Linearizable) s).toLinear(0.0)
        : s;
    return f.createLinearRing(flat.getCoordinates());
  }

  private static LinearRing[] deriveLinearRings(LineString[] hs, GeometryFactory f) {
    if (hs == null || hs.length == 0) return new LinearRing[0];
    LinearRing[] out = new LinearRing[hs.length];
    for (int i = 0; i < hs.length; i++) {
      out[i] = deriveLinearRing(hs[i], f);
    }
    return out;
  }

  /**
   * Returns the structural shell (may be CircularString, CompoundCurve,
   * LineString or LinearRing). Null for empty.
   */
  public LineString getExteriorCurve() {
    return structuralShell;
  }

  /**
   * Returns the structural interior ring (may be curved). Index 0..getNumInteriorRing()-1.
   */
  public LineString getInteriorCurveN(int n) {
    return structuralHoles[n];
  }

  @Override
  public String getGeometryType() {
    return "CurvePolygon";
  }

  @Override
  protected CurvePolygon copyInternal() {
    GeometryFactory f = getFactory();
    if (isEmpty() || structuralShell == null) return new CurvePolygon(f);
    LineString shellCopy = (LineString) structuralShell.copy();
    LineString[] holeCopies = new LineString[structuralHoles.length];
    for (int i = 0; i < structuralHoles.length; i++) {
      holeCopies[i] = (LineString) structuralHoles[i].copy();
    }
    return new CurvePolygon(shellCopy, holeCopies, f);
  }

  @Override
  public Geometry toLinear(double tolerance) {
    GeometryFactory f = getFactory();
    if (isEmpty() || structuralShell == null) return f.createPolygon();
    LineString shellFlat = (structuralShell instanceof Linearizable)
        ? (LineString) ((Linearizable) structuralShell).toLinear(tolerance)
        : structuralShell;
    LinearRing shell = f.createLinearRing(shellFlat.getCoordinates());
    LinearRing[] holeRings = new LinearRing[structuralHoles.length];
    for (int i = 0; i < structuralHoles.length; i++) {
      LineString h = structuralHoles[i];
      LineString hflat = (h instanceof Linearizable)
          ? (LineString) ((Linearizable) h).toLinear(tolerance)
          : h;
      holeRings[i] = f.createLinearRing(hflat.getCoordinates());
    }
    return f.createPolygon(shell, holeRings);
  }
}
