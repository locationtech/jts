/*
 * Copyright (c) 2026 Jody Garnett and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.twkb;

import java.io.DataInput;
import java.io.IOException;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

/**
 * Limits applied while reading a single TWKB geometry: the total of declared counts
 * against the bytes available, and the nesting depth of geometry collections.
 */
final class Budget {

    static final int MAX_BYTES = parseMaxBytes(
        System.getProperty(TWKBReader.MAX_BYTES_PROPERTY_NAME));

    static final int MAX_DEPTH = Integer.getInteger(WKBReader.MAX_DEPTH_PROPERTY_NAME, 1000);

    private long remaining;

    private int depth;

    Budget(int maxBytes) {
        this.remaining = maxBytes;
    }

    /**
     * Each counted item is encoded in at least minBytes distinct bytes, so the total of
     * all counts cannot exceed the input size.
     */
    int readCount(DataInput in, int minBytes, String fieldName)
        throws IOException, ParseException {
        final int count = Varint.readUnsignedVarInt(in);
        remaining -= Integer.toUnsignedLong(count) * minBytes;
        if (remaining < 0) {
            throw new ParseException(fieldName + " value is too large");
        }
        return count;
    }

    void enterCollection() throws ParseException {
        if (++depth > MAX_DEPTH) {
            throw new ParseException("Geometry nesting depth exceeds maximum of " + MAX_DEPTH);
        }
    }

    void exitCollection() {
        depth--;
    }

    static int parseMaxBytes(String value) {
        if (value != null) {
            try {
                int maxBytes = Integer.parseInt(value.trim());
                if (maxBytes > 0) {
                    return maxBytes;
                }
            } catch (NumberFormatException ignore) {
                // fall back to unlimited
            }
        }
        return Integer.MAX_VALUE;
    }
}
