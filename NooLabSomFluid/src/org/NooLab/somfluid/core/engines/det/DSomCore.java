package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.Assignates;
import org.NooLab.somfluid.core.engines.det.results.SomTargetResults;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.data.DataSampler;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.communication.LatticeFutureVisor;
import org.NooLab.somfluid.env.communication.NodeTask;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;

 

public class DSomCore {

	DSom dSom;
	
	
	ModelingSettings modset;
	DataSampler dataSampler = new DataSampler();
	
	
	int targetVarColumn= -1;
	int[] usagevector ;
	int[] blacklistPositions ;
	ArrayList<Double> useIntensityVector;
	
	// contains methods for determining the usage vector, inclusive selections for 
	// evolutionary optimization 
	Assignates assignates ;
	
	ArrayList<Integer> sampleRecordIDs;
	
	int absoluteRecordCount ;
	int somSteps; // initial value is taken from ModelingSettings
	 
	double neighbourhoodRadius ;
	double mapRadius = 1.0;
	double timeConstant;
	double learningRate, initialLearningRate;
	
	ModelingSettings modelingSettings ;  
	ClassificationSettings  classifySettings ;
	
	
	// ------------------------------------------
	ArrUtilities arrutil = new ArrUtilities();
	PrintLog out;
	
	// ========================================================================
	public DSomCore(DSom dsom) {
		 
		dSom = dsom;
		// ... dSom.somData;
		out = dSom.out;
		
		// out.print(2, "requesting neighborhood for <11> from particle field via Som-Lattice");
		// dSom.somLattice.getNeighborhoodNodes(11) ; out.delay(200);
		// dSom.somLattice.getNeighborhoodNodes(15) ;
		
		dataSampler.setModelingSettings( dSom.modelingSettings );
		
		modelingSettings = dSom.modelingSettings ;
		classifySettings = modelingSettings.getClassifySettings() ; 
		
		prepareSomProcess();
		
	}
	// ========================================================================


	// ------------------------------------------------------------------------
	/**
	 * 
	 * the ClassesDictionary contains the 0/1 class indicator<br/><br/>
	 * we need to do it close to the SOM, since the class assignment could change while learning:<br/>
	 * there are learning modes, which optimize the SOM, or some stats params of it, or are
	 * dependent on the node profiles... in such cases, the classes assignment can change ONLINE
	 * WHILE executing the SOM!!  <br/><br/>	 
	 *  
	 * 
	 * besides the binary indicator, it also contains<br/>
	 * a vector with ID, true (physical record number), kind of sample (in TV modeling), 
	 * 
	 */
	private void createClassesDictionary(){
		
		// somClassesDictionary = new ClassesDictionary();
		
		// providing the reference to the modelingSettings object
		// modset.setSomClassesDictionary( somClassesDictionary ) ;
	}
 
	// ========================================================================
	
	
	// ========================================================================
	
	
	protected void prepareSomProcess(){
	
		int tvColumnIndex=-1, indexColumnIndex=-1 ;
		
		DataTable dtable ;
		DataTableCol col;
		
		Variables variables ;
		Variable var;
		
		MetaNodeIntf node;
		
		ArrayList<Variable> blacklist;
		
		
		// determine the usagevector, excluding index columns tv column
	
		modset = dSom.modelingSettings ;
		
		dtable = dSom.somData.getDataTable();
	
		assignates = new Assignates( dSom ); 
		
		variables = dSom.somData.getVariables();
		
		usagevector = new int[variables.size()] ;
		blacklistPositions = new int[variables.size()] ;
		
		
		// note that the TV will be included into the usagevector, 
		// but it will be excluded from similarity considerations !
		
		for (int i=0;i<usagevector.length;i++){
			
			var = variables.getItem(i);
			col = dtable.getColumn(i);
			
			blacklistPositions[i] = 0;
			if (col.getStatisticalDescription().getMvCount()> 0.92*col.size()){
				var.setIsEmpty(1) ;
			}
			
			if ((var.isIndexcandidate()) || (col.getDataFormat()==0)){
				usagevector[i] = 0; 
				if (indexColumnIndex<0){ indexColumnIndex=i;}
				var.setIndexcandidate(true) ;
				continue;
			}
			 
			if ((var.isTVcandidate()) || ( var.getLabel().contentEquals( modset.getActiveTvLabel() ))){
				usagevector[i] = 0;
				if ((var.isTV()) || ( var.getLabel().contentEquals( modset.getActiveTvLabel() ))){
					usagevector[i] = 1; 
					tvColumnIndex=i;
				}
				continue;
			}
			 
			if (col.getDataFormat()>=8){
				usagevector[i] = 0; 
				continue;
			}
			
			if (col.getStatisticalDescription().getMvCount()> 0.11*col.size()){
				usagevector[i] = 0; 
				continue;
			}
			if (variables.getBlacklist().size()>0){
				
				boolean hb = (variables.getBlacklistLabels().contains(var.getLabel()) );
				if ( hb ){
					usagevector[i] = 0; 
					blacklistPositions[i] = 1;
					continue;
				}
			}
			
			usagevector[i] = 1;
		}
		
		
		// should be saved here
		// dSom.somLattice.distributeIntensionalitySurface().prepareWeightVector()
		
		somSteps = modset.getMaxSomEpochCount();
		absoluteRecordCount = dSom.somData.getRecordCount() ;
		
		// set blacklist, usevector to similarity object of lattice, and sim object of all nodes
		SimilarityIntf simIntf = null ;
		
		for (int i=0;i<dSom.somLattice.size();i++){
			
			node = dSom.somLattice.getNode(i);

			
			
			simIntf = node.getSimilarity();
			
			simIntf.setUsageIndicationVector(usagevector);
			simIntf.setBlacklistIndicationVector(blacklistPositions);
			
			simIntf.setIndexTargetVariable(tvColumnIndex) ;
			simIntf.setIndexIdColumn(indexColumnIndex) ;
			
			
			node.cleanInitializationByUsageVector( simIntf.getUsageIndicationVector() );
		} // i-> all nodes
		
		
											out.print(4,"similarity obj = "+simIntf.toString());

		// TODO: createClassesDictionary()
		
	}

	// ========================================================================
	
	
	// ========================================================================
	
	
	private void performDSom(){
		
		int loopcount=0;
		boolean done=false;
		
		
		 
		
		
		while ((done==false) && (dSom.getUserbreak()==false)){
		
			// the initial execution of the SOM
			// this is a simple run of the SOM. no sprites, for evolutionary optimization
			// which we could add here in this loop below. (loop level L3/L4)
			if (modset.getEvolutionaryAssignateSelection() == false){
				done = executeSOM();
			}
		
			if ((modset.getEvolutionaryAssignateSelection() ) && (dSom.getUserbreak()==false)){
				/*
				 *  in case of evolutionary modeling we need to integrate
				 *  - validation, for calculating the cost function
				 *  - sub-sampling = using significantly decreased samples for
				 *                   evolutionary optimization
				 */
				
			}
			
			if ((modset.getSpriteAssignateDerivation() )&& (dSom.getUserbreak()==false)){
				if ((modset.getMaxL2LoopCount()<0) ){
					// at least one loop to check for the new derivatives
					modset.setMaxL2LoopCount(1) ; 
				}
				
				
			} // SpriteAssignateDerivation() ?
			
			loopcount++;
			// http://theputnamprogram.wordpress.com/2011/12/21/technical-aspects-of-modeling/
			if ((modset.getMaxL2LoopCount()>0) && (loopcount> modset.getMaxL2LoopCount())){
				done = false;
			}
		  
			
		} // done ?
	}


	/**
	 *  executing the SOM;
	 *  1. basically, this just organized the learning epochs, where "organizing" meanse
	 *     to determine the actual sample of records (only their indexes, of course).
	 *     The first epoch is just basic priming, the second more extended priming of the SOM ;
	 *     for that we need not to take all records (only about 3% or at least 100 records)
	 *  2. the initial learning rate and neighborhood parameters are adjusted, becoming
	 *     smaller and smaller with increasing epoch count,
	 *  3. the data are sent to the core SOM process, i.e. the SOM now perceives the data vectors,
	 *     according to the settings of use vectors and weight vectors
	 *    
	 */
	private boolean executeSOM(){
		
		boolean rB=false;
		DSomDataPerception somDataPerception;
		double learningRate;
		
		int  limitforConsideredRecords = 0, actualRecordCount  ;
		
		int currentEpoch =0;
		 
		
		try{
			modset = dSom.modelingSettings ;	
			
			
			// absolute_record_count has been set in "setData(SomDataObject)" below...
			actualRecordCount = absoluteRecordCount;
			initialLearningRate = modset.getInitialLearningRate();
			learningRate = initialLearningRate; 
					
			// initial maximum radius for influence; it is covering the map only for rather small maps.
			// in very large maps there are several mechanisms (pre-sampling, multi-winners) that 
			// help to avoid a large initial radius
			// TODO make it more adaptive (asymptotically to a max size of N nodes)
			mapRadius = 1.2* (Math.sqrt( dSom.somLattice.size() )/2.0) ; 
			
	        // later we will have the possibility for particular block sampling schemes, 
			// utilizing the dataSampler object
			
			if (limitforConsideredRecords > 1) {
				actualRecordCount = limitforConsideredRecords;
			}
			 
			
			while ((currentEpoch < somSteps) && (dSom.getUserbreak()==false)) {
											out.print(1, "Som learning epoch... "+(currentEpoch+1));
											
				// before (re-)starting, we have to reset the node statistics, and the list of entries, except the weights of the vector
				clearNodesExtension( currentEpoch );
				
				// sample size is dependent on epoch and number of records
				adjustSampleAndSteps( currentEpoch, absoluteRecordCount ); // second epoch much too small... 
				actualRecordCount = sampleRecordIDs.size() ;
				
				adjustIntensityParameters( currentEpoch, actualRecordCount ) ;
				 
				// now feeding the data into the somLattice
				somDataPerception = new DSomDataPerception( dSom, sampleRecordIDs );
				
				somDataPerception.setLoopParameters( currentEpoch, somSteps );
				somDataPerception.setDynamicsParameters( learningRate, timeConstant , neighbourhoodRadius) ;
										//  epoch 1 :       0.2           411             6.5
				somDataPerception.go();
				currentEpoch++;
				
				
			} // currentEpoch -> maxSomEpochCount 
			
			
			if ( (dSom.sfProperties.getSomType()==SomFluidProperties._SOMTYPE_MONO) && 
				 (modelingSettings.getTargetedModeling() )){
			 
				dSom.somResults = new SomTargetResults( dSom, sampleRecordIDs, modelingSettings);
				dSom.somResults.prepare();
				
			 
				if (classifySettings.isFullSpelaDiagnostics()) {
					// list of results from multiple modeling, based on variation of central parameters 
					// resolution, ECR, alpha/beta target, samples, 
					// available only after evolutionary optimization
				}
				
			} // is it a targeted modeling?
			
			
			
			consoleDisplay();
			
			// TODO: release event message
			
			
			rB=true;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		out.print(1, "Basic Som learning has been finished.");
		return rB;
	}


 


	private void consoleDisplay() {
		MetaNodeIntf  node;
		ArrayList<Double> values;
		int nrc;
		String str ;
		
		out.print(2,true,"\nintensional profiles of nodes:");
		
		for (int i=0;i<dSom.somLattice.size();i++){
			node = dSom.somLattice.getNode(i) ;
			
			values = node.getIntensionality().getProfileVector().getValues();
			nrc = node.getExtensionality().getCount() ;
		
			str = arrutil.arr2text( values ,2 ) ;
			out.print(2,true, "i "+i+"  ---  [n:"+nrc+"] "+str ); // output suppressing the prefix ...
		}
		out.print(2,true," - - - - - \n");
	}
	// ========================================================================
	
	
	private void  adjustSampleAndSteps( int currentEpoch, int actualRecordCount)  {
	
		sampleRecordIDs = dataSampler.createEffectiveRecordList( absoluteRecordCount, currentEpoch, somSteps ,  actualRecordCount);
		
		if ((currentEpoch <= 1) && (actualRecordCount < (dSom.getSize()*6))){
			somSteps = somSteps + (Math.round(( dSom.getSize()*6)/(actualRecordCount)));
		}
	
	}


	private void adjustIntensityParameters( int currentEpoch, int actualRecordCount){
		
		double d, _f, speed2=1.0;
		
		double mapsize ;
		int nodecount = dSom.somLattice.size();
		
		mapsize = Math.sqrt( nodecount ) ;
			
		if (mapRadius > 1.3) {
			d = 0;
		} else {
			d = 1;
		}
		
		timeConstant =  ((d + actualRecordCount * Math.pow(mapRadius, 0.25)) / (d + Math.log10(mapRadius)));
		
		if (somSteps + 1 > 1) {
	
			_f = (float) (1.0f - ((0.6 * (currentEpoch - 1.0f) / (somSteps)) + 0.2f))/speed2;
			
			neighbourhoodRadius = (double) (1.1f * mapsize / (2.0f) * _f);
	
			learningRate = (double) ( (initialLearningRate *(currentEpoch))* (1.0f * _f) + learningRate + 0.02f);
		}
	                                     // out.print(2," _f : "+_f+ "   NeighbourhoodRadius "+NeighbourhoodRadius+ "    LearningRate "+LearningRate);
		mapRadius = mapsize/3.0f;
		 
	}


	/*
	
	
			stdSom = new DSomCoreEngine( modelingSettings, generalSettings, somdata ) ;
			
		 
			
			stdSom.setIDcolumn( id_col );
			stdSom.setTargetVariableIndex( tv_col) ;
	
			// simulating here if necessary ?? CHECK!!
			
			//
			
			stdSom.setMinimalNodesize(3); 
			// mSOM.size = 12 ;
			
			if (modset.getAutoSomSizing()==true){
				som_side_length = setthelp.estimateSomSize( somdata.getRecordSize(), somdata.getRecordCount() ); 
			} else {
				som_side_length = modset.getMapSideLength() ;
			}
			
			// this actually allocates the nodes into an array of SOMnode
			stdSom.setSize(som_side_length); // is equal to width or height of the map, but MapSize = size*size !!!
			
			stdSom.setStepsCount( 4);
			
			stdSom.setInfluenceMode(1);
			 
			
			stdSom.setreducedpriority(  modset.getThreadPriority(3)<=1 ) ;
			
			stdSom.setinitialLearningRate( 0.01 ) ;
			 
			
			stdSom.initializeSOM( modset.UseVectorModegetDedicated() );
	
			
			                                               System.out.println("SOM-Learning has been started...");
			r = stdSom.execute();
			
			if ( (!stdSom.isAvailable()) || (r<0)){
				System.out.println("Learning not successfully finished (err="+String.valueOf(r)+")!");
				
		    } else {
		    	System.out.println("SOM has successfully been created!");
			    
		    } // end if avail, actually we need an event !!
					
		
			stdSom.setStateFinished( true );
				
	
	 */
	
	private void clearNodesExtension( int currentEpoch){
		
		NodeTask task;
		LatticeFutureVisor latticeFutureVisor;
		
		MetaNodeIntf  node;
		ExtensionalityDynamicsIntf  ext;
		IntensionalitySurfaceIntf  ints;
		
		for (int i=0;i<dSom.somLattice.size();i++){
		
			node = dSom.somLattice.getNode(i);
			
			ext  = node.getExtensionality();
			ints = node.getIntensionality() ;
			
			ext.clear();
			
		}
		
		if (currentEpoch >= 1) {
			 // send a message to all nodes
			
			
			
			/*
			latticeFutureVisor = new LatticeFutureVisor( dSom.somLattice,  NodeTask._TASK_SETVAR );
			 
			
			task = new NodeTask( NodeTask._TASK_SETVAR, (Object)so.encode( (Object)vars.getActiveVariableLabels()) );
			// do it for all nodes
			dSom.somFluidParent.notifyAllNodes( task );
			
			latticeFutureVisor.waitFor();
			*/
		}
	}


	// ========================================================================
	
	
	public void perform() {
		 
		new DsomStarter();
		
	}
	
	// jst for starting in its own thread, but hiding the Runnable interface from/for the outside
	class DsomStarter implements Runnable{

		Thread dsomThrd;
		
		public DsomStarter(){
			
			dsomThrd = new Thread(this,"dsomThrd");
			dsomThrd.start();
		}
		
		@Override
		public void run() {
			performDSom();
		}
		
	} // inner class DsomStarter

	public double getMapRadius() {
		 
		return mapRadius;
	}
	
	
	
	
	
	
	
	
	
	
}
