package test.jts.perf.math;

import java.math.*;

import com.vividsolutions.jts.math.DD;
import com.vividsolutions.jts.util.*;

/**
 * Times evaluating floating-point expressions using 
 * various extended precision APIs.
 * 
 * @author Martin Davis
 *
 */
public class DDExpressionPerf 
{

	public static void main(String[] args) throws Exception
	{
		  DDExpressionPerf test = new DDExpressionPerf();
	    test.run();
	}

	public DDExpressionPerf() { }
	
	public void run() {
	  int n = 1000000;
		double doubleTime   = runDouble(n);
		double ddTime       = runDoubleDouble(n);
		double ddSelfTime   = runDoubleDoubleSelf(n);
		double bigDecTime   = runBigDecimal(n);
		
		System.out.println("BigDecimal VS double performance factor = " + bigDecTime/doubleTime);
		System.out.println("BigDecimal VS DD performance factor = " + bigDecTime/ddTime);
		
		System.out.println("DD VS double performance factor = " + ddTime/doubleTime);
		System.out.println("DD-Self VS double performance factor = " + ddSelfTime/doubleTime);
		
	}
	
	public double runDouble(int nIter)
	{
		Stopwatch sw = new Stopwatch();
		for (int i = 0; i < nIter; i++) {
      double a = 9.0;
      double factor = 10.0;
			
			double aMul = factor * a;
			double aDiv = a / factor;
			
			double det = a * a - aMul * aDiv;
//			System.out.println(det);
		}
		sw.stop();
		System.out.println("double:          nIter = " + nIter 
				+ "   time = " + sw.getTimeString());
		return sw.getTime() / (double) nIter;
	}
	
	
	public double runBigDecimal(int nIter)
	{
		Stopwatch sw = new Stopwatch();
		for (int i = 0; i < nIter; i++) {
			
      BigDecimal a = (new BigDecimal(9.0)).setScale(20);
      BigDecimal factor = (new BigDecimal(10.0)).setScale(20);
			BigDecimal aMul = factor.multiply(a);
			BigDecimal aDiv = a.divide(factor, BigDecimal.ROUND_HALF_UP);
			
			BigDecimal det = a.multiply(a)
					.subtract(aMul.multiply(aDiv));
//			System.out.println(aDiv);
//			System.out.println(det);
		}
		sw.stop();
		System.out.println("BigDecimal:      nIter = " + nIter 
				+ "   time = " + sw.getTimeString());
		return sw.getTime() / (double) nIter;
	}
	
  public double runDoubleDouble(int nIter)
  {
    Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {
      
      DD a = new DD(9.0);
      DD factor = new DD(10.0);
      DD aMul = factor.multiply(a);
      DD aDiv = a.divide(factor);
      
      DD det = a.multiply(a)
          .subtract(aMul.multiply(aDiv));
//      System.out.println(aDiv);
//      System.out.println(det);
    }
    sw.stop();
    System.out.println("DD:              nIter = " + nIter 
        + "   time = " + sw.getTimeString());
    return sw.getTime() / (double) nIter;
  }
  
  public double xrunDoubleDoubleSelf(int nIter)
  {
    Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {
      
      DD a = new DD(9.0);
      DD factor = new DD(10.0);
      DD aMul = factor.multiply(a);
      DD aDiv = a.divide(factor);
      
      DD det = a.multiply(a)
          .subtract(aMul.multiply(aDiv));
//      System.out.println(aDiv);
//      System.out.println(det);
    }
    sw.stop();
    System.out.println("DD:              nIter = " + nIter 
        + "   time = " + sw.getTimeString());
    return sw.getTime() / (double) nIter;
  }
  
	//*
	public double runDoubleDoubleSelf(int nIter)
	{
		Stopwatch sw = new Stopwatch();
		for (int i = 0; i < nIter; i++) {
			
      double a = 9.0;
      double factor = 10.0;
			DD c = new DD(9.0);
			c.selfMultiply(factor);
			DD b = new DD(9.0);
			b.selfDivide(factor);
			
			DD a2 = new DD(a);
			a2.selfMultiply(a);
			DD b2 = new DD(b);
			b2.selfMultiply(c);
			a2.selfDivide(b2);
			DD det = a2;
//			System.out.println(aDiv);
//			System.out.println(det);
		}
		sw.stop();
		System.out.println("DD-Self:         nIter = " + nIter 
				+ "   time = " + sw.getTimeString());
		return sw.getTime() / (double) nIter;
	}
	//*/
	
}