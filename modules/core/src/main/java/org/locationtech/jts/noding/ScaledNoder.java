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

package org.locationtech.jts.noding;

import java.util.Collection;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.util.CollectionUtil;

/**
 * Wraps a {@link Noder} and transforms its input
 * into the integer domain.
 * This is intended for use with Snap-Rounding noders,
 * which typically are only intended to work in the integer domain.
 * Offsets can be provided to increase the number of digits of available precision.
 * <p>
 * Clients should be aware that rescaling can involve loss of precision,
 * which can cause zero-length line segments to be created.
 * These in turn can cause problems when used to build a planar graph.
 * This situation should be checked for and collapsed segments removed if necessary.
 *
 * @version 1.7
 */
public class ScaledNoder
    implements Noder
{
  private Noder noder;
  private double scaleFactor;
  private double offsetX;
  private double offsetY;
  private boolean isScaled = false;

  public ScaledNoder(Noder noder, double scaleFactor) {
    this(noder, scaleFactor, 0, 0);
  }

  public ScaledNoder(Noder noder, double scaleFactor, double offsetX, double offsetY) {
    this.noder = noder;
    this.scaleFactor = scaleFactor;
    // no need to scale if input precision is already integral
    isScaled = ! isIntegerPrecision();
  }

  public boolean isIntegerPrecision() { return scaleFactor == 1.0; }

  public Collection getNodedSubstrings()
  {
    Collection splitSS = noder.getNodedSubstrings();
    if (isScaled) rescale(splitSS);
    return splitSS;
  }

  public void computeNodes(Collection inputSegStrings)
  {
    Collection intSegStrings = inputSegStrings;
    if (isScaled)
      intSegStrings = scale(inputSegStrings);
    noder.computeNodes(intSegStrings);
  }

  private Collection scale(Collection segStrings)
  {
//  	System.out.println("Scaled: scaleFactor = " + scaleFactor);
    return CollectionUtil.transform(segStrings,
                                    new CollectionUtil.Function() {
      public Object execute(Object obj) {
        SegmentString ss = (SegmentString) obj;
        return new NodedSegmentString(scale(ss.getCoordinates()), ss.getData());
      }
                                    }
      );
  }

  private Coordinate[] scale(Coordinate[] pts)
  {
    Coordinate[] roundPts = new Coordinate[pts.length];
    for (int i = 0; i < pts.length; i++) {
      roundPts[i] = new Coordinate(
          Math.round((pts[i].x - offsetX) * scaleFactor),
          Math.round((pts[i].y - offsetY) * scaleFactor),
          pts[i].z
        );
    }
    Coordinate[] roundPtsNoDup = CoordinateArrays.removeRepeatedPoints(roundPts);
    return roundPtsNoDup;
  }

  //private double scale(double val) { return (double) Math.round(val * scaleFactor); }

  private void rescale(Collection segStrings)
  {
//  	System.out.println("Rescaled: scaleFactor = " + scaleFactor);
  	CollectionUtil.apply(segStrings,
  			new CollectionUtil.Function() {
  		public Object execute(Object obj) {
  			SegmentString ss = (SegmentString) obj;
  			rescale(ss.getCoordinates());
  			return null;
  		}
  	}
  	);
  }

  private void rescale(Coordinate[] pts)
  {
    Coordinate p0 = null;
    Coordinate p1 = null;
    
    if (pts.length == 2) {
      p0 = new Coordinate(pts[0]);
      p1 = new Coordinate(pts[1]);
    }

    for (int i = 0; i < pts.length; i++) {
      pts[i].x = pts[i].x / scaleFactor + offsetX;
      pts[i].y = pts[i].y / scaleFactor + offsetY;
    }
    
    if (pts.length == 2 && pts[0].equals2D(pts[1])) {
      System.out.println(pts);
    }
  }

  //private double rescale(double val) { return val / scaleFactor; }
}
