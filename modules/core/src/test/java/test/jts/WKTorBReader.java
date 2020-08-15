/*
 * Copyright (c) 2019 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;

/**
 * Reads a geometry from a string in either WKT or WKB format.
 * 
 * @author Martin Davis
 *
 */
public class WKTorBReader {
  
  public static Geometry read(String geomStr, GeometryFactory geomfact) {
    WKTorBReader rdr = new WKTorBReader(geomfact);
    try {
      return rdr.read(geomStr);
    }
    catch (ParseException ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }
  
  public static boolean isWKB(String str) {
    return isHex(str, MAX_CHARS_TO_CHECK);
  }

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
    if (chLow >= 'a' && chLow <= 'f') return true;
    return false;
  }

  private static final int MAX_CHARS_TO_CHECK = 6;
  private GeometryFactory geomFactory;
  
  public WKTorBReader(GeometryFactory geomFact) {
    this.geomFactory = geomFact;
  }
  
  public Geometry read(String geomStr) throws ParseException {
    String trimStr = geomStr.trim();
    if (isWKB(trimStr)) {
      return readWKBHex(trimStr, geomFactory);
    }
    return readWKT(trimStr, geomFactory);

  }
  
  public static Geometry readWKT(String wkt, GeometryFactory geomFact)
  throws ParseException 
  {
    WKTReader rdr = new WKTReader(geomFact);
    return rdr.read(wkt);
  }
  
  public static Geometry readWKBHex(String wkb, GeometryFactory geomFact)
  throws ParseException
  {
    WKBReader rdr = new WKBReader(geomFact);
    return rdr.read(WKBReader.hexToBytes(wkb));
  }
}
