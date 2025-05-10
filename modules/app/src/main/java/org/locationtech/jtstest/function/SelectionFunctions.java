/*
 * Copyright (c) 2016 Vivid Solutions.
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

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;
import org.locationtech.jtstest.geomfunction.Metadata;

public class SelectionFunctions 
{
  
  public static Geometry intersectsPrep(Geometry a, final Geometry mask)
  {
    PreparedGeometry prep = PreparedGeometryFactory.prepare(mask);
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return prep.intersects(g);
      }
    });
  }
  
  public static Geometry intersects(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return mask.intersects(g);
      }
    });
  }
  
  public static Geometry contains(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return mask.contains(g);
      }
    });
  }
  
  public static Geometry containsPrep(Geometry a, final Geometry mask)
  {
    PreparedGeometry prep = PreparedGeometryFactory.prepare(mask);
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return prep.contains(g);
      }
    });
  }
  
  public static Geometry covers(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return mask.covers(g);
      }
    });
  }
  
  public static Geometry coversPrep(Geometry a, final Geometry mask)
  {
    PreparedGeometry prep = PreparedGeometryFactory.prepare(mask);
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return prep.covers(g);
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
  
  public static Geometry touches(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return mask.touches(g);
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
  
  public static Geometry relatePattern(Geometry a, final Geometry mask, String pattern)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return mask.relate(g, pattern);
      }
    });
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
  public static Geometry lengthGreaterThan(Geometry a, final double minLen)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.getLength() > minLen;
      }
    });
  }
  public static Geometry lengthLessThan(Geometry a, final double maxLen)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.getLength() < maxLen;
      }
    });
  }
  public static Geometry lengthZero(Geometry a)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.getLength() == 0.0;
      }
    });
  }
  public static Geometry areaGreaterThan(Geometry a, final double minArea)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.getArea() > minArea;
      }
    });
  }
  public static Geometry areaLessThan(Geometry a, final double maxArea)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.getArea() < maxArea;
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
  
  public static Geometry interiorPointWithin(Geometry a, final Geometry mask)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return g.getInteriorPoint().within(mask);
      }
    });
  }
  
  public static Geometry withinDistance(Geometry a, final Geometry mask, double maximumDistance)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return mask.isWithinDistance(g, maximumDistance);
      }
    });
  }

  public static Geometry withinDistanceIndexed(Geometry a, final Geometry mask, double maximumDistance)
  {
    IndexedFacetDistance indexedDist = new IndexedFacetDistance(mask);
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        boolean isWithinDist = indexedDist.isWithinDistance(g, maximumDistance);
        return isWithinDist;
      }
    });
  }

  public static Geometry maxInCircleRadiusWithin(Geometry a, 
      @Metadata(title="Max Radius Length")
      double maximumRadius)
  {
    return select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return MaximumInscribedCircle.isRadiusWithin(g, maximumRadius);
      }
    });
  }
  
  public static Geometry select(Geometry geom, GeometryPredicate pred)
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
