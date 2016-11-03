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
import java.io.InputStream;

/**
 * An adapter to allow an {@link InputStream} to be used as an {@link InStream}
 */
public class InputStreamInStream
	implements InStream
{
  private InputStream is;

  public InputStreamInStream(InputStream is)
  {
    this.is = is;
  }

  public void read(byte[] buf) throws IOException
  {
    is.read(buf);
  }
}
