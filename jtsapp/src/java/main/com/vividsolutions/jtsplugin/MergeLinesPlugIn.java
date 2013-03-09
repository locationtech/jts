
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtsplugin;

import com.vividsolutions.jump.workbench.model.Layer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jtstest.clean.LineStringExtracter;

import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringEndpointStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;



/**
 * @version 1.7
 */
public class MergeLinesPlugIn extends AbstractPlugIn {
  private static String LAYER_NAME = "Merged Lines";

  public boolean execute(PlugInContext context) throws Exception {
    LineMerger lineMerger = new LineMerger();
    lineMerger.add(node(extractLineStrings(
        context.getLayerViewPanel().getSelectionManager()
                             .getSelectedItems())));
    if (context.getLayerManager().getLayer(LAYER_NAME) != null) {
      context.getLayerManager().remove(context.getLayerManager().getLayer(LAYER_NAME));
    }
    Layer layer = context.addLayer(StandardCategoryNames.WORKING, LAYER_NAME,
      FeatureDatasetFactory.createFromGeometry(
        lineMerger.getMergedLineStrings()));
    layer.addStyle(new ArrowLineStringEndpointStyle.SolidEnd());
    layer.addStyle(new ArrowLineStringEndpointStyle.FeathersStart());
    return true;
  }
  
  private List extractLineStrings(Collection geometries) {
    ArrayList lineStrings = new ArrayList();
    LineStringExtracter extracter = new LineStringExtracter();
    for (Iterator i = geometries.iterator(); i.hasNext(); ) {
      Geometry geometry = (Geometry) i.next();
      lineStrings.add(extracter.extract(geometry));      
    }
    return lineStrings;
  }

  private Geometry node(List geometries) {
    if (geometries.isEmpty()) {
      return new GeometryFactory().createPoint((Coordinate) null);
    }

    Geometry union = (Geometry) geometries.get(0);
    for (int i = 1; i < geometries.size(); i++) {
      union = union.union((Geometry) geometries.get(i));
    }

    return union;
  }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(this, new String[] { "JTS" },
      getName(), false, null,
      new MultiEnableCheck().add(context.getCheckFactory()
                                        .createWindowWithLayerManagerMustBeActiveCheck())
                            .add(context.getCheckFactory()
                                        .createWindowWithSelectionManagerMustBeActiveCheck())
                            .add(context.getCheckFactory()
                                        .createAtLeastNItemsMustBeSelectedCheck(1)));
  }
}
