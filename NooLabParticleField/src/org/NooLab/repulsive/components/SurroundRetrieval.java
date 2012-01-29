package org.NooLab.repulsive.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.convhull.JarvisMarch2D;

import org.NooLab.graph.TreeLinesIntf;
import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.components.data.LineXY;
import org.NooLab.repulsive.components.data.PointXY;
import org.NooLab.repulsive.components.data.RetrievalParamSet;
import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.components.topology.CoverageByBars;
import org.NooLab.repulsive.components.topology.CoverageByHull;
import org.NooLab.repulsive.components.topology.Surround;
import org.NooLab.repulsive.components.topology.geom.MinimalSpanningTree;
 
import org.NooLab.repulsive.intf.RepulsionFieldObjectsIntf;
import org.NooLab.repulsive.intf.SurroundRetrievalObserverIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldBasicIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.utilities.net.GUID;


/**
 * 
 * once instantiated, it should be avoided to send parallel tasks to this class 
 * 
 * 
 */
public class SurroundRetrieval implements Runnable {

	public static final int _TASK_PARTICLE_RX = 1;
	public static final int _TASK_PARTICLE    = 2;
	
	public static final int _TASK_SURROUND_C = 4; 
	public static final int _TASK_SURROUND_X = 5; 

	public static final int _TASK_SURROUND_MST    = 10;
	public static final int _TASK_SURROUND_CXHULL = 11;
	
	
	
	
	int callingStyle = -1;
	
	SurroundRetrievalObserverIntf  srObserver;
	
	
	RepulsionFieldCore rfCore;
	RepulsionFieldBasicIntf parentField;
	Object objParent;
	
	SurroundBuffers surroundBuffers;
	
	ParticlesIntf particles;
	
	ArrayList<RetrievalParamSet> paramSets = new ArrayList<RetrievalParamSet>(); 
	
 	Map<String,Object> resultMap = new HashMap<String,Object>();
	  
	Thread srThrd;
	int surroundTask=-1;
	
	
	
	public SurroundRetrieval( RepulsionFieldCore rfcore ){
		 
		rfCore = rfcore;
		
		objParent = rfCore;
		
		callingStyle = 1;
		
		srObserver = (SurroundRetrievalObserverIntf)rfcore ;  
		
		parentField = (RepulsionFieldBasicIntf)rfcore; 
		
		particles = parentField.getParticles();
	}
	 
	public SurroundRetrieval( Object parent , SurroundRetrievalObserverIntf observer ){
		
		if (parent==null){
			return;
		}
		
		callingStyle = 2;
		RepulsionFieldBasicIntf rfcore = (RepulsionFieldBasicIntf) parent ; 
		RepulsionFieldObjectsIntf rfObjects = (RepulsionFieldObjectsIntf)parent ;
		
		objParent = parent;
		
		srObserver = observer;
		parentField = rfcore;
		particles = parentField.getParticles();
		
		surroundBuffers = rfObjects.getSurroundBuffers() ;  
	}
	
	// for selecting a single particle, results returned via interfaced event
	public int addRetrieval(int xpos, int ypos, boolean autoselect) {
		int index=-1;

		RetrievalParamSet paramSet;

		paramSet = new RetrievalParamSet();
		 
		paramSet.xpos = xpos ;
		paramSet.ypos = ypos ;
		
		paramSet.particleIndex = -1 ;
		paramSet.surroundN  = 0 ;
		paramSet.autoselect = autoselect ;
		
		
		index = paramSets.size();
		paramSets.add(paramSet) ;
		
		return index;

	}


	public int addRetrieval( int particleIndex , int surroundN ,  
							 int selectMode, boolean autoselect){
		int index=-1;

		RetrievalParamSet paramSet;

		paramSet = new RetrievalParamSet();
		 
		paramSet.particleIndex = particleIndex ;
		paramSet.surroundN  = surroundN ;
		paramSet.selectMode = selectMode ;
		paramSet.autoselect = autoselect ;
		
		
		index = paramSets.size();
		paramSets.add(paramSet) ;
		
		return index;
	}
	
	
	public int addRetrieval( int xpos, int ypos , int surroundN ,  
			  				 int selectMode, boolean autoselect){
		
		RetrievalParamSet paramSet;
		
		paramSet = new RetrievalParamSet();
		 
		paramSet.xpos = xpos ;
		paramSet.ypos = ypos ;
		paramSet.surroundN  = surroundN ;
		paramSet.selectMode = selectMode ;
		paramSet.autoselect = autoselect ;
		
		
		int index = paramSets.size();
		paramSets.add(paramSet) ;
		return index;
	}

	
	public int addRetrieval(int[] indexes, double thickness, double cfgparam, boolean autoselect) {
		 
		RetrievalParamSet paramSet;
		
		paramSet = new RetrievalParamSet();
		 
		
		paramSet.particleIndexes = Arrays.copyOf(indexes, indexes.length) ;
		paramSet.surroundExtent  = thickness ;
		paramSet.cfgparams = new double[]{cfgparam} ;
		paramSet.autoselect = autoselect ;
		
		
		int index = paramSets.size();
		paramSets.add(paramSet) ;
		return index;
	}

	public String go( int paramSetIndex, int task , String guidStr) {
		
		surroundTask = task;
		
		paramSets.get(paramSetIndex).task = task;
		paramSets.get(paramSetIndex).guid = guidStr;
		
		srThrd = new Thread(this,"srThrd");
		 
		srThrd.start();
		
		return guidStr;
	}
	
	public String go( int paramSetIndex, int task ) {
		
		String guidStr="";
		
		guidStr = GUID.randomvalue() ;
		
		guidStr = go( paramSetIndex, task , guidStr);
		
		return guidStr;
	}
	
	
	@Override
	public void run() {
		
		surroundBuffers.out.delay(5);
		
		if (surroundTask<=_TASK_PARTICLE){
			getParticle( surroundTask );
			return;
		}
				
		
		if ((surroundTask==_TASK_SURROUND_C) || (surroundTask==_TASK_SURROUND_X)){
			getSurrounds(surroundTask);
			return;
		}
		
		if (surroundTask<=_TASK_SURROUND_MST){
			getSpanningTree();
			return;
		}
		if (surroundTask<=_TASK_SURROUND_CXHULL){
			getConvexHull();
			return;
		}
	}



	private void getParticle( int style ){
		
		int xpos, ypos;
		Particle p;
		
		Surround surround;
		SurroundResults results = new SurroundResults();
		
		if (callingStyle==1){
			surround = new Surround(rfCore);
		}else{
			surround = new Surround(parentField,surroundBuffers);
		}
		
		RetrievalParamSet rps = paramSets.get(paramSets.size()-1) ;
		
		xpos = rps.xpos ; 
		ypos = rps.ypos ;
		
		results.particleIndex = surround.getParticleAt( xpos, ypos, -1);// particles.get(0).radius );
			
 
		p = particles.get( results.particleIndex ) ;
		
		if ((results!=null) && (p!=null)){
			results.getCoordinate()[0] = p.x;
			results.getCoordinate()[1] = p.y;

			results.setGuid( rps.guid);
			results.setParamSet( rps );
			results.timeflag = System.currentTimeMillis();

			resultMap.put(rps.guid, results);

			if (srObserver!=null){
				srObserver.surroundRetrievalUpdate(this, rps.guid);
			}
			
		}
	}

	private void getSpanningTree(){
		
		if (this.callingStyle<=1){
			return;
		}
		
		
		PointXY[] points = prepareListOfPoints();
		
		if ((points==null) || (points.length==0)){
			return;
		}
		
		
		// calculate the tree  
		 
		MinimalSpanningTree mst = new MinimalSpanningTree(points);
		// get edges as pairs of indices 
		TreeLinesIntf treelines = mst.getTreeLines();
		
		
		// dependent on the settings, get coverage: 
		// we draw an ellipse (could degenerate to a line) around each edge,
		// then determine all particles that are within the ellipse or, in case of lines, close nearby
	
		RetrievalParamSet rps = paramSets.get(paramSets.size()-1) ;
		
		CoverageByBars coverage = new CoverageByBars( objParent, srObserver, this, rps);// RetrievalParamSet
		
		coverage.calculate( treelines, null, rps.task, rps.surroundExtent, rps.cfgparams[0] );
		  
		// no callback here, will be invoked in coverage object... 
	}

	private void getConvexHull(){
		
		if (this.callingStyle<=1){
			return;
		}
		 
		
		PointXY[] points = prepareListOfPoints();
		
		if ((points==null) || (points.length==0)){
			return;
		}
		
		ArrayList<Point2D> cpoints = changePointsFormat(points);
		
		
		JarvisMarch2D  jch = new JarvisMarch2D();
		Polygon2D poly = jch.convexHull(cpoints); 
		
		RetrievalParamSet rps = paramSets.get(paramSets.size()-1) ;
		
		CoverageByHull coverage = new CoverageByHull( objParent, srObserver, this, rps);// RetrievalParamSet
		
		coverage.calculate( poly, points, rps.task, rps.surroundExtent, rps.cfgparams[0] );
		  
		// no callback here, will be invoked in coverage object... which runs in its own thread
	
	}

	private void getSurrounds(int style){
		 
		SurroundResults results =null;
		
		if (paramSets.size()==0){
			return;
		}
		
		
		RetrievalParamSet p = paramSets.get(paramSets.size()-1) ;
		
		if (style== _TASK_SURROUND_C){
			results = getSurround( p.xpos, p.ypos, p.surroundN, p.selectMode,  p.autoselect);
		}
		if (style== _TASK_SURROUND_X){
			results = getSurround( p.particleIndex, p.surroundN, p.selectMode,  p.autoselect);
		}
		
		if (results!=null){
			
			
			results.setGuid( p.guid);
			results.setParamSet( p );
			 

			// put his to the result map
			resultMap.put(p.guid, results);

			srObserver.surroundRetrievalUpdate(this, p.guid);
		}
	}
	
	
	private SurroundResults getSurround( int xpos, int ypos , int surroundN ,  
			  							 int selectMode, boolean autoselect){

		SurroundResults results = new SurroundResults(); 
		Surround surround;
		
		if (callingStyle==1){
			surround = new Surround(rfCore);
		}else{
			surround = new Surround(parentField,surroundBuffers);
		}


		results.setParticleIndexes( surround.getGeometricSurround( xpos, ypos ,surroundN,Surround._CIRCLE ) ); 
		results.setParticleDistances( surround.getParticleDistances() );
		
		return results;
		
	}
	
	private SurroundResults getSurround( int index , int surroundN ,  
				 					     int selectMode, boolean autoselect){
		
		SurroundResults results = new SurroundResults(); 
		 
		Surround surround;
		
		if (callingStyle==1){
			surround = new Surround(rfCore);
		}else{
			surround = new Surround(parentField,surroundBuffers);
		}

		results.setParticleIndexes( surround.getGeometricSurround( index ,surroundN,Surround._CIRCLE ) ); 
		results.setParticleDistances( surround.getParticleDistances()) ;
		
		return results;
	}
	
	/**
	 * changing from array[] to collection
	 */
	private ArrayList<Point2D> changePointsFormat( PointXY[] points){
	
		ArrayList<Point2D> cpoints = new ArrayList<Point2D>() ;
		Point2D p;
		
		for (int i=0;i<points.length;i++){
			p = new Point2D();
			p = Point2D.create( points[i].x, points[i].y) ;
			cpoints.add(p) ;
		}
		return cpoints;
	}

	private PointXY[] prepareListOfPoints( ){
		 
		// get the list of points
		
		PointXY[] points = new PointXY[0];
		int ix;
		  
		if (paramSets.size()<1){
			return points ;
		}
		RetrievalParamSet p = paramSets.get(paramSets.size()-1) ;
		
		if (p.particleIndexes.length<=1){
			return points ;
		}
		
		
		points = new PointXY[p.particleIndexes.length];
		
		for (int i=0;i< points.length;i++){
			
			ix = p.particleIndexes[i] ;
			if ( (ix>=0) && (particles!=null) && (ix<particles.size())){
				points[i] = new PointXY();
				points[i].x = particles.get(ix).x ;
				points[i].y = particles.get(ix).y ;
				points[i].index = ix;
			}
		} // i->
		
		return points;
	}

	public Object getResultsByGuid(String guid ) {
		Object resultObject=null;
		 
		resultObject = resultMap.get(guid);
		
		return resultObject;
	}
	
	public SurroundRetrievalObserverIntf getSurroundRetrievalObserver(){
		return srObserver;
	}

	public Map<String, Object> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<String, Object> resultMap) {
		this.resultMap = resultMap;
	}
	
}	
 

 