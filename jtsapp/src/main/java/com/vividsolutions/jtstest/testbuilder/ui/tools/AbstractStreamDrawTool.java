/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jtstest.testbuilder.AppCursors;
import com.vividsolutions.jtstest.testbuilder.model.GeometryType;

/**
 * @version 1.7
 */
public abstract class AbstractStreamDrawTool extends LineBandTool {

	protected AbstractStreamDrawTool() {
	  super(AppCursors.DRAW_GEOM);
	}

	protected abstract int getGeometryType();

  protected void mouseLocationChanged(MouseEvent e) {
    try {
      if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) 
          == InputEvent.BUTTON1_DOWN_MASK) {
        Coordinate newCoord = toModelCoordinate(e.getPoint());
        if (newCoord.distance(lastCoordinate()) < gridSize())
          return;
        //add(toModelSnapped(e.getPoint()));
        add(newCoord);
      }

      tentativeCoordinate = toModelSnapped(e.getPoint());
      redrawIndicator();
    } catch (Throwable t) {
    }
  }

  public void mousePressed(MouseEvent e) {
    setBandType();
    super.mousePressed(e);
  }


	public void mouseClicked(MouseEvent e) {
		setBandType();
		super.mouseClicked(e);
	}

	protected void bandFinished() throws Exception {
		setType();
		geomModel().addComponent(getCoordinates());
		panel().updateGeom();
	}

	private void setType() {
		if (panel().getModel() == null)
			return;
		panel().getGeomModel().setGeometryType(getGeometryType());
	}

	private void setBandType() {
		int geomType = getGeometryType();
		setCloseRing(geomType == GeometryType.POLYGON);
	}
}
