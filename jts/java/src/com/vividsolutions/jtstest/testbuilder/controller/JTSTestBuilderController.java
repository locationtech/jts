package com.vividsolutions.jtstest.testbuilder.controller;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilderFrame;
import com.vividsolutions.jtstest.testbuilder.model.LayerList;

public class JTSTestBuilderController 
{

  public static void geometryViewChanged()
  {
    JTSTestBuilderFrame.getGeometryEditPanel().updateView();
  }

  public static void extractComponentToTest(Coordinate pt)
  {
    double toleranceInModel = JTSTestBuilderFrame.getGeometryEditPanel().getToleranceInModel();
    LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();
    Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) 
      return;
    JTSTestBuilderFrame.instance().getModel().addCase(new Geometry[] { comp, null });
    JTSTestBuilderFrame.instance().updateTestCases();
  }
}
