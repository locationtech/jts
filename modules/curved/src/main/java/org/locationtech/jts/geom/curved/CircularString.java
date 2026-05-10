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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * A connected sequence of circular arcs, where each consecutive triple of
 * control points (start, mid, end) defines one arc and the end point of one
 * arc is the start point of the next.
 * <p>
 * This is a phase-1 stand-in: the control points are stored as a single
 * {@link CoordinateSequence} (inherited via {@link LineString}) and spatial
 * operations fall through to the parent's polyline behaviour. Native
 * arc-aware algorithms are out of scope for this module today.
 */
public class CircularString extends LineString {
  private static final long serialVersionUID = 1L;

  public CircularString(CoordinateSequence points, GeometryFactory factory) {
    super(points, factory);
  }

  @Override
  public String getGeometryType() {
    return "CircularString";
  }
}
