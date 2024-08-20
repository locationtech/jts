/*
 * Copyright (c) 2023 Martin Davis.
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

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Location;

/**
 * Traces the evaluation of a {@link TopologyPredicate}.
 * 
 * @author mdavis
 *
 */
public class TopologyPredicateTracer {

  /**
   * Creates a new predicate tracing the evaluation of a given predicate.
   * 
   * @param pred the predicate to trace
   * @return the traceable predicate
   */
  public static TopologyPredicate trace(TopologyPredicate pred) {
    return new PredicateTracer(pred);
  }
  
  private TopologyPredicateTracer() {
    
  }
  
  private static class PredicateTracer implements TopologyPredicate 
  {
    private TopologyPredicate pred;
  
    private PredicateTracer(TopologyPredicate pred) {
      this.pred = pred;
    }
    
    public String name() { return pred.name(); }
    
    @Override
    public boolean requireSelfNoding() {
      return pred.requireSelfNoding();
    }
    
    public boolean requireInteraction() {
      return pred.requireInteraction();
    }
    
    @Override
    public boolean requireCovers(boolean isSourceA) {
      return pred.requireCovers(isSourceA);
    }
    
    @Override
    public boolean requireExteriorCheck(boolean isSourceA) {
      return pred.requireExteriorCheck(isSourceA);
    }
    
    @Override
    public void init(int dimA, int dimB) {
      pred.init(dimA, dimB);  
      checkValue("dimensions");
    }
    
    @Override
    public void init(Envelope envA, Envelope envB) {
      pred.init(envA, envB);  
      checkValue("envelopes");
    }
    
    @Override
    public void updateDimension(int locA, int locB, int dimension) {
      String desc = "A:" + Location.toLocationSymbol(locA)
        + "/B:" + Location.toLocationSymbol(locB)
        + " -> " + dimension;
      String ind = "";
      boolean isChanged = isDimChanged(locA, locB, dimension);
      if (isChanged) {
        ind = " <<< ";
      }
      System.out.println(desc + ind);
      pred.updateDimension(locA, locB, dimension);
      if (isChanged) {
        checkValue("IM entry");
      }
    }
  
    private boolean isDimChanged(int locA, int locB, int dimension) {
      if (pred instanceof IMPredicate) {
        return ((IMPredicate) pred).isDimChanged(locA, locB, dimension);
      }
      return false;
    }
  
    private void checkValue(String source) {
      if (pred.isKnown()) {
        System.out.println(name() + " = " + pred.value() 
        + " based on " + source);
      }
    }
  
    @Override
    public void finish() {
      pred.finish();
    }
  
    @Override
    public boolean isKnown() {
      return pred.isKnown();
    }
    
    @Override
    public boolean value() {
      return pred.value();
    }
  
    public String toString() {
      return pred.toString();
    }
  }
}
