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

package org.locationtech.jtstest.testbuilder.controller;


import java.awt.Color;

import javax.swing.JFileChooser;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jtstest.clean.CleanDuplicatePoints;
import org.locationtech.jtstest.testbuilder.AppConstants;
import org.locationtech.jtstest.testbuilder.AppStrings;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderFrame;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderToolBar;
import org.locationtech.jtstest.testbuilder.SpatialFunctionPanel;
import org.locationtech.jtstest.testbuilder.TestBuilderDialogs;
import org.locationtech.jtstest.testbuilder.geom.GeometryElementLocater;
import org.locationtech.jtstest.testbuilder.model.GeometryEditModel;
import org.locationtech.jtstest.testbuilder.model.LayerList;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.ui.ImageUtil;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testbuilder.ui.render.ViewStyle;
import org.locationtech.jtstest.testbuilder.ui.tools.DeleteByBoxTool;
import org.locationtech.jtstest.testbuilder.ui.tools.EditVertexTool;
import org.locationtech.jtstest.testbuilder.ui.tools.ExtractComponentTool;
import org.locationtech.jtstest.testbuilder.ui.tools.InfoTool;
import org.locationtech.jtstest.testbuilder.ui.tools.LineStringTool;
import org.locationtech.jtstest.testbuilder.ui.tools.MoveTool;
import org.locationtech.jtstest.testbuilder.ui.tools.PanTool;
import org.locationtech.jtstest.testbuilder.ui.tools.PointTool;
import org.locationtech.jtstest.testbuilder.ui.tools.RectangleTool;
import org.locationtech.jtstest.testbuilder.ui.tools.SelectElementTool;
import org.locationtech.jtstest.testbuilder.ui.tools.StreamPolygonTool;
import org.locationtech.jtstest.testbuilder.ui.tools.Tool;
import org.locationtech.jtstest.testbuilder.ui.tools.ZoomTool;


public class JTSTestBuilderController 
{ 
  private static ResultController resultController = new ResultController();

  /*
  private static boolean autoZoomOnNextChange = false;

  
  public static void requestAutoZoom()
  {
    autoZoomOnNextChange  = true;
  }
  */
  public JTSTestBuilderController() {
    
  }
  public static ResultController resultController() {
    return resultController;
  }
  public static TestBuilderModel model() {
    return frame().getModel();
  }
  public static GeometryEditPanel editPanel() {
    return JTSTestBuilderFrame.getGeometryEditPanel();
  }
  public static SpatialFunctionPanel spatialFunctionPanel() {
    return JTSTestBuilderFrame.getSpatialFunctionPanel();
  }

  public static JTSTestBuilderToolBar toolbar() {
    return frame().getToolbar();
  }  

  public static JTSTestBuilderFrame frame() {
    return JTSTestBuilderFrame.instance();
  }

  public GeometryEditModel getGeomEditModel() {
    return JTSTestBuilder.model().getGeometryEditModel();
  }
  
  public void reportException(Exception e) {
    SwingUtil.reportException(frame(), e);
  }
  
  public void geometryChanged()
  {
    if (spatialFunctionPanel().isAutoExecute()) {
      resultController.execute(false);
    }
    geometryViewChanged();
  }
  
  public void geometryViewChanged()
  {
    editPanel().updateView();
    //TODO: provide autoZoom checkbox on Edit tab to control autozooming (default = on)
  }
  
  public Geometry getGeometryA() {
    return getGeomEditModel().getGeometry(0);
  }

  public Geometry getGeometryB() {
    return getGeomEditModel().getGeometry(1);
  }

  public void exchangeGeometry() {
    getGeomEditModel().exchangeGeometry();
  }
  
  public void caseAdd(Geometry[] geoms, String name)
  {
    model().addCase(geoms, name);
    JTSTestBuilderFrame.instance().updateTestCases();
    JTSTestBuilderFrame.instance().showGeomsTab();
    selectClear();
  }
  
  public void copyElementsToTestCase(Coordinate pt)
  {
    double toleranceInModel = editPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry comp = lyrList.getElement(pt, toleranceInModel);
    if (comp == null) 
      return;
    caseAdd(new Geometry[] { comp, null }, "Extract");
  }
  
  public void copyElementsToTestCase(Geometry aoi, boolean isSegments)
  {
    //double toleranceInModel = JTSTestBuilderFrame.getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry[] comp;
    comp = lyrList.getElements(aoi, isSegments);
    if (comp == null) 
      return;
    caseAdd(comp, "Extract");
    toolbar().selectZoomButton();
    modeZoomIn();
  }

  public void copyElementToClipboard(Coordinate pt)
  {
    double toleranceInModel = editPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry comp = lyrList.getElement(pt, toleranceInModel);
    if (comp == null) 
      return;
    SwingUtil.copyToClipboard(comp, false);
  }
  
  public void selectElements(Geometry aoi)
  {
    Geometry geom = model().getGeometryEditModel().getGeometry();
    Geometry comp = null;
    if (geom != null) {
      comp = GeometryElementLocater.extractElements(geom, aoi);
    }
    if (comp == null) {
      model().clearSelection();
    } 
    else {
      model().getLayerSelect().setEnabled(true);
      model().setSelection(comp);
    }
    geometryViewChanged();
    layerListRefresh();
  }

  public void selectClear() {
    model().clearSelection();
    layerListRefresh();
  }
  
  public void setFocusGeometry(int index) {
    model().getGeometryEditModel().setEditGeomIndex(index);
    toolbar().setFocusGeometry(index);    
  }

  public void flash(Geometry geom)
  {
    JTSTestBuilderFrame.getGeometryEditPanel().flash(geom);
  }
  
  public void inspectGeometry()
  {
    JTSTestBuilderFrame.instance().inspectGeometry();
  }

  public void inspectResult()
  {
    JTSTestBuilderFrame.instance().inspectResult();
  }

  public void inspectGeometry(String name, Geometry geometry) {
    JTSTestBuilderFrame.instance().inspectGeometry(name, geometry);
  }

  public void inspectGeometryDialog(String name, Geometry geometry)
  {
    TestBuilderDialogs.inspectGeometry(frame(), name, geometry);
  }
  
  public void inspectGeometryDialogForCurrentCase()
  {
    int geomIndex = model().getGeometryEditModel().getGeomIndex();
    Geometry geometry = model().getCurrentCase().getGeometry(geomIndex);
    TestBuilderDialogs.inspectGeometry(frame(), geomIndex, geometry);
  }
  
  public void resultClear()
  {
    frame().getResultWKTPanel().clearResult();
    model().setResult(null);
    editPanel().updateView();
  }
  
  public void setResult(String opName, Object result) {
    model().setResult(result);
    model().setOpName(opName);
    frame().getResultWKTPanel().setOpName(opName);
    frame().getResultWKTPanel().setExecutedTime("");
    frame().getResultWKTPanel().setResult(result);
    geometryViewChanged();
  }
  
  public void setCommandErr(String msg) {
    frame().getCommandPanel().setError(msg);
  }
  
  public void saveImageAsPNG() {
    //JTSTestBuilderFrame.instance().actionSaveImageAsPNG();
    JFileChooser pngFileChooser = TestBuilderDialogs.getSavePNGFileChooser();
    try {
      String fullFileName = SwingUtil.chooseFilenameWithConfirm(frame(), pngFileChooser);  
      if (fullFileName == null) return;
        ImageUtil.writeImage(editPanel(), 
            fullFileName,
            ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      reportException(x);
    }
  }
  
  public void saveImageToClipboard() {
    try {
      ImageUtil.saveImageToClipboard(editPanel(), 
          ImageUtil.IMAGE_FORMAT_NAME_PNG);
    }
    catch (Exception x) {
      reportException(x);
    }
  }

  //==================================
  
  public void layerListUpdate() {
    JTSTestBuilderFrame.instance().updateLayerList();
  }
  
  public void layerListRefresh() {
    JTSTestBuilderFrame.instance().refreshLayerList();
  }
  
  //================================
      

  
  private void setTool(Tool tool) {
    editPanel().setCurrentTool(tool);
  }
  public void modeDrawRectangle() {
    setTool(RectangleTool.getInstance());
  }

  public void modeDrawPolygon() {
    setTool(StreamPolygonTool.getInstance());
  }

  public void modeDrawLineString() {
    setTool(LineStringTool.getInstance());
  }

  public void modeDrawPoint() {
    setTool(PointTool.getInstance());
  }

  public void modeInfo() {
    setTool(InfoTool.getInstance());
  }

  public void modeExtractComponent() {
    setTool(ExtractComponentTool.getInstance());
  }

  public void modeSelectComponent() {
    setTool(SelectElementTool.getInstance());
  }

  public void modeDeleteVertex() {
    setTool(DeleteByBoxTool.getInstance());
  }
  public void modeEditVertex() {
    setTool(EditVertexTool.getInstance());
  }
  public void modeMove() {
    setTool(MoveTool.getInstance());
  }
  public void modeZoomIn() {
    setTool(ZoomTool.getInstance());
  }

  public void modePan() {
    setTool(PanTool.getInstance());
  }
  public void zoomOneToOne() {
    editPanel().getViewport().zoomToInitialExtent();
  }

  public void zoomToFullExtent() {
    editPanel().zoomToFullExtent();
  }

  public void zoomToResult() {
    editPanel().zoomToResult();
  }

  public void zoomToInput() {
    editPanel().zoomToInput();
  }

  public void zoomToInputA() {
    editPanel().zoomToGeometry(0);
  }

  public void zoomToInputB() {
    editPanel().zoomToGeometry(1);
  }
  
  public void zoomToGeometry(Geometry geom) {
    editPanel().zoom(geom);
  }
  
  public void caseMoveTo(int dir, boolean isZoom) {
    if (dir < 1) {
      model().cases().prevCase();
    }
    else {
    model().cases().nextCase();
    }
    frame().updateTestCaseView();
    selectClear();
    if (isZoom) zoomToInput();
  }

  public void caseCopy() {
    model().cases().copyCase();
    frame().updateTestCases();
    selectClear();
  }
  
  public void caseCreateNew() {
    model().cases().createNew();
    frame().showGeomsTab();
    frame().updateTestCases();
    selectClear();
  }
  
  public void caseDelete() {
    model().cases().deleteCase();
    frame().updateTestCases();
    selectClear();
  }
  
  //========================================

  public void resultCopyToTest() 
  {
    Object currResult = model().getResult();
    if (! (currResult instanceof Geometry))
      return;
    model().addCase(
        new Geometry[] { (Geometry) currResult, null }, 
        "Result of " + model().getOpName());
    frame().updateTestCases(); 
  }
  
  //========================================
  
  public void displayInfo(Coordinate modelPt)
  {
    displayInfo( editPanel().getInfo(modelPt) );
  }
  
  public void displayInfo(String s)
  {
    displayInfo(s, true);
  }
  
  public void displayInfo(String s, boolean showTab)
  {
    frame().getLogPanel().addInfo(s);
    if (showTab) frame().showInfoTab();
  }
  
  //========================================
  
  public void setViewStyle(ViewStyle viewStyle) {
    editPanel().setViewStyle(viewStyle);
    geometryViewChanged();
  }

  //=============================================
  
  public void removeDuplicatePoints() {
    CleanDuplicatePoints clean = new CleanDuplicatePoints();
    Geometry cleanGeom = clean.clean(model().getGeometryEditModel().getGeometry(0));
    model().getCurrentCase().setGeometry(0, cleanGeom);
    frame().geometryChanged();
  }

  public void changeToLines() {
    Geometry cleanGeom = LinearComponentExtracter.getGeometry(model().getGeometryEditModel().getGeometry(0));
    model().getCurrentCase().setGeometry(0, cleanGeom);
    frame().geometryChanged();
  }
  
  //=============================================

  public void indicatorShow(Geometry geom, Color lineClr)
  {
    if (! JTSTestBuilderFrame.isShowingIndicators()) return;
    
    if (JTSTestBuilderFrame.isSavingIndicators()) {
      //-- refresh layer list panel only when indicator layer is created
      boolean refreshLayerList = ! model().hasLayer(AppStrings.LYR_INDICATORS);
      model().addIndicator(geom);
      if (refreshLayerList) 
        frame().refreshLayerList();
    }
    editPanel().draw(geom, lineClr, AppConstants.INDICATOR_FILL_CLR);
  }
}
