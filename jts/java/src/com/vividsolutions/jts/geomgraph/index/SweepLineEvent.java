


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
package com.vividsolutions.jts.geomgraph.index;

/**
 * @version 1.7
 */
public class SweepLineEvent
  implements Comparable
{ 
  private static final int INSERT = 1;
  private static final int DELETE = 2;

  private Object label;    // used for red-blue intersection detection
  private double xValue;
  private int eventType;
  private SweepLineEvent insertEvent = null; // null if this is an INSERT event
  private int deleteEventIndex;
  private Object obj;

  /**
   * Creates an INSERT event.
   * 
   * @param label the edge set label for this object
   * @param x the event location
   * @param obj the object being inserted
   */
  public SweepLineEvent(Object label, double x, Object obj)
  {
    this.eventType = INSERT;
    this.label = label;
    xValue = x;
    this.obj = obj;
  }

  /**
   * Creates a DELETE event.
   * 
   * @param x the event location
   * @param insertEvent the corresponding INSERT event
   */
  public SweepLineEvent(double x, SweepLineEvent insertEvent)
  {
    eventType = DELETE;
    xValue = x;
    this.insertEvent = insertEvent;
  }

  public boolean isInsert() { return eventType == INSERT; }
  public boolean isDelete() { return eventType == DELETE; }
  public SweepLineEvent getInsertEvent() { return insertEvent; }
  public int getDeleteEventIndex() { return deleteEventIndex; }
  public void setDeleteEventIndex(int deleteEventIndex) { this.deleteEventIndex = deleteEventIndex; }

  public Object getObject() { return obj; }

  public boolean isSameLabel(SweepLineEvent ev)
  {
    // no label set indicates single group
    if (label == null) return false;
    return label == ev.label;
  }
  /**
   * Events are ordered first by their x-value, and then by their eventType.
   * Insert events are sorted before Delete events, so that
   * items whose Insert and Delete events occur at the same x-value will be
   * correctly handled.
   */
  public int compareTo(Object o) {
    SweepLineEvent pe = (SweepLineEvent) o;
    if (xValue < pe.xValue) return  -1;
    if (xValue > pe.xValue) return   1;
    if (eventType < pe.eventType) return  -1;
    if (eventType > pe.eventType) return   1;
    return 0;
  }


}
