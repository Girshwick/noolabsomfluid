package org.NooLab.somscreen;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.strings.ArrUtilities;
import org.apache.commons.collections.CollectionUtils;

public class EvoBasics implements Serializable{

	private static final long serialVersionUID = 5254385568805654924L;

	public static final int _QUANT_LOW = 1;
	public static final int _QUANT_TOP = 2;
	public static final int _QUANT_MED = 3;
	public static final int _QUANT_CTR = 5;

	
	
	
	ArrayList<Double> evolutionaryWeights = new ArrayList<Double>() ; 
	ArrayList<Integer> evolutionaryCounts = new ArrayList<Integer>() ; 
	
	ArrayList<String> knownVariables = new ArrayList<String>(); 
	
	EvoTasks evoTasks;

	int bestModelHistoryIndex;
	
	
	
	
	// ========================================================================
	public EvoBasics(){
		
	}
	
	public EvoBasics(EvoBasics eb) {
		evolutionaryWeights.addAll( eb.evolutionaryWeights) ; 
		evolutionaryCounts.addAll( eb.evolutionaryCounts) ; 
		knownVariables.addAll( eb.knownVariables) ;
		bestModelHistoryIndex = eb.bestModelHistoryIndex;
	}
	
	private void initializeEvoTaskList() {
		
		evoTasks = new EvoTasks(knownVariables) ;
	}
	// ========================================================================

	
	public int incEvolutionaryCount(int index) {
		int ec=1 ;
		try{

			if (index>evolutionaryCounts.size()){
				evolutionaryCounts.add(0) ;
			}	
			if (evolutionaryCounts.size() > this.evolutionaryWeights.size()){
				evolutionaryWeights.add(0.5) ;
			}
			
			ec = evolutionaryCounts.get(index) ;
			ec++;
			evolutionaryCounts.set(index, ec);

			
		}catch(Exception e){
			ec=-1;
		}
		
		return ec; 
	}
	
	@SuppressWarnings("unchecked")
	public EvoMetrices integrateEvoMetricHistories( EvoMetrices ems1, EvoMetrices ems2, int loopcount) {
	
		int k;
		double _score = -1.0 ;
		EvoBasics eb1,eb2 ;
		ArrayList<Double> ews;
		ArrayList<Integer> ecs; 
		
		
		if ((ems2==null) || (ems2.getEvmItems().size()==0)){
			return ems1;
		}
		if ((ems2.getBestResult()!=null) && (ems2.getBestResult().getSqData()!=null) ){
			_score = ems2.getBestResult().getSqData().getScore();
		}
		ems2.getBestResult().setActualScore( _score );
		if ((ems1!=null) && (ems1.getEvmItems().size()>0)){
			k = ems1.getEvmItems().size() ;
		}else{
			k=0;
		}
		for (int i=0;i<ems2.getEvmItems().size();i++){
			ems2.getEvmItems().get(i).setLoopCount(loopcount+1) ;
			ems2.getEvmItems().get(i).setStep( k + i + 1) ;
		}
		
		if ((ems1==null) ){
			ems1 = new EvoMetrices(ems2, false); 
			
			ems1.setCurrentBaseEvoMetrik( ems2.getCurrentBaseMetric() );
			ems1.addEvmItems( ems2.getEvmItems() );
			
			ems1.getProposedVariableIndexes().clear();
			ems1.getProposedVariableIndexes().addAll( ems2.getBestResult().getVarIndexes() );
			
			return ems1;
		}
	
		// just the metrices as indices of variables (list of list)
		// removes all double entries
		ems1.setExploredMetrices( (ArrayList<ArrayList<Integer>>) CollectionUtils.union( ems1.getExploredMetrices(), ems2.getExploredMetrices()) );
	
		// history of compressed results , may contain double entries from different runs, TODO need to be removed...
		ems1.addEvmItems( ems2.getEvmItems() );
		if (ems1.bestResult==null){
			ems1.bestResult = new EvoMetrik(ems2.bestResult) ;
		}
		
		// integrating evo weights and counts
		eb1 = ems1.getEvoBasics() ;
		eb2 = ems2.getEvoBasics() ;
		
		int evosz2 = eb2.getEvolutionaryCounts().size();
		if (evosz2==0){
			return ems1;
		}
		int ec,ec1, ec2;
		double ew1,ew2, ew;
		
		// first integrating positions into em2 (which is always longer by ADDED positions) based on counts of em1 
		for (int i=0;i<eb1.getEvolutionaryCounts().size();i++){
			ec1 = eb1.getEvolutionaryCounts().get(i) ;
			ec2 = eb2.getEvolutionaryCounts().get(i) ;
			ew1 = eb1.getEvolutionaryWeights().get(i) ;
			ew2 = eb2.getEvolutionaryWeights().get(i) ;
			
			ew = (((double)ec1)*ew1) + (((double)ec2)*ew2);
			ec = ec1+ec2;
			if (ec>0){
				ew = ew/((double)ec) ;
			}else{
				ew=0.0;
			}
			eb2.getEvolutionaryCounts().set(i, ec);
			eb2.getEvolutionaryWeights().set(i, ew) ;
			
		}
		
		ews = new ArrayList<Double>(eb2.getEvolutionaryWeights());
		ecs = new ArrayList<Integer>(eb2.getEvolutionaryCounts());
		
		// then changing references
		eb1.setEvolutionaryCounts(ecs);
		eb1.setEvolutionaryWeights(ews);
		
		
		// is the second one (last) better than the previous (first) one ? 
		if ((ems1.getBestResult()==null) || ( ems2.getBestResult().getActualScore() < ems1.getBestResult().getActualScore())){
			// creates a new instance by cloning 
if ( ems2.getBestResult()==null){
	k=0;
}
			ems1.setBestResult( ems2.getBestResult() ) ;
			ems1.setCurrentBaseEvoMetrik( ems2.getBestResult() );
			ems1.getProposedVariableIndexes().clear();
			ems1.getProposedVariableIndexes().addAll( ems2.getBestResult().getVarIndexes() );
		}
		
		ems1.sort( EvoMetrices._SORT_SCORE,-1 ) ;
		
		// in evoBasics, adjust bestModelHistoryIndex, acc. to the respective index values in evmItems<>
		return ems1;
	}

	public void adjustEvoVectorsForChanges(int lengthDiff, ArrayList<String> addedLabels) {
		int n;
		
		for (int i=0;i<addedLabels.size();i++){
			knownVariables.add( addedLabels.get(i) );
			
			n = knownVariables.size() ;
			if (n>evolutionaryCounts.size()){
				evolutionaryCounts.add(0) ;
			}
			if (n>evolutionaryWeights.size()){
				evolutionaryWeights.add(0.5) ;
			}
		}

	}

	public double getAverageCount() {
		double avgC = -1.0 ;
		int ecs=0;
		
		if (evolutionaryCounts.size()>0){
			ecs = ArrUtilities.arraySum(evolutionaryCounts);
			avgC = (double)ecs/((double)evolutionaryCounts.size()) ;
		}
		return avgC;
	}

	public void setSelectionparameters(){
		
	}
	
	public ArrayList<Integer> getQuantilByWeight(double quantile, double loWeightLimit, double hiWeightLimit, ArrayList<String> definitelyExcluded ) {
		
		return getQuantilByWeight( quantile, loWeightLimit, hiWeightLimit, definitelyExcluded, false);
	}
	
	public ArrayList<Integer> getQuantilByWeight( double quantile, 
												  double loWeightLimit, double hiWeightLimit, 
												  ArrayList<String> definitelyExcluded,
												  boolean allowExtension) {
		
		IndexDistance ixd;
		IndexedDistances ixds = new IndexedDistances();
		ArrayList<Integer> selectedIndexes = new ArrayList<Integer>();
		double ew, minew=1.01, maxew=-0.1 ;
		
		
		if (loWeightLimit<0)loWeightLimit=0.0;
		if (hiWeightLimit>1.0)hiWeightLimit=1.0;
		if (hiWeightLimit < loWeightLimit)hiWeightLimit = loWeightLimit+0.000001;
		
		for (int i=0;i<this.evolutionaryWeights.size();i++){
			ew = evolutionaryWeights.get(i);
			if (minew>ew)minew=ew ;
			if (maxew<ew)maxew=ew ;
				
			if ((ew>=loWeightLimit) && (ew<= hiWeightLimit)){
				String varLabel = this.knownVariables.get(i);
				ixd = new IndexDistance(i,ew,varLabel);
				
				if (definitelyExcluded.indexOf(varLabel)<0){
					ixds.add(ixd);
				}
			}
		} // i->
		
		if ((allowExtension) && (ixds.size()==0) && (maxew<loWeightLimit)){
			selectedIndexes = getQuantilByWeight( quantile, loWeightLimit*0.9, 1.0, definitelyExcluded,false);	
		} else {

			ixds.sort(-1);
			int qn = (int)Math.round( (double)ixds.size()*quantile);
			qn = Math.min( Math.max(qn,3), ixds.size());
			for (int i = 0; i < qn; i++) {
				selectedIndexes.add(ixds.getItem(i).getIndex());
			}

		}
		return selectedIndexes;
	}
	
	public void getQuantilByWeight(double quantile ) {
		getQuantilByWeight(quantile, _QUANT_TOP);
	}

	public void getQuantilByWeight(double quantile , int mode) {
		
	}
	public void getQuantilByWeight(double quantile, double center ) {
		
	}

	
	
	public ArrayList<Double> getEvolutionaryWeights() {
		return evolutionaryWeights;
	}

	@SuppressWarnings("unchecked")
	public void setEvolutionaryWeights(ArrayList<Double> weights) {
		
		evolutionaryWeights = new ArrayList<Double>(weights);
		if (evolutionaryCounts.size() != evolutionaryWeights.size()){
			evolutionaryCounts = ArrUtilities.createCollection( evolutionaryWeights.size(), (int)0) ;
		}
	}

	public ArrayList<Integer> getEvolutionaryCounts() {
		return evolutionaryCounts;
	}

	public void setEvolutionaryCounts(ArrayList<Integer> counts) {
		evolutionaryCounts = counts;
	}

	public ArrayList<String> getKnownVariables() {
		return knownVariables;
	}

	public void setKnownVariables(ArrayList<String> variablesStr) {
		
		knownVariables.clear();
		
		if ((variablesStr!=null) && (variablesStr.size()>0)){
			
			knownVariables.addAll( variablesStr ) ;
			
			initializeEvoTaskList();
			
		}
	}
	
	
	/**
	 * @return the bestModelHistoryIndex
	 */
	public int getBestModelHistoryIndex() {
		return bestModelHistoryIndex;
	}
	/**
	 * @param bestModelHistoryIndex the bestModelHistoryIndex to set
	 */
	public void setBestModelHistoryIndex(int bestModelHistoryIndex) {
		this.bestModelHistoryIndex = bestModelHistoryIndex;
	}
	public EvoTasks getEvoTasks() {
		return evoTasks;
	}
	 
	public void setEvoTasks( EvoTasks evt) {
		this.evoTasks = evt;
	}
	
	
	
}
