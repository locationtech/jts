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
package org.locationtech.jts.awt;

import java.awt.geom.Point2D;

import org.locationtech.jts.geom.Coordinate;

/**
 * Copies point ordinates with no transformation.
 * 
 * @author Martin Davis
 *
 */
public class IdentityPointTransformation
implements PointTransformation
{
	public void transform(Coordinate model, Point2D view)
	{
		view.setLocation(model.x, model.y);
	}
}