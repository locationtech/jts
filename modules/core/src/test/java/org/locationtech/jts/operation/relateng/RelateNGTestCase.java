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
package org.locationtech.jts.operation.relateng;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

public abstract class RelateNGTestCase extends GeometryTestCase {

  private boolean isTrace = false;
  
  public RelateNGTestCase(String name) {
    super(name);
  }
  
  protected void checkIntersectsDisjoint(String wkta, String wktb, boolean expectedValue) {
    checkPredicate(RelatePredicate.intersects(), wkta, wktb, expectedValue);
    checkPredicate(RelatePredicate.intersects(), wktb, wkta, expectedValue);
    checkPredicate(RelatePredicate.disjoint(), wkta, wktb, ! expectedValue);
    checkPredicate(RelatePredicate.disjoint(), wktb, wkta, ! expectedValue);
  }
  
  protected void checkContainsWithin(String wkta, String wktb, boolean expectedValue) {
    checkPredicate(RelatePredicate.contains(), wkta, wktb, expectedValue);
    checkPredicate(RelatePredicate.within(),   wktb, wkta, expectedValue);
  }
  
  protected void checkCoversCoveredBy(String wkta, String wktb, boolean expectedValue) {
    checkPredicate(RelatePredicate.covers(),    wkta, wktb, expectedValue);
    checkPredicate(RelatePredicate.coveredBy(), wktb, wkta, expectedValue);
  }
  
  protected void checkCrosses(String wkta, String wktb, boolean expectedValue) {
    checkPredicate(RelatePredicate.crosses(), wkta, wktb, expectedValue);
    checkPredicate(RelatePredicate.crosses(), wktb, wkta, expectedValue);
  }
  
  protected void checkOverlaps(String wkta, String wktb, boolean expectedValue) {
    checkPredicate(RelatePredicate.overlaps(), wkta, wktb, expectedValue);
    checkPredicate(RelatePredicate.overlaps(), wktb, wkta, expectedValue);
  }
  
  protected void checkTouches(String wkta, String wktb, boolean expectedValue) {
    checkPredicate(RelatePredicate.touches(), wkta, wktb, expectedValue);
    checkPredicate(RelatePredicate.touches(), wktb, wkta, expectedValue);
  }
  
  protected void checkEquals(String wkta, String wktb, boolean expectedValue) {
    checkPredicate(RelatePredicate.equalsTopo(), wkta, wktb, expectedValue);
    checkPredicate(RelatePredicate.equalsTopo(), wktb, wkta, expectedValue);
  }
  
  protected void checkRelate(String wkta, String wktb, String expectedValue) {
    Geometry a = read(wkta);
    Geometry b = read(wktb);
    RelateMatrixPredicate pred = new RelateMatrixPredicate();
    TopologyPredicate predTrace = trace(pred);
    RelateNG.relate(a, b, predTrace);
    String actualVal = pred.getIM().toString();
    assertEquals(expectedValue, actualVal);
 } 
  
  protected void checkRelateMatches(String wkta, String wktb, String pattern, boolean expectedValue) {
    TopologyPredicate pred = RelatePredicate.matches(pattern);
    checkPredicate(pred, wkta, wktb, expectedValue);
 } 
  
  protected void checkPredicate(TopologyPredicate pred, String wkta, String wktb, boolean expectedValue) {
    Geometry a = read(wkta);
    Geometry b = read(wktb);
    TopologyPredicate predTrace = trace(pred);
    boolean actualVal = RelateNG.relate(a, b, predTrace);
    assertEquals(expectedValue, actualVal);
  }
  
  void checkPrepared(String wkta, String wktb)
  {
    Geometry a = read(wkta);
    Geometry b = read(wktb);
    RelateNG prep_a = RelateNG.prepare(a);
   
    assertEquals("equalsTopo", prep_a.evaluate(b, RelatePredicate.equalsTopo()), 
                            RelateNG.relate(a, b, RelatePredicate.equalsTopo()));
  
    assertEquals("intersects", prep_a.evaluate(b, RelatePredicate.intersects()), 
        RelateNG.relate(a, b, RelatePredicate.intersects()));
    assertEquals("disjoint",   prep_a.evaluate(b, RelatePredicate.disjoint()), 
        RelateNG.relate(a, b, RelatePredicate.disjoint()));
    assertEquals("covers",     prep_a.evaluate(b, RelatePredicate.covers()), 
        RelateNG.relate(a, b, RelatePredicate.covers()));
    assertEquals("coveredBy",  prep_a.evaluate(b, RelatePredicate.coveredBy()), 
        RelateNG.relate(a, b, RelatePredicate.coveredBy()));
    assertEquals("within",     prep_a.evaluate(b, RelatePredicate.within()), 
        RelateNG.relate(a, b, RelatePredicate.within()));
    assertEquals("contains",   prep_a.evaluate(b, RelatePredicate.contains()), 
        RelateNG.relate(a, b, RelatePredicate.contains()));
    assertEquals("crosses",    prep_a.evaluate(b, RelatePredicate.crosses()), 
        RelateNG.relate(a, b, RelatePredicate.crosses()));
    assertEquals("touches",    prep_a.evaluate(b, RelatePredicate.touches()), 
        RelateNG.relate(a, b, RelatePredicate.touches()));
    
    assertEquals("relate",     prep_a.evaluate(b).toString(), 
                            RelateNG.relate(a, b).toString());
  }
  
  void checkPreparedMatches(String wkta, String wktb, String pattern) {
    Geometry a = read(wkta);
    Geometry b = read(wktb);
    RelateNG prep_a = RelateNG.prepare(a);

    assertEquals("matches " + pattern,  prep_a.evaluate(b, RelatePredicate.matches(pattern) ),
              RelateNG.relate(a, b, RelatePredicate.matches(pattern) )
        );
  }

  TopologyPredicate trace(TopologyPredicate pred) {
    if (! isTrace)
      return pred;
    
    System.out.println("----------- Pred: " + pred.name());

    return TopologyPredicateTracer.trace(pred);
  }
}
