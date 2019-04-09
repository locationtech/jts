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

package org.locationtech.jts.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Utility methods for working with {@link String}s.
 * 
 * @author Martin Davis
 *
 */
public class StringUtil
{

  private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS;
  private static String DECIMAL_FORMAT_PATTERN;
  private static NumberFormat SIMPLE_ORDINATE_FORMAT;

  /**
   * Static initialization of {@linkplain #SIMPLE_ORDINATE_FORMAT}
   */
  static {
    DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.ROOT);
    setDecimalFormatPattern("0.#");
  }

  /**
   * Gets the pattern used to print numbers using {@linkplain #toString(double)} function
   *
   * @return the pattern
   */
  public static String getDecimalFormatPattern() {
    return DECIMAL_FORMAT_PATTERN;
  }

  /**
   * Sets the pattern used to print numbers using {@linkplain #toString(double)} function
   *
   * @param pattern a pattern
   */
  public static void setDecimalFormatPattern(String pattern) {
    DecimalFormat df = new DecimalFormat(pattern, DECIMAL_FORMAT_SYMBOLS);
    DECIMAL_FORMAT_PATTERN = pattern;
    SIMPLE_ORDINATE_FORMAT = df;
  }

  /**
   * Gets the {@linkplain RoundingMode} applied when using {@link #toString(double)}
   * @return A rounding mode
   */
  public static RoundingMode getRoundingMode() {
    return SIMPLE_ORDINATE_FORMAT.getRoundingMode();
  }

  /**
   * Sets the {@linkplain RoundingMode} to apply when using {@link #toString(double)}
   */
  public static void setRoundingMode(RoundingMode roundingMode) {
    SIMPLE_ORDINATE_FORMAT.setRoundingMode(roundingMode);
  }

  /**
   * Mimics the the Java SE {@link String#split(String)} method.
   *
   * @param s the string to split.
   * @param separator the separator to use.
   * @return the array of split strings.
   */
 public static String[] split(String s, String separator)
 {
   int separatorlen = separator.length();
   ArrayList tokenList = new ArrayList();
   String tmpString = "" + s;
   int pos = tmpString.indexOf(separator);
   while (pos >= 0) {
     String token = tmpString.substring(0, pos);
     tokenList.add(token);
     tmpString = tmpString.substring(pos + separatorlen);
     pos = tmpString.indexOf(separator);
   }
   if (tmpString.length() > 0)
     tokenList.add(tmpString);
   String[] res = new String[tokenList.size()];
   for (int i = 0; i < res.length; i++) {
     res[i] = (String) tokenList.get(i);
   }
   return res;
 }

 public final static String NEWLINE = System.getProperty("line.separator");

 /**
  *  Returns an throwable's stack trace
  */
 public static String getStackTrace(Throwable t) {
     ByteArrayOutputStream os = new ByteArrayOutputStream();
     PrintStream ps = new PrintStream(os);
     t.printStackTrace(ps);
     return os.toString();
 }

 public static String getStackTrace(Throwable t, int depth) {
     String stackTrace = "";
     StringReader stringReader = new StringReader(getStackTrace(t));
     LineNumberReader lineNumberReader = new LineNumberReader(stringReader);
     for (int i = 0; i < depth; i++) {
         try {
             stackTrace += lineNumberReader.readLine() + NEWLINE;
         } catch (IOException e) {
             Assert.shouldNeverReachHere();
         }
     }
     return stackTrace;
 }

  /**
   * Creates a string of the  provided {@link double} value using
   * the globally set {@linkplain #SIMPLE_ORDINATE_FORMAT}.
   *
   * @param d    a double value.
   * @return     A numerical string.
   *
   * @see #setDecimalFormatPattern(String)
   * @see #setRoundingMode(RoundingMode)
   */
  public static String toString(double d)
  {
    return SIMPLE_ORDINATE_FORMAT.format(d);
  }

  public static String spaces(int n)
  {
    return chars(' ', n);
  }
  
  public static String chars(char c, int n)
  {
    char[] ch = new char[n];
    for (int i = 0; i < n; i++) {
      ch[i] = c;
    }
    return new String(ch);
  }
}
