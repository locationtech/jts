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
package com.vividsolutions.jtstest.testbuilder;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jtstest.testbuilder.model.*;
import com.vividsolutions.jtstest.testbuilder.ui.*;
import com.vividsolutions.jtstest.testbuilder.ui.style.AWTUtil;
import com.vividsolutions.jtstest.testbuilder.ui.tools.*;
import com.vividsolutions.jtstest.testbuilder.ui.render.*;

/**
 * Panel which displays rendered geometries.
 * 
 * @version 1.7
 */
public class GeometryEditPanel extends JPanel 
{
  private static double HIGHLIGHT_SIZE = 50.0;
  private static Color HIGHLIGHT_COLOR = new Color(255, 192, 0, 150);

  private static Color[] selectedPointColor = { new Color(0, 64, 128, 255),
      new Color(170, 64, 0, 255) };


  private TestBuilderModel model;
  private GeometryEditModel geomModel;
  
  
  private DrawingGrid grid = new DrawingGrid();
  private GridRenderer gridRenderer;

  boolean stateAddingPoints = false;

  Coordinate highlightPoint;
  Point2D lastPt = new Point2D.Double();

  private Tool currentTool = null;  //PolygonTool.getInstance();

  private Viewport viewport = new Viewport(this);

  //----------------------------------------
  BorderLayout borderLayout1 = new BorderLayout();
  private RenderManager renderMgr;
  //private OperationMonitorManager opMonitor;
  
  public GeometryEditPanel() {
    gridRenderer = new GridRenderer(viewport, grid);
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    renderMgr = new RenderManager(this);
    //opMonitor = new OperationMonitorManager(this, viewport);

    setToolTipText("");
    setBorder(BorderFactory.createEmptyBorder());
    setCurrentTool(RectangleTool.getInstance());
  }

  
  public void setModel(TestBuilderModel model) {
    this.model = model;
    geomModel = model.getGeometryEditModel();
  }

  public TestBuilderModel getModel() {
    return model;
  }

  public void setGridEnabled(boolean isEnabled) {
    gridRenderer.setEnabled(isEnabled);
  }

  public GeometryEditModel getGeomModel()
  {
    return model.getGeometryEditModel();
  }
  public Viewport getViewport() { return viewport; }

  public void updateView()
  {
  	renderMgr.setDirty(true);

//    fireGeometryChanged(new GeometryEvent(this));
    forceRepaint();
  }
  
  public void forceRepaint() {
    renderMgr.setDirty(true);

    Component source = SwingUtilities.windowForComponent(this);
    if (source == null)
      source = this;
    source.repaint();
  }

  private LayerList getLayerList()
  {
    return model.getLayers();
  }
  
  public void setShowingInput(boolean isEnabled)
  {
    if (model == null) return;
    getLayerList().getLayer(LayerList.LYR_A).setEnabled(isEnabled);
    getLayerList().getLayer(LayerList.LYR_B).setEnabled(isEnabled);
    forceRepaint();
  }
  
  public void setShowingGeometryA(boolean isEnabled) {
    if (model == null) return;
    getLayerList().getLayer(LayerList.LYR_A).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setShowingGeometryB(boolean isEnabled) {
    if (model == null) return;
    getLayerList().getLayer(LayerList.LYR_B).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setShowingResult(boolean isEnabled) 
  {
    if (model == null) return;
    getLayerList().getLayer(LayerList.LYR_RESULT).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setGridSize(double gridSize) {
    grid.setGridSize(gridSize);
    forceRepaint();
  }

  public void setHighlightPoint(Coordinate pt) {
    highlightPoint = pt;
  }

  public boolean isAddingPoints() {
    return stateAddingPoints;
  }

  public void updateGeom()
  {
  	renderMgr.setDirty(true);
    geomModel.geomChanged();
  }
  
  public String getToolTipText(MouseEvent event) {
//    if (event.getPoint().x < 100) return null;
    Coordinate pt = viewport.toModelCoordinate(event.getPoint());
    double toleranceInModel = EditVertexTool.TOLERANCE_PIXELS / getViewport().getScale();
    return GeometryLocationsWriter.writeLocation(getLayerList(), pt, toleranceInModel);
//    return viewport.toModel(event.getPoint()).toString();
//    return null;
  }

  public String getInfo(Coordinate pt)
  {
    double toleranceInModel = EditVertexTool.TOLERANCE_PIXELS / getViewport().getScale();
    GeometryLocationsWriter writer = new GeometryLocationsWriter();
    writer.setHtml(false);
    return writer.writeLocationString(getLayerList(), pt, toleranceInModel);
  }

  public double getGridSize() {
    return grid.getGridSize();
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    renderMgr.render();
    renderMgr.copyImage(g);
  }
  
  /*
   // MD - obsolete
  public void render(Graphics g)
  {
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    
    gridRenderer.paint(g2);
    getLayerList().paint((Graphics2D) g2, viewport);
  }
  */
  
  private void drawHighlight(Graphics2D g) {
    if (highlightPoint == null)
      return;
    double size = HIGHLIGHT_SIZE;
    Point2D viewPt = viewport.convert(highlightPoint);
    double x = viewPt.getX();
    double y = viewPt.getY();
    Ellipse2D.Double shape = new Ellipse2D.Double(x - size / 2, y - size / 2,
        size, size);
    AWTUtil.setStroke(g, 4);
    g.setColor(HIGHLIGHT_COLOR);
    g.draw(shape);
  }


  public Point2D snapToGrid(Point2D modelPoint) {
    return grid.snapToGrid(modelPoint);
  }

  void jbInit() throws Exception {
    this.addComponentListener(new java.awt.event.ComponentAdapter() {

      public void componentResized(ComponentEvent e) {
        this_componentResized(e);
      }
    });
    this.setBackground(Color.white);
    this.setBorder(BorderFactory.createLoweredBevelBorder());
    this.setLayout(borderLayout1);
    /*
    this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

      public void mouseMoved(MouseEvent e) {
        this_mouseMoved(e);
      }
    });
    this.addMouseListener(new java.awt.event.MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        this_mouseClicked(e);
      }
    });
    */
  }

  /*
  void this_mouseClicked(MouseEvent e) {
  }

  void this_mouseMoved(MouseEvent e) {
  }
*/
  
  void this_componentResized(ComponentEvent e) {
  	renderMgr.componentResized();
    viewport.update();
  }

  public void setCurrentTool(Tool currentTool) {
    removeMouseListener(this.currentTool);
    removeMouseMotionListener(this.currentTool);
    this.currentTool = currentTool;
    currentTool.activate();
    setCursor(currentTool.getCursor());
    addMouseListener(currentTool);
    addMouseMotionListener(currentTool);
  }

  public void zoomToInput() {
    zoom(geomModel.getEnvelope());
  }

  public void zoomToFullExtent() {
    zoom(geomModel.getEnvelopeAll());
  }

  public void zoom(Envelope zoomEnv) 
  {
  	renderMgr.setDirty(true);
  	
    if (zoomEnv.isNull()) {
      viewport.zoomToInitialExtent();
      return;
    }

    double averageExtent = (zoomEnv.getWidth() + zoomEnv.getHeight()) / 2d;
    double buffer = averageExtent * 0.03;
    
    zoomEnv.expandToInclude(zoomEnv.getMaxX() + buffer,
    		zoomEnv.getMaxY() + buffer);
    zoomEnv.expandToInclude(zoomEnv.getMinX() - buffer,
    		zoomEnv.getMinY() - buffer);
    viewport.zoom(zoomEnv);
  }

  public void zoom(Point center,
			double realZoomFactor) {

  	renderMgr.setDirty(true);

		double width = getSize().width / realZoomFactor;
		double height = getSize().height / realZoomFactor;
		double bottomOfNewViewAsPerceivedByOldView = center.y
				+ (height / 2d);
		double leftOfNewViewAsPerceivedByOldView = center.x
				- (width / 2d);
		Point bottomLeftOfNewViewAsPerceivedByOldView = new Point(
				(int) leftOfNewViewAsPerceivedByOldView,
				(int) bottomOfNewViewAsPerceivedByOldView);
		Point2D bottomLeftOfNewViewAsPerceivedByModel = viewport.toModel(bottomLeftOfNewViewAsPerceivedByOldView);
		viewport.setScale(getViewport().getScale() * realZoomFactor);
		viewport.setViewOrigin(bottomLeftOfNewViewAsPerceivedByModel.getX(), bottomLeftOfNewViewAsPerceivedByModel.getY());
	}

  public void zoomPan(double xDisplacement, double yDisplacement) 
  {
  	renderMgr.setDirty(true);
    getViewport().setViewOrigin(getViewport().getViewOriginX() - xDisplacement,
        getViewport().getViewOriginY() - yDisplacement);
  }

  public Renderer getRenderer()
  {
  	return new GeometryEditPanelRenderer();
  }
  
  class GeometryEditPanelRenderer implements Renderer
  {
  	private Renderer layerListRenderer = null;
  	
    public void render(Graphics2D g)
    {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      
      gridRenderer.paint(g2);
      layerListRenderer = getLayerList().getRenderer(viewport);
      layerListRenderer.render((Graphics2D) g2);
      drawHighlight(g2);
    }
    
  	public void cancel()
  	{
  		if (layerListRenderer != null)
  			layerListRenderer.cancel();
  	}

  }
}


