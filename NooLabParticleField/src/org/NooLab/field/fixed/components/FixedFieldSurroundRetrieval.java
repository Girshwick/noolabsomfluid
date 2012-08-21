package org.NooLab.field.fixed.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.NooLab.field.fixed.FixedField;
import org.NooLab.field.repulsive.components.ParticleGrid;
import org.NooLab.field.repulsive.components.data.RetrievalParamSet;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.field.repulsive.intf.SurroundRetrievalObserverIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;

public class FixedFieldSurroundRetrieval {

	public static final int _TASK_PARTICLE_RX = 1;
	public static final int _TASK_PARTICLE    = 2;
	
	public static final int _TASK_SURROUND_C  = 4; 
	public static final int _TASK_SURROUND_X  = 5; 

	public static final int _TASK_SURROUND_MST    = 10;
	public static final int _TASK_SURROUND_CXHULL = 11;


	SurroundRetrievalObserverIntf  srObserver;
	
	FixedField fixedField;
	FixedFieldParticlesIntf particles;
	
	// ParticleGrid particleGrid;
	GridSubstrate particleGrid ;
	
	
	Map<String,Object> resultMap = new HashMap<String,Object>(); 
	ArrayList<RetrievalParamSet> paramSets = new ArrayList<RetrievalParamSet>(); 
	
	SurroundResults results =null;
	RetrievalParamSet p=null ;
	
	
	int surroundN  = 8;
	int selectMode = 1;
	boolean autoselect = false ;
	private PrintLog out;
	
	// ========================================================================
	public FixedFieldSurroundRetrieval(FixedField fixedfield) {
		fixedField = fixedfield;
		
		particles = (FixedFieldParticlesIntf) fixedField.getParticles() ;
		particleGrid = null;
		
		particleGrid = fixedField.getGrid() ;
		
		out = fixedField.getOut() ;
	}
	
	public FixedFieldSurroundRetrieval(	FixedField fixedfield, int surroundN, int selectMode,
										boolean autoselect) {
		fixedField = fixedfield;
		
		particles = (FixedFieldParticlesIntf) fixedField.getParticles() ;
		
		particleGrid = fixedField.getGrid() ;
		
		this.surroundN  = surroundN;
		this.selectMode = selectMode;
		this.autoselect = autoselect ;
		
		out = fixedField.getOut() ;
	}


	// ========================================================================
	
	
	private void provideResults(){
		

		if (results!=null){
			
			results.setGuid( p.guid);
			results.setParamSet( p );

				
			resultMap.put(p.guid, results);
			
			
			if ((paramSets!=null) ) {
				for (int i = 0; i < paramSets.size(); i++) {
					if (paramSets.get(i)!=null){
						String gs = paramSets.get(i).guid;
						if (gs.contentEquals(p.guid)) {
							paramSets.set(i, null);
							break;
						}
					}
				}
			}
			// out.print(2, "SurroundRetrieval, size of paramSets: "+paramSets.size()) ;
			// out.print(2, "-                          resultMap: "+resultMap.size()) ;
			// observer = RepulsionField
			srObserver.surroundRetrievalUpdate(this, p.guid);
		}

	}

	private void prepareRetrieval(){
		
		SurroundResults results =null;
		
		if (paramSets.size()==0){
			return;
		}
		 
		
		RetrievalParamSet p=null ;
		int pin = paramSets.size()-1;
		while ((pin>=0) && (p==null)){
			p = paramSets.get(pin) ;
			pin--;
		}
		 
		if (p!=null){
			if (p.surroundN > particles.size()*(2.0/3.0)){ p.surroundN = (int) (particles.size()*(2.0/3.0)); };
		}
	}

	
	public String get(int particleIx, int tasktype) {
		String guid; // only for compatibility, not really used...
		
		guid = GUID.randomvalue() ;
		
		
		try{
			
			// determine it
			double x,y ;
			SurroundResults results = new SurroundResults(); 
			// Surround surround;
			
			// ArrayList<IndexDistance> indexedDistances ;
			IndexedDistances indexedDistances ;;
			
			// results.particleIndex = particleGrid.getIndexNear( particles.get(index).x , particles.get(index).y);
			
			// get circle, or any other figure
			
			FixedFieldParticle p = (FixedFieldParticle) particles.get( particleIx ) ;
			
													out.print(4, "going to retrieve surrounding particles around particle ix:"+particleIx+"...");
			
			// ParticleGrid._CIRCLE == default , setShape is optional
			
			IndexListRetriever ixr = particleGrid.getIndexListRetriever();
			indexedDistances = ixr.getIndexedDistancesFromNeighboorhood( (int)p.getX(), (int)p.getY(), surroundN ) ;
			
				
			out.print(3, "particles for surround around particle <ix:"+particleIx+">retrieved (n="+indexedDistances.size()+")...");
			
			if ((results!=null) && (p!=null)){
				results.getCoordinate()[0] = p.x;
				results.getCoordinate()[1] = p.y;
			}
			
			results.getIndexedDistances().clear();
			results.setGuid(guid) ;

			if ((indexedDistances!=null) && (indexedDistances.size()>0)){
				results.getIndexedDistances().addAll( indexedDistances.getItems() ) ;
				
				results.setParticleIndexes( particleGrid.extractIndexesFromIndexedDistances(indexedDistances.getItems()) );
			}

			// send it to the result object list
			fixedField.getEventsreceptor().onSelectionRequestCompleted(results);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return guid;
	}

	

	public String get(int xpos, int ypos, int taskSurroundC) {
		String guid; // only for compatibility, not really used...
		
		guid = GUID.randomvalue() ;
		
		
		
		
		
		return guid;
	}

	public PrintLog getOut() {
		return out;
	}

}
