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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Buffered {@link TWKBOutputStream}. Useful for e.g. encoding the size of an encoded geometry
 * <b>before</b> writing the bytes for the geometry itself.
 */
class BufferedTKWBOutputStream extends TWKBOutputStream {

    public static BufferedTKWBOutputStream create() {
        BufferedDataOutput buff = BufferedDataOutput.create();
        return new BufferedTKWBOutputStream(buff);
    }

    public int size() {
        return ((BufferedDataOutput) super.out).writtenSize();
    }

    private BufferedTKWBOutputStream(BufferedDataOutput buff) {
        super(buff);
    }

    public void writeTo(TWKBOutputStream out) throws IOException {
        BufferedDataOutput bufferedOut = ((BufferedDataOutput) super.out);
        int size = bufferedOut.writtenSize();
        byte[] buff = bufferedOut.buffer();
        out.write(buff, 0, size);
    }

    private static class BufferedDataOutput extends DataOutputStream {

        static BufferedDataOutput create() {
            return new BufferedDataOutput(new InternalByteArrayOutputStream());
        }

        public int writtenSize() {
            return ((InternalByteArrayOutputStream) super.out).size();
        }

        public byte[] buffer() {
            return ((InternalByteArrayOutputStream) super.out).buffer();
        }

        BufferedDataOutput(InternalByteArrayOutputStream out) {
            super(out);
        }

        private static class InternalByteArrayOutputStream extends ByteArrayOutputStream {
            public byte[] buffer() {
                return super.buf;
            }
        }
    }
}
