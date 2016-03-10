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
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.*;

/**
 * A reification of a function which can be executed on a 
 * {@link Geometry}, possibly with other arguments.
 * The function may return a Geometry or a scalar value.
 * 
 * @author Martin Davis
 *
 */
public interface GeometryFunction 
{

	/**
	 * Gets the category name of this function
	 * 
	 * @return the category name of the function
	 */
	String getCategory();
	
	/**
	 * Gets the name of this function
	 * 
	 * @return the name of the function
	 */
	String getName();
	
	/**
	 * Gets the description of this function
	 * 
	 * @return the name of the function
	 */
	String getDescription();
	
	/**
	 * Gets the parameter names for this function
	 * 
	 * @return the names of the function parameters
	 */
	String[] getParameterNames();
	
	/**
	 * Gets the types of the other function arguments,
	 * if any.
	 * 
	 * @return the types
	 */
	Class[] getParameterTypes();
	
	/**
	 * Gets the return type of this function
	 * 
	 * @return the type of the value returned by this function
	 */
	Class getReturnType();
	
	/**
	 * Gets a string representing the signature of this function.
	 * 
	 * @return the string for the function signature
	 */
	String getSignature();
	
	/**
	 * Invokes this function.
	 * Note that any exceptions returned must be 
	 * {@link RuntimeException}s.
	 * 
	 * @param geom the target geometry 
	 * @param args the other arguments to the function
	 * @return the value computed by the function
	 * 
	 */
	Object invoke(Geometry geom, Object[] args);
	
	/**
	 * Two functions are the same if they have the 
	 * same name, parameter types and return type.
	 * 
	 * @param obj
	 * @return true if this object is the same as the <tt>obj</tt> argument
	 */
	boolean equals(Object obj);

  public abstract boolean isBinary();
	
}
