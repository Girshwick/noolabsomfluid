package org.NooLab.repulsive.components;


import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.RepulsionFieldIntf;
import org.NooLab.repulsive.intf.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.repulsive.particles.ParticlesIntf;
import org.NooLab.utilities.logging.PrintLog;




public class SamplingField implements Runnable, RepulsionFieldEventsIntf{

	RepulsionFieldIntf srepulsionField;
	
	RepulsionField parentField;
	
	private int width , height;
	
	double deceleration = 1.0 ; 
	double repulsion = 1.0 ; 
	double energy = 1.0 ;  
	
	int nbrParticles = 5;

	ParticlesIntf particles ;

	boolean completed=false;
	int iterations =0;
	
	
	Thread fsThrd;
	PrintLog out;
	
	public SamplingField( RepulsionField parentfield ){
	 
		parentField = parentfield;	
		
		width  = parentField.getAreaWidth() ;
		height = parentField.getAreaHeight() ;
		
		out = parentField.out;
		
		initializeRepulsionField();
		
		fsThrd = new Thread(this,"fsThrd");
	}
	
	
	public void initializeRepulsionField() {
		
		srepulsionField = RepulsionField.create();
		
		srepulsionField.setName("samplerField" );
		// preventing infinite mutual calling
		RepulsionField.setNestedInstance(1); // 
		
		srepulsionField.setColorSize(false, true);
		srepulsionField.setDelayedOnset(200);
		srepulsionField.useParallelProcesses(0);
		srepulsionField.setAreaSize(width, height);
		
		srepulsionField.init(srepulsionField, nbrParticles, energy, repulsion, deceleration);

		srepulsionField.registerEventMessaging(this);
		particles = srepulsionField.getParticles();

	}
	
	/** this one gets started by the embedding RepulsionField */
	public void start(){
		
		fsThrd.start();
	}
	
	private void perform(){
		srepulsionField.setAreaSize( width, height );
		
		out.print(2,"preparing the sampling field...");
		
		parentField.getSampler().setSamplesAvailable(false) ;
		 
		completed=false;
		int z=0;
		while (completed==false){
			
			srepulsionField.update();
			
			if(srepulsionField.isUpdateFinished()==false){
				while (srepulsionField.isUpdateFinished()==false){
					out.delay(1);
				}
			}else{
				
				out.delay(1);
				
				if (z%5000==0){
					// showLocations(1);
				}
			}
			
			z++;
		}
		
		iterations = z;
		out.delay(50);
		System.out.println();
		// out.print(2,"preparing the sampling field: "+z+" iterations have been performed");
		z=0;
		
	}

	@Override
	public void run() {
		perform();
	}
	
	@SuppressWarnings("unused")
	private void showLocations(int consoleOut){
		double r,x,y ;
		
		out.print(2, "\n\n");
		
		if (consoleOut > 0) {
			out.print(2, "\n   -----------");
			for (int i = 0; i < particles.size(); i++) {

				x = Math.round(particles.get(i).x);
				y = Math.round(particles.get(i).y);
				r = Math.round(particles.get(i).radius);

				out.print(2, "x: " + x + "  y: " + y + "  r: " + r);

			}
		}
		parentField.getSampler().setSampleRoots(particles);
	}
	
	@Override
	public void onLayoutCompleted(int flag) {
		
		out.print(2,"The sampling field completed its preparation phase."); 
		
		completed=true;
		
		if (out.getPrintlevel()>=3){
			showLocations(1);
		}
	}

	@Override
	public void statusMesage(String msg) {
		
	}


	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}


	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}


	@Override
	public void onSelectionRequestCompleted(Object results) {
	
		
	}


	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onActionAccepted(int action, int state, Object param) {
		// TODO Auto-generated method stub
		
	}


	
}
