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
package org.locationtech.jts.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts occurrences of objects.
 * @deprecated
 * @author Martin Davis
 *
 */
public class ObjectCounter <T>
{

  private final Map<T, AtomicInteger> counts = new HashMap<>();
  
  public ObjectCounter() {
  }

  public void add(T o)
  {
    AtomicInteger counter = counts.get(o);
    if (counter == null)
      counts.put(o, new AtomicInteger(1));
    else
      counter.incrementAndGet();
  }
  
  // TODO: add remove(Object o)
  
  public int count(T o)
  {
    AtomicInteger counter = counts.get(o);
    if (counter == null)
      return 0;
    else
      return counter.get();
   
  }

}
