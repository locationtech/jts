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

import org.locationtech.jts.geom.IntersectionMatrix;

/**
 * Evaluates the full relate {@link IntersectionMatrix}.
 * @author mdavis
 *
 */
class RelateMatrixPredicate extends IMPredicate
{
  public RelateMatrixPredicate() {
  }
  
  public String name() { return "relateMatrix"; }
  
  @Override
  public boolean requireInteraction() {
    //-- ensure entire matrix is computed
    return false;
  }
  
  @Override
  public boolean isDetermined() {
    //-- ensure entire matrix is computed
    return false;
  }
  
  @Override
  public boolean valueIM() {
    //-- indicates full matrix is being evaluated
    return false;

  }
  
  /**
   * Gets the current state of the IM matrix (which may only be partially complete).
   * 
   * @return the IM matrix
   */
  public IntersectionMatrix getIM() {
    return intMatrix;
  }

}