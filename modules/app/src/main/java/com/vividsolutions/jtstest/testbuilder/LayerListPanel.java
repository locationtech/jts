/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.testbuilder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.locationtech.jts.geom.*;

import com.vividsolutions.jtstest.test.Testable;
import com.vividsolutions.jtstest.testbuilder.model.Layer;
import com.vividsolutions.jtstest.testbuilder.model.LayerList;
import com.vividsolutions.jtstest.testbuilder.model.TestCaseEdit;

/**
 * @version 1.7
 */
public class LayerListPanel extends JPanel {
    BorderLayout borderLayout1 = new BorderLayout();
    private DefaultListModel listModel = new DefaultListModel();
    JScrollPane jScrollPane1 = new JScrollPane();
    LayerCheckBoxList list = new LayerCheckBoxList(listModel);
    BorderLayout borderLayout2 = new BorderLayout();

    public LayerListPanel() {
        try {
            uiInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        registerListSelectionListener();
    }

    private void uiInit() throws Exception {
        setSize(200, 250);
        setLayout(borderLayout2);
        list.setBackground(SystemColor.control);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectionBackground(Color.GRAY);
        add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(list, null);
    }

    private void registerListSelectionListener() {
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (list.getSelectedValue() == null)
                    return;
//TODO: implement event logic        }
        }});
    }

    public void populateList() {
        listModel.clear();
        LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();
        
        for (int i = 0; i < lyrList.size(); i++) {
          Layer lyr = lyrList.getLayer(i);
          listModel.addElement(lyr);
        }
    }

    
}


