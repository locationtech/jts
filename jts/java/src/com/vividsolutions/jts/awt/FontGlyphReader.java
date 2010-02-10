package com.vividsolutions.jts.awt;

import java.util.*;
import java.awt.Font;
import java.awt.font.*;
import com.vividsolutions.jts.geom.*;

/**
 * Provides methods to read {@link Font} glyphs for strings 
 * into {@link Polygonal} geometry.
 * <p>
 * It is suggested to use larger point sizes to render fonts glyphs,
 * to reduce the effects of scale-dependent hints.
 * The resulting geometry are in the base coordinate system 
 * of the font.  
 * The geometry can be further transformed as necessary using
 * {@link AffineTransformation}s.
 * 
 * @author Martin Davis
 *
 */
public class FontGlyphReader 
{
	public static final String FONT_SERIF = "Serif";
	public static final String FONT_SANSERIF = "SanSerif";
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
   * @param flatness the flatness to use
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
