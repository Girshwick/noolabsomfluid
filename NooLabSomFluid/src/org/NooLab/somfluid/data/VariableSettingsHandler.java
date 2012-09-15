package org.NooLab.somfluid.data;

import java.util.ArrayList;
import java.util.Arrays;


import org.NooLab.somfluid.structures.VariableSettingsHandlerIntf;
import org.NooLab.utilities.datatypes.ValuePair;



/**
 * 
 * 

<b>[ID]</b>	the active ID variable </br>
<b>[IDS]</b>	other ID variables, which are not used in any way </br>
<b>[TV]</b>	the target variable </br>
<b>[TVS]</b>	other potential target variables; they could be used as mutual TV in a automated series of investigations </br>
<b>[blacklist]</b>	black-listed variables </br>
<b>[whitelist]</b>	variables that are mandatorily included in any model </br>
<b>[absolute exclude]</b>	variables that are absolutely excluded, no mechanism
	                exists to activate them </br>
<b>[treatment]</b>	variables indicating a treatment;  </br>
	        those variables might act as confounders, any inclusion must be checked carefully; </br>
 	        yet, in some designs they need to be included as well </br>
	        they will be checked in post-processing </br>
<b>[group]</b>	in experimental designs (forward as well as retrograde) these are important apriori organizational variables; </br>
	        often expected to be formed by random assignment, they are not randomly built; </br>
	        else, data could be split/filtered by those variables
                by default, they are excluded; </br> </br>
 * 
 * 
 * 
 */
public class VariableSettingsHandler implements VariableSettingsHandlerIntf{

	String somModelingMode = "single" ;
	
	String idVariable = "";
	ArrayList<String> idVariableCandidates = new ArrayList<String>(); 

	String targetVariable ="" ;
	ArrayList<String> targetVariableCandidates = new ArrayList<String>(); 
	
	ArrayList<String> tvGroupLabels = new ArrayList<String>(); 
	ArrayList<ValuePair> tvGroupIntervals = new ArrayList<ValuePair>();

	ArrayList<String> initialSelection = new ArrayList<String>();
	ArrayList<String> whiteListedVariables = new ArrayList<String>();
	ArrayList<String> blackListedVariables = new ArrayList<String>();
	ArrayList<String> absoluteExclusions = new ArrayList<String>();
	
	ArrayList<String> treatmentDesignVariables = new ArrayList<String>();
	ArrayList<String> groupIndicatorDesignVariables = new ArrayList<String>();
	
	
	
	int absoluteExclusionsMode = 1 ;
	
	
	// ========================================================================
	public VariableSettingsHandler(){
		 
	}
	
	// for cloning
	public VariableSettingsHandler( VariableSettingsHandlerIntf inSets){
		 
		idVariable = inSets.getIdVariable();
		idVariableCandidates = new ArrayList<String>( inSets.getIdVariableCandidates() ); 

		targetVariable = inSets.getTargetVariable() ;
		targetVariableCandidates = new ArrayList<String>(  inSets.getTargetVariableCandidates() ); 
		tvGroupLabels = new ArrayList<String>( inSets.getTvGroupLabels() ); 
		tvGroupIntervals = new ArrayList<ValuePair>( inSets.getTvGroupIntervals() ); 
			
		initialSelection = new ArrayList<String>( inSets.getInitialSelection() );
		whiteListedVariables = new ArrayList<String>( inSets.getWhiteListedVariables());
		blackListedVariables = new ArrayList<String>( inSets.getBlackListedVariables());
		absoluteExclusions = new ArrayList<String>( inSets.getAbsoluteExclusions());
		
		treatmentDesignVariables = new ArrayList<String>( inSets.getTreatmentDesignListedVariables() );
		groupIndicatorDesignVariables = new ArrayList<String>( inSets.getGroupIndicatorDesignVariables() );
		
		
		absoluteExclusionsMode = inSets.getAbsoluteExclusionsMode() ;
		
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


	@Override
	public void setIdVariableCandidates(ArrayList<String> stringsList) {
		// 
		idVariableCandidates = new ArrayList<String> (stringsList) ;
	}


	@Override
	public ArrayList<String> getIdVariableCandidates() {
		return idVariableCandidates;
	}

	@Override
	public void setIdVariableCandidates(String[] idCandidates) {
		ArrayList<String> stringsList = new ArrayList<String>(Arrays.asList(idCandidates)) ;
		
	}

	@Override
	public String getIdVariable() {
		if (idVariable==null){
			idVariable="";
		}
		return idVariable;
	}

	@Override
	public void setIdVariable(String varLabel) {
		// 
		idVariable = varLabel ;
	}


	@Override
	public void setSomTargetModelingMode(String modeStr) {
		somModelingMode = modeStr;		
	}

	@Override
	public String getSomModelingMode() {
		return somModelingMode;
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



	public String getTargetVariable() {
		if (targetVariable==null){
			targetVariable="";
		}
		return targetVariable;
	}


	@Override
	public ArrayList<ValuePair> getTvGroupIntervals() {
		 
		return tvGroupIntervals;
	}
	
	@Override
	public void addSingleTargetGroupDefinition(double borderLo, double borderHi, String groupLabel) {
		//
		tvGroupLabels.add(groupLabel);
		tvGroupIntervals.add( new ValuePair(borderLo, borderHi)) ;
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



	@Override
	public void setGroupIndicatorDesignVariables( ArrayList<String> strings) {
		// 
		groupIndicatorDesignVariables = new ArrayList<String>(strings);  
	}

	@Override
	public void setGroupIndicatorDesignVariables(String[] strings) {
		groupIndicatorDesignVariables = new ArrayList<String>(Arrays.asList(strings));
	}

	@Override
	public ArrayList<String> getGroupIndicatorDesignVariables() {
		return groupIndicatorDesignVariables;
	}

	@Override
	public ArrayList<String> getGroupDesignVariables() {
		return groupIndicatorDesignVariables;
	}

	public void setTvGroupIntervals(ArrayList<ValuePair> tvGroupIntervals) {
		this.tvGroupIntervals = tvGroupIntervals;
	}

	@Override
	public void setWhiteListedVariables(ArrayList<String> stringsList) {
		//  
		whiteListedVariables = new ArrayList<String>(stringsList);
	}



	@Override
	public void setWhiteListedVariables(String[] stringsList) {
		whiteListedVariables = new ArrayList<String>(Arrays.asList(stringsList));
	}



	@Override
	public ArrayList<String> getWhiteListedVariables() {
		return whiteListedVariables;
	}



	public ArrayList<String> getBlackListedVariables() {
		return blackListedVariables;
	}



	public void setBlackListedVariables(ArrayList<String> blackListedvariables) {
		blackListedVariables = new ArrayList<String>(blackListedvariables);
	}



	public ArrayList<String> getAbsoluteExclusions() {
		return absoluteExclusions;
	}



	public void setAbsoluteExclusions(ArrayList<String> absoluteexclusions) {
		absoluteExclusions = new ArrayList<String>(absoluteexclusions); 
	}



	@Override
	public void setAbsoluteExclusions(ArrayList<String> items, int mode) {
		// 
		absoluteExclusionsMode  = mode;
		absoluteExclusions = new ArrayList<String>(items); 
	}



	public int getAbsoluteExclusionsMode() {
		return absoluteExclusionsMode;
	}



	public void setAbsoluteExclusionsMode(int mode) {
		this.absoluteExclusionsMode = mode;
	}



	@Override
	public void setTreatmentDesignVariables(ArrayList<String> stringsList) {
		// 
		treatmentDesignVariables = new ArrayList<String>(stringsList); 
	}



	@Override
	public void setTreatmentDesignListedVariables(String[] strings) {
		// 
		treatmentDesignVariables = new ArrayList<String>(Arrays.asList(strings)); 
	}

	public ArrayList<String> getTreatmentDesignVariables() {
		return treatmentDesignVariables;
	}

	@Override
	public ArrayList<String> getTreatmentDesignListedVariables() {
		// 
		return treatmentDesignVariables;
	}


	@Override
	public String createXmlRepresenation() {
		 
		return "";
	}

	@Override
	public String createSimplifiedIniRepresenation() {
		return "";
	}




	
	
	
}
