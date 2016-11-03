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
