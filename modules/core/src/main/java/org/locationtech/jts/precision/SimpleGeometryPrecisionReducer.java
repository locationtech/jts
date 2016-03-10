
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
package org.locationtech.jts.precision;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryEditor;

/**
 * Reduces the precision of the coordinates of a {@link Geometry}
 * according to the supplied {@link PrecisionModel}, without
 * attempting to preserve valid topology.
 * <p>
 * In the case of {@link Polygonal} geometries, 
 * the topology of the resulting geometry may be invalid if
 * topological collapse occurs due to coordinates being shifted.
 * It is up to the client to check this and handle it if necessary.
 * Collapses may not matter for some uses.  An example
 * is simplifying the input to the buffer algorithm.
 * The buffer algorithm does not depend on the validity of the input geometry.
 *
 * @version 1.7
 * 
 * @deprecated use GeometryPrecisionReducer
 */
public class SimpleGeometryPrecisionReducer
{
	/**
	 * Convenience method for doing precision reduction on a single geometry,
	 * with collapses removed and keeping the geometry precision model the same.
	 * 
	 * @param g
	 * @param precModel
	 * @return the reduced geometry
	 */
	public static Geometry reduce(Geometry g, PrecisionModel precModel)
	{
		SimpleGeometryPrecisionReducer reducer = new SimpleGeometryPrecisionReducer(precModel);
		return reducer.reduce(g);
	}
	
  private PrecisionModel newPrecisionModel;
  private boolean removeCollapsed = true;
  private boolean changePrecisionModel = false;

  public SimpleGeometryPrecisionReducer(PrecisionModel pm)
  {
    newPrecisionModel = pm;
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

  public Geometry reduce(Geometry geom)
  {
    GeometryEditor geomEdit;
    if (changePrecisionModel) {
      GeometryFactory newFactory = new GeometryFactory(newPrecisionModel, geom.getFactory().getSRID());
      geomEdit = new GeometryEditor(newFactory);
    }
    else
      // don't change geometry factory
      geomEdit = new GeometryEditor();

    return geomEdit.edit(geom, new PrecisionReducerCoordinateOperation());
  }

  private class PrecisionReducerCoordinateOperation
      extends GeometryEditor.CoordinateOperation
  {
    public Coordinate[] edit(Coordinate[] coordinates, Geometry geom)
    {
      if (coordinates.length == 0) return null;

      Coordinate[] reducedCoords = new Coordinate[coordinates.length];
      // copy coordinates and reduce
      for (int i = 0; i < coordinates.length; i++) {
        Coordinate coord = new Coordinate(coordinates[i]);
        newPrecisionModel.makePrecise(coord);
        reducedCoords[i] = coord;
      }
      // remove repeated points, to simplify returned geometry as much as possible
      CoordinateList noRepeatedCoordList = new CoordinateList(reducedCoords, false);
      Coordinate[] noRepeatedCoords = noRepeatedCoordList.toCoordinateArray();

      /**
       * Check to see if the removal of repeated points
       * collapsed the coordinate List to an invalid length
       * for the type of the parent geometry.
       * It is not necessary to check for Point collapses, since the coordinate list can
       * never collapse to less than one point.
       * If the length is invalid, return the full-length coordinate array
       * first computed, or null if collapses are being removed.
       * (This may create an invalid geometry - the client must handle this.)
       */
      int minLength = 0;
      if (geom instanceof LineString) minLength = 2;
      if (geom instanceof LinearRing) minLength = 4;

      Coordinate[] collapsedCoords = reducedCoords;
      if (removeCollapsed) collapsedCoords = null;

      // return null or orginal length coordinate array
      if (noRepeatedCoords.length < minLength) {
          return collapsedCoords;
      }

      // ok to return shorter coordinate array
      return noRepeatedCoords;
    }
  }
}
