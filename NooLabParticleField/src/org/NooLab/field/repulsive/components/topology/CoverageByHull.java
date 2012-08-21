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
import math.geom2d.polygon.Polygon2D;

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
public class CoverageByHull extends AbstractCoverage {
 
	Polygon2D polygonCH;
	
	
	// ------------------------------------------------------------------------
	public CoverageByHull( Object parent, SurroundRetrievalObserverIntf observer, FluidFieldSurroundRetrieval srt, RetrievalParamSet rps) {
		super( parent, observer, srt, rps);
		
		
	}
	// ------------------------------------------------------------------------

	
 


	protected boolean isInsideGeomObject( Object geomObj, Point2D testP2D){
		
		Polygon2D poly = (Polygon2D)geomObj;
		return 	poly.getBoundary().isInside(testP2D);
		
	}

	// determineCoverageByHull
	protected void determineCoverage( ) {
	
		ArrayList<Integer> pset  ;	
		
		
		Polygon2D polygonCH = (Polygon2D)graphGeomObject ;
		
		ArrayList<Object> geomObjects = new ArrayList<Object>();
		
		if (results==null){
			results = new SurroundResults();
		}
		
		results.objects.add( new ResultObjekt( "P",polygonCH.getBoundary() )) ;
		                                      //   -> CirculinearContourArray2D
		
		geomObjects.add(polygonCH) ;
		
		Map<String, Object> map = surroundRetrieval.getResultMap();
		if (map==null){
			surroundRetrieval.setResultMap( new HashMap<String, Object>()) ;
			map = surroundRetrieval.getResultMap();
		}
		if (rps==null){
			rps = new RetrievalParamSet();
			rps.task = task; 
			
		}

		// get bounding box
		
		for (int i=0;i<points.length;i++){
			boundingBox = adjustBoundingBox( boundingBox, points[i]); 
		}
		
		// check particles... note that the "inside" method is defined as abstract, 
		// and actually implemented in this class as "isInsideGeomObject()"
		results.setParticleIndexes( getParticlesByIndexInside( geomObjects, boundingBox ) );

		forwardingResults( results );
			
	}
 



}
