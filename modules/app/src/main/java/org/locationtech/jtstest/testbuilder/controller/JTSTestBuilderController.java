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

package org.locationtech.jtstest.testbuilder.controller;

import javax.swing.JTextArea;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderFrame;
import org.locationtech.jtstest.testbuilder.model.LayerList;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;


public class JTSTestBuilderController 
{
  /*
  private static boolean autoZoomOnNextChange = false;

  
  public static void requestAutoZoom()
  {
    autoZoomOnNextChange  = true;
  }
  */
  
  public static void geometryViewChanged()
  {
    getGeometryEditPanel().updateView();
    //TODO: provide autoZoom checkbox on Edit tab to control autozooming (default = on)
  }

  public static GeometryEditPanel getGeometryEditPanel()
  {
    return JTSTestBuilderFrame.getGeometryEditPanel();
  }

  public static Geometry getGeometryA() {
    return JTSTestBuilder.model().getGeometryEditModel().getGeometry(0);
  }

  public static Geometry getGeometryB() {
    return JTSTestBuilder.model().getGeometryEditModel().getGeometry(1);
  }

  public static void zoomToFullExtent()
  {
    getGeometryEditPanel().zoomToFullExtent();
  }
  
  public static void zoomToInput()
  {
    getGeometryEditPanel().zoomToInput();
  }
  
  public static void addTestCase(Geometry[] geom, String name)
  {
    JTSTestBuilderFrame.instance().getModel().addCase(geom, name);
    JTSTestBuilderFrame.instance().updateTestCases();
  }
  
  public static void extractComponentsToTestCase(Coordinate pt)
  {
    double toleranceInModel = getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();
    Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) 
      return;
    JTSTestBuilderFrame.instance().getModel().addCase(new Geometry[] { comp, null });
    JTSTestBuilderFrame.instance().updateTestCases();
  }
  
  public static void extractComponentsToTestCase(Geometry aoi)
  {
    //double toleranceInModel = JTSTestBuilderFrame.getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();
    Geometry[] comp = lyrList.getComponents(aoi);
    if (comp == null) 
      return;
    JTSTestBuilderFrame.instance().getModel().addCase(comp);
    JTSTestBuilderFrame.instance().updateTestCases();
    JTSTestBuilderFrame.instance().getToolbar().clearToolButtons();
    JTSTestBuilderFrame.instance().getToolbar().unselectExtractComponentButton();
    JTSTestBuilderFrame.instance().getGeometryEditPanel().setCurrentTool(null);
  }
  
  public static void copyComponentToClipboard(Coordinate pt)
  {
    double toleranceInModel = getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();
    Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) 
      return;
    SwingUtil.copyToClipboard(comp, false);
  }
  
  public static void setFocusGeometry(int index) {
    JTSTestBuilderFrame.instance().getModel().getGeometryEditModel().setEditGeomIndex(index);
    JTSTestBuilderFrame.instance().getToolbar().setFocusGeometry(index);    
  }

  public static void inspectGeometry()
  {
    JTSTestBuilderFrame.instance().actionInspectGeometry();
  }
  public static void inspectGeometryDialog()
  {
    JTSTestBuilderFrame.instance().actionInspectGeometryDialog();
  }
}
