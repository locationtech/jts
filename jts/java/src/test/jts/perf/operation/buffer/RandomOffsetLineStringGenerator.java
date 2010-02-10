package test.jts.perf.operation.buffer;

import com.vividsolutions.jts.geom.*;


/**
 * Generates random {@link LineString}s, which are somewhat coherent
 * in terms of how far they deviate from a given line segment,
 * and how much they twist around.
 * <p>
 * The method is to recursively perturb line segment midpoints by a random offset.  
 * 
 * @author mbdavis
 *
 */
public class RandomOffsetLineStringGenerator
{
	public static Geometry generate(double maxSegLen, int numPts, GeometryFactory fact)
	{
		RandomOffsetLineStringGenerator rlg = new RandomOffsetLineStringGenerator(maxSegLen, numPts);
		return rlg.generate(fact);
	}
	
	private double maxSegLen;
	private int numPts;
	private int exponent2 = 5;
	private Coordinate[] pts;
	private Coordinate endPoint;
	
	
	public RandomOffsetLineStringGenerator(double maxSegLen, int numPts)
	{
		this.maxSegLen = maxSegLen;
		
		exponent2 = (int) (Math.log(numPts) / Math.log(2));
		int pow2 = pow2(exponent2);
		if (pow2 < numPts) 
			exponent2 += 1;
		
		this.numPts = pow2(exponent2) + 1;
	}

	public Geometry generate(GeometryFactory fact)
	{
		pts = new Coordinate[numPts];
		
		pts[0] = new Coordinate();
		
		double ang = Math.PI * Math.random();
		endPoint = new Coordinate(maxSegLen * Math.cos(ang),maxSegLen * Math.sin(ang));
		pts[numPts - 1] = endPoint;
		
		
		int interval = numPts / 2;
		while (interval >= 1) {
			createRandomOffsets(interval);
			interval /= 2;
		}
		return fact.createLineString(pts);
	}
	
	private void createRandomOffsets(int interval)
	{
//		for (int i = 0; i )
		int inc = pow2(exponent2);
		
		while (inc > 1) {
			computeRandomOffsets(inc);
			inc /= 2;
		}
	}
	
	private void computeRandomOffsets(int inc)
	{
		int inc2 = inc / 2;
		for (int i = 0; i + inc2 < numPts; i += inc) {
			int midIndex = i + inc2;
			int endIndex = i + inc;
			
			Coordinate segEndPoint;
			
			double segFrac = 0.5 + randomFractionPerturbation();
			
			if (endIndex >= numPts) {
				segEndPoint = endPoint;
				segFrac = midIndex / numPts;
			}
			else {
				segEndPoint = pts[i + inc];
			}
			pts[midIndex] = computeRandomOffset(pts[i], segEndPoint, segFrac);
		}
	}
	
	private Coordinate computeRandomOffset(Coordinate p0, Coordinate p1, double segFrac)
	{
		double len = p0.distance(p1);
		double len2 = len / 2;
		double offsetLen = (len * Math.random()) - len2;
		LineSegment seg = new LineSegment(p0, p1);
		return seg.pointAlongOffset(segFrac, offsetLen);
	}
	
	private double randomFractionPerturbation()
	{
		double rnd = Math.random();
		double mag = rnd * rnd * rnd;
		int sign = Math.random() > 0.5 ? 1 : -1;
		return sign * mag;
	}
	
	private static int pow2(int exponent)
	{
		int pow2 = 1;
		for (int i = 0; i < exponent; i++) {
			pow2 *= 2;
		}
		return pow2;
	}

}
