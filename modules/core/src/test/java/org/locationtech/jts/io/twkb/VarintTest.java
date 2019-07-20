/*
 * Copyright (c) 2019 Gabriel Roldan.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.twkb;

import static org.junit.Assert.assertEquals;
import static org.locationtech.jts.io.twkb.Varint.readSignedVarInt;
import static org.locationtech.jts.io.twkb.Varint.readSignedVarLong;
import static org.locationtech.jts.io.twkb.Varint.readUnsignedVarInt;
import static org.locationtech.jts.io.twkb.Varint.readUnsignedVarLong;
import static org.locationtech.jts.io.twkb.Varint.writeSignedVarInt;
import static org.locationtech.jts.io.twkb.Varint.writeSignedVarLong;
import static org.locationtech.jts.io.twkb.Varint.writeUnsignedVarInt;
import static org.locationtech.jts.io.twkb.Varint.writeUnsignedVarLong;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

public class VarintTest {

    public @Test void testWriteSignedVarInt() throws IOException {
        checkSignedVarInt(Integer.MIN_VALUE);
        checkSignedVarInt(Integer.MAX_VALUE);
        checkSignedVarInt(0);
        checkSignedVarInt(1001);
        checkSignedVarInt(-1001);
    }

    private void checkSignedVarInt(final int value) throws IOException {
        ByteArrayDataOutput out = newDataOutput();
        writeSignedVarInt(value, out);
        int read = readSignedVarInt(newDataInput(out.toByteArray()));
        assertEquals(value, read);
    }

    public @Test void testWriteUnsignedVarInt() throws IOException {
        checkUnsignedVarInt(Integer.MAX_VALUE);
        checkUnsignedVarInt(0);
        checkUnsignedVarInt(1001);
    }

    private void checkUnsignedVarInt(final int value) throws IOException {
        ByteArrayDataOutput out = newDataOutput();
        writeUnsignedVarInt(value, out);
        int read = readUnsignedVarInt(newDataInput(out.toByteArray()));
        assertEquals(value, read);
    }

    public @Test void testWriteSignedVarLong() throws IOException {
        checkSignedVarLong(Long.MIN_VALUE);
        checkSignedVarLong(Long.MAX_VALUE);
        checkSignedVarLong(0);
        checkSignedVarLong(1001);
        checkSignedVarLong(-1001);
    }

    private void checkSignedVarLong(final long value) throws IOException {
        ByteArrayDataOutput out = newDataOutput();
        writeSignedVarLong(value, out);
        long read = readSignedVarLong(newDataInput(out.toByteArray()));
        assertEquals(value, read);
    }

    public @Test void testWriteUnsignedVarLong() throws IOException {
        checkUnsignedVarLong(Long.MAX_VALUE);
        checkUnsignedVarLong(0);
        checkUnsignedVarLong(1001);
    }

    private void checkUnsignedVarLong(final long value) throws IOException {
        ByteArrayDataOutput out = newDataOutput();
        writeUnsignedVarLong(value, out);
        long read = readUnsignedVarLong(newDataInput(out.toByteArray()));
        assertEquals(value, read);
    }

    private DataInput newDataInput(byte[] buffer) {
        return new DataInputStream(new ByteArrayInputStream(buffer));
    }

    private static class ByteArrayDataOutput extends DataOutputStream {
        ByteArrayDataOutput(ByteArrayOutputStream out) {
            super(out);
        }

        public byte[] toByteArray() {
            return ((ByteArrayOutputStream) super.out).toByteArray();
        }
    }

    private ByteArrayDataOutput newDataOutput() {
        return new ByteArrayDataOutput(new ByteArrayOutputStream());
    }
}
