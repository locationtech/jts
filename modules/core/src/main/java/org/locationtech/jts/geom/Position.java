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
package org.locationtech.jts.geom;

/**
 * Indicates the position of a location relative to a 
 * node or edge component of a planar topological structure.
 * 
 * @version 1.7
 */
public class Position {

  /** Specifies that a location is <i>on</i> a component */
  public static final int ON      = 0;
  
  /** Specifies that a location is to the <i>left</i> of a component */  
  public static final int LEFT    = 1;
  
  /** Specifies that a location is to the <i>right</i> of a component */  
  public static final int RIGHT   = 2;
  
  /**
   * Returns LEFT if the position is RIGHT, RIGHT if the position is LEFT, or the position
   * otherwise.
   */
  public static final int opposite(int position)
  {
    if (position == LEFT) return RIGHT;
    if (position == RIGHT) return LEFT;
    return position;
  }
}
