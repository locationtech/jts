package org.locationtech.jts.operation.union;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class DisjointSetsTest extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(DisjointSetsTest.class);
  }
  
  public DisjointSetsTest(String name) {
    super(name);
  }
  
  public void testIntsModulo3() {
    int[] nums = new int[] {
        11,22,3,45,5,62,7
    };
    checkIntsModulo(nums, 3, new String[] {
        "3,45",
        "11,5,62",
        "22,7"
    });
  }
  
  public void testIntsModulo2() {
    int[] nums = new int[] {
        11,22,3,45,5,62,7
    };
    checkIntsModulo(nums, 2, new String[] {
        "22,62",
        "11,3,45,5,7"
    });
  }
  
  public void checkIntsModulo(int[] nums, int modulus, String[] setsExpected) {
    DisjointSets dset = new DisjointSets(nums.length);
    for (int i = 1; i < nums.length; i++) {
      for (int j = 0; j < i; j++) {
        if (nums[j] % modulus == nums[i] % modulus) {
          dset.merge(i,  j);
        }
      }
    }
    String[] sets = dumpSets(nums, dset);
    assertEquals(setsExpected.length, sets.length);
    for (int i = 0; i < sets.length; i++) {
      assertEquals(setsExpected[i], sets[i]);
    }
  }

  private String[] dumpSets(int[] nums, DisjointSets dset) {
    int nSet = dset.getNumSets();
    //System.out.println("# Sets = " + nSet);
    String[] sets = new String[nSet];
    for (int s = 0; s < nSet; s++) {
      //System.out.println("---- Set " + s);
      int size = dset.getSetSize(s);
      String setStr = "";
      for (int si = 0; si < size; si++) {
        int itemIndex = dset.getSetItem(s, si);
        if (si > 0) setStr += ",";
        setStr += nums[itemIndex];
      }
      sets[s] = setStr;
      
      System.out.println(setStr);
    }
    return sets;
  }
}
