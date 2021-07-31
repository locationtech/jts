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
package org.locationtech.jtstest.testrunner;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *  Converts test File's to TestCase's and runs them.
 *
 * @version 1.7
 */
public class TestEngine
     implements Runnable
{
  private List<File> testFiles;
  // default is to run all tests
  private int testCaseIndexToRun = -1;
  private boolean running = false;
  private List<TestRun> testRuns = new ArrayList<TestRun>();
  private TestReader testReader = new TestReader();

  private Date start = null;
  private Date end = null;

  /**
   *  Creates a TestEngine.
   */
  public TestEngine() { }

  /**
   *  Sets the File's that contain the tests.
   */
  public void setTestFiles(List<File> testFiles) {
    this.testFiles = testFiles;
  }
  
  public void setTestCaseIndexToRun(int testCaseIndexToRun)
  {
  	this.testCaseIndexToRun = testCaseIndexToRun;
  }
  
  public int getExceptionCount() {
    int exceptionCount = 0;
    for (Test test : getTests() ) {
      if (test.getException() != null) {
        exceptionCount++;
      }
    }
    return exceptionCount;
  }

  public int getFailedCount() {
    int failedCount = 0;
    for (Test test : getTests() ) {
      if ((test.getException() == null) && (!test.isPassed())) {
        failedCount++;
      }
    }
    return failedCount;
  }

  public int getPassedCount() {
    int passedCount = 0;
    for (Test test : getTests() ) {
      if (test.isPassed()) {
        passedCount++;
      }
    }
    return passedCount;
  }

  public int getParseExceptionCount() {
    return testReader.getParsingProblems().size();
  }

  /**
   *  Returns whether the TestEngine is running any TestCase's.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   *  Returns the total number of tests.
   */
  public int getTestCount() {
    int count = 0;
    for (TestRun testRun : testRuns) {
      count += testRun.getTestCount();
    }
    return count;
  }

  public int getTestCaseCount() {
    int count = 0;
    for (TestRun testRun : testRuns ) {
      count += testRun.getTestCases().size();
    }
    return count;
  }

  public List getParsingProblems() {
    return Collections.unmodifiableList(testReader.getParsingProblems());
  }

  public List<TestRun> getTestRuns() {
    return testRuns;
  }

  public Date getStart() {
    return start;
  }

  public Date getEnd() {
    return end;
  }

  public void clearParsingProblems() {
    testReader.clearParsingProblems();
  }

  public void run() {
    running = true;
    start = new Date();
    clearParsingProblems();
    testRuns = createTestRunsFromFiles();
    System.out.println("Running tests...");
    for (TestRun testRun : testRuns) {
      if (testCaseIndexToRun >= 0) {
      	testRun.setTestCaseIndexToRun(testCaseIndexToRun);
      }
    	testRun.run();
    }
    end = new Date();
    running = false;
  }
  
  private List<Test> getTests(TestRun testRun) {
    List<Test> tests = new ArrayList<Test>();
    for (TestCase testCase : testRun.getTestCases() ) {
      tests.addAll(testCase.getTests());
    }
    return tests;
  }

  private List<Test> getTests() {
    List<Test> tests = new ArrayList<Test>();
    for (TestRun testRun : testRuns ) {
      tests.addAll(getTests(testRun));
    }
    return tests;
  }

 
  
  /**
   *  Creates TestRun's, one for each test File.
   */
  private List<TestRun> createTestRunsFromFiles() {
    List<TestRun> testRuns = new ArrayList<TestRun>();
    int runIndex = 0;
    for (File testFile : testFiles ) {
      runIndex++;
      System.out.println("Reading test file " + testFile.getAbsolutePath());
      TestRun testRun = testReader.createTestRun(testFile, runIndex);
      if (testRun != null) {
        testRuns.add(testRun);
      }
    }
    return testRuns;
  }
}
