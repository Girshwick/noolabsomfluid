package org.NooLab.somfluid.components.variables;

import java.util.ArrayList;

import org.NooLab.somfluid.components.ModelOptimizer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;




public class VariableSubsets {
 
		
		
		SomDataObject somData;
		ModelOptimizer optimizerInstance; 
		
		ArrayList<ArrayList<Integer>> subsets = new ArrayList<ArrayList<Integer>>();
		
		// ====================================================================
		public VariableSubsets( ModelOptimizer modopti){
			optimizerInstance = modopti;
			somData = optimizerInstance.getSomDataObj() ;
			
		}
		// ====================================================================
		
		
		
		
		public int size() {
			// 
			return subsets.size();
		}

		
		public void setSharedSet(ArrayList<String> currVariableSelection) {
			 
			
		}
		
		
		
		public void prepare(int count){
			
			int nparts=1, nvtotal, nvwhite, nvblack,nvtv=1, nvix=1, availableVarsCount;
			Variables vars;
			String label;
			
			somData = optimizerInstance.getSomDataObj() ;
			vars = somData.getVariables();
			
			nvtotal = vars.size() ;
			nvwhite = vars.getWhitelistLabels().size();
			nvblack = vars.getBlackList().size() ;
			nvtv =   vars.getTargetedVariables().size() ;
			nvix =   vars.getAllIndexVariables().size() ;
			
			// subtract index var, target vars, whitelist, blacklist
			availableVarsCount = nvtotal - nvwhite - nvblack - nvtv - nvix ;
			
			
			
			if (count>1){
				nparts = (int) ((double)availableVarsCount/((double)count))*availableVarsCount;	
			}else{
				nparts =1;
			}
			
			ArrayList<Integer> subset ;
			subset = new ArrayList<Integer>();
			int sizeOfSubsets = (int)Math.round( (double)availableVarsCount/(double)nparts );
				
			int allocated=0;
			int setix=0;
			for (int i=0;i<vars.size();i++){
				
				double fract = (double)allocated/(double)sizeOfSubsets ;
				if((i>0) && (fract==0.0) && (setix<count)){
					
					subset = new ArrayList<Integer>();
				}
				
				
				Variable v = vars.getItem(i) ;
				label = v.getLabel() ;
				 
				
				if (variableIsAllocatable(vars,i)){
					subset.add(i) ;
				}
				
				
			} // i-> all parts
			
		}
		
		private boolean variableIsAllocatable( Variables variables, int index ){
			boolean rB=true;
			Variable variable;
			
			variable = variables.getItem(index) ;
			
			if (rB){
				rB = (variable.isID()==false) && (variable.isIndexcandidate()==false);
			}
			if (rB){
				rB = (variable.isTV()==false) && (variable.isTVcandidate()==false);
			}
			if (rB){
				rB = variable.isTVcandidate()==false;
			}
			if (rB){
				rB = variables.getBlackList().contains(variable)==false;
			}
			if (rB){
				rB = variables.getAbsoluteFieldExclusions().contains(variable)==false;
			}
			
			return rB;
		}
		
		public ArrayList<Integer> getSubset(int index){
			ArrayList<Integer> subset = null;
			
			return subset;
		}
		
	} // inner class VariableSubsets

