/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.ui;

import java.awt.Component;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.util.Collection;
import java.awt.datatransfer.*;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.testbuilder.model.GeometryTransferable;
import org.locationtech.jtstest.util.StringUtil;


public class SwingUtil {

  public static FileFilter XML_FILE_FILTER = createFileFilter("JTS Test XML File (*.xml)", ".xml");
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
    return new Integer(val);
  }
  
  public static Double convertDouble(String str) {
    double val = 0;
    try {
      val = Double.parseDouble(str);
    } catch (NumberFormatException ex) {
    }
    return new Double(val);
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
    return new Integer(val);
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
    return new Double(val);
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
  	if (o instanceof Geometry) {
  		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
  				new GeometryTransferable((Geometry) o, isFormatted), null);
  	}
  	else  
  		// transfer as string
  		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				new StringSelection(o.toString()), null);
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

}
