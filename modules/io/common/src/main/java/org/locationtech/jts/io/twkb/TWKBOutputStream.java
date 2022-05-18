/*
 * Copyright (c) 2019 Gabriel Roldan, 2022 Aurélien Mino
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

import java.io.DataOutput;
import java.io.IOException;

/**
 * Facade for a {@link DataOutput} providing the specific operations necessary for encoding in TWKB,
 * including variable-length encoding
 */
class TWKBOutputStream {

    protected final DataOutput out;

    public TWKBOutputStream(DataOutput out) {
        this.out = out;
    }

    public static TWKBOutputStream of(DataOutput out) {
        return new TWKBOutputStream(out);
    }

    public void writeByte(int ubyte) throws IOException {
        out.writeByte(ubyte);
    }

    public void write(byte[] buff, int offset, int length) throws IOException {
        out.write(buff, offset, length);
    }

    public void writeUnsignedVarInt(int value) throws IOException {
        Varint.writeUnsignedVarInt(value, out);
    }

    public void writeSignedVarLong(long value) throws IOException {
        Varint.writeSignedVarLong(value, out);
    }
}
