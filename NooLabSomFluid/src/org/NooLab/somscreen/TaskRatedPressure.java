package org.NooLab.somscreen;

import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.properties.OptimizerSettings;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.strings.ArrUtilities;
import org.apache.commons.collections.CollectionUtils;


// ArrayList<EvoTaskOfVariable> EvoBasics.evoTasks

public class TaskRatedPressure {


	SomDataObject somData ;
	
	SomFluidProperties sfProperties ;
	OptimizerSettings optimizerSettings; 

	EvoBasics evoBasics;
	EvoMetrices evoMetrices;
	
	Variables variables;
	
	int activeVarCount ;
	int trpThreshold = 5 ;
	
	private ArrayList<String> nonCommonVarList;
	ArrayList<String> urgingVariables = new ArrayList<String> ();
	ArrayList<Integer> urgingVariablesIndexes = new ArrayList<Integer>();
	
	ArrayList<Integer> preferredVariables = new ArrayList<Integer>();
	private ArrayList<Integer> suggestedIndexes = new ArrayList<Integer>();
	
	
	// ========================================================================
	public TaskRatedPressure( SomHostIntf somHost, EvoMetrices metrices , EvoBasics evobasics) {
		
		somData = somHost.getSomDataObj() ;
		sfProperties = somHost.getSfProperties();
		optimizerSettings = sfProperties.getModelingSettings().getOptimizerSettings() ;
		  
		
		evoMetrices = metrices;
		
		
		variables = somData.getVariables();
		
		nonCommonVarList = variables.collectAllNonCommons( sfProperties.getAbsoluteFieldExclusions() );
		int ncn = nonCommonVarList.size() ;
		activeVarCount = variables.size() - ncn ;
		
	}
	// ========================================================================


	public void setpreferrables(ArrayList<Integer> preferredVars) {

		preferredVariables = new ArrayList<Integer>(preferredVars) ;
	}

	/**
	 * 
	 * @param proposedSelection the list of indices that could be extended; </br></br>
	 * @param modeParams  0 = random by coin; 1 = enforced ; </br>optional second parameter as condition [0,1],n = if size<=n; </br>  
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> considerUrgingVariables(ArrayList<Integer> proposedSelection, int ...modeParams) {
		ArrayList<Integer> proposedVarIndexes,putativeProposal   ;
		int[] mode = new int[2];
		boolean consider=true, isKnownMetric; 
		int ix;
		
		
		if (urgingVariables.size()==0){
			return proposedSelection;
		}
		proposedVarIndexes = ArrUtilities.copyList(proposedSelection) ;
		
		mode[1] = 99999;
		if (modeParams!=null){
			if (modeParams.length>0) mode[0] = modeParams[0];
			if (modeParams.length>1) mode[1] = modeParams[1];
			
		}else{
			return proposedSelection;
		}
		
		if (proposedSelection.size()<mode[1]){
			if (mode[0]==0){
				consider =  evoMetrices.jrandom.nextDouble()>0.5;
			}
			if (mode[0]==1){
				consider=true;
			}
			if (consider){ // respect already calculated metrices
				
				putativeProposal = ArrUtilities.copyList(proposedSelection) ;
				// we have to check one after another in order to avoid proposing a metric 
				// that we have already been calculating previously; CollectionUtils.union() is not quite possible
				for (int i=0;i<urgingVariablesIndexes.size();i++){
					
					ix = urgingVariablesIndexes.get(i) ;
					if (putativeProposal.indexOf(ix)<0){
						putativeProposal.add(ix);
					}
					else{ continue;}
					
					Collections.sort( putativeProposal );
					isKnownMetric = !evoMetrices.proposedMetricIsUnexplored( putativeProposal );
					
					if (isKnownMetric){
						// remove it right again
						putativeProposal.remove( putativeProposal.size()-1);
					}
				}
				proposedVarIndexes = putativeProposal;
				// (ArrayList<Integer>) CollectionUtils.union( urgingVariablesIndexes, proposedVarIndexes );
			}
		}
		
		Collections.sort( proposedVarIndexes );
		
		return proposedVarIndexes;
	}


	// determineEvolutionaryDueCounts();
	@SuppressWarnings("unchecked")
	public boolean determineUrgingVariables(){

		boolean rB=false;
		String varLabel="";
		double tsc=0.0,v,cmax=-9999.0;
		double[] c = new double[0];
		int ix;
		ArrayList<Double> pressures = new ArrayList<Double>();
		ArrayList<String> activeVariables ;
		Variable variable ;
		EvoTaskOfVariable evt ;
		IndexedDistances ixDists = new IndexedDistances();
		IndexDistance ixd ;
		
		try{
			evoBasics = evoMetrices.evoBasics ;
			
			int[] ecs = (int[]) ArrUtilities.changeArraystyle(evoBasics.evolutionaryCounts) ;
			int ecsum = ArrUtilities.arraySum(ecs) ;
			int ecmax = ArrUtilities.arraymax(ecs) ;
			double avgEc = (double)ecsum / (double)activeVarCount ;
			
			urgingVariablesIndexes.clear() ;
			 
			
			if ((ecmax< trpThreshold) || ((ecs.length>50) && (ecsum<150)) || 
				((ecs.length>25) && ((ecmax < 10-Math.log(ecs.length))))){
				return false;
			}
			
			variables = somData.getVariables() ;
			
			activeVariables = (ArrayList<String>) CollectionUtils.subtract(somData.getNormalizedDataTable().getColumnHeaders(), nonCommonVarList) ;
						                      //  contains a small bug: first position is null, despite sources are well-defined   
			activeVariables = ArrUtilities.clean(activeVariables);
			 
			
			for (int i=0; i< activeVariables.size();i++){
			
				pressures.add(0.0) ;
				varLabel = activeVariables.get(i) ;
				
				if (varLabel==null){
					continue;
				}
				variable = variables.getItemByLabel(varLabel) ;
				
				if ((variable.isID()) || variable.isIndexcandidate() || variable.isTV() || variable.isTVcandidate() ){
					continue ;
				}
				
				ix = variables.getIndexByLabel(varLabel) ;
				double pv = 1.0 - (double)evoBasics.evolutionaryCounts.get(ix)/ecmax;
				
				// we have to weight by evoweight: lower weights reduce the pressure, higher weights increase the pressure
				//pressures.set(i, pv );
				
				
				double ew = evoBasics.evolutionaryWeights.get(ix) ;
				ew = (0.3+ Math.sqrt(3*Math.log10(0.3 + Math.sqrt(11* (0.1+ew)))))/1.08 ; // scales [0..1] to [0.86 .. 1.5], with 0.5 -> 1.36
				pv = pv * ew ;
				
				ixd = new IndexDistance(i,pv,varLabel); 
				ixDists.add(ixd);
				
			} // i->
			
			ixDists.sort(-1);
			for (int i=0; i< ixDists.size();i++){
				
				ixDists.getItem(i).setSecindex(i) ;
			}
			
			int rankThreshold =  (int) ((double)ixDists.size()*0.2) ;
			if (rankThreshold<=2)rankThreshold=2; 
			
			  
			urgingVariables.clear();
			for (int i=0; i< ixDists.size();i++){
				if (i<rankThreshold)
				urgingVariables.add(ixDists.getItem(i).getGuidStr()) ;
			}
			ix=0;
			for (int i=0; i< urgingVariables.size();i++){
				varLabel = urgingVariables.get(i);
				ix = variables.getIndexByLabel(varLabel) ;
				 
				urgingVariablesIndexes.add(ix) ;
			}
			
			rB = urgingVariablesIndexes.size()>0;
			
		}catch(Exception e){
			
		}
		ix=0;
		return rB;
	}
	
	
	@SuppressWarnings("unchecked")
	private void _obs_determineUrgingVariables(){

		double tsc=0.0,v,cmax=-9999.0;
		double[] c = new double[0];
		int ix;
		
		ArrayList<String> activeVariables ;
		Variable variable ;
		EvoTaskOfVariable evt ;
		IndexedDistances ixDists = new IndexedDistances();
		IndexDistance ixd ;
		
		try{
			
			
			int[] ecs = (int[]) ArrUtilities.changeArraystyle(evoBasics.evolutionaryCounts) ;
			int ecsum = ArrUtilities.arraySum(ecs) ;
			int ecmax = ArrUtilities.arraymax(ecs) ;
			double avgEc = (double)ecsum / (double)activeVarCount ;
			
			
			if ((ecmax<12) || (avgEc<6) ){
				return;
			}
			
			variables = somData.getVariables() ;
			
			activeVariables = (ArrayList<String>) CollectionUtils.disjunction( somData.getNormalizedDataTable().getColumnHeaders(), nonCommonVarList) ;
			
			
			for (int i=0; i< activeVariables.size();i++){
			
				String varLabel = activeVariables.get(i) ;
				if (varLabel==null){
					continue;
				}
				variable = variables.getItemByLabel(varLabel) ;
				
				if ((variable.isID()) || variable.isIndexcandidate() || variable.isTV() || variable.isTVcandidate() ){
					continue ;
				}
				if (variable.getValueScaleNiveau() >= Variable._VARIABLE_SCALE_NOMINAL)
								for (int k=0;k<c.length;k++)c[k]=0.0;
				
				evt = evoBasics.getEvoTasks().getEvoTaskItemByLabel( variable.getLabel());
				
								if (c.length==0){
									c = new double[ evt.initialevotaskWeights.length];
								}
				
								// 
				c[0] = (((double)(evt.meets))        * evt.evotaskWeights[0] )/(1.0) ; 
				c[1] = ((double)evt.singularAdd  	 * evt.evotaskWeights[1] )/(1.0) ;
				c[2] = ((double)evt.singularRemoval  * evt.evotaskWeights[2] )/(1.0) ;
				c[3] = ((double)evt.collinearPlus    * evt.evotaskWeights[3] )/(1.0) ;
				c[4] = ((double)evt.collinearNeg     * evt.evotaskWeights[4] )/(1.0) ;
				//tsc = evt. ;
				
				v = ArrUtilities.arraySum(c);
				if (v>=0){
					ixd = new IndexDistance( i, v, variable.getLabel() );
					ixDists.add(ixd) ;
					if (cmax<v){
						cmax = v;
					}
				}
				 
			} // i -> all variables
			cmax = Math.max(cmax, 1.0) ;
			
			for (int i=0;i<ixDists.size();i++){
				v = (1.0 + cmax - ixDists.getItem(i).getDistance())/(1.0+cmax) ;
				ixDists.getItem(i).setDistance(v);
			}
			
			ixDists.sort(-1);
			// preferredVariables
			
			boolean isect=true;
			
			
			suggestedIndexes = new ArrayList<Integer>() ;
			
			while (suggestedIndexes.size()==0){
				

				for (int i=0;i<ixDists.size();i++){
					ixd = ixDists.getItem(i) ;
					if ((preferredVariables.size()>0) && (isect)){
						ix = ixd.getIndex() ;
						if (preferredVariables.indexOf(ix)>=0){
							suggestedIndexes.add( ix ) ;
						}
						
					}else{
						suggestedIndexes.add( ixd.getIndex() ) ;
					}
					if ((suggestedIndexes.size()>10) || (suggestedIndexes.size()>ixDists.size()*0.62)){
						break;
					}
				}
				isect = false;
			} // any item collected ?
			
			
			
		}catch(Exception e){
			
		}
		
	}

	public ArrayList<Integer> getSuggestions( int topN ) { 
		
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		if (topN>suggestedIndexes.size()-1){
			topN=suggestedIndexes.size()-1 ;
		}
		
		for (int i=0;i<topN;i++){
			indexes.add(suggestedIndexes.get(i));
		}
		
		return indexes ;
	}


	public ArrayList<String> getUrgingVariables() {
		return urgingVariables;
	}


	public ArrayList<Integer> getPreferredVariables() {
		return preferredVariables;
	}


	public void setPreferredVariables(ArrayList<Integer> preferredVariables) {
		this.preferredVariables = preferredVariables;
	}

	
	
}
