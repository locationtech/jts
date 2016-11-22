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
package org.locationtech.jtstest.geomop;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import org.locationtech.jts.geom.*;


/**
 * A registry to manage a collection of {@link GeometryFunction}s.
 * 
 * @author Martin Davis
 *
 */
public class GeometryFunctionRegistry 
{
  public static GeometryFunctionRegistry create()
  {
    GeometryFunctionRegistry funcRegistry = new GeometryFunctionRegistry();
    
    funcRegistry.add(TestCaseGeometryFunctions.class);

    
    return funcRegistry;
  }

	private List functions = new ArrayList();
	
	public GeometryFunctionRegistry()
	{
	}
	
	public GeometryFunctionRegistry(Class clz)
	{
		add(clz);
	}
	  	
	public static boolean hasGeometryResult(GeometryFunction func)
	{
		return Geometry.class.isAssignableFrom(func.getReturnType());
	}
	
	/**
	 * Adds functions for all the static methods in the given class.
	 * 
	 * @param geomFuncClass
	 */
	public void add(Class geomFuncClass)
	{
		List funcs = createFunctions(geomFuncClass);
		// sort list of functions so they appear nicely in the UI list
		Collections.sort(funcs);
		add(funcs);
	}
	
	/**
	 * Adds functions for all the static methods in the given class.
	 * 
	 * @param geomFuncClassname the name of the class to load and extract functions from
	 */
	public void add(String geomFuncClassname)
	 throws ClassNotFoundException
	{
		Class geomFuncClass = null;
		geomFuncClass = this.getClass().getClassLoader().loadClass(geomFuncClassname);
		add(geomFuncClass);
	}
	

	public void add(Collection funcs)
	{
		for (Iterator i = funcs.iterator(); i.hasNext(); ) {
			GeometryFunction f = (GeometryFunction) i.next();
			add(f);
		}
	}
	
	/**
	 * Create {@link GeometryFunction}s for all the static 
	 * methods in the given class
	 * 
	 * @param functionClass
	 * @return a list of the functions created
	 */
	public List createFunctions(Class functionClass) {
		List funcs = new ArrayList();
		Method[] method = functionClass.getMethods();
		for (int i = 0; i < method.length; i++) {
			int mod = method[i].getModifiers();
			if (Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
				funcs.add(StaticMethodGeometryFunction.createFunction(method[i]));
			}
		}
		return funcs;
	}

	
	/**
	 * Adds a function if it does not currently
   * exist in the registry, or replaces the existing one
	 * with the same signature.
	 * 
	 * @param func a function
	 */
	public void add(GeometryFunction func)
	{
		functions.add(func);
	}
	
	
	/*
		int index = functions.indexOf(func);
		if (index == -1) {
			sortedFunctions.put(func.getName(), func);
		}
		else {
			functions.set(index, func);
		}	
	}
	*/

	
  
  /**
   * Finds the first function which matches the given name and argument count.
   * 
   * @param name
   * @return a matching function, or null
   */
  public GeometryFunction find(String name, int argCount)
  {
    for (Iterator i = functions.iterator(); i.hasNext(); ) {
      GeometryFunction func = (GeometryFunction) i.next();
      String funcName = func.getName();
      if (funcName.equalsIgnoreCase(name) 
      		&& func.getParameterTypes().length == argCount)
        return func;
    }
    return null;
  }
  /**
   * Finds the first function which matches the given name.
   * 
   * @param name
   * @return a matching function, or null
   */
  public GeometryFunction find(String name)
  {
    for (Iterator i = functions.iterator(); i.hasNext(); ) {
      GeometryFunction func = (GeometryFunction) i.next();
      String funcName = func.getName();
      if (funcName.equalsIgnoreCase(name))
        return func;
    }
    return null;
  }
}
