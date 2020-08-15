/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.geomop;

import java.lang.reflect.*;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.Assert;
import org.locationtech.jtstest.util.*;


/**
 * A {@link GeometryFunction} which calls a static
 * {@link Method}.
 * 
 * @author Martin Davis
 *
 */
public class StaticMethodGeometryFunction
	extends BaseGeometryFunction
{
	private static final String FUNCTIONS_SUFFIX = "Functions";
	private static final String PARAMETERS_SUFFIX = "Parameters";
	private static final String DESCRIPTION_SUFFIX = "Description";
	
	public static StaticMethodGeometryFunction createFunction(Method method)
	{
		Assert.isTrue(Geometry.class.isAssignableFrom((method.getParameterTypes())[0]));
		
		Class clz = method.getDeclaringClass();
		
		String funcName = method.getName();
		String description = "dummy";
		String[] paramNames = new String[] { };
		Class[] paramTypes = extractParamTypes(method);
		Class returnType = method.getReturnType();
		
		return new StaticMethodGeometryFunction(funcName, 
				description,
				paramNames, paramTypes,
				returnType, method);    
	}
	
	
	private static Class[] extractParamTypes(Method method)
	{
		Class[] methodParamTypes = method.getParameterTypes();
		Class[] types = new Class[methodParamTypes.length - 1];
		for (int i = 1; i < methodParamTypes.length; i++)
			types[i-1] = methodParamTypes[i];
		return types;
	}

  private Method method;

	public StaticMethodGeometryFunction(
			String name, 
			String description,
			String[] parameterNames, 
			Class[] parameterTypes, 
			Class returnType,
			Method method)
	{
		super(name, description, parameterNames, parameterTypes, returnType);
    this.method = method;
	}
	
  public Object invoke(Geometry g, Object[] arg) 
  {
    return invoke(method, null, createFullArgs(g, arg));
  }

  /**
   * Creates an arg array which includes the target geometry as the first argument
   * 
   * @param g
   * @param arg
   * @return
   */
  private static Object[] createFullArgs(Geometry g, Object[] arg)
  {
  	int fullArgLen = 1;
  	if (arg != null) 
  		fullArgLen = arg.length + 1;
  	Object[] fullArg = new Object[fullArgLen];
  	fullArg[0] = g;
  	for (int i = 1; i < fullArgLen; i++) {
  		fullArg[i] = arg[i-1];
  	}
  	return fullArg;
  }
  
  public static Object invoke(Method method, Object target, Object[] args)
  {
    Object result;
    try {
      result = method.invoke(target, args);
    }
    catch (InvocationTargetException ex) {
      Throwable t = ex.getCause();
      if (t instanceof RuntimeException)
      	throw (RuntimeException) t;
      throw new RuntimeException(invocationErrMsg(ex));
    }
    catch (Exception ex) {
      System.out.println(ex.getMessage());
      throw new RuntimeException(ex.getMessage());
    }
    return result;
  }
  
  private static String invocationErrMsg(InvocationTargetException ex)
  {
    Throwable targetEx = ex.getTargetException();
    String msg = getClassname(targetEx.getClass())
    + ": " +
    targetEx.getMessage();
    return msg;
  }
  
  public static String getClassname(Class javaClass)
  {
    String jClassName = javaClass.getName();
    int lastDotPos = jClassName.lastIndexOf(".");
    return jClassName.substring(lastDotPos + 1, jClassName.length());
  }
}
