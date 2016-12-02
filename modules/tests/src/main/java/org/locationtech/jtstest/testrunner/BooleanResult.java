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
