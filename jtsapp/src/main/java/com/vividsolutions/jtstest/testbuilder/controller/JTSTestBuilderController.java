package com.vividsolutions.jtstest.testbuilder.controller;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.testbuilder.GeometryEditPanel;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilder;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilderFrame;
import com.vividsolutions.jtstest.testbuilder.model.LayerList;
import com.vividsolutions.jtstest.testbuilder.ui.SwingUtil;

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
}
