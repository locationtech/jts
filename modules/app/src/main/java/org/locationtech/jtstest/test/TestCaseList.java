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
package org.locationtech.jtstest.test;

import java.util.*;

import org.locationtech.jtstest.testbuilder.model.TestCaseEdit;


/**
 * @version 1.7
 */
public class TestCaseList {
  ArrayList tests = new ArrayList();

  public TestCaseList() { }

  public List getList() {
    return tests;
  }
  public int size() {
    return tests.size();
  }
  public Testable get(int i) {
    return (Testable) tests.get(i);
  }
  public void add(Testable tc) {
    tests.add(tc);
  }
  public void add(TestCaseEdit tc, int i) {
    arrayAdd(tests, tc, i);
  }
  public void add(TestCaseList tcl) {
    for (Iterator i = tcl.tests.iterator(); i.hasNext(); ) {
      tests.add((Testable) i.next());
    }
  }
  public void remove(int i) {
    tests.remove(i);
  }

  /**
   * A shim for ArrayList.add(Object, int)
   * 
   * @param list ArrayList to add to
   * @param o object to add
   * @param index index to add at
   */
  private static void arrayAdd(ArrayList list, Object o, int index) {
    list.add(o);
    // adding at or after end of array?
    if (index >= list.size()) {
      return;
    }
    
    int n = list.size();
    // move elements up to make room for new element
    for (int i = n-1; i > index; i--) {
      list.set(i, list.get(i - 1));
    }
    list.set(index, o);
  }
}


