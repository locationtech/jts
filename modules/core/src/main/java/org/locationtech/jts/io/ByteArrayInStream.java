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
package org.locationtech.jts.io;

/**
 * Allows an array of bytes to be used as an {@link InStream}.
 * To optimize memory usage, instances can be reused
 * with different byte arrays.
 */
public class ByteArrayInStream
	implements InStream
{
	/*
	 * Implementation improvement suggested by Andrea Aime - Dec 15 2007
	 */
	
  private byte[] buffer;
	private int position;

	/**
	 * Creates a new stream based on the given buffer.
	 * 
	 * @param buffer the bytes to read
	 */
	public ByteArrayInStream(final byte[] buffer) {
		setBytes(buffer);
	}

	/**
	 * Sets this stream to read from the given buffer
	 * 
	 * @param buffer the bytes to read
	 */
	public void setBytes(final byte[] buffer) {
		this.buffer = buffer;
		this.position = 0;
	}

	/**
	 * Reads up to <tt>buf.length</tt> bytes from the stream
	 * into the given byte buffer.
	 * 
	 * @param buf the buffer to place the read bytes into
	 */
	public void read(final byte[] buf) {
		int numToRead = buf.length;
		// don't try and copy past the end of the input
		if ((position + numToRead) > buffer.length) {
			numToRead = buffer.length - position;
			System.arraycopy(buffer, position, buf, 0, numToRead);
			// zero out the unread bytes
			for (int i = numToRead; i < buf.length; i++) {
				buf[i] = 0;
			}
		}
		else {
			System.arraycopy(buffer, position, buf, 0, numToRead);			
		}
		position += numToRead;
	}
}
