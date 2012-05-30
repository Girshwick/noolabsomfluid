package org.NooLab.somfluid;

import java.util.ArrayList;
import java.util.Arrays;




public class VariableSettingsHandler implements VariableSettingsHandlerIntf{

	
	ArrayList<String> initialSelection = new ArrayList<String>();
	ArrayList<String> blackListedVariables = new ArrayList<String>();
	ArrayList<String> absoluteExclusions = new ArrayList<String>();
	
	int absoluteExclusionsMode = 1 ;
	
	String targetVariable ="" ;
	ArrayList<String> targetVariableCandidates = new ArrayList<String>(); 
	
	ArrayList<String> tvGroupLabels ;
	
	// ========================================================================
	public VariableSettingsHandler(){
		 
	}
	// ========================================================================
	
	
	
	@Override
	public void setTargetVariables(String ...vlabels) {
		
		String vlabel = vlabels[0];
		targetVariable = vlabel;
		targetVariableCandidates = new ArrayList<String>(Arrays.asList(vlabels));
		if (targetVariableCandidates.size()>1){
			targetVariableCandidates.remove(0) ;
		}
	}

	
	@Override
	public void setInitialSelection(String[] strings) {
		
		initialSelection = new ArrayList<String>(Arrays.asList(strings));
 	}

	@Override
	public void setBlackListedVariables(String[] strings) {
		
		blackListedVariables = new ArrayList<String>(Arrays.asList(strings));
	}

	@Override
	public void setAbsoluteExclusions(String[] strings, int mode) {
		 
		absoluteExclusions = new ArrayList<String>(Arrays.asList(strings));
		absoluteExclusionsMode = mode;
	}



	public ArrayList<String> getInitialSelection() {
		return initialSelection;
	}



	public void setInitialSelection(ArrayList<String> initialSelection) {
		this.initialSelection = initialSelection;
	}



	public ArrayList<String> getBlackListedVariables() {
		return blackListedVariables;
	}



	public void setBlackListedVariables(ArrayList<String> blackListedVariables) {
		this.blackListedVariables = blackListedVariables;
	}



	public ArrayList<String> getAbsoluteExclusions() {
		return absoluteExclusions;
	}



	public void setAbsoluteExclusions(ArrayList<String> absoluteExclusions) {
		this.absoluteExclusions = absoluteExclusions;
	}



	public String getTargetVariable() {
		return targetVariable;
	}



	@Override
	public void setTvGroupLabels(String ...labels) {
		
		String[] tvglabels = new String[0];
		
		if ((labels!=null) && (labels.length>0)){
			tvGroupLabels = new ArrayList<String>(Arrays.asList(tvglabels));
		}
		
		
	}

 
	public ArrayList<String> getTvGroupLabels() {
		return tvGroupLabels;
	}



	public void setTvGroupLabels(ArrayList<String> tvGroupLabels) {
		this.tvGroupLabels = tvGroupLabels;
	}



	public int getAbsoluteExclusionsMode() {
		return absoluteExclusionsMode;
	}



	public void setAbsoluteExclusionsMode(int absoluteExclusionsMode) {
		this.absoluteExclusionsMode = absoluteExclusionsMode;
	}



	public ArrayList<String> getTargetVariableCandidates() {
		return targetVariableCandidates;
	}



	public void setTargetVariableCandidates(ArrayList<String> targetVariableCandidates) {
		this.targetVariableCandidates = targetVariableCandidates;
	}



	public void setTargetVariable(String targetVariable) {
		this.targetVariable = targetVariable;
	}

	
	
	
}
