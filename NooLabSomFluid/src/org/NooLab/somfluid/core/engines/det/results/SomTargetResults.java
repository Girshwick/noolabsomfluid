package org.NooLab.somfluid.core.engines.det.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;

import org.NooLab.somfluid.SomApplicationResults;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.application.SomAppValidationIntf;
import org.NooLab.somfluid.core.application.SomApplication;
import org.NooLab.somfluid.core.application.SomValidation;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.DSomCore;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.data.DataSampler;
import org.NooLab.somfluid.properties.ModelingSettings;

import org.NooLab.somfluid.core.categories.extensionality.* ;
import org.NooLab.somfluid.core.categories.imports.ExtensionalityDynamicsImportIntf;
import org.NooLab.somfluid.core.categories.imports.IntensionalitySurfaceImportIntf;
import org.NooLab.somfluid.core.categories.imports.SimilarityImportIntf;


/**
 * 
 * TODO: for multi-target case, store NPV per target group, which requires an arry npvs[]
 * 
 * 
 * 
 */
public class SomTargetResults {

	DSom dSom;


	VirtualLattice somLattice;
	SomDataObject somData;
	
	
	ModelingSettings modelingSettings ;
	ClassificationSettings classifySettings;
	
	Roccer roccer;
	ModelProperties modelProperties = new ModelProperties ();
	
	boolean singleModeUndefined = false;
	
	// ------------------------------------------
	
	
	
	
	// a collection of lists, each item in this collection refers to a particular node
	ArrayList<FrequencyList> minorityFrequencies = new ArrayList<FrequencyList>();
	
	// ArrayList<Majorities> samplesResults = new ArrayList<Majorities>();
	
	private Majorities majorities ;
	private SampleValidator sampleValidator ;
	
	
	
	ArrayList<ValueCode> expectedValueCodes = new ArrayList<ValueCode>();
	
	LatticeClassDescription latticeClassDescription ;
	
	DataSampler dataSampler ;
	
	// ------------------------------------------
	ArrUtilities arrutil;
	PrintLog out;
	
	
	// ========================================================================
	public SomTargetResults( DSom dsom, DataSampler datasampler, ModelingSettings modSettings) {
		// 
		
		dSom = dsom;
		 
		
		somLattice = dSom.getSomLattice();
		somData = dSom.getSomData();
		 
		
		dataSampler = datasampler;
		ArrayList<Integer> sampleRecord;
		
		modelingSettings = modSettings;
		classifySettings = modelingSettings.getClassifySettings() ;
	
		
		prepareExpectedValueCodes();
		
		latticeClassDescription = new LatticeClassDescription();
		out = somData.getOut() ;
		
		arrutil =  modelingSettings.arrutil ;
	}

	
	//  
	public void prepare() {
		 

		if ( (dSom.getSfProperties().getSomType()==SomFluidProperties._SOMTYPE_MONO) && 
			 (modelingSettings.getTargetedModeling() )){

			/*
			 * we have to prepare the results for the training sample and for the validation samples
			 */
			
			if (dSom.getModelingSettings().getValidationActive()){
										out.print(1, "performing validation...");
										
				// not active so far ... :  this.performValidation( dataSampler.getValidationSet() );
										
				prepare( DataSampler._SAMPLE_TRAINING);
				
				
				prepare( DataSampler._SAMPLE_VALIDATION);
										out.print(2, "performing validation done.");
			}else{
				
			}
			

			
			
			if (classifySettings.isExtendedResultsRequested()) {
				// e.g. mis-classifications, RoC, Spela, ...
				
				// list of results from multiple modeling, based on variation of central parameters 
				// resolution, ECR, alpha/beta target, samples, 
				// available only after evolutionary optimization
				
			}
			
			
		} // is it a targeted modeling?
		
		
		
	}

	public void performValidation(ArrayList<Integer> validationSet) {

		// 1. 
		// mode=SINGLE : first we have to collect the pairs (record number, TV group index) for all records
		//               that are markable as target according to the TG definition 
		
		// somData
		
		// 2. we check for each node whether the node satisfies the risk conditions
		
		// 3. put these records
		
		
	}

	// ========================================================================	
	
	/**
	 * this is being called e.g. by "executeSOM()" in class "DSomCore{}"
	 * @param sampleTraining 
	 * 
	 */  
	protected void prepare( int sampleIdentifier ) {
		 
		try{
			// get actual sample through the samplesMapping of dataSampler
			
			
			// further processing dependent on the modeling mode
			
			
			String _atv = classifySettings.getActiveTargetVariable();
			if (_atv.length()==0){
				if (modelingSettings.getActiveTvLabel().length()>0){
					classifySettings.setActiveTargetVariable( modelingSettings.getActiveTvLabel() );
				}
			}
			
			int tm = classifySettings.getTargetMode();
			if (classifySettings==null){
				
			}
			singleModeUndefined = ( classifySettings.getActiveTargetVariable().length()==0) ||
								  ( classifySettings.getTargetGroupDefinition() == null ) ||
								  ( classifySettings.getTargetGroupDefinition().length==0) ;
			
			if ((tm == ClassificationSettings._TARGETMODE_SINGLE) && (singleModeUndefined==false)){
				
				validateSingleTarget( sampleIdentifier ); // still for both samples here
				// displayResultsOnNodes();
				return;
			}  
			
			if ((tm == ClassificationSettings._TARGETMODE_MULTI) || (singleModeUndefined==true)){
				// multi class, often covering all possible values, ordinal scale of support points
				classifySettings.setTargetMode( ClassificationSettings._TARGETMODE_MULTI ) ;
				
				validateMultiClassTarget();
				displayResultsOnNodes();
				return;
			}
			
			if (tm == ClassificationSettings._TARGETMODE_REGR){
			
				validateRegressionClassTarget();
				return;
			}
					
			
		}catch(Exception e){
			
		}
		
		
		
	}
	
	protected void validateSingleTarget( int sampleIdentifier ){
		
											out.print(2, "checking for results on SOM by assuming mode = _TARGETMODE_SINGLE");
		try{
			
			
			if (sampleIdentifier == DataSampler._SAMPLE_TRAINING){
				majorities = new Majorities();	
				
				majorities.determineTargetGroups( somLattice );
				this.modelProperties.trainingSample = majorities.getTrainingSample();
											out.print(2, "creating results : "+  this.toString()+"\n"+
													 	 "for training sample of lattice : "+ somLattice.toString());
				// modelProperties.trainingSample.roc = new RoC( majorities.roccer._rocN);
				modelProperties.trainingSample.tpSingularity = modelProperties.trainingSample.roc.rocCurve[1][0]; 
			}
			if (sampleIdentifier == DataSampler._SAMPLE_VALIDATION){
				/* dealing with validation sample is fundamentally different from checking the
				 * classification results for training set.
				 * 
				 * here, we do NOT inspect the SOM, rather we send one record after another
				 * to the SOM and retrieve the result
				 * 
				 */
				
				sampleValidator = new SampleValidator( dataSampler.getValidationSet() ) ;
				sampleValidator.perform();

				this.modelProperties.validationSamples.add( sampleValidator.majorities.getModelProperties().getTrainingSample() );
				
				sampleValidator.pseudoLattice.clear();
				sampleValidator.pseudoLattice.close();
				sampleValidator.pseudoLattice=null;
				
			}
			 
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	protected void validateMultiClassTarget(){
		
		
											out.print(2, "checking for results on SOM by assuming mode = _TARGETMODE_MULTI");
		try{
			
			// checking for all nodes the majority TV class indicator,
			// the values are checked against the derived TG intervals, then a label is attached
			// else a list about the frequencies of minority cases is established
			
			
			majorities = new Majorities();
			majorities.determineGroups();
			 
			
		}catch(Exception e){
			
		}
	}
	
	
	protected void validateRegressionClassTarget() {
	
											out.print(2, "checking for results on SOM by assuming mode = _TARGETMODE_REGR");
		try{
			
			
		}catch(Exception e){
			
		}
		
	}


	private void prepareExpectedValueCodes() {
		// 
		// expectedValueCodes , ArrayList<ValueCode>
		
		/* from  classifySettings
			double[][] TGdefinition ;
			String[] TGlabels;
		   	
		*/
	}

	private void displayResultsOnNodes() {
		 
		MetaNodeIntf node;
		int nrc, nrcSum =0;
		double v;
		
		String str ;
		out.print(2,true,"\npredictive qualities of nodes and lattice:\n");
	
		int tm = classifySettings.getTargetMode();
		
		for (int i=0;i<somLattice.size();i++){
			
			node = somLattice.getNode(i) ;
			
			v = node.getExtensionality().getPPV() ;
			str = "ppv = " + String.format("%.3f",v);

			
			if (tm == ClassificationSettings._TARGETMODE_MULTI){
				v = node.getExtensionality().getMajorityValueIdentifier();
				str = str + " ,  majority for target value : "+String.format("%.3f",v);
			}else{
				str = str + " ,  node is qualified target : "+ node.getIntensionality().isQualifiedTarget() ;
			}
			
			if (node.getActivation()>=0){
				nrc = node.getExtensionality().getCount() ;
				if (nrc>0){
					out.print(2,true, "i "+i+"  ---  [n:"+nrc+"] "+str ); // output suppressing the prefix ...
				}else{
					out.print(2,true, "i "+i+"  ---  [n:"+nrc+"] ");
				}
				nrcSum = nrcSum+nrc ;
			}
			
		} // i -> all nodes
		
		
		out.print(2,"content of all nodes N="+nrcSum);
		out.print(2,true," - - - - - \n");
	}


	/**
	 * @return the sampleValidator
	 */
	public SampleValidator getSampleValidator() {
		return sampleValidator;
	}




	/**
	 * @return the modelProperties
	 */
	public ModelProperties getModelProperties() {
		return modelProperties;
	}




	// ........................................................................
	private class Majorities 	implements 
	 								FrequencyListGeneratorIntf {
		
		FrequencyList frequencyList  ; 
		RocCurve roc ;
		Roccer roccer;
		LatticeClassDescription lcd;
		
		ArrayList<Integer> ecrNodes = new ArrayList<Integer>();
		
		ModelProperties modelProps;
		
		public Majorities(){
			
			lcd = latticeClassDescription ;
		}


		public ValidationSet getTrainingSample() {
			
			return new ValidationSet( modelProps.trainingSample);
		}


		public ModelProperties getModelProperties() {
			
			return modelProps;
		}


		/**
		 * 
		 * used in case of multi-group targets
		 */
		public void determineGroups() {
			
			MetaNodeIntf node;
			ArrayList<Double> tvValues  ;
			
			
			
			for (int i=0;i<somLattice.size();i++){
				
				node = somLattice.getNode(i) ;
				
				// get target variable column from extensionality container
				tvValues = node.getTargetVariableValues();
				
				frequencyList = new FrequencyList( ((FrequencyListGeneratorIntf)this) ) ;
				frequencyList.listIndex = i ;
				frequencyList.serialID = node.getSerialID() ;
				
				// now check this list of values for groups of different values and
				// determine their frequency 
				
				if (expectedValueCodes.size()==0){
					// knowing nothing about possible values in TV
					frequencyList.digestValues( tvValues );
				}else{
					
					// using info from ClassificationSettings, sometimes known, e.g. if
					// defined by simulation, or by user-based setting, or from 
					// analyzing the raw data and the respective encoding into normalized data 
					frequencyList.digestValues( tvValues , expectedValueCodes);
					
					// checking for known labels, if none present, then sort and 
					// assign an enumeration string (like "tg001"), and store it in 
					// ClassificationSettings
				}
				
				evaluateByEcrRiskMeasure(false);
				
				minorityFrequencies.add(frequencyList) ;

				
				node.getExtensionality().setPPV( frequencyList.ppv ) ;
				node.getExtensionality().setMajorityValueIdentifier( frequencyList.majority.observedValue );

				lcd.ccSum = lcd.ccSum + (frequencyList.ppv * node.getExtensionality().getCount() ) ;
				lcd.rnSum = lcd.rnSum + node.getExtensionality().getCount() ;
				
			} // i-> all nodes in lattice
			
			determineROCvalues();
			
			createGlobalDescription();
			
			
			
			// put this info as a property to the lattice
			
		}
		
		
		/**
		 * 
		 * used in case of single group targets 
		 */
		public void determineTargetGroups( VirtualLattice somLattice ){
			
			boolean hb;
			int nodeCases=0,nodeTN = 0, nodeTP = 0, nodeFP = 0, nodeFN = 0;
			double groupIdentifyingValue, generalECR = -1.0 ;
			MetaNodeIntf node;
			ArrayList<Double> tvValues  ;
			double[][] tgDef;
			String[] tgLabels;
			
			
			
			tgDef = classifySettings.getTargetGroupDefinition() ;
			tgLabels = classifySettings.getTGlabels();
			
			// set results to  somLattice.getModelProperties();
			// which serves as a container for all results that describe the lattice as a model
			modelProps = somLattice.getModelProperties();
			
			if (somLattice.size()<=0){
				return;
			}
			
			// -> all nodes
			for (int i=0;i<somLattice.size();i++){
				
				groupIdentifyingValue = -1;
				
				node = somLattice.getNode(i) ;
				
				// get target variable column from extensionality container
				tvValues = node.getTargetVariableValues();

				frequencyList = new FrequencyList( ((FrequencyListGeneratorIntf)this) ) ;
				frequencyList.listIndex = i ;
				frequencyList.serialID = node.getSerialID() ;
				
				// now check this list of values for the target group
				// it determines the ppv  
				generalECR = classifySettings.getECR() ;
				frequencyList.digestValuesForTargets( tvValues, generalECR, tgDef, tgLabels );
				
				
				// hb = (frequencyList.majority.observedValue >= 0) || ();
				// hb = evaluateByEcrRiskMeasure(true); 
				hb = generalECR > (1.0-frequencyList.ppv) ;
				
				if (hb){
					minorityFrequencies.add(frequencyList) ;
					groupIdentifyingValue = frequencyList.majority.observedValue ;
					
					node.getExtensionality().setPPV( frequencyList.ppv ) ;
					
					if ((frequencyList.ppv<1) && (frequencyList.npv<=0)){
						frequencyList.npv = 1-frequencyList.ppv;
					}
					node.getExtensionality().setNPV( frequencyList.npv ) ;
					node.getIntensionality().setTargetStatus(hb);
					
					
					nodeTP = nodeTP + (int) ((double)node.getExtensionality().getCount() * frequencyList.ppv);
					
					nodeFP = nodeFP + (int) ((double)node.getExtensionality().getCount() * (1-frequencyList.ppv));
					
					nodeCases = nodeCases + nodeTP;
					
					ecrNodes.add(i) ;
					
					lcd.rnSum = lcd.rnSum + node.getExtensionality().getCount() ;
				} // ecr satisfied ?
				else{
					node.getExtensionality().setPPV( frequencyList.ppv ) ;
					node.getExtensionality().setNPV( frequencyList.npv ) ;
					node.getIntensionality().setTargetStatus(hb);
					
					nodeFN = nodeFN + (int) ((double)node.getExtensionality().getCount() * (1-frequencyList.npv) );
					nodeTN = nodeTN + (int) ((double)node.getExtensionality().getCount() * frequencyList.npv );
					
					nodeCases = nodeCases + nodeFN;
					
					groupIdentifyingValue = frequencyList.majority.observedValue ;
				}
				node.getExtensionality().setMajorityValueIdentifier( groupIdentifyingValue );

				lcd.ccSum = lcd.ccSum + (frequencyList.ppv * node.getExtensionality().getCount() ) ;
				
			} // -> all nodes
			
			// TODO: determine the best ppv
			determineROCvalues(); // TODO: nodes should be sorted along decreasing ppv !!
			                      //       so far, the TP @ ECR is wrong !!!
			modelProps.trainingSample.setSampleSize( dataSampler.getSizeTrainingSet() );
			
			
			// getRocCurve creates a copy of the double[][] array
			modelProps.trainingSample.roc.rocCurve = roccer._rocN.getRocCurve() ;
			modelProps.trainingSample.roc = new RoC( roccer._rocN );
			
			nodeCases = roccer.totalPCount;
			modelProps.trainingSample.observationCount = roccer.totalObsCount; 
			modelProps.trainingSample.casesCount = nodeCases;
			modelProps.trainingSample.truePositives  = nodeTP;
			modelProps.trainingSample.trueNegatives  = nodeTN; // ??? non-TG values in non-hb nodes
			modelProps.trainingSample.falsePositives = nodeFP; // ??? non TG values in hb nodes , as implied by the ecr which is measuring the quality of the node
			nodeFN = nodeCases-nodeTP;
			modelProps.trainingSample.falseNegatives = nodeFN; // TG values in non-hb nodes , as implied by the ecr which is measuring the quality of the node
			modelProps.trainingSample.ecrNodes = new ArrayList<Integer>(ecrNodes) ;
			
			modelProps.trainingSample.tpSingularity = modelProps.trainingSample.roc.getTpSingularityValue(); //  rocCurve[1][0] ;
			
			modelProps.ecr = generalECR ;
			modelProps.targetGroups = classifySettings.getTargetGroupDefinition();
			modelProps.targetMode = classifySettings.getTargetMode() ;
			modelProps.targetVariable = classifySettings.getActiveTargetVariable() ;
			modelProps.targetVariableIndex = somLattice.getNode(0).getSimilarity().getIndexTargetVariable() ;
			
			
			
			nodeTP = 0;
			for (int i=0;i<ecrNodes.size();i++){
				node = somLattice.getNode( ecrNodes.get(i) ) ;
				
				nodeTP = nodeTP + (int) ((double)node.getExtensionality().getCount() * node.getExtensionality().getPPV() );
				nodeFP = nodeFP + (int) ((double)node.getExtensionality().getCount() * (1-node.getExtensionality().getPPV()));
					
			}
			modelProps.trainingSample.ecrTP = nodeTP;
			modelProps.trainingSample.ecrFP = nodeFP;
			modelProps.trainingSample.ecrRelSize = (double)(nodeTP+nodeFP)/ (double)modelProps.trainingSample.observationCount;
			modelProps.trainingSample.ecrRelTP = (double)nodeTP/(double)modelProps.trainingSample.casesCount ;
			modelProps.trainingSample.ecrRelRisk = (double)nodeTP/(double)(nodeTP+nodeFP) ;
			 
			out.printErr(2,"\nquality @ ecr, risk compliant nodes count n="+ecrNodes.size()+
					       ",  tp="+nodeTP+", fp="+nodeFP+
					       ",  tp singularity ="+ String.format("%.3f",modelProps.trainingSample.tpSingularity) +
					       ",  AuC "+String.format("%.4f",modelProps.trainingSample.roc.AuC)+"\n");
			
			// displayResultsOnNodes();
		}
		
		 

		private void createGlobalDescription(){
			
			/*
			 *  TODO:  - create a complete confusion matrix
			 *         - create ppv per sub-target
			 *         - global ROC, ROC per class in multitargets
			 *         - TP @ FP=0
			 */
			
			MetaNodeIntf node;
			ArrayList<Double> tvValues  ;
			double v;
			
			tvValues = new ArrayList<Double>() ;
			
			for (int i=0;i<somLattice.size();i++){
				node = somLattice.getNode(i) ;
				tvValues.addAll( node.getTargetVariableValues() );
			}
			
			lcd.overallFrequencyList = new FrequencyList( ((FrequencyListGeneratorIntf)this) ) ;
			
			if (classifySettings.getTargetMode() ==ClassificationSettings._TARGETMODE_SINGLE){
				lcd.overallFrequencyList.digestValuesForTargets( tvValues, classifySettings.getECR(),
																 classifySettings.getTargetGroupDefinition(), 
																 classifySettings.getTGlabels()) ;
			}
			if (classifySettings.getTargetMode() ==ClassificationSettings._TARGETMODE_MULTI){
				lcd.overallFrequencyList.digestValues( tvValues );
			}

			// somehow that gets wrong, ... displaying values >1
			
			lcd.ppv = (double)lcd.ccSum/((double)lcd.rnSum) ;
			//   tv_fqsum/(tv_fqsum + )  ;
			lcd.totalRecordCount = tvValues.size() ;
			
			lcd.tpSingularity = getTpSingularity(); // the value for TP @ FP=0
			
			int tv_fqsum=0, fq ;
			String str="",tvInfoStr="" ;
			
			for (int i=0;i<lcd.overallFrequencyList.itemFrequencies.items.size();i++){
				 
				v = lcd.overallFrequencyList.itemFrequencies.items.get(i).observedValue ;
				fq = lcd.overallFrequencyList.itemFrequencies.items.get(i).frequency;
				str = str + "("+fq+","+String.format("%.2f",v)+")  ";
				
				if (classifySettings.getTargetMode() ==ClassificationSettings._TARGETMODE_SINGLE){
					if (v>=0){
						tv_fqsum = tv_fqsum+fq ;    
						tvInfoStr = "   for target  : "+ tv_fqsum+"\n" ;
					}
				}
			}
			out.printErr(2, "overall ...\n"+
					        "   ppv         : "+ String.format("%.2f",lcd.ppv)+"\n"+
		     			    "   frequencies : "+ str+"\n"+ tvInfoStr +
		     			    "   tp singular : " +String.format("%.2f",lcd.tpSingularity)+" \n ") ; 
			
			
		}
		
		/**
		 * 
		 * TP without FP. PPV without risk, 
		 * starting point of ROC
		 * 
		 * @return
		 */
		public double getTpSingularity(){
			double result=0;
			int nrc, rsum=0;
			double v, ppv;
			String str;
			
			MetaNodeIntf node;
			
			
			for (int i=0;i<somLattice.size();i++){
				
				node = somLattice.getNode(i) ;
				
				ppv = node.getExtensionality().getPPV() ;
				
				v = node.getExtensionality().getMajorityValueIdentifier();
				
				nrc = node.getExtensionality().getCount() ;

				if (ppv==1.0){
					rsum = rsum+ nrc; 
				}
				
			} // i -> all nodes
			
			result = (double)rsum/lcd.totalRecordCount;
			
			return result;
		}
		// ........................................................................
			
		
		/**
		 * here we qualify towards the target group !
		 */
		private boolean evaluateByEcrRiskMeasure(boolean targetDefined) {
			
			boolean ecrSatisfied = false;
			double generalECR = -1.0;
			boolean noECRs = true ;
			int k;
			
			generalECR = classifySettings.getECR() ;
			noECRs = (classifySettings.getECRs()==null) || (classifySettings.getECRs().length==0) ;
				
			if ((generalECR<0.0) && (noECRs)){
				// we make it accessible, since there is no definition of risk
				frequencyList.majorityIsActive = 1 ;
				return true;
			}
			
			if ((noECRs==true) && (generalECR<0)){
				frequencyList.majorityIsActive = -1;
				
			}else{
				
				if (targetDefined){
					// there is a problem if are for beta's... ecr could be > 0.99 such the majority would -3.0
					frequencyList.majorityIsActive = 0 ;
					if (generalECR<0.5){
						// A: majority is in TG 
						if (frequencyList.majority.observedValue>=0){
							if (( generalECR > (1.0-frequencyList.ppv) ) ){
								frequencyList.majorityIsActive = 1 ;
								// frequencyList.majority.observedValue = ...
								ecrSatisfied=true;
							}	

						}else{
						// B: majority is NOT in TG, ... we need a group-ppv and a target ppv
						//    example: ecr=0.14 ppv=0.7 -> false, ppv=0.89 -> true
							if (( generalECR > (1-frequencyList.ppv) ) ){
								
								frequencyList.majorityIsActive = 1 ;
								ecrSatisfied=true;
							}	
						}
					} // generalECR<0.5 == std
					else{ // >0.5 tolerant for missed targets
						
					}
					 
				}else{
					if ( generalECR > (1.0-frequencyList.ppv)) {
						frequencyList.majorityIsActive = 1 ;
						ecrSatisfied=true;
					}else{
						frequencyList.majorityIsActive = 0 ;
					}
					
				}
			
				//  e.g. 0.2 -> ppv should be > 0.8 or: (1-ppv) < ecr 
				
			}
			k=0 ;
			return ecrSatisfied;
		} // evaluateByEcrRiskMeasure()


		private void determineROCvalues() {
			//  
			// -> wrap this into a class
			roccer = new Roccer();
			roccer.measureCurve() ;
			
		} // determineROCvalues()
		
		
		
	} // inner class Majorities 
	// ........................................................................

	/**
	 * this class provides all the necessary measurement "devices" for characterizing
	 * the classification of "new" observations
	 * 
	 * a dynamic list ("field") of "pseudo-nodes" will be set up, which then
	 * can be checked as we do in case of training sample 
	 * 
	 */
	class SampleValidator{
		
		private SomAppValidationIntf somValidation ;
		
		ArrayList<Integer> recordIDs ;
		
		// we need a pseudo lattice in order to create a RoC!
		LatticePropertiesIntf latticeProperties;
		VirtualLattice pseudoLattice;

		Majorities majorities;
		
		
		// ................................................
		public SampleValidator( ArrayList<Integer> recordIndices){
			
			recordIDs = recordIndices;
			somValidation = (SomAppValidationIntf)(new SomApplication());
			// somValidation = somFluidParent.getSomValidationInstance() ;
			
			latticeProperties = dSom.getSomProcessParent().getLatticeProperties() ;
			
			
			/*
			 * 
			 */
			pseudoLattice = new VirtualLattice(dSom.getSomProcessParent(), latticeProperties,50);
		}
		// ................................................
		
		
		public void perform(){
											out.print(2,"...going to validate the SomLattice using "+(recordIDs.size())+" records...") ;
			recordIDs = dataSampler.getValidationSet() ;
			establishPseudoLattice();
											out.print(2,"...all records of the validation sample have been visited...") ;
			// now we are going to test that...
			// the pseudoLattice contains 
			majorities = new Majorities();	
			majorities.determineTargetGroups( pseudoLattice );
			
			// now, the validation result data of the pseudo lattice are in the modelProperties.trainingSample
			// we have to copy those result data to the modelProperties.validationSample of the main somLattice
			
			somLattice.getModelProperties().setValidationSample( pseudoLattice.getModelProperties().getTrainingSample() ) ;
			somLattice.getModelProperties().getValidationSample().setSampleSize( dataSampler.getSizeValidationSet() );
			
		}
		/** this is applicable only to validation of classification tasks, for regression tasks we need sth different ! */
		protected void establishPseudoLattice(){
			
			int ix, bmuIndex ;
			double tvVal ;
			int tp=0,fp=0, fn=0, tn=0, p, tvIndex;
			long nodeID;
			boolean observationIsTarget, classifiedAsTarget;

			SomApplicationResults resultObj;
			ResultAspect rAspect;
			MetaNode nativeNode, pseudoNode;
			ArrayList<Integer> recordIndexes = new ArrayList<Integer>();
			
			somValidation.setDSomInstance(dSom) ;
			somValidation.setSomData(somData) ;
			
			try {

				for (int i=0;i<recordIDs.size();i++){
					
					// we "prepare" a record and send it to the lattice
					ix = recordIDs.get(i) ; 
					
					// there is no need to actually prepare the data, since we share the data source
					// it will do much the same as in the last step of learning
					resultObj = somValidation.classify( ix ) ;
					 
					
					if ((resultObj!=null) && (resultObj.topEcrNodes!=null) && (resultObj.topEcrNodes.size()>0)){

						
						bmuIndex = resultObj.topEcrNodes.get(0);
						
						recordIndexes.clear();
						recordIndexes.add( bmuIndex )  ;
						
						// does the pseudoLattice contain a node with serialID of the native node?
						nativeNode = somLattice.getNode(bmuIndex) ;
						nodeID = nativeNode.getNumID() ; 
						// we have to add the record ix to the pseudoNode; 
						pseudoNode = pseudoLattice.getNodeByNumId( nodeID ) ;
						if (pseudoNode==null){
							
							// pseudoNode = new MetaNode( pseudoLattice, somData);
							
							pseudoNode = new MetaNode( pseudoLattice, somData, nativeNode) ;
							 
							pseudoNode.setNumID( nodeID ) ;
							pseudoNode.clearData() ;
							pseudoLattice.addNode(pseudoNode) ; // ??
						}
						
						pseudoNode.importDataByIndex(recordIndexes);						
						
						rAspect = resultObj.aspects.get(0) ;
						
						observationIsTarget = rAspect.classType >= 1 ;
						classifiedAsTarget  = rAspect.observationClassType >= 1 ; 
						
						// actually, this is not necessary here...
						if ((observationIsTarget) && (classifiedAsTarget)){
							tp++;
						}
						if ((observationIsTarget==false) && (classifiedAsTarget==false)){
							tn++;
						}
						if ((observationIsTarget) && (classifiedAsTarget==false)){
							fp++;
						}
						if ((observationIsTarget==false) && (classifiedAsTarget)){
							fn++;
						}
						
					}else{
						
					}
				} // i-> all records

				
				ix=0;
				
			} catch (Exception e) {

				e.printStackTrace();
			}
			ix=0;
		}
		
		
	} // inner class SampleValidator
	
	// ........................................................................
	
	class Roccer{
		RoC _rocN, _rocR;
		
		int totalNCount = 0;
		int totalPCount = 0;
		int totalObsCount=0;
			
		
		
		public Roccer(){
			
		}
		
		@SuppressWarnings("rawtypes")
		class rocitComparator implements Comparator{

			
			@Override
			public int compare( Object roci1, Object roci2 ) {
				double v1,v2 ;
				int result=0, n1,n2 ;
				
				v1 = ((tRocItem)roci1).ppv ;
				v2 = ((tRocItem)roci2).ppv ;

				n1 = ((tRocItem)roci1).nodeSize ;
				n2 = ((tRocItem)roci2).nodeSize ;
				
				if (v1>v2){
					result=-1;
				}else{
					if (v1<v2){
						result= 1;
					}	
					if (v1==v2){
						if (n1>n2){
							result=-1;
						}else{
							result= 1;
						}
						
					}
				}
				
				return result;
			}
			
		}
		class tRocItem{
			long nodeID = -1;
			int nodeSize = -1;
			double ppv, npv ;
			double cumTpRate = -1.0;
			double ordFpRate = -1.0;
			double relNodeSize;
		}
		
		@SuppressWarnings("unchecked")
		public void measureCurve(){
			tRocItem rItem;
 			ArrayList<tRocItem> rocItems = new ArrayList<tRocItem>();  
 			MetaNode node;
 			int k,pSum=0, nSum=0  ,nonemptyNodeCount=0;
 			double tpr,fpr,v1,v2 ;
 			 
 			k=0;
			// we first create a list of items : nodeID, node size, ppv, npv, npv[] 
			for (int i=0;i<somLattice.size();i++){
				
				rItem = new tRocItem() ;
				node = somLattice.getNode(i) ;
				
				rItem.nodeID = node.getSerialID() ;
				rItem.ppv = node.getExtensionality().getPPV( ) ;
				rItem.npv = node.getExtensionality().getNPV( ) ;
				rItem.nodeSize = node.getExtensionality().getCount();
				rItem.relNodeSize = 0.0 ;
				
				totalObsCount = totalObsCount + rItem.nodeSize ;
				
				totalPCount = (int) (totalPCount + rItem.nodeSize * rItem.ppv) ; 
				totalNCount = (int) (totalNCount + rItem.nodeSize * rItem.npv) ;
				
				if (rItem.nodeSize>0){
					nonemptyNodeCount++ ;
				}
				rocItems.add(rItem);
				
				 
			} // i ->
			
			// next we sort this list : decreasing ppv, decreasing size
			
			Collections.sort( rocItems, new rocitComparator()) ;
			
			k=0;
			// then we determine ROC as pairs of values , triggered by changing ppv in items
			_rocN = new RoC();
			_rocN.rocCurve = new double[2][nonemptyNodeCount] ;

			_rocR = new RoC();
			_rocR.rocCurve = new double[2][nonemptyNodeCount] ;
			
			pSum=0; nSum=0; 
			double nn,tn;
			int z=0, rcz=0 ,fpzn=0; 
			
			try{
				_rocN.rocCurve[0][0] = 0;
				_rocN.rocCurve[1][0] = 0 ;
				for (int i=0;i<rocItems.size();i++){
					v1 = Math.max(0,rocItems.get(i).npv);
					v2 = Math.max(0,rocItems.get(i).ppv);
					if ((v1<=0.0) && (v2<1.0)){
						v1 = 1.0-v2;
					}
					if (rocItems.get(i).nodeSize==0){
						continue;
					}
					rocItems.get(i).relNodeSize = (double)rocItems.get(i).nodeSize/(double)totalObsCount;
					_rocR.rocCurve[0][rcz] = v1;
					_rocR.rocCurve[1][rcz] = v2;
					
					pSum = pSum + (int) Math.round( v2*rocItems.get(i).nodeSize );
					nSum = nSum + (int) Math.round( v1*rocItems.get(i).nodeSize );
					
					tpr = (double)pSum/(double)totalPCount ;
					fpr = (double)nSum/(double)totalNCount ;
					
					nn = Math.min(Math.max(0,fpr),1.0) ;
					tn = Math.min(Math.max(0,tpr),1.0);
					
					// care for bindings in x-direction == for nn
					if ((z>0) && (nn==_rocN.rocCurve[0][z-1])){
						if (z-1>=0){
							_rocN.rocCurve[0][z-1] = nn;
							_rocN.rocCurve[1][z-1] = tn ;
						}
						
					}else{
						if (z-fpzn<_rocN.rocCurve[0].length){
							_rocN.rocCurve[0][z-fpzn] = nn;
							_rocN.rocCurve[1][z-fpzn] = tn ;
							z++;
						}
						if (fpr==0.0){
							fpzn++;
						}

					}
				
					
				}
				// due to bindings of fpr values, we have to resize
				
				double[] r1values = new double[z] ;
				double[] r2values = new double[z] ;
				System.arraycopy(_rocN.rocCurve[0], 0, r1values , 0, z-fpzn);
				System.arraycopy(_rocN.rocCurve[1], 0, r2values , 0, z-fpzn);
				
				_rocN.rocCurve = new double[2][z] ;
				System.arraycopy( r1values , 0,  _rocN.rocCurve[0], 0, z-fpzn);
				System.arraycopy( r2values , 0,  _rocN.rocCurve[1], 0, z-fpzn);
				
				
				r1values = new double[z] ;
				r2values = new double[z] ;
				
				System.arraycopy(_rocR.rocCurve[0], 0, r1values , 0, z-fpzn);
				System.arraycopy(_rocR.rocCurve[1], 0, r2values , 0, z-fpzn);
				
				_rocR.rocCurve = new double[2][z] ;
				System.arraycopy( r1values , 0,  _rocR.rocCurve[0], 0, z-fpzn);
				System.arraycopy( r2values , 0,  _rocR.rocCurve[1], 0, z-fpzn);
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			// 
			double dx, dy ;
			if ((_rocN.rocCurve[1].length>=3)){
				dy = _rocN.rocCurve[1][1]-_rocN.rocCurve[1][0] ;
				dx = _rocN.rocCurve[0][1]-_rocN.rocCurve[0][0] ;
				v1 = dy/dx ;
				this._rocR.riskDynamics = v1 ;
			}
			
			
			// last we determine the area under curve 
			
			z = _rocN.rocCurve[0].length;
			v2 = 0;
			
			for (int i=1;i<z;i++){
				 
				dy = (_rocN.rocCurve[1][i]+_rocN.rocCurve[1][i-1])/2.0 ; // <- tp rates
				dx = _rocN.rocCurve[0][i]-_rocN.rocCurve[0][i-1] ; // <- fp rate
				v1 = dx*dy;
				v2 = v2+v1;
			}
			
			_rocN.AuC = v2 ;
			k=0;
			
			/*
			 *  explicitly sum up clusters P,N until ECR is reached, for 
			 *    - a plot  X:cumul. risk, Y:N of records
			 *    - list of nodes, that fall within
			 *    - 
			 */
			
		}
		
		
		
	}
}
