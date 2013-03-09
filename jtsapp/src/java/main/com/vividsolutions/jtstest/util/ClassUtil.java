package com.vividsolutions.jtstest.util;

import java.lang.reflect.Field;

public class ClassUtil 
{
  public static String getClassname(Class clz)
  {
    String jClassName = clz.getName();
    int lastDotPos = jClassName.lastIndexOf(".");
    return jClassName.substring(lastDotPos + 1, jClassName.length());
  }

  public static String[] getStringArrayClassField(Class clz, String name) 
  {
  	try {
  		Field field = clz.getField(name);
  		String[] str = (String[]) field.get(null);
  		return str;
  	}
  	catch (NoSuchFieldException ex) {  	}
  	catch (IllegalAccessException ex) {  	}
		return null;
  }
  
  public static String getStringClassField(Class clz, String name) 
  {
  	try {
  		Field[] f = clz.getDeclaredFields();
  		Field field = clz.getField(name);
  		String str = (String) field.get(null);
  		return str;
  	}
  	catch (NoSuchFieldException ex) {  	}
  	catch (IllegalAccessException ex) {  	}
		return null;
  }
}
