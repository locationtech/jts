

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
package org.locationtech.jts.index.sweepline;

/**
 * @version 1.7
 */
public class SweepLineInterval {

  private double min, max;
  private Object item;

  public SweepLineInterval(double min, double max)
  {
    this(min, max, null);
  }

  public SweepLineInterval(double min, double max, Object item)
  {
    this.min = min < max ? min : max;
    this.max = max > min ? max : min;
    this.item = item;
  }

  public double getMin() { return min;  }
  public double getMax() { return max;  }
  public Object getItem() { return item; }

}
