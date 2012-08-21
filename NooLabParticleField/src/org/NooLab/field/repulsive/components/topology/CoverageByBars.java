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
import org.NooLab.field.repulsive.components.FluidFieldSurroundRetrieval;
import org.NooLab.field.repulsive.components.data.LineXY;
import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.field.repulsive.components.data.RetrievalParamSet;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.field.repulsive.intf.SurroundRetrievalObserverIntf;
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
public class CoverageByBars extends AbstractCoverage{

	  
	
	// --------------------------------------
	
	LineXY[] treeLines;

	
	// --------------------------------------
	
	Thread cvgThrd;
	
	PrintLog out;
	
	// ------------------------------------------------------------------------
	public CoverageByBars( Object parent, SurroundRetrievalObserverIntf observer, FluidFieldSurroundRetrieval srt, RetrievalParamSet rps) {
		super( parent, observer, srt, rps);
		 
	}
	// ------------------------------------------------------------------------

 

	// determineCoverageByLines
	protected void determineCoverage() {
	
		
		ArrayList<Integer> pset  ;	
		
		PointXY startpoint , endpoint ;
	
		
		TreeLinesIntf treelines = (TreeLinesIntf)graphGeomObject;
		
		PointXYIntf[] ppxy;
		
		treeLines = new LineXY[treelines.size()];
		
		
		for (int i=0;i<treelines.size();i++){
			if (treelines.getItem(i)!=null){
				
				ppxy = treelines.getItem(i).getPpointXY() ;
				treeLines[i] = new LineXY( ppxy[0].getX(), ppxy[0].getY(),ppxy[1].getX(),ppxy[1].getY());
				treeLines[i].setIndex(i) ;
			}
		}
		
		if (results==null){
			results = new SurroundResults();
			
		}
		
		startpoint = new PointXY();
		endpoint = new PointXY();
		
		// we should test all ellipses in a single approach, since the ellipses are always much
		// smaller than the collection of particles
		
		if (surroundExtent<=0.000001){
			// distance constantly < 0.32 * averageDistance 
			results.setParticleIndexes( getParticlesCloseToLine( startpoint, endpoint) );
			
		}else{
			 
			results.setParticleIndexes( getParticlesWithinEllipses(treeLines) );
		}
		
		forwardingResults( results  );
		
		/*
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
		 */
		
	}

	

	protected boolean isInsideGeomObject( Object geomObj, Point2D testP2D){
		
		Ellipse2D ellipse = (Ellipse2D)geomObj;
		return 	ellipse.isInside(testP2D);
	}
	
	
	private int[] getParticlesWithinEllipses( LineXY[] lines) {
		
		ArrayList<Integer> pset = new ArrayList<Integer>();
		ArrayList<Integer> pset2 = new ArrayList<Integer>();
		boolean covered;
		 
		PointXY startpoint, endpoint;
		Point2D   testP2D;
		RepulsionFieldParticle particle;
		Ellipse2D ellipse;
		
		
		// ArrayList<Ellipse2D> ellipses = new ArrayList<Ellipse2D>();
		ArrayList<Object> geomObjects = new ArrayList<Object>();
		 
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
			// ellipses.add(ellipse) ;
			
			geomObjects.add(ellipse) ;
			
			results.objects.add( new ResultObjekt( "E",ellipse.clone())) ;
			
			boundingBox = adjustBoundingBox( boundingBox, startpoint); // PointXY
			boundingBox = adjustBoundingBox( boundingBox, endpoint);
		} // i ->
		
		// looking for particles that are included
		
		// first determine lowest index value ...
		
		 
		//
		return  getParticlesByIndexInside( geomObjects, boundingBox );
		  
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



	private int[] getParticlesCloseToLine(PointXY startpoint, PointXY endpoint) {
		 
		return null;
	}




}
