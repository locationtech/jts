package org.locationtech.jts.io;

import java.util.Locale;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class OrdinateFormatTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(OrdinateFormatTest.class);
  }

  public OrdinateFormatTest(String name) { super(name); }
  
  public void testLargeNumber() {
    // ensure scientific notation is not used
    checkFormat(1234567890.0, "1234567890");
  }

  public void testVeryLargeNumber() {
    // ensure scientific notation is not used
    // note output is rounded since it exceeds double precision accuracy
    checkFormat(12345678901234567890.0, "12345678901234567000");
  }

  public void testDecimalPoint() {
    checkFormat(1.123, "1.123");
  }

  public void testNegative() {
    checkFormat(-1.123, "-1.123");
  }

  public void testFractionDigits() {
    checkFormat(1.123456789012345, "1.123456789012345");
    checkFormat(0.0123456789012345, "0.0123456789012345");
  }

  public void testLimitedFractionDigits2() {
    checkFormat(1.123456789012345, 2, "1.12");
    checkFormat(1.123456789012345, 3, "1.123");
    checkFormat(1.123456789012345, 4, "1.1235");
    checkFormat(1.123456789012345, 5, "1.12346");
    checkFormat(1.123456789012345, 6, "1.123457");
  }

  public void testMaximumFractionDigits() {
    checkFormat(0.0000000000123456789012345, "0.0000000000123456789012345");
  }

  public void testPi() {
    checkFormat(Math.PI, "3.141592653589793");
  }

  public void testNaN() {
    checkFormat(Double.NaN, "NaN");
  }

  public void testInf() {
    checkFormat(Double.POSITIVE_INFINITY, "Inf");
    checkFormat(Double.NEGATIVE_INFINITY, "-Inf");
  }

  private void checkFormat(double d, String expected) {
    String actual = OrdinateFormat.DEFAULT.format(d);
    assertEquals(expected, actual);
  }
  
  private void checkFormat(double d, int maxFractionDigits, String expected) {
    OrdinateFormat format = OrdinateFormat.create(maxFractionDigits);
    String actual = format.format(d);
    assertEquals(expected, actual);
  }
  
  private void checkFormatAllLocales(double d, int maxFractionDigits, String expected) {
    OrdinateFormat format = OrdinateFormat.create(maxFractionDigits);
    String actual = format.format(d);
    assertEquals(expected, actual);
  }
  
  private void checkFormatLocales(Locale locale, double d, int maxFractionDigits, String expected) {
    OrdinateFormat format = OrdinateFormat.create(maxFractionDigits);
    String actual = format.format(d);
    assertEquals(expected, actual);
  }
}
