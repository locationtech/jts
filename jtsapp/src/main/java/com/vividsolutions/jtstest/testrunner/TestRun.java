

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
package com.vividsolutions.jtstest.testrunner;

import java.io.File;
import java.util.*;

import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jtstest.geomop.*;


/**
 * @version 1.7
 */
public class TestRun implements Runnable 
{
	// default is to run all cases
	private int testCaseIndexToRun = -1;
  private String description;
  private List testCases = new ArrayList();
  private PrecisionModel precisionModel;
  private GeometryOperation geomOp = null;
  private ResultMatcher resultMatcher = null;
  private int runIndex;
  private File testFile;
  private File workspace;

  /**
   * 
   * @param description
   * @param runIndex
   * @param precisionModel
   * @param geomOp a GeometryOperation to use for all tests in this run (may be null)
   * @param testFile
   */
  public TestRun(String description, 
  		int runIndex, 
  		PrecisionModel precisionModel,
  		GeometryOperation geomOp,
  		ResultMatcher resultMatcher,
      File testFile) {
    this.description = description;
    this.runIndex = runIndex;
    this.precisionModel = precisionModel;
    this.geomOp = geomOp;
    this.resultMatcher = resultMatcher;
    this.testFile = testFile;
  }

  public void setWorkspace(File workspace) {
    this.workspace = workspace;
  }

  public void setTestCaseIndexToRun(int testCaseIndexToRun)
  {
  	this.testCaseIndexToRun = testCaseIndexToRun;
  }
  /**
   * @return null if no workspace set
   */
  public File getWorkspace() {
    return workspace;
  }

  public int getTestCount() {
    int count = 0;
    for (Iterator i = testCases.iterator(); i.hasNext(); ) {
      TestCase testCase = (TestCase) i.next();
      count += testCase.getTestCount();
    }
    return count;
  }

  public String getDescription() {
    return description;
  }

  public int getRunIndex() {
    return runIndex;
  }

  public PrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  public GeometryOperation getGeometryOperation()
  {
  	// use the main one if it was user-specified or this run does not have an op specified
  	if (TopologyTestApp.isGeometryOperationSpecified()
  			|| geomOp == null)
  		return TopologyTestApp.getGeometryOperation();
  	
  	return geomOp;
  }
  
  public ResultMatcher getResultMatcher()
  {
  	// use the main one if it was user-specified or this run does not have an op specified
  	if (TopologyTestApp.isResultMatcherSpecified()
  			|| resultMatcher == null)
  		return TopologyTestApp.getResultMatcher();
  	
  	return resultMatcher;
  }
  
  public List getTestCases() {
    return Collections.unmodifiableList(testCases);
  }

  public File getTestFile() {
    return testFile;
  }

  public void addTestCase(TestCase testCase) {
    testCases.add(testCase);
  }

  public void run() {
    for (Iterator j = testCases.iterator(); j.hasNext(); ) {
      TestCase testCase = (TestCase) j.next();
      if (testCaseIndexToRun < 0 || testCase.getCaseIndex() == testCaseIndexToRun)
      	testCase.run();
    }
  }

}

