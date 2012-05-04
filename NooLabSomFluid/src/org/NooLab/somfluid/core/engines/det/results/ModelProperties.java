package org.NooLab.somfluid.core.engines.det.results;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somscreen.SomQualityData;
 

import org.math.array.*;

public class ModelProperties implements Serializable,
										ModelPropertiesIntf{

	private static final long serialVersionUID = -4278442100712457201L;

	private int index=-1;
	
	String targetVariable = "" ;
	int targetVariableIndex = -1;
	
	int targetMode = -1;
	
	double[][] targetGroups;
	double ecr = -1.0 ;
	
	// here, in a particular lattice, there is just 1, in SomBags there could be many 
	ValidationSet trainingSample   = new ValidationSet();

	/** by default, there is just 1 validationsample, 
	 * but there could be many, if we characterize its statistics 
	 */
	ArrayList<ValidationSet> validationSamples ;
	ValidationSet globalSample     = new ValidationSet();


	public String dSomGuid="";


	public SomFluidTask task;

	private boolean calculationsOk=false;

	private ArrayList<String> variableSelection;

	public SomQualityData sqData;

	public double ecrForAdjustment;


	
	
	// some measures describing the lattice
	
		// the training sample should NOT be different from validation samples, they should belong to the "same" population   

		// homogeneity measures with target group, non-target group,
		// 
	
	// some measures describing the population of nodes
	
	
	
	
	// ========================================================================
	public ModelProperties(){
		validationSamples = new ArrayList<ValidationSet>();
		validationSamples.add( new ValidationSet()) ;
		
	}
	
	public ModelProperties(ModelProperties modprop) {

		dSomGuid = modprop.dSomGuid ;
		index = modprop.index;
		
		
		targetVariable = modprop.targetVariable ;
		targetVariableIndex = modprop.targetVariableIndex;
		
		targetMode = modprop.targetMode;
		
		targetGroups = DoubleArray.copy( modprop.targetGroups) ;

		ecr = modprop.ecr;
		
		calculationsOk = modprop.calculationsOk;
		
		trainingSample = new ValidationSet( modprop.trainingSample);

		
		validationSamples = new ArrayList<ValidationSet>();
		
		for (int i=0;i<modprop.validationSamples.size();i++){
			validationSamples.add( new ValidationSet(modprop.validationSamples.get(i)) );
		}
		globalSample = new ValidationSet( modprop.globalSample);

		variableSelection = new ArrayList<String>();
		if ((modprop.variableSelection!=null) && (modprop.variableSelection.size()>0)){
			variableSelection.addAll(modprop.variableSelection);
		}
	}
 
 
	
	// ========================================================================


	public void close(){
		validationSamples.clear();
		validationSamples=null;
		//globalSample.ecrNodes.clear();
		globalSample=null;
		if ((trainingSample!=null) && (trainingSample.ecrNodes!=null)){
			trainingSample.ecrNodes.clear();
		}
		trainingSample=null;
	}

	public void setIndex(int index) {
		 this.index = index;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	public String getTargetVariable() {
		return targetVariable;
	}



	public void setTargetVariable(String targetVariable) {
		this.targetVariable = targetVariable;
	}



	public int getTargetVariableIndex() {
		return targetVariableIndex;
	}



	public void setTargetVariableIndex(int targetVariableIndex) {
		this.targetVariableIndex = targetVariableIndex;
	}



	public int getTargetMode() {
		return targetMode;
	}



	public void setTargetMode(int targetMode) {
		this.targetMode = targetMode;
	}



	public double[][] getTargetGroups() {
		return targetGroups;
	}



	public void setTargetGroups(double[][] targetGroups) {
		this.targetGroups = targetGroups;
	}



	public double getEcr() {
		return ecr;
	}



	public void setEcr(double ecr) {
		this.ecr = ecr;
	}



	public ValidationSet getTrainingSample() {
		return trainingSample;
	}

	public void setTrainingSample(ValidationSet trainingSample) {
		this.trainingSample = trainingSample;
	}

	public ValidationSet getValidationSample() {
		return validationSamples.get(0);
	}

	public ArrayList<ValidationSet> getValidationSamples() {
		return validationSamples;
	}

	public void setValidationSamples(ArrayList<ValidationSet> validationSamples) {
		this.validationSamples = validationSamples;
	}

	public void setValidationSample(ValidationSet validationSample) {
		if (validationSamples==null){
			validationSamples = new ArrayList<ValidationSet>();
		}
		if (validationSamples!=null){
			validationSamples.clear() ;
			this.validationSamples.add( validationSample);
		}
		
	}
	public void addValidationSample(ValidationSet validationSample) {
		this.validationSamples.add( validationSample);
	}

	
	public ValidationSet getGlobalSample() {
		return globalSample;
	}



	public void setGlobalSample(ValidationSet globalSample) {
		this.globalSample = globalSample;
	}

	public boolean isCalculationsOk() {
		 
		return calculationsOk;
	}

	/**
	 * @param calculationsOk the calculationsOk to set
	 */
	public void setCalculationsOk(boolean calculationsOk) {
		this.calculationsOk = calculationsOk;
	}

	public void setVariableSelection(ArrayList<String> varSelection) {
		if ((varSelection!=null) && (varSelection.size()>0)){
			variableSelection = new ArrayList<String>();
			variableSelection.addAll(varSelection);
		}
	}

	/**
	 * @return the variableSelection
	 */
	public ArrayList<String> getVariableSelection() {
		return variableSelection;
	}

	public ArrayList<String> getVariableSelection( ArrayList<String> previousSelection ) {
		ArrayList<String>  _selection ;
		if((variableSelection==null) || (variableSelection.size()==0)){
			_selection = previousSelection ;
		}else{
			_selection = previousSelection ;
		}
		return _selection ;
	}
	
	
}
