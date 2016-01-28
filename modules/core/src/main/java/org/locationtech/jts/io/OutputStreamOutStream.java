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
import java.io.OutputStream;

/**
 * An adapter to allow an {@link OutputStream} to be used as an {@link OutStream}
 */
public class OutputStreamOutStream
	implements OutStream
{
  private OutputStream os;

  public OutputStreamOutStream(OutputStream os)
  {
    this.os = os;
  }
  public void write(byte[] buf, int len) throws IOException
  {
    os.write(buf, 0, len);
  }
}
