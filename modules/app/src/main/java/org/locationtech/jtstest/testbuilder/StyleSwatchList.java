/*
 * Copyright (c) 2025 Martin Davis.
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;

public class StyleSwatchList extends JList<StyleSwatchList.StyleSwatch> {

  public static StyleSwatchList create(BasicStyle... styles) {
    DefaultListModel<StyleSwatch> listModel = new DefaultListModel<>();
    for (BasicStyle style : styles) {
      listModel.addElement(new StyleSwatch(style));
    }
    StyleSwatchList ssList = new StyleSwatchList(listModel);
    return ssList;
  }

  public StyleSwatchList(ListModel<StyleSwatch> model) {
    super(model); // Initialize JList with the provided model

    // Set our custom renderer
    this.setCellRenderer(new PanelRenderer());

    // Optional: Set selection mode
    this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // this.setPreferredSize(new Dimension(16, 16));
    // this.setMaximumSize(new Dimension(16, 10000));
  }

  public BasicStyle getStyle(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
      int index = locationToIndex(e.getPoint()); 
      if (index == -1)
        return null;
      Rectangle cellBnds = getCellBounds(index, index);
      if (! cellBnds.contains(e.getPoint()))
        return null;
      
      // Get the model from this JList instance
      ListModel<StyleSwatch> currentModel = getModel();
      StyleSwatch clickedSS = currentModel.getElementAt(index);
      return clickedSS.style;
    }
    return null;
  }

  /**
   * Custom JPanel to display a rectangle with border and fill colors. This
   * remains a static nested class.
   */
  protected static class StyleSwatch extends JPanel {
    private BasicStyle style;
    // private String name; // Identifier for the panel

    public StyleSwatch(BasicStyle style) {
      this.style = style;
      this.setPreferredSize(new Dimension(20, 16));
      this.setOpaque(true); // Important for rendering in JList
    }

    /*
     * public String getPanelName() { return name; }
     */

    public BasicStyle getStyle() {
      return style;
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int rectMargin = 3;
      int borderThickness = 2;
      int rectX = rectMargin;
      int rectY = rectMargin;
      int rectWidth = getWidth() - 2 * rectMargin;
      int rectHeight = getHeight() - 2 * rectMargin;

      g2d.setColor(style.getFillColor());
      g2d.fillRect(rectX, rectY, rectWidth, rectHeight);

      g2d.setColor(style.getLineColor());
      g2d.setStroke(new BasicStroke(borderThickness));
      g2d.drawRect(rectX, rectY, rectWidth, rectHeight);

      /*
       * if (name != null && !name.isEmpty()) { FontMetrics fm = g2d.getFontMetrics();
       * g2d.setColor(getContrastColor(fillColor)); int stringWidth =
       * fm.stringWidth(name); int textX = (getWidth() - stringWidth) / 2; int textY =
       * fm.getAscent() + (getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
       * g2d.drawString(name, textX, textY); }
       */
      g2d.dispose();
    }

    /*
     * private Color getContrastColor(Color color) { double luminance = (0.299 *
     * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255.0;
     * return luminance > 0.5 ? Color.BLACK : Color.WHITE; }
     */
  }

  static class PanelRenderer implements ListCellRenderer<StyleSwatch> {
    @Override
    public Component getListCellRendererComponent(JList<? extends StyleSwatch> list, StyleSwatch panel, int index,
        boolean isSelected, boolean cellHasFocus) {
      if (isSelected) {
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(list.getSelectionBackground().darker(), 1),
            BorderFactory.createEmptyBorder(1, 1, 1, 1)));
      } else {
        panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      }
      return panel;
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      // Create a JFrame to host the JList
      JFrame frame = new JFrame("JList Custom Panels Demo");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setLayout(new BorderLayout());

      StyleSwatchList ssList = StyleSwatchList.create(new BasicStyle(Color.BLACK, new Color(255, 100, 100)),
          new BasicStyle(Color.DARK_GRAY, new Color(100, 255, 100)),
          new BasicStyle(Color.BLUE, new Color(100, 100, 255)));

      // Add a MouseListener to this JList
      ssList.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          BasicStyle s = ssList.getStyle(e);
          String message = "\nStyle: " + s;

          // Get the parent window for the dialog
          Window parentWindow = SwingUtilities.getWindowAncestor(ssList);

          JOptionPane.showMessageDialog(parentWindow, // Parent component for the dialog
              message, "Panel Clicked", JOptionPane.INFORMATION_MESSAGE);
        }
      });
      // Add the JList (within a JScrollPane for scrollability) to the frame
      frame.add(new JScrollPane(ssList), BorderLayout.CENTER);

      // Size the frame and make it visible
      frame.pack();
      frame.setMinimumSize(new Dimension(16, 16));
      frame.setLocationRelativeTo(null); // Center on screen
      frame.setVisible(true);
    });
  }
}
