/*
 * Copyright (c) 2019 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.model;

import java.util.Stack;

import org.locationtech.jts.geom.Geometry;

public class UndoBuffer {

  Stack<Geometry> buffer = new Stack<Geometry>();
  
  public void save(Geometry g) {
    if (g == null) return;
    buffer.push(g);
  }

  public boolean isEmpty() {
    return buffer.isEmpty();
  }
  
  public Geometry peek() {
    return buffer.peek();
  }
  
  public Geometry pop() {
    if (buffer.isEmpty()) return null;
    return buffer.pop();
  }
  
  /**
   * Pops the buffer if the top geometry is teh 
   * same as the given geometry.
   * 
   * @param geometry
   */
  public void pop(Geometry geometry) {
    if (isEmpty()) return;
    if (peek() == geometry) {
      pop();
    }
  }
  
  public void clear() {
    buffer.clear();
  }


}
