/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.SegmentString;

class CoverageEdge extends BasicSegmentString {
  
  public static boolean isAllValid(List<CoverageEdge> segStrings) {
    for (CoverageEdge ss : segStrings) {
      if (! ss.isAllValid())
        return false;
    }
    return true;
  }
  
  private boolean[] isInvalid;
  private boolean[] isValid;

  public CoverageEdge(Coordinate[] pts, Object data) {
    super(pts, data);
    isInvalid = new boolean[size() - 1];
    isValid = new boolean[size() - 1];
  }
  
  public boolean isValid(int index) {
    return isValid[index];
  }

  public boolean isAllValid() {
    for (int i = 0; i < isValid.length; i++) {
      if (! isValid[i])
        return false;
    }
    return true;
  }

  public boolean isKnown(int i) {
    return isValid[i] || isInvalid[i];
  }
  
  public void markInvalid(int i) {
    if (isValid[i])
      throw new IllegalStateException("Setting valid edge to invalid");
    isInvalid[i] = true;
  }

  public void markValid(int i) {
    if (isInvalid[i])
      throw new IllegalStateException("Setting invalid edge to valid");
    isValid[i] = true;
  }

  public void createChains(List<SegmentString> chainList) {
    int endIndex = 0;
    while (true) {
      int startIndex = findChainStart(endIndex); 
      if (startIndex >= size() - 1)
        break;
      endIndex = findChainEnd(startIndex);
      SegmentString ss = createChain(this, startIndex, endIndex);
      chainList.add(ss);
    }
  }

  private static SegmentString createChain(SegmentString segString, int startIndex, int endIndex) {
    Coordinate[] pts = new Coordinate[endIndex - startIndex + 1];
    int ipts = 0;
    for (int i = startIndex; i < endIndex + 1; i++) {
      pts[ipts++] = segString.getCoordinate(i).copy();
    }
    return new BasicSegmentString(pts, segString.getData());
  }

  private int findChainStart(int index) {
    while (index < isInvalid.length && ! isInChain(index)) {
      index++;
    }
    return index;
  }

  private int findChainEnd(int index) {
    index++;
    while (index < isInvalid.length && isInChain(index)) {
      index++;
    }
    return index;
  }
  
  private boolean isInChain(int index) {
    return ! isValid[index] && isInvalid[index];
  }

}
