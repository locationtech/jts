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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;
import org.locationtech.jts.io.gml2.*;
import org.locationtech.jtstest.testbuilder.model.*;



/**
 * @version 1.7
 */
public class TestCaseTextDialog extends JDialog {
    private TestCaseEdit test;
    //----------------------------------
    JPanel dialogPanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextArea txtGeomView = new JTextArea();
    JPanel jPanel1 = new JPanel();
    JPanel cmdButtonPanel = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JButton btnCopy = new JButton();
    JButton btnOk = new JButton();
    JPanel textFormatPanel = new JPanel();
    JPanel allOptionsPanel = new JPanel();
    JPanel functionsPanel = new JPanel();
    BoxLayout boxLayout1 = new BoxLayout(functionsPanel, BoxLayout.Y_AXIS);
    ButtonGroup textFormatGroup = new ButtonGroup();
    
    JRadioButton rbSVG = new JRadioButton();
    JRadioButton rbXML = new JRadioButton();
    JRadioButton rbXMLWKB = new JRadioButton();
    JRadioButton rbTestCaseJava = new JRadioButton();
    JRadioButton rbJTSJava = new JRadioButton();
    JRadioButton rbWKB = new JRadioButton();
    JRadioButton rbWKT = new JRadioButton();
    JRadioButton rbWKTFormatted = new JRadioButton();
    JRadioButton rbGML = new JRadioButton();
    
    JCheckBox intersectsCB = new JCheckBox();

    public TestCaseTextDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public TestCaseTextDialog() {
        this(null, "", false);
    }

    void jbInit() throws Exception {
        dialogPanel.setLayout(borderLayout1);
        jScrollPane1.setPreferredSize(new Dimension(500, 300));
        txtGeomView.setLineWrap(true);
        jPanel1.setLayout(borderLayout2);
        btnCopy.setEnabled(true);
        btnCopy.setText("Copy");
        btnCopy.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnSelect_actionPerformed(e);
            }
        });
        btnOk.setToolTipText("");
        btnOk.setText("Close");
        btnOk.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnOk_actionPerformed(e);
            }
        });
        rbXML.setText("Test XML");
        rbXML.setToolTipText("");
        rbXML.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rbXML_actionPerformed(e);
            }
        });
        rbXMLWKB.setText("Test XML - WKB");
        rbXMLWKB.setToolTipText("");
        rbXMLWKB.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rbXMLWKB_actionPerformed(e);
            }
        });
        rbSVG.setText("SVG");
        rbSVG.setToolTipText("");
        rbSVG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rbSVG_actionPerformed(e);
            }
        });
        
        rbTestCaseJava.setText("TestCase Java");
        rbTestCaseJava.setToolTipText("");
        rbTestCaseJava.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rbTestCaseJava_actionPerformed(e);
            }
        });
        rbJTSJava.setEnabled(false);
        rbJTSJava.setText("JTS Java ");
        
        rbWKT.setText("WKT");
        rbWKT.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
              rbWKT_actionPerformed(e);
          }
      });
        
        rbWKTFormatted.setText("WKT-Formatted");
        rbWKTFormatted.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
              rbWKTFormatted_actionPerformed(e);
          }
      });
        
        rbWKB.setText("WKB");
        rbWKB.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
              rbWKB_actionPerformed(e);
          }
      });
        rbGML.setText("GML");
        rbGML.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
              rbGML_actionPerformed(e);
          }
      });
        getContentPane().add(dialogPanel);
        dialogPanel.add(jScrollPane1, BorderLayout.CENTER);
        dialogPanel.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(cmdButtonPanel, BorderLayout.SOUTH);
        cmdButtonPanel.add(btnCopy, null);
        cmdButtonPanel.add(btnOk, null);
        textFormatPanel.add(rbWKT, null);
        textFormatPanel.add(rbWKTFormatted, null);
        textFormatPanel.add(rbWKB, null);
        textFormatPanel.add(rbGML, null);
        textFormatPanel.add(rbSVG, null);
        textFormatPanel.add(rbTestCaseJava, null);
        textFormatPanel.add(rbXML, null);
        textFormatPanel.add(rbXMLWKB, null);
        textFormatPanel.add(rbJTSJava, null);
        jScrollPane1.getViewport().add(txtGeomView, null);
        
        allOptionsPanel.setLayout(borderLayout3);
        allOptionsPanel.add(textFormatPanel, BorderLayout.NORTH);
        allOptionsPanel.add(functionsPanel, BorderLayout.CENTER);
       
        jPanel1.add(allOptionsPanel, BorderLayout.CENTER);

        textFormatGroup.add(rbJTSJava);
        textFormatGroup.add(rbTestCaseJava);
        textFormatGroup.add(rbXML);
        textFormatGroup.add(rbXMLWKB);
        textFormatGroup.add(rbWKT);
        textFormatGroup.add(rbWKTFormatted);
        textFormatGroup.add(rbWKB);
        textFormatGroup.add(rbGML);
        textFormatGroup.add(rbSVG);
    }

    public void setTestCase(TestCaseEdit test) {
        this.test = test;
        // choose default format
        rbXML.setEnabled(true);
        rbXML.doClick();
    }

    void btnOk_actionPerformed(ActionEvent e) {
        setVisible(false);
    }

    void btnSelect_actionPerformed(ActionEvent e) {
        txtGeomView.selectAll();
        txtGeomView.copy();
    }

    void rbTestCaseJava_actionPerformed(ActionEvent e) {
        txtGeomView.setText((new JavaTestWriter()).write(test));
    }

    void rbXML_actionPerformed(ActionEvent e) {
      txtGeomView.setText((new XMLTestWriter()).getTestXML(test));
  }
    
    void rbXMLWKB_actionPerformed(ActionEvent e) {
      txtGeomView.setText((new XMLTestWriter()).getTestXML(test, false));
  }
    
    void rbSVG_actionPerformed(ActionEvent e) {
      txtGeomView.setText(SVGTestWriter.getTestSVG(test));
  }
    
    void rbWKB_actionPerformed(ActionEvent e) {
    	writeView(
    			convertToWKB(test.getGeometry(0)),
    			convertToWKB(test.getGeometry(1)),
    			convertToWKB(test.getResult())
    			);
    }
    
    void rbWKT_actionPerformed(ActionEvent e) {
    	writeView(
    			test.getGeometry(0) == null ? null : test.getGeometry(0).toString(),
        	test.getGeometry(1) == null ? null :test.getGeometry(1).toString(),
          test.getResult() == null ? null :test.getResult().toString()
    			);
  }
    
    void rbWKTFormatted_actionPerformed(ActionEvent e) {
    	writeView(
    			convertToWKT(test.getGeometry(0), true),
    			convertToWKT(test.getGeometry(1), true),
    			convertToWKT(test.getResult(), true)
    			);
  }
    
    void rbGML_actionPerformed(ActionEvent e) {
      writeView(
          convertToGML(test.getGeometry(0)),
          convertToGML(test.getGeometry(1)),
          convertToGML(test.getResult())
          );
  }
    
  private void writeView(String a, String b, String result)
  {
  	txtGeomView.setText("");
  	writeViewGeometry("A", a);
  	writeViewGeometry("B", b);
  	writeViewGeometry("Result", result);
  }
  
  private void writeViewGeometry(String tag, String str)
  {
  	if (str == null || str.length() <= 0) return;
		txtGeomView.append(tag + ":\n\n");
		txtGeomView.append(str);
		txtGeomView.append("\n\n");
  }
  
    private String convertToWKT(Geometry g, boolean isFormatted)
    {
    	if (g == null) return "";
    	if (! isFormatted)
    		return g.toString();
    	WKTWriter writer = new WKTWriter();
    	writer.setFormatted(isFormatted);
    	writer.setMaxCoordinatesPerLine(5);
    	return writer.write(g);
    }
    
    private String convertToWKB(Geometry g)
    {
    	if (g == null) return "";
    	return WKBWriter.toHex((new WKBWriter().write(g)));
    }
    
    private String convertToGML(Geometry g)
    {
      if (g == null) return "";
      return (new GMLWriter()).write(g);
    }
    
}
