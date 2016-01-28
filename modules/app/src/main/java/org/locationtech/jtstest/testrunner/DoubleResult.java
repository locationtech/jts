
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
package org.locationtech.jtstest.testrunner;



/**
 * @version 1.7
 */
public class DoubleResult implements Result {
  private double value;

  public DoubleResult(Double value) {
    this.value = value.doubleValue();
  }

  public boolean equals(Result other, double tolerance) {
    if (!(other instanceof DoubleResult)) {
      return false;
    }
    DoubleResult otherResult = (DoubleResult) other;
    double otherValue = otherResult.value;

    return Math.abs(value-otherValue) <= tolerance;
  }

  public String toLongString() {
    return Double.toString(value);
  }

  public String toFormattedString() {
    return Double.toString(value);
  }

  public String toShortString() {
    return Double.toString(value);
  }
}

