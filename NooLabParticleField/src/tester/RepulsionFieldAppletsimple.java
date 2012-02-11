package tester;

import java.awt.Graphics2D;
import java.util.ArrayList;
 

import math.geom2d.conic.Ellipse2D;
import math.geom2d.line.Line2D;
import math.geom2d.polygon.Polygon2D;

import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.RepulsionFieldFactory;
 
import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.components.RepulsionFieldProperties;
import org.NooLab.repulsive.components.SurroundRetrieval;
import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.intf.ParticleDataHandlingIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.intf.particles.GraphParticleIntf;
import org.NooLab.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.repulsive.particles.Particles;

 

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;
 



/**
 * 
 * 
 * this is an exploration of a probabilistic grid, accessible through the object RepulsionField.
 * "nodes" are able to float freely, in principle.
 * they take a position that is near the average for all of them, as an approximation to the most dense packaging.
 *  
 * There are however, always "defects" in the hexagonal arrangement.
 * 
 * TODO: for visualization of large areas including scrolling capability: use processing's off-screen buffer!
 * 	     PGraphics buf = createGraphics(500, 500, P3D);
 * 		 http://wiki.processing.org/w/Draw_to_off-screen_buffer
 * 
 */
public class RepulsionFieldAppletsimple 	extends 
														PApplet 
											implements 
														RepulsionFieldEventsIntf, 
														ParticleDataHandlingIntf {
 
	private static final long serialVersionUID = -3695827681774169931L;

	RepulsionFieldFactory rfFactory;
	RepulsionFieldIntf repulsionField;
	
	
	int passes ;
	int lastx;
	boolean showMouseFeedback = false;
 	PApplet applet;
 	Graphics2D g2d ;
 	
	boolean drawing=true;
	boolean mouseIsActive = false;
	boolean _SHIFT=false, _CTRL=false ;
	boolean showSelectionDetails=false;
	
	double deceleration = 1.0 ; 
	double repulsion = 1.0 ; 
	double energy = 1.0 ;  
	
	int nbrParticles = 71 ;
	
	String requestGuid;
	int selectedParticleIndex = -1;
	ArrayList<Integer> selectedSet = new ArrayList<Integer>(); 
	 
	ArrayList<Ellipse2D> ellipses = new ArrayList<Ellipse2D>();
	ArrayList<Line2D> lines = new ArrayList<Line2D>();
	ArrayList<Polygon2D> polygons = new ArrayList<Polygon2D>();
	  
	
	
	
	public void setup() {
		applet = this;
		
		showKeyCommands() ;
		
		size(1000, 600);
		frameRate(15);
		
		colorMode(RGB, 255);
		
		smooth();
		noStroke();
		background(0);
		
		
		// drawing=false; 
		initializeRepulsionField( nbrParticles );
		
		repulsionField.update();
		 
		g2d = ((PGraphicsJava2D)g).g2;
		
		// installAdditionalJApplet();
	}
	
	 
	
	private void initializeRepulsionField(int nodeCount) { 


		int runRequestTester=0;
		
		// TODO: properties offered by static create(s) and interface
		
		if (runRequestTester>0){
			// for testing, 100 = 100ms delay between calls, press +/. to ac-/decelerate
			rfFactory = new RepulsionFieldFactory("test:RQ=1000");
			
			
			repulsionField = rfFactory.getRepulsionField() ;
			
		}else{
			// simple call 
			repulsionField = (new RepulsionFieldFactory()).getRepulsionField() ;
		}
		
		
		repulsionField.setFieldIsRandom(true);
		
		repulsionField.useParallelProcesses(1); // set to 0 for debugging
		
		repulsionField.registerEventMessaging(this);
		repulsionField.setName("app") ;
		repulsionField.setColorSize(false, true);
		repulsionField.setInitialLayoutMode(RepulsionField._INIT_LAYOUT_REGULAR);
		
		_adaptAppletSize( nodeCount, 11.5 );
		
		repulsionField.setMaxDensityDeviationPercent( 5.0 );
		// setting basic parameters for the dynamic behavior of the particles
		// will be used if there is nothing to load
		
		repulsionField.setDynamics( nbrParticles, energy, repulsion, deceleration );
		repulsionField.setBorderMode( Neighborhood.__BORDER_ALL); // all=rectangle, none=torus (borderless)
		
		// this populates the field, set "nbrParticles" to 0 if you will import coordinates or a field
		repulsionField.init( nodeCount );
		
		// importing set of coordinates from last auto-save , or from given filename 
		// any particle existing before will be removed;
		// if no file will be found, it will be initialized randomly
		// >>>>>>>>> repulsionField.init( "importField" );
		// or with filename:  
		// repulsionField.init("importField:filename=C:/Users/kwa/rf/config/~RepulsionFieldData-app-10000.dat" );
		// C:/Users/kwa/rf/config/~RepulsionFieldData-app-10000.dat
		
		// or alternatively in two steps:
		// repulsionField.init(repulsionField,1 );
		// repulsionField.importField();
		
		
		repulsionField.setDelayedOnset(2000);
	}
	
	
	private void showKeyCommands(){
		
		println();
		println("the following key commands are available...");
		println("   a  ->  add a new particle at a random location");
		println("   A  ->  add a new particle at the position of the mouse cursor");
		println("   d  ->  remove a random particle");
		println("   r  ->  remove the selected particle");
		// println("   F  ->  fix the position for the selected particle (toggling the f-state)");
		// println("   f  ->  release all fixations");
		println();
		println("   S  ->  increase surrounds for circular selections");
		println("   s  ->  decrease surrounds for circular selections");
		println();
		println("   p  ->  calculate membership of particles to a MST-like area based on such a set ");
		println("   h  ->  calculate membership of particles enclosed by a convex hull");
		// println("   c  ->  activate cohesion for selected particles (they will move continuously)");
		// println("   z  ->  move one particle towards another along the ");
		println("   u  ->  remove/clear the collected set");
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
		println("   ctrl-click  : add a particle to a set");
		println();
		println("------------------------------------------------------------------");
		println("starting (please wait) ...\n");
	}



	// ---- events from processing ------------------------
	
	public void keyPressed(){
		_SHIFT = false; 
		_CTRL  = false;
		int pix ;
		
		
		if (key=='A'){
			mouseIsActive = false;
			pix = repulsionField.addParticles( mouseX,mouseY);
			
			_testSelect(pix);
		}
		if (key=='a'){
			mouseIsActive = false;
			pix = repulsionField.addParticles(1);
		}
		if (key=='b'){
			mouseIsActive = false;
			pix = repulsionField.splitParticle( selectedParticleIndex, this );
		}
		if (key=='d'){
			mouseIsActive = false;
			repulsionField.deleteParticle( (int)(Math.random()*(repulsionField.getNumberOfParticles() )));
		}
		if (key=='r'){
			mouseIsActive = false;
			if (selectedParticleIndex>=0){
				repulsionField.deleteParticle( selectedParticleIndex );
			}
			selectedParticleIndex = -1;
		}
		 
		if (key=='u'){
			selectedSet.clear();
			ellipses.clear();
			unselectAllParticles(); 
		}         
		if (key=='p'){
			if (selectedSet.size()<=1){
				println("the selected set is too small.");
				return;
			}
			String guid = repulsionField.getParticlesOfFiguratedSet( RepulsionFieldIntf.__SELECTION_FIGURE_MST, selectedSet, 
													   		  		 1.7, 1.0, true );// thickness of skeleton inflation, 
																		// endPointRatio for ellipsis construction, autoselect)
		}
		if (key=='h'){
			if (selectedSet.size()<=1){
				println("the selected set is too small.");
				return;
			}
			repulsionField.getParticlesOfFiguratedSet( RepulsionFieldIntf.__SELECTION_FIGURE_CONVEXHULL, selectedSet, 
					   								   1, 1, true ); // thickness = 1, >1, <1 , mode=1 -> area, 2->ring,  autoselect)

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
		
		if (keyCode==49){ // "+"
			rfFactory.getRqTester().increaseFrequency();
		} 
		if (keyCode==45){ // "-"
			rfFactory.getRqTester().decreaseFrequency(); 
		}
		
		if (keyCode==16){
			_SHIFT = true;
		}
		if (keyCode==17){
			_CTRL = true;
		}
		if (key=='x'){
			repulsionField.storeRepulsionField(); // also: with dedicated name outside of user-dir
			System.exit(0) ;
		}
	}
	
	public void mouseReleased() {
		
	}
	
	public void mouseClicked() {
		// no output call here, any result will arrive in the event method "onSelectionRequestCompleted()"
		// that has been implemented for / defined through the interface "RepulsionFieldEventsIntf"
		 mouseIsActive = false;
		 println("click ("+mouseX+","+mouseY+") received...");
		 
		 if (_SHIFT){
			 _SHIFT = false;

			 requestGuid = repulsionField.getSurround( mouseX,mouseY,  1, true);
			 
		 }else{
			 requestGuid = repulsionField.selectParticleAt( mouseX, mouseY, true );
		 }
		 
		 if (_CTRL){
			 requestGuid = repulsionField.selectParticleAt( mouseX, mouseY, true );
		 }
		 mouseIsActive = true;
	}
	 
	
	// ---- events from RepulsionField --------------------
	
	/**
	 * 
	 * the requests for a selection return to here, whether for single particle or for a vicinity
	 */
	@Override
	public void onSelectionRequestCompleted(Object resultsObj) {
		SurroundResults results;
		String str ;
		
		
		results = (SurroundResults)resultsObj;
		
		if ((results!=null) && (results.objects!=null)){
			results.exportEllipses(ellipses);
			results.exportLines(lines);
			results.exportPolygons(polygons);
			 
		}
		if (results.getParticleIndexes().length==1){
			selectedParticleIndex = results.getParticleIndexes()[0]; 
			println("Index of selected Particle : "+selectedParticleIndex) ;
			// particles = repulsionField.getParticles() ;
			// particles.get( selectedParticleIndex).setDisplayedColor( RepulsionField._OUT_SELECTCOLOR) ;
			
			if (_CTRL){
				if (selectedSet.indexOf( selectedParticleIndex)<0){
					selectedSet.add(selectedParticleIndex);
					
					if (selectedSet.size() >= 2) {
						str = results.arrutil.arr2text(selectedSet);
						println("Now there are <" + selectedSet.size()+ "> items in the selected set: "+ str);
					}
				}
			}
			
		}else{
			int[] indexes = results.getParticleIndexes();
			str = results.arrutil.arr2text( indexes ,0, 29 );
			
			println("set of selected particles (by index, first 30 items): "+str) ;
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

	@Override
	public void onCalculationsCompleted() {
	 
		println("\nNow the RepulsionField is prepared to be used.\n");
		 
	}

	@Override
	public void statusMessage(String msg) {
		println("Message from RepulsionField: "+msg);
	}

	
	// ---- processing drawing loop -----------------------
	public void draw() {
		
		
		if (drawing){
			// repulsionField.setAreaSize( width, height );
			
			repulsionField.update();
		
			background(0);

			drawParticles();
			
			drawEllipses();
			
			drawPoly();
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
			 
			drawparticle(i,1);
			// print(i+" ");
		}
	}
	
	private void drawparticle(int index, int indicator){
		 
		GraphParticleIntf particle ;
		GraphParticlesIntf particles ;
		
		try{
			// particles = repulsionField.getParticles();
			// for dynamic display of changing chart , use this:
			particles = repulsionField.getGraphParticles();
			
			if ((index<0) || (index>particles.size()-1)){ return;}
			particle = particles.get(index);
			
			int r = particle.getDisplayedColor().getIntR();
			int g = particle.getDisplayedColor().getIntG();
			int b = particle.getDisplayedColor().getIntB();
			
			if ((selectedSet.indexOf( index)>=0) && (indicator<=1)){
				r = 230;g=50;b=50;
			}
			fill(r, g, b);

			double x,y ;
			x = particle.getX();
			y = particle.getY();
			
			// (particle.radius/3), (int)( particle.radius)/3);
			if ((x<=applet.width) && (y<applet.height)){
				ellipse((int) x, (int) y, (int) (particle.getRadius()), (int) (particle.getRadius()));
			}else{
				// offscreen buffer, sized as the area 
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void drawEllipses(){
		java.awt.Color col = new java.awt.Color(255,1,1);
		try{
			stroke(255 );
			 
			g2d.setColor( col );
			for (int i=0;i<ellipses.size();i++){
				if (ellipses.get(i) != null) {
					ellipses.get(i).draw(g2d);
				}
			}
			// println( ""+(ellipses.size())+" ellipses drawn.");
			
			noStroke();
			
		}catch(Exception e){
			
		}
		

	}
	
	 
	private void drawPoly(){
		java.awt.Color col = new java.awt.Color(255,1,1);
		try{
			stroke(255 );
			 
			g2d.setColor( col );
			for (int i=0;i<polygons.size();i++){
				if (polygons.get(i) != null) {
					polygons.get(i).draw(g2d);
				}
			}
			// println( ""+(ellipses.size())+" ellipses drawn.");
			
			noStroke();
			
		}catch(Exception e){
			
		}
		

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


	private void _testSelect( int index){
		
		repulsionField.getGraphParticles().get(index).setDisplayedColor( new int[]{250,30,250});
		
		drawparticle(index,2);
	}



	private void unselectAllParticles(){
		GraphParticlesIntf particles ;
		
		particles = repulsionField.getGraphParticles();
		
		for (int i=0;i<particles.size();i++){
			particles.get(i).unselect(); 
		}
	}
	
	
	private void _adaptAppletSize( int nodeCount, double avgDensity  ){

		if (nodeCount < 100) {
			repulsionField.setAreaSizeMin();
		} else {
			// order is important in first call of these
			repulsionField.setDefaultDensity(avgDensity);
			repulsionField.setAreaSizeAuto(nodeCount);

			applet.width = repulsionField.getAreaSize()[0] + 10;
			applet.height = repulsionField.getAreaSize()[1] + 10;
			size(applet.width, applet.height);
		}
		
		// will overrule the foregoing only if the resulting area would be
		// larger (and not too large)
		repulsionField.setAreaSize(800, 400);
		// establish change if there is any
		applet.width = repulsionField.getAreaSize()[0] ;
		applet.height = repulsionField.getAreaSize()[1] ;
		size(applet.width+10 , applet.height+10);
		
	}
	
	// ---- events ------------------------------------------------------------
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
	public void handlingDataOnParticleSplit(Object observable,
			Particles particles, int originix, int pullulix) {
		// TODO Auto-generated method stub
		
	}
}

