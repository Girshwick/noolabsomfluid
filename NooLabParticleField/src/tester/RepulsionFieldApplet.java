package tester;


import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.components.SurroundRetrieval;
import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.repulsive.particles.Particle;

import processing.core.PApplet;



/**
 * 
 * this is an exploration of a probabilistic grid.
 * "nodes" are able to float freely, in principle.
 * they take a position that is near the average for all of them,
 * as an approximation to the most dense packaging. 
 * There are however, always "defects" in the hexagonal arrangement.
 * 
 * 
 */
public class RepulsionFieldApplet extends PApplet implements RepulsionFieldEventsIntf {
 
	private static final long serialVersionUID = -3695827681774169931L;

	RepulsionFieldIntf repulsionField;
	
	
	int passes ;
	int lastx;
	boolean showMouseFeedback = false;
 	PApplet applet;
	boolean drawing=true;
	boolean mouseIsActive = false;
	boolean _SHIFT=false;
	boolean showSelectionDetails=false;
	
	double deceleration = 1.0 ; 
	double repulsion = 1.0 ; 
	double energy = 1.0 ;  
	
	int nbrParticles = 661 ;
	int selectedParticleIndex = -1;
	
	ParticlesIntf particles ;
	String surroundGuid;
	
	
	public void setup() {
		applet = this;
		
		showKeyCommands() ;
		
		size(1000, 600);
		frameRate(40);
		
		colorMode(RGB, 255);
		background(0);
		smooth();
		noStroke();
		background(0);
		
		
		// drawing=false; 
		initializeRepulsionField();
		
		repulsionField.update();
		
	}
	
	private void initializeRepulsionField() { 
		
		
		// repulsionField = RepulsionFieldCore.create();

		repulsionField.useParallelProcesses(0); // set to 0 for debugging
		
		repulsionField.registerEventMessaging(this);
		repulsionField.setName("app") ;
		repulsionField.setColorSize(false, true);
		repulsionField.setInitialLayoutMode(RepulsionField._INIT_LAYOUT_REGULAR);
		repulsionField.setAreaSize( applet.width, applet.height );		
		
		// setting basic parameters for the dynamic behavior of the particles
		// will be used if there is nothing to load
		repulsionField.setDynamics( nbrParticles, energy, repulsion, deceleration );
		repulsionField.setBorderMode( Neighborhood.__BORDER_ALL); // all=rectangle, none=torus (borderless)
		
		// this populates the field, set "nbrParticles" to 0 if you will import coordinates or a field
		// repulsionField.init(repulsionField, 641 );
		
		// importing set of coordinates from last auto-save , or from given filename 
		// any particle existing before will be removed;
		// if nothing found, it will be initialized randomly
		repulsionField.init( "importField" );
		// or with filename:  repulsionField.init(repulsionField, "importField", [filename] );
		
		// or alternatively:
		// repulsionField.init(repulsionField,1 );
		// repulsionField.importField();
		
		
 
		particles = repulsionField.getParticles();
		
		repulsionField.setDelayedOnset(2000);
	}
	
	
	// ---- events from processing ------------------------
	
	public void keyPressed(){
		_SHIFT = false; 
		
		if (key=='A'){
			mouseIsActive = false;
			repulsionField.addParticles( mouseX,mouseY);
		}
		if (key=='a'){
			mouseIsActive = false;
			repulsionField.addParticles(1);
		}
		if (key=='d'){
			mouseIsActive = false;
			repulsionField.deleteParticle( (int)(Math.random()*(particles.size()-1)));
		}
		if (key=='r'){
			mouseIsActive = false;
			if (selectedParticleIndex>=0){
				repulsionField.deleteParticle( selectedParticleIndex );
			}
			selectedParticleIndex = -1;
		}
		
		if (key=='t'){
			mouseIsActive = false;
			repulsionField.releaseShakeIt(3);
		}
		if (key=='T'){
			mouseIsActive = false;
			repulsionField.releaseShakeIt(8);
		}

		if (key=='m'){
			repulsionField.mobilityDecrease();
		}
		if (key=='M'){
			repulsionField.mobilityIncrease();
		}
		if (key=='b'){
			repulsionField.setAdaptiveBehavior(false);
		}
		if (key=='B'){
			repulsionField.setAdaptiveBehavior(true);
		}
		if (key=='f'){
			showMouseFeedback = !showMouseFeedback;  
		}
		
		if (key=='s'){
			repulsionField.selectionSizeDecrease(1,1);
		}
		if (key=='S'){
			// if the new selectionsize is larger than the buffers, the buffers will 
			// be recalculated (which can take some time, dependent on the size of the field, 
			// the size of the selection and the computer)
			repulsionField.selectionSizeIncrease(1,1);
		}
		
		if (key=='e'){
			repulsionField.exportCoordinates( RepulsionField._RFUSERDIR + "/data/coodinates.dat" );
		}
		
		
		if (keyCode==16){
			_SHIFT = true;
		}
		if (key=='x'){
			repulsionField.storeRepulsionField(); // also: with dedicated name outside of user-dir
			System.exit(0) ;
		}
	}
	
	
	public void mouseClicked() {
		// no output call here, any result will arrive in the event method "onSelectionRequestCompleted()"
		// that has been implemented for / defined through the interface "RepulsionFieldEventsIntf"
		 mouseIsActive = false;
		 println("click ("+mouseX+","+mouseY+") received...");
		 
		 if (_SHIFT){
			 _SHIFT = false;

			repulsionField.getSurround( mouseX,mouseY,  1, true);
			 
		 }else{
			 repulsionField.selectParticleAt( mouseX, mouseY, true );
		 }
		 
		 mouseIsActive = true;
	}
	 
	
	// ---- events from RepulsionField --------------------
	
	@Override
	public void onSelectionRequestCompleted(Object resultsObj) {
		SurroundResults results;
		 
		
		results = (SurroundResults)resultsObj;
		
		if (results.getParticleIndexes().length==1){
			selectedParticleIndex = results.getParticleIndexes()[0]; 
			println("Index of selected Particle : "+selectedParticleIndex) ;
		}
		if (showSelectionDetails){

			printIndexList(results, 1);

			int i = results.getParamSet().getTask();

			if (results.getParamSet().getTask() >= SurroundRetrieval._TASK_SURROUND_C) {
				printIndexList(results, 2);
			}
		}
	}

	@Override
	public void onLayoutCompleted(int flag) {
	
		println("Message from RepulsionField: layout has been completed.\n");
		drawing=true;
		
		width  = repulsionField.getAreaSize()[0];
		height = repulsionField.getAreaSize()[1];
		
		mouseIsActive = true;
	}

	 

	
	// ---- processing drawing loop -----------------------
	public void draw() {
		
		
		if (drawing){
			// repulsionField.setAreaSize( width, height );
		
			repulsionField.update();
		
			background(0);

			drawParticles();
		}
		if ((mouseIsActive ) && (showMouseFeedback)){
			if ((passes % 10==0) && (lastx!=mouseX)){
				println("mouse-X,Y : "+mouseX+" - "+mouseY+"");
				lastx=mouseX;
			}
		}
		passes++;
	}

	// ---- some output methods ---------------------------
	
	private void drawParticles(){
		
		nbrParticles = repulsionField.getNumberOfParticles();
		
		for (int i = 0; i < nbrParticles; ++i) {
			 
			drawparticle(i);
			// print(i+" ");
		}
	}
	
	private void drawparticle(int index){
		 
		Particle particle ;
		try{
			if ((index<0) || (index>particles.size()-1)){ return;}
			particle = particles.get(index);
			int r, g, b;
			r = particle.getR();
			g = particle.getG();
			b = particle.getB();
			fill(r, g, b);

			// ellipse((int) particle.x, (int) particle.y, (int)
			// (particle.radius/3), (int)( particle.radius)/3);
			ellipse((int) particle.x, (int) particle.y, (int) (particle.radius), (int) (particle.radius));
		} catch (Exception e){}
	}
	
	
	private void printIndexList( SurroundResults results, int typestyle){
		String[] valuePairs;
		
		if(typestyle<=0){
			println(""+results.getParticleIndexes()+" <index,ditance> value pairs have been received.") ;
		}
		if (typestyle==1){
			printIndexList ( results.getParticleIndexes());
		}
		if (typestyle==2){
			printIndexList ( results.getParticleDistances());
		}
		if (typestyle>=3){
			valuePairs = new String[results.getParticleIndexes().length];
			for (int i=0;i<valuePairs.length;i++){
				double v = Math.round(results.getParticleDistances()[i]*10.0)/10.0;
				valuePairs[i] = "("+results.getParticleIndexes()[i]+","+v +") ";
			}
			printIndexList ( valuePairs);
		}
	}
	private void printIndexList( int[] ivalues){
		
		System.out.print("index values    : ") ;
		for (int i=0;i<ivalues.length;i++){
			System.out.print(ivalues[i]+" ");
		}
		System.out.println();
	}
	private void printIndexList( double[] ivalues){
		
		System.out.print("distance values : ") ;
		for (int i=0;i<ivalues.length;i++){
			System.out.print( (double)(Math.round(ivalues[i]*10.0)/10.0)+" ");
		}
		System.out.println();
	}
	private void printIndexList( String[] ivalues){
		
		System.out.print("distance values : ") ;
		for (int i=0;i<ivalues.length;i++){
			System.out.print( ivalues[i]+" ");
		}
		System.out.println();
	}


	private void showKeyCommands(){
		
		println();
		println("the following key commands are available...");
		println("   a  ->  add a new particle at a random location");
		println("   d  ->  remove a latest particle");
		println();
		println("   S  ->  increase surrounds in selections");
		println("   s  ->  decrease surrounds in selections");
		println();
		println("   t  ->  shake'em a bit");
		println("   T  ->  shake'em !");
		println("   m  ->  decrease mobility of particles");
		println("   M  ->  increase mobility of particles");
		println();
		println("   e  ->  export data ");
		println();
		println("   x  ->  exit");
		println();
		println("   click       : select item");
		println("   shift-click : select surround");
	
		println("------------------------------------------------------------------");
		println("starting (please wait) ...\n");
	}

	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		 
		applet.width  = width;
		applet.height = height;
	}

	@Override
	public void onActionAccepted(int action, int stateflag, Object param) {
		  
		if (action== RepulsionFieldEventsIntf._FIELDACTION_ADD){
			
		}
		if (action== RepulsionFieldEventsIntf._FIELDACTION_DEL) {
			println("state of request delete for index = "+(int)((Integer)param)+" : "+stateflag) ;
		}
		
	}

	@Override
	public void onCalculationsCompleted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void statusMessage(String msg) {
		// TODO Auto-generated method stub
		
	}
}