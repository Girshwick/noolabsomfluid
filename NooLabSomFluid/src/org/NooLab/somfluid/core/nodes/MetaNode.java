package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;


import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.logging.PrintLog;

import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.core.categories.connex.*;
import org.NooLab.somfluid.core.categories.extensionality.*;
import org.NooLab.somfluid.core.categories.imports.*;
import org.NooLab.somfluid.core.categories.intensionality.*;
import org.NooLab.somfluid.core.categories.similarity.*;
import org.NooLab.somfluid.core.engines.NodeStatistics;
import org.NooLab.somfluid.core.engines.det.ProfileVectorMatcher;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
import org.NooLab.somfluid.env.communication.*;

import org.NooLab.somfluid.util.BasicStatisticalDescription;
 


/**
 * 
 * the meta-node is either <br/>
 * - a standard SOM node  <br/>
 * - any arbitrary function which returns a "signature",  <br/>
 *   e.g. any SOM, a ANN  <br/><br/>
 *   
 * in case of default SOM, the Node is just the profile with its cluster content, in other words, 
 * nearly a constant, only dependent from the data, which are constant anyway. <br/><br/>
 * 
 * If we replace this fixation with a "function" (category theory: arrow), we gain a 
 * lot of flexibility and adaptivity. <br/><br/>
 * The structure which reflects this is the MetaNode. <br/>
 * 
 * This allows for multi-dimensional fractal growth, since the Som inside a node may grow and 
 * differentiate, too, of course
 * 
 * Another important difference to standard SOM nodes is that the nodes are actively processing
 * their content, i.e. additionally to the calculation triggered by the
 * nework (change in data, explicit activation trigger), they also run more or less periodically an 
 * internal update mechanism 
 * 
 * -> NOT the central instance distributes the data once the data are in the network, but the meta nodes themselves!!
 * 
 * The Meta-Node has private properties, that are instantiated by the network, but that are separate
 * instances afterwards. 
 * These properties are
 * - similarity function
 * - activity level
 * - connectivity : amount of relations, types of relations (purely random, directed in/out)
 * 
 * the processes of a meta node are
 * 
 * - updating the weight vector
 * - growing directed fibers (axons, dendrites)
 * - "efforts" devoted to "digesting" versus "transmitting" (default 0.98 : 0.02)
 * - differentiating into SOM (nested, pullulated/outgrowing)
 * - 
 * 
 * The SomFluid objects holds a copy of the weight vectors of all nodes (?in this lattice?)
 * 
 * 
 * Nodes do not hold a copy of the data in most scenarios;
 * instead, they refer to the SomDataObject that contains the input data
 * 
 * Yet there is the possibility (via imported interface) that the intensity of the ...
 * 
 * A proper distinction between the extensional part and the intensional part is established.
 * In the intensional part, profiles are distinguished from weights (in standard Kohonen, they are not distinguished)
 * It is important to understand that what is commonly called "weight" vector is not a weight vector at all. 
 * Its the profile vector that describes the intensional mapping of the extensional list.
 * 
 * 
 * blacklist, whitelist are defined on the level of the lattice
 */
public class MetaNode   extends
                                    AbstractMetaNode
						implements  
						
								 	NetworkMessageIntf,
								 	// for messages from the Node level (between nodes)
								 	NodesMessageIntf
								 				    	  {
	
	ClassificationDescription  classifyDescription;
/*  put this all in ClassificationDescription
     public double Sum_of_Cost    = -1;
    public double weighted_variance_total = -1;
	
    // these two are set in the "describe classification" context
    public String majorityLabel="" ; // the most frequent class in the node
    public String majorityValue="" ; // the most frequent TV norm-value in the node
    
    //  the proportion of "cases" in the Node;
    //  what a "case" is, is defined in SOMPrototypes
	//  this has as many items as there are target groups 
	        -> note that there are 2 modes: 
	           - several tgroups defining 1 target class, or
	           - several tgroups defined for multi-class modeling
	                                                                             
	public double[] targetproportion ;  
	public boolean[] isPrototype ;
 
  
 */

	int _MinimalSizeBeforeSplit = 10 ;
	int _AbsoluteMinimumForSplit = 5 ;
	
	
	
	// TODO for those contexts as represented by these interfaces, we need 
	//      the respective properties Objects for persistence
	
	/** the count of weight adjustment operations applied to the node */
	int virtualRecordCount = 0; 
	private boolean contentSensitiveInfluence;
	private boolean changeIsSizedependent=true;

	boolean calculateAllVariables=false;
	private ArrayList<Integer> sdoIndexValues = new ArrayList<Integer>();
	
	int mppLevel=0;
	
	/**   ??? */
	ArrayList<IndexDistanceIntf> listOfQualifiedIndexes = new ArrayList<IndexDistanceIntf>();
	
	
	// ------------------------------------------------------------------------
	public MetaNode( VirtualLattice virtualLatticeNodes, DataSourceIntf somData ){
		super(virtualLatticeNodes,  somData );
		mppLevel = virtualLatticeNodes.getMultiProcessingLevel();
	}
	
	public MetaNode(VirtualLattice somlattice, DataSourceIntf somData, MetaNode templateNode) {
		super(somlattice,  somData );
		
		extensionality =  new ExtensionalityDynamics( (ExtensionalityDynamics)templateNode.extensionality ) ;
		
		intensionality = new IntensionalitySurface( (IntensionalitySurface)templateNode.intensionality ) ;
		
		similarity = new Similarity( (Similarity)templateNode.similarity );
		
		_MinimalSizeBeforeSplit = templateNode._MinimalSizeBeforeSplit ;
		_AbsoluteMinimumForSplit = templateNode._AbsoluteMinimumForSplit ;
		
		 
		virtualRecordCount = templateNode.virtualRecordCount ; 
		contentSensitiveInfluence = templateNode.contentSensitiveInfluence ;
		changeIsSizedependent = templateNode.changeIsSizedependent ;

		calculateAllVariables = templateNode.calculateAllVariables;
		if(templateNode.sdoIndexValues!=null) sdoIndexValues.addAll( templateNode.sdoIndexValues );
		
		if (templateNode.listOfQualifiedIndexes!=null) listOfQualifiedIndexes.addAll( templateNode.listOfQualifiedIndexes) ;
		
		classifyDescription = templateNode.classifyDescription ;
	}
	
	// ------------------------------------------------------------------------	
	  
	

	public void clearData(){
		this.extensionality.getListOfRecords().clear();
		this.extensionality.getStatistics().resetFieldStatisticsAll() ;
	}
	
	public void clear(){
		stopAutonomyProcess();
		
		this.extensionality.getListOfRecords().clear();
		this.extensionality.getStatistics().resetFieldStatisticsAll() ;
		
		virtualRecordCount=0;
		/*
		this.similarity.clear() ;
		
		intensionality.clear(0) ;
		variableLabels.clear() ;
		*/
		sobj = null;
		sampler=null;
	}
	
	public void evaluateExtensions() {
		
		int ix,np, rcount,rIndex;
		double sv, minSimDist=99999999.9, maxSimDist = -1.01;
		ArrayList<Double> intensNodeProfile, recordVector;
		ArrayList<Integer> recIndexes;
		
		ProfileVectorMatcher recordSorter = new ProfileVectorMatcher(mppLevel,out);
		ArrayList<Integer>  boundingindexlist = new ArrayList<Integer> ();
		
		ArrayList<IndexDistanceIntf> sortedRecords = new ArrayList<IndexDistanceIntf> ();
		
		
		
		recIndexes = this.extensionality.getListOfRecords();
		
		intensNodeProfile = intensionality.getProfileVector().getValues();
		
		
		rcount = this.extensionality.getCount() ;
		
		recordSorter.setParameters(intensNodeProfile, rcount, boundingindexlist);
		
		
		for (int i=0;i<rcount;i++){
		
			// the extension of the node contains only the indices that point to 
			// the underlying table as a record index, not by the index value of in the data record
			
			rIndex = extensionality.getRecordItem(i); 
			
			
			recordVector = this.somData.getRecordByIndex( rIndex,2) ; // 2 == record from normalized data

			if (recordVector.size()>0){
				recordSorter.addRecordToCollection(recordVector);
			}
		}
		
		SimilarityIntf simIntf = this.similarity ;
		recordSorter.createListOfMatchingUnits( 2, simIntf); // 2 -> explicitly provided data records 
		recordSorter.partialSort( -1, rcount) ;
		
		listOfQualifiedIndexes = new ArrayList<IndexDistanceIntf>( recordSorter.getList( -1 ) );
		
		for (int i=0;i<listOfQualifiedIndexes.size();i++){
			ix = listOfQualifiedIndexes.get(i).getIndex() ;
			rIndex = extensionality.getRecordItem(i);
			listOfQualifiedIndexes.get(i).setIndex(rIndex) ;
		}
		recordSorter.clear();
		recordSorter=null;
		
		
		// sort
	}


	// use it like so: (resource: http://tutorials.jenkov.com/java-generics/methods.html)
	// WeightVector weightVector   = getInfoFromNode( WeightVector.class, 1);
	@Override
	public <T> T getInfoFromNode(Class<T> rqClass, int infoID)  throws IllegalAccessException, 
																		InstantiationException {
		
		String className = rqClass.getSimpleName();
		T object = null ;
		
		// T object = rqClass.newInstance();
		// set properties via reflection.
		
		if (className.toLowerCase().contains("profilevector")){
			object = (T) profileVector;
		}
		
		// size, StatsObject, etc. ....
		
		return object;
	}
 
	
	
	// ---- Events from NetworkMessageIntf ------------------------------------

	@Override
	public void adjustProfile(  ArrayList<Double> datarecord,
								int nodeIndex,
								double learningrate, double influence, 
								double sizeFactor, int mode ) {

		int return_value=-1, contrastEnh;
    	int err=1;
    	int vn,_d, recordcount  ;
    	boolean _blocked=true, calcThis, calculateAllVariables=false ;
    	double contextual_influence_reduction=1;
    	double _new_w=0.0;
    	double _old_w=0.0, w_d , _v, _LR=1.0f,change ;
    	
    	NodeStatistics nodeStats ;
    	ArrayList<Double> usevector, weightvector, nodeProfile, vector2 ;

		try {

			err = 2;
			// in case of excluded variable (that are not IX or TV) we possibly
			// want to calculate the profile values to
			// calculateAllVariables = modelingSettings.calc_all_variables();
			calculateAllVariables = false ;
			
			weightvector = intensionality.getWeightsVector(); // not used so far
			
			usevector = this.similarity.getUsageIndicationVector() ;
			
			// creating copies of the two vectors
			nodeProfile = new  ArrayList<Double>(intensionality.getProfileVector().getValues()) ;
			
			nodeStats = this.getExtensionality().getStatistics() ;
			vector2 = new ArrayList<Double>(datarecord);

			recordcount = extensionality.getCount() ;
			
			if (contentSensitiveInfluence) {
				// not available yet
				/*
				 * content sensitive update means, that nodes show a context specific resistance to get updated 
				 * in the standard way. 
				 * If the relative difference is very large, the update will be smaller However, in order to 
				 * normalize the actual difference into the relative difference, we need a global statistics
				 * abut each variable across all nodes. This is very expensive, thus 
				 * we update only very rarely (around 5x per learning epoch)
				 * 
				 * So far we have no such statistics running, thus we cannot perform this yet
				 */

			} // contentSensitiveInfluence ?

			err = 3;
			// data object is available through the object pointer <SD> ==
			// SOM_data
			_d = intensionality.getProfileVector().getValues().size();

			 
			
			contrastEnh = 0;
			

			for (int w = 0; w < _d; w++) { // across all fields
											err = 4;

				_old_w = nodeProfile.get(w); // saving weight of variable[w] as old weight
				if (nodeProfile.get(w)>1.0){
					_old_w = 1.0;
					nodeProfile.set(w, 1.0) ; 
				}
				
				
				
				// work on the variable only if allowed
				calcThis = (usevector.get(w) > 0 ); // || (w==this.similarity.getIndexTargetVariable()
				
				if (calcThis) {
					if (calculateAllVariables) {
						// we may update all variables, the distance will be
						// calc'd only for used ones anyway !
						if (w == similarity.getIndexIdColumn()) {
							calcThis = false; // always to exclude: the index column
						}
						// || ( w == similarity.getIndexIdColumn() ))
						// we include the target variable, so we can see the expected mean value,
						// albeit this info will be often overruled
					}
				}

				if (calcThis) {

											err = 5;

					if (((nodeProfile.get(w) >= 0) && (vector2.get(w) >= 0)) && (vector2.get(w) <= 1)) {
						// excluding MV ...
											err = 6;
						sizeFactor = 1;

						if (virtualRecordCount + recordcount <= Math.sqrt(0.1 + recordcount)) {

											err = 7;
							w_d = 0;
							w_d = (nodeProfile.get(w) - vector2.get(w))/3.0;
							// w_d = w_d + w_d * (3 * nodeProfile.get(w) + vector2.get(w))/ 4;
									
							if (recordcount==0){
							 	
							}
							
						} else {

							w_d = (nodeProfile.get(w) - vector2.get(w));
											err = 8;
						} // else: recordcount <= ?

                       // w_d = w_d * contextual_influence_reduction; // not specified so far, working with default=1 here

											err = 9;
						_LR = learningrate;

						if ((recordcount + Math.log10(1 + virtualRecordCount) <= 2) && (!_blocked)) {
							// also in the neighbourhood
							// not used so far

											err = 10;
							_LR = (double) (_LR + (1 - _LR)/ (11 + Math.log10(1 + _d) + Math.log10(1 + virtualRecordCount)));
						}
											err = 11;
											
						change = (double) ((_LR * influence * w_d) * (1 / (1 + contrastEnh)));

						change = (double) adaptChangeRelativeToNodeExtSize(change, recordcount);

											err = 12;
						if ((change < -1) || (change > 2)) {
							change = 0;
						}
						if (recordcount == 0) {
							// in the beginning = the first updates, we scale the new record much higher (ratio 4:1),
							// if we have updated it often, this difference decreases more and more
							// TODO: we should scale 
							double imprintingRatioScale = Math.sqrt(0.01 * (double)virtualRecordCount);
							_new_w = (double)( (nodeProfile.get(w)* (double)(1.0+imprintingRatioScale) + 
									           (vector2.get(w) * (4.0+imprintingRatioScale))))/(5.0+(imprintingRatioScale*2.0));
							// _new_w = vector2.get(w);
							nodeProfile.set(w, _new_w) ; //
							
						} else {
							_new_w = vector2.get(w) + change;
						}

						virtualRecordCount = virtualRecordCount + 1;

						if ((_new_w > 1.0) && (_new_w < 1.041)){
							_new_w = 1.0 ;
						}
if ((_new_w<0) || (_new_w>1.04)){
	err=16;
}
							
											err = 14;
						if (_new_w > 1) {
											err = 15;
                       		out.printErr(1,"\nProblem in AdjustWeights(a): "+
                       						   "\n   node index = "+nodeIndex +
                       						   "\n   position   = "+w +
                       		                   "\n   new weight = "+String.format("%4.3f", _new_w)+
                       		                   "\n   LR         = "+String.format("%4.3f", _LR)+
                       		                   "\n   Influence  = "+String.format("%4.3f", influence)+
                       		                   "\n   w_d        = "+String.format("%4.3f", w_d)+
                       		                   "\n   VirtRecord_count = "+String.valueOf(virtualRecordCount)+
                       		                   "\n");
							// TODO: rollback
							
                       		_new_w = 1 ;
						}

						if ((_new_w < 0) && (_new_w > -0.3)) {
							_new_w = 0;
						}

						if (usevector.get(w)<=0.0){
							_new_w=0.0;
						}
						
						if (_new_w>1.0)_new_w=1.0;
						nodeProfile.set(w, _new_w);
						
											err = 17;
						if ((_new_w >= 0) && (_new_w <= 1)) {
							return_value = 0;
						}
					} // exclude MV for both vectors

				} // usagevector[w] ?
				err = 0;
			} // w-> across all fields

			// if everything is ok (err=0, we do not throw exceptions), we finally have to put the values 
			// from the local array "nodeProfile" (=at first a clone of original vector of the profile in the lattice!)
			// back to the node, 
			if (err==0){
				//
				intensionality.getProfileVector().setValues(nodeProfile) ;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		err=0;
	}
	
	public void insertDataAndAdjust( ArrayList<Double> dataNewRecord, int recordIndexInTable) {

		insertDataAndAdjust( dataNewRecord,recordIndexInTable,1, 1.0, -1 ) ;
	}

	 
 
	@Override
	public void insertDataAndAdjust( ArrayList<Double> dataNewRecord,
									 // int nodeIndex,
									 int recordIndexInTable,
									 int ithWinner,
									 double learningrate ,
									 int fillingLimitForMeanStyle) {
		
		int nodeExtSize, recordcount=0,_d, err=1;
		double _old_pv , _new_pv, fieldValue ;
		boolean calcThis ;
		
		NodeStatistics  nodeStats ;
		BasicStatisticalDescription fieldStats ;
		
		ArrayList<Double> usevector,weightvector , nodeProfile ;
		
		// dependent on size of extensional container !
		nodeExtSize = getExtensionality().getCount() ;
		
		
		try{
			calculateAllVariables = false ;
											err = 2;
		//  the weight vector is NOT the use vector, the weight vector describes the weight of a variable IFF used
		//  the use vector is in the similarity part : SimilarityIntf simIntf ;
											
			weightvector = intensionality.getWeightsVector(); //  
			
			//fillingLimitForMeanStyle 
			
			usevector = this.similarity.getUsageIndicationVector() ;
			
			nodeProfile = new  ArrayList<Double>(intensionality.getProfileVector().getValues()) ;
			recordcount = extensionality.getCount() ;
			// UsageIndicationVector() is different from Similarity and Intensionality !!!!!!!!!!!!!!!!!!!!!!!!!!!!
											if (serialID<=-10){
												ArrayList<Double>  uv = getIntensionality().getUsageIndicationVector() ;
												String str = ArrUtilities.arr2Text(uv, 1) ;
												out.print(2,"insertDataAndAdjust(), UsageIndicationVector from Similarity : "+ str);
											}
			
			
											err = 3;
			nodeStats = extensionality.getStatistics() ;
			_d = intensionality.getProfileVector().getValues().size();
			
			for (int w = 0; w < _d; w++) { // across all fields
				 
				fieldValue = dataNewRecord.get(w); 
				
				if (fieldValue > 1.0){
					fieldValue = 1.0;
					dataNewRecord.set(w, 1.0) ;  
				}
				
											err = 4;
				_old_pv = nodeProfile.get(w); // saving weight of variable[w] as old weight
				
				if (nodeProfile.get(w)>1.0){
					_old_pv = 1.0;
					nodeProfile.set(w, 1.0) ;  
				}
				
				
				fieldStats = nodeStats.getFieldValues().get(w);
				
								if (nodeStats.getFieldValues().size()< w){
									nodeStats.getFieldValues().add( new BasicStatisticalDescription()) ;
								}
											err = 5;
				// work on the variable only if allowed
				calcThis = (usevector.get(w) > 0) || (w==this.similarity.getIndexTargetVariable());
				
				if (calcThis==false) {
					if (calculateAllVariables) {
						calcThis=true;	
						// we may update all variables, the distance will be
						// calc'd only for used ones anyway !
						if (w == similarity.getIndexTargetVariable()) {
							calcThis = false; // always to exclude: the index column
						}
						if (somData.getVariables().getItem(w).getIsEmpty()>0 ){
							calcThis = false;
						}
						// || ( w == similarity.getIndexIdColumn() ))
						// we include the target variable, so we can see the expected mean value,
						// albeit this info will be often overruled
					}
				}
											err = 7;
				if (calcThis) {
											err = 10;
											
					if (((nodeProfile.get(w) >= 0) && ( fieldValue >= 0)) && (fieldValue <= 1)) {
						// excluding MV ...
											err = 11;
					
						if (recordcount <= 0) {
							// after initialization
							_old_pv = nodeProfile.get(w);

							// for the first real data record, we replace the random value by the real value of
							// the first observation that we add to this node ;
							// ( fieldValue derives indeed from  "dataNewRecord" !) 
							nodeProfile.set(w, fieldValue );
											err = 12;							
							fieldStats.clear();
							fieldStats.introduceValue( fieldValue );
							_new_pv = fieldValue;

						} else {
											err = 14;
							_new_pv = dataNewRecord.get(w);
							
							fieldStats.introduceValue( fieldValue ); // fieldValue = dataNewRecord.get(w); 
							_new_pv = fieldStats.getMean() ; 
							// _new_pv will be set as the profile value at pos w (see below)
							// ongoing update of more complicated stats, like skewness, kurtosis
							
							// for the actual best match (ith winner index = 0) we keep a small trace of the last profile value, 
							// but not in the last epoch!
							// which is controlled in "updateWinningNode()" in class "DSomDataPerception{}"
							
							if (ithWinner==0){ 
								double nlw = 6.0;
								nlw = (double) (nlw - (ithWinner + 1));
								_new_pv = ((nodeProfile.get(w)) + nlw * _new_pv) / (nlw + 1.0);
							}
							
						}

						 
						recordcount++;
						if ((_new_pv > 1.0) && (_new_pv < 1.01)){
							_new_pv = 1.0 ;
						}
							
						if (_new_pv > 1) {
							// _new_w = 1 ;
											err = 15;
							out.printErr(1, "\nProblem in AdjustWeights(b): "+
                       				        "\n   position in vector = "+w +
                       		                "\n   new profile value  = "+String.format("%4.3f", _new_pv)+
                       		                "\n   count of records   = "+String.valueOf(recordcount)+
                       		                "\n");
                            // TODO: rollback
                    	   _new_pv = 1.0;
                       }
                       
if ((_new_pv<0) || (_new_pv>1.04)){
	err=16;
}						
                       if ((_new_pv<0) && (_new_pv>-0.3)){_new_pv=0;}

                       
                       	if ((similarity.getUsageIndicationVector().get(w)<=0.0) && (w!=this.similarity.getIndexTargetVariable())){
                       		_new_pv = 0.0;
                       	}

                       	if (_new_pv>1.0)_new_pv=1.0;
       					nodeProfile.set(w, _new_pv);
                       
                       
                   } // exclude MV for both vectors   
                                                     
                } // usagevector[w] ?
				err = 0;
				
			} // w-> across all fields
			
			extensionality.addRecordByIndex( recordIndexInTable ) ; 
			
			// if everything is ok (err=0, we do not throw exceptions), we finally have to put the values 
			// from "nodeProfile" (=clone of original vector of the profile in the lattice!)
			// back to the node, 
			if (err==0){
				 
				intensionality.getProfileVector().setValues(nodeProfile) ;
			}
			recordcount = extensionality.getCount() ;
		}catch(Exception e){
			String str = ""+err ;
			out.print(2, "problem in insertDataAndAdjust(), error code: "+str) ;
			e.printStackTrace();
		}
		
		
		
	}

	/**
	 * this removal we use in case the record is already known by index...
	 * 
	 * @param nodeIndex
	 * @param recordIndexInTable
	 * @param learningrate
	 */
	public void removeDataAndAdjust( int recordIndexInTable,
			 						 double learningrate ) {
		int rcount,rix ;
		
		ArrayList<Double> profilevalues, xDataVector;
		
		
		try{
			
			profilevalues = this.intensionality.getProfileVector().getValues() ;
			rcount = this.extensionality.getCount() ;
			if (rcount < this._AbsoluteMinimumForSplit ){
				return ;
			}

			xDataVector = this.somData.getRecordByIndex(recordIndexInTable, 2);
			
			rix = extensionality.getListOfRecords().indexOf(recordIndexInTable) ;
			
			if (rix<0){
				out.print(2, "request for removal of record <"+recordIndexInTable+"> from node failes, record not found.");
				return ;
			}
			// extensionality.removeRecordByIndex( recordIndexInTable );
			extensionality.getListOfRecords().remove(rix) ;
			extensionality.getStatistics().removeRecordData(xDataVector);

			// also recalculate intensionality = profile values vector
			intensionality.getProfileVector().changeProfile(xDataVector, extensionality.getCount(), -1);		 
			
		}catch(Exception e){
			
		}
	}

	/**
	 *  those records that are least similar to the profile will be exported, 
	 *  the removal will adapt the basic statistics;
	 *  
	 *  @param countOfRecords the number of records to be transferred;
	 *  @param quality   <0: the least similar records, >0: the most similar records, 0: all
	 */
	@Override
	public ArrayList<Integer> exportDataFromNode( int countOfRecords, int quality, boolean removeExports) {
		
		ArrayList<Integer> exportedRecords = new ArrayList<Integer>(); 
		ProfileVectorMatcher recordSorter = new ProfileVectorMatcher( mppLevel,out );
		ArrayList<Integer>  boundingindexlist = new ArrayList<Integer> ();
		
		ArrayList<Double> profilevalues, xDataVector = null ;
		int rcount, rIndex,ix;
		
		ArrayList<IndexDistanceIntf> sortedRecords = new ArrayList<IndexDistanceIntf> ();
		SimilarityIntf simIntf;
		
		try{
			
			// 
			profilevalues = this.intensionality.getProfileVector().getValues() ;
			rcount = this.extensionality.getCount() ;
			if (rcount < _MinimalSizeBeforeSplit ){
				return exportedRecords;
			}
			
			recordSorter.setParameters(profilevalues, rcount, boundingindexlist);
			
			
			for (int i=0;i<rcount;i++){
			
				// the extension of the node contains only the indices that point to 
				// the underlying table as a record index, not by the index value of in the data record
				
				rIndex = extensionality.getRecordItem(i); 
				xDataVector = this.somData.getRecordByIndex( rIndex,2) ; // 2 == record from normalized data
	
				if (xDataVector.size()>0){
					recordSorter.addRecordToCollection(xDataVector);
				}
			}
			
			simIntf = this.similarity ;
			recordSorter.createListOfMatchingUnits( 2, simIntf); // 2 -> explicitly provided data records 
			recordSorter.partialSort( -1, countOfRecords) ;
			sortedRecords = recordSorter.getList( -1 ) ;
			
			// now reading the end of the list, if quality<0
			int k= sortedRecords.size()-1;
			
			if ((quality<0) && (k>= _MinimalSizeBeforeSplit)){
				
				int z=0;
				while ((k >= _AbsoluteMinimumForSplit) && (z<countOfRecords)){
	
					ix = sortedRecords.get(k).getIndex();
					rIndex = extensionality.getRecordItem(ix); 
					
					if (removeExports){
						xDataVector = this.somData.getRecordByIndex( rIndex,2) ;
						
						extensionality.removeRecordByIndex( ix );  
						extensionality.getStatistics().removeRecordData(xDataVector) ;
	
						// also recalculate intensionality = profile values vector
						intensionality.getProfileVector().changeProfile( xDataVector ,extensionality.getCount(), -1 );
					}
					
					// finally add the index of the record in the data table to the list of exported indexes
					exportedRecords.add(rIndex);
					z++; k--;
				}
	
			} // reading from the end of the list
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return exportedRecords;
	}

	@Override
	public ArrayList<Integer> exportDataFromNode( double smallestPortion, double largestPortion, int quality, boolean removeExports) {
		// 
		ArrayList<Integer> exportedRecords = new ArrayList<Integer>();
		
		
		return exportedRecords;
	}

	@Override
	public void importDataByIndex(ArrayList<Integer> recordIndexes) {
		// 
		int ix, nodesize = 0;
		ArrayList<Double> dataNewRecord ;
		
		nodesize = extensionality.getCount() ;
		
		for (int i=0;i<recordIndexes.size();i++){
			
			ix = recordIndexes.get(i) ;
			if (ix>=0){
				
				dataNewRecord = somData.getNormalizedDataTable().getRowValuesArrayList(ix) ;
				insertDataAndAdjust( dataNewRecord , ix);
				 
			}
		} // all records to be imported to this node
		
	}

	@Override
	public ArrayList<Integer> getExtensionRecordsIndexValues() {
		/*
		ArrayList<Long> recIndexValues=null;
		
		if (recIndexValues==null){
			recIndexValues = new ArrayList<Long>();
		}
		*/
		return getExtensionality().getListOfRecords();
		
	}

	public void setSomData(SomDataObject somdata) {
		 
		somData = somdata;
	}


	public void alignProfileVector(ProfileVectorIntf profileVectorObj) {
		profileVector = profileVectorObj ;
	}

	public ArrayList<IndexDistanceIntf> getListOfQualifiedIndexes() {
		return listOfQualifiedIndexes;
	}


	@Override
	public ArrayList<Double> getTargetVariableValues() {
		
		ArrayList<Double> values = new ArrayList<Double>();
		int tvIndex, recordIndex;
		double v;
		
		tvIndex = this.similarity.getIndexTargetVariable() ;
		if (tvIndex<0){
			return values;
		}
		
		for (int i=0;i<extensionality.getCount();i++){
			
			recordIndex = extensionality.getRecordItem(i) ;
			v = somData.getRecordByIndex(recordIndex, 2).get(tvIndex) ;
			
			if (v>=0){
				values.add(v) ;
			}
			
		}// i-> all records in container
		
		
		return values;
	}

	/**
	 * 
	 * the basic initialization set all the profile values to some value;
	 * yet, we do not want to see random values in fields that are excluded
	 * 
	 */
	@Override
	public void cleanInitializationByUsageVector( ArrayList<Double> usagevector) {

		double v=0.0 ;
		// calculateAllVariables = modelingSettings.calc_all_variables();
		boolean calculateAllVariables = false ;

		
		for (int i=0;i<usagevector.size();i++){
			
			if (usagevector.get(i)<=0){
				
				if (calculateAllVariables==false){
					v = 0.0;
				}else{
					v = 0.0;
				}
				if (i<intensionality.getProfileVector().getValues().size()){
					intensionality.getProfileVector().getValues().set(i,v);
				}
			}
		}
		
	}

	private double adaptChangeRelativeToNodeExtSize(double _cval, int recordcount) {

		double _v, return_value;

		return_value = _cval;
		if (changeIsSizedependent) {

			// thats for merging nodes: _v := ((_fval1*_count1) +
			// (_fval2*_count2))/(_count1 + _count2)

			_v = Math.log10(1 + recordcount) + 1.0; // >= 1 ,, FVirtRecord_count
			_v = Math.sqrt(1 / _v);

			return_value = _cval * _v;
		}

		return return_value;

	}
	
	
	@Override
	public void onRequestForAdaptingWeightVector( Object obj, Object params) {
		ProfileVectorIntf weightVector;
		
		
		if (openLatticeFutureTask == NodeTask._TASK_ADAPT){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
	}



	/**
	 * note that objects arrive as encoded objects in order to guarantee perfect decoupling without clone()-ing!!
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onDefiningFeatureSet(Object obj , DataHandlingPropertiesIntf obj2) {
		 
		// ArrayList<String> varlabels;
		Object dcobj = decodeMsgObject(obj);
		
		if (dcobj!=null){
			// varlabels = (ArrayList<String>) dcobj;
											out.print(4, "node <" + serialID + ">, onDefiningFeatureSet()");
			setVariableLabels(new ArrayList<String>((ArrayList<String>) dcobj)); // (varlabels);
		}
		
		if (openLatticeFutureTask == NodeTask._TASK_SETVAR){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
			openLatticeFutureGuid = "";
			openLatticeFutureTask = -1;
			// tasks for waiting should not overlap !!!
		}
		
	}
	
	@Override
	public void onDefiningTargetVar(Object obj) {

		Object dcobj = decodeMsgObject(obj);
		
		if (dcobj!=null){
			
			targetVariableLabel = (String)dcobj ;
		}

		// 
		if (openLatticeFutureTask == NodeTask._TASK_SETTV){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
	}
	
	
	@Override
	public void onSendingDataObject( Object data, DataHandlingPropertiesIntf datahandler) {
		
		out.print(4, "node <"+serialID+">, onSendingDataObject()");

		// formatting the data object: here, the object contains the index pointer to the
		// SomDataObject
		
		
		
		//  we put this index into the list
		sdoIndexValues.add( 0  ); // TODO very incompelete...
		
		// and trigger recalculation, if immediate recalc is requested by propertized option
		
		
		// check for waiting futures
	 
		if (openLatticeFutureTask == NodeTask._TASK_SETDATA){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
		return  ;
	}


	@Override
	public void onRequestForDedicatedUpdate() {
		//  
		
		 
		if (openLatticeFutureTask == NodeTask._TASK_UPDATE){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
	}

	@Override
	public void onRequestForRandomInit(Object obj ) {
		 
		initializeSOMnode();
		 
											out.print(4, "task _TASK_RNDINIT> received ...");
				
		if (openLatticeFutureTask == NodeTask._TASK_RNDINIT){
											out.print(4, "task identified as <_TASK_RNDINIT>, node now informing lattice about guid : "+openLatticeFutureGuid);
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
	}

	@Override
	public void onRequestForDataRemoval(ArrayList<Long> dataIndex) {
		//  
		
		
		// NodeTask._TASK_REMOVE
	}


	@Override
	public void onRequestForDataRemoval() {
		//  
		
		// NodeTask._TASK_REMOVE
	}


	@Override
	public void onRequestForMemoryReset() {
		//  
		
		
		// NodeTask._TASK_CLEAR
	}


	@Override
	public void onArrivalOfChemicalStimulus() {
		//  
		
	}


	@Override
	public void onRequestForChangingActivityLevel() {
		//  
		
	}

	// ========================================================================

	@Override
	public IntensionalitySurfaceIntf importIntensionalitySurface( long serialID ) {
		IntensionalitySurfaceIntf intensionality;
			
		intensionality = virtualLattice.distributeIntensionalitySurface(serialID); // ProfileVector@30d83d
		
		return intensionality;
	}

	
	@Override
	public IntensionalitySurfaceIntf importIntensionalitySurface() {
		// that's the intenionality of the whole lattice ! 
		
		IntensionalitySurfaceIntf intensionality;
		
		intensionality = virtualLattice.distributeIntensionalitySurface(); // ProfileVector@30d83d
		
		return intensionality;
	}
	
	@Override
	public SimilarityIntf importSimilarityConcepts() {
		SimilarityIntf similarity;
		
		similarity = virtualLattice.distributeSimilarityConcept(); // Similarity@1db6942
		return similarity;
	}

	@Override
	public SimilarityIntf importSimilarityConcepts(long serialID) {
		SimilarityIntf similarity;
		
		similarity = virtualLattice.distributeSimilarityConcept(serialID); // Similarity@1db6942
		return similarity;
	}

	@Override
	public ExtensionalityDynamicsIntf importExtensionalityDynamics() {
		ExtensionalityDynamicsIntf extensionality;
		
		extensionality = virtualLattice.distributeExtensionalityDynamics();
		return extensionality;
	}

	// for copying nodes, we transfer basic structures
	public void importExtensionalityDynamics( ExtensionalityDynamicsIntf extension ) {
		extensionality = extension;
	}
	public void importIntensionalitySurface( IntensionalitySurfaceIntf intension ) {
		intensionality = intension;
	}
	public void importSimilarity( SimilarityIntf simile ) {
		similarity = simile;
	}
 
		
	// for basic initialization, the node fetches structures from the lattice
	@Override
	public ExtensionalityDynamicsIntf importExtensionalityDynamics(long serialID) {
		ExtensionalityDynamicsIntf extensionality;
		
		extensionality = virtualLattice.distributeExtensionalityDynamics(serialID);
		return extensionality;
	}

	@Override
	public MetaNodeConnectivityIntf importMetaNodeConnectivity() {
		MetaNodeConnectivityIntf nodeConnectivity ;
		nodeConnectivity = virtualLattice.distributeNodeConnectivity();
		return nodeConnectivity;
	}

	@Override
	public MetaNodeConnectivityIntf importMetaNodeConnectivity(long serialID) {
		
		MetaNodeConnectivityIntf nodeConnectivity ;
		nodeConnectivity = virtualLattice.distributeNodeConnectivity(serialID);
		return nodeConnectivity;
	}

	@Override
	public void setContentSensitiveInfluence(boolean flag) {
		
		contentSensitiveInfluence = flag ;
	}

	@Override
	public int size() {
		return getExtensionality().getListOfRecords().size();
	}










 

 

	 

}








