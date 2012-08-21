package org.NooLab.field.repulsive.components.topology;

import java.util.*;

import math.geom2d.Point2D;

import org.NooLab.field.repulsive.components.SurroundBuffers;
import org.NooLab.field.repulsive.components.FluidFieldSurroundRetrieval;
import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.field.repulsive.components.data.RetrievalParamSet;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.field.repulsive.intf.RepulsionFieldObjectsIntf;
import org.NooLab.field.repulsive.intf.SurroundRetrievalObserverIntf;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldBasicIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;
import org.NooLab.utilities.logging.PrintLog;





abstract public class AbstractCoverage implements Runnable{

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
	
	Object graphGeomObject ;
	
	double surroundExtent;
	double figParam ;
	
	PointXY[] points;
	PointXY[] boundingBox = new PointXY[4];
	
	// --------------------------------------
	
	Thread cvgThrd;
	
	PrintLog out;
	
	// ------------------------------------------------------------------------
	public AbstractCoverage( Object parent, SurroundRetrievalObserverIntf observer, FluidFieldSurroundRetrieval srt, RetrievalParamSet rps ){
		

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
			
			boundingBox[0] = new PointXY(999999,999999); // we start top left
			boundingBox[1] = new PointXY(-999999,999999);     // top right, ...
			boundingBox[2] = new PointXY(999999,-999999); 
			boundingBox[3] = new PointXY(-999999,-999999); 
			
		}catch(Exception e){
			// String msg ="";
		}
		
	}
	// ------------------------------------------------------------------------


	public void calculate( Object obj, PointXY[] points, int task, double extent, double param ){
		
		graphGeomObject = obj ;
		
		if (task<0){
			this.task = task;
		}
		
		this.points = points;
		
		surroundExtent = extent; 
		figParam = param ;
		
		cvgThrd.start() ;
 	}
	
	
	public void forwardingResults( SurroundResults  results  ){
		
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
		
	
		map.put( resultsGuidStr, results ) ;
	
		// returning results via callback
		if (srObserver!=null){    
			srObserver.surroundRetrievalUpdate(surroundRetrieval, resultsGuidStr); 
		}
	
	}
	
	
	
	@Override
	public void run() {
	
		determineCoverage ( );
	}
	
	
	protected abstract void determineCoverage( );
	
	protected abstract boolean isInsideGeomObject( Object geomObj, Point2D testP2D) ;
	
	
	//ArrayList<Integer> pset  ArrayList<Ellipse2D> ellipses
	protected int[] getParticlesByIndexInside( ArrayList<Object> geomObjects,PointXY[] boundingBox ){
		
		ArrayList<Integer> pset = new ArrayList<Integer>()  ;
		Point2D   testP2D;
		RepulsionFieldParticle particle;
		Object geomObject;
		
		boolean covered;
		
		

		for (int i= 0; i<particles.size() ;i++){
			
			particle = particles.get(i) ;
			
			// within bounding box?   no? skip the rest , -> next particle by index value
			if ((particle.x<boundingBox[0].x-averageDistance*1.1) || (particle.x>boundingBox[1].x+averageDistance*1.1)){
				continue;
			}
			if ((particle.y<boundingBox[0].y-averageDistance*1.1) || (particle.y>boundingBox[3].y+averageDistance*1.1)){
				continue;
			}
			
			 
			
			testP2D = new Point2D( particle.x, particle.y);
			
			// test particle across all ellipses
			for (int s=0;s<geomObjects.size();s++){
				
				geomObject = geomObjects.get(s) ;
				 
				covered = isInsideGeomObject(geomObject,testP2D);
				
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
		
		
		int[] particleIndexes = new int[pset.size()] ;
		
		for (int i=0;i< pset.size();i++){
			particleIndexes[i] = pset.get(i) ;
		}
		return particleIndexes;
	}
	
	
	protected PointXY[] adjustBoundingBox( PointXY[] boundingBox, PointXY p){
		
		
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
	
	protected double distance( PointXY startpoint , PointXY endpoint ) {
		
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
