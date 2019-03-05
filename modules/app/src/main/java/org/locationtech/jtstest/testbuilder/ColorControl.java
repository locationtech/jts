package org.locationtech.jtstest.testbuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class ColorControl {
  public interface ColorListener {
    void colorChanged(Color clr);
  }
  
  public static JButton OLDcreateColorButton(Component comp, Color initColor, ColorListener colorListener) {
    JButton btn = new JButton();
    Dimension dim = new Dimension(16,16);
    btn.setMinimumSize(dim);
    btn.setMaximumSize(dim);
    btn.setOpaque(true);
    btn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Color clr = showColorChooser(comp, "Background Color", initColor);
        if (clr != null) {
          colorListener.colorChanged(clr);
        }
      }
    });    
    return btn;
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
    ctl.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        Color clr = showColorChooser(comp, title, initColor);
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
}
