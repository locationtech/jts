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
package com.vividsolutions.jtstest.util;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;

/**
 * Reads a {@link Geometry} from a string which is in either WKT or WKBHex format
 *
 * @author Martin Davis
 * @version 1.7
 */
public class WKTOrWKBReader
{
  private static boolean isHex(String str, int maxCharsToTest)
  {
    for (int i = 0; i < maxCharsToTest && i < str.length(); i++) {
      char ch = str.charAt(i);
      if (! isHexDigit(ch))
        return false;
    }
    return true;
  }

  private static boolean isHexDigit(char ch)
  {
    if (Character.isDigit(ch)) return true;
    char chLow = Character.toLowerCase(ch);
    if (ch >= 'a' && ch <= 'f') return true;
    return false;
  }

  private static final int MAX_CHARS_TO_CHECK = 6;

  private GeometryFactory geomFactory;
  private WKTReader wktReader;
  private WKBReader wkbReader;

  public WKTOrWKBReader()
  {
    this(new GeometryFactory());
  }

  public WKTOrWKBReader(GeometryFactory geomFactory)
  {
    wktReader = new WKTReader(geomFactory);
    wkbReader = new WKBReader(geomFactory);
  }

  public Geometry read(String geomStr)
      throws ParseException
  {
    String trimStr = geomStr.trim();
    if (isHex(trimStr, MAX_CHARS_TO_CHECK))
      return wkbReader.read(WKBReader.hexToBytes(trimStr));
    return wktReader.read(trimStr);
  }
}