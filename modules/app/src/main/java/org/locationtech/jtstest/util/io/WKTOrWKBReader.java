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
package org.locationtech.jtstest.util.io;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;

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