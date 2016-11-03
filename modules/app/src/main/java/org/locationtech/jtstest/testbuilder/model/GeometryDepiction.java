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

import java.awt.Color;


/**
 * @version 1.7
 */
public class GeometryDepiction 
{
	
  public static final Color GEOM_A_HIGHLIGHT_CLR = new Color(0, 0, 255);
  public static final Color GEOM_A_LINE_CLR = new Color(0, 0, 255, 150);
  public static final Color GEOM_A_FILL_CLR = new Color(200, 200, 255, 150);

  public static final Color GEOM_B_HIGHLIGHT_CLR = new Color(255, 0, 0);
  public static final Color GEOM_B_LINE_CLR = new Color(150, 0, 0, 150);
  public static final Color GEOM_B_FILL_CLR = new Color(255, 200, 200, 150);
  
  // YellowGreen
  public static final Color GEOM_RESULT_LINE_CLR = new Color(120, 180, 0, 200);
  // Yellow
  public static final Color GEOM_RESULT_FILL_CLR = new Color(255, 255, 100, 100);
  
  public static final GeometryDepiction RESULT = new GeometryDepiction(
			new Color(154, 205, 0, 150),
			new Color(255, 255, 100, 100),
			// Yellow
			null);
  
  public static final GeometryDepiction GEOM_A = new GeometryDepiction(
      new Color(0, 0, 255, 150),
      new Color(200, 200, 255, 150),
      Color.cyan);

	public static final GeometryDepiction GEOM_B = new GeometryDepiction(
      new Color(255, 0, 0, 150),
      new Color(255, 200, 200, 150),
      Color.pink);


    private Color color;
    private Color fillColor;
    private Color bandColor;

    public Color getColor() {
        return color;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public Color getBandColor() {
        return bandColor;
    }

    public GeometryDepiction(Color color, Color fillColor, Color bandColor) {
        this.color = color;
        this.fillColor = fillColor;
        this.bandColor = bandColor;
    }
}
