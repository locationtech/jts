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
package org.locationtech.jtstest.testbuilder.model;

import org.locationtech.jtstest.testbuilder.AppConstants;

public class DisplayParameters {
  
  protected static boolean showingGrid = true;
  protected static boolean showingStructure = false;
  protected static boolean showingOrientation = false;
  protected static boolean showingVertices = true;
  protected static boolean showingLabel = true;
  protected static boolean showingCoordinates = true;
  protected static boolean isMagnifyingTopology = false;
  protected static double topologyStretchSize = AppConstants.TOPO_STRETCH_VIEW_DIST;

  public static boolean isShowingStructure() {
    return showingStructure;
  }

  public static void setShowingStructure(boolean show) {
    showingStructure = show;
  }

  public static boolean isShowingOrientation() {
    return showingOrientation;
  }

  public static void setShowingOrientation(boolean show) {
    showingOrientation = show;
  }

  public static boolean isShowingGrid() {
    return showingGrid;
  }

  public static void setShowingGrid(boolean show) {
    showingGrid = show;
  }

  public static boolean isShowingVertices() {
    return showingVertices;
  }

  public static void setShowingVertices(boolean show) {
    showingVertices = show;
  }

  public static void setShowingLabel(boolean show) {
    showingLabel = show;
  }

  public static boolean isShowingLabel() {
    return showingLabel;
  }

  public static boolean isMagnifyingTopology() {
    return isMagnifyingTopology;
  }

  public static void setMagnifyingTopology(boolean show) {
    isMagnifyingTopology = show;
  }

  public static void setTopologyStretchSize(double pixels) {
    topologyStretchSize = pixels;
  }

  public static double getTopologyStretchSize() {
    return topologyStretchSize;
  }

  public static int MAX_DISPLAY_POINTS = 2000;

  public static final int FILL_BASIC = 1;
  public static final int FILL_VARY = 2;
  public static final int FILL_RAINBOW = 3;
  public static final int FILL_RAINBOW_RANDOM = 4;

  private static int fillType = FILL_BASIC;

  public static int fillType() {
    return fillType;
  }

  public static void setFillType(int type) {
    fillType = type;  
  }

}
