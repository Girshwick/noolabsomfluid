package org.NooLab.field.repulsive.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.convhull.JarvisMarch2D;

import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.data.LineXY;
import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.field.repulsive.components.data.RetrievalParamSet;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.field.repulsive.components.topology.CoverageByBars;
import org.NooLab.field.repulsive.components.topology.CoverageByHull;
import org.NooLab.field.repulsive.components.topology.Surround;
import org.NooLab.field.repulsive.components.topology.geom.MinimalSpanningTree;
import org.NooLab.field.repulsive.intf.RepulsionFieldObjectsIntf;
import org.NooLab.field.repulsive.intf.SurroundRetrievalObserverIntf;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldBasicIntf;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;
import org.NooLab.graph.TreeLinesIntf;
 
 
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;


/**
 * 
 * once instantiated, it should be avoided to send parallel tasks to this class 
 * 
 * 
 */
public class FluidFieldSurroundRetrieval implements Runnable {

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
	
	// SurroundBuffers surroundBuffers;
	
	ParticleGrid particleGrid;
	RepFieldParticlesIntf particles;
	 
	ArrayList<RetrievalParamSet> paramSets = new ArrayList<RetrievalParamSet>(); // !!!!!!!!!!
	
 	Map<String,Object> resultMap = new HashMap<String,Object>();  // !!!!!!!!!!!!!!!!!
	  
 	SelectionConstraints selectionConstraints;
 	
	Thread rfSurrThrd;
	int surroundTask=-1;
	
	Map<String,Thread> threadMap = new HashMap<String,Thread>();
	
	PrintLog out = new PrintLog(2,true);
	private boolean selectionExecParallel=false;
	private boolean adjustmentWait;
	
	// ========================================================================
	public FluidFieldSurroundRetrieval( RepulsionFieldCore rfcore ){
		 
		rfCore = rfcore;
		
		objParent = rfCore;
		
		callingStyle = 1;
		
		srObserver = (SurroundRetrievalObserverIntf)rfcore ;  
		
		parentField = (RepulsionFieldBasicIntf)rfcore; 
		
		particles = parentField.getParticles();
	}
	 
	
	public FluidFieldSurroundRetrieval( Object parent , ParticleGrid pg, SurroundRetrievalObserverIntf observer ){
		
		if (parent==null){
			return;
		}
		
		particleGrid = pg;
		callingStyle = 2;
		RepulsionFieldBasicIntf rfcore = (RepulsionFieldBasicIntf) parent ; 
		RepulsionFieldObjectsIntf rfObjects = (RepulsionFieldObjectsIntf)parent ;
		
		objParent = parent;
		
		srObserver = observer;
		parentField = rfcore;
		particles = parentField.getParticles();
		
		// surroundBuffers = rfObjects.getSurroundBuffers() ;  
	}
	// ========================================================================
	
	
	public void close() {
		if ((rfSurrThrd!=null) && (rfSurrThrd.isInterrupted()==false)){
			
			// rfSurrThrd.interrupt();
		}
 
		paramSets.clear(); 
		
	 	resultMap.clear();
		   
	 	
	 	if ((rfSurrThrd !=null) &&(rfSurrThrd.isInterrupted()==false)){
	 		// rfSurrThrd.interrupt();
	 	}
	 	
	 	rfSurrThrd=null;
	 	 
		threadMap.clear();
		
		threadMap=null;
		out=null;
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
	
	public void setParticleGrid( ParticleGrid pg ){ particleGrid = pg; }
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

	/**
	 * 
	 * the mistake is to put the Runnable ontop to the public class
	 * instead to create a worker (inner) class for each request
	 * 
	 * @param paramSetIndex
	 * @param task
	 * @param guidStr
	 * @return
	 */
	public String go( int paramSetIndex, int task , String guidStr) {
		boolean ok=false;
		
		surroundTask = task;
		adjustmentWait=true;
		
		if (paramSets!=null) {
			for (int i=paramSets.size()-1;i>0; i--) {
				if (paramSets.get(i)==null) {
					paramSets.remove(i);
				}
			}
		}
		
		while (ok==false){
			try {
				if (paramSetIndex > paramSets.size()-1) {
					paramSetIndex = paramSets.size() - 1;
				}
				paramSets.get(paramSetIndex).task = task;
				paramSets.get(paramSetIndex).guid = guidStr;

				ok = true;
			} catch (Exception e) {

			}
		}
		adjustmentWait=false;
		
		if (selectionExecParallel){
			rfSurrThrd = new Thread(this,"rfSurrThrd-"+guidStr);
			 
			threadMap.put(guidStr, rfSurrThrd);
			rfSurrThrd.setPriority(8) ;
			rfSurrThrd.start();
			
		}else{
			runDispatch();	
		}
		
		
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
		
		runDispatch();
		
	}

	private void runDispatch(){

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
		RepulsionFieldParticle p;
		
		Surround surround;
		SurroundResults results = new SurroundResults();
		
		/*
		if (callingStyle==1){
			surround = new Surround(rfCore);
		}else{
			surround = new Surround(parentField,surroundBuffers);
		}
		*/
		
		RetrievalParamSet rps = paramSets.get(paramSets.size()-1) ;
		
		xpos = rps.xpos ; 
		ypos = rps.ypos ;
		
		out.print(4, "going to retrieve particle...");
		// results.particleIndex = surround.getParticleAt( xpos, ypos, -1);// particles.get(0).radius );
		//  care about deactivated particleGrid !!! also, the reference might (have) change(d) due to the update
		results.particleIndex = particleGrid.getIndexNearLocation( xpos, ypos) ;
 
		out.print(4, "particle retrieved.  ");
		
		p = particles.get( results.particleIndex ) ;
		
		if ((results!=null) && (p!=null)){
			results.getCoordinate()[0] = p.x;
			results.getCoordinate()[1] = p.y;

			// no index distance here, of course, but for compatibility we create a single element
			IndexDistance ids = new IndexDistance ( results.particleIndex, 0.0 , "");
			results.getIndexedDistances().clear();
			results.getIndexedDistances().add( ids ) ;

			
			results.setGuid( rps.guid);
			results.setParamSet( rps );
			results.timeflag = System.currentTimeMillis();

			resultMap.put(rps.guid, results);

			if (srObserver!=null){
				srObserver.surroundRetrievalUpdate(this, rps.guid);
			}
			
		}
		
		Thread thrd = threadMap.get(rps.guid);
		 		 
		threadMap.remove(rps.guid);
		
		try {
			if (thrd!=null)
			thrd.join();
		} catch (InterruptedException e) {
		}
		thrd = null;

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
		int z=0;
		while ((adjustmentWait)&&(z<5)){z++; out.delay(2);}
		
		 
		
		RetrievalParamSet p=null ;
		int pin = paramSets.size()-1;
		while ((pin>=0) && (p==null)){
			p = paramSets.get(pin) ;
			pin--;
		}
		 
		if (p!=null){
			if (p.surroundN > particles.size()*(2.0/3.0)){ p.surroundN = (int) (particles.size()*(2.0/3.0)); };
		}
		
		if (style== _TASK_SURROUND_C){
			results = getSurround( p.xpos, p.ypos, p.surroundN, p.selectMode,  p.autoselect);
		}
		if (style== _TASK_SURROUND_X){
			results = getSurround( p.particleIndex, p.guid, p.surroundN, p.selectMode,  p.autoselect);
		}
		
		if (results!=null){
			
			
			results.setGuid( p.guid);
			results.setParamSet( p );
			 

			// put this to the result map, where is this cleared !!!
			if (selectionExecParallel){}
				
			resultMap.put(p.guid, results);
			
			z=0;
			while ((z<5) && (adjustmentWait)){
				out.delay(2) ;
				z++;
			}
			
			if ((paramSets!=null) ) {
				for (int i = 0; i < paramSets.size(); i++) {
					if (paramSets.get(i)!=null){
						String gs = paramSets.get(i).guid;
						if (gs.contentEquals(p.guid)) {
							paramSets.set(i, null);
							break;
						}
					}
				}
			}
			// out.print(2, "SurroundRetrieval, size of paramSets: "+paramSets.size()) ;
			// out.print(2, "-                          resultMap: "+resultMap.size()) ;
			// observer = RepulsionField
			srObserver.surroundRetrievalUpdate(this, p.guid);
		}
		
		Thread thrd = threadMap.get(p.guid);
	 
		// out.print(2, "nulling thread of guid: "+p.guid) ;
		out.delay(20); 
		threadMap.remove(p.guid);
		if (thrd!=null){
			thrd.setPriority(1); // type "rfSurrThrd"
			// ClassLoader cl = thrd.getContextClassLoader();
			ThreadGroup tg = thrd.getThreadGroup();
			if (tg!=null){
				tg.setMaxPriority(8);
			}

			try {
				// thrd.interrupt();
				// thrd.join() ;
				// thrd = null;

			} catch (Exception e) {
			}
		}
	}
	
	
	private SurroundResults getSurround( int xpos, int ypos , int surroundN ,  
			  							 int selectMode, boolean autoselect){
		int ix;
		SurroundResults results = new SurroundResults(); 
		// Surround surround;
		
		ArrayList<IndexDistance> indexedDistances ;
		 
		
		ix = particleGrid.getIndexNearLocation( xpos, ypos) ;
		
		if (ix<0){
			return null ;
		}
		results.particleIndex = ix;
		// get circle, or any other figure
		
		RepulsionFieldParticle p = particles.get( results.particleIndex ) ;
		
		out.print(3, "going to retrieve particles...");
		
		
		indexedDistances = particleGrid.getIndexListRetriever().setConstraints( selectionConstraints ).getIndexedDistancesFromNeighboorhood( p.x, p.y, surroundN ) ;

		out.print(2, "particles retrieved (n="+indexedDistances.size()+")...");
		
		if ((results!=null) && (p!=null)){
			results.getCoordinate()[0] = p.x;
			results.getCoordinate()[1] = p.y;
		}
		
		results.getIndexedDistances().clear();
		 
		
		if ((indexedDistances!=null) && (indexedDistances.size()>0)){
			results.getIndexedDistances().addAll( indexedDistances ) ;
			
			results.setParticleIndexes( particleGrid.extractIndexesFromIndexedDistances(indexedDistances) );
		}

		
		return results;
		
	}
	
	private SurroundResults getSurround( int index , String guid, int surroundN ,  
		     							 int selectMode, boolean autoselect){
		
		double x, y ;
		SurroundResults results = new SurroundResults(); 
		// Surround surround;
		
		ArrayList<IndexDistance> indexedDistances ;
		
		// results.particleIndex = particleGrid.getIndexNear( particles.get(index).x , particles.get(index).y);
		
		// get circle, or any other figure
		
		RepulsionFieldParticle p = particles.get( index ) ;
		
		out.print(4, "going to retrieve particles (b)...");
		
		// ParticleGrid._CIRCLE == default , setShape is optional
		indexedDistances = particleGrid.getIndexListRetriever().setConstraints( selectionConstraints ).getIndexedDistancesFromNeighboorhood( p.x, p.y, surroundN ) ;
		
		out.print(4, "particles retrieved ...");
		
		if ((results!=null) && (p!=null)){
			results.getCoordinate()[0] = p.x;
			results.getCoordinate()[1] = p.y;
		}
		results.setGuid(guid) ;
		results.getIndexedDistances().clear();


		if ((indexedDistances!=null) && (indexedDistances.size()>0)){
			results.getIndexedDistances().addAll( indexedDistances ) ;
			
			results.setParticleIndexes( particleGrid.extractIndexesFromIndexedDistances(indexedDistances) );
		}

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


	public void setSelectionConstraints( SelectionConstraints selectconstraints) {
		// 
		selectionConstraints = selectconstraints;
	}


	/**
	 * @return the selectionExecParallel
	 */
	public boolean isSelectionExecParallel() {
		return selectionExecParallel;
	}


	/**
	 * @param selectionExecParallel the selectionExecParallel to set
	 */
	public void setSelectionExecParallel(boolean selectionExecParallel) {
		this.selectionExecParallel = selectionExecParallel;
	}
	
}	
 

 