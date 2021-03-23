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

package org.locationtech.jtstest.testbuilder.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.util.Collection;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jtstest.util.ExceptionFormatter;
import org.locationtech.jtstest.util.StringUtil;
import org.locationtech.jtstest.util.io.MultiFormatReader;


public class SwingUtil {

  public static FileFilter XML_FILE_FILTER = createFileFilter("JTS Test XML File (*.xml)", ".xml");
  public static FileFilter HTML_FILE_FILTER = createFileFilter("HTML File (*.html)", ".html");
  public static  FileFilter JAVA_FILE_FILTER = createFileFilter("Java File (*.java)", ".java");
  public static  FileFilter PNG_FILE_FILTER = createFileFilter("PNG File (*.png)", ".png");

    /**
     * 
     * Example usage:
     * <pre>
     * SwingUtil.createFileFilter("JEQL script (*.jql)", "jql")
     * </pre>
     * @param description
     * @param extension
     * @return the file filter
     */
    public static FileFilter createFileFilter(final String description, String extension)
    {
      final String dotExt = extension.startsWith(".") ? extension : "." + extension;
      FileFilter ff =  new FileFilter() {
        public String getDescription() {
          return description;
        }
        public boolean accept(File f) {
          return f.isDirectory() || f.toString().toLowerCase().endsWith(dotExt);
        }
      };
      return ff;
    }

    /**
     * 
     * @param comp
     * @param fileChooser
     * @return filename chosen, or
     * null if choose was cancelled for some reason
     */
    public static String chooseFilenameWithConfirm(Component comp, JFileChooser fileChooser) {
      try {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(comp)) {
          File file = fileChooser.getSelectedFile();
          if (! SwingUtil.confirmOverwrite(comp, file)) return null;
          String fullFileName = fileChooser.getSelectedFile().toString();
          return fullFileName;
        }
      }
      catch (Exception x) {
        SwingUtil.reportException(comp, x);
      }
      return null;
    }


  public static boolean confirmOverwrite(Component comp, File file)
  {
    if (file.exists()) {
      int decision = JOptionPane.showConfirmDialog(comp, file.getName()
           + " exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION,
          JOptionPane.WARNING_MESSAGE);
      if (decision == JOptionPane.NO_OPTION) {
        return false;
      }
    }
    return true;
  }
    
  public static void setEnabledWithBackground(Component comp, boolean isEnabled)
  {
    comp.setEnabled(isEnabled);
    if (isEnabled)
      comp.setBackground(SystemColor.text);
    else
      comp.setBackground(SystemColor.control);
  }
  
  public static Object coerce(Object val, Class clz) {
    if (val == null) return val;
    if (val.getClass() == clz) return val;
    if (val instanceof String && (clz == Double.class || clz == double.class))
      return convertDouble((String) val);
    if (val instanceof String && (clz == Integer.class || clz == int.class))
      return convertInteger((String) val);
    return val;
  }
  
  public static Integer convertInteger(String str) {
    int val = 0;
    try {
      val = Integer.parseInt(str);
    } catch (NumberFormatException ex) {
    }
    return val;
  }
  
  public static Double convertDouble(String str) {
    double val = 0;
    try {
      val = Double.parseDouble(str);
    } catch (NumberFormatException ex) {
    }
    return val;
  }
  
  public static Integer getInteger(JTextField txt, Integer defaultVal) {
    String str = txt.getText();
    if (str.trim().length() <= 0)
      return defaultVal;

    int val = 0;
    try {
      val = Integer.parseInt(str);
    } catch (NumberFormatException ex) {
    }
    return val;
  }
  
  public static Double getDouble(JTextField txt, Double defaultVal) {
    String str = txt.getText();
    if (str.trim().length() <= 0)
      return defaultVal;

    double val = 0;
    try {
      val = Double.parseDouble(str);
    } catch (NumberFormatException ex) {
    }
    return val;
  }
  
  public static String value(JTextComponent txt) {
    return txt.getText();
  }
  
  public static Object value(JComboBox cb, Object[] val)
  {
  	int selIndex = cb.getSelectedIndex();
  	if (selIndex == -1) 
  		return null;
  	return val[selIndex];
  }
  
  public static void copyToClipboard(Object o, boolean isFormatted)
  {
    if (o == null) return;
    
  	if (o instanceof Geometry) {
  		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
  				new GeometryTransferable((Geometry) o, isFormatted), null);
  	}
  	else {
  		// transfer as string
  		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				new StringSelection(o.toString()), null);
  	}
  }

  public static Object getFromClipboard() {
		Transferable transferable = getContents(Toolkit.getDefaultToolkit()
				.getSystemClipboard());

		try {
		if (transferable.isDataFlavorSupported(GeometryTransferable.GEOMETRY_FLAVOR)) {
			return transferable.getTransferData(GeometryTransferable.GEOMETRY_FLAVOR);
		}
		// attempt to read as string
		return transferable.getTransferData(DataFlavor.stringFlavor);
		}
		catch (Exception ex) {
			// eat exception, since there isn't anything we can do
		}
		return null;
	}
  
  public static Transferable getContents(Clipboard clipboard) {
    try {
        return clipboard.getContents(null);
    } catch (Throwable t) {
        return null;
    }
  }
    
  public static void reportException(Component c, Exception e) {
    JOptionPane.showMessageDialog(c, StringUtil.wrap(e.toString(), 80), "Exception",
        JOptionPane.ERROR_MESSAGE);
    e.printStackTrace(System.out);
  }

  public static JButton createButton(ImageIcon icon, String tip, ActionListener action ) {
    JButton btn = new JButton();
    btn.setToolTipText(tip);
    btn.setIcon(icon);
    btn.setMargin(new Insets(0, 0, 0, 0));
    if (action != null) btn.addActionListener(action);
    btn.setFocusable(false);
    btn.setFocusPainted(false);
    return btn;
  }

  public static JButton createButton(String title, String tip, ActionListener action ) {
    JButton btn = new JButton();
    btn.setText(title);
    if (tip != null) btn.setToolTipText(tip);
    //btn.setMargin(new Insets(0, 0, 0, 0));
    if (action != null) btn.addActionListener(action);
    return btn;
  }
  public static JButton createButton(String title, ImageIcon icon, String tip, ActionListener action ) {
    return createButton(title, icon, tip, action, false);
  }
    
  public static JButton createButton(String title, ImageIcon icon, String tip, ActionListener action, boolean isFocusable ) {
    JButton btn = new JButton();
    if (title != null) btn.setText(title);
    if (tip != null) btn.setToolTipText(tip);
    if (icon != null) {
      btn.setIcon(icon);
      btn.setIconTextGap(2);
    }
    btn.setMargin(new Insets(0, 2, 0, 2));
    if (action != null) btn.addActionListener(action);
    if (! isFocusable) {
      btn.setFocusable(false);
      btn.setFocusPainted(false);
    }
    return btn;
  }

  public static boolean isCtlKeyPressed(ActionEvent e) {
    return (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK;
  }

  public static boolean isShiftKeyPressed(ActionEvent e) {
    return (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK;
  }

  public static void showTab(JTabbedPane tabPane, String tabName)
  {
    tabPane.setSelectedIndex(tabPane.indexOfTab(tabName));
  }
  
  public static void setAntiAlias(Graphics2D g, boolean isOn) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        isOn ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

  }
}
