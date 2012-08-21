package org.NooLab.field.repulsive.components;

import java.util.Vector;

import org.NooLab.field.Stoppable;
import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.infra.PhysicsDigester;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticles;
import org.NooLab.utilities.logging.PrintLog;

// nbthrd  sfthrd wqthrd 


public class FluidFieldCollectStatistics implements Runnable,Stoppable{
	
	RepulsionFieldCore  parentField;
	RepulsionFieldParticles particles;
 	
	boolean wasMetaStable=false;
	public boolean isPhysicsProcessActivated=false;
	boolean isSCRunning=false, isSCStopped=false ;
	public boolean isWaiting=true;
	Thread statsThrd;
	
	boolean showStatisticsInfo = true;
	Vector<Double> stabilityTrendItems = new Vector<Double>(); 
	Vector<Double> movedDistanceTrendItems = new Vector<Double>();
	
	int trendEstimatorSetSize = 18;
	double trendStabilityValue = 1.0;
	boolean frozen;
	
	int processorCount=4;
	int pz=0;
	
	PhysicsDigester physicsProcess;
	FieldSampler sampler ;
	
	PrintLog out;
	
	
	public FluidFieldCollectStatistics( RepulsionFieldCore  parent ){
		
		
		try{
			 
			this.parentField = parent ; 
			
			out = parentField.out ;
			particles = parentField.particles;
			
			isSCRunning=false ;
			isSCStopped=false;
			
			if (parentField.isMultiProc()){
				physicsProcess = parentField.getPhysicsProcess();
				processorCount = parentField.getThreadcount() ;
			}
			
			if (parentField.isMultiProc()==false){
				processorCount=1;	
			}
			sampler = parentField.getSampler() ;
			
			if ((statsThrd!=null) && (isSCRunning)){
				isSCRunning=false; out.delay(50);
			}
			isSCStopped = false;
			
			statsThrd = new Thread(this,"statsThrd");
			statsThrd.setPriority(5); //norm=5, max=10
			 
			statsThrd.start();
			while ((isSCRunning==false) && (isSCStopped==false)){
				out.delay(5);
			}
			
	    
			out.print(3,"Object CollectStatistics() has been constructed.");
			
		}catch(Exception e){
			out.printErr(2, "Critical failure of constructing CollectStatistics() for field <"+parent.getName()+"> ! ");
		}
		
	}
	
	public void explicitCall(){
		parentField.collectStatistics(particles);
	}
	
	public void setCurrentMovedDistanceSum(double movedDistanceSum) {
		boolean stable=false;
										out.print(4,"setCurrentMovedDistanceSum(), ... ");
		movedDistanceTrendItems.add(movedDistanceSum) ;
		if (movedDistanceTrendItems.size()>20){
			movedDistanceTrendItems.remove(0);
										out.print(4,"calling valuesTrendStable(a)... ");
			stable = valuesTrendStable(stabilityTrendItems);
		}
										out.print(4,"setCurrentMovedDistanceSum(), leaving... ");
	}

	public void setCurrentStability(double stability) {

		boolean nowstable=false;
		
		 
									out.print(4,"calling valuesTrendStable(b)... ");
		stabilityTrendItems.add(stability);
		
		if (stabilityTrendItems.size()> trendEstimatorSetSize){
			stabilityTrendItems.remove(0);
		}
										
		nowstable = valuesTrendStable(stabilityTrendItems);
		
		if ((nowstable) && (wasMetaStable==false)){
			String vs1 = ""+stability;
			// out.print(2, "meta-stability of field detected, stable trend of stability (v="+vs1+")");
			wasMetaStable = true;
		}
		if ((nowstable==false) && (wasMetaStable)){
			// out.print(2, "meta-stability lost.");
			wasMetaStable=false;
		}
		
		
	}

	public double getTrendStabilityValue() {
		return trendStabilityValue;
	}
	
	public boolean getShowStatisticsInfo() {
		return showStatisticsInfo;
	}
	
	public void setShowStatisticsInfo(boolean showStatisticsInfo) {
		this.showStatisticsInfo = showStatisticsInfo;
	}
	private boolean valuesTrendStable( Vector<Double> values){
		
		boolean rB = false;
		double t;
		
		out.print(4, "Collecting statistics ("+pz+") about changes. n of values = "+ values.size());
		
		// we need less for multi process contexts
		t = 2+(1+Math.log(0.2+processorCount)/trendEstimatorSetSize) ;
		
		if (values.size() >= t) {
			
			double v1 = values.get(0);
			double v2 = values.get(values.size() - 1);
			
			double r = (Math.abs(v1-v2))/( (v1+v2)/2.0);
			
			rB = r<0.06;
			
			r = Math.round(r*10000.0)/(10000.0);
			trendStabilityValue = r;  ;
			                    
			out.print(4, "Collecting statistics ("+pz+") about changes: trend stability value (np="+parentField.getNumberOfParticles()+", last value="+values.get(values.size() - 1)+") = "+r);			
			pz++;
			if ((r<0.000001) || (pz%10==0)){
				if (showStatisticsInfo){
					out.print(3, "stability = "+r);
				}
			}
			particles.getItems().trimToSize();
			if (pz>1000)pz=1;
		}
		return rB;
	}
	
	public void run() {
		boolean isCScalculating=false;
		int err=0;
		isSCRunning = true;
		isWaiting = false;
		isSCStopped = false;
		 
		
		
		try{
	
			int z=0;
			while ((isSCRunning) && (isSCStopped==false) ){
				z++;
				if (z>100000000)z=2;
										if (z==1){
											String str = parentField.getName() ;
												   if (str.length()==0){ str = parentField.toString(); }		
											out.print(3,"CollectStatistics worker thread has been started by <"+str+">... "); 
										}
										if (z%5==0){
											out.print(4, "collectStatistics() worker thread (waiting:"+isWaiting+")...");
										}
				if (parentField.isMultiProc()){
					if (parentField.getPhysicsProcess()==null){
						out.delay(1);
										if (z%5==0){
											out.print(4, "collectStatistics() worker thread (multiProc): physicsProcess == null ...");
										}
						// continue;
					}
					if ((physicsProcess!=null) && (physicsProcess.isActivated()) &&(parentField.isStopped() )){
						out.delay(1);
						out.print(3, "collectStatistics() worker thread stopping due to stopped parentField.");
						break;
					}
				}
										if (z%5==0){
											out.print(4, "collectStatistics() worker thread, loop control:  isWaiting="+isWaiting+
													     "  isCScalculating="+isCScalculating+
													     "  isPhysicsProcessActivated="+isPhysicsProcessActivated);
										}
				if ((isWaiting==false) || (parentField.isMultiProc()==false)){
					if (isCScalculating==false){
	
						isWaiting = true;
						isCScalculating=true;
						
											if (z%5==0){
												out.print(4, "calling collectStatistics()...");
											}
						if ((parentField.isMultiProc()==false) || ((parentField.isMultiProc()) )){
							try{                                 //  && (isPhysicsProcessActivated)
								
								if ((sampler!=null) && (particles.size()> 300 + (80* processorCount))){
									parentField.collectStatistics( sampler.getParticlesList() );
								}else{
									 
										parentField.collectStatistics( particles );
									 
								}
								frozen = parentField.isFieldLayoutFrozen();
								
								if (frozen){
									isSCRunning=false;
								}
											out.print(4, "collectStatistics() returned...");
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						
						isCScalculating=false;
						isWaiting=true;
						
					} // NOT isCalculating?
				} // not waiting of monoproc?
				out.delay(100);
			}// ->
			
		}catch(Exception e){
			e.printStackTrace();
		}  
		isSCStopped = true ;
		isSCRunning = false;
		
		
			out.print(3, "after stopping collectStatistics() worker thread, loop control:"+
						 "  isWaiting = "+isWaiting + "\n"+
					     "  isCScalculating = "+isCScalculating + "\n"+
					     "  isPhysicsProcessActivated = "+isPhysicsProcessActivated + "\n"+
					     "  parent stopped = "+ parentField.isStopped() + "\n"+
					     "         running = "+ parentField.isRunning() + "\n"+
		     			 "         frozen  = "+ parentField.isFieldLayoutFrozen() );
		
		out.print(3,"CollectStatistics thread for RepulsionField <"+parentField.getName()+"> has been stopped." );
		out.print(4,"isSCStopped="+isSCStopped+" ,  isStopped="+parentField.isStopped());
	}
	
	public void restart(){
		if (isSCRunning==false){
			statsThrd = new Thread(this,"statsThrd");
			statsThrd.start();
			out.print(3,"CollectStatistics thread has been re-started... ");
		}
	}
	public void stop(){
		isSCStopped=true;
		
		out.print(3,"CollectStatistics thread is stopping... ");
		int z=0;
		while ((isSCRunning) && (z<2000)){
			out.delay(1); z++;
		}
		try{
			statsThrd.join();
		}catch(Exception e){}
		statsThrd=null ;
	}
	
	public boolean isWaiting() {
		return isWaiting;
	}
	public void  setWaiting(boolean flag) {
		isWaiting=flag;
	}
	
	@Override
	public boolean isRunning() {
		 
		return isSCRunning;
	}
	
	
	@Override
	public Thread getThread() {
		 
		return statsThrd;
	}
	
	
	// ----------------------------------------------------
	
}


