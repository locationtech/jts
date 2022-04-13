/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class VertexSequencePackedRtreeTest extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(VertexSequencePackedRtreeTest.class);
  }
  
  public VertexSequencePackedRtreeTest(String name) {
    super(name);
  }
  
  public void test1() {
    VertexSequencePackedRtree tree = createSPRtree(1,1);
    checkQuery(tree, 1,1,4,4,   result( 0 ));
  }

  public void test2() {
    VertexSequencePackedRtree tree = createSPRtree(0,0, 1,1);
    checkQuery(tree, 1,1,4,4,   result( 1 ));
  }

  public void test6() {
    VertexSequencePackedRtree tree = createSPRtree(0,0, 1,1,  2,2,  3,3,  4,4,  5,5);
    checkQuery(tree, 2,2,4,4,   result( 2,3,4 ));
    checkQuery(tree, 0,0,0,0,   result( 0 ));
  }
  
  public void test10() {
    VertexSequencePackedRtree tree = createSPRtree(0,0, 1,1,  2,2,  3,3,  4,4,  5,5, 6,6,  7,7,  8,8,  9,9,  10,10);
    checkQuery(tree, 2,2,4,4,   result( 2,3,4 ));
    checkQuery(tree, 7,7,8,8,   result( 7,8 ));
    checkQuery(tree, 0,0,0,0,   result( 0 ));
  }
  
  public void test6WithDups() {
    VertexSequencePackedRtree tree = createSPRtree(0,0, 1,1,  2,2,  3,3,  4,4,  5,5, 4,4,  3,3,  2,2, 1,1,  0,0);
    checkQuery(tree, 2,2,4,4,   result( 2,3,4, 6, 7, 8 ));
    checkQuery(tree, 0,0,0,0,   result( 0, 10 ));
  }
  
  private void checkQuery(VertexSequencePackedRtree tree, 
      double xmin, double ymin, double xmax, double ymax, int[] expected) {
    Envelope env = new Envelope(xmin, xmax, ymin, ymax);
    int[] result = tree.query(env);
    assertEquals(expected.length, result.length);
    assertTrue( isEqualResult(expected, result) );
  }

  private boolean isEqualResult(int[] expected, int[] result) {
    for (int i = 0; i < result.length; i++) {
      if (expected[i] != result[i])
        return false;
      
    }
    return true;
  }

  private int[] result(int... i) {
    return i;
  }

  private VertexSequencePackedRtree createSPRtree(int... ords) {
    int  numCoord = ords.length / 2;
    Coordinate[] pt = new Coordinate[numCoord];
    for (int i = 0 ; i < numCoord; i++) {
      pt[i] = new Coordinate(ords[2*i], ords[2*i+1]);
    }
    return new VertexSequencePackedRtree(pt);
  }
}
