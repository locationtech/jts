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

package org.locationtech.jts.awt;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.util.AffineTransformation;

/**
 * Provides methods to read {@link Font} glyphs for strings 
 * into {@link Polygonal} geometry.
 * <p>
 * It is suggested to use larger point sizes to render fonts glyphs,
 * to reduce the effects of scale-dependent hints.
 * The result geometry is in the base coordinate system of the font.  
 * The geometry can be further transformed as necessary using
 * {@link AffineTransformation}s.
 * 
 * @author Martin Davis
 *
 */
public class FontGlyphReader 
{
  /**
   * The font name of the Java logical font Serif.
   */
  public static final String FONT_SERIF = "Serif";
  
  /**
   * The font name of the Java logical font SansSerif.
   * <p>
   * DEPRECATED - use FONT_SANSSERIF
   */
  public static final String FONT_SANSERIF = "SansSerif";
  
  
  /**
   * The font name of the Java logical font SansSerif.
   */
  public static final String FONT_SANSSERIF = "SansSerif";
  
  /**
   * The font name of the Java logical font Monospaced.
   */

  public static final String FONT_MONOSPACED = "Monospaced";
	
  // a flatness factor empirically determined to provide good results
  private static final double FLATNESS_FACTOR = 400;
  
  /**
   * Converts text rendered in the given font and pointsize to a {@link Geometry}
   * using a standard flatness factor.
   *  
   * @param text the text to render
   * @param fontName the name of the font
   * @param pointSize the pointSize to render at
   * @param geomFact the geometryFactory to use to create the result
   * @return a polygonal geometry representing the rendered text
   */
  public static Geometry read(String text, String fontName, int pointSize, GeometryFactory geomFact)
  {
    return read(text, new Font(fontName, Font.PLAIN, pointSize), geomFact);
  }
  
  /**
   * Converts text rendered in the given {@link Font} to a {@link Geometry}
   * using a standard flatness factor.
   * 
   * @param text the text to render
   * @param font  the font to render with
   * @param geomFact the geometryFactory to use to create the result
   * @return a polygonal geometry representing the rendered text
   */
  public static Geometry read(String text, Font font, GeometryFactory geomFact)
  {
    double flatness = font.getSize() / FLATNESS_FACTOR;
    return read(text, font, flatness, geomFact);
  }
  
  /**
   * Converts text rendered in the given {@link Font} to a {@link Geometry}
   * 
   * @param text the text to render
   * @param font  the font to render with
   * @param flatness the flatness factor to use
   * @param geomFact the geometryFactory to use to create the result
   * @return a polygonal geometry representing the rendered text
   */
  public static Geometry read(String text, Font font, double flatness, GeometryFactory geomFact)
  {
    char[] chs = text.toCharArray();
    FontRenderContext fontContext = new FontRenderContext(null, false, true);
    GlyphVector gv = font.createGlyphVector(fontContext, chs);
    List polys = new ArrayList();
    for (int i = 0; i < gv.getNumGlyphs(); i++) {
      Geometry geom = ShapeReader.read(gv.getGlyphOutline(i), flatness, geomFact);
      for (int j = 0; j < geom.getNumGeometries(); j++) {
        polys.add(geom.getGeometryN(j));
      }
    }
    return geomFact.buildGeometry(polys);
  }
      
}
