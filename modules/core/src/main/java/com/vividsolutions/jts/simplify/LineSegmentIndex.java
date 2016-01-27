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

package com.vividsolutions.jts.simplify;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.*;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * An spatial index on a set of {@link LineSegment}s.
 * Supports adding and removing items.
 *
 * @author Martin Davis
 */
class LineSegmentIndex
{
  private Quadtree index = new Quadtree();

  public LineSegmentIndex()
  {
  }

  public void add(TaggedLineString line) {
    TaggedLineSegment[] segs = line.getSegments();
    for (int i = 0; i < segs.length; i++) {
      TaggedLineSegment seg = segs[i];
      add(seg);
    }
  }

  public void add(LineSegment seg)
  {
    index.insert(new Envelope(seg.p0, seg.p1), seg);
  }

  public void remove(LineSegment seg)
  {
    index.remove(new Envelope(seg.p0, seg.p1), seg);
  }

  public List query(LineSegment querySeg)
  {
    Envelope env = new Envelope(querySeg.p0, querySeg.p1);

    LineSegmentVisitor visitor = new LineSegmentVisitor(querySeg);
    index.query(env, visitor);
    List itemsFound = visitor.getItems();

//    List listQueryItems = index.query(env);
//    System.out.println("visitor size = " + itemsFound.size()
//                       + "  query size = " + listQueryItems.size());
//    List itemsFound = index.query(env);

    return itemsFound;
  }
}

/**
 * ItemVisitor subclass to reduce volume of query results.
 */
class LineSegmentVisitor
    implements ItemVisitor
{
// MD - only seems to make about a 10% difference in overall time.

  private LineSegment querySeg;
  private ArrayList items = new ArrayList();

  public LineSegmentVisitor(LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public void visitItem(Object item)
  {
    LineSegment seg = (LineSegment) item;
    if (Envelope.intersects(seg.p0, seg.p1, querySeg.p0, querySeg.p1))
      items.add(item);
  }

  public ArrayList getItems() { return items; }
}
