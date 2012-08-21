package org.NooLab.field.repulsive.components.topology;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

 
import math.geom2d.Angle2D;
import math.geom2d.Point2D;
import math.geom2d.conic.Ellipse2D;
import math.geom2d.line.Line2D;

import org.NooLab.field.repulsive.components.Neighborhood;
import org.NooLab.field.repulsive.components.SurroundBuffers;
import org.NooLab.field.repulsive.components.FluidFieldSurroundRetrieval;
import org.NooLab.field.repulsive.components.data.LineXY;
import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.field.repulsive.components.data.RetrievalParamSet;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.field.repulsive.intf.RepulsionFieldObjectsIntf;
import org.NooLab.field.repulsive.intf.SurroundRetrievalObserverIntf;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldBasicIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;
import org.NooLab.graph.PointXYIntf;
import org.NooLab.graph.TreeLinesIntf;
import org.NooLab.utilities.logging.PrintLog;
 



/**
 * 
 * http://geom-java.sourceforge.net/api/overview-summary.html
 * 
 * 
 * @author kwa
 *
 */
public class _CoverageByBars implements Runnable{

	 
	
	RepulsionFieldBasicIntf parentField;
	RepulsionFieldObjectsIntf fieldObjects;
	
	SurroundBuffers surroundBuffers;
	
	RepFieldParticlesIntf particles;
	
	SurroundRetrievalObserverIntf  srObserver;
	String resultsGuidStr = "" ;
	
	SurroundResults results ; 
	
	FluidFieldSurroundRetrieval surroundRetrieval;
	RetrievalParamSet rps;
	int task = -1;
	
	double averageDistance ;
	
	
	// --------------------------------------
	
	LineXY[] treeLines;
	double surroundExtent;
	double figParam ;
	
	// --------------------------------------
	
	Thread cvgThrd;
	
	PrintLog out;
	
	// ------------------------------------------------------------------------
	public _CoverageByBars( Object parent, SurroundRetrievalObserverIntf observer, FluidFieldSurroundRetrieval srt, RetrievalParamSet rps) {
		String guid = rps.guid ; 
		
		try{
			
			parentField = (RepulsionFieldBasicIntf)parent;
			particles = parentField.getParticles();
			 
			fieldObjects = (RepulsionFieldObjectsIntf)parent ;
			 
			surroundRetrieval = srt;
			this.rps = rps;
			task = rps.task ;
			
			srObserver = surroundRetrieval.getSurroundRetrievalObserver();
			
			// the receiver will call 
			// SurroundResults result = (SurroundResults) Observable.getResultsByGuid(guid)
			// in its calback method
			
			surroundBuffers = fieldObjects.getSurroundBuffers() ;
			out = surroundBuffers.out ;
			
			results = (SurroundResults) surroundRetrieval.getResultsByGuid(guid) ;
			
			averageDistance = particles.getAverageDistance() ;
			
			resultsGuidStr = guid;
			 
			cvgThrd = new Thread(this,"cvgThrd");
			
		}catch(Exception e){
			// String msg ="";
		}
		
		
	}
	// ------------------------------------------------------------------------


	public void calculate( TreeLinesIntf treelines , int task, double extent, double param ){
		
		PointXYIntf[] ppxy;
		
		treeLines = new LineXY[treelines.size()];
		if (task<0){
			this.task = task;
		}
		
		for (int i=0;i<treelines.size();i++){
			if (treelines.getItem(i)!=null){
				
				ppxy = treelines.getItem(i).getPpointXY() ;
				treeLines[i] = new LineXY( ppxy[0].getX(), ppxy[0].getY(),ppxy[1].getX(),ppxy[1].getY());
				treeLines[i].setIndex(i) ;
			}
		}
			
		surroundExtent = extent; 
		figParam = param ;
		
		cvgThrd.start() ;
 	}

	
	@Override
	public void run() {
	
		determineCoverageByLines( );
	}




	private void determineCoverageByLines( ) {
	
		
		ArrayList<Integer> pset  ;	
		
		PointXY startpoint , endpoint ;
	
		
		if (results==null){
			results = new SurroundResults();
			
		}
		
		startpoint = new PointXY();
		endpoint = new PointXY();
		
		// we should test all ellipses in a single approach, since the ellipses are always much
		// smaller than the collection of particles
		
		if (surroundExtent<=0.000001){
			// distance constantly < 0.32 * averageDistance 
			 pset = getParticlesCloseToLine( startpoint, endpoint);
			
		}else{
			// pset = getParticlesWithinEllipse( startpoint, endpoint);
			pset = getParticlesWithinEllipses(treeLines);
		}
		
		
		int[] particleIndexes = new int[pset.size()] ;
		
		for (int i=0;i< pset.size();i++){
			particleIndexes[i] = pset.get(i) ;
		}
		
		 
		
		Map<String, Object> map = surroundRetrieval.getResultMap();
		if (map==null){
			surroundRetrieval.setResultMap( new HashMap<String, Object>()) ;
			map = surroundRetrieval.getResultMap();
		}
		if (rps==null){
			rps = new RetrievalParamSet();
			rps.task = task; 
			
		}

		results.setParamSet(rps) ;
		results.setParticleIndexes(particleIndexes);
	
		map.put( resultsGuidStr, results ) ;
	
		// returning results via callback
		if (srObserver!=null){    
			srObserver.surroundRetrievalUpdate(surroundRetrieval, resultsGuidStr); 
		}
	
		
	}

	

	private Ellipse2D imposeEllipseAroundLine(PointXY startpoint, PointXY endpoint){
		
		Ellipse2D ellipse = null;
		
		double d,minAxis, theta;
		Point2D  middlePoint;
		Line2D majorAxis ; 
		
		
		d = distance( startpoint , endpoint );
		if (d<0.001){
			return null;
		}
		
        // Main constructor: define center by a point plus major and minor semi axis, and orientation angle.
		majorAxis = new Line2D(startpoint.x,startpoint.y , endpoint.x, endpoint.y);
		
        // Returns the horizontal angle formed by the line joining the two given points.
		theta = Angle2D.getHorizontalAngle( majorAxis ) ;
		
		// the lines middle point, it will be also the center of the ellipse
		middlePoint = new Point2D( (startpoint.x + endpoint.x)/2.0, (startpoint.y + endpoint.y)/2.0) ;
		 
		minAxis =  averageDistance * (averageDistance * surroundExtent)/d ;
		 
		ellipse = new Ellipse2D( middlePoint, d, minAxis , theta ); 
		
		
		return ellipse;
	}
	
	private PointXY[] adjustBoundingBox( PointXY[] boundingBox, PointXY p){
		
		
		if ( boundingBox[0].x > p.x){
			boundingBox[0].x = p.x;   
			boundingBox[3].x = p.x;
		}
		if ( boundingBox[1].x < p.x){
			boundingBox[1].x = p.x;
			boundingBox[2].x = p.x;
		}
		if ( boundingBox[0].y > p.y){
			boundingBox[0].y = p.y;   
			boundingBox[1].y = p.y;
		}
		if ( boundingBox[2].y < p.y){
			boundingBox[2].y = p.y;
			boundingBox[3].y = p.y;
		}
		return boundingBox;  
	}
	
	private ArrayList<Integer> getParticlesWithinEllipses( LineXY[] lines) {
		
		ArrayList<Integer> pset = new ArrayList<Integer>();
		boolean covered;
		 
		PointXY startpoint, endpoint;
		Point2D   testP2D;
		RepulsionFieldParticle particle;
		Ellipse2D ellipse;
		PointXY[] boundingBox = new PointXY[4];
		
		ArrayList<Ellipse2D> ellipses = new ArrayList<Ellipse2D>();
		
		
		boundingBox[0] = new PointXY(999999,999999); // we start top left
		boundingBox[1] = new PointXY(-999999,999999);     // top right, ...
		boundingBox[2] = new PointXY(999999,-999999); 
		boundingBox[3] = new PointXY(-999999,-999999); 
		
		startpoint = new PointXY();
		endpoint = new PointXY();
		
		// preparing all ellipses, also determining the bounding box (as 4 PointXY) which includes all patricles
		// in parallel, we determin the bounding box (min,max of x,y)
		
		for (int i=0;i<lines.length;i++){
		
			startpoint.x =  lines[i].x[0];
			startpoint.y =  lines[i].y[0];

			endpoint.x =  lines[i].x[1];
			endpoint.y =  lines[i].y[1];
			
			ellipse = imposeEllipseAroundLine( startpoint, endpoint) ;
			ellipses.add(ellipse) ;
			results.objects.add( new ResultObjekt( "E",ellipse.clone())) ;
			
			boundingBox = adjustBoundingBox( boundingBox, startpoint);
			boundingBox = adjustBoundingBox( boundingBox, endpoint);
		} // i ->
		
		// looking for particles that are included
		
		// first determine lowest index value ...
		
		// 
		for (int i= 0; i<particles.size() ;i++){
			
			particle = particles.get(i) ;
			
			if ((particle.x<boundingBox[0].x-averageDistance*1.1) || (particle.x>boundingBox[1].x+averageDistance*1.1)){
				continue;
			}
			if ((particle.y<boundingBox[0].y-averageDistance*1.1) || (particle.y>boundingBox[3].y+averageDistance*1.1)){
				continue;
			}
			// within bounding box?
			
			// no? skip the rest , -> next index value
			// 
			
			testP2D = new Point2D( particle.x, particle.y);
			
			// test particle across all ellipses
			for (int s=0;s<ellipses.size();s++){
				ellipse = ellipses.get(s) ;
				
				
				covered = ellipse.isInside(testP2D);
				if (covered) {
					if (pset.contains(i)==false){
						pset.add(i) ;
					}
					// out.print(2, "+++ particle added,     index:"+i+", location x,y:" + df.format(particle.x)+"," + df.format(particle.y)+" +++");
				}else{
					// out.print(2, "--- particle NOT added, index:"+i+", location x,y:" + df.format(particle.x)+"," + df.format(particle.y)+" ---");
				}
			} // s->
			
		} // i->
		
		
		return pset;
	}

	/**
	 * http://geom-java.sourceforge.net/api/overview-summary.html
	 * 
	 * @param startpoint
	 * @param endpoint
	 * @return
	 */
	@SuppressWarnings("unused")
	private ArrayList<Integer> getParticlesWithinEllipse(PointXY startpoint, PointXY endpoint) {
		 
		Vector<Integer> pset = new Vector<Integer>();
		 
		Neighborhood neighborhood ; 
		int k;
		boolean covered;
		 
		DecimalFormat df = new DecimalFormat("#.##");
		Ellipse2D ellipse;
		Point2D  testP2D ;
		 
		RepulsionFieldParticle particle;
		
		ArrayList<Point2D> testedP2Ds = new ArrayList<Point2D> ();
		
		
		
		ellipse = imposeEllipseAroundLine( startpoint, endpoint) ;
		 
		if (ellipse==null){
			return (new ArrayList<Integer>());
		}
		
		// determine the infimum & supremum for the index in the particles population
		neighborhood = fieldObjects.getNeighborhood() ;
		
		// this relies on the fact that the xyPlane contains coordinates which are sorted along the x-dimension
		// a recursive half-split should provide the results pretty fast 
		int[] infsup = neighborhood.getInfimumSupremumForLocations( startpoint.x, endpoint.x);
		
		if (infsup[0]>infsup[1]){ k=infsup[0]; infsup[0]=infsup[1]; infsup[1]=k; }
		
		// preselectedParticles.size()
		// for (int i= infsup[0]; i< infsup[1];i++){
		for (int i= 0; i<particles.size() ;i++){
			//int ix = particles.get(i);
			particle = particles.get(i) ;
			
			testP2D = new Point2D( particle.x, particle.y);
			testedP2Ds.add(testP2D) ;
			covered = ellipse.isInside(testP2D);
			if (covered) {
			
		       ;
		        
				pset.add(i) ;
				out.print(2, "+++ particle added,     index:"+i+", location x,y:" + df.format(particle.x)+"," + df.format(particle.y)+" +++");
			}else{
				// out.print(2, "--- particle NOT added, index:"+i+", location x,y:" + df.format(particle.x)+"," + df.format(particle.y)+" ---");
			}
		}
		  
		
		results.objects.add( new ResultObjekt( "E",ellipse.clone())) ;
	 
		int n ;
		n = testedP2Ds.size();
		
		return (new ArrayList<Integer>(pset));
	}




	private ArrayList<Integer> getParticlesCloseToLine(PointXY startpoint, PointXY endpoint) {
		 
		return null;
	}




	private double distance( PointXY startpoint , PointXY endpoint ) {
		
		return distance( startpoint.x, startpoint.y,endpoint.x, endpoint.y ) ;
	}




	private double distance(double x1, double y1, double x2, double y2) {
		
		double d=-1.0;
		double dx,dy;
		
		dx = (x1 - x2);
		dy = (y1 - y2);
		
		d = Math.round(Math.sqrt((dx*dx) + (dy*dy)) );
		
		return d ;
	
	} // distance()
}
