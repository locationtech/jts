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
package org.locationtech.jtstest.testbuilder.ui.tools;

import org.locationtech.jtstest.testbuilder.model.*;


/**
 * @version 1.7
 */
public class LineStringTool extends AbstractStreamDrawTool 
{
    private static LineStringTool singleton = null;

    public static LineStringTool getInstance() {
        if (singleton == null)
            singleton = new LineStringTool();
        return singleton;
    }

    private LineStringTool() 
    {
    }
    
    protected int getGeometryType()
    {
    	return GeometryType.LINESTRING;
    }
 }
