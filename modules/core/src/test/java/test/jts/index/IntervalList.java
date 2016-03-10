
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
package test.jts.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.index.bintree.Interval;



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
