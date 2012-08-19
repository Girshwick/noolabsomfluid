package org.NooLab.field.repulsive.components;

import java.util.Random;
import java.util.TimerTask;

import org.NooLab.field.repulsive.RepulsionField;
import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.field.repulsive.particles.Particle;
import org.NooLab.field.repulsive.particles.Particles;



/**
 * 
 * this class runs a process, which periodically takes a small patch (100..200 particles) 
 * that is selected by random, then it runs spatial optimization, and a refresh of the
 * particleGrid structure 
 * 
 */
public class GridOptimizer {

	RepulsionFieldCore rfCore;
	OptiWorker optimizer ;
	
	boolean isActive = false;
	
	long periodTime=80000;
	Random random = new Random();
	
	public GridOptimizer(RepulsionFieldCore rfc) {
		rfCore = rfc ;
		init();
	}
	public GridOptimizer(RepulsionFieldCore rfc, boolean immediateStart) {
		rfCore = rfc ;
		isActive = immediateStart;
		init();
	}
	private void init(){
		//  
		
		optimizer = new OptiWorker();
		
		random.setSeed(9182) ;
		random.nextDouble() ;
	}

	
	
	class OptiWorker implements Runnable{

		boolean isRunning = false, isWorking=false;;
		
		long lastWorkTime;
		
		Thread optiThrd;
		// ................................................
		public OptiWorker(){
		
			optiThrd = new Thread(this ,"optiThrd") ;
		
			if (isActive){
				optiThrd.start() ;
			}
		}
		// ................................................
		
		private void perform(){
			Particles particles;
			Particle p;
			RepulsionField rField ;
			
			double w,h ,x,y ;
			int c,r,ix,n ;
			int[] pixes ;
			
			try{
											
				
				particles = new Particles(rfCore);
				
				// random selection of location
				w = rfCore.getAreaWidth() ;
				h = rfCore.getAreaHeight() ;
				
				x = Math.round( w*0.01 + w * 0.98 * random.nextDouble()); // -> /3 == DEBUG ONLY
				y = Math.round(h*0.01 + h * 0.98 * random.nextDouble() );
											rfCore.out.print(2,"optimizing grid in patch around x,y: "+x+","+y);
				// get particle there
				ix = rfCore.getParticleGrid().getIndexNear(x, y) ;
				
				// get surrounding
				n = 80 ;
				if (rfCore.particles.size()>1000){
					n = (int) Math.round(n + 23*Math.log10( rfCore.particles.size())) ;
				}
				pixes = rfCore.getParticleGrid().getIndexListRetriever().getIndexesFromNeighboorhood(x, y, n) ;
				
				

				for (int i=0;i<pixes.length;i++){
					p = rfCore.particles.get(pixes[i]);
					if (p!=null){
						if (rfCore.out.getPrintlevel()>=3){
							p.setDisplayedColor( new int[]{240,160,10}) ;
						}
						particles.add( p );
					}
				}
				
				// loop for optimization , 30 times
				for (int i=0;i<20;i++){
					for (int k=0;k<pixes.length;k++){
						rfCore.doPhysicsFor( pixes[k] );
						rfCore.updateParticlesByDislocation( pixes[k] );
					}
					
					rfCore.out.delay(200) ;
					if (rfCore.isFieldLayoutFrozen()==false){
						return ;
					}
				}
				
				rfCore.setFieldLayoutFrozen(false);
				
				rField = rfCore.getRepulsionFieldFactory().getRepulsionField() ;
				rField.update();
				rfCore.collectStatistics(rfCore.particles) ;
				rField.updatingBuffersFromCore( -1);
				
				rfCore.out.delay(2000);
				rfCore.freezeLayout( -1, 1) ;
				
				// for DEBUG ONLY = printlevel >= 3 
				SurroundResults results = new SurroundResults();
				results.setParticleIndexes(pixes) ; 
				
				if (rfCore.out.getPrintlevel() >= 3) {
					rField.onSelectionRequestCompleted(results);

					for (int i = 0; i < pixes.length; i++) {
						p = rfCore.particles.get(pixes[i]);
						if (p != null) {
							p.resetColor();
						}
					}
				}
			}catch(Exception e){
			}
		}

		@Override
		public void run() {
			long dt;
			isRunning=true;
			lastWorkTime = System.currentTimeMillis() ;
			
			rfCore.out.delay(30000);
			
			try{
				
				while (isRunning){
					
					dt = (System.currentTimeMillis() - lastWorkTime);
					
					/*
					if (rfCore.isRunning()==false ){
						isRunning = false;
						break;
					}
					*/
					
					if ((dt>periodTime) && (isWorking==false) ){
						isWorking=true;
						
						if (rfCore.isFieldLayoutFrozen()){
							perform();
						}
						
						lastWorkTime = System.currentTimeMillis() ;
						isWorking = false;
					}
					rfCore.out.delay(100) ;
				}// ->

			}catch(Exception e){
			}
			
			dt=0;
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void setRunning(boolean isRunning) {
			this.isRunning = isRunning;
		}

		public boolean isWorking() {
			return isWorking;
		}
		
	}



	public long getPeriodTime() {
		return periodTime;
	}

	public void setPeriodTime(long periodTime) {
		this.periodTime = periodTime;
	}

	public void start() {
		if (optimizer.isRunning == false){
			optimizer = new OptiWorker();
		}
	}

	public void stop() {
		optimizer.isRunning = false;
		rfCore.out.delay(200);
		try{
		
			optimizer=null;
			
		}catch(Exception e){
		}
		
	}

	public void setActive(boolean flag) {
		isActive = flag ;
		
	}
	
 

	 
}
