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
package org.locationtech.jtstest.testbuilder;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jtstest.testbuilder.model.DisplayParameters;
import org.locationtech.jtstest.testbuilder.model.GeometryEditModel;
import org.locationtech.jtstest.testbuilder.model.GeometryStretcherView;
import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.model.LayerList;
import org.locationtech.jtstest.testbuilder.model.StaticGeometryContainer;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.GeometryLocationsWriter;
import org.locationtech.jtstest.testbuilder.ui.Viewport;
import org.locationtech.jtstest.testbuilder.ui.render.DrawingGrid;
import org.locationtech.jtstest.testbuilder.ui.render.GeometryPainter;
import org.locationtech.jtstest.testbuilder.ui.render.GridElement;
import org.locationtech.jtstest.testbuilder.ui.render.LayerRenderer;
import org.locationtech.jtstest.testbuilder.ui.render.LegendElement;
import org.locationtech.jtstest.testbuilder.ui.render.RenderManager;
import org.locationtech.jtstest.testbuilder.ui.render.Renderer;
import org.locationtech.jtstest.testbuilder.ui.render.TitleElement;
import org.locationtech.jtstest.testbuilder.ui.render.ViewStyle;
import org.locationtech.jtstest.testbuilder.ui.style.AWTUtil;
import org.locationtech.jtstest.testbuilder.ui.tools.Tool;


/**
 * Panel which displays rendered geometries.
 * 
 * Zoom methods take arguments in model space.
 * 
 * @version 1.7
 */
public class GeometryEditPanel extends JPanel 
{	
  private TestBuilderModel tbModel;
  
  private DrawingGrid grid = new DrawingGrid();
  private GridElement gridElement;
  private LegendElement legendElement;
  private TitleElement titleElement;

  boolean stateAddingPoints = false;

  Coordinate markPoint;
  Point2D lastPt = new Point2D.Double();

  private Tool currentTool = null;  //PolygonTool.getInstance();

  private Viewport viewport = new Viewport(this);

  private RenderManager renderMgr;
  //private OperationMonitorManager opMonitor;
  
  //----------------------------------------
  BorderLayout borderLayout1 = new BorderLayout();
  
  GeometryPopupMenu menu = new GeometryPopupMenu();

  private ViewStyle viewStyle;

  public GeometryEditPanel() {
    viewStyle = new ViewStyle();
    gridElement = new GridElement(viewport, grid);
    legendElement = new LegendElement(viewport);
    titleElement = new TitleElement(viewport);
    
    try {
      initUI();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    renderMgr = new RenderManager(this);
    //opMonitor = new OperationMonitorManager(this, viewport);
  }

  void initUI() throws Exception {
    this.addComponentListener(new java.awt.event.ComponentAdapter() {

      public void componentResized(ComponentEvent e) {
        this_componentResized(e);
      }
    });
    this.setBackground(viewStyle.getBackground());
    this.setBorder(BorderFactory.createLoweredBevelBorder());
    this.setLayout(borderLayout1);
    
    setToolTipText("");
    setBorder(BorderFactory.createEmptyBorder());
    
    // deactivate for now, since it interferes with right-click zoom-out
    //addMouseListener(new PopupClickListener());
  }

  class PopupClickListener extends MouseAdapter
  {
    public void mousePressed(MouseEvent e)
    {
      if (e.isPopupTrigger())
        doPopUp(e);
    }
    public void mouseReleased(MouseEvent e)
    {
      if (e.isPopupTrigger())
        doPopUp(e);
    }
    private void doPopUp(MouseEvent e)
    {
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
  }


  public void setModel(TestBuilderModel model) {
    this.tbModel = model;
  }
  
  public TestBuilderModel getModel() {
    return tbModel;
  }
  public GeometryEditModel getGeomModel()
  {
    return tbModel.getGeometryEditModel();
  }

  public ViewStyle getViewStyle() {
    return viewStyle;
  }
  
  public void setViewStyle(ViewStyle viewStyle) {
    this.viewStyle = viewStyle;
  }
  
  public Color getBackgroundColor() {
    return viewStyle.getBackground();
  }
  
  public Viewport getViewport() { return viewport; }

  public void updateView()
  {
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
    return tbModel.getLayers();
  }
  
  public void setShowingGrid(boolean isEnabled)
  {
    viewStyle.setGridEnabled(isEnabled);
    forceRepaint();
  }
  
  public void setShowingInput(boolean isEnabled)
  {
    if (tbModel == null) return;
    getLayerList().getLayer(LayerList.LYR_A).setEnabled(isEnabled);
    getLayerList().getLayer(LayerList.LYR_B).setEnabled(isEnabled);
    forceRepaint();
  }
  
  public void setShowingGeometryA(boolean isEnabled) {
    if (tbModel == null) return;
    getLayerList().getLayer(LayerList.LYR_A).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setShowingGeometryB(boolean isEnabled) {
    if (tbModel == null) return;
    getLayerList().getLayer(LayerList.LYR_B).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setShowingResult(boolean isEnabled) 
  {
    if (tbModel == null) return;
    getLayerList().getLayer(LayerList.LYR_RESULT).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setHighlightPoint(Coordinate pt) {
    markPoint = pt;
  }

  public boolean isAddingPoints() {
    return stateAddingPoints;
  }

  public void updateGeom()
  {
  	renderMgr.setDirty(true);
    getGeomModel().geomChanged();
  }
  
  public String getToolTipText(MouseEvent event) {
//    if (event.getPoint().x < 100) return null;
    Coordinate pt = viewport.toModelCoordinate(event.getPoint());
    double toleranceInModel = AppConstants.TOLERANCE_PIXELS / getViewport().getScale();
    // avoid weird scale issues
    if (toleranceInModel <= 0.0) return null;
    return GeometryLocationsWriter.writeLocation(getLayerList(), pt, toleranceInModel);
//    return viewport.toModel(event.getPoint()).toString();
//    return null;
  }

  public double getToleranceInModel()
  {
    return AppConstants.TOLERANCE_PIXELS / getViewport().getScale();
  }
  
  public String getInfo(Coordinate pt)
  {
    double toleranceInModel = AppConstants.TOLERANCE_PIXELS / getViewport().getScale();
    GeometryLocationsWriter writer = new GeometryLocationsWriter();
    writer.setHtml(false);
    return writer.writeLocationString(getLayerList(), pt, toleranceInModel);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    renderMgr.render();
    renderMgr.copyImage(g);
  }
  
  private void drawBorder(Graphics2D g, Color clr) {    
    Stroke strokeBox = new BasicStroke(1, // Width of stroke
        BasicStroke.CAP_BUTT,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        null, // Dash pattern
        0);                   // Dash phase 
    g.setStroke(strokeBox);
    g.setPaint(clr);
    
    int height = (int) viewport.getHeightInView();
    int width = (int) viewport.getWidthInView();
    g.drawRect(0,0,width - 1, height - 1);
  }
  
  private static int VERTEX_SIZE = AppConstants.VERTEX_SIZE + 1;
  private static double VERTEX_SIZE_OVER_2 = VERTEX_SIZE / 2;
  
  private static int INNER_SIZE = VERTEX_SIZE  - 2;
  private static double INNER_SIZE_OVER_2 = INNER_SIZE / 2;
  
  private void drawHighlightedVertices(Graphics2D g, List coords, Color clr) {
    Rectangle2D rect = new Rectangle2D.Double();
    for (int i = 0; i < coords.size(); i++) {
      Coordinate pt = (Coordinate) coords.get(i);
      Point2D p = viewport.toView(pt);
      rect.setFrame(
          p.getX() - VERTEX_SIZE_OVER_2,
          p.getY() - VERTEX_SIZE_OVER_2, 
          VERTEX_SIZE, 
          VERTEX_SIZE);
      g.setColor(clr);
      g.fill(rect);
      Rectangle2D rectInner = new Rectangle2D.Double(
          p.getX() - INNER_SIZE_OVER_2,
          p.getY() - INNER_SIZE_OVER_2, 
          INNER_SIZE, 
          INNER_SIZE);
      g.setColor(AppConstants.VERTEX_HIGHLIGHT_CLR);
      g.fill(rectInner);

    }
  }
  
  private void drawHighlightedVertex(Graphics2D g, Coordinate pt, Color clr) {
    Rectangle2D rect = new Rectangle2D.Double();
    Point2D p = viewport.toView(pt);
    rect.setFrame(
        p.getX() - VERTEX_SIZE_OVER_2,
        p.getY() - VERTEX_SIZE_OVER_2, 
        VERTEX_SIZE, 
        VERTEX_SIZE);
    g.setColor(clr);
    g.fill(rect);
    Rectangle2D rectInner = new Rectangle2D.Double(
        p.getX() - INNER_SIZE_OVER_2,
        p.getY() - INNER_SIZE_OVER_2, 
        INNER_SIZE, 
        INNER_SIZE);
    g.setColor(AppConstants.VERTEX_HIGHLIGHT_CLR);
    g.fill(rectInner);
  }
  
  private static double VERTEX_SHADOW_SIZE_OVER_2 = AppConstants.VERTEX_SHADOW_SIZE / 2;

  private void drawVertexShadow(Graphics2D g, Coordinate pt, Color clr) {
    Ellipse2D rect = new Ellipse2D.Double();
    Point2D p = viewport.toView(pt);
    rect.setFrame(
        p.getX() - VERTEX_SHADOW_SIZE_OVER_2,
        p.getY() - VERTEX_SHADOW_SIZE_OVER_2, 
        AppConstants.VERTEX_SHADOW_SIZE, 
        AppConstants.VERTEX_SHADOW_SIZE);
    g.setColor(clr);
    g.fill(rect);
  }
  
  private void drawMark(Graphics2D g) {
    if (markPoint == null)
      return;
    
    String markLabel = markPoint.x + ",  " + markPoint.y;
    int strWidth = g.getFontMetrics().stringWidth(markLabel);

    double markSize = AppConstants.HIGHLIGHT_SIZE;
    Point2D highlightPointView = viewport.toView(markPoint);
    double markX = highlightPointView.getX();
    double markY = highlightPointView.getY();
    Ellipse2D.Double shape = new Ellipse2D.Double(
        markX - markSize / 2, 
        markY - markSize / 2,
        markSize, markSize);
    AWTUtil.setStroke(g, 4);
    g.setColor(AppConstants.HIGHLIGHT_CLR);
    g.draw(shape);
    
    // draw label box
    Envelope viewEnv = viewport.getViewEnv();
    
    int bottomOffset = 10;
    int boxHgt = 20;
    int boxPadX = 20;
    int boxWidth = strWidth + 2 * boxPadX;
    int arrowWidth = 10;
    int arrowOffset = 2;
    int labelOffsetY = 5;
    
    int bottom = (int) viewEnv.getMaxY() - bottomOffset;
    int centreX = (int) (viewEnv.getMinX() + viewEnv.getMaxX()) / 2;
    
    int boxMinX = centreX - boxWidth/2;
    int boxMaxX = centreX + boxWidth/2;
    int boxMinY = bottom - boxHgt;
    int boxMaxY = bottom;
    
    int[] xpts = new int[] { 
        boxMinX, centreX - arrowWidth/2, (int) markX, centreX + arrowWidth/2,
        boxMaxX, boxMaxX,   boxMinX };
    int[] ypts = new int[] {  
        boxMinY, boxMinY, (int) (markY + arrowOffset), boxMinY,
        boxMinY, boxMaxY, boxMaxY };
    
    Polygon poly = new Polygon(xpts, ypts, xpts.length);
    
    g.setColor(AppConstants.HIGHLIGHT_FILL_CLR);
    g.fill(poly);
    AWTUtil.setStroke(g, 1);
    g.setColor(ColorUtil.opaque(AppConstants.HIGHLIGHT_CLR));
    g.draw(poly);

    // draw mark point label
    g.setColor(Color.BLACK);
    g.drawString(markLabel, centreX - strWidth/2, boxMaxY - labelOffsetY);

  }

  /**
   * Draws a mask surround to indicate that geometry is being visually altered
   * @param g
   */
  private void drawRevealMask(Graphics2D g) {
    double viewWidth = viewport.getWidthInView();
    double viewHeight = viewport.getHeightInView();
    
    float minExtent = (float) Math.min(viewWidth, viewHeight);
    float maskWidth = (float) (minExtent * AppConstants.MASK_WIDTH_FRAC / 2);
    
    Area mask = new Area(new Rectangle2D.Float(
    		(float) 0, (float) 0, 
    		(float) viewWidth, (float) viewHeight));
    
    Area maskHole = new Area(new Rectangle2D.Float(
    		(float) maskWidth, 
    		(float) maskWidth, 
    		((float) viewWidth) - 2 * maskWidth, 
    		((float) viewHeight) - 2 * maskWidth));
    
    mask.subtract(maskHole);
    g.setColor(AppConstants.MASK_CLR);
    g.fill(mask);
  }

  public void draw(Geometry geom, Color lineClr, Color fillClr) {
    Graphics2D gr = (Graphics2D) getGraphics();
    GeometryPainter.paint(geom, getViewport(), gr, lineClr, fillClr);
  }
  
  public void flash(Geometry g)
  {
    Graphics2D gr = (Graphics2D) getGraphics();
    gr.setXORMode(viewStyle.getBackground());
    Stroke stroke = new BasicStroke(5);
    
    Geometry flashGeom = g;
    double vSize = viewSize(g);
    if (vSize <= 2 || g instanceof org.locationtech.jts.geom.Point)
      flashGeom = flashPointGeom(g);
    
    try {
      GeometryPainter.paint(flashGeom, viewport, gr, Color.RED, null, stroke);
      Thread.sleep(200);
      GeometryPainter.paint(flashGeom, viewport, gr, Color.RED, null, stroke);
    }
    catch (Exception ex) { 
      // nothing we can do
    }
    gr.setPaintMode();
  }
  
  private double viewSize(Geometry geom) {
    Envelope env = geom.getEnvelopeInternal();
    return viewport.toView(env.getDiameter());
  }
  
  private Geometry flashPointGeom(Geometry g)
  {
    double ptRadius = viewport.toModel(4);
    return g.buffer(ptRadius);
  }
  
  
  public Point2D snapToGrid(Point2D modelPoint) {
    return grid.snapToGrid(modelPoint);
  }

  void this_componentResized(ComponentEvent e) {
  	renderMgr.componentResized();
    viewport.update(this.getSize());
  }

  /**
   * 
   * @param newTool tool to set, or null to clear tool
   */
  public void setCurrentTool(Tool newTool) {
    if (currentTool != null) currentTool.deactivate();
    currentTool = newTool;
    if (currentTool != null) {
      currentTool.activate(this);
    }
    else {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  public void zoomToGeometry(int i) {
    Geometry g = getGeomModel().getGeometry(i);
    if (g == null) return;
    zoom(g.getEnvelopeInternal());
  }

  public void zoomToInput() {
    zoom(getGeomModel().getEnvelope());
  }

  public void zoomToResult() {
    zoom(getGeomModel().getEnvelopeResult());
  }

  public void zoomToFullExtent() {
    zoom(getGeomModel().getEnvelopeAll());
  }
  
  public void zoom(Geometry geom) 
  {
    if (geom == null) return;
    zoom(geom.getEnvelopeInternal());
  }
  
  public void zoom(Point2D zoomBox1, Point2D zoomBox2) 
  {
    Envelope zoomEnv = new Envelope();
    zoomEnv.expandToInclude(zoomBox1.getX(), zoomBox1.getY());
    zoomEnv.expandToInclude(zoomBox2.getX(), zoomBox2.getY());
    zoom(zoomEnv);
  }
  
  public void zoom(Envelope zoomEnv) {
    if (zoomEnv == null)
      return;

    if (zoomEnv.isNull()) {
      viewport.zoomToInitialExtent();
      return;
    }
    double averageExtent = (zoomEnv.getWidth() + zoomEnv.getHeight()) / 2d;
    // fix to allow zooming to points
    if (averageExtent == 0.0)
      averageExtent = 1.0;
    double buffer = averageExtent * 0.1;
    zoomEnv.expandBy(buffer);
    viewport.zoom(zoomEnv);
  }

  /**
   * Zoom to a point, ensuring that the zoom point remains in the same screen location.
   * 
   * @param zoomPt
   * @param zoomFactor
   */
  public void zoom(Point2D zoomPt, double zoomFactor) {
    double zoomScale = getViewport().getScale() * zoomFactor;
    viewport.zoom(zoomPt, zoomScale);
  }
  
  public void zoomPan(double dx, double dy) {
    getViewport().zoomPan(dx, dy);
  }

  public String cursorLocationString(Point2D pView)
  {
    Point2D p = getViewport().toModel(pView);
    NumberFormat format = getViewport().getScaleFormat();
    return format.format(p.getX()) 
    + ", " 
    + format.format(p.getY());
  }

  public Renderer getRenderer()
  {
    return new GeometryEditPanelRenderer();
  }
  
  class GeometryEditPanelRenderer implements Renderer
  {
    private static final double LAYER_SHIFT_GUTTER_FACTOR = 0.3;
    private GeometryStretcherView stretchView = null;
  	private Renderer currentRenderer = null;
    private boolean isRevealingTopology = false; 
    private boolean isRenderingStretchVertices = false;
    private double layerShiftX; 
    
  	public GeometryEditPanelRenderer()
  	{
      if (DisplayParameters.isRevealingTopology()) {
        stretchView = new GeometryStretcherView(getGeomModel());
        stretchView.setStretchSize(viewport.toModel(DisplayParameters.getTopologyStretchSize()));
        stretchView.setNearnessTolerance(viewport.toModel(GeometryStretcherView.NEARNESS_TOL_IN_VIEW));
        stretchView.setEnvelope(viewport.getModelEnv());
        isRevealingTopology = DisplayParameters.isRevealingTopology();
        isRenderingStretchVertices = stretchView.isViewPerformant();
      }  		
  	}
  	
    public void render(Graphics2D g2)
    {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      
      if (isRevealingTopology) {
        if (isRenderingStretchVertices) {
          //renderMagnifiedVertexShadows(g2);
          renderMagnifiedVertexMask(g2);
        }
        else {
          // render indicator that shows stretched view is non-performant
          renderRevealTopoWarning(g2);
        }
      }
      
      if (viewStyle.isGridEnabled()) {
        gridElement.paint(g2);
      }
      if (viewStyle.isBorderEnabled()) {
        drawBorder(g2, viewStyle.getBorderColor());
      }
      layerShiftX = computeLayerShift(tbModel.getLayersAll());
      
      renderLayersTheme(tbModel.getLayersBase(), g2);
      renderLayersCore(getLayerList(), g2);
      renderLayersTheme(tbModel.getLayersTop(), g2);
      renderLayersTheme(tbModel.getLayersFloating(), g2);
      
      if (isRevealingTopology && isRenderingStretchVertices) {
      	renderMagnifiedVertices(g2);
      }
      
      if (viewStyle.isGridEnabled()) {
        gridElement.paintTop(g2);
      }
      
      drawMark(g2);
      
      if (viewStyle.isLegendEnabled()) {
        legendElement.setBorderEnabled(viewStyle.isLegendBorderEnabled());
        legendElement.setStatsEnabled(viewStyle.isLegendStatsEnabled());
        legendElement.setMetricsEnabled(viewStyle.isLegendMetricsEnabled());
        legendElement.setBorderColor(viewStyle.getBorderColor());
        legendElement.setFill(viewStyle.getLegendFill());
        legendElement.paint(tbModel.getLayersLegend(), g2);
      }
      if (viewStyle.isTitleEnabled()) {
        titleElement.setBorderColor(viewStyle.getBorderColor());
        titleElement.setBorderEnabled(viewStyle.isTitleBorderEnabled());
        titleElement.setFill(viewStyle.getTitleFill());
        titleElement.setTitle(viewStyle.getTitle());
        titleElement.paint(g2);
      }
    }
    
    private double computeLayerShift(LayerList lyrList) {
      Envelope envBase = computeLayersEnv(lyrList, false);
      Envelope envShifted = computeLayersEnv(lyrList, true);
      if (envShifted.isNull()) 
        return 0;
      double offsetX = envBase.getMaxX() - envShifted.getMinX();
      return (1 + LAYER_SHIFT_GUTTER_FACTOR) * offsetX;
    }
    
    private Envelope computeLayersEnv(LayerList lyrList, boolean isShifted) {
      Envelope env = new Envelope();
      int n = lyrList.size();
      for (int i = 0; i < n; i++) {
        Layer layer = lyrList.getLayer(i);
        if (isShifted == layer.getLayerStyle().isShifted()) {
          env.expandToInclude(layer.getEnvelope());
        }
      }
      return env;
    }
    
    private void renderLayersCore(LayerList layerList, Graphics2D g)
    {
      int n = layerList.size();
      for (int i = 0; i < n; i++) {
        Layer layer = layerList.getLayer(i);
        currentRenderer = createRendererCore(layer, i);
        currentRenderer.render(g);
      }
      currentRenderer = null;
    }

    private void renderLayersTheme(LayerList layerList, Graphics2D g)
    {
      int n = layerList.size();
      for (int i = n - 1; i >= 0; i--) {
        Layer layer = layerList.getLayer(i);
        currentRenderer = createRenderer(layer);
        currentRenderer.render(g);
      }
      currentRenderer = null;
    }

    private Renderer createRendererCore(Layer layer, int i) {
      if (isRevealingTopology && isRenderingStretchVertices
          && stretchView != null && i < 2) {
        //System.out.println("rendering stretch verts");
        return new LayerRenderer(layer,
            new StaticGeometryContainer(stretchView.getStretchedGeometry(i)),
            viewport);
      }
      return createRenderer(layer);
    }

    private Geometry offsetGeometry(Geometry geom, double offsetX) {
      if (geom == null) return null;
      AffineTransformation trans = AffineTransformation.translationInstance(offsetX, 0);
      return trans.transform(geom); 
    }

    private Renderer createRenderer(Layer layer) {
      if (layerShiftX > 0 
          && layer.getLayerStyle().isShifted()) {
        return new LayerRenderer(layer,
            new StaticGeometryContainer(offsetGeometry(layer.getGeometry(), layerShiftX)),
            viewport);
      }
      return new LayerRenderer(layer, viewport);
    }
    
    public void renderMagnifiedVertices(Graphics2D g)
    {
      LayerList layerList = getLayerList();
      for (int i = 0; i < 2; i++) {
        // respect layer visibility
        if (! layerList.getLayer(i).isEnabled()) continue;
        
        List stretchedVerts = stretchView.getStretchedVertices(i);
        if (stretchedVerts == null) continue;
        for (int j = 0; j < stretchedVerts.size(); j++) {
          Coordinate p = (Coordinate) stretchedVerts.get(j);
          drawHighlightedVertex(g, p, 
            i == 0 ? AppColors.GEOM_A_HIGHLIGHT_CLR :
              AppColors.GEOM_B_HIGHLIGHT_CLR);
        } 
      }
    }
    
    public void renderMagnifiedVertexShadows(Graphics2D g)
    {
      if (stretchView == null) return;
      for (int i = 0; i < 2; i++) {
        List stretchedVerts = stretchView.getStretchedVertices(i);
        if (stretchedVerts == null) continue;
        for (int j = 0; j < stretchedVerts.size(); j++) {
          Coordinate p = (Coordinate) stretchedVerts.get(j);
          drawVertexShadow(g, p, AppConstants.VERTEX_SHADOW_CLR);
        }
      }
    }
    
    public void renderMagnifiedVertexMask(Graphics2D g)
    {
      if (stretchView == null) return;
      
      // render lowlight background
      Rectangle2D rect = new Rectangle2D.Float();
      rect.setFrame(
          0,
          0, 
          viewport.getWidthInView(), 
          viewport.getHeightInView());
      g.setColor(AppConstants.MASK_CLR);
      g.fill(rect);
  
      // highlight mag vertices
      for (int i = 0; i < 2; i++) {
        List stretchedVerts = stretchView.getStretchedVertices(i);
        if (stretchedVerts == null) continue;
        for (int j = 0; j < stretchedVerts.size(); j++) {
          Coordinate p = (Coordinate) stretchedVerts.get(j);
          drawVertexShadow(g, p, Color.WHITE);
        }
      }
    }
    
    public void renderRevealTopoWarning(Graphics2D g)
    {
      if (stretchView == null) return;

      float maxx = (float) viewport.getWidthInView();
      float maxy = (float) viewport.getHeightInView();
      GeneralPath path = new GeneralPath();
      path.moveTo(0, 0);
      path.lineTo(maxx, maxy);
      path.moveTo(0, maxy);
      path.lineTo(maxx, 0);
      // render lowlight background
      g.setColor(AppConstants.MASK_CLR);
      g.setStroke(new BasicStroke(30));
      g.draw(path);
  
    }
    
  	public synchronized void cancel()
  	{
  		if (currentRenderer != null)
  			currentRenderer.cancel();
  	}

  }






}


