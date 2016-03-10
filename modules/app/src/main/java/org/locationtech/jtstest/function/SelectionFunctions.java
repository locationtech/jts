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

package org.locationtech.jtstest.function;

import java.util.*;

import org.locationtech.jts.geom.*;



public class SelectionFunctions 
{
  public static Geometry intersects(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return mask.intersects(g);
      }
    });
  }
  
  public static Geometry covers(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.covers(mask);
      }
    });
  }
  
  public static Geometry coveredBy(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.coveredBy(mask);
      }
    });
  }
  
  public static Geometry disjoint(Geometry a, Geometry mask)
  {
    List selected = new ArrayList();
    for (int i = 0; i < a.getNumGeometries(); i++ ) {
      Geometry g = a.getGeometryN(i);
      if (mask.disjoint(g)) {
        selected.add(g);
      }
    }
    return a.getFactory().buildGeometry(selected);
  }
  public static Geometry valid(Geometry a)
  {
    List selected = new ArrayList();
    for (int i = 0; i < a.getNumGeometries(); i++ ) {
      Geometry g = a.getGeometryN(i);
      if (g.isValid()) {
        selected.add(g);
      }
    }
    return a.getFactory().buildGeometry(selected);
  }
  public static Geometry invalid(Geometry a)
  {
    List selected = new ArrayList();
    for (int i = 0; i < a.getNumGeometries(); i++ ) {
      Geometry g = a.getGeometryN(i);
      if (! g.isValid()) {
        selected.add(g);
      }
    }
    return a.getFactory().buildGeometry(selected);
  }
  public static Geometry areaGreater(Geometry a, final double minArea)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.getArea() > minArea;
      }
    });
  }
  public static Geometry areaZero(Geometry a)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.getArea() == 0.0;
      }
    });
  }
  public static Geometry within(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.within(mask);
      }
    });
  }
  
  private static Geometry select(Geometry geom, GeometryPredicate pred)
  {
    List selected = new ArrayList();
    for (int i = 0; i < geom.getNumGeometries(); i++ ) {
      Geometry g = geom.getGeometryN(i);
      if (pred.isTrue(g)) {
        selected.add(g);
      }
    }
    return geom.getFactory().buildGeometry(selected);

  }
  
  public static Geometry firstNComponents(Geometry g, int n)
  {
    List comp = new ArrayList();
    for (int i = 0; i < g.getNumGeometries() && i < n; i++) {
      comp.add(g.getGeometryN(i));
    }
    return g.getFactory().buildGeometry(comp);
  }
}

interface GeometryPredicate
{
  boolean isTrue(Geometry geom);
}
