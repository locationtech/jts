/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.ui.render;

import java.awt.Color;

import org.locationtech.jtstest.testbuilder.AppColors;

/**
 * Settings to configure the appearance of the Geometry View.
 * 
 * @author Martin Davis
 *
 */
public class ViewStyle {

  // the default values here are the ones shown in UI on app startup
  
  private boolean isGridEnabled = true;
  private boolean isLegendEnabled = false;
  private boolean isLegendBorderEnabled = true;
  private boolean isTitleEnabled = false;
  private boolean isTitleBorderEnabled = true;
  private String title = "";
  private Color clrBackground = AppColors.GEOM_VIEW_BACKGROUND;

  public ViewStyle() {
    
  }
  
  public void setGridEnabled(boolean isEnabled) {
    this.isGridEnabled = isEnabled;
  }
  
  public boolean isGridEnabled() {
    return isGridEnabled;
  }
  
  public void setLegendEnabled(boolean isEnabled) {
    this.isLegendEnabled = isEnabled;
  }
  
  public boolean isLegendEnabled() {
    return isLegendEnabled;
  }

  public void setTitleEnabled(boolean isEnabled) {
    this.isTitleEnabled = isEnabled;
  }
  
  public boolean isTitleEnabled() {
    return isTitleEnabled;
  }

  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getTitle() {
    return title;
  }

  public void setBackground(Color clrBackground) {
    this.clrBackground = clrBackground;
  }

  public Color getBackground() {
    return clrBackground;
  }

  public void setLegendBorderEnabled(boolean selected) {
    isLegendBorderEnabled = selected;
  }

  public boolean isLegendBorderEnabled() {
    return isLegendBorderEnabled;
  }

  public void setTitleBorderEnabled(boolean selected) {
    isTitleBorderEnabled = selected;
  }

  public boolean isTitleBorderEnabled() {
    return isTitleBorderEnabled;
  }

  
}
