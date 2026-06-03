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
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

/** A collection of {@link Polygon} and {@link CurvePolygon} members. */
public class MultiSurface extends MultiPolygon implements Linearizable {
  private static final long serialVersionUID = 1L;

  public MultiSurface(Polygon[] members, GeometryFactory factory) {
    super(members, factory);
  }

  @Override
  public String getGeometryType() {
    return "MultiSurface";
  }

  @Override
  public Geometry getBoundary() {
    if (isEmpty()) {
      return getFactory().createMultiLineString();
    }
    List<LineString> all = new ArrayList<>();
    boolean sawCurveMember = false;
    for (int i = 0; i < getNumGeometries(); i++) {
      Polygon p = (Polygon) getGeometryN(i);
      if (p instanceof CurvePolygon) sawCurveMember = true;
      Geometry b = p.getBoundary();
      if (b instanceof MultiLineString || b instanceof MultiCurve) {
        for (int j = 0; j < b.getNumGeometries(); j++) {
          LineString r = (LineString) b.getGeometryN(j);
          all.add(r);
          if (!(r instanceof LinearRing)) sawCurveMember = true;
        }
      } else if (b instanceof LineString) {
        LineString r = (LineString) b;
        all.add(r);
        if (!(r instanceof LinearRing)) sawCurveMember = true;
      }
    }
    LineString[] arr = all.toArray(new LineString[0]);
    // Soundness (refactor): for pure-linear MultiSurface (no CurvePolygon members and
    // all collected pieces are LinearRings) return plain MLS to match super contract.
    // Otherwise (or when any curved) return MultiCurve container (preserves identity
    // for curve-aware callers, is-a MLS so most code continues to work).
    if (!sawCurveMember) {
      boolean allRings = true;
      for (LineString r : arr) {
        if (!(r instanceof LinearRing)) { allRings = false; break; }
      }
      if (allRings) {
        return getFactory().createMultiLineString(arr);
      }
    }
    GeometryFactory f = getFactory();
    if (f instanceof CurvedGeometryFactory) {
      return ((CurvedGeometryFactory) f).createMultiCurve(arr);
    }
    return new MultiCurve(arr, f);
  }

  @Override
  protected MultiSurface copyInternal() {
    int n = getNumGeometries();
    Polygon[] members = new Polygon[n];
    for (int i = 0; i < n; i++) {
      members[i] = (Polygon) getGeometryN(i).copy();
    }
    return new MultiSurface(members, getFactory());
  }

  @Override
  public Geometry toLinear(double tolerance) {
    GeometryFactory f = getFactory();
    int n = getNumGeometries();
    Polygon[] linearMembers = new Polygon[n];
    for (int i = 0; i < n; i++) {
      Geometry m = getGeometryN(i);
      if (m instanceof Linearizable) {
        linearMembers[i] = (Polygon) ((Linearizable) m).toLinear(tolerance);
      } else {
        linearMembers[i] = (Polygon) m.copy();
      }
    }
    return f.createMultiPolygon(linearMembers);
  }
}
