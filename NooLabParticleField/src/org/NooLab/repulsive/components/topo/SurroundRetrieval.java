package org.NooLab.repulsive.components.topo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.components.data.RetrievalParamSet;
import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.intf.SurroundRetrievalObserverIntf;
 
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.ParticlesIntf;
import org.NooLab.utilities.net.GUID;


/**
 * 
 * once instantiated, it should be avoided to send parallel tasks to this class 
 * 
 * 
 */
public class SurroundRetrieval implements Runnable {

	public static final int _TASK_PARTICLE_RX = 1;
	public static final int _TASK_PARTICLE    = 2;
	
	public static final int _TASK_SURROUND_C = 4; 
	public static final int _TASK_SURROUND_X = 5; 
	
	SurroundRetrievalObserverIntf  srObserver;
	RepulsionField parentField; 
	ParticlesIntf particles;
	
	ArrayList<RetrievalParamSet> paramSets = new ArrayList<RetrievalParamSet>(); 
	
	Map<String,Object> resultMap = new HashMap<String,Object>();
	  
	Thread srThrd;
	int surroundTask=-1;
	
	public SurroundRetrieval( RepulsionField parent ){
		parentField = parent ;
		srObserver = (SurroundRetrievalObserverIntf)parentField ;
		
		particles = parentField.getParticles();
	}
	 
	// for selecting a single particle, results returned via interfaced event
	public int addRetrieval(int xpos, int ypos, boolean autoselect) {
		int index=-1;

		RetrievalParamSet paramSet;

		paramSet = new RetrievalParamSet();
		 
		paramSet.xpos = xpos ;
		paramSet.ypos = ypos ;
		
		paramSet.particleIndex = -1 ;
		paramSet.surroundN  = 0 ;
		paramSet.autoselect = autoselect ;
		
		
		index = paramSets.size();
		paramSets.add(paramSet) ;
		
		return index;

	}


	public int addRetrieval( int particleIndex , int surroundN ,  
							 int selectMode, boolean autoselect){
		int index=-1;

		RetrievalParamSet paramSet;

		paramSet = new RetrievalParamSet();
		 
		paramSet.particleIndex = particleIndex ;
		paramSet.surroundN  = surroundN ;
		paramSet.selectMode = selectMode ;
		paramSet.autoselect = autoselect ;
		
		
		index = paramSets.size();
		paramSets.add(paramSet) ;
		
		return index;
	}
	
	
	public int addRetrieval( int xpos, int ypos , int surroundN ,  
			  				 int selectMode, boolean autoselect){
		RetrievalParamSet paramSet;
		
		paramSet = new RetrievalParamSet();
		 
		paramSet.xpos = xpos ;
		paramSet.ypos = ypos ;
		paramSet.surroundN  = surroundN ;
		paramSet.selectMode = selectMode ;
		paramSet.autoselect = autoselect ;
		
		
		int index = paramSets.size();
		paramSets.add(paramSet) ;
		return index;
	}

	
	public String go( int paramSetIndex, int task ) {
		String guidStr="";
		
		guidStr = GUID.randomvalue() ;
		
		surroundTask = task;
		
		paramSets.get(paramSetIndex).task = task;
		paramSets.get(paramSetIndex).guid = guidStr;
		
		srThrd = new Thread(this,"srThrd");
		 
		srThrd.start();
		
		return guidStr;
	}
	
	
	@Override
	public void run() {
		
		if (surroundTask<=_TASK_PARTICLE){
			getParticle( surroundTask );
		}
				
		
		if ((surroundTask==_TASK_SURROUND_C) || (surroundTask==_TASK_SURROUND_X)){
			getSurrounds(surroundTask);
		}
		
	}



	private void getParticle( int style ){
		
		int xpos, ypos;
		SurroundResults results = new SurroundResults();
		Surround surround = new Surround(parentField);
		Particle p;
		
		RetrievalParamSet rps = paramSets.get(paramSets.size()-1) ;
		
		xpos = rps.xpos ; 
		ypos = rps.ypos ;
		
		results.particleIndex = surround.getParticleAt( xpos, ypos, -1);// particles.get(0).radius );
			
 
		p = particles.get( results.particleIndex ) ;
		
		if ((results!=null) && (p!=null)){
			results.getCoordinate()[0] = p.x;
			results.getCoordinate()[1] = p.y;

			results.setGuid( rps.guid);
			results.setParamSet( rps );
			results.timeflag = System.currentTimeMillis();

			resultMap.put(rps.guid, results);

			srObserver.surroundRetrievalUpdate(this, rps.guid);
		}
	}

	private void getSurrounds(int style){
		 
		SurroundResults results =null;
		
		if (paramSets.size()==0){
			return;
		}
		
		
		RetrievalParamSet p = paramSets.get(paramSets.size()-1) ;
		if (style== _TASK_SURROUND_C){
			results = getSurround( p.xpos, p.ypos, p.surroundN, p.selectMode,  p.autoselect);
		}
		if (style== _TASK_SURROUND_X){
			results = getSurround( p.particleIndex, p.surroundN, p.selectMode,  p.autoselect);
		}
		
		if (results!=null){
			
			
			results.setGuid( p.guid);
			results.setParamSet( p );
			 

			// put his to the result map
			resultMap.put(p.guid, results);

			srObserver.surroundRetrievalUpdate(this, p.guid);
		}
	}
	
	
	private SurroundResults getSurround( int xpos, int ypos , int surroundN ,  
			  							 int selectMode, boolean autoselect){

		SurroundResults results = new SurroundResults(); 
		 
		Surround surround = new Surround(parentField);

		results.setParticleIndexes( surround.getGeometricSurround( xpos, ypos ,surroundN,Surround._CIRCLE ) ); 
		results.setParticleDistances( surround.getParticleDistances() );
		
		return results;
		
	}
	
	private SurroundResults getSurround( int index , int surroundN ,  
				 					     int selectMode, boolean autoselect){
		SurroundResults results = new SurroundResults(); 
		 
		Surround surround = new Surround(parentField);

		results.setParticleIndexes( surround.getGeometricSurround( index ,surroundN,Surround._CIRCLE ) ); 
		results.setParticleDistances( surround.getParticleDistances()) ;
		
		return results;
	}
	
	
	public Object getResultsByGuid(String guid ) {
		Object resultObject=null;
		 
		resultObject = resultMap.get(guid);
		
		return resultObject;
	}
	
}	
 

 