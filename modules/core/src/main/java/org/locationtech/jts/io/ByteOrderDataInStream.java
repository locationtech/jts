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
  public void setOrder(int byteOrder)
  {
    this.byteOrder = byteOrder;
  }

  /**
   * Reads a byte value
   *
   * @return the byte read
   */
  public byte readByte()
  	throws IOException
  {
    stream.read(buf1);
    return buf1[0];
  }

  public int readInt()
	throws IOException
  {
    stream.read(buf4);
    return ByteOrderValues.getInt(buf4, byteOrder);
  }
  public long readLong()
	throws IOException
  {
    stream.read(buf8);
    return ByteOrderValues.getLong(buf8, byteOrder);
  }

  public double readDouble()
	throws IOException
  {
    stream.read(buf8);
    return ByteOrderValues.getDouble(buf8, byteOrder);
  }

}
