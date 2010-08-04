/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.testbuilder.model;

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
