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

package org.locationtech.jts.util;

import junit.framework.TestCase;

/**
 * @version 1.7
 */
public class PriorityQueueTest
    extends TestCase
{
  public PriorityQueueTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(PriorityQueueTest.class);
  }

  public void testOrder1() throws Exception {
    PriorityQueue q = new PriorityQueue();
    q.add(Integer.valueOf(1));
    q.add(Integer.valueOf(10));
    q.add(Integer.valueOf(5));
    q.add(Integer.valueOf(8));
    q.add(Integer.valueOf(-1));
    checkOrder(q);
  }
  
  public void testOrderRandom1() throws Exception {
    PriorityQueue q = new PriorityQueue();
    addRandomItems(q, 100);
    checkOrder(q);
  }
  
  private void addRandomItems(PriorityQueue q, int num)
  {
    for (int i = 0 ; i < num; i++) {
      q.add(Integer.valueOf((int) (num * Math.random())));
    }
  }
  
  private void checkOrder(PriorityQueue q)
  {
    Comparable curr = null;
    
    while (! q.isEmpty()) {
      Comparable next = (Comparable) q.poll();
      //System.out.println(next);
      if (curr == null)
        curr = next;
      else 
        assertTrue(next.compareTo(curr) >= 0);
    }
  }
}
