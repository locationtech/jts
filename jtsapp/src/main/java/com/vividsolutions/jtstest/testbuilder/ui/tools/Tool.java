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

import java.awt.Cursor;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import com.vividsolutions.jtstest.testbuilder.GeometryEditPanel;


/**
 * @version 1.7
 */
public interface Tool extends MouseListener, MouseMotionListener, MouseWheelListener, KeyListener  {

    Cursor getCursor();

    void activate(GeometryEditPanel panel);

    void deactivate();
}
