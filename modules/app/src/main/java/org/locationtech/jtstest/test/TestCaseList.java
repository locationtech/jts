

/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
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


