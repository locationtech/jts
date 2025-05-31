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
package org.locationtech.jtstest.testbuilder.ui.tools;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.geom.GeometryPartDeleter;


/**
 * Deletes vertices or components within a selection box from a geometry component
 * @version 1.7
 */
public class DeleteByBoxTool extends BoxBandTool {
  private static DeleteByBoxTool singleton = null;

  public static DeleteByBoxTool getInstance() {
    if (singleton == null)
      singleton = new DeleteByBoxTool();
    return singleton;
  }

  private DeleteByBoxTool() {
    super();
  }

  protected void gestureFinished() 
  {      
    Envelope env = getBox().getEnvelopeInternal();
    Geometry g = geomModel().getGeometry();
    
    Geometry edit = null;
    
    if (isRightButton()) {
      edit = GeometryPartDeleter.deleteVertices(g, env);
    }
    else if (isControlKeyDown()) {
      edit = GeometryPartDeleter.deleteComponents(g, env, true);
    }
    else {
      edit = GeometryPartDeleter.deleteComponents(g, env, false);
    }
    
    geomModel().setGeometry(edit);
  }


}
