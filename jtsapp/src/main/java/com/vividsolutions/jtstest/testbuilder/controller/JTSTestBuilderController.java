package com.vividsolutions.jtstest.testbuilder.controller;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.testbuilder.GeometryEditPanel;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilderFrame;
import com.vividsolutions.jtstest.testbuilder.model.LayerList;
import com.vividsolutions.jtstest.testbuilder.ui.SwingUtil;

public class JTSTestBuilderController 
{
  private static boolean autoZoomOnNextChange = false;

  public static void requestAutoZoom()
  {
    autoZoomOnNextChange  = true;
  }
  public static void geometryViewChanged()
  {
    getGeometryEditPanel().updateView();
    //TODO: provide autoZoom checkbox on Edit tab to control autozooming (default = on)
    if  (autoZoomOnNextChange) {
      autoZoom();
    }
  }
  private static void autoZoom()
  {
    zoomToInput();
    autoZoomOnNextChange = false;
  }

  private static GeometryEditPanel getGeometryEditPanel()
  {
    return JTSTestBuilderFrame.getGeometryEditPanel();
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
