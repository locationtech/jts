
/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.precision;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryEditor;

/**
 * Reduces the precision of a {@link Geometry}
 * according to the supplied {@link PrecisionModel},
 * ensuring that the result is valid (unless specified otherwise).
 * <p>
 * By default the reduced result is topologically valid
 * (i.e. {@link Geometry#isValid()} is true).
 * To ensure this a polygonal geometry is reduced in a topologically valid fashion
 * (technically, by using snap-rounding).
 * Note that this may change polygonal geometry structure
 * (e.g. two polygons separated by a distance below the specified precision
 * will be merged into a single polygon).
 * <p>
 * In general input must be valid geometry, or an {@link IllegalArgumentException} 
 * will be thrown. However if the invalidity is "mild" or very small then it
 * may be eliminated by precision reduction.
 * <p> 
 * Alternatively, geometry can be reduced pointwise by using {@link #setPointwise(boolean)}.
 * In this case the result geometry topology may be invalid.
 * Linear and point geometry are always reduced pointwise (i.e. without further change to 
 * topology or structure), since this does not change validity.
 * <p>
 * By default the geometry precision model is not changed.
 * This can be overridden by using {@link #setChangePrecisionModel(boolean)}.
 * <p>
 * Normally collapsed components (e.g. lines collapsing to a point) 
 * are not included in the result. 
 * This behavior can be changed by using {@link #setRemoveCollapsedComponents(boolean)}.
 *
 * @version 1.12
 */
public class GeometryPrecisionReducer
{
	/**
	 * Convenience method for doing precision reduction 
   * on a single geometry,
	 * with collapses removed 
   * and keeping the geometry precision model the same,
   * and preserving polygonal topology.
	 * 
	 * @param g the geometry to reduce
	 * @param precModel the precision model to use
	 * @return the reduced geometry
   * @throws IllegalArgumentException if the reduction fails due to invalid input geometry is invalid
	 */
	public static Geometry reduce(Geometry g, PrecisionModel precModel)
	{
		GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(precModel);
		return reducer.reduce(g);
	}
	
	/**
	 * Convenience method for doing pointwise precision reduction 
   * on a single geometry,
	 * with collapses removed 
   * and keeping the geometry precision model the same,
   * but NOT preserving valid polygonal topology.
	 * 
	 * @param g the geometry to reduce
	 * @param precModel the precision model to use
	 * @return the reduced geometry
	 */
	public static Geometry reducePointwise(Geometry g, PrecisionModel precModel)
	{
		GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(precModel);
		reducer.setPointwise(true);
		return reducer.reduce(g);
	}
	
  private PrecisionModel targetPM;
  private boolean removeCollapsed = true;
  private boolean changePrecisionModel = false;
  private boolean isPointwise = false;

  public GeometryPrecisionReducer(PrecisionModel pm)
  {
    targetPM = pm;
  }

  /**
   * Sets whether the reduction will result in collapsed components
   * being removed completely, or simply being collapsed to an (invalid)
   * Geometry of the same type.
   * The default is to remove collapsed components.
   *
   * @param removeCollapsed if <code>true</code> collapsed components will be removed
   */
  public void setRemoveCollapsedComponents(boolean removeCollapsed)
  {
    this.removeCollapsed = removeCollapsed;
  }

  /**
   * Sets whether the {@link PrecisionModel} of the new reduced Geometry
   * will be changed to be the {@link PrecisionModel} supplied to
   * specify the precision reduction.
   * <p>  
   * The default is to <b>not</b> change the precision model
   *
   * @param changePrecisionModel if <code>true</code> the precision model of the created Geometry will be the
   * the precisionModel supplied in the constructor.
   */
  public void setChangePrecisionModel(boolean changePrecisionModel)
  {
    this.changePrecisionModel = changePrecisionModel;
  }

  /**
   * Sets whether the precision reduction will be done 
   * in pointwise fashion only.  
   * Pointwise precision reduction reduces the precision
   * of the individual coordinates only, but does
   * not attempt to recreate valid topology.
   * This is only relevant for geometries containing polygonal components.
   * 
   * @param isPointwise if reduction should be done pointwise only
   */
  public void setPointwise(boolean isPointwise)
  {
    this.isPointwise = isPointwise;
  }

  /**
   * Reduces the precision of a geometry, 
   * according to the specified strategy of this reducer.
   * 
   * @param geom the geometry to reduce
   * @return the precision-reduced geometry
   * @throws IllegalArgumentException if the reduction fails due to invalid input geometry is invalid
   */
  public Geometry reduce(Geometry geom)
  {
    Geometry reduced = PrecisionReducerTransformer.reduce(geom, targetPM, isPointwise);
    
    // TODO: incorporate this in the Transformer above
    if (changePrecisionModel) {
      return changePM(reduced, targetPM);
    }
    return reduced;
  }
  
  /**
   * Duplicates a geometry to one that uses a different PrecisionModel,
   * without changing any coordinate values.
   * 
   * @param geom the geometry to duplicate
   * @param newPM the precision model to use
   * @return the geometry value with a new precision model
   */
  private Geometry changePM(Geometry geom, PrecisionModel newPM)
  {
  	GeometryEditor geomEditor = createEditor(geom.getFactory(), newPM);
  	// this operation changes the PM for the entire geometry tree
  	return geomEditor.edit(geom, new GeometryEditor.NoOpGeometryOperation());
  }
  
  private GeometryEditor createEditor(GeometryFactory geomFactory, PrecisionModel newPM)
  {
    // no need to change if precision model is the same
  	if (geomFactory.getPrecisionModel() == newPM)
  		return new GeometryEditor();
  	// otherwise create a geometry editor which changes PrecisionModel
  	GeometryFactory newFactory = createFactory(geomFactory, newPM);
  	GeometryEditor geomEdit = new GeometryEditor(newFactory);
    return geomEdit;
  }
  
  private GeometryFactory createFactory(GeometryFactory inputFactory, PrecisionModel pm)
  {
    GeometryFactory newFactory 
  	= new GeometryFactory(pm, 
  			inputFactory.getSRID(),
  			inputFactory.getCoordinateSequenceFactory());
    return newFactory;
  }
  
}
