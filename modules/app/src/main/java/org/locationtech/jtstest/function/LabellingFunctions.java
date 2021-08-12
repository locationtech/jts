/*
 * Copyright (c) 2018 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jtstest.testbuilder.geom.ConstrainedInteriorPoint;

public class LabellingFunctions {

  public static Geometry labelPoint(Geometry g) {
    Coordinate pt = ConstrainedInteriorPoint.getCoordinate((Polygon) g);
    return g.getFactory().createPoint(pt);
  }
  
  public static Geometry labelPointConstrained(Geometry g, Geometry con) {
    Envelope envCon = con.getEnvelopeInternal();
    Coordinate pt = ConstrainedInteriorPoint.getCoordinate((Polygon) g, envCon);
    return g.getFactory().createPoint(pt);
  }
}
