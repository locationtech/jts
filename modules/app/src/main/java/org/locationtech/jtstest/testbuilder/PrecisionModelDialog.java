

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
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.locationtech.jts.geom.PrecisionModel;



/**
 * @version 1.7
 */
public class PrecisionModelDialog extends JDialog {
  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  PrecisionModelPanel precisionModelPanel = new PrecisionModelPanel();
  JPanel jPanel1 = new JPanel();
  JButton okButton = new JButton();

  public PrecisionModelDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public PrecisionModelDialog() {
    this(null, "", false);
  }
  void jbInit() throws Exception {
    panel1.setLayout(borderLayout1);
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    getContentPane().add(panel1);
    panel1.add(precisionModelPanel, BorderLayout.CENTER);
    panel1.add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(okButton, null);
  }

  void okButton_actionPerformed(ActionEvent e) {
    setVisible(false);
  }

  public PrecisionModel getPrecisionModel() {
    return precisionModelPanel.getPrecisionModel();
  }

  public void setPrecisionModel(PrecisionModel precisionModel) {
    precisionModelPanel.setPrecisionModel(precisionModel);
  }
}
