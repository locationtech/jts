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
package org.locationtech.jtstest.testbuilder;



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
  JLabel lblCrosses = new JLabel();
  JLabel equalsAB = new JLabel();
  JLabel lblWithin = new JLabel();
  JLabel containsBA = new JLabel();
  JLabel lblTouches = new JLabel();
  JLabel containsAB = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel equalsBA = new JLabel();
  JLabel lblPredBA = new JLabel();
  JLabel lblPredAB = new JLabel();
  JPanel predicates = new JPanel();
  JLabel lblIntersects = new JLabel();
  JLabel lblDisjoint = new JLabel();
  JLabel lblOverlaps = new JLabel();
  JLabel lblEquals = new JLabel();
  JLabel lblContains = new JLabel();
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
  private JLabel lblCoveredBy = new JLabel();
  private JLabel lblCovers = new JLabel();
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

      int dimA = testCase.getGeometry(0).getDimension();
      int dimB = testCase.getGeometry(1).getDimension();
      
      setPredicate(equalsAB, im.isEquals(dimA, dimB));
      setPredicate(disjointAB, im.isDisjoint());
      setPredicate(intersectsAB, im.isIntersects());
      setPredicate(touchesAB, im.isTouches(dimA, dimB));
      setPredicate(crossesAB, im.isCrosses(dimA, dimB));
      setPredicate(withinAB, im.isWithin());
      setPredicate(containsAB, im.isContains());
      setPredicate(overlapsAB, im.isOverlaps(dimA, dimB));
      setPredicate(coversAB, im.isCovers());
      setPredicate(coveredByAB, im.isCoveredBy());

      setPredicate(equalsBA, imBA.isEquals(dimB, dimA));
      setPredicate(disjointBA, imBA.isDisjoint());
      setPredicate(intersectsBA, imBA.isIntersects());
      setPredicate(touchesBA, imBA.isTouches(dimB, dimA));
      setPredicate(crossesBA, imBA.isCrosses(dimB, dimA));
      setPredicate(withinBA, imBA.isWithin());
      setPredicate(containsBA, imBA.isContains());
      setPredicate(overlapsBA, imBA.isOverlaps(dimB, dimA));
      setPredicate(coversBA, imBA.isCovers());
      setPredicate(coveredByBA, imBA.isCoveredBy());
    }
  }

  void setRelateLabel(JLabel lbl, int imValue) {
    buf.setCharAt(0, org.locationtech.jts.geom.Dimension.toDimensionSymbol(imValue));
    lbl.setText(buf.toString());
  }

  private static Color CLR_TRUE = Color.GREEN.darker().darker();
  private static Color CLR_FALSE = Color.RED.darker();
  
  void setPredicate(JLabel lbl, boolean b) {
    String val = b ? "T" : "F";
    lbl.setText(val);
    lbl.setForeground(b ? CLR_TRUE : CLR_FALSE );
    lbl.setBackground(Color.WHITE);
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
    txtAB.setBackground(AppColors.BACKGROUND);
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
    txtBA.setBackground(AppColors.BACKGROUND);
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
    
    

    lblPredBA.setFont(new java.awt.Font("Dialog", 2, 12));
    lblPredBA.setText("BA");
    lblPredAB.setFont(new java.awt.Font("Dialog", 2, 12));
    lblPredAB.setText("AB");
    predicates.setLayout(gridBagLayout1);
    predicates.setBorder(border1);
    
    this.add(jPanel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST,
        GridBagConstraints.BOTH, new Insets(0, -6, 0, 6), 0, 0));
    jPanel2.add(predicates, null);
    
    
    predicates.add(lblPredAB,         new GridBagConstraints(1, 0, 1, 1, 0.1, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    predicates.add(lblPredBA,         new GridBagConstraints(2, 0, 1, 1, 0.1, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    
    addPredicate("Equals", lblEquals, equalsAB, equalsBA, 1);
    
    setDivider(lblIntersects);
    addPredicate("Intersects", lblIntersects, intersectsAB, intersectsBA, 2);
    addPredicate("Disjoint", lblDisjoint, disjointAB, disjointBA, 3);

    setDivider(lblContains);
    addPredicate("Contains", lblContains, containsAB, containsBA, 4);
    addPredicate("Within", lblWithin, withinAB, withinBA, 5);

    setDivider(lblCovers);
    addPredicate("Covers", lblCovers, coversAB, coversBA, 6);
    addPredicate("CoveredBy", lblCoveredBy, coveredByAB, coveredByBA, 7);  
    
    setDivider(lblCrosses);
    addPredicate("Crosses", lblCrosses, crossesAB, crossesBA, 8);
    addPredicate("Overlaps", lblOverlaps, overlapsAB, overlapsBA, 9);
    addPredicate("Touches", lblTouches, touchesAB, touchesBA, 10);

  }

  private void addPredicate(String name, JLabel lblName, JLabel valueAB, JLabel valueBA, int row) {
    addPredicateName(name, lblName, row);
    addPredicateValue(valueAB, 1, row);
    addPredicateValue(valueBA, 2, row);
  }
  private void addPredicateName(String name, JLabel lbl, int y) {
    lbl.setText(name);
    lbl.setToolTipText("");
    lbl.setForeground(Color.blue);
    lbl.setFont(new java.awt.Font("Dialog", 1, 12));
    
    predicates.add(lbl,         
        new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
        ,GridBagConstraints.EAST, 
        GridBagConstraints.NONE, new Insets(0, 5, 0, 10), 0, 0));
  }

  private void addPredicateValue(JLabel lbl, int x, int y) {
    lbl.setFont(new java.awt.Font("Dialog", 1, 12));
    lbl.setText("-");
    
    predicates.add(lbl, 
        new GridBagConstraints(x, y, 1, 1, 0.0, 0.0, 
        GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 0, 0, 0), 0, 0));
  }
  
  private void setDivider(JLabel lbl) {
    lbl.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
  }


  private void expectedIntersectionMatrixChanged() {
    /*
    if (expectedImTextField.getText().length() == 9) {
      testCase.setExpectedIntersectionMatrix(expectedImTextField.getText());
    }
    */
  }
}


