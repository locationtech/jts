/*
 * Copyright (c) 2022 Martin Davis.
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
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.GeometryEditor;

/**
 * Transforms a component in a geometry.
 * 
 * @author Martin Davis
 *
 */
public class GeometryComponentTransformer 
{

  public static Geometry transform(Geometry geom, AffineTransformation trans)
  {
    Geometry geomTrans = geom.copy();
    geomTrans.apply(trans);
    return geomTrans;
  }
  
  public static Geometry transform(Geometry geom, Geometry component, AffineTransformation trans)
  {
    GeometryEditor editor = new GeometryEditor();
    TransformOperation compOp = new TransformOperation(component, trans);
    Geometry compEditGeom = editor.edit(geom, compOp);
    if (compOp.isEdited()) return compEditGeom;
    return geom;
  }
  
  private static class TransformOperation
    implements GeometryEditor.GeometryEditorOperation
  {
    private Geometry component;
    private boolean isEdited = false;
    private AffineTransformation trans;
    
    public TransformOperation(Geometry component, AffineTransformation trans)
    {
      this.component = component;
      this.trans = trans;
    }
    
    public boolean isEdited() { return isEdited; }

    @Override
    public Geometry edit(Geometry geometry, GeometryFactory factory)
    {               
      if (geometry == component) {
          isEdited = true;
          Geometry compTrans = component.copy();
          compTrans.apply(trans);
          return compTrans;
      }
      return geometry;
    }
    
  }
  
  
}
