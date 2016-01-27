
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
package test.jts.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.index.bintree.Interval;


/**
 * @version 1.7
 */
public class IntervalList
{
  List list = new ArrayList();

  public IntervalList() {
  }

  public void add(Interval interval)
  {
    list.add(interval);
  }

  public List query(Interval searchInterval)
  {
    List result = new ArrayList();
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      Interval interval = (Interval) i.next();
      if (interval.overlaps(searchInterval))
        result.add(interval);
    }
    return result;
  }


}
