/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class CGAlgorithmsTest
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(CGAlgorithmsTest.class);
  }

  public CGAlgorithmsTest(String name) { super(name); }



  public void testOrientationIndexRobust() throws Exception 
  { 
    Coordinate p0 = new Coordinate(219.3649559090992, 140.84159161824724); 
    Coordinate p1 = new Coordinate(168.9018919682399, -5.713787599646864); 
    Coordinate p = new Coordinate(186.80814046338352, 46.28973405831556); 
    int orient = CGAlgorithms.orientationIndex(p0, p1, p); 
    int orientInv = CGAlgorithms.orientationIndex(p1, p0, p); 
    assert(orient != orientInv); 
  } 
}
