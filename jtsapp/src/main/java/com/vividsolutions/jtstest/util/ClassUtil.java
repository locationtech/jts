package com.vividsolutions.jtstest.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
  
  public static Object dynamicCall(String clzName, String methodName, Class[] methodParamTypes, Object[] methodArgs)
      throws ClassNotFoundException, SecurityException, NoSuchMethodException,
      IllegalArgumentException, InstantiationException, IllegalAccessException,
      InvocationTargetException
  {
    Class clz = Class.forName(clzName);
    
    Class[] constParTypes = new Class[] { String.class, String.class };
    Constructor constr = clz.getConstructor(new Class[0]);
    Object dummyto = constr.newInstance(new Object[0]);
    
    Method meth = clz.getMethod(methodName, methodParamTypes);
    Object result = meth.invoke(dummyto, methodArgs);
    return result;
  }

}
