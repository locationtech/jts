

/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package org.locationtech.jtstest.test;

import java.util.*;


/**
 * @version 1.7
 */
public class TestCaseList {
  ArrayList tests = new ArrayList();
  int countError = 0;
  int countTotal = 0;

  public TestCaseList() { }

  public List getList() {
    return tests;
  }

  public void add(Testable tc) {
    tests.add(tc);
  }

  public void add(TestCaseList tcl) {
    for (Iterator i = tcl.tests.iterator(); i.hasNext(); ) {
      tests.add((Testable) i.next());
    }
  }

  public void run() {
    for (int i = 0; i < tests.size(); i++) {
      boolean failed = true;
      String failedMsg = "";
      Testable tc = (Testable) tests.get(i);
      String resultStr = "Passed";
      countTotal++;
      try {
        tc.runTest();
        failed = tc.isFailed();
        failedMsg = tc.getFailedMsg();
      }
      catch (Exception e) {
        failedMsg = e.getMessage();
        System.out.println(e.getMessage());
        e.printStackTrace();
        failed = true;
      }
      if (failed) {
        countError++;
        resultStr = "FAILED! - " + failedMsg;
      }
      System.out.println(countTotal + ". " + tc.getName() + " - " + resultStr);
    }
    String errReport = "  All tests passed";
    if (countError > 0) {
      errReport = "  Failed: " + countError;
    }
    System.out.print("\nTotal tests: " + countTotal + errReport);
  }
}


