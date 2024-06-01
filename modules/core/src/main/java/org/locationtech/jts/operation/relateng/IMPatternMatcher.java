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

import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.Location;

/**
 * A predicate that matches a DE-9IM pattern.
 * 
 * <h3>FUTURE WORK</h3>
 * Extend the expressiveness of the DE-9IM pattern language to allow:
 * <ul>
 * <li>Combining patterns via disjunction using "|".
 * <li>Limiting patterns via geometry dimension.  
 * A dimension limit specifies the allowable dimensions 
 * for both or individual geometries as [d] or [ab] or [ab;cd]
 * </ul>
 * 
 * @author Martin Davis
 *
 */
class IMPatternMatcher extends IMPredicate
{
  private String imPattern = null;
  private IntersectionMatrix patternMatrix;
  
  public IMPatternMatcher(String imPattern) {
    this.imPattern = imPattern;
    this.patternMatrix = new IntersectionMatrix(imPattern);
  }
  
  public String name() { return "IMPattern"; }
  
  //TODO: implement requiresExteriorCheck by inspecting matrix entries for E
  
  public void init(Envelope envA, Envelope envB) {
    super.init(dimA, dimB);
    //-- if pattern specifies any non-E/non-E interaction, envelopes must not be disjoint
    boolean requiresInteraction = requireInteraction(patternMatrix);
    boolean isDisjoint = envA.disjoint(envB);
    setValueIf(false, requiresInteraction && isDisjoint);
  }

  @Override
  public boolean requireInteraction() {
    return requireInteraction(patternMatrix);
  }
  
  private static boolean requireInteraction(IntersectionMatrix im) {
    boolean requiresInteraction = 
        isInteraction(im.get(Location.INTERIOR, Location.INTERIOR))
        || isInteraction(im.get(Location.INTERIOR, Location.BOUNDARY))
        || isInteraction(im.get(Location.BOUNDARY, Location.INTERIOR))
        || isInteraction(im.get(Location.BOUNDARY, Location.BOUNDARY));
    return requiresInteraction;
  }
  
  private static boolean isInteraction(int imDim) {
    return imDim == Dimension.TRUE || imDim >= Dimension.P;
  }

  @Override
  public boolean isDetermined() {
    /**
     * Matrix entries only increase in dimension as topology is computed.
     * The predicate can be short-circuited (as false) if
     * any computed entry is greater than the mask value. 
     */
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        int patternEntry = patternMatrix.get(i, j);
        
        if (patternEntry == Dimension.DONTCARE)
          continue;
        
        int matrixVal = getDimension(i, j);
        
        //-- mask entry TRUE requires a known matrix entry
        if (patternEntry == Dimension.TRUE) {
          if (matrixVal < 0)
            return false;
        }
        //-- result is known (false) if matrix entry has exceeded mask
        else if (matrixVal > patternEntry)
          return true;
      }
    }
    return false;
  }
  
  @Override
  public boolean valueIM() {
    boolean val = intMatrix.matches(imPattern);
    return val;
  }
  
  public String toString() {
    return name() + "(" + imPattern + ")";
  }
}