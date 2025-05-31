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
import org.locationtech.jtstest.testbuilder.ui.tools.SelectComponentTool;
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

  public GeometryEditModel geomEditModel() {
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
    return geomEditModel().getGeometry(0);
  }

  public Geometry getGeometryB() {
    return geomEditModel().getGeometry(1);
  }

  public void exchangeGeometry() {
    geomEditModel().exchangeGeometry();
  }
  
  public void addTestCase(Geometry[] geom, String name)
  {
    model().addCase(geom, name);
    JTSTestBuilderFrame.instance().updateTestCases();
    JTSTestBuilderFrame.instance().showGeomsTab();
  }
  
  public void extractComponentsToTestCase(Coordinate pt)
  {
    double toleranceInModel = editPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) 
      return;
    model().addCase(new Geometry[] { comp, null });
    JTSTestBuilderFrame.instance().updateTestCases();
  }
  
  public void extractComponentsToTestCase(Geometry aoi, boolean isSegments)
  {
    //double toleranceInModel = JTSTestBuilderFrame.getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry[] comp;
    comp = lyrList.getComponents(aoi, isSegments);
    if (comp == null) 
      return;
    model().addCase(comp);
    JTSTestBuilderFrame.instance().updateTestCases();
    toolbar().selectZoomButton();
    modeZoomIn();
  }

  public void copyComponentToClipboard(Coordinate pt)
  {
    double toleranceInModel = editPanel().getToleranceInModel();
    LayerList lyrList = model().getLayers();
    Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) 
      return;
    SwingUtil.copyToClipboard(comp, false);
  }
  
  public void selectComponents(Geometry aoi)
  {
    LayerList lyrList = model().getLayers();
    Geometry[] comp;
    comp = lyrList.getComponents(aoi);
    if (comp == null) {
      model().clearSelection();
    } 
    else {
      model().getLayerSelect().setEnabled(true);
      //TODO: allow selecting from A or B when enabled
      if (comp[0] != null) {
        model().setSelection(comp[0]);
      }
      else {
        model().setSelection(comp[1]);
      }
    }
    geometryViewChanged();
    layerListRefresh();
    layerListUpdate();
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

  public void inspectGeometry(String tag, Geometry geometry) {
    JTSTestBuilderFrame.instance().inspectGeometry(tag, geometry);
  }

  public void inspectGeometryDialogForCurrentCase()
  {
    int geomIndex = JTSTestBuilder.model().getGeometryEditModel().getGeomIndex();
    Geometry geometry = model().getCurrentCase().getGeometry(geomIndex);
    TestBuilderDialogs.inspectGeometry(frame(), geomIndex, geometry);
  }
  
  public void clearResult()
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
    setTool(SelectComponentTool.getInstance());
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
  
  public void caseMoveTo(int dir, boolean isZoom) {
    if (dir < 1) {
      model().cases().prevCase();
    }
    else {
    model().cases().nextCase();
    }
    frame().updateTestCaseView();
    if (isZoom) zoomToInput();
  }

  public void caseCopy() {
    model().cases().copyCase();
    frame().updateTestCases();
  }
  
  public void caseCreateNew() {
    model().cases().createNew();
    frame().showGeomsTab();
    frame().updateTestCases();
  }
  
  public void caseDelete() {
    model().cases().deleteCase();
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
