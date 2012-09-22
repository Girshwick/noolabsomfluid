package org.NooLab.chord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.NooLab.chord.util.ThrdUtilities;
import org.apache.commons.collections.CollectionUtils;
 
   


/**
 * 
 * The multi digester provides an easy way to implement multi-threaded digesting
 * of large collections of items, if (!) the items can be calculated independent of
 * each other.<br/>
 * Often, items become independent if the integration level is chosen sufficiently high, e.g.
 * instead of working on words, ons should work on documents.<br/> 
 * 
 * Alternatively, this organizer of multi-threaded digesting is suited to work on collections
 * of items, if only a read access is performed, storing results in an intermediate structure.<br/>
 * 
 * The MultiDigester does not know anything about your data, it just organizes the threads, which call
 * a routine within your calling class. In order to establish the linkage, you have to create an inner class to that
 * worker class, and that inner class has to implement a small, very simple interface, comprising just 1 single routine: perform(id);<br/>
 * 
 * The routine is implemented by the inner class, and the interface knows about the address of this routine's embedding class
 * through the interface... a simple (so-called) callback pattern.<br/>
 * 
 * The MultiDigester also provides utilities to assign particular index positions of an array (or a collection) to the 
 * threads, the only information needed is the length (size) of the array (collection);<br/>
 * 
 * Finally, you start the calculation by invoking the digester's execute() routine.<br/>
 * 
 * The Threads are organized each as an object, all threads are organized into an array for easy access. <br/>
 * 
 * It is possible to provide the size for each of the tasks, e.g. as image sizes or document sizes; such, the digester
 * knows roughly about a hanging thread, assuming a O(n^3) complexity (quite a worse case...);<br/>
 * Such threads can be killed automatically, if the option has been set accordingly. 
 * 
 */
@SuppressWarnings("static-access")
public class MultiDigester   {

	public static final int _PRIORITY_LO  = 3;
	public static final int _PRIORITY_MID = 5;
	public static final int _PRIORITY_HI  = 8;
	
	int threadcount; 
	int threadPriority;
	
	ItemSet[] itemsubset  ;
	int[] itemsIndex;
	int itemCount=0;
	
	String[] debugStrings ;
	int[] taskSizes = new int[0];
	double taskDelays[] = new double[0];
	
	double maxMinutes = 2.5 ;
	
	int diagnosticPrintOut = 0;
	int amorphousPill = 0;
	
	boolean autoStoponTaskDelay = false ;
	boolean balancedExecution = false ;
	boolean shuffledIndices = false ;
	
	
	
	// ..................................................
	
	private IndexedItemsCallbackIntf indexedItemsRoutine;
	
	private IndexedItems2DCallbackIntf coordinateItemsRoutine;
	
	// private CompletionEventMessageCallIntf completionObserver = null;
	
	private ThreadedDigester[] workerThreads ;

	private ThrdUtilities util = new ThrdUtilities() ;
	
	
	// ..................................................

	HashMap<Integer,Integer> processContent ; 
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	// we provide an overloaded constructor for providing different interfaces
	
	/**
	 * a constructor for 2-dimensional arrays, like pixels from an image (if the image is provided as coordinates)<br/>
	 * 
	 * internally, the 2 dimensions are translated into n 1-dimensional lists (n=threadcount)
	 * 
	 * @param threadcount
	 * @param routine
	 */

	public MultiDigester(int threadcount, IndexedItems2DCallbackIntf routine){
		
		coordinateItemsRoutine = routine;
		
	}
		
		
	/**
	 * a constructor for 1-dimensional lists
	 * 
	 * @param threadcount
	 * @param routine  the callback-address = a class which implements the interface "IndexedItemsCallbackIntf"
	 * 
	 */
	public MultiDigester(int threadcount, IndexedItemsCallbackIntf routine){
		
		this.indexedItemsRoutine = routine;
		this.threadcount = threadcount;
		
		itemsubset = new ItemSet[threadcount] ;
		
		// creating n subsets, n = threadcount
		initializeSubSets() ;
		
		reset(); // creates the threads via initializeThreads()
		
	}
	 
	
	public void reset(){
		
		initializeThreads();
	}
	
	public void close(){
		amorphousPill = 1;
	}
	 
	
	protected void initializeSubSets(){
		int i=0;
	
		for (i=0;i<threadcount;i++){
			itemsubset[i] = new ItemSet() ;
		}

	}
	
	/**
	 * 
	 * creating the set of thread-objects, putting them into wait state upon start
	 * execute() will set active -> true, which will bring the thread to work on the data 
	 * 
	 */
	private void initializeThreads(){
		int k;
		
		if (workerThreads!=null){
			k=0;
			for (int i = 0; i < workerThreads.length; i++) {
				workerThreads[i].stop();
				workerThreads[i] = null;
			}
			workerThreads = null;
		}
		
		// System.gc() ; 

		try {
			// Thread.sleep(0,5) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		workerThreads = new ThreadedDigester[threadcount];
		
		for (int i=0;i<threadcount;i++){
			
			workerThreads[i] = new ThreadedDigester(i);
			
			try {
				Thread.sleep(2) ;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 
	 * @param there are constants "MultiDigester._PRIORITY_*"
	 */
	public void setPriority( int priority){
		threadPriority=priority;
		
		if ((workerThreads!=null) && (workerThreads.length>0)){
			
			for (int i=0;i<threadcount;i++){
				if (workerThreads[i]!=null){
					workerThreads[i].setPriority(threadPriority);
				}
			}
			
		}
	}
	
	/**
	 * 
	 * distributing the items into the different subsets;
	 * each subset will then be assigned to one of the threads
	 * 
	 * @param items
	 */
	public void prepareItemSubSets( int itemcount, int offset ){
		int n=0, id=0 ;
		int item;

		itemCount = itemcount;
		itemsIndex = new int[itemcount] ;
		
		if (offset>0){
			offset=0;
		}
		
		for (int i=0;i<itemsubset.length;i++){
			itemsubset[i].items.clear();	
		}
		
		
		for(n=offset;n<itemsIndex.length;n++){
			
			itemsIndex[n] = n;
			
			 
				item = itemsIndex[n];
				
				id = id % threadcount;

				addItem(id, item) ;
				
				id++;
		 
		}
		n=0;
		
		if (shuffledIndices){
			n=0;
			for (int i=0;i<itemsubset.length;i++){
				Collections.shuffle( itemsubset[i].items );
				Collections.shuffle( itemsubset[i].items );
			}
		}
		
	}
	
	public void importDebugStrings( int[] tasksizes ){
		int minsize = -1, maxsize = -1 ;
		double tr ;
		
		
		
		taskSizes = new int[tasksizes.length] ;
		
		for (int i=0;i<taskSizes.length;i++){
		
			if (i<tasksizes.length){
				taskSizes[i] = tasksizes[i]; 
			}
		}
		 
		// calculate potential delay on earliest task
		
		taskDelays = new double[ taskSizes.length] ;
		minsize = util.minimum(tasksizes) ;
		maxsize = util.maximum(tasksizes) ;
		
		for (int i=0;i<taskSizes.length;i++){
			
			tr = (taskSizes[i])/(minsize+1) * (maxsize/(minsize+1));
			
			tr = tr*tr*tr ;
			
			taskDelays[i] = tr ;
		}
		
		
	}

	
	
	public void importDebugStrings( String[] debugstr ){
	
		debugStrings = new String[itemsIndex.length] ;
		
		for (int i=0;i<debugStrings.length;i++){
		
			if (i<debugstr.length){
				debugStrings[i] = debugstr[i]; 
			}
		}
		
	}
	
	
	
	
	protected void addItem( int setId, int itemindex){
	
		itemsubset[setId].items.add(itemindex);
	 
	}

	/**
	 * 
	 * this methods sets the variable "active" , thereby starting all the threads.<br/>
	 * 
	 * It waits until all threads have been completed or stopped.
	 * 
	 */
	public void execute() {
		int closedthreadSum = 0;
		String notFinished = "", dbugmsg = "";
		double maxDelayFactor;
		long firstFinishnanoTime = 0;
		int z;
		
		
		
		// we create n threads (n=threadcount) as objects, which runs 1 thread each;
		// each thread works on its assigned collection of items: itemsubset[]
		// inside the thread, we call routine.perform( id ) ;

		try {
			
			closedthreadSum = 0;
			
			// set all active
			for (int i = 0; i < threadcount; i++) { 
				
				try {
					// Thread.currentThread().sleep(1);
				} catch (Exception e) {

				}
				
				if (itemsubset[i].items.size()>0){

					workerThreads[i].itemindex=0;
					workerThreads[i].active = true; workerThreads[i].loopFinished=false;
					
				} else {
					workerThreads[i].alive = false ; workerThreads[i].active = false;
				}
			}

			// get starting time = now
			
			// expected latest completion time, based on taskDelays[]
			
				maxDelayFactor = util.maximum(taskDelays) ;
				
			//  latest expected time
				
			
			//  enter the waiting loop 
			z = 0; // a simple counter
			while (closedthreadSum < threadcount) {
				closedthreadSum = 0;
				notFinished = "";
				dbugmsg = "";
				int ix,p;
				
				for (int i = 0; i < threadcount; i++) {

					if ( (workerThreads[i].loopFinished) || (workerThreads[i].active == false) || ((workerThreads[i].alive == false))){
						closedthreadSum++;
					} else {
						notFinished = notFinished + i + "("+workerThreads[i].selectedIndexValue+"), ";
						
						if ((debugStrings!=null) ){
							
							ix = workerThreads[i].selectedIndexValue ;
							// p = processContent.get(ix) ;
							if ((ix>=0) && (ix<debugStrings.length)){
								dbugmsg = dbugmsg + "  " +debugStrings[ix] + "\n" ;
							}
						}
					}
				}

				if ((threadcount-closedthreadSum==1) && (firstFinishnanoTime==0)){
					
					// firstFinishnanoTime == now
				}

				Thread.currentThread().sleep(10);
				
				if (closedthreadSum < threadcount) {

					if (diagnosticPrintOut>=4){
						Thread.currentThread().sleep(10);
						System.out.println("  unfinished threads : " + notFinished);

						if ((diagnosticPrintOut>=2) && (threadcount -closedthreadSum <= 2)){
							System.out.println( dbugmsg );
						}
						
					}
					
				} // closedthreadSum < threadcount ?
				
				
				if ((z>5) && (z % 10 ==0) && (autoStoponTaskDelay==true)){
					// check latest expected finishing time, time distance
					
				}
				
				z++ ;
			} // while (closedthreadSum < threadcount) ->

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (diagnosticPrintOut>=4){
			System.out.println("\nall threads ("+threadcount+") have been finished.");
		}
	}

	
	public void stopAll(){
		
		for (int i=0;i<workerThreads.length;i++){
			stop(i); 
		}
	}
	
	
	public int stop(int index){
		int resultState= -1;
		
		try {
			if ((index < workerThreads.length) && (index >= 0)) {
				resultState = 1;
				workerThreads[index].stop();
				resultState = 2;

				Thread.currentThread().sleep(10);
				
				resultState = 3;
				if (workerThreads[index].thrd.isAlive()){
					resultState = 5;
				}else{
					resultState = 0;
				}
			}

		} catch (Exception e) {
			// e.printStackTrace();
		}

		return resultState;
	}
	
	
	
	public int getRunningThreadsCount() {
		int c=0;
		
		for (int i=0;i<workerThreads.length;i++){
			if (workerThreads[i].alive){
				c++;
			}
		}
		return c;
	}


	public void testCall( int id){
		
		
		System.out.println("just before calling back the working routine") ;
		indexedItemsRoutine.perform( -1, id ) ; // -1 = processID = not threaded 
		
	}
	
	protected void performWork( int processID, int id){
		
		indexedItemsRoutine.perform( processID, id ) ;
	}
	
	
	// ====================================================
	class ThreadedDigester implements Runnable{

		private int index, itemindex , selectedIndexValue; 
		 
		private Thread thrd;
		
		String thrdName="";
		boolean alive,active = false, loopFinished=false;
		
		
		
		// . . . . . . . . . . . . . . . . . . . . . . . . 
		public ThreadedDigester(int ix){
			index = ix;
			thrdName = "multidigThrd-"+ix ;
			thrd = new Thread(this,thrdName) ;
			
			thrdName = thrd.getName() ;
			thrd.start() ;
			
		}
		public void setPriority(int threadpriority) {
			thrd.setPriority( threadpriority ) ;
		}
		// . . . . . . . . . . . . . . . . . . . . . . . . 
		
		public void startDigester(){
			
			active = true;
			
		}
		
		public void stop(){
			
			try{
				alive=false;; active=false;
				thrd.interrupt();
				// Thread.sleep(5) ;
			}catch(Exception e){
			}
			loopFinished=true;
		}
		
		 
		public void run() {
			int z=0;
			try {
				
				loopFinished=false;
				alive = true; 
			
				itemindex = 0;
				selectedIndexValue = -1;
			
			 
				while ((alive) && (amorphousPill<=0)) {

					if ( active == false ){
						
						z++;
						try{
							amorphousPill = indexedItemsRoutine.getClosedStatus() ;
							
							if (diagnosticPrintOut >= 4){
								Thread.currentThread().sleep(1);
								System.out.println("thread ("+index+") is waiting...") ;
							}else{
								//if (z>10000)
								{
								Thread.currentThread().sleep(1); // prevent processor activity 100%...
								}
							}
							if (amorphousPill>0){
								alive=false; active=false;
								break;
							}
						}catch(Exception e){}
					} 
					
					if (active){
						z=0;
						if ((balancedExecution) && (itemindex==0)){
							// we activate in the order of the threads == in the order of original indexes
							while (earlierWorkerStarted(index)==false){
								Thread.currentThread().sleep(1); 
							}
						}
						if (itemindex < itemsubset[index].items.size() ){
							
							selectedIndexValue = itemsubset[index].items.get(itemindex) ;
							
							if (diagnosticPrintOut>=4){
								Thread.currentThread().sleep(1);
								System.out.println("thread ("+index+") is working on item : " + selectedIndexValue) ;
							}
							
							performWork( index, selectedIndexValue ) ;
							
							/*
							if ((balancedExecution) && (itemindex<itemCount-threadcount-1)){
								while (workerIsAhead(index, selectedIndexValue )){
									Thread.currentThread().sleep(1); 
									// waiting for the slowest worker
								}
								
							}
							*/
						}
						
					
						itemindex++;
					}

					if (itemindex >=  itemsubset[index].items.size()){
						active = false ;
						loopFinished=true;
						// on option: stop thread completely, or (=default) keep thread alive to avoid frequent re-creation
					}
					
				} // waiting thread

			} catch (InterruptedException e) {
			 
				e.printStackTrace();
			}
			loopFinished=true;
			if (diagnosticPrintOut>=2){
				System.out.println( "Thread <"+thrdName+"> has been stopped");
			}
		}
 
		
		/**
		 * 
		 * @param thrdIndex the index of the worker thread
		 * @param itemIndex the index value of the item it has been working on
		 * @return
		 */
		protected boolean workerIsAhead( int thrdIndex, int itemIndex ){
			boolean rB = false;
			ThreadedDigester worker;
			int minW, minix=999999999;
			
			for (int i=0;i<workerThreads.length;i++){
				worker = workerThreads[i];
				if ((worker!=null) && (worker.alive)){
					if (minix>worker.itemindex){
						minix = worker.itemindex;
						minW = i;
					}
				}
			}
			
			if (minix < itemindex-(2.3*threadcount)){
				rB=true;
			}
			return rB;
		}
		
		/**
		 * this checks whether a worker with a lower index value already worked on its first item
		 * 
		 * @param thrdIndex
		 * @return
		 */
		protected boolean earlierWorkerStarted( int thrdIndex ){
			boolean rB = false;
			ThreadedDigester worker;
			
			if (thrdIndex==0){
				if (itemindex==0){
					return true;
				}
			}
			if ((itemindex==1)&& (thrdIndex<threadcount)){
				// we check for all workers that just started, whether there companions with higher index
				// also have started, if not we return false, which means that his worker will wait a bit
				rB=true;
				for (int i=thrdIndex+1;i<workerThreads.length;i++){
					worker = workerThreads[i];
					if ((worker!=null) && (worker.alive)){
						rB = worker.itemindex>0;
					}
					if (rB==false){
						return false;
					}
				}
			}
			// for all workers with lower index we check if they have started at all
			// if not this worker has to wait
			for (int i=thrdIndex-1;i>=0;i--){
				worker = workerThreads[i];
				if ((worker!=null) && (worker.alive)){
					rB = worker.itemindex>0;
				}
			}
			
			return rB;
		}
		
	}

	// ====================================================

	
	// ------------------------------------------------------------------------

	public int getThreadcount() {
		return threadcount;
	}



	public void setThreadcount(int threadcount) {
		this.threadcount = threadcount;
	}



	public static int getProcessorCount() {
		// 
		return 1;
	}


	public ItemSet[] getDocumentsubset() {
		return itemsubset;
	}



	public void setDocumentsubset(ItemSet[] documentsubset) {
		this.itemsubset = documentsubset;
	}


	public void setDiagnosticPrintOut(int diagnosticPrintOut) {
		this.diagnosticPrintOut = diagnosticPrintOut;
	}


	public boolean isAutoStoponTaskDelay() {
		return autoStoponTaskDelay;
	}


	public void setAutoStoponTaskDelay(boolean autoStoponTaskDelay) {
		this.autoStoponTaskDelay = autoStoponTaskDelay;
	}


	public boolean isBalancedExecution() {
		return balancedExecution;
	}


	public void setBalancedExecution(boolean flag) {
		balancedExecution = flag;
	}


	public void setShuffledIndices(boolean flag) {
		
		shuffledIndices = flag;
	}


 

}


