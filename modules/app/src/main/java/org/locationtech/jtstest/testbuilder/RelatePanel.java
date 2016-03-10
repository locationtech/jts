

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


import java.awt.event.ActionEvent;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import java.awt.*;

import javax.swing.*;

import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.Location;
import org.locationtech.jtstest.testbuilder.model.TestCaseEdit;


/**
 * @version 1.7
 */
public class RelatePanel extends JPanel {
  TestCaseEdit testCase;
  StringBuffer buf = new StringBuffer("X");
  // buffer to turn chars into strings
  //-------------------------------------
  Border border1;
  Border border2;
  JPanel jPanel2 = new JPanel();
  JLabel jLabel17 = new JLabel();
  JLabel equalsAB = new JLabel();
  JLabel jLabel16 = new JLabel();
  JLabel containsBA = new JLabel();
  JLabel jLabel15 = new JLabel();
  JLabel containsAB = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel equalsBA = new JLabel();
  JLabel jLabel6 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JPanel predicates = new JPanel();
  JLabel jLabel3 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel19 = new JLabel();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel18 = new JLabel();
  JLabel disjointAB = new JLabel();
  JLabel disjointBA = new JLabel();
  JLabel intersectsAB = new JLabel();
  JLabel intersectsBA = new JLabel();
  JLabel touchesAB = new JLabel();
  JLabel touchesBA = new JLabel();
  JLabel crossesAB = new JLabel();
  JLabel crossesBA = new JLabel();
  JLabel withinAB = new JLabel();
  JLabel withinBA = new JLabel();
  JLabel overlapsAB = new JLabel();
  JLabel overlapsBA = new JLabel();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  JPanel jPanel3 = new JPanel();
  JLabel relateIE = new JLabel();
  JLabel relateIB = new JLabel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JPanel jPanel1 = new JPanel();
  JLabel jLabel14 = new JLabel();
  JLabel jLabel13 = new JLabel();
  JLabel jLabel12 = new JLabel();
  JLabel jLabel11 = new JLabel();
  JLabel jLabel10 = new JLabel();
  JTextField txtAB = new JTextField();
  JLabel jLabel23 = new JLabel();
  JLabel relateBI = new JLabel();
  JLabel jLabel22 = new JLabel();
  JLabel relateEI = new JLabel();
  JLabel jLabel21 = new JLabel();
  JLabel jLabel20 = new JLabel();
  JLabel relateBE = new JLabel();
  JLabel relateEE = new JLabel();
  JTextField txtBA = new JTextField();
  JLabel relateBB = new JLabel();
  JLabel jLabel9 = new JLabel();
  JLabel relateEB = new JLabel();
  JLabel jLabel8 = new JLabel();
  JLabel jLabel7 = new JLabel();
  JLabel relateII = new JLabel();
  TitledBorder titledBorder1;
  JLabel tickCrossLabel = new JLabel();
  GridBagLayout gridBagLayout4 = new GridBagLayout();
  JPanel matrixPanel = new JPanel();
  GridBagLayout gridBagLayout5 = new GridBagLayout();
  Border border3;
  private final ImageIcon tickIcon = new ImageIcon(this.getClass().getResource("tickShaded.gif"));
  private final ImageIcon crossIcon = new ImageIcon(this.getClass().getResource("crossShaded.gif"));
  private final ImageIcon clearIcon = new ImageIcon(this.getClass().getResource("clear.gif"));
  private DocumentListener expectedImDocumentListener =
    new DocumentListener() {

      public void insertUpdate(DocumentEvent e) {
        expectedIntersectionMatrixChanged();
      }

      public void removeUpdate(DocumentEvent e) {
        expectedIntersectionMatrixChanged();
      }

      public void changedUpdate(DocumentEvent e) {
        expectedIntersectionMatrixChanged();
      }
    };
  private JLabel coversAB = new JLabel();
  private JLabel coversBA = new JLabel();
  private JLabel jLabel110 = new JLabel();
  private JLabel jLabel111 = new JLabel();
  private JLabel coveredByAB = new JLabel();
  private JLabel coveredByBA = new JLabel();

  public RelatePanel() {
    try {
      jbInit();
      clearResults();
      //expectedImTextField.getDocument().addDocumentListener(expectedImDocumentListener);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void setTestCase(TestCaseEdit testCase) {
    this.testCase = testCase;
    clearResults();
    //expectedImTextField.setText(testCase.getExpectedIntersectionMatrix());
  }

  public void clearResults() {
    equalsAB.setText("-");
    equalsBA.setText("-");
    disjointAB.setText("-");
    disjointBA.setText("-");
    intersectsAB.setText("-");
    intersectsBA.setText("-");
    touchesAB.setText("-");
    touchesBA.setText("-");
    crossesAB.setText("-");
    crossesBA.setText("-");
    withinAB.setText("-");
    withinBA.setText("-");
    containsAB.setText("-");
    containsBA.setText("-");
    overlapsAB.setText("-");
    overlapsBA.setText("-");
    coversAB.setText("-");
    coversBA.setText("-");
    coveredByAB.setText("-");
    coveredByBA.setText("-");
    relateII.setText("-");
    relateIB.setText("-");
    relateIE.setText("-");
    relateBI.setText("-");
    relateBB.setText("-");
    relateBE.setText("-");
    relateEI.setText("-");
    relateEB.setText("-");
    relateEE.setText("-");
    txtAB.setText("");
    txtBA.setText("");
    tickCrossLabel.setIcon(clearIcon);
  }

  public void runTests() {
    if (testCase.getGeometry(0) != null && testCase.getGeometry(1) != null) {
      IntersectionMatrix im = testCase.getIM();
      IntersectionMatrix imBA = new IntersectionMatrix(im);
      //IntersectionMatrix expectedIm = new IntersectionMatrix(expectedImTextField.getText());
      imBA.transpose();
      setRelateLabel(relateII, im.get(Location.INTERIOR, Location.INTERIOR));
      setRelateLabel(relateIB, im.get(Location.INTERIOR, Location.BOUNDARY));
      setRelateLabel(relateIE, im.get(Location.INTERIOR, Location.EXTERIOR));
      setRelateLabel(relateBI, im.get(Location.BOUNDARY, Location.INTERIOR));
      setRelateLabel(relateBB, im.get(Location.BOUNDARY, Location.BOUNDARY));
      setRelateLabel(relateBE, im.get(Location.BOUNDARY, Location.EXTERIOR));
      setRelateLabel(relateEI, im.get(Location.EXTERIOR, Location.INTERIOR));
      setRelateLabel(relateEB, im.get(Location.EXTERIOR, Location.BOUNDARY));
      setRelateLabel(relateEE, im.get(Location.EXTERIOR, Location.EXTERIOR));
      //tickCrossLabel.setIcon(im.matches(expectedIm.toString()) ? tickIcon : crossIcon);
      txtAB.setText(im.toString());
      txtBA.setText(imBA.toString());

      setPredicate(equalsAB, im.isEquals(testCase.getGeometry(0).getDimension(),
          testCase.getGeometry(1).getDimension()));
      setPredicate(disjointAB, im.isDisjoint());
      setPredicate(intersectsAB, im.isIntersects());
      setPredicate(touchesAB, im.isTouches(testCase.getGeometry(0).getDimension(),
          testCase.getGeometry(1).getDimension()));
      setPredicate(crossesAB, im.isCrosses(testCase.getGeometry(0).getDimension(),
          testCase.getGeometry(1).getDimension()));
      setPredicate(withinAB, im.isWithin());
      setPredicate(containsAB, im.isContains());
      setPredicate(overlapsAB, im.isOverlaps(testCase.getGeometry(0).getDimension(),
          testCase.getGeometry(1).getDimension()));
      setPredicate(coversAB, im.isCovers());
      setPredicate(coveredByAB, im.isCoveredBy());


      setPredicate(equalsBA, imBA.isEquals(testCase.getGeometry(1).getDimension(),
          testCase.getGeometry(0).getDimension()));
      setPredicate(disjointBA, imBA.isDisjoint());
      setPredicate(intersectsBA, imBA.isIntersects());
      setPredicate(touchesBA, imBA.isTouches(testCase.getGeometry(1).getDimension(),
          testCase.getGeometry(0).getDimension()));
      setPredicate(crossesBA, imBA.isCrosses(testCase.getGeometry(1).getDimension(),
          testCase.getGeometry(0).getDimension()));
      setPredicate(withinBA, imBA.isWithin());
      setPredicate(containsBA, imBA.isContains());
      setPredicate(overlapsBA, imBA.isOverlaps(testCase.getGeometry(1).getDimension(),
          testCase.getGeometry(0).getDimension()));
      setPredicate(coversBA, imBA.isCovers());
      setPredicate(coveredByBA, imBA.isCoveredBy());
    }
  }

  void setRelateLabel(JLabel lbl, int imValue) {
    buf.setCharAt(0, org.locationtech.jts.geom.Dimension.toDimensionSymbol(imValue));
    lbl.setText(buf.toString());
  }

  void setPredicate(JLabel lbl, boolean b) {
    String val = b ? "T" : "F";
    lbl.setText(val);
  }

  void jbInit() throws Exception {
    border1 = new TitledBorder(BorderFactory.createLineBorder(Color.gray, 1),
        "Binary Predicates");
    border2 = new TitledBorder(BorderFactory.createLineBorder(Color.gray, 1),
        "Intersection Matrix");
    titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(Color.gray,
        1), "Intersection Matrix");
    border3 = BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(Color.gray,
        1), "Intersection Matrix"), BorderFactory.createEmptyBorder(0, 5, 0,
        5));
    this.setLayout(gridBagLayout3);
    this.setPreferredSize(new java.awt.Dimension(233, 100));
    jLabel17.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel17.setForeground(Color.blue);
    jLabel17.setText("Crosses");
    equalsAB.setFont(new java.awt.Font("Dialog", 1, 12));
    equalsAB.setToolTipText("");
    equalsAB.setText("-");
    jLabel16.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel16.setForeground(Color.blue);
    jLabel16.setToolTipText("");
    jLabel16.setText("Within");
    containsBA.setFont(new java.awt.Font("Dialog", 1, 12));
    containsBA.setText("-");
    jLabel15.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel15.setForeground(Color.blue);
    jLabel15.setToolTipText("");
    jLabel15.setText("Touches");
    containsAB.setFont(new java.awt.Font("Dialog", 1, 12));
    containsAB.setText("-");
    equalsBA.setFont(new java.awt.Font("Dialog", 1, 12));
    equalsBA.setToolTipText("");
    equalsBA.setText("-");
    jLabel6.setFont(new java.awt.Font("Dialog", 2, 12));
    jLabel6.setText("BA");
    jLabel5.setFont(new java.awt.Font("Dialog", 2, 12));
    jLabel5.setText("AB");
    predicates.setLayout(gridBagLayout1);
    predicates.setBorder(border1);
    jLabel3.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel3.setForeground(Color.blue);
    jLabel3.setToolTipText("");
    jLabel3.setText("Intersects");
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel2.setForeground(Color.blue);
    jLabel2.setToolTipText("");
    jLabel2.setText("Disjoint");
    jLabel19.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel19.setForeground(Color.blue);
    jLabel19.setToolTipText("");
    jLabel19.setText("Overlaps");
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel1.setForeground(Color.blue);
    jLabel1.setText("Equals");
    jLabel18.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel18.setForeground(Color.blue);
    jLabel18.setToolTipText("");
    jLabel18.setText("Contains");
    disjointAB.setFont(new java.awt.Font("Dialog", 1, 12));
    disjointAB.setToolTipText("");
    disjointAB.setText("-");
    disjointBA.setFont(new java.awt.Font("Dialog", 1, 12));
    disjointBA.setText("-");
    intersectsAB.setFont(new java.awt.Font("Dialog", 1, 12));
    intersectsAB.setText("-");
    intersectsBA.setFont(new java.awt.Font("Dialog", 1, 12));
    intersectsBA.setText("-");
    touchesAB.setFont(new java.awt.Font("Dialog", 1, 12));
    touchesAB.setText("-");
    touchesBA.setFont(new java.awt.Font("Dialog", 1, 12));
    touchesBA.setText("-");
    crossesAB.setFont(new java.awt.Font("Dialog", 1, 12));
    crossesAB.setText("-");
    crossesBA.setFont(new java.awt.Font("Dialog", 1, 12));
    crossesBA.setText("-");
    withinAB.setFont(new java.awt.Font("Dialog", 1, 12));
    withinAB.setText("-");
    withinBA.setFont(new java.awt.Font("Dialog", 1, 12));
    withinBA.setText("-");
    overlapsAB.setFont(new java.awt.Font("Dialog", 1, 12));
    overlapsAB.setText("-");
    overlapsBA.setFont(new java.awt.Font("Dialog", 1, 12));
    overlapsBA.setText("-");
    relateIE.setFont(new java.awt.Font("Dialog", 1, 12));
    relateIE.setText("F");
    relateIB.setFont(new java.awt.Font("Dialog", 1, 12));
    relateIB.setText("F");
    jPanel1.setLayout(gridBagLayout2);
    jLabel14.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel14.setForeground(Color.blue);
    jLabel14.setText("A");
    jLabel13.setFont(new java.awt.Font("Dialog", 2, 12));
    jLabel13.setForeground(Color.blue);
    jLabel13.setText("Ext");
    jLabel12.setFont(new java.awt.Font("Dialog", 2, 12));
    jLabel12.setForeground(Color.blue);
    jLabel12.setText("Bdy");
    jLabel11.setFont(new java.awt.Font("Dialog", 2, 12));
    jLabel11.setForeground(Color.blue);
    jLabel11.setToolTipText("");
    jLabel11.setText("Int");
    jLabel10.setFont(new java.awt.Font("Dialog", 2, 12));
    jLabel10.setForeground(Color.red);
    jLabel10.setToolTipText("");
    jLabel10.setText("Ext");
    txtAB.setBackground(SystemColor.control);
    txtAB.setFont(new java.awt.Font("Dialog", 0, 12));
    txtAB.setMaximumSize(new java.awt.Dimension(100, 21));
    txtAB.setMinimumSize(new java.awt.Dimension(100, 21));
    txtAB.setPreferredSize(new java.awt.Dimension(100, 21));
    txtAB.setToolTipText("");
    txtAB.setEditable(false);
    txtAB.setHorizontalAlignment(SwingConstants.LEFT);
    jLabel23.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel23.setForeground(Color.red);
    jLabel23.setText("B");
    relateBI.setFont(new java.awt.Font("Dialog", 1, 12));
    relateBI.setText("F");
    jLabel22.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel22.setForeground(Color.blue);
    jLabel22.setToolTipText("");
    jLabel22.setText("A");
    relateEI.setFont(new java.awt.Font("Dialog", 1, 12));
    relateEI.setText("F");
    jLabel21.setToolTipText("");
    jLabel21.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel21.setForeground(Color.blue);
    jLabel21.setToolTipText("");
    jLabel21.setText("A");
    jLabel20.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel20.setForeground(Color.red);
    jLabel20.setToolTipText("");
    jLabel20.setText("B");
    relateBE.setFont(new java.awt.Font("Dialog", 1, 12));
    relateBE.setText("F");
    relateEE.setFont(new java.awt.Font("Dialog", 1, 12));
    relateEE.setText("F");
    txtBA.setBackground(SystemColor.control);
    txtBA.setFont(new java.awt.Font("Dialog", 0, 12));
    txtBA.setMaximumSize(new java.awt.Dimension(100, 21));
    txtBA.setMinimumSize(new java.awt.Dimension(100, 21));
    txtBA.setPreferredSize(new java.awt.Dimension(100, 21));
    txtBA.setEditable(false);
    txtBA.setHorizontalAlignment(SwingConstants.LEFT);
    relateBB.setFont(new java.awt.Font("Dialog", 1, 12));
    relateBB.setText("F");
    jLabel9.setFont(new java.awt.Font("Dialog", 2, 12));
    jLabel9.setForeground(Color.red);
    jLabel9.setToolTipText("");
    jLabel9.setText("Bdy");
    relateEB.setFont(new java.awt.Font("Dialog", 1, 12));
    relateEB.setText("F");
    jLabel8.setFont(new java.awt.Font("Dialog", 2, 12));
    jLabel8.setForeground(Color.red);
    jLabel8.setToolTipText("");
    jLabel8.setText("Int");
    jLabel7.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel7.setForeground(Color.red);
    jLabel7.setText("B");
    relateII.setBackground(Color.white);
    relateII.setFont(new java.awt.Font("Dialog", 1, 12));
    relateII.setText("F");
    jPanel3.setLayout(gridBagLayout4);
    jPanel3.setBorder(border3);
    tickCrossLabel.setIcon(clearIcon);
    matrixPanel.setLayout(gridBagLayout5);
    coversAB.setFont(new java.awt.Font("Dialog", 1, 12));
    coversAB.setToolTipText("");
    coversAB.setText("-");
    coversBA.setFont(new java.awt.Font("Dialog", 1, 12));
    coversBA.setToolTipText("");
    coversBA.setText("-");
    jLabel110.setText("CoveredBy");
    jLabel110.setToolTipText("");
    jLabel110.setForeground(Color.blue);
    jLabel110.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel111.setText("Covers");
    jLabel111.setToolTipText("");
    jLabel111.setForeground(Color.blue);
    jLabel111.setFont(new java.awt.Font("Dialog", 1, 12));
    coveredByAB.setFont(new java.awt.Font("Dialog", 1, 12));
    coveredByAB.setText("-");
    coveredByBA.setFont(new java.awt.Font("Dialog", 1, 12));
    coveredByBA.setText("-");
    this.add(jPanel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jPanel3.add(jPanel1, new GridBagConstraints(100, 100, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(jLabel8,  new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
    matrixPanel.add(jLabel9,  new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
    matrixPanel.add(jLabel10,  new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
    matrixPanel.add(jLabel11,  new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    matrixPanel.add(jLabel12,  new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    matrixPanel.add(jLabel13,  new GridBagConstraints(2, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    matrixPanel.add(jLabel14,  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    matrixPanel.add(relateII,  new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(relateIB,  new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(relateIE,  new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(relateBI,  new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(relateBB,  new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(relateBE,  new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(relateEI,  new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(relateEB,  new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(relateEE,  new GridBagConstraints(6, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    matrixPanel.add(jLabel7,     new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    jPanel1.add(txtBA,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    jPanel1.add(jLabel22,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    jPanel1.add(jLabel23,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jPanel1.add(tickCrossLabel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0,
        0, 0), 0, 0));
    jPanel1.add(jLabel20,   new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 3), 0, 0));
    jPanel1.add(jLabel21,   new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jPanel1.add(txtAB,    new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    jPanel3.add(matrixPanel, new GridBagConstraints(100, 110, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0,
        0, 0), 0, 0));
    this.add(jPanel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST,
        GridBagConstraints.BOTH, new Insets(0, -6, 0, 6), 0, 0));
    jPanel2.add(predicates, null);
    predicates.add(equalsAB,         new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(equalsBA,         new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(jLabel5,         new GridBagConstraints(1, 0, 1, 1, 0.1, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    predicates.add(jLabel6,         new GridBagConstraints(2, 0, 1, 1, 0.1, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    predicates.add(jLabel1,         new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(jLabel2,         new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(jLabel3,         new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 10), 0, 0));
    predicates.add(jLabel15,         new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(jLabel17,         new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(jLabel16,         new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(jLabel18,         new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(containsAB,         new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(containsBA,         new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(disjointAB,         new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(disjointBA,         new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(intersectsAB,         new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(intersectsBA,         new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(touchesAB,         new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(touchesBA,         new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(crossesAB,         new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(crossesBA,         new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(withinAB,         new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(withinBA,         new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(overlapsAB,         new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(overlapsBA,         new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(coversAB,          new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(coversBA,         new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(jLabel19,         new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(jLabel110,             new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(jLabel111,        new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    predicates.add(coveredByAB,  new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    predicates.add(coveredByBA, new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }


  private void expectedIntersectionMatrixChanged() {
    /*
    if (expectedImTextField.getText().length() == 9) {
      testCase.setExpectedIntersectionMatrix(expectedImTextField.getText());
    }
    */
  }
}


