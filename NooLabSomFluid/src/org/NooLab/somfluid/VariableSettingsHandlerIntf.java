package org.NooLab.somfluid;

import java.util.ArrayList;

public interface VariableSettingsHandlerIntf {

	void setTargetVariables(String ...vlabels);

	void setBlackListedVariables(String[] strings);

	void setAbsoluteExclusions(String[] strings, int mode);

	void setInitialSelection(String[] strings) ;


	public ArrayList<String> getInitialSelection()  ;

	public void setInitialSelection(ArrayList<String> initialSelection) ;
	
	public ArrayList<String> getBlackListedVariables() ;
	
	public void setBlackListedVariables(ArrayList<String> blackListedVariables) ;

	public ArrayList<String> getAbsoluteExclusions() ;

	public void setAbsoluteExclusions(ArrayList<String> absoluteExclusions) ;

	public String getTargetVariable() ;

	void setTvGroupLabels(String ...labels);
	

	public ArrayList<String> getTvGroupLabels()  ;

	public void setTvGroupLabels(ArrayList<String> tvGroupLabels) ;

	public int getAbsoluteExclusionsMode() ;

	public void setAbsoluteExclusionsMode(int absoluteExclusionsMode) ;

	public ArrayList<String> getTargetVariableCandidates() ;

	public void setTargetVariableCandidates(ArrayList<String> targetVariableCandidates) ;

	public void setTargetVariable(String targetVariable) ;
	
	
}
