package org.NooLab.chord;

import java.util.HashMap;
import java.util.Vector;

import org.NooLab.chord.util.ThrdUtilities;
 
   


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

	int threadcount; 
	int threadPriority;
	
	ItemSet[] itemsubset  ;
	int[] itemsIndex;
	
	String[] debugStrings ;
	int[] taskSizes = new int[0];
	double taskDelays[] = new double[0];
	
	double maxMinutes = 2.5 ;
	
	int diagnosticPrintOut = 0;
	
	boolean autoStoponTaskDelay = false;
	
	// long autoStopDelay = 1000*60*10; // 10 minutes ....
	
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
		
		reset();
		
	}
	
 
	
	public void reset(){
		
		initializeThreads();
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

		if (workerThreads!=null){
			for (int i = 0; i < workerThreads.length; i++) {
				workerThreads[i] = null;
			}
			workerThreads = null;
		}
		
		workerThreads = new ThreadedDigester[threadcount];
		
		for (int i=0;i<threadcount;i++){
			workerThreads[i] = new ThreadedDigester(i);
		}
		
	}
	
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
			// set all active
			for (int i = 0; i < threadcount; i++) {
				
				try {
					// Thread.currentThread().sleep(1);
				} catch (Exception e) {

				}
				
				if (itemsubset[i].items.size()>0){
					workerThreads[i].active = true;
				} else {
					workerThreads[i].alive = false ;
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

					if (workerThreads[i].active == false) {
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
				
				if (closedthreadSum < threadcount) {

					Thread.currentThread().sleep(1);

					if (diagnosticPrintOut>=4){
						Thread.currentThread().sleep(1800);
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
		
		boolean alive,active = false;
		
		
		
		// . . . . . . . . . . . . . . . . . . . . . . . . 
		public ThreadedDigester(int ix){
			index = ix;
			
			thrd = new Thread(this) ;
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
			thrd.interrupt();
		}
		
		@SuppressWarnings("static-access")
		public void run() {
			
			try {
				
			
				alive = true; 
			
				itemindex = 0;
				selectedIndexValue = -1;
			
			 
				while (alive) {

					if ( active == false ){
						// thrd.sleep(100);

						if (diagnosticPrintOut >= 4){
							Thread.currentThread().sleep(10);
							System.out.println("thread ("+index+") is waiting...") ;
						}
					} 
					
					if (active){
						
						selectedIndexValue = itemsubset[index].items.get(itemindex) ;
						
						if (diagnosticPrintOut>=4){
							Thread.currentThread().sleep(2);
							System.out.println("thread ("+index+") is working on item : " + selectedIndexValue) ;
						}
						
						// itemsubset[index].currentIndex = itemindex ;
						
						performWork( index, selectedIndexValue ) ;
					
						itemindex++;
						
						if (itemindex >=  itemsubset[index].items.size()){
							alive= false;
							active = false ;
						}
					}

				} // waiting thread

			} catch (InterruptedException e) {
			 
				e.printStackTrace();
			}
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


 

}


