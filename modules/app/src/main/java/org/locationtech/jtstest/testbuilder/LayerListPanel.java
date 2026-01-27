/*
 * Copyright (c) 2016 Martin Davis.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.model.LayerList;
import org.locationtech.jtstest.testbuilder.model.StaticGeometryContainer;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;

/**
 * @version 1.7
 */
public class LayerListPanel extends JPanel {
  
  private static final int TAB_INDEX_LAYER = 0;

  private static final String LBL_LAYER = "Style";
  private static final String LBL_VIEW = "View";
  
  JPanel listPanel = new JPanel();
  Box buttonPanel = Box.createVerticalBox();
  JTabbedPane tabPane = new JTabbedPane();
  private LayerStylePanel lyrStylePanel;
  List<LayerItemPanel> layerItems = new ArrayList<LayerItemPanel>();

  private JButton btnCopy;
  private JButton btnInspect;
  private JButton btnUp;
  private JButton btnDown;
  private JButton btnDelete;
  private JButton btnPaste;
  private JButton btnZoom;

  private Layer focusLayer;

  public LayerListPanel() {
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void uiInit() throws Exception {
    //setSize(300, 250);
    setBackground(AppColors.BACKGROUND);
    setLayout(new BorderLayout());
    
    JPanel panelLeft = new JPanel();
    panelLeft.setLayout(new BorderLayout());

    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
    listPanel.setBackground(AppColors.BACKGROUND);
    listPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,0));

    JScrollPane scrollPane1 = new JScrollPane();
    scrollPane1.setBackground(AppColors.BACKGROUND);
    scrollPane1.setOpaque(true);
    scrollPane1.getViewport().add(listPanel, null);
    scrollPane1.setPreferredSize(new Dimension(150, 250));
    scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    panelLeft.add(scrollPane1, BorderLayout.CENTER);
    panelLeft.add(buttonPanel, BorderLayout.EAST);

    btnCopy = SwingUtil.createButton(AppIcons.ADD, 
        "Copy layer to a new layer",
            new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            layerCopy();
          }
        });
    buttonPanel.add(btnCopy);
    
    btnZoom = SwingUtil.createButton(AppIcons.ZOOM, 
        "Zoom to layer",
            new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            layerZoom();
          }
        });
    buttonPanel.add(btnZoom);
    
    btnInspect = SwingUtil.createButton(AppIcons.GEOM_INSPECT, 
        "Inspect layer geometry",
            new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            layerInspect();
          }
        });
    buttonPanel.add(btnInspect);
    
    btnPaste = SwingUtil.createButton(AppIcons.PASTE, 
        "Paste geometry into layer",
            new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            layerPaste(focusLayer);
          }
        });
    buttonPanel.add(btnPaste);
    btnUp = SwingUtil.createButton(AppIcons.UP, 
        "Move layer up",
            new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            layerUp(focusLayer);
          }
        });
    buttonPanel.add(btnUp);
    btnDown = SwingUtil.createButton(AppIcons.DOWN, 
        "Move layer down",
            new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            layerDown(focusLayer);
          }
        });
    buttonPanel.add(btnDown);
    
    btnDelete = SwingUtil.createButton(AppIcons.DELETE, 
        AppStrings.TIP_LAYER_CLEAR,
            new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (SwingUtil.isCtlKeyPressed(e)) {
              layerDelete(focusLayer);
            }
            else {
              layerClear(focusLayer);
            }
         }
        });
    buttonPanel.add(btnDelete);
    
    add(panelLeft, BorderLayout.WEST);
    
    lyrStylePanel = new LayerStylePanel();
    GeometryViewStylePanel viewStylePanel = new GeometryViewStylePanel();
    
    //add(lyrStylePanel, BorderLayout.CENTER);    

    //tabFunctions.setBackground(jTabbedPane1.getBackground());
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getViewport().add(lyrStylePanel, null);
    tabPane.add(scrollPane,  LBL_LAYER);
    tabPane.add(viewStylePanel,   LBL_VIEW);
    add(tabPane, BorderLayout.CENTER);
  }

  protected LayerItemPanel findLayerItem(Layer lyr) {
    for (LayerItemPanel lip : layerItems) {
      if (lip.getLayer() == lyr) return lip;
    }
    return null;
  }

  public void showTabLayerStyle(String title) {
    tabPane.setSelectedIndex(TAB_INDEX_LAYER);
    //tabPane.setTitleAt(0, LBL_LAYER + " - " + title);
    //SwingUtil.showTab(tabPane, LBL_LAYER_STYLE);
  }
  
  public void populateList() {
    listPanel.removeAll();
    layerItems.clear();
    
    addLayers(JTSTestBuilder.model().getLayersTop());
    addLayers(JTSTestBuilder.model().getLayers());
    addLayers(JTSTestBuilder.model().getLayersBase());
    addLayers(JTSTestBuilder.model().getLayersFloating());
    setLayerFocus(layerItems.get(0));
  }

  public void updateList() {
    for (LayerItemPanel layerItemPanel : layerItems) {
      layerItemPanel.update();
    }
  }

  private void addLayers(LayerList lyrList) {
    for (int i = 0; i < lyrList.size(); i++) {
      Layer lyr = lyrList.getLayer(i);
      LayerItemPanel item = new LayerItemPanel(lyr, this);
      listPanel.add(item);
      layerItems.add(item);
    }
  }
  
  public void setLayerFocus(LayerItemPanel layerItem) {
    for (LayerItemPanel item : layerItems) {
      item.setFocusLayer(false);
    }
    layerItem.setFocusLayer(true);
    Layer layer = layerItem.getLayer();
    showTabLayerStyle(layer.getName());
    lyrStylePanel.setLayer(layer, layer.isModifiable());
    focusLayer = layer;
    updateButtons(focusLayer);
  }

  private void updateButtons(Layer lyr) {
    boolean isModifiable = lyr.isModifiable();

    // every layer is copyable
    btnCopy.setEnabled(true);
    btnPaste.setEnabled(isModifiable && ! lyr.hasGeometry());
    btnZoom.setEnabled(lyr.hasGeometry());
    btnUp.setEnabled(isModifiable);
    btnDown.setEnabled(isModifiable);
    btnDelete.setEnabled(isModifiable);
  }

  private void layerCopy() {
    Layer copy = JTSTestBuilder.model().layerCopy(focusLayer);
    populateList();
    setLayerFocus(findLayerItem(copy));
    JTSTestBuilder.controller().geometryViewChanged();
  }

  private void layerInspect() {
    JTSTestBuilder.controller().inspectGeometry(focusLayer.getName(), focusLayer.getGeometry());
  }
  
  private void layerZoom() {
    JTSTestBuilder.controller().zoomToGeometry(focusLayer.getGeometry());
  }
  
  private void layerDelete(Layer lyr) {
    // don't remove if non-empty
    if (lyr.hasGeometry()) return;
    
    JTSTestBuilder.model().layerDelete(lyr);
    populateList();
    JTSTestBuilder.controller().geometryViewChanged();
  }
  
  private void layerUp(Layer lyr) {
    JTSTestBuilder.model().layerUp(lyr);
    populateList();
    setLayerFocus(findLayerItem(lyr));
    JTSTestBuilder.controller().geometryViewChanged();
  }
  
  private void layerDown(Layer lyr) {
    JTSTestBuilder.model().layerDown(lyr);
    populateList();
    setLayerFocus(findLayerItem(lyr));
    JTSTestBuilder.controller().geometryViewChanged();
  }
  
  private void layerClear(Layer lyr) {
    lyr.getSource().clear();
    updateButtons(focusLayer);
    JTSTestBuilder.controller().geometryViewChanged();
  }
  
  protected void layerPaste(Layer lyr) {
    try {
      // don't paste into non-empty layers to avoid losing data
      if (lyr.hasGeometry()) return;
      
      Geometry geom = JTSTestBuilder.model().readGeometryFromClipboard();
      // this will error if layer is not modifiable
      StaticGeometryContainer src = (StaticGeometryContainer) lyr.getSource();
      src.setGeometry(geom);
      updateButtons(focusLayer);
      JTSTestBuilder.controller().geometryViewChanged();
    } catch (Exception e) {
      SwingUtil.reportException(this, e);
    }
  }
}

class LayerItemPanel extends JPanel {
  private static Font FONT_FOCUS = new java.awt.Font("Dialog", Font.BOLD, 12);
  private static Font FONT_NORMAL = new java.awt.Font("Dialog", Font.PLAIN, 12);
  
  private Border BORDER_CONTROL = BorderFactory.createLineBorder(CLR_CONTROL);
  private Border BORDER_HIGHLIGHT = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
  
  private static final Color CLR_CONTROL = AppColors.BACKGROUND;
  private static final Color CLR_HIGHLIGHT = ColorUtil.darker(CLR_CONTROL, .95);
  
  private Layer layer;
  private JCheckBox checkbox;
  private LayerListPanel lyrListPanel;
  private LayerItemPanel self;
  private JPanel namePanel;
  private boolean hasFocus;
  private JLabel lblName;
  private JPanel swatch;

  LayerItemPanel(Layer lyr, LayerListPanel lyrListPanel) {
    this.layer = lyr;
    this.lyrListPanel = lyrListPanel;
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    self = this;
  }

  public String getToolTipText(MouseEvent e) {
    return layer.getNameInfo();
  }
  
  public Layer getLayer() {
    return layer;
  }

  public void setFocusLayer(boolean hasFocus) {
    setBackground(hasFocus ? AppColors.TAB_FOCUS : AppColors.BACKGROUND);
    setBorder(hasFocus ? BORDER_HIGHLIGHT : BORDER_CONTROL );
    lblName.setFont(hasFocus ? FONT_FOCUS : FONT_NORMAL);
    revalidate();
    this.hasFocus = hasFocus;
  }
  
  public boolean isFocusLayer() {
    return hasFocus;
  }
  
  public void update() {
    LayerStyleSwatchControl.update(swatch, layer);
    lblName.setText( layer.getName());
    lblName.setForeground(layer.hasGeometry() ? Color.BLACK : Color.GRAY);
    checkbox.setSelected(layer.isEnabled());
  }
  
  private void uiInit() throws Exception {
    setSize(250, 250);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setBackground(AppColors.BACKGROUND);
    //setOpaque(true);
    setAlignmentX(Component.LEFT_ALIGNMENT);
    setBorder(BORDER_CONTROL);
    
    checkbox = new JCheckBox();
    add(checkbox);
    checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
    checkbox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        layerVisAction();
      }
    });
    checkbox.setSelected(layer.isEnabled());
    checkbox.setOpaque(false);

    
    lblName = new LayerName(layer);
    lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
    lblName.setMinimumSize(new Dimension(100,12));
    lblName.setPreferredSize(new Dimension(100,12));
    lblName.setMaximumSize(new Dimension(100,12));
    lblName.setFont(FONT_NORMAL);
    lblName.addMouseListener(new HighlightMouseListener(this));
    lblName.addMouseListener(new MouseAdapter()  
    {  
      public void mouseClicked(MouseEvent e)  
      {  
        lyrListPanel.setLayerFocus(self);
      }
    }); 
    
    swatch = LayerStyleSwatchControl.create(layer);
    add(swatch);
    add(Box.createRigidArea(new Dimension(4,0)));
    
    namePanel = new JPanel();
    add(namePanel);
    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
    //namePanel.setBackground(CLR_CONTROL);
    namePanel.setOpaque(false);
    namePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    namePanel.setMinimumSize(new Dimension(100,14));
    namePanel.setPreferredSize(new Dimension(100,14));
    namePanel.setMaximumSize(new Dimension(100,14));
    //namePanel.setBorder(BORDER_HIGHLIGHT);;
    namePanel.addMouseListener(new HighlightMouseListener(this));
    
    namePanel.add(lblName);
    
    update();
  }

  private void layerVisAction() {
    boolean isVisible = checkbox.isSelected();
    layer.setEnabled(isVisible);
    repaint();
    JTSTestBuilder.controller().geometryViewChanged();
  }
  
  class HighlightMouseListener extends MouseAdapter {
    private LayerItemPanel comp;

    HighlightMouseListener(LayerItemPanel comp) {
      this.comp = comp;
    }
    
    public void mouseEntered(MouseEvent e) {
      if (comp.isFocusLayer()) return;
      comp.setBackground(CLR_HIGHLIGHT);
      //comp.setBorder(BORDER_HIGHLIGHT);
      comp.revalidate();
    }

    public void mouseExited(MouseEvent e) {
      if (comp.isFocusLayer()) return;
      comp.setBackground(AppColors.BACKGROUND);
      //comp.setBorder(BORDER_CONTROL);
      comp.revalidate();
   }
  }

}

class LayerName extends JLabel {
  private Layer layer;

  public LayerName(Layer layer) {
    super(layer.getName());
    this.layer = layer;
    setToolTipText(layer.getName()); 
  }

  public String getToolTipText(MouseEvent e) {
    return layer.getNameSummary();
  }
}

class LayerStyleSwatchControl extends JPanel {

  public static LayerStyleSwatchControl create(Layer layer) {
    LayerStyleSwatchControl ctl = new LayerStyleSwatchControl(layer);
    //update(ctl, layer);  
    return ctl;
  }
  
  private Layer layer;

  public LayerStyleSwatchControl(Layer layer) {
    this.layer = layer;
    Dimension dim = new Dimension(16,16);
    setMinimumSize(dim);
    setPreferredSize(dim);
    setMaximumSize(dim);
    setOpaque(true);
    setToolTipText(layer.getName()); 
  }

  public String getToolTipText(MouseEvent e) {
    return layer.getNameSummary();
  }
  
  public static void update(JPanel ctl, Layer layer) {
    
    Color fillClr = Color.WHITE;
    if (layer.getGeometryStyle().isFilled())
      fillClr = layer.getGeometryStyle().getFillColor();

    int lineWidth = 1;
    if (layer.getGeometryStyle().getStrokeWidth() > 1)
      lineWidth = 2;
    
    /*
    Graphics2D gr = (Graphics2D) ctl.getGraphics();
    gr.setColor(fillClr);
    gr.setPaint(fillClr);
    gr.fillRect(0, 0, 10, 10);
*/
    ctl.setBackground( fillClr );  
    ctl.setBorder(BorderFactory.createLineBorder(layer.getGeometryStyle().getLineColor(), lineWidth));
  }
}

class StyleSwatchPanel extends JPanel {

  private BasicStyle style;

  public StyleSwatchPanel(BasicStyle style) {
    this.style = style;
    
    Dimension dim = new Dimension(16,16);
    setMinimumSize(dim);
    setPreferredSize(dim);
    setMaximumSize(dim);
    setOpaque(true);
    update(style);
  }
  
  public void update(BasicStyle style) {
    
    Color fillClr = style.getFillColor() == null ? style.getFillColor() :Color.WHITE;
    setBackground( fillClr );  

    int lineWidth = 1;
    if (style.getStrokeWidth() > 1)
      lineWidth = 2;

    setBorder(BorderFactory.createLineBorder(style.getLineColor(), lineWidth));
  }
}
