package org.NooLab.somscreen;

import java.util.ArrayList;

import org.NooLab.somfluid.components.*;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.properties.OptimizerSettings;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;


// ArrayList<EvoTaskOfVariable> EvoBasics.evoTasks

public class TaskRatedPressure {


	SomDataObject somData ;
	
	OptimizerSettings optimizerSettings; 

	EvoBasics evoBasics;
	
	ArrayList<Integer> preferredVariables = new ArrayList<Integer>();
	ArrayList<Integer> suggestedIndexes = new ArrayList<Integer>();
	
	// ========================================================================
	public TaskRatedPressure( SomDataObject somdata, OptimizerSettings optiSet, EvoBasics evobasics) {
		somData = somdata;
		optimizerSettings = optiSet ;
		evoBasics = evobasics ;	
	}
	// ========================================================================


	public void setpreferrables(ArrayList<Integer> preferredVars) {

		preferredVariables = new ArrayList<Integer>(preferredVars) ;
	}

	public void determineUrgingVariables(){

		double tsc=0.0,v,cmax=-9999.0;
		double[] c = new double[0];
		int ix;
		
		Variables variables;
		Variable variable ;
		EvoTaskOfVariable evt ;
		IndexedDistances ixDists = new IndexedDistances();
		IndexDistance ixd ;
		
		try{
			
			
			
			
			variables = somData.getVariables() ;
			
			for (int i=0; i< variables.size();i++){
				
				variable = variables.getItem(i) ;
				
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
				
				v = ArrUtilities.arraysum(c);
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

	
	
}
