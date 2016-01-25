/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jts.geom.util;

import com.vividsolutions.jts.geom.*;

/**
 * A visitor to {@link Geometry} componets, which 
 * allows short-circuiting when a defined condition holds.
 *
 * @version 1.7
 */
public abstract class ShortCircuitedGeometryVisitor
{
  private boolean isDone = false;

  public ShortCircuitedGeometryVisitor() {
  }

  public void applyTo(Geometry geom) {
    for (int i = 0; i < geom.getNumGeometries() && ! isDone; i++) {
      Geometry element = geom.getGeometryN(i);
      if (! (element instanceof GeometryCollection)) {
        visit(element);
        if (isDone()) {
          isDone = true;
          return;
        }
      }
      else
        applyTo(element);
    }
  }

  protected abstract void visit(Geometry element);

  protected abstract boolean isDone();
}
