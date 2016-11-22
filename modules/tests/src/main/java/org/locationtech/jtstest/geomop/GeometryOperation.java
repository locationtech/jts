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

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.testrunner.Result;


/**
 * Interface for classes which execute operations on {@link Geometry}s.
 * The arguments may be presented as Strings, even if they
 * should be calling a method with non-String arguments.
 * Geometry will always be supplied as Geometry objects, however.
 * This interface abstracts out the invocation of a method
 * on a Geometry during a Test.  Subclasses can provide substitute
 * or additional methods during runs of the same test file.
 *
 * @author Martin Davis
 * @version 1.7
 */
public interface GeometryOperation
{
	/**
	 * Gets the class of the return type of the given operation.
	 * 
	 * @param opName the name of the operation
	 * @return the class of the return type of the specified operation
	 */
  public Class getReturnType(String opName);

  /**
   * Invokes an operation on a {@link Geometry}.
   *
   * @param opName name of the operation
   * @param geometry the geometry to process
   * @param args the arguments to the operation (which may be typed as Strings)
   * @return the result of the operation
   * @throws Exception if some error was encountered trying to find or process the operation
   */
  Result invoke(String opName, Geometry geometry, Object[] args)
      throws Exception;
}