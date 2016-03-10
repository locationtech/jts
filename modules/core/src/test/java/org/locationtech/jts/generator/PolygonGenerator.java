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
package org.locationtech.jts.generator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.valid.IsValidOp;

/**
 * 
 * This class is used to create a polygon within the specified bounding box.
 * 
 * Sucessive calls to create may or may not return the same geometry topology.
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class PolygonGenerator extends GeometryGenerator {
	protected int numberPoints = 4;
	protected int numberHoles = 0;
	protected int generationAlgorithm = 0;
	
	/**
	 * Creates rectangular polygons
	 */
	public static final int BOX = 0;
	
	/**
	 * Creates polygons whose points will not be rectangular when there are more than 4 points 
	 */
	public static final int ARC = 1;
	
	private static final int RUNS = 5;
	
	/**
	 * As the user increases the number of points, the probability of creating a random valid polygon decreases. 
	 * Please take not of this when selecting the generation style, and the number of points. 
	 * 
	 * May return null if a geometry could not be created.
	 * 
	 * @see #getNumberPoints()
	 * @see #setNumberPoints(int)
	 * @see #getGenerationAlgorithm()
	 * @see #setGenerationAlgorithm(int)
	 * 
	 * @see #BOX
	 * @see #ARC
	 * 
	 * @see org.locationtech.jts.generator.GeometryGenerator#create()
	 * 
	 * @throws IllegalStateException When the alg is not valid or the number of points is invalid
	 * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
	 */
	public Geometry create() {

		if(geometryFactory == null){
			throw new NullPointerException("GeometryFactory is not declared");
		}
		if(boundingBox == null || boundingBox.isNull()){
			throw new NullPointerException("Bounding Box is not declared");
		}
		if(numberPoints<4){
			throw new IllegalStateException("Too few points");
		}
		
		double x = boundingBox.getMinX(); // base x
		double dx = boundingBox.getMaxX()-x;
		
		double y = boundingBox.getMinY(); // base y
		double dy = boundingBox.getMaxY()-y;
		
		Polygon p = null;
		
		for(int i=0;i<RUNS;i++){
			switch(getGenerationAlgorithm()){
			case BOX:
				p = createBox(x,dx,y,dy,numberHoles,numberPoints,geometryFactory);
				break;
			case ARC:
				p = createArc(x,dx,y,dy,numberHoles,numberPoints,geometryFactory);
				break;
			default:
				throw new IllegalStateException("Invalid Alg. Specified");
			}
			
			IsValidOp valid = new IsValidOp(p);
			if(valid.isValid()){
				return p;
			}
		}
		return null;
	}
	
	private static Polygon createArc(double x, double dx, double y, double dy, int nholes, int npoints, GeometryFactory gf){
		// make outer ring first
		double radius = dx<dy?dx/3:dy/3;
		
		double cx = x+(dx/2); // center
		double cy = y+(dy/2); // center
		
		LinearRing outer = createArc(cx,cy,radius,npoints,gf);
		
		if(nholes == 0){
			return gf.createPolygon(outer,null);
		}
		
		LinearRing[] inner = new LinearRing[nholes];
		
		radius *= .75;
		int degreesPerHole = 360/(nholes+1);
		int degreesPerGap = degreesPerHole/nholes;
		degreesPerGap = degreesPerGap<2?2:degreesPerGap;
		degreesPerHole = (360-(degreesPerGap*nholes))/nholes;
		
		if(degreesPerHole < 2)
			throw new RuntimeException("Slices too small for poly. Use Box alg.");
		
		int start = degreesPerGap/2;
		for(int i=0;i<nholes;i++){
			int st = start+(i*(degreesPerHole+degreesPerGap)); // start angle 
			inner[i] = createTri(cx,cy,st,st+degreesPerHole,radius,gf);
		}
		
		
		return gf.createPolygon(outer,inner);
	}
	
	private static LinearRing createTri(double cx, double cy ,int startAngle, int endAngle, double radius, GeometryFactory gf){

		Coordinate[] coords = new Coordinate[4];
		
		double fx1,fx2,fy1,fy2;
		
		double angle = Math.toRadians(startAngle);
		fx1 = Math.sin(angle)*radius; // may be neg.
		fy1 = Math.cos(angle)*radius; // may be neg.
		
		angle = Math.toRadians(endAngle);
		fx2 = Math.sin(angle)*radius; // may be neg.
		fy2 = Math.cos(angle)*radius; // may be neg.
		
		coords[0] = new Coordinate(cx,cy);
		gf.getPrecisionModel().makePrecise(coords[0]);
		coords[1] = new Coordinate(cx+fx1,cy+fy1);
		gf.getPrecisionModel().makePrecise(coords[1]);
		coords[2] = new Coordinate(cx+fx2,cy+fy2);
		gf.getPrecisionModel().makePrecise(coords[2]);
		coords[3] = new Coordinate(cx,cy);
		gf.getPrecisionModel().makePrecise(coords[3]);
		
		return gf.createLinearRing(coords);
	}
	
	private static LinearRing createArc(double cx, double cy ,double radius, int npoints, GeometryFactory gf){

		Coordinate[] coords = new Coordinate[npoints+1];
		
		double theta = 360/npoints;
		
		for(int i=0;i<npoints;i++){
			double angle = Math.toRadians(theta*i);
			
			double fx = Math.sin(angle)*radius; // may be neg.
			double fy = Math.cos(angle)*radius; // may be neg.
			
			coords[i] = new Coordinate(cx+fx,cy+fy);
			gf.getPrecisionModel().makePrecise(coords[i]);
		}
		
		coords[npoints] = new Coordinate(coords[0]);
		gf.getPrecisionModel().makePrecise(coords[npoints]);
		
		return gf.createLinearRing(coords);
	}
	
	private static Polygon createBox(double x, double dx, double y, double dy, int nholes, int npoints, GeometryFactory gf){
		// make outer ring first
		LinearRing outer = createBox(x,dx,y,dy,npoints,gf);
		
		if(nholes == 0){
			return gf.createPolygon(outer,null);
		}
		
		LinearRing[] inner = new LinearRing[nholes];
		
		int nrow = (int)Math.sqrt(nholes);
		int ncol = nholes/nrow;
		
		double ddx = dx/(ncol+1);
		double ddy = dy/(nrow+1);
		
		// spacers
		double spx = ddx/(ncol+1);
		double spy = ddy/(nrow+1);
		
		// should have more grids than required
		int cindex = 0;
		for(int i=0;i<nrow;i++){
			for(int j=0;j<ncol;j++){
				if(cindex<nholes){
					// make another box
					int pts = npoints/2;
					pts = pts<4?4:pts;
					
					inner[cindex++] = createBox(spx+x+j*(ddx+spx),ddx,spy+y+i*(ddy+spy),ddy,pts,gf);
				}
			}
		}
		
		return gf.createPolygon(outer,inner);
	}
	
	private static LinearRing createBox(double x, double dx, double y, double dy, int npoints, GeometryFactory gf){

		//figure out the number of points per side
		int ptsPerSide = npoints/4;
		int rPtsPerSide = npoints%4;
		Coordinate[] coords = new Coordinate[npoints+1];
		coords[0] = new Coordinate(x,y); // start
		gf.getPrecisionModel().makePrecise(coords[0]);
		
		int cindex = 1;
		for(int i=0;i<4;i++){ // sides
			int npts = ptsPerSide+(rPtsPerSide-->0?1:0);
			// npts atleast 1
			
			if(i%2 == 1){ // odd vert
				double cy = dy/npts;
				if(i > 1) // down
					cy *=-1;
				double tx = coords[cindex-1].x;
				double sy = coords[cindex-1].y;
				
				for(int j=0;j<npts;j++){
					coords[cindex] = new Coordinate(tx,sy+(j+1)*cy);
					gf.getPrecisionModel().makePrecise(coords[cindex++]);
				}
			}else{ // even horz
				double cx = dx/npts;
				if(i > 1) // down
					cx *=-1;
				double ty = coords[cindex-1].y;
				double sx = coords[cindex-1].x;
				
				for(int j=0;j<npts;j++){
					coords[cindex] = new Coordinate(sx+(j+1)*cx,ty);
					gf.getPrecisionModel().makePrecise(coords[cindex++]);
				}
			}
		}
		coords[npoints] = new Coordinate(x,y); // end
		gf.getPrecisionModel().makePrecise(coords[npoints]);
		
		return gf.createLinearRing(coords);
	}

	/**
	 * @return Returns the generationAlgorithm.
	 */
	public int getGenerationAlgorithm() {
		return generationAlgorithm;
	}

	/**
	 * @param generationAlgorithm The generationAlgorithm to set.
	 */
	public void setGenerationAlgorithm(int generationAlgorithm) {
		this.generationAlgorithm = generationAlgorithm;
	}

	/**
	 * @return Returns the numberHoles.
	 */
	public int getNumberHoles() {
		return numberHoles;
	}

	/**
	 * @param numberHoles The numberHoles to set.
	 */
	public void setNumberHoles(int numberHoles) {
		this.numberHoles = numberHoles;
	}

	/**
	 * @return Returns the numberPoints.
	 */
	public int getNumberPoints() {
		return numberPoints;
	}

	/**
	 * @param numberPoints The numberPoints to set.
	 */
	public void setNumberPoints(int numberPoints) {
		this.numberPoints = numberPoints;
	}
	
}
