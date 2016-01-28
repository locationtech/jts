/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jtstest.testrunner;

/**
 * An Exception which indicates a problem during reflection
 *
 * @author Martin Davis
 * @version 1.7
 */
public class JTSTestReflectionException
    extends Exception
{
  public JTSTestReflectionException(String message) {
    super(message);
  }
  
  public JTSTestReflectionException(String opName, Object[] args) {
    super(createMessage(opName, args));
  }
  
  private static String createMessage(String opName, Object[] args) {
		String msg = "Could not find Geometry method: " + opName + "(";
		for (int j = 0; j < args.length; j++) {
			if (j > 0) {
				msg += ", ";
			}
			msg += args[j].getClass().getName();
		}
		msg += ")";
		return msg;
	}

}