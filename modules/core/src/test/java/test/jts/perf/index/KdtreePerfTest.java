package test.jts.perf.index;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class KdtreePerfTest extends PerformanceTestCase {

	private List<Coordinate> points;
	private KdTree tree;
	private Coordinate query;
	private int k;

	public static void main(String[] args) {
		PerformanceTestRunner.run(KdtreePerfTest.class);
	}

	public KdtreePerfTest(String name) {
		super(name);
		setRunSize(new int[] { 100_000, 1_000_000, 5_000_000 });
		setRunIterations(1);
	}

	@Override
	public void startRun(int size) throws Exception {
		Random rnd = new Random(12345);
		points = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			points.add(new Coordinate(rnd.nextDouble(), rnd.nextDouble()));
		}
		tree = new KdTree(); // empty tree
		query = new Coordinate(rnd.nextDouble(), rnd.nextDouble());
		k = Math.max(1, size / 100);
		
		for (Coordinate pt : points) {
			tree.insert(pt);
		}
	}

	/**
	 * Do a single k-NN query using the kd-tree. Framework will time this method.
	 */
	public void runKdTreeNearest() {
		@SuppressWarnings("unused")
		List<KdNode> result = tree.nearestNeighbors(query, k);
	}

	/**
	 * Do a single k-NN query by brute-force. Framework will time this method.
	 */
	public void runBruteForceNearest() {
		// make a copy of the points list
		List<Coordinate> copy = new ArrayList<>(points);
		copy.sort(Comparator.comparingDouble(query::distance));
		@SuppressWarnings("unused")
		List<Coordinate> nearest = copy.subList(0, Math.min(k, copy.size()));
	}
	
	public void runKdTreeEnvelope() {
		tree.query(new Envelope(0.25, 0.75, 0.25, 0.75));
	}
	
	public void runKdTreeEnvelopeAll() {
		tree.query(new Envelope(0, 1, 0, 1));
	}
}