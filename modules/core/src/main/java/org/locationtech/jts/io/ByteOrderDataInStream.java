/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io;

import java.io.IOException;

/**
 * Allows reading a stream of Java primitive datatypes from an underlying
 * {@link InStream},
 * with the representation being in either common byte ordering.
 */
public class ByteOrderDataInStream
{
 
  private int byteOrder = ByteOrderValues.BIG_ENDIAN;
  private InStream stream;
  // buffers to hold primitive datatypes
  private byte[] buf1 = new byte[1];
  private byte[] buf4 = new byte[4];
  private byte[] buf8 = new byte[8];
  private byte[] bufLast = null;
  
  private long count = 0;

  public ByteOrderDataInStream()
  {
    this.stream = null;
  }

  public ByteOrderDataInStream(InStream stream)
  {
    this.stream = stream;
  }

  /**
   * Allows a single ByteOrderDataInStream to be reused
   * on multiple InStreams.
   *
   * @param stream
   */
  public void setInStream(InStream stream)
  {
    this.stream = stream;
  }
  
  /**
   * Sets the ordering on the stream using the codes in {@link ByteOrderValues}.
   * 
   * @param byteOrder the byte order code
   */
  public void setOrder(int byteOrder)
  {
    this.byteOrder = byteOrder;
  }
  
  /**
   * Gets the number of bytes read from the stream.
   * 
   * @return the number of bytes read
   */
  public long getCount() {
    return count;
  }
  
  /**
   * Gets the data item that was last read from the stream.
   * 
   * @return the data last read
   */
  public byte[] getData() {
    return bufLast;
  }
  
  /**
   * Reads a byte value.
   *
   * @return the value read
   * @throws IOException if an I/O error occurred
   * @throws ParseException if not enough data could be read
   */
  public byte readByte()
  	throws IOException, ParseException
  {
    read(buf1);
    return buf1[0];
  }

  /**
   * Reads an int value.
   * 
   * @return the value read
   * @throws IOException if an I/O error occurred
   * @throws ParseException if not enough data could be read
   */
  public int readInt()
	throws IOException, ParseException
  {
    read(buf4);
    return ByteOrderValues.getInt(buf4, byteOrder);
  }
  
  /**
   * Reads a long value.
   * 
   * @return the value read
   * @throws IOException if an I/O error occurred
   * @throws ParseException if not enough data could be read
   */
  public long readLong()
	throws IOException, ParseException
  {
    read(buf8);
    return ByteOrderValues.getLong(buf8, byteOrder);
  }

  /**
   * Reads a double value.
   * 
   * @return the value read
   * @throws IOException if an I/O error occurred
   * @throws ParseException if not enough data could be read
   */
  public double readDouble()
	throws IOException, ParseException
  {
    read(buf8);
    return ByteOrderValues.getDouble(buf8, byteOrder);
  }

  private void read(byte[] buf) throws IOException, ParseException {
    int num = stream.read(buf);
    if (num < buf.length) 
      throw new ParseException("Attempt to read past end of input");
    bufLast = buf;
    count += num;
  }
  
}
