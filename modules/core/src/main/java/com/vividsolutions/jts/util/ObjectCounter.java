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
package com.vividsolutions.jts.util;

import java.util.*;

/**
 * Counts occurences of objects.
 * 
 * @author Martin Davis
 *
 */
public class ObjectCounter 
{

  private Map counts = new HashMap();
  
  public ObjectCounter() {
  }

  public void add(Object o)
  {
    Counter counter = (Counter) counts.get(o);
    if (counter == null)
      counts.put(o, new Counter(1));
    else
      counter.increment();
  }
  
  // TODO: add remove(Object o)
  
  public int count(Object o)
  {
    Counter counter = (Counter) counts.get(o);
    if (counter == null)
      return 0;
    else
      return counter.count();
   
  }
  private static class Counter
  {
    int count = 0;
    
    public Counter()
    {
      
    }
    
    public Counter(int count)
    {
      this.count = count;
    }
    
    public int count()
    {
      return count;
    }
    
    public void increment()
    {
      count++;
    }
  }
}
