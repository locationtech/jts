/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.simplify;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;

class RingHullIndex {

  //TODO: use a proper spatial index
  List<RingHull> hulls = new ArrayList<RingHull>();
  
  public void add(RingHull ringHull) {
    hulls.add(ringHull);
  }
  
  public List<RingHull> query(Envelope queryEnv) {
    List<RingHull> result = new ArrayList<RingHull>();
    for (RingHull hull : hulls) {
      Envelope envHull = hull.getEnvelope();
      if (queryEnv.intersects(envHull)) {
        result.add(hull);
      }
    }
    return result;
  }
  
}
