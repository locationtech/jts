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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderFrame;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderToolBar;
import org.locationtech.jtstest.testbuilder.model.DisplayParameters;
import org.locationtech.jtstest.testbuilder.model.GeometryEditModel;
import org.locationtech.jtstest.testbuilder.model.LayerList;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
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
  public JTSTestBuilderController() {
    
  }

  public void setFillType(int fillType) {
    DisplayParameters.setFillType(fillType);
    geometryViewChanged();
  }
  
  public void geometryViewChanged()
  {
    getGeometryEditPanel().updateView();
    //TODO: provide autoZoom checkbox on Edit tab to control autozooming (default = on)
  }

  public GeometryEditPanel getGeometryEditPanel()
  {
    return JTSTestBuilderFrame.getGeometryEditPanel();
  }

  public GeometryEditModel geomEditModel() {
    return JTSTestBuilder.model().getGeometryEditModel();
  }
  
  public Geometry getGeometryA() {
    return geomEditModel().getGeometry(0);
  }

  public Geometry getGeometryB() {
    return geomEditModel().getGeometry(1);
  }

  public void exchangeGeometry() {
    geomEditModel().exchangeGeometry();
  }

  
  public void zoomToFullExtent()
  {
    getGeometryEditPanel().zoomToFullExtent();
  }
  
  public void zoomToInput()
  {
    getGeometryEditPanel().zoomToInput();
  }
  
  public void addTestCase(Geometry[] geom, String name)
  {
    model().addCase(geom, name);
    JTSTestBuilderFrame.instance().updateTestCases();
    JTSTestBuilderFrame.instance().showGeomsTab();
  }
  
  public void extractComponentsToTestCase(Coordinate pt)
  {
    double toleranceInModel = getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) 
      return;
    model().addCase(new Geometry[] { comp, null });
    JTSTestBuilderFrame.instance().updateTestCases();
  }
  
  public void extractComponentsToTestCase(Geometry aoi)
  {
    //double toleranceInModel = JTSTestBuilderFrame.getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry[] comp = lyrList.getComponents(aoi);
    if (comp == null) 
      return;
    model().addCase(comp);
    JTSTestBuilderFrame.instance().updateTestCases();
    toolbar().clearToolButtons();
    toolbar().unselectExtractComponentButton();
    editPanel().setCurrentTool(null);
  }

  public void copyComponentToClipboard(Coordinate pt)
  {
    double toleranceInModel = getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) 
      return;
    SwingUtil.copyToClipboard(comp, false);
  }
  
  public void setFocusGeometry(int index) {
    model().getGeometryEditModel().setEditGeomIndex(index);
    toolbar().setFocusGeometry(index);    
  }

  public void inspectGeometry()
  {
    JTSTestBuilderFrame.instance().actionInspectGeometry();
  }

  public void inspectResult()
  {
    JTSTestBuilderFrame.instance().inspectResult();
  }

  public void inspectGeometryDialog()
  {
    JTSTestBuilderFrame.instance().actionInspectGeometryDialog();
  }
  public void clearResult()
  {
    JTSTestBuilderFrame.instance().getResultWKTPanel().clearResult();
    model().setResult(null);
    editPanel().updateView();
  }

  public void saveImageAsPNG() {
    JTSTestBuilderFrame.instance().actionSaveImageAsPNG();
  }
  public void saveImageToClipboard() {
    JTSTestBuilderFrame.instance().actionSaveImageToClipboard();
  }
  
  public void updateLayerList() {
    JTSTestBuilderFrame.instance().updateLayerList();
  }
  
  //================================
      
  private TestBuilderModel model() {
    return JTSTestBuilderFrame.instance().getModel();
  }
  private GeometryEditPanel editPanel() {
    return JTSTestBuilderFrame.instance().getGeometryEditPanel();
  }

  private JTSTestBuilderToolBar toolbar() {
    return JTSTestBuilderFrame.instance().getToolbar();
  }  

}
