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
package org.locationtech.jts.index.intervalrtree;

import org.locationtech.jts.index.ArrayListVisitor;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class SortedPackedIntervalRTreeTest extends TestCase {
  
  public static void main(String args[]) {
    TestRunner.run(SortedPackedIntervalRTreeTest.class);
  }

  public SortedPackedIntervalRTreeTest(String name) { super(name); }
  
  /**
   * See JTS GH Issue #19.
   * Used to infinite-loop on empty geometries.
   * 
   */
  public void testEmpty() {
    SortedPackedIntervalRTree spitree = new SortedPackedIntervalRTree();
    ArrayListVisitor visitor = new ArrayListVisitor();
    spitree.query(0, 1, visitor);
  }
}
