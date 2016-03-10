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
package org.locationtech.jtstest.testrunner;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.io.*;

import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.testbuilder.AppStrings;
import org.locationtech.jtstest.util.StringUtil;



/**
 * @version 1.7
 */
public class SimpleReportWriter implements ReportWriter
{

  private boolean verbose;
  private StringWriter reportBuf;

  public SimpleReportWriter(boolean verbose) { this.verbose = verbose; }

  public String writeReport(TestEngine engine) {
    reportBuf = new StringWriter();
    reportOnParsingProblems(engine.getParsingProblems());
    reportOnTestRuns(engine.getTestRuns());
    reportBuf.write("\n\n");
    reportSummary(engine);
    reportBuf.write("\n");
    reportOnTime(engine.getStart(), engine.getEnd());
    return reportBuf.toString();
  }

  public String write(Test test) {
    reportBuf = new StringWriter();
    reportOnTest(test);
    return reportBuf.toString();
  }

  public void reportOnTime(Date start, Date end) {
    long elapsedTime = end.getTime() - start.getTime();
    long days = elapsedTime / (24 * 60 * 60 * 1000);
    long hours = (elapsedTime - (days * 24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
    long minutes = (elapsedTime - (hours * 60 * 60 * 1000)) / (60 * 1000);
    double seconds = (elapsedTime - (minutes * 60 * 1000)) / (1000d);
    String message = "Elapsed time: ";
    message += days > 0 ? days + " days, " : "";
    message += hours > 0 ? hours + " hours, " : "";
    message += minutes > 0 ? minutes + " minutes, " : "";
    message += seconds > 0 ? seconds + " seconds" : "";
    reportBuf.write(message);
  }

  public void reportOnTest(Test test) 
  {
    String id = test.getGeometryIndex() + " " + test.getOperation();
    for (int i = 0; i < test.getArgumentCount(); i++) {
      id += " " + test.getArgument(i);
    }
    if (test.getExpectedResult() instanceof BooleanResult) {
      id += ", " + test.getExpectedResult().toShortString();
    }
    if (test.getDescription().length() > 0) {
      id += ", " + test.getDescription();
    }
    String report = "";
    if (test.getException() != null) {
      reportBuf.write("Test Threw Exception (" + id + ")" + "     "
          + (verbose ? StringUtil.getStackTrace(test.getException()) : test.getException().toString()) + "\n"
          );
    }
    else if (test.isPassed() && verbose) {
      reportBuf.write("Test Passed (" + id + ")" + "\n");
    }
    else if (! test.isPassed()) {
      reportBuf.write("Test Failed (" + id + ")" + "\n");
      if (verbose) {
        reportBuf.write("  Expected: " + test.getExpectedResult().toFormattedString() + "\n");
        try {
          reportBuf.write("  Actual: " + test.getActualResult().toFormattedString() + "\n");
        }
        catch (Exception e) {
          Assert.shouldNeverReachHere(e.toString());
        }
      }
    }
  }

  private void reportOnParsingProblems(List parsingProblems) {
    if (parsingProblems.isEmpty()) {
      return;
    }
    reportBuf.write("\n");
    for (Iterator i = parsingProblems.iterator(); i.hasNext(); ) {
      String parsingProblem = (String) i.next();
      reportBuf.write(parsingProblem);
      reportBuf.write("\n");
    }
  }

  private void reportOnTestRuns(List testRuns) {
    for (Iterator i = testRuns.iterator(); i.hasNext(); ) {
      TestRun testRun = (TestRun) i.next();
      reportOnTestRun(testRun);
    }
  }

  private void reportOnTestRun(TestRun testRun) {
    reportOnTestCases(testRun.getTestCases());
  }

  private void reportOnTestCases(List testCases) {
    for (Iterator i = testCases.iterator(); i.hasNext(); ) {
      TestCase testCase = (TestCase) i.next();
      if (testCase.isRun())
      	reportOnTestCase(testCase);
    }
  }

  private boolean areAllTestsPassed(TestCase testCase) {
    for (Iterator i = testCase.getTests().iterator(); i.hasNext(); ) {
      Test test = (Test) i.next();
      if (! test.isPassed()) {
        return false;
      }
    }
    return true;
  }

  private void reportOnTestCase(TestCase testCase) {
    if (areAllTestsPassed(testCase) && ! verbose) {
      return;
    }
    reportBuf.write("\n");
    reportBuf.write(AppStrings.LABEL_TEST_CASE + " " + testCase.getTestRun().getTestFile().getName()
                    + " - #" + testCase.getCaseIndex()
                    + " (" + testCase.getLineNumber() + ")"
        + (testCase.getDescription().length() > 0 ? ": " + testCase.getDescription() :
        "") + "\n");
    reportOnTests(testCase.getTests());
  }

  private void reportOnTests(List tests) {
    for (Iterator i = tests.iterator(); i.hasNext(); ) {
      Test test = (Test) i.next();
      reportOnTest(test);
    }
  }

  public void reportSummary(TestEngine engine) {
    if (engine.getParseExceptionCount() > 0) {
      reportBuf.write(engine.getParseExceptionCount() + " parsing exceptions\n");
    }
    reportBuf.write(engine.getTestCaseCount() + " cases with " + engine.getTestCount() + " tests"
        + "  --  ");
    reportBuf.write(engine.getPassedCount()
         + " passed, " + engine.getFailedCount() + " failed, " + engine.getExceptionCount()
         + " exceptions");
    if (engine.getParseExceptionCount() + engine.getFailedCount() + engine.getExceptionCount() > 0) {
      reportBuf.write("\n\n*******  ERRORS ENCOUNTERED IN RUN  ********\n");
    }
  }
}


