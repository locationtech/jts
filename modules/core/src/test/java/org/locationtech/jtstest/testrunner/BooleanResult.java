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
public class BooleanResult implements Result {
    private boolean result;

    public BooleanResult(boolean result) {
        this.result = result;
    }

    public BooleanResult(Boolean result) {
        this(result.booleanValue());
    }

    public boolean equals(Result other, double tolerance) {
        if (!(other instanceof BooleanResult))
            return false;
        BooleanResult otherBooleanResult = (BooleanResult) other;
        return result == otherBooleanResult.result;
    }

    public String toFormattedString() {
        return toShortString();
    }

    public String toLongString() {
        return toShortString();
    }

    public String toShortString() {
        return result ? "true" : "false";
    }
}
