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
package org.locationtech.jtstest.testbuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import org.locationtech.jtstest.function.*;
import org.locationtech.jtstest.util.*;


/**
 * @version 1.7
 */
public class GeometryFunctionListPanel extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();

  private DefaultListModel listModel = new DefaultListModel();

  JScrollPane jScrollPane1 = new JScrollPane();

  JList list = new JList(listModel){
    public String getToolTipText(MouseEvent e) {
      int index = locationToIndex(e.getPoint());
      if (-1 < index) {
      	GeometryFunction func = (GeometryFunction) getModel().getElementAt(index);
        return func.getSignature();
      } else {
        return null;
      }
    }
  };

  BorderLayout borderLayout2 = new BorderLayout();
  Border border1;

  private class GeometryFunctionCellRenderer extends JLabel implements
      ListCellRenderer 
  {
    Border spaceBorder = BorderFactory.createEmptyBorder(0, 4, 1, 0);
    
    private final ImageIcon binaryIcon = new ImageIcon(this.getClass()
        .getResource("BinaryGeomFunction.png"));

    private final ImageIcon unaryIcon = new ImageIcon(this.getClass()
        .getResource("UnaryGeomFunction.png"));

    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      GeometryFunction func = (GeometryFunction) value;
      String name = StringUtil.capitalize(func.getName());
      setBorder(spaceBorder);
      setText(name);
      setOpaque(true);
      setIcon(func.isBinary() ? binaryIcon : unaryIcon);
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setEnabled(list.isEnabled());
      setFont(list.getFont());
      return this;
    }
    
    /*
    public String getToolTipText(MouseEvent e)
    {
    	return getText();
    }
    */
  }

  public GeometryFunctionListPanel() {
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    list.setCellRenderer(new GeometryFunctionCellRenderer());
  }

  private void jbInit() throws Exception {
    setSize(200, 250);
    border1 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    setLayout(borderLayout2);
    setBorder(border1);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(list, null);
  }

  /*
  private void registerListSelectionListener() {
    list.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {

          public void valueChanged(ListSelectionEvent e) {
            if (list.getSelectedValue() == null)
              return;
          }
        });
  }
*/
  
  public void registerListSelectionListener(ListSelectionListener listener) {
    list.getSelectionModel().addListSelectionListener(listener);
  }

  public void registerMouseListener(MouseListener listener) {
    list.addMouseListener(listener);
  }

  public GeometryFunction getFunction() {
    if (list.getSelectedValue() == null)
      return null;
    return (GeometryFunction) list.getSelectedValue();
  }
   
  public void populate(List funcs) {
//  listModel.clear();
         for (Iterator i = funcs.iterator(); i.hasNext(); ) {
             GeometryFunction func = (GeometryFunction) i.next();
             listModel.addElement(func);
         }
     }
}
