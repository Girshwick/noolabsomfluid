package org.NooLab.somfluid.core.engines.det.results;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.utilities.logging.PrintLog;




public class SomTargetResults {

	DSom dSom;
	VirtualLattice somLattice;
	SomDataObject somData;
	SomFluid somFluidParent;
	
	SomValidation validation;
	
	ModelingSettings modelingSettings ;
	ClassificationSettings classifySettings;
	
	boolean singleModeUndefined = false;
	
	// ------------------------------------------
	
	// a collection of lists, each item in this collection refers to a particular node
	ArrayList<FrequencyList> minorityFrequencies = new ArrayList<FrequencyList>();
	
	Majorities majorities ;
	
	ArrayList<ValueCode> expectedValueCodes = new ArrayList<ValueCode>();
	
	LatticeClassDescription latticeClassDescription ;
	
	
	// ------------------------------------------
	PrintLog out;
	
	// ========================================================================
	public SomTargetResults( DSom dsom, ArrayList<Integer> sampleRecord, ModelingSettings modSettings) {
		// 
		dSom = dsom;
		
		somFluidParent = dSom.getSomFluidParent() ;
		somLattice = dSom.getSomLattice();
		somData = dSom.getSomData();
		
		validation = new SomValidation(somFluidParent);
		
		
		modelingSettings = modSettings;
		classifySettings = modelingSettings.getClassifySettings() ;
	
		
		prepareExpectedValueCodes();
		
		latticeClassDescription = new LatticeClassDescription();
		out = somData.getOut() ;
	}
	
	private void prepareExpectedValueCodes() {
		// 
		// expectedValueCodes , ArrayList<ValueCode>
		
		/* from  classifySettings
			double[][] TGdefinition ;
			String[] TGlabels;
		   	
		*/
	}
	// ========================================================================	

	/**
	 * this is being called e.g. by "executeSOM()" in class "DSomCore{}"
	 * 
	 */
	public void prepare() {
		 
		try{
			
			// further processing dependent on the modeling mode
			
			
			String _atv = classifySettings.getActiveTargetVariable();
			if (_atv.length()==0){
				if (modelingSettings.getActiveTvLabel().length()>0){
					classifySettings.setActiveTargetVariable( modelingSettings.getActiveTvLabel() );
				}
			}
			
			int tm = classifySettings.getTargetMode();
			
			singleModeUndefined = ( classifySettings.getActiveTargetVariable().length()==0) ||
								  ( classifySettings.getTargetGroupDefinition() == null ) ||
								  ( classifySettings.getTargetGroupDefinition().length==0) ;
			
			if ((tm == ClassificationSettings._TARGETMODE_SINGLE) && (singleModeUndefined==false)){
				validateSingleTarget();
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
	

	
	private void displayResultsOnNodes() {
		 
		MetaNodeIntf node;
		int nrc ;
		double v;
		
		String str ;
		out.print(2,true,"\npredictive qualities of nodes and lattice:");

		
		for (int i=0;i<somLattice.size();i++){
			
			node = somLattice.getNode(i) ;
			
			v = node.getExtensionality().getPPV() ;
			str = "ppv = " + String.format("%.3f",v);
			
			
			v = node.getExtensionality().getMajorityValueIdentifier();
			str = str + " ,  majority for target value : "+String.format("%.3f",v);
			
			
			nrc = node.getExtensionality().getCount() ;
			if (nrc>0){
				out.print(2,true, "i "+i+"  ---  [n:"+nrc+"] "+str ); // output suppressing the prefix ...
			}else{
				out.print(2,true, "i "+i+"  ---  [n:"+nrc+"] ");
			}
			
			
		} // i -> all nodes
		
		out.print(2,true," - - - - - \n");
	}
	
	

	protected void validateSingleTarget(){
		
											out.print(2, "checking for results on SOM by assuming mode = _TARGETMODE_SINGLE");
		try{
			
			
		}catch(Exception e){
			
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
	
	
	// ........................................................................
	class Majorities 	implements 
	 								FrequencyListGeneratorIntf {
		
		FrequencyList frequencyList  ; 
		RocCurve roc ;
		LatticeClassDescription lcd;
		
		public Majorities(){
			
			lcd = latticeClassDescription ;
		}


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
				
				evaluateByEcrRiskMeasure();
				
				minorityFrequencies.add(frequencyList) ;

				
				node.getExtensionality().setPPV( frequencyList.ppv ) ;
				node.getExtensionality().setMajorityValueIdentifier( frequencyList.majority.observedValue );

				lcd.ccSum = lcd.ccSum + (frequencyList.ppv * node.getExtensionality().getCount() ) ;
				lcd.rnSum = lcd.rnSum + node.getExtensionality().getCount() ;
				
			} // i-> all nodes in lattice
			
			createGlobalDescription();
			
			
			determineROCvalues();
			
			// put this info as a property to the lattice
			
		}

		private void createGlobalDescription(){
			
			/*
			 *  TODO:  - create a complete confusion matrix
			 *         - create ppv per sub-target
			 *         - global ROC, ROC per class
			 *         - TP @ FP=0
			 */
			
			MetaNodeIntf node;
			ArrayList<Double> tvValues  ;
			
			
			tvValues = new ArrayList<Double>() ;
			
			for (int i=0;i<somLattice.size();i++){
				node = somLattice.getNode(i) ;
				tvValues.addAll( node.getTargetVariableValues() );
			}
			
			lcd.overallFrequencyList = new FrequencyList( ((FrequencyListGeneratorIntf)this) ) ;
			lcd.overallFrequencyList.digestValues( tvValues );
			
			lcd.ppv = lcd.ccSum/((double)lcd.rnSum) ;
			
			
			
			String str="" ;
			for (int i=0;i<lcd.overallFrequencyList.itemFrequencies.items.size();i++){
				double v = lcd.overallFrequencyList.itemFrequencies.items.get(i).observedValue ;
				str = str + "("+lcd.overallFrequencyList.itemFrequencies.items.get(i).frequency+","+String.format("%.2f",v)+")  ";
			}
			out.printErr(2, "overall ...\n"+
					        "   ppv         : "+ String.format("%.2f",lcd.ppv)+
		     			    "   frequencies : "+ str) ;
		}
		
		
		// ........................................................................
			
		
		
		private void evaluateByEcrRiskMeasure() {
			double generalECR = -1.0;
			boolean noECRs = true ;
			
			
			generalECR = classifySettings.getECR() ;
			noECRs = (classifySettings.getECRs()==null) || (classifySettings.getECRs().length==0) ;
				
			if ((generalECR<0.0) && (noECRs)){
				// we make it accessible, since there is no definition of risk
				frequencyList.majorityIsActive = 1 ;
				return;
			}
			
			if (noECRs==false){
				frequencyList.majorityIsActive = -1;
				
				
			}else{
				
				//  e.g. 0.2 -> ppv should be > 0.8 or: (1-ppv) < ecr 
				if ( generalECR > (1.0-frequencyList.ppv) ){
					frequencyList.majorityIsActive = 1 ;
				}else{
					frequencyList.majorityIsActive = 0 ;
				}
				
			}
			
			
		} // evaluateByEcrRiskMeasure()


		private void determineROCvalues() {
			//  
			// -> wrap this into a class
			
			// we first create a list of items (minorFrqIndex, ppv)
			for (int i=0;i<minorityFrequencies.size();i++){
				
			} // i ->
			
			// next we sort this list : decreasing ppv, decreasing size
			
			
			// then we determine ROC as pairs of values , triggered by changing ppv in items
			
			
			
			// last we determine the area under curve using some external package (flanagan?)
			
			
			
		} // determineROCvalues()
		
		
		
	} // inner class Majorities 
	// ........................................................................
		
	
	 
	private void validateRegressionClassTarget() {

											out.print(2, "checking for results on SOM by assuming mode = _TARGETMODE_REGR");
		try{
			
			
		}catch(Exception e){
			
		}
		
	}

	
}
