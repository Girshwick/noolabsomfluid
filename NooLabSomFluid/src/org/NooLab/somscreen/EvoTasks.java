package org.NooLab.somscreen;

import java.util.ArrayList;



public class EvoTasks {
	
	ArrayList<EvoTaskOfVariable> taskVarItems = new ArrayList<EvoTaskOfVariable>();
	ArrayList<String> varLabels = new ArrayList<String>();
	
	int meetsMax = 0;
	int singularAddMax = 0;
	int singularRemovalMax = 0;
	int collinearPlusMax = 0;
	int collinearNegMax = 0;
	
	// ========================================================================
	public EvoTasks(ArrayList<String> varlabels) {
		 
		varLabels = new ArrayList<String>(varlabels);	
		
		init();
	}

	private void init(){
		int kvn;
		EvoTaskOfVariable etv;
		
		kvn = varLabels.size() ;
		
		for (int i=0;i<kvn;i++){
			
			etv = new EvoTaskOfVariable( varLabels.get(i) );
			taskVarItems.add( etv );

			etv.variableCount = kvn;  
		}
		
	}


	public EvoTaskOfVariable getEvoTaskItemByLabel( String varLabel){
		// should be parallel to string-based list
		int p;
		EvoTaskOfVariable evt=null;
		
		p = varLabels.indexOf(varLabel) ; 
		if (p>=0){
			evt = taskVarItems.get(p) ;
			if (evt.varLabel.contentEquals(varLabel)==false){
				evt=null;
			}
			
		}
		if (evt==null){
			for (int i=0;i<taskVarItems.size();i++){
				if (taskVarItems.get(i).varLabel.contentEquals(varLabel)){
					evt = taskVarItems.get(i);
					break ;
				}
			}
		}
		return evt;
	}
	
	
	/**
	 * this is called BEFORE the metric is changed, best immediately
	 * before modeling, or "While" modeling (in parallel mode);
	 * 
	 * it is NEVER being called while a change campaign is running
	 * 
	 * -> it is called for each single step of SomScreening
	 * 
	 * determining min and max for any of the fields,
	 * substracting the min per field in all variables,
	 * 
	 * calculating the ratio
	 * 
	 * ... that's all quite adhoc, needs a different structure, sth like a map...
	 *     for addressing the fields and the values
	 */
	public void renormalizeParameters(){
		
		int k;
		EvoTaskOfVariable evt ;
		int[] fmin = new int[5];
		int[] fmax = new int[5];
		
		for (int i=0;i<5;i++){
			fmin[i] =  999999;
			fmax[i] = -999999;
		}
		// ............................
		k=0;
		
		for (int i=0;i<taskVarItems.size();i++){
			
			evt = taskVarItems.get(i) ;
			 
			compField( evt.meets, 0, fmin, fmax );
			compField( evt.singularAdd, 1, fmin, fmax );
			compField( evt.singularRemoval, 2, fmin, fmax );
			compField( evt.collinearPlus, 3, fmin, fmax );
			compField( evt.collinearNeg, 4, fmin, fmax );
			
		} // i-> all taskVarItems
		
		k=0;
		for (int i=0;i<taskVarItems.size();i++){
			
			evt = taskVarItems.get(i) ;
			
			evt.meets           = evt.meets - fmin[0] ;           fmax[0] = fmax[0] - fmin[0] ;  
			evt.singularAdd     = evt.singularAdd - fmin[1] ;     fmax[1] = fmax[1] - fmin[1] ;  
			evt.singularRemoval = evt.singularRemoval - fmin[2] ; fmax[2] = fmax[2] - fmin[2] ;  
			evt.collinearPlus   = evt.collinearPlus - fmin[3] ;   fmax[3] = fmax[3] - fmin[3] ;  
			evt.collinearNeg   = evt.collinearNeg - fmin[4] ;     fmax[4] = fmax[4] - fmin[4] ; 
			
			evt.meetsRatio           = evt.meets/((double)fmax[0]+1.0);
			evt.singularAddRatio     = evt.singularAdd/((double)fmax[1]+1.0);
			evt.singularRemovalRatio = evt.singularRemoval/((double)fmax[2]+1.0);
			evt.collinearPlusRatio   = evt.collinearPlus/((double)fmax[3]+1.0);
			evt.collinearNegRatio    = evt.collinearNeg/((double)fmax[4]+1.0);
			
		} // i->
		
		
	}
	
	private void compField( int value, int dim, int[] fmin, int[] fmax ){
		
		if ( fmin[dim] > value)fmin[dim] = value;
		if ( fmax[dim] < value)fmax[dim] = value;
		
	}
	
	
	public void updateEvoTaskItem( String varLabel, int action  ){
		
		EvoTaskOfVariable varItem ;
		
		
		varItem = getEvoTaskItemByLabel(varLabel);
				  if (varItem==null){
					  return;
				  }
		
		varItem.meets++;
		
        if (action>0){
        	if (action==1){
				varItem.singularAdd++;
        	}
        	if (action==2){
        		varItem.collinearPlus++ ;
        	}
        }

        if (action<0){
        	if (action== -1){
				varItem.singularRemoval++;
        	}
        	if (action== -2){
        		varItem.collinearNeg++;
        	}
        }
		
	}

	// find the items with maximum pressure
	public ArrayList<Integer> getACandidates(int n) {
		 
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		for (int i=0;i<taskVarItems.size();i++){
			
		}
		
		
		return indexes;
	}

	public ArrayList<Integer> getRCandidates(int n) {
		 
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		
		return indexes;
	}
 
	
	
	
}
