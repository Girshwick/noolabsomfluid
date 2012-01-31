package org.NooLab.repulsive.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.NooLab.repulsive.components.data.FieldPoint;
import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.repulsive.components.data.RequestBorder;
import org.NooLab.repulsive.components.infra.C2DComparable;
import org.NooLab.repulsive.intf.ItemLinkageIntf;
import org.NooLab.repulsive.intf.Stoppable;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;



/*
 * 
 * http://stackoverflow.com/questions/122105/java-what-is-the-best-way-to-filter-a-collection

 * this (:: https://www.iam.unibe.ch/scg/svn_repos/Sources/ForEach/ ) offer a select query imitation
 * for strings , like 
 * for (Select<String> each : select(collection)) {
 *  each.yield = each.value.length() > 3;
 *  }
 *  
 *  
 */


/**
 * TODO:
 *         buffering all particles and their N (e.g.8, min 3) neighbors , until recalculated
 *         then, in background, again updating
 *         
 *         Such, we get overlapping fields of links, which allows even to traverse 
 *         the whole network efficiently
 *         
 *         do not mistake this linkage as the same as the linkage within the particles.
 *         That within the particles is semantical, and thus more persistent, while this here is
 *         purely physical
 *         
 * 
 * this class is a helper object that provides the data structures and bookkeeping 
 * functionality of the location of the particlesm such that the distance of particles 
 * in a dynamic grid can be determined, where the position of particles 
 * is "mostly" stable but can change strongly due to changes  </br>
 * - in properties of the particles  </br>
 * - of the number of particles </br>
 * - the rules that create a particular topology </br> 
 * </br></br>
 * 
 * Buffer:
 * The buffer is an independent entity ("SurroundBuffer"), which refers to particles via their index;</br>
 * basically, it maps the particle index to a list of other particles, where this list 
 * comes in sorted along the distances  
 * 
 * </br></br>
 * 
 * As such it is the generalization of the adjacency matrix, or also for the comparator for a NavigableSet. </br> </br>
 * 
 * The requests for navigation can take different forms.  </br>
 * 
 * One may ask about the distance between any two objects, using different geometries 
 * (linear, hyperbolic, parabolic [=inverse hyperbolic]); </br>
 * 
 * Or one  asks about all objects within a certain distance. </br> </br>
 * 
 * It runs its own thread for all maintenance operations. </br>
 * 
 * For returning results, it offers two modes: execution and event via interface 
 * (requires registration of a callback) </br> </br>
 * 
 * The Neighborhood class is particularly helpful for tasks like SOM or large swarms, since
 * it prevents looping in the main thread
 *   
 */
public class Neighborhood implements Runnable, Stoppable{

	protected static final int __UPDATE = 1;
	protected static final int __GET_D  = 5; // get the distance between two items
	protected static final int __GET_DS = 5; // get the average distance of closest items in a surround
	protected static final int __GET_NB = 8; // get a collection of items within a particular distance 
	protected static final int __GET_NN = 9; // get a collection of N closest items 
	
	// some topological properties of the area regarding the border  
	public static final int __BORDER_ALL  = 1; // rectangle
	public static final int __BORDER_NONE = 2; // torus
	public static final int __BORDER_OPEN_L = 5; // 
	public static final int __BORDER_OPEN_R = 6; // 
	public static final int __BORDER_OPEN_T = 7; // 
	public static final int __BORDER_OPEN_D = 8; //
	public static final int __BORDER_OPEN_ZA = 15; //
	public static final int __BORDER_OPEN_ZB = 16; //
	public static final int __BORDER_OPEN_Z  = 17; //
	
	public static final int __BORDER_LR_ONLY = 20; // 
	public static final int __BORDER_SN_ONLY = 21; // SN = south-north 
	public static final int __BORDER_EW_ONLY = 22; // 
	public static final int __BORDER_ZA_ONlY = 23; // 
	public static final int __BORDER_ZB_ONLY = 24; // 
	
	public static final int __BORDER_SE_ONLY = 21; //
	public static final int __BORDER_NW_ONLY = 21; //

	
	String parentName = "" ;
	
	/** imports methods for non-grid connections between items;
	 *  not yet available 
	 */
	ItemLinkageIntf itemLinkage; 
	 
	
	Dimension xDim ;
	Dimension yDim ;
	Dimension zDim ;
	Plane xyPlane;
	
	ArrayList<QTask> taskQueue = new ArrayList<QTask>();
	
	// --------------------------------

	
	int borderMode = __BORDER_ALL;
	
	// distance from right to left [n]-times the concerning direction,
	int borderInfinityFactor = 10; 
	
	// the size of the embedding "canvas"
	int width, height, depth;
	
	// --------------------------------

	double surroundScaleFactor = 0.7;
	double averageDistance = 0.0 ;
	
	
	// --------------------------------
	
	Map<String, RequestBorder> requestBorders = new HashMap<String, RequestBorder>();
	
	Particles particles;
	SurroundBuffers  surroundBuffers ;
	boolean buffersOpen=true;
	
	// --------------------------------
	
	Thread nbThrd = null;
	boolean isRunning=false, isWorking;
	int performActionThreadCount=0;
	
	ArrUtilities arrutil = new ArrUtilities();
	PrintLog out;
	
	// ------------------------------------------------------------------------
	public Neighborhood( int bordermode, SurroundBuffers sb, PrintLog outprn){
		
		out = outprn;
		if (out==null){
			out = new PrintLog(2,false) ;
		}
		surroundBuffers = sb;
		
		if (surroundBuffers!=null){
			surroundBuffers.registerNeighborhood(this) ;
		}
		
		if ( (bordermode!=__BORDER_ALL) && (bordermode!=__BORDER_NONE)){
			bordermode =__BORDER_ALL ;
		}
		init( bordermode );
	}
	
	
	private void init( int bmode ){
		borderMode = bmode;
			
		
		xDim = new Dimension(this,"x");
		yDim = new Dimension(this,"y");
		zDim = new Dimension(this,"z");

		xyPlane = new Plane(this,"xy");
		
		if (surroundBuffers!=null){
			particles = (Particles) surroundBuffers.parentField.getParticles();
		}
		
		if (isRunning){
			isRunning = false;
			out.delay(20);
		}
		
		nbThrd = null ;
		nbThrd = new Thread(this,"nbThrd");
		nbThrd.start();
	}
	// ------------------------------------------------------------------------
	 
	/**
	 * returns the index of the particle! (NOT of the coordinate in xyPlane)
	 * 
	 * if radius <= 0 we look for the closest one to the requested variables
	   if radius >0, it simply applies as an additional condition : only WIHTIN sicu radius it will 
	   be searched for  the closest 
	 
	   ATTENTION: this MUST be thread safe, so we have to extract the values from objects in dedicated data objects 
	              or we use a multi-slot storage for distance values, where the identifier of the slot (a has map) is issued
	              by the calling instance of this routine (random guid)
	*/
	public int getItemsCloseTo(int xpos, int ypos ) {
		int index=-1;
		Coordinate2D c2D;
		
		try{
			c2D = (getItemsCloseTo(xpos, ypos, -1, "" )).get(0);
			index = c2D.particleIndex ;
			
		}catch(Exception e){}
		
		return index;
	}

	public Vector<Coordinate2D> getItemsCloseTo(int xpos, int ypos, double radius, String guidStr ) {
	
		Vector<Coordinate2D> c2Ds = new Vector<Coordinate2D>();
		Coordinate2D c2D;

		RequestBorder requestBorder; 
		int index=-1;
		boolean found=false,preselect;
		int cx=0,n;
		double x,y,dx,dy,dc2x ;
		
		/*
		 * xyPlane.coordinates comes in as being sorted by "x first" !
		 */
		 
		
		n = xyPlane.coordinates.size() ;
		if (averageDistance<=0){
			// an estimation
			averageDistance = ((width+height)/2)/Math.sqrt(n+1);
		}
		
		double f = Math.max(1.0,radius / averageDistance) ;
		int tempListSize = (int)(18 * f) ;
		
		cx=0;
		for (int i=0;i<n;i++){
			x = xyPlane.coordinates.get(i).getXvalue();
			
			if (radius <= 0) {
				if (x > (xpos - averageDistance * 1.6)) {
					 
					break;
				} 	// this is badly slow: we need a dedicated routine, which uses 
					// a different approach: a coarse map <cxValue,index>, solution: maybe a grid of the area ?
			}else{
				 
				if (this.borderMode == __BORDER_ALL) {
					if (x > (xpos - averageDistance * 1.2)) { // radius 
						 
						break;
					}
				}
				if (borderMode == __BORDER_NONE) {
					// 
					if ((xpos > averageDistance * 1.6) && (x > (xpos - averageDistance * 1.2))) {
					    // experimental: no break -> take all 
						// break;
					}
					
				}
			}
		} // i-> n :: all coordinates
 
		if (cx<0)cx=0;
		n = xyPlane.coordinates.size() ; 
		double restrictionDx = (radius+averageDistance*0.3);
		
		while ((found==false) && (cx<n)){
			
			// is there a RequestBorder object containing guidStr?
			if (requestBorders.containsKey(guidStr)){
				
				int ccPIndex = xyPlane.coordinates.get(cx).particleIndex ;
				requestBorder = requestBorders.get(guidStr) ;
				
				boolean hbb = requestBorder.indexIsAccessible(ccPIndex, xyPlane.coordinates.size() );
				
				
				if (hbb==false){
					continue;
				}
			} // testing for exclusion
			
			x = xyPlane.coordinates.get(cx).getXvalue();
			y = xyPlane.coordinates.get(cx).getYvalue();
			
			
			dx = Math.abs(x-xpos);
			dy = Math.abs(y-ypos);
			
			// respects torus topology
			dx = Math.abs( getLinearDistanceX(xpos,x) );
			dy = Math.abs( getLinearDistanceY(ypos,y));
			
			
			preselect = false; 
			
			if (radius>0){
				
				preselect = dx<=restrictionDx;
				if (preselect){
					preselect = dy<=restrictionDx;
				}
			}else{
				dc2x = 999999;
				if (c2Ds.size()>0){
					
					double _x = c2Ds.get(c2Ds.size()-1).getXvalue();

					dc2x = Math.abs(_x - xpos);
					dc2x = Math.abs( this.getLinearDistanceX(_x,xpos));
				}
				// is the last item in the sorted list extract "c2Ds" larger than the current distance?
				preselect = (c2Ds.size()==0) || 
							(  (dx < averageDistance*1.8 ) && 
							   (( dc2x > dx ) || (dx<averageDistance*0.38)));  // if yes, then put it to the 
				if ((dy > averageDistance*1.4 )){
					preselect = false;
				}
			}
			
			if (preselect){
				c2D = xyPlane.coordinates.get(cx);
				  
				c2D.compareMode = Coordinate2D._COMPARE_X;
				c2D.activeHash = guidStr;
				index = c2D.particleIndex;
				if (c2Ds.size()==0){
					c2Ds.add(c2D);
				}else{
					c2Ds.insertElementAt(c2D,0) ;
				}
				 
				Collections.sort(c2Ds, new C2DComparable());
				
				if ((radius<=0) && (c2Ds.size()> tempListSize)){
					c2Ds.remove( c2Ds.size()-1 ); //
				  // still not correct: if the column is very high we will get ony a small band	
				} // we have to restrict by y-distance as well... -> by the mean distance between the particles
				  // which has been imported to a global variable
			}
			if (radius<=0){
				// radius<0 == selection of a single particle
				if (((averageDistance>0) && (x>xpos+averageDistance*1.4)) ){
					break;
				}
			}else{
				// problematic in case of torus topology...
				if ((dx>radius) && (x > xpos + 1.3*radius)){
					// so for now, we don't break
					if (borderMode == __BORDER_ALL) {
						break;
					}
				}
			}
			cx++;
		} // -> preselection x found ?
		
		if (radius<=0){
			int p = getClosestCoordinate(c2Ds,xpos, ypos );
			
			if (p!=0){
				c2Ds.set(0, c2Ds.get(p)) ;
			}
			// outside, we will be interested only in the first element, if radius < 0
			
		}else{  
			 
		}
if ( ((xpos>430) && (xpos<460))	&& ((ypos<50))){
	String str="";
	
	int[] items = new int[c2Ds.size()];
	for (int i=0;i<items.length;i++){
		items[i] = c2Ds.get(i).particleIndex ;
	}
	str = "["+arrutil.arr2text(items)+"]";
	out.print(4,"bordermode:"+borderMode+" (w,h:"+width+","+height+
			    "),  preselected items in c2Ds : "+c2Ds.size()+"\n"+
			    "items: "+str) ;
}
		return c2Ds;
	}
	
	

	public ArrayList<IndexDistance> getAdjustedSurroundSelection( int xpos, int ypos, 
														   		  double radius, String guidStr ) {
		
		// IndexDistance is a simple object containing the pair index, distance, identified by guidstr
		ArrayList<IndexDistance> indexDistances = new ArrayList<IndexDistance>();
		IndexDistance idist;
		
		Coordinate2D c2D;
		 
		
		double x,y,dx, xd,yd ;
		Vector<Coordinate2D> c2Ds = new Vector<Coordinate2D>(); 

		c2Ds = getItemsCloseTo( xpos, ypos, radius ,guidStr );
		
		x=0;
		
		for (int i=0;i<c2Ds.size();i++){
			
			c2D = c2Ds.get(i);
			x = c2D.cxValue;
			y = c2D.cyValue;
			
			xd = xpos-x;
			yd = ypos-y;
			
			// respects torus topology
			xd = this.getLinearDistanceX(xpos, x);
			yd = this.getLinearDistanceY(ypos, y);
			
			dx = Math.sqrt( (xd)*(xd) + (yd)*(yd));
		
			c2D.compareMode = Coordinate2D._COMPARE_D;
			c2D.setDistanceValue(guidStr, dx);
		} // i->
		
		// sorting according to distance

		Collections.sort(c2Ds, new C2DComparable()); 
		x=0;
		for (int i=0;i<c2Ds.size();i++){
			c2D = c2Ds.get(i);
			dx = c2D.getDistanceValue(guidStr) ;
			
			idist = new IndexDistance(c2D.particleIndex, dx, guidStr) ;
			
			indexDistances.add(idist) ;
		}
		
		return indexDistances;
	}

	/**
	 * 
	 * thats purely internal!! the returned index values refers just to the temporary structure c2Ds
	 *  
	 * @param c2Ds
	 * @param xpos
	 * @param ypos
	 * @return
	 */
	private int getClosestCoordinate(Vector<Coordinate2D> c2Ds, int xpos, int ypos) {
		Coordinate2D c2D;
		double x,y,dx ,yd,xd, minDx=999999999;
		int minix=-1;
		
		
		for (int i=0;i<c2Ds.size();i++){
			c2D = c2Ds.get(i);
			x = c2D.cxValue;
			y = c2D.cyValue;
			
			xd = xpos-x;
			yd = ypos-y;
			
			// respects torus topology
			xd = this.getLinearDistanceX( xpos, x);
			yd = this.getLinearDistanceY( ypos, y);

			dx = Math.sqrt( (xd)*(xd) + (yd)*(yd));
		
			if (minDx > dx){
				minDx = dx ;
				minix = i;
			}
		} // i->
		
		return minix;
		
	}

	/**
	 * entry point for calls from Surround object;
	 * called from Surround.getGeometricSurround();
	 * 
	 * 
	 * @param index
	 * @param surroundSize
	 * @param expectedCollSize
	 * @param guidStr
	 * @return
	 */
	public ArrayList<IndexDistance> getItemsOfSurround(int index, int surroundSize, int expectedCollSize, String guidStr) {
		// TODO: to make this thread safe, it must be called as an inner class (from Surround object) !!!
		// otherwise, "indexedDistances" will contained weird things...
		// also we have to care about the exclusion parameters
		ArrayList<IndexDistance> indexedDistances = new ArrayList<IndexDistance>() ;
		
		int cix;
		int[] areaItems = new int[0] ;
		double sinz ,surroundRadius;
		
		// we retrieve bands in x and y directions (or planes in 3d case);
		// then we check the max distance from the anchor in both directions for each dimension
		// finally, we select items from dimension arrays (xdim, ydim) by distance
		
		int xs = xyPlane.size();
		boolean bcix = xyPlane.mapContainsIndex(index);
		int sbxn = 0;
		if (surroundBuffers.surroundExtension != null){
			sbxn = surroundBuffers.surroundExtension.size();
		}
		if (xs!=expectedCollSize){
			
			out.print(2, "neighborhood.getItemsOfSurround(), checking buffer for particle "+index+", sizes:"+xs+"(+"+sbxn+") ,"+expectedCollSize+", indexed in xyPlane ? -> "+bcix);
		}
		if ((xs+sbxn>=expectedCollSize) && ( bcix )){
			String sbName = surroundBuffers.parentName ;
			int sbaState = surroundBuffers.bufferIsOfState( index, surroundSize );
			boolean sba = sbaState>=5; 
			if (sba==false){
				sba = surroundBuffers.bufferIsAvailable( index, surroundSize);
				
			}
			if ((surroundBuffers!=null) && (sba) && (surroundBuffers.getBufferingSwitchedOff()==false)){
				
				out.print(2, "Neighborhood(), retrieving buffer into <indexedDistance> ...  ");
				
				indexedDistances = surroundBuffers.exportBuffer( index, surroundSize , guidStr);
				 
				out.print(2, "\nbuffer used for request on particle "+index);
				out.print(3, "Neighborhood("+surroundBuffers.neighborhood.toString()+"), retrieving buffer completed. \n"); 
				return indexedDistances;
			}else{
				if (index>638)
				out.print(5, "retrieving buffer for index <"+sbName+"> into <indexedDistance> denied: "+
							 "surroundbuffers exist? -> "+(surroundBuffers!=null)+", available? -> "+sba+" "+
							 ",  buffer switch? -> "+surroundBuffers.getBufferingSwitchedOff()+" ...  ");
			}
			if(surroundBuffers.getBufferingSwitchedOff()==false){
				if (index>638)
				out.print(2, "\nbuffer (context:"+sbName+") for particle "+index+" is not available (sizes:"+xs+" ,"+expectedCollSize+"), direct retrieval will be started");
			}
			   //  (int)
			sinz = Math.round(Math.sqrt(surroundSize)+0.3);
			 
			surroundRadius = (averageDistance * sinz* surroundScaleFactor*1.1);
			
			// get surround will correct for borders, according to the settings,
			// such that always slightly more than n=surroundSize items will be returned
			
			indexedDistances = xyPlane.getSurround(index, surroundRadius, guidStr);
			 				 
			double dvMax = Math.sqrt( ((double)surroundBuffers.selectionSize+0.1)/3.14)* this.averageDistance *0.72;
			
			
			try{
				
				ArrayList<IndexDistance> ixsex = surroundBuffers.getIxDisFromSurroundExtension( index,dvMax,indexedDistances,guidStr );
				if ((ixsex!=null) && (ixsex.size()>0)){
					indexedDistances.addAll( ixsex );
				}
			}catch(Exception e){
				
			} 
			
			
			int k = indexedDistances.size();  
			if (k>surroundSize){k=surroundSize;}
			areaItems=new int[k] ;
			
			
			for(int i=0;i<k;i++){
				index = indexedDistances.get(i).getIndex() ;
				if (index>=0){
					cix = xyPlane.positionTableMap.get(index);
				
					if (cix >= 0) {
						xyPlane.coordinates.get(cix).removeTempDistanceSlot( guidStr);
					}
				}
			}
			
			// clearing the rest
			for (int i=indexedDistances.size()-1;i>k-1;i--){
				index = indexedDistances.get(i).getIndex() ;
				if ((index>=0) && (xyPlane!=null) && (xyPlane.positionTableMap!=null)){
					if (xyPlane.positionTableMap.containsKey(index)){
						cix = xyPlane.positionTableMap.get(index);
						if (cix >= 0) {
							xyPlane.coordinates.get(cix).removeTempDistanceSlot(guidStr);
							indexedDistances.remove(i);
						}
					}
				}
			}
			
		}
		 
		if (buffersOpen){ 
			// (selectionSize >= surroundSize)
			if ((surroundBuffers.bufferIsAvailable( index, surroundSize))==false){
				// This mainly happens after change of selection size
				// we should not do it here if getBufferingSwitchedOff()==true,... if false then we are in
				// use-mode
				
				if(surroundBuffers.getBufferingSwitchedOff()==false){
					surroundBuffers.importIndexDistance(index, indexedDistances ) ;
				}
			}
		}
		return indexedDistances;
	}

	// .... UPDATE ............................................................
	/**
	 * public entrance to the class. </br></br>
	 * The parameters will be packed into an object and then
	 * put into a queue together with the action identifier </br>
	 * Handling of the queue is opening a new object for each entry,
	 * where "handling" of update then invokes the private method 
	 * "updateAction" for the queued item.  
	 * 
	 */
	public void update(int index, double x, double y, double radius){
		
		// we put this information and the type of the request (here:"update") into a queue
		Item _loc_item = new Item(index, x,y,radius);
		QTask t = new QTask(__UPDATE, _loc_item);
	
		taskQueue.add(t) ;
		// int n = taskQueue.size();
		// if (n>10){ out.print(2, "size of taskQueue : "+n);}
	}
	

	
	
	/**
	 * private realization of the update action, now running in its own threaded container 
	 * @param item
	 */
	private void updateAction( PerformAction pa, Item item ){
		/*
		xDim.update(item.index,item.x,"x");
		yDim.update(item.index,item.y,"y");
		zDim.update(item.index,item.z,"z");
		*/
		
		xyPlane.update( item ,"xy" );
		  
		// signal to the main loop, that the job is done
		pa.waitmode=0;
	}
	// ........................................................................
	
	// .... GET ...............................................................

	public void get(){
		
		
	}
	
	private void getAction( PerformAction pa, Item item ){
		
	}

	// ........................................................................
	
	
	// ========================================================================
	class PerformAction  {
		
		int waitmode=0;
		
		QTask qTask;
		Thread performthrd;
		public PerformAction(){
			  	
		}

		public PerformAction goFor(QTask qTask) {
			waitmode=0;
			
			if (qTask==null){
				return this;
			}
			this.qTask = qTask;
			if (qTask.actionID == __UPDATE ){
				// setting the waitmode before starting the tread
				// waitmode is used by the main loop to handle critical actions
				waitmode=1;
			}
			// performActionThreadCount++;
			// performthrd.start();
			
			handlingPerformRequest();
			
			return this;
		}

		private void handlingPerformRequest(){
			
			
			if (qTask.actionID == __UPDATE ){
				updateAction( this, qTask.item);
			}
			if (qTask.actionID == __GET_D ){
				
			}

		}
 

		public int getWaitmode() {
			return waitmode;
		}

		
		
	}// inner class PerformAction
	// ========================================================================
	
	public void stop(){
		isRunning=false;
		out.print(3, "neighborhood process is stopping...");
		while (isWorking){
			out.delay(1);
		}
		nbThrd=null;
	}
	
	@Override
	public Thread getThread() {
		 
		return nbThrd;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void restartProcess(){
		
		if ((nbThrd==null) || (isRunning==false)){
			//if (nbThrd==null){
			try{
				nbThrd.join();
			}catch(Exception e){}
			nbThrd = null;
			
			nbThrd = new Thread(this,"nbThrd");
			
			nbThrd.start();
			out.print(2, "neighborhood process has been re-started.");
		}
			
	}
	
	public void finalizeQ(){
		finalizeQ(0);
	}
	public void finalizeQ(int maxAllowed){
		
		if (taskQueue.size()>maxAllowed){
			out.print(3, "start finalizing, size of taskQueue    : "+taskQueue.size());
			
			while (taskQueue.size()>0){
				out.delay(1);
			}
			
			out.print(3, "finalizing finished, size of taskQueue : "+taskQueue.size());
		}
		
	}
	/**
	 * 
	 * the acceptance loop of the main thread of the Neighborhood class,
	 * which is working on the queue;
	 * such, it is thread safe
	 * 
	 */
	@Override
	public void run() {
		
		PerformAction pa ;
											out.print(3, "neighborhood process has been started.");
		isWorking=false;
		isRunning=true;
		
		try{
			
			while (isRunning){
				
				if ((isWorking==false) && (taskQueue.size()>0)){
					isWorking=true;
					
					if (performActionThreadCount<=20){
						
						try{
							// out.print(2, "opening new object 'PerformAction(n="+performActionThreadCount+")', task queue = "+taskQueue.size() );
							pa = (new PerformAction()).goFor(taskQueue.get(0));
					
							// this mechanism allows to block critical actions, while the less critical (reading),
							// could be done in parallel
							if (pa.waitmode > 0) {
								while (pa.waitmode > 0) {
									out.delay(1);
								}
							}
							if (taskQueue.size() > 0) {
								taskQueue.remove(0);
							}
						}catch(Exception e){
							
						}
					}else{
						out.print(3, "count of performActionThreadCount : "+performActionThreadCount);
					}
					isWorking=false;
				}
				if (taskQueue.size()==0){
					out.delay(2);
				}
				
			}// ->
			
		}catch(Exception e){
			e.printStackTrace();
		}
		isRunning = false;
		out.print(3, "neighborhood process has been stopped.");
	}

	public void openBuffersForOverwrite() {
		 
		buffersOpen = true ;
	}


	public void fixPositionBuffers() {
		 
		buffersOpen = false ;
	}


	public void clearPositionBuffers() {
		 
		
	}


	public int getUpperBoundForSurround(int i, int surroundSize, int expectedCollSize) {
	 
		return expectedCollSize;
	}

	public Plane getXyPlane() {
		return xyPlane;
	}


	public double getAverageDistance() {
		return averageDistance;
	}

	public void setAverageDistance(double averageDistance) {
		this.averageDistance = averageDistance;
	}

	public int getBorderMode() {
		return borderMode;
	}


	public void setBorderMode(int bordermode) {
		int bm = bordermode;
		this.borderMode = bordermode;
		if (borderMode!=bm){
			init(borderMode);
		}
	}


	public void setAreaSize( int w, int h){
		setAreaSize(w,h,-1);
	}
	public void setAreaSize( int w, int h, int z){
		width = w;
		height = h;
		depth = z;
	}
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}


	public String getParentName() {
		return parentName;
	}


	public void setParentName(String parentName) {
		this.parentName = parentName;
	}


	synchronized public void excludeBeyond(int exludeIndicesBorder, int direction, String requestguid) {
		int n;
		RequestBorder rb = new RequestBorder();
		// create a border object, set mode to "border" (not "set" here)
		
		rb.type = RequestBorder._TYPE_VALUES;
		rb.requestGuid = requestguid;
		
		if (direction>=1){
			
			rb.upperBorder = exludeIndicesBorder;
			rb.lowerBorder = 0;
		}
		if (direction<1){
			rb.upperBorder = xyPlane.coordinates.size() ;
			rb.lowerBorder = exludeIndicesBorder;
			
		}
		 
		requestBorders.put(requestguid, rb);
	}


	public void removeExclusion(String requestguid) {
		requestBorders.remove(requestguid) ;
	}

	public double getLinearDistanceX(double x1, double x2){
		return getLinearDistance(x1, x2, width);
	}
	public double getLinearDistanceY(double y1, double y2){
		return getLinearDistance(y1, y2, height);
	}
	
	
	public double distance( double x1 , double y1, double x2 , double y2){
		
		return -1;
	}
	
	public double getLinearDistance(double x1, double x2, int maxDist) {
		double result = -1.0;
		double xd0, w;
		
		
		
		result = ( x1 - x2) ;
		
		if (borderMode == __BORDER_NONE)
		{
			
			
			w = (double)(maxDist*1.0) ;
			if ( Math.abs(result) > w / 2.0) {
				// initial distance larger than 50% of the width of the area?
				// -> so it could be just at the left and the right border ->
				// subtract the area width
				
				xd0 = (w - Math.max(x1,x2) + (Math.min(x1,x2))) ;
				if (Math.abs(xd0) < Math.abs(result)){
					result = xd0;
				}
			}
		}
		
		return result;
	}


	public int updateAsCloneFrom( Neighborhood srcNB ) {
		//
		int r=-1;
		try{
			
			
			if ((xyPlane.coordinates == null) || (xyPlane.coordinates.size()==0) || (xyPlane.positionTableMap.values().size()==0)){
			
				xyPlane.coordinates = new ArrayList<Coordinate2D>(srcNB.xyPlane.coordinates ) ;
				xyPlane.positionTableMap = new HashMap<Integer,Integer>(srcNB.xyPlane.positionTableMap);
				
			}else{

				xyPlane.coordinates = new ArrayList<Coordinate2D>(srcNB.xyPlane.coordinates ) ;
				xyPlane.positionTableMap = new HashMap<Integer,Integer>(srcNB.xyPlane.positionTableMap);
				
			}
			
			r = 0;
		}catch(Exception e){
			r = -3;
		}
		return r;
	}

	/**
	 * 
	 * direction = 1 -> we are looking for the infimum, i.e. the index of the coordinate with 
	 *                  largest value still smaller than the target values 
	 * direction = 2 -> we are looking for the infimum, i.e. the index of the coordinate with 
	 *                  largest value still smaller than the target values 
	 * 
	 * @param targetX
	 * @param currentIndex
	 * @param direction
	 * @return
	 */
	private int divisionSearch( double targetX , int leftIndex, int rightIndex, int direction){
		double xv, xvn = 0,xvf=0;
		int resultIndex = -1;
		int dix ;
		
		
		
		
		if (direction<=1){  // direction<=1 == infimum
			
			if (rightIndex<leftIndex){
				int k=rightIndex; rightIndex = leftIndex; leftIndex=k;
			}
			xv = xyPlane.coordinates.get(leftIndex ).cxValue ;
			
			if (resultIndex>=0){ 
				return resultIndex;
			}
			
			// the left index still yields a x-value too large... 
			if (xv>targetX){
				rightIndex = leftIndex;
				resultIndex = divisionSearch( targetX , (int)leftIndex/2,rightIndex, direction);
			}else{
				// now the current index yields a x-value smaller than the target...
				xvn = xyPlane.coordinates.get(leftIndex+1 ).cxValue ;
				// is the value behind the next index larger again? -> then we would have found
				if (xvn>targetX){
					resultIndex = Math.max( 0,leftIndex-3); // a small border to the left...
					out.print(2, "found index at infimum ("+(Math.round(xv*10.0)/10.0)+") for target value ("+(Math.round(targetX*10.0)/10.0)+") = "+ resultIndex);
					return resultIndex ;
				}else{
					// we are too small, so we add the half of our index value
					// is even our right border too small?
					xvf = xyPlane.coordinates.get( rightIndex ).cxValue ;
					if (xvf<targetX){
						
						resultIndex = divisionSearch( targetX , rightIndex,rightIndex + ( xyPlane.coordinates.size()+ rightIndex)/2, direction);
					}else{
						dix = Math.max( 1,Math.abs( ((rightIndex-leftIndex)/2)));
						resultIndex = divisionSearch( targetX , (int)(leftIndex + dix),rightIndex, direction);
					}
				}
			}
		} // direction<=1 == infimum
		else{  // ---------------------------------------------------------------------------------------------------------------------------------
			
			
			
			if (direction>=2){  // direction>=2 == supremum,
				
				int infix = divisionSearch( targetX , leftIndex, rightIndex, 1);
				int supix = infix+1;
				xv = xyPlane.coordinates.get(supix).cxValue ;
				while ((supix<xyPlane.coordinates.size()-1) && ( xv < targetX )){
					supix++;
					xv = xyPlane.coordinates.get(supix).cxValue;
				}
				out.print(2, "found index at supremum ("+(Math.round(xv*10.0)/10.0)+") for target value ("+(Math.round(targetX*10.0)/10.0)+") = "+ supix);
				resultIndex = supix;
			} // direction>=2 == supremum
		}
		
	 
		return resultIndex;
	}
	// this relies on the fact that the xyPlane contains coordinates which are sorted along the x-dimension
	// a recursive half-split should provide the results pretty fast 
	public int[] getInfimumSupremumForLocations(double xs, double xe) {
		
		int ixi,ixs;
		double k;
		int[] infsup = new int[2] ;
		infsup[0] = -1; infsup[1] = -1 ;
		
		ArrayList<Coordinate2D> cc = xyPlane.coordinates ;
		//HashMap<Integer,Integer>cmap = (HashMap<Integer, Integer>) xyPlane.positionTableMap;
		
		if (xs>xe){ k=xs;xs=xe;xe=k;}
		
		try{
			// search infimum
			ixi = divisionSearch( xs, (int)(cc.size()/2),(int)(cc.size()/2), 1) ;
			// search supremum
			ixs = divisionSearch( xe, (int)(cc.size()/2), (int)(cc.size()/2), 2) ;
			
			 
			infsup[0] = xyPlane.coordinates.get(ixi).getIndex();  
			infsup[1] = xyPlane.coordinates.get(ixs).getIndex(); 
			
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		
		return infsup ;
	}


	public double[] adjustSpatialPositionsToBorderSettings(double xpos, double ypos, double radius, int neighborhoodBorderMode) {
		
		double[] xyPos = new double[2] ;

		if (neighborhoodBorderMode == Neighborhood.__BORDER_NONE) {
			if (xpos > width ) {    
				xpos = xpos - width; // screen wrap
			} else {
				if (xpos < 0) {
					xpos = xpos + width;
				}
			}
			if (ypos > height ) {
				ypos = ypos - height;
			} else {
				if (ypos < 0) {
					ypos = ypos + height;
				}
			}
			double rf=1.6;
			
			// if (xpos<=1)xpos=1;
			if (xpos<=(radius/(rf*1.1)))xpos=(radius/(rf*1.1));
			if (xpos>=width-(radius/rf))xpos=width-(radius/rf);

			//if (ypos<=1)ypos=1;
			if (ypos<=(radius/(rf*1.1)))ypos=(radius/(rf*1.1));
			if (ypos>=height-(radius/rf))ypos=height-(radius/rf);
		} // __BORDER_NONE ?
			
		xyPos[0] = xpos ;
		xyPos[1] = ypos ;
		
		return xyPos;
	}


	
} // class Neighborhood





class QTask{
	
	Item item;
	int actionID;
	
	public QTask(int action, Item item){
		this.item = item;
		actionID = action;
	}
	
}


class Item{
	
	int index;
	double x; 
	double y;
	double z; 
	double radius;
	
	public Item( int index, double x, double y, double radius ){
		
		this.index = index;
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
	
	
}


/**
 * 
 * the central idea is to create epsilon-columns/rows, which can
 * have different height
 * 
 * @author kwa
 *
 */
class VirtualTableMapping{
	
	Map<Integer,Integer> positionTableMap = new HashMap<Integer,Integer>();
	// this map is defined as <I,I> = <particles index (from dynamic field), coordinates position>
	// particles may be completely unordered, so we need to create a virtual table for fast navigation
	// around neighborhoods
	// 2. external index -> epsilon-column
	// wrapped into an object with additional functionality
	// Map<Integer,Vector<Integer>> position2DTableMap = new HashMap<Integer,Vector<Integer>>();
	
	public VirtualTableMapping(){
		
	}
	
	
}


class Dimension {
	
	ArrayList<Coordinate1D> coordinates = new ArrayList<Coordinate1D>();
	
	// we use a int,int - map for fast access to the list of coordinates using an index
	// so we create a "virtual" unordered outside-index, which behaves as if it would be ordered
	Map<Integer,Integer> positionMap = new HashMap<Integer,Integer>();
	String name = "";
	
	Neighborhood nParent ;
	
	public Dimension( Neighborhood nparent, String _name){
	
		name = _name;
		nParent = nparent;
	}
	
	public void update(int index, double cvalue, String name) {
		Coordinate1D lastItem;
		int p;
		
		// first checking whether it is available in 
		if (containsIndex(index)==false){
			lastItem = new Coordinate1D(cvalue,index,name);
			add( lastItem  ) ;
		}else{
			set(index,cvalue) ;
			p = positionMap.get(index);
			lastItem = coordinates.get(p) ;
		}
		
		sort(0,lastItem);
	}

	public void set(int index, double cvalue) {
		 int p;
		 Coordinate1D cp;
		 
		 if (positionMap.containsKey(index)){
			p = positionMap.get(index);
			cp = coordinates.get(p);

			cp.setCvalue(cvalue);
		}
	}

	public void add( Coordinate1D c ){
		int n;
		
		n = coordinates.size(); 
		coordinates.add(c);
		positionMap.put(c.particleIndex , n);
	}
	
	public Coordinate get(int index){
		int p;
		Coordinate cp;
		 
		p = positionMap.get(index );
		cp = coordinates.get(p);

		return cp;
	}
	
	public int size(){
		return coordinates.size();
	}
	
	/** 
	 *  ATTENTION: this does not return all items in the n-dimensional surround, but only the surround
	 *             within the single selected dimension.</br>
	 *             Later, when combining the intra-dimensional surrounds to a cross-dimensional surround
	 *             we will find too many items, that we will have to filter, nevertheless, we have a good guess!
	 *             If the items are "regularly" arranged, we can reduce the search distance by 1.2 * root(n)() (in 2 d -> sqrt)</br></br> 
	 *             
	 *  this method returns an array of index values (external index, i.e. referring to particles in RepulsionField), 
	 *  that are near the requested index value; </br>
	 *  to achieve that, the index is translated into the internal index value, followed by lookup
	 *  in the ArrayList "coordinates": This list is always ordered !!! </br></br>
	 *  
	 *  For the retrieval of the surround, the value of "borderMode" is referred to, which 
	 *  defines the actual topology of the grid area (rectangle, torus, cylinder, partially open etc.</br></br>
	 *  
	 *  For partially open topologies, the parameter "borderInfinityFactor" applies,
	 *  
	 *  This surround considers only those particles that are located in a horizontal band of height averageDistance/2;</br>
	 *  this means that we can NOT just take the linear surround in the correlations array;
	 *  instead we have to select the items explicitly and count 
	 */


	
	public int[] getSurround( int index, double surroundRadius ){
		int[] surround = new int[0];
		int stepsaround;
		Vector<Integer> candidateItems =  new Vector<Integer>();
		int ap,pb,pa,sp,cc;
		double avgDist = nParent.averageDistance ;
		Coordinate1D ci;
		double cx0, cxpL,cxpR;
		
		// asking for the index of xDim, which is the key in the positionMap
		if (containsIndex(index)){
			// get the anchor point in the coordinates collection
			ap = positionMap.get(index);
			cx0 = coordinates.get(ap).cvalue;
			/*
			 * coordinates contains all items sorted according to xdim.
			 * that is, asking for a certain distance we will get a vertical band
			 * the same we will do later with ydim, using the results from here as constraint (or checking the intersection )   
			 */
			cc=0; int i=0;
			boolean withinBounds=true;
			while ((withinBounds)){
				// border handling according to settings !!
				// TODO: for now, simply with border all
				pb = ap-i;
				ci = coordinates.get(pb);
				cxpL = ci.cvalue;
				int m=0;
				// left side of x-bounded channel
				if ( cxpL > cx0 - surroundRadius){
					sp= ci.particleIndex;
					candidateItems.add( sp) ;
					m++;
				}
				// right side of x-bounded channel
				if ( cxpL > cx0 - surroundRadius){
					sp= ci.particleIndex;
					candidateItems.add( sp) ;
					m++;
				}
				
				
				i++;
			}// ->
			
			
			/*
			surround = new int[pa-pb];
			
			
			// dependent on the border mode, and the type of the request,
			// determine the neighborhood
			int z=0;
			for (int i=pb;i<pa;i++){
				sp = coordinates.get(i).index;
				surround[z] = sp;
				z++;
			}
			*/
			
			
		} // index available
		
		return surround;
	}

	
	public boolean containsIndex(int index) {
		boolean rB=false;
		int p;
		
		rB= positionMap.containsKey( index );
		
		return rB;
	}

	public void sort(int direction, Coordinate triggeringItem) {
		int p,index;
		Object tItem;
		Object[] items;
		Coordinate cc;
		 
		tItem = (Object)triggeringItem ;
 
		Collections.sort(coordinates);
		 

		items = coordinates.toArray(); 
		
		p = Arrays.binarySearch( items, tItem);
		
		if (p>=0){
			positionMap.put(triggeringItem.particleIndex, p);
			// now, from here on, we have to adjust all entries in the positionMap 
			for (int k=p+1;k<coordinates.size();k++){
				cc = coordinates.get(k) ;
				index = cc.particleIndex ;
				positionMap.put(cc.particleIndex, k);
			}// k->
		}
	}


}

 
