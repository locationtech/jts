


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
package org.locationtech.jts.algorithm;

/**
 * Indicates that a {@link HCoordinate} has been computed which is
 * not representable on the Cartesian plane.
 *
 * @version 1.7
 * @see HCoordinate
 */
public class NotRepresentableException extends Exception {

  public NotRepresentableException() {
    super("Projective point not representable on the Cartesian plane.");
  }

}
