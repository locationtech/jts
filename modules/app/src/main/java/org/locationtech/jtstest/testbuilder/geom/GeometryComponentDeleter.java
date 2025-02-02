/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.geom;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.GeometryEditor;

/**
 * Deletes a component from a geometry.
 * 
 * @author Martin Davis
 *
 */
public class GeometryComponentDeleter 
{

  public static Geometry deleteComponent(Geometry geom, Geometry component)
  {
    GeometryEditor editor = new GeometryEditor();
    DeleteComponentOperation compOp = new DeleteComponentOperation(component);
    Geometry compEditGeom = editor.edit(geom, compOp);
    if (compOp.isEdited()) return compEditGeom;
    return geom;
  }
  
  private static class DeleteComponentOperation
    implements GeometryEditor.GeometryEditorOperation
  {
    private Geometry component;
    private boolean isEdited = false;
    
    public DeleteComponentOperation(Geometry component)
    {
      this.component = component;
    }
    
    public boolean isEdited() { return isEdited; }

    @Override
    public Geometry edit(Geometry geometry, GeometryFactory factory)
    {               
      if (geometry == component) {
          isEdited = true;
          return null;
      }
      return geometry;
    }
    
  }
  
  
}
