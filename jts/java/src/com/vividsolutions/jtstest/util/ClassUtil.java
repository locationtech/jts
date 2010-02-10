package com.vividsolutions.jtstest.util;

public class ClassUtil 
{
  public static String getClassname(Class javaClass)
  {
    String jClassName = javaClass.getName();
    int lastDotPos = jClassName.lastIndexOf(".");
    return jClassName.substring(lastDotPos + 1, jClassName.length());
  }

}
