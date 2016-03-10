/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.math;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests basic arithmetic operations for {@link DD}s.
 * 
 * @author Martin Davis
 *
 */
public class DDBasicTest 
  extends TestCase
{
  public static void main(String args[]) {
      TestRunner.run(DDBasicTest.class);
    }

  public DDBasicTest(String name) { super(name); }

  public void testNaN()
  {
  	assertTrue(DD.valueOf(1).divide(DD.valueOf(0)).isNaN());
  	assertTrue(DD.valueOf(1).multiply(DD.NaN).isNaN());
  }
  

  public void testAddMult2()
  {
  	checkAddMult2(new DD(3));
  	checkAddMult2(DD.PI);
  }
  
  public void testMultiplyDivide()
  {
  	checkMultiplyDivide(DD.PI, DD.E, 1e-30);
  	checkMultiplyDivide(DD.TWO_PI, DD.E, 1e-30);
  	checkMultiplyDivide(DD.PI_2, DD.E, 1e-30);
  	checkMultiplyDivide(new DD(39.4), new DD(10), 1e-30);
  }

  public void testDivideMultiply()
  {
  	checkDivideMultiply(DD.PI, DD.E, 1e-30);
  	checkDivideMultiply(new DD(39.4), new DD(10), 1e-30);
  }
   
  public void testSqrt()
  {
  	// the appropriate error bound is determined empirically
  	checkSqrt(DD.PI, 1e-30);
  	checkSqrt(DD.E, 1e-30);
  	checkSqrt(new DD(999.0), 1e-28);
  }

  private void checkSqrt(DD x, double errBound)
  {
  	DD sqrt = x.sqrt();
  	DD x2 = sqrt.multiply(sqrt);
  	checkErrorBound("Sqrt", x, x2, errBound);
  }

  public void testTrunc()
  {
  	checkTrunc(DD.valueOf(1e16).subtract(DD.valueOf(1)),
  			DD.valueOf(1e16).subtract(DD.valueOf(1)));
  	// the appropriate error bound is determined empirically
  	checkTrunc(DD.PI, DD.valueOf(3));
  	checkTrunc(DD.valueOf(999.999), DD.valueOf(999));
  	
  	checkTrunc(DD.E.negate(), DD.valueOf(-2));
  	checkTrunc(DD.valueOf(-999.999), DD.valueOf(-999));
  }

  private void checkTrunc(DD x, DD expected)
  {
  	DD trunc = x.trunc();
  	boolean isEqual = trunc.equals(expected);
  	assertTrue(isEqual);
  }

  public void testPow()
  {
  	checkPow(0, 3, 16 * DD.EPS);
  	checkPow(14, 3, 16 * DD.EPS);
  	checkPow(3, -5, 16 * DD.EPS);
  	checkPow(-3, 5, 16 * DD.EPS);
  	checkPow(-3, -5, 16 * DD.EPS);
  	checkPow(0.12345, -5, 1e5 * DD.EPS);
  }
  
  public void testReciprocal()
  {
  	// error bounds are chosen to be "close enough" (i.e. heuristically)
  	
  	// for some reason many reciprocals are exact
  	checkReciprocal(3.0, 0);
  	checkReciprocal(99.0, 1e-29);
  	checkReciprocal(999.0, 0);
  	checkReciprocal(314159269.0, 0);
  }
  
  public void testBinom()
  {
  	checkBinomialSquare(100.0, 1.0);
  	checkBinomialSquare(1000.0, 1.0);
  	checkBinomialSquare(10000.0, 1.0);
  	checkBinomialSquare(100000.0, 1.0);
  	checkBinomialSquare(1000000.0, 1.0);  	
  	checkBinomialSquare(1e8, 1.0);
  	checkBinomialSquare(1e10, 1.0);
  	checkBinomialSquare(1e14, 1.0);
  	// Following call will fail, because it requires 32 digits of precision
//  	checkBinomialSquare(1e16, 1.0);
  	
  	checkBinomialSquare(1e14, 291.0);
  	checkBinomialSquare(5e14, 291.0);
  	checkBinomialSquare(5e14, 345291.0);
  }

  private void checkAddMult2(DD dd)
  {
  	DD sum = dd.add(dd);
  	DD prod = dd.multiply(new DD(2.0));
  	checkErrorBound("AddMult2", sum, prod, 0.0);
  }

  private void checkMultiplyDivide(DD a, DD b, double errBound)
  {
  	DD a2 = a.multiply(b).divide(b);
  	checkErrorBound("MultiplyDivide", a, a2, errBound);
  }

  private void checkDivideMultiply(DD a, DD b, double errBound)
  {
  	DD a2 = a.divide(b).multiply(b);
  	checkErrorBound("DivideMultiply", a, a2, errBound);
  }

  private DD delta(DD x, DD y)
  {
  	return x.subtract(y).abs();
  }
  
  private void checkErrorBound(String tag, DD x, DD y, double errBound)
  {
  	DD err = x.subtract(y).abs();
  	//System.out.println(tag + " err=" + err);
  	boolean isWithinEps = err.doubleValue() <= errBound;
  	assertTrue(isWithinEps);
  }
  
  /**
   * Computes (a+b)^2 in two different ways and compares the result.
   * For correct results, a and b should be integers.
   * 
   * @param a
   * @param b
   */
  void checkBinomialSquare(double a, double b)
  {
  	// binomial square
  	DD add = new DD(a);
  	DD bdd = new DD(b);
  	DD aPlusb = add.add(bdd);
  	DD abSq = aPlusb.multiply(aPlusb);
//  	System.out.println("(a+b)^2 = " + abSq);
  	
  	// expansion
  	DD a2dd = add.multiply(add);
  	DD b2dd = bdd.multiply(bdd);
  	DD ab = add.multiply(bdd);
  	DD sum = b2dd.add(ab).add(ab);
  	
//  	System.out.println("2ab+b^2 = " + sum);
  	
  	DD diff = abSq.subtract(a2dd);
//  	System.out.println("(a+b)^2 - a^2 = " + diff);
  	
  	DD delta = diff.subtract(sum);
  	
  	//System.out.println();
  	//System.out.println("A = " + a + ", B = " + b);
  	//System.out.println("[DD]     2ab+b^2 = " + sum
  	//		+ "   (a+b)^2 - a^2 = " + diff
  	//		+ "   delta = " + delta);
  	printBinomialSquareDouble(a,b);

  	boolean isSame = diff.equals(sum);
  	assertTrue(isSame);
  	boolean isDeltaZero = delta.isZero();
  	assertTrue(isDeltaZero);
  }
  
  void printBinomialSquareDouble(double a, double b)
  {
  	double sum = 2*a*b + b*b;
  	double diff = (a + b) * (a + b) - a*a;
  	//System.out.println("[double] 2ab+b^2= " + sum
  	//		+ "   (a+b)^2-a^2= " + diff
  	//		+ "   delta= " + (sum - diff));
  }
  
  public void testBinomial2()
  {
  	checkBinomial2(100.0, 1.0);
  	checkBinomial2(1000.0, 1.0);
  	checkBinomial2(10000.0, 1.0);
  	checkBinomial2(100000.0, 1.0);
  	checkBinomial2(1000000.0, 1.0);  	
  	checkBinomial2(1e8, 1.0);
  	checkBinomial2(1e10, 1.0);
  	checkBinomial2(1e14, 1.0);
  	
  	checkBinomial2(1e14, 291.0);
  	
  	checkBinomial2(5e14, 291.0);
  	checkBinomial2(5e14, 345291.0);
  }

  void checkBinomial2(double a, double b)
  {
  	// binomial product
  	DD add = new DD(a);
  	DD bdd = new DD(b);
  	DD aPlusb = add.add(bdd);
  	DD aSubb = add.subtract(bdd);
  	DD abProd = aPlusb.multiply(aSubb);
//  	System.out.println("(a+b)^2 = " + abSq);
  	
  	// expansion
  	DD a2dd = add.multiply(add);
  	DD b2dd = bdd.multiply(bdd);
  	
//  	System.out.println("2ab+b^2 = " + sum);
  	
  	// this should equal b^2
  	DD diff = abProd.subtract(a2dd).negate();
//  	System.out.println("(a+b)^2 - a^2 = " + diff);
  	
  	DD delta = diff.subtract(b2dd);
  	
  	//System.out.println();
  	//System.out.println("A = " + a + ", B = " + b);
  	//System.out.println("[DD] (a+b)(a-b) = " + abProd
  	//		+ "   -((a^2 - b^2) - a^2) = " + diff
  	//		+ "   delta = " + delta);
//  	printBinomialSquareDouble(a,b);

  	boolean isSame = diff.equals(b2dd);
  	assertTrue(isSame);
  	boolean isDeltaZero = delta.isZero();
  	assertTrue(isDeltaZero);
  }
  

  private void checkReciprocal(double x, double errBound)
  {
  	DD xdd = new DD(x);
  	DD rr = xdd.reciprocal().reciprocal();
  	
  	double err = xdd.subtract(rr).doubleValue();
  	
  	//System.out.println("DD Recip = " + xdd 
  	//		+ " DD delta= " + err
  	//		+ " double recip delta= " + (x - 1.0/(1.0/x)) );
  	
  	assertTrue(err <= errBound);
  }
 
  private void checkPow(double x, int exp, double errBound)
  {
  	DD xdd = new DD(x);
  	DD pow = xdd.pow(exp);
  	//System.out.println("Pow(" + x + ", " + exp + ") = " + pow);
  	DD pow2 = slowPow(xdd, exp);
  	
  	double err = pow.subtract(pow2).doubleValue();
  	
  	boolean isOK = err < errBound;
  	if (! isOK)
  		System.out.println("Test slowPow value " + pow2);
  		
  	assertTrue(err <= errBound);
  }
 
	private DD slowPow(DD x, int exp)
	{
		if (exp == 0)
			return DD.valueOf(1.0);
		
		int n = Math.abs(exp);
		// MD - could use binary exponentiation for better precision & speed
		DD pow = new DD(x);
		for (int i = 1; i < n; i++) {
			pow = pow.multiply(x);
		}
		if (exp < 0) {
			return pow.reciprocal();
		}
		return pow;
	}

	

}