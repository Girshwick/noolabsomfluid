package org.NooLab.somfluid.core.engines.det;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.SettingsTransporter;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.ValuePair;

import com.jamesmurty.utils.XMLBuilder;






public class ClassificationSettings implements Serializable{

	private static final long serialVersionUID = 4357835213182063303L;
	
	public static final int _TARGETMODE_NONE   = -3;
	public static final int _TARGETMODE_NOTSET =  0;
	
	
	/** 
	 * just 1 class , often from binary criterion; use target groups to define one or 
	 * several intervals that represent the desired outcome; as in case of any target modeling,
	 * one should consider optimization of the SOM (evolutionary, dependency sprites)
	 */
	public static final int _TARGETMODE_SINGLE =  1;
	
	/** multi class, often covering all possible values, ordinal scale of support points */
	public static final int _TARGETMODE_MULTI  =  2;
	
	/** 
	 * This represents a dynamic and internal target; the assignment of values of this
	 * target variable changes in the course of modeling as a  function of a particular
	 * quantile, which a particular node satisfies; requires a parameter that describes that quantile
	 * as a proportion (e.g. 0.1 for the 10% quantile); can be used to find a model that maximizes
	 * the ROC for a given risk attitude (as operationalized by the ECR)  
	 */
	public static final int _TARGETMODE_VARIANCE_QUANT = 5;
	
	
	/** regression, i.e. similar to multi, but continuous cost function (real scale of target values)
	 *  could be beta controlled additionally !  
	 *  example: the SOM should establish a prediction of a score, instead of a binary action, or a predefined class;
	 *  then, the (linear? exponential?) regression from observed score to predicted score is being investigated 
	 */
	public static final int _TARGETMODE_REGR   =  9;
	
	
	public static final int _TARGET_DEFLEVEL_RAW  = 1;
	public static final int _TARGET_DEFLEVEL_NORM = 2;
	
	
	/** provides the summarizing description of misclassifications */
	public static final int _XREQ_MISCLASSIFICATIONS_STD  = 1 ;
	/** provides the full description of misclassifications per node and per target group */
	public static final int _XREQ_MISCLASSIFICATIONS_FULL = 2 ;
	
	/** returns the measure Area-under-curve of a ROC curve, that describes a classificator  */
	public static final int _XREQ_ROC_STD    = 5 ;
	/** 
	 * returns the series of paired values that describes the ROC curve; as all ROC stuff, this also
	 * requires "SomFluidProperties._SOMTYPE_MONO" , i.e. a targeted SOM 
	 */
	public static final int _XREQ_ROC_DETAIL = 6 ;
	/** 
	 * in case of "ClassificationSettings._TARGETMODE_MULTI", this provides ROC results for 
	 * any of the target groups  
	 */
	public static final int _XREQ_ROC_FULL   = 7 ;
	
	/** 
	 * in case of  _TARGETMODE_MULTI, a re-organization (by combination) of the target groups
	 * is calculated, such that the ppv for the whole SOM gets maximized; </br>
	 * this way, one may 
	 * requires parameters (set to -3.0) if you don't want to apply the respective control parameter:
	 *   param 1: minimal size of the set of target groups;
	 *   param 2: threshold ppv -&gt; if the ppv for a sub-group is &lt;threshold, then the group will 
	 *            be primary subject for combination 
	 */
	public static final int _XREQ_OPTIMALCUTS = 15 ;
	
	/** differential modeling and meta-modeling according to the Spela-approach  */
	public static final int _XREQ_SPELADIAGNOSTICS = 25 ;
	
	/** the list that holds the requests for extended results */
	ArrayList<ResultRequests> extendedResultRequests = new ArrayList<ResultRequests>(); 
	
	/** -3 = not possible, disabled ;
	 *   0 = not set, 
	 *   1 = single class (still multiple target groups for this class possible) TV has been set
	 *   2 = multi class 
	 */
	int targetMode  = 0 ;
	
	/**   */
	String activeTargetVariable = "" ;
	
	// min max of the interval [0|1][ min|max]
	/**   */
	double[][] 		TGdefinition ;
	/**   */
	String[] 		TGlabels;
	/** */
	boolean automaticTargetGroupDefinition = true;
	
	
	/** 
	 * value for the accepted error-cost-ratio, which controls the acceptance of nodes as part of a predictive model;
	 * requires a target variable
	 */
	double 			ecr = 0.2 ;   // actually, this could be an array too, since for various
							    // classes in an ordinally segmented TV, we could have different tolerances !!!
	/**   */
	double[] 		ECRs = null;  // optional for _TARGETMODE_MULTI 
	
	/**   */
	double preferredSensitivity = -1.0;

	/** the maximum fraction of records that are allowed to get selected, overrules ecr and preferredSensitivity */
	double capacityAsSelectedTotal = ecr/2.0 ; // approximate estimation
	
	/**   */
	boolean isEcrAdaptationAllowed = false;

	
	/**   */
	int maxTypeIcount  = -1;
	/**   */
	int maxTypeIIcount = -1;
	
	 
	transient ArrUtilities arrutil = new ArrUtilities ();

	private int targetGroupDefinitionLevel;

	
	
	// ========================================================================
	public ClassificationSettings(ModelingSettings modelingSettings){
		
		
	}
	// ========================================================================	


	
	
	
	public int getTargetMode() {
		return targetMode;
	}

	
	public void setTargetMode(int targetedModeling) {
		this.targetMode = targetedModeling;
	}


	public String getActiveTargetVariable() {
		return activeTargetVariable;
	}


	public void setActiveTargetVariable(String targetVariable) {
		activeTargetVariable = targetVariable;
	}


	public double[][] getTGdefinition() {
		return TGdefinition;
	}
	public void setTGdefinition(double[][] tGdefinition) {
		TGdefinition = tGdefinition;
	}
	
	public double[][] getTargetGroupDefinition() {
		return TGdefinition;
	}

	public boolean setTargetGroupDefinition(double[][] tGdefinition) {
		
		if ((tGdefinition==null) || (tGdefinition.length==0) || (tGdefinition[0].length!=2)){
			return false;
		}
		TGdefinition = tGdefinition ;
		return true;
	}
	
	public void setTargetGroupDefinitionAuto(boolean flag){
		
		if (targetMode == ClassificationSettings._TARGETMODE_SINGLE){
			return;
		}
		if (targetMode == ClassificationSettings._TARGETMODE_MULTI){
			TGdefinition = new double[0][0];
			automaticTargetGroupDefinition = flag;
		}
		// as soon as data are available, the target groups will be inferred ...
	}
	
	/**
	 * if there is more than 1 interval, the right side is excluded except for the left-most interval;
	 * the left border is always included in the interval
	 * 
	 * @param intervalBorders
	 * @return
	 */
	public boolean setTargetGroupDefinition(double[] intervalBorders){
		String[] labels = new String[intervalBorders.length+1] ;
		return setTargetGroupDefinition(intervalBorders, labels);
	}
	
	
	public boolean setTargetGroupDefinition( ArrayList<ValuePair> tvGroupIntervals){
		return setTargetGroupDefinition( tvGroupIntervals, new ArrayList<String>()) ;
	}
	
	public boolean setTargetGroupDefinition( ArrayList<ValuePair> tvGroupIntervals, 
			                                 ArrayList<String> tvGroupLabels){
		double[] intervalBorders ;
		ValuePair vp;
		int t = Math.max(2,2*tvGroupIntervals.size()) ;
		
		intervalBorders = new double[t];
		if (tvGroupIntervals.size()==0){
			intervalBorders[0] = 0.0;
			intervalBorders[1] = 0.5;
		}else{
			
			for (int i=0;i<tvGroupIntervals.size();i++){
				vp = tvGroupIntervals.get(i) ;
				intervalBorders[i*2]   = (Double) vp.getValue1() ;
				intervalBorders[i*2+1] = (Double) vp.getValue2() ;
			}
		}
		if ((tvGroupLabels.size()>0) && (tvGroupLabels.size()<=tvGroupIntervals.size())){
			TGlabels = arrutil.changeArrayStyle(tvGroupLabels, "");
		}else{
			
		}
		
		
		return setTargetGroupDefinition( intervalBorders);
	}
	
	
	
	
	public boolean setTargetGroupDefinition(double[] intervalBorders, String[] labels){
		
		if (targetMode == _TARGETMODE_SINGLE){
			// logico-performative mode mismatch, nothing will be done
			return false;
		}
		
		TGdefinition = new double[0][0];
		int ibCount = intervalBorders.length-1;
		double min,max ;
		String str;
		
		if ((intervalBorders==null) || ((intervalBorders.length<=1) && (intervalBorders[intervalBorders.length-1]<1.0))){
			automaticTargetGroupDefinition = true;
			return false;
		}
		
		automaticTargetGroupDefinition = false;
		
		 
		TGdefinition = new double[ibCount][2];
		TGlabels  = new String[ibCount] ;
		
		for (int i=0;i<ibCount;i++){
			min = intervalBorders[i];
			if (min<1.0){
				if (i<intervalBorders.length-1){
					max = intervalBorders[i + 1];
				}else{
					max = 1.0;
				}
				if (max==1.0)max=1.0000000001;
				
				TGdefinition[i][0] = min;
				TGdefinition[i][1] = max;
				
				if (i<labels.length){
					str = labels[i]; if ((str==null) || (str.length()==0)){str="target-group-"+(i+1);}
					TGlabels[i] = str;
				}else{
					TGlabels[i] = "target-group-"+i;
				}
			}
		}
		 
		return true;
	}
	
	public void setTargetGroupDefinitionExclusions( double[] targetExclusions){
		
	}
	
	/**
	 * both borders of the interval are included 
	 * 
	 * @param min
	 * @param max
	 */
	public void setSingleTargetGroupDefinition(double min, double max) {
		setSingleTargetGroupDefinition(min, max, "mono target") ;
	}
	public void setSingleTargetGroupDefinition(double min, double max, String label) {
		
		if (targetMode == _TARGETMODE_MULTI ){
			// nothing happens... mode mismatch
			return;
		}
		
		TGdefinition = new double[1][2];
		
		TGdefinition[0][0] = min;
		TGdefinition[0][1] = max;
		
		TGlabels = new String[1] ;
		TGlabels[0] = label;
		
	}
	
	public void addSingleTargetGroupDefinition(double min, double max, String label) {
		double[][] newTgDef;
		String[] newTgLabels;
		String tgLabel= label;
		boolean hb ;
		
		
		if ((TGdefinition==null) || (TGdefinition.length==0)){
			setSingleTargetGroupDefinition(min,max);
			return;
		}
		
		newTgDef = new double[TGdefinition.length+1][2];
		System.arraycopy(TGdefinition, 0, newTgDef,0, TGdefinition.length) ;
		
		newTgDef[newTgDef.length-1][0] = min;
		newTgDef[newTgDef.length-1][1] = max;
		
		TGdefinition = null;
		TGdefinition = newTgDef ;
		
		if (tgLabel==null) {
			tgLabel = "target <#>" ;
		}
		if (tgLabel.length()==0){
			tgLabel = "target <#>" ;
		}
		
		String str = arrutil.arr2text( TGlabels, ";");
		str = str.replaceAll("  ", " ").replace("; ", ";").replace(" ;", ";");
		hb = str.contains(label+";") || str.contains(";"+label);
		
		int p = arrutil.arrValuePos(TGlabels, label,0) ;
		if (hb){
			tgLabel = TGlabels[p]+" <#>" ; 
		}
		if ( (tgLabel.contains("<#>"))){
			int z= TGlabels.length;
			tgLabel = tgLabel.replace("<#>", ""+z);
		}
		
		newTgLabels = new String[TGlabels.length+1];
		System.arraycopy(TGlabels, 0, newTgLabels,0, TGlabels.length) ;
		
		newTgLabels[newTgLabels.length-1] = tgLabel ;
		TGlabels = null;
		TGlabels = newTgLabels;
		
		
	}
	
	public void addSingleTargetGroupDefinition(double min, double max) {
		addSingleTargetGroupDefinition(min,max,"target <#>");
	}




	public boolean isAutomaticTargetGroupDefinition() {
		return automaticTargetGroupDefinition;
	}
	public boolean getAutomaticTargetGroupDefinition() {
		return automaticTargetGroupDefinition;
	}

	public void setAutomaticTargetGroupDefinition(
			boolean automaticTargetGroupDefinition) {
		this.automaticTargetGroupDefinition = automaticTargetGroupDefinition;
	}



	public String[] getTGlabels() {
		return TGlabels;
	}


	public void setTGlabels(String[] tGlabels) {
		TGlabels = tGlabels;
	}
	public void setTGlabels(ArrayList<String> tglabels) {

		TGlabels  = arrutil.changeArrayStyle( tglabels, "");
	}

	/**
	 *  if the ECR is not met by the conditions of a node in a developed SOM, it will NOT be 
	 *  regarded as a part of the model;
	 *  the ECR is also regarded when checking the necessity for splitting nodes
	 *   	
	 * @param _ecr
	 */
	public void setErrorCostRatioRiskPreference(double _ecr) {
		ecr = _ecr;
		
	}
	public double getECR() {
		return ecr;
	}


	public void setECR(double _ecr) {
		ecr = _ecr;
	}


	public double[] getECRs() {
		return ECRs;
	}


	public void setECRs(double[] _ecrs) {
		ECRs = _ecrs;
	}


	public int getMaxTypeIcount() {
		return maxTypeIcount;
	}


	public void setMaxTypeIcount(int maxTypeIcount) {
		this.maxTypeIcount = maxTypeIcount;
	}


	public int getMaxTypeIIcount() {
		return maxTypeIIcount;
	}


	public void setMaxTypeIIcount(int maxTypeIIcount) {
		this.maxTypeIIcount = maxTypeIIcount;
	}

  
	public boolean isExtendedResultsRequested() {
		boolean rB=false;
		
		rB = extendedResultRequests.size()>0;
		
		return rB;
	}




	public boolean addExtendedResultRequest(int extResultIdentifier, double... params) {
		boolean rB=false;
		double[] parameters ;
		
		
		ResultRequests rrq = new ResultRequests();
		
		if ((params!=null) && (params.length>0)){
			
			parameters = new double[params.length] ;
			int z=0;
			for ( double pv : params ){
				parameters[z] = pv ;
				z++;
			}
		}else{
			parameters = new double[0] ; // just preventing a null
		}
		
		rrq.setIdentifier( extResultIdentifier ) ;
		rrq.setParameters( parameters );
		
		extendedResultRequests.add( rrq );
		
		rB=true;
		 
		return rB;
	}



	public ArrayList<ResultRequests> getExtendedResultRequests() {
		return extendedResultRequests;
	}

	public void setExtendedResultRequests(
			ArrayList<ResultRequests> extendedResultRequests) {
		this.extendedResultRequests = extendedResultRequests;
	}



	public double getEcr() {
		return ecr;
	}

	public void setEcr(double ecr) {
		this.ecr = ecr;
	}


	public void setCapacityAsSelectedTotal(double threshold) {
		 
		capacityAsSelectedTotal = threshold;
	}
	
	public double getCapacityAsSelectedTotal() {
		return capacityAsSelectedTotal;
	}





	public void setPreferredSensitivity(double value) {
		preferredSensitivity = value ;
	}

	public double getPreferredSensitivity() {
		return preferredSensitivity;
	}

 
	public void setEcrAdaptationAllowed(boolean flag) {
		isEcrAdaptationAllowed = flag ;
	}
 
	public boolean isEcrAdaptationAllowed() {
		return isEcrAdaptationAllowed;
	}
	public boolean getEcrAdaptationAllowed() {
		return isEcrAdaptationAllowed;
	}




	public XMLBuilder exportPropertiesAsXBuilder(SettingsTransporter settingsTransporter) {

		return settingsTransporter.exportPropertiesAsXBuilder(this) ;
	}




	/**
	 * 
	 * @param defLevel raw=1, or norm=2
	 */
	public void setTargetGroupDefinitionLevel(int defLevel) {
		targetGroupDefinitionLevel = defLevel;
	}





	public int getTargetGroupDefinitionLevel() {
		return targetGroupDefinitionLevel;
	}



 

	
	
}
