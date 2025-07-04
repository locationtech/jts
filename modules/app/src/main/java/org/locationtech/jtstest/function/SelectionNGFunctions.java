/*
 * Copyright (c) 2024 Martin Davis.
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

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.relateng.IntersectionMatrixPattern;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.operation.relateng.RelatePredicate;
import org.locationtech.jtstest.geomfunction.Metadata;

public class SelectionNGFunctions 
{
  public static Geometry intersects(Geometry a, final Geometry mask)
  {
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return RelateNG.relate(mask, g, RelatePredicate.intersects());
      }
    });
  }
  
  public static Geometry intersectsPrep(Geometry a, final Geometry mask)
  {
    RelateNG relateNG = RelateNG.prepare(mask);
    Envelope maskEnv = mask.getEnvelopeInternal();
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        if (maskEnv.disjoint(g.getEnvelopeInternal()))
          return false;
       return relateNG.evaluate(g, RelatePredicate.intersects());
      }
    });
  }
  
  public static Geometry contains(Geometry a, final Geometry mask)
  {
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return RelateNG.relate(mask, g, RelatePredicate.contains());
      }
    });
  }
  
  public static Geometry containsPrep(Geometry a, final Geometry mask)
  {
    RelateNG relateNG = RelateNG.prepare(mask);
    Envelope maskEnv = mask.getEnvelopeInternal();
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        if (maskEnv.disjoint(g.getEnvelopeInternal()))
          return false;
       return relateNG.evaluate(g, RelatePredicate.contains());
      }
    });
  }
  
  public static Geometry covers(Geometry a, final Geometry mask)
  {
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return RelateNG.relate(mask, g, RelatePredicate.covers());
      }
    });
  }
  
  public static Geometry coversPrep(Geometry a, final Geometry mask)
  {
    RelateNG relateNG = RelateNG.prepare(mask);
    Envelope maskEnv = mask.getEnvelopeInternal();
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        if (maskEnv.disjoint(g.getEnvelopeInternal()))
          return false;
        return relateNG.evaluate(g, RelatePredicate.covers());
      }
    });
  }
  
  public static Geometry touches(Geometry a, final Geometry mask)
  {
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return RelateNG.relate(mask, g, RelatePredicate.touches());
      }
    });
  }
  
  public static Geometry touchesPrep(Geometry a, final Geometry mask)
  {
    RelateNG relateNG = RelateNG.prepare(mask);
    Envelope maskEnv = mask.getEnvelopeInternal();
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        if (maskEnv.disjoint(g.getEnvelopeInternal()))
          return false;
        return relateNG.evaluate(g, RelatePredicate.touches());
      }
    });
  }
  
  public static Geometry adjacent(Geometry a, final Geometry mask)
  {
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return RelateNG.relate(mask, g, RelatePredicate.matches(IntersectionMatrixPattern.ADJACENT));
      }
    });
  }
  
  public static Geometry adjacentPrep(Geometry a, final Geometry mask)
  {
    RelateNG relateNG = RelateNG.prepare(mask);
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return relateNG.evaluate(g, RelatePredicate.matches(IntersectionMatrixPattern.ADJACENT));
      }
    });
  }
  
  public static Geometry relatePattern(Geometry a, final Geometry mask, 
      @Metadata(title="DE-9IM Pattern")
      String pattern)
  {
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return RelateNG.relate(mask, g, RelatePredicate.matches(pattern));
      }
    });
  }
  
  public static Geometry relatePatternPrep(Geometry a, final Geometry mask, 
      @Metadata(title="DE-9IM Pattern")
      String pattern)
  {
    RelateNG relateNG = RelateNG.prepare(mask);
    return SelectionFunctions.select(a, new GeometryPredicate() {
      public boolean isTrue(Geometry g) {
        return relateNG.evaluate(g, RelatePredicate.matches(pattern));
      }
    });
  }
  
}
  

