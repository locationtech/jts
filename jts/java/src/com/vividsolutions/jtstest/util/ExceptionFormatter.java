package com.vividsolutions.jtstest.util;

public class ExceptionFormatter {

  public static String getFullString(Throwable ex)
  {
    return ex.getClass().getName() + " : " + ex.toString();
  }

}
