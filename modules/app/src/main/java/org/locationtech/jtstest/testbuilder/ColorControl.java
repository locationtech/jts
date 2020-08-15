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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class ColorControl {
  public interface ColorListener {
    void colorChanged(Color clr);
  }
  
  public static JPanel create(Component comp, String title, Color initColor, ColorListener colorListener) {
    JPanel ctl = new JPanel();
    ctl.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    
    Dimension dim = new Dimension(20, 20);
    ctl.setMinimumSize(dim);
    ctl.setPreferredSize(dim);
    ctl.setMaximumSize(dim);
    ctl.setOpaque(true);
    ctl.setToolTipText(title);
    ctl.setBackground(initColor);
    ctl.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        Color clr = showColorChooser(comp, title, getColor(ctl));
        if (clr != null) {
          ctl.setBackground(clr);
          colorListener.colorChanged(clr);
        }
      }
    });    
    return ctl;
  }
  
  private static Color showColorChooser(Component comp, String title, Color initColor) {
    return JColorChooser.showDialog(comp, title, initColor);
  }

  public static void update(JPanel ctl, Color clr) {
    ctl.setBackground(clr);
  }
  
  public static Color getColor(JPanel clrCtl) {
    return clrCtl.getBackground();
  }
}
