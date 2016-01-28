
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



/**
 * @version 1.7
 */
public class IntegerResult implements Result {
  private int value;

  public IntegerResult(Integer value) {
    this.value = value.intValue();
  }

  public boolean equals(Result other, double tolerance) {
    if (!(other instanceof IntegerResult)) {
      return false;
    }
    IntegerResult otherResult = (IntegerResult) other;
    int otherValue = otherResult.value;

    return Math.abs(value-otherValue) <= tolerance;
  }

  public String toLongString() {
    return Integer.toString(value);
  }

  public String toFormattedString() {
    return Integer.toString(value);
  }

  public String toShortString() {
    return Integer.toString(value);
  }
}

