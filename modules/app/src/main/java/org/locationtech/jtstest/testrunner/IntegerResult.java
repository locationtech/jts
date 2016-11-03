
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

