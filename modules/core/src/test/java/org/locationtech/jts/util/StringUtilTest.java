package org.locationtech.jts.util;

import junit.framework.TestCase;

import java.math.RoundingMode;
import java.util.Locale;

public class StringUtilTest extends TestCase {

  public StringUtilTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(StringUtilTest.class);
  }

  public void testDecimalPattern()
  {
    String pattern = StringUtil.getDecimalFormatPattern();
    assertEquals("0.#", pattern);

    StringUtil.setDecimalFormatPattern("0.00");
    assertEquals("0.00", StringUtil.getDecimalFormatPattern());

    RoundingMode r = StringUtil.getRoundingMode();
    if (r != RoundingMode.UNNECESSARY) {
      StringUtil.setRoundingMode(RoundingMode.UNNECESSARY);
      assertEquals(RoundingMode.UNNECESSARY, StringUtil.getRoundingMode());
    }
    else {
      StringUtil.setRoundingMode(RoundingMode.UP);
      assertEquals(RoundingMode.UP, StringUtil.getRoundingMode());
    }

    StringUtil.setRoundingMode(r);
    assertEquals(r, StringUtil.getRoundingMode());

    assertEquals("0.00", StringUtil.getDecimalFormatPattern());

    StringUtil.setDecimalFormatPattern("0.#");
  }

  public void testDecimalSeperatorIsPoint() {

    Locale current = Locale.getDefault();
    Locale.setDefault(Locale.GERMAN);
    String s1 = StringUtil.toString(1.5);
    assertEquals("1.5", s1);
    String s2 = StringUtil.toString(1001.551);
    assertEquals("1001.6", s2);

    Locale.setDefault(current);
  }
}
