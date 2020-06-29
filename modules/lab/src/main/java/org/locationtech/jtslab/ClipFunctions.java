/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtslab;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jtslab.clip.RectangleClipPolygon;

public class ClipFunctions {
  
  public static Geometry clipPoly(Geometry geom, Geometry rectangle) {
    return RectangleClipPolygon.clip(geom, rectangle);
  }

  public static Geometry clipPolyPrecise(Geometry geom, Geometry rectangle, double scaleFactor) {
    return RectangleClipPolygon.clip(geom, rectangle, new PrecisionModel(scaleFactor));
  }
  
  public static Geometry clipByIntersection(Geometry geom, Geometry rectangle) {
    // short-circuit check
    Envelope rectEnv = rectangle.getEnvelopeInternal();
    if (rectEnv.contains(geom.getEnvelopeInternal())) return geom.copy();
    
    return rectangle.intersection(geom);
  }
  
  public static Geometry clipByIntersectionOpt(Geometry geom, Geometry rectangle) {
    // short-circuit check
    Envelope rectEnv = rectangle.getEnvelopeInternal();
    if (rectEnv.contains(geom.getEnvelopeInternal())) return geom.copy();
    if (! rectangle.intersects(geom)) return null;
    return rectangle.intersection(geom);
  }


}
