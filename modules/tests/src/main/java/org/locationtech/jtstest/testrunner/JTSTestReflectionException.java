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

package org.locationtech.jtstest.testrunner;

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