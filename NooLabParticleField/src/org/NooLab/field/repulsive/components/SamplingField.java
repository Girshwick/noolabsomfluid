package org.NooLab.field.repulsive.components;



import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.RepulsionFieldFactory;
import org.NooLab.field.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.utilities.logging.PrintLog;




public class SamplingField implements Runnable, RepulsionFieldEventsIntf{

	RepulsionFieldCore srepulsionField;
	RepulsionFieldCore parentField;

	RepulsionFieldFactory rfFactory;
	
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
	
	public SamplingField( RepulsionFieldCore parentfield ){
	 
		parentField = parentfield;	
		rfFactory = parentField.getRepulsionFieldFactory() ;
		
		width  = parentField.getAreaWidth() ;
		height = parentField.getAreaHeight() ;
		
		out = parentField.out;
		
		initializeRepulsionField();
		
		fsThrd = new Thread(this,"fsThrd");
	}
	
	
	public void initializeRepulsionField() {
		
		srepulsionField = rfFactory.getRepulsionField().createCoreInstance(rfFactory) ;
		
		// srepulsionField = (RepulsionFieldCore)RepulsionFieldCore.create(); 
		
		srepulsionField.setName("samplerField" );
		// preventing infinite mutual calling
		RepulsionFieldCore.setNestedInstance(1); // 
		
		srepulsionField.setColorSize(false, true);
		srepulsionField.setDelayedOnset(200);
		srepulsionField.useParallelProcesses(0);
		srepulsionField.setAreaSize(width, height);
		
		srepulsionField.init(nbrParticles, energy, repulsion, deceleration);

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
		// TODO: recalculate
	}


	@Override
	public void onActionAccepted(int action, int state, Object param) {
		
	}


	@Override
	public void onCalculationsCompleted() {
		
	}


	@Override
	public void statusMessage(String msg) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean getInitComplete() {
		// TODO Auto-generated method stub
		return false;
	}


	
}
