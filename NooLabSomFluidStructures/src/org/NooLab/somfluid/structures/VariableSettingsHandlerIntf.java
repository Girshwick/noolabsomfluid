package org.NooLab.somfluid.structures;

import java.util.ArrayList;
import java.util.Collection;

import org.NooLab.utilities.datatypes.ValuePair;





public interface VariableSettingsHandlerIntf {


	public void setInitialSelection(String[] strings) ;

	public void setInitialSelection(ArrayList<String> initialSelection) ;

	public ArrayList<String> getInitialSelection()  ;

	
	// ----------------------------------------------------

	public void setIdVariableCandidates( ArrayList<String> idVariableCandidates) ;

	public void setIdVariable( String idVariableCandidates) ;
	
	public ArrayList<String> getIdVariableCandidates();

	public String getIdVariable();

	// ----------------------------------------------------
	

	// public void setSomTargetModelingMode(String modeStr);

	 
	public void setSomTargetModelingMode(String modeStr);
	
	
	public void setTargetVariable(String targetVariable) ;

	public void setTargetVariables(String ...vlabels);
	public void setTargetVariableCandidates(ArrayList<String> targetVariableCandidates) ;
	
	public ArrayList<String> getTargetVariableCandidates() ;

	public String getTargetVariable() ;
	

	public void setTvGroupLabels(String ...labels);
	public void setTvGroupLabels(ArrayList<String> tvGroupLabels) ;

	/** several groups are allowed, both for single target as well as for multi targets  */
	public void addSingleTargetGroupDefinition(double borderLo, double borderHi, String groupLabel);
	
	public ArrayList<ValuePair> getTvGroupIntervals();
	
	public ArrayList<String> getTvGroupLabels()  ;

	// ----------------------------------------------------    
	
	public void setTreatmentDesignVariables( ArrayList<String> stringsList);
	public void setTreatmentDesignListedVariables( String[] stringsList);

	public ArrayList<String> getTreatmentDesignListedVariables() ;
	
	public void setGroupIndicatorDesignVariables(ArrayList<String> items);
	public void setGroupIndicatorDesignVariables( String[] stringsList);
	
	public ArrayList<String> getGroupIndicatorDesignVariables() ;
	
	// ----------------------------------------------------

	public void setWhiteListedVariables( ArrayList<String> stringsList);
	public void setWhiteListedVariables( String[] stringsList);

	public ArrayList<String> getWhiteListedVariables() ;
	
	public void setBlackListedVariables(String[] strings);
	public void setBlackListedVariables(ArrayList<String> blackListedVariables) ;

	public ArrayList<String> getBlackListedVariables() ;
	
	public void setAbsoluteExclusions(ArrayList<String> items, int mode);
	public void setAbsoluteExclusions(String[] strings, int mode);
	public void setAbsoluteExclusions(ArrayList<String> absoluteExclusions) ;

	public ArrayList<String> getAbsoluteExclusions() ;

	public int getAbsoluteExclusionsMode() ;
	
	public void setAbsoluteExclusionsMode(int absoluteExclusionsMode) ;

	public ArrayList<String> getGroupDesignVariables();

	// ----------------------------------------------------

	public String createXmlRepresenation();
	
	public String createSimplifiedIniRepresenation() ;

	
	// === just for serialization =============================================
	
	
	public String getSomModelingMode() ;

	public ArrayList<String> getTreatmentDesignVariables() ;

	public void setTvGroupIntervals(ArrayList<ValuePair> tvGroupIntervals) ;

	public void setIdVariableCandidates(String[] idCandidates);

	
	
	
	
}
