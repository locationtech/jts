package org.locationtech.jts.operation.relateng;

import org.locationtech.jts.geom.Dimension;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class RelatePredicateTest extends TestCase {

  private static final String A_EXT_B_INT = "***.***.1**";
  private static final String A_INT_B_INT = "1**.***.***";

  public static void main(String args[]) {
    TestRunner.run(RelatePredicateTest.class);
  }
  
  public RelatePredicateTest(String name) {
    super(name);
  }
  
  public void testIntersects() {
    checkPredicate(RelatePredicate.intersects(), A_INT_B_INT, true);
  }
 
  public void testDisjoint() {
    checkPredicate(RelatePredicate.intersects(), A_EXT_B_INT, false);
    checkPredicate(RelatePredicate.disjoint(), A_EXT_B_INT, true);
  }

  public void testCovers() {
    checkPredicate(RelatePredicate.covers(), A_INT_B_INT, true);
    checkPredicate(RelatePredicate.covers(), A_EXT_B_INT, false);
  }

  public void testCoversFast() {
    checkPredicatePartial(RelatePredicate.covers(), A_EXT_B_INT, false);
  }

  public void testMatch() {
    checkPredicate(RelatePredicate.matches("1***T*0**"), "1**.*2*.0**", true);
  }

  //=======================================================
  
  private void checkPredicate(TopologyPredicate pred, String im, boolean expected) {
    applyIM(im, pred);
    checkPred(pred, expected);    
  }
  
  private void checkPredicatePartial(TopologyPredicate pred, String im, boolean expected) {
    applyIM(im, pred);
    boolean isKnown = pred.isKnown();
    assertTrue("predicate value is not known", isKnown);
    checkPred(pred, expected);
  }

  private void checkPred(TopologyPredicate pred, boolean expected) {
    pred.finish();
    boolean actual = pred.value();
    assertEquals(expected, actual);
  }
  
  private static void applyIM(String imIn, TopologyPredicate pred) {
    String im = cleanIM(imIn);
    for (int i = 0; i < 9; i++) {
      int locA = i / 3;
      int locB = i - 3 * locA;
      char entry = im.charAt(i);
      if (entry == '0' || entry == '1' || entry == '2') {
        int dim = Dimension.toDimensionValue(entry);
        pred.updateDimension(locA, locB, dim);
      }
    }
  }

  private static String cleanIM(String im) {
    String im1 = im.replaceAll("\\.", "");
    return im1;
  }
  
}
