/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.*;

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
	
}
