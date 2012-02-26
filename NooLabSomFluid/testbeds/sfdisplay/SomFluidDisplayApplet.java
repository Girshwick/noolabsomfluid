package sfdisplay;


import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Vector;

import math.geom2d.conic.Ellipse2D;
import math.geom2d.line.Line2D;
import math.geom2d.polygon.Polygon2D;

import org.NooLab.compare.utilities.math.DistanceControlProperties;
import org.NooLab.glue.common.DataContainer;
import org.NooLab.glue.components.MessageBoardFactoryProperties;
import org.NooLab.glue.components.pkg.TaskPackage;
import org.NooLab.glue.instances.ParticipantFactory;
import org.NooLab.glue.instances.ParticipantSourceIntf;
import org.NooLab.glue.instances.ResultReceiverIntf;
import org.NooLab.glue.subscription.Future;
import org.NooLab.glue.subscription.FutureIntf;
import org.NooLab.glue.subscription.FutureProperties;
import org.NooLab.glue.subscription.FuturesIntf;
import org.NooLab.glue.subscription.context.Context;
import org.NooLab.glue.subscription.context.ContextInfra;
import org.NooLab.glue.subscription.context.ContextIntf;
import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.RepulsionFieldFactory;

import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.components.SurroundRetrieval;
import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.intf.ParticleDataHandlingIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.intf.particles.GraphParticleIntf;
import org.NooLab.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidFactoryClientIntf;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidRequestPackageIntf;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.objects.StringedObjects;

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;






/**
 * 
 * The purpose of the applet is to display a SOM... and to display ONLY;
 * used infrastructure is the Glue, where the calc server send a lattice to this display module
 * there is no possibility to deliver commands so far
 * 
 * this applet acts as a Glue-client for SomFluids;
 * it is a compound derivative from "RepulsionField" applet and "MsgReceptorInstance" ;
 * 
 * technically, it is a "Glue Source", since it triggers the updates through 
 * dedicated messages. The SomFluid component implements (at least) a "Glue Receptor" 
 * (for the purpose of this applet), returning the description of the SOM,
 * either just the intensional layer of the nodes (incl. position), or additionally
 * the extensional container (transporting the indexes of the records per node)  
 * 
 * 
 * This applet runs a drawing loop that displays a data structure,
 * which in turn gets updated through asynchronous receive events.
 * 
 * The SomFluidDisplay requires the Glue MessageBoard to run
 * 
 */
public class SomFluidDisplayApplet extends PApplet {
 
	private static final long serialVersionUID = -3695827681774169931L;

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
	
	int nbrParticles = 61 ;
	
	String requestGuid;
	int selectedParticleIndex = -1;
	ArrayList<Integer> selectedSet = new ArrayList<Integer>(); 
	 
	ArrayList<Ellipse2D> ellipses = new ArrayList<Ellipse2D>();
	ArrayList<Line2D> lines = new ArrayList<Line2D>();
	ArrayList<Polygon2D> polygons = new ArrayList<Polygon2D>();
	  
	SomDisplaySrcInstance srcInstance;
	boolean triggerIsActive = false;
	boolean connectionEstablished = false;
	
	SomFluidFactoryClientIntf sfFactory;
	
	
	public void setup() {
		applet = this;
		
		showGlueKeyCommands() ;
		
		size(1000, 600);
		frameRate(10);
		
		colorMode(RGB, 255);
		
		smooth();
		noStroke();
		background(0);
		
	  
		srcInstance = new SomDisplaySrcInstance ();
		
		g2d = ((PGraphicsJava2D)g).g2;
		
		// installAdditionalJApplet();
	}
	
 
	
	private void showSomKeyCommands(){
		
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

	 
	private void showGlueKeyCommands(){
		/*
		while (srcInstance.participant == null ){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) { }
		}
	*/
		println();
		println("the following key commands are available...");
		println("   s  ->  send single task - simple, without context information");
		println("   S  ->                     with context information included");
		println("   m  ->  send multiple tasks (10) in a row");
		println("   f  ->  prepare and deliver subscriptions");
		println("   g  ->  retrieve all results waiting on the MessageBoard");
		println("   d  ->  delete all results on the MessageBoard for this client");
		println("   u  ->  disconnect");
		println("   r  ->  (re-) connect");
		println("   q  ->  cancel last task");
		println("   x  ->  exit");
		println();
		println("------------------------------------------------------------------");
		println("starting...");
	}


	// ---- events from processing ------------------------
	
	public void keyPressed(){
		_SHIFT = false; 
		_CTRL  = false;
		int pix ;
		
		
		if (key=='A'){
			mouseIsActive = false;
			// addParticles( mouseX,mouseY);
			// _testSelect(pix);
		}
		if (key=='a'){
			mouseIsActive = false;
			// addParticles(1);
		}
		if (key=='b'){
			mouseIsActive = false;
			// .splitParticle( selectedParticleIndex, this );
		}
		if (key=='d'){
			mouseIsActive = false;
			// deleteParticle( (int)(Math.random()*(repulsionField.getNumberOfParticles() )));
		}
		if (key=='r'){
			mouseIsActive = false;
			if (selectedParticleIndex>=0){
				// deleteParticle( selectedParticleIndex );
			}
			selectedParticleIndex = -1;
		}
		 
		if (key=='u'){
			selectedSet.clear();
			ellipses.clear();
			// unselectAllParticles(); 
		}         
		if (key=='p'){
			if (selectedSet.size()<=1){
				println("the selected set is too small.");
				return;
			}
			// String guid = repulsionField.getParticlesOfFiguratedSet( RepulsionFieldIntf.__SELECTION_FIGURE_MST, selectedSet, 1.7, 1.0, true );
				// thickness of skeleton inflation, endPointRatio for ellipsis construction, autoselect)
		}
		if (key=='h'){
			if (selectedSet.size()<=1){
				println("the selected set is too small.");
				return;
			}
			// .getParticlesOfFiguratedSet( RepulsionFieldIntf.__SELECTION_FIGURE_CONVEXHULL, selectedSet, 1, 1, true ); 
			// thickness = 1, >1, <1 , mode=1 -> area, 2->ring,  autoselect)

		}
		
		if (key=='t'){
			mouseIsActive = false;
			// .releaseShakeIt(3);
		}
		if (key=='T'){
			mouseIsActive = false;
			// .releaseShakeIt(8);
		}

		if (key=='m'){
			// .mobilityDecrease();
		}
		if (key=='M'){
			// .mobilityIncrease();
		}
		if (key=='b'){
			// .setAdaptiveBehavior(false);
		}
		if (key=='B'){
			// .setAdaptiveBehavior(true);
		}
		if (key=='f'){
			showMouseFeedback = !showMouseFeedback;  
		}
		
		if (key=='s'){
			// .selectionSizeDecrease(1,1);
		}
		if (key=='S'){
			// if the new selectionsize is larger than the buffers, the buffers will 
			// be recalculated (which can take some time, dependent on the size of the field, 
			// the size of the selection and the computer)
			// .selectionSizeIncrease(1,1);
		}
		
		if (key=='e'){
			// .exportCoordinates( RepulsionField._RFUSERDIR + "/data/coodinates.dat" );
		}
		
		if (keyCode==49){ // "+"
			// .getRqTester().increaseFrequency();
		} 
		if (keyCode==45){ // "-"
			// .getRqTester().decreaseFrequency(); 
		}
		
		if (keyCode==16){
			_SHIFT = true;
		}
		if (keyCode==17){
			_CTRL = true;
		}
		if (key=='x'){
			// .storeRepulsionField(); // also: with dedicated name outside of user-dir
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

			// requestGuid = repulsionField.getSurround( mouseX,mouseY,  1, true);
			 
		 }else{
			// requestGuid = repulsionField.selectParticleAt( mouseX, mouseY, true );
		 }
		 
		 if (_CTRL){
			// requestGuid = repulsionField.selectParticleAt( mouseX, mouseY, true );
		 }
		 mouseIsActive = true;
	}
	 
	 
	
	// ---- processing drawing loop -----------------------
	public void draw() {
		
		
		if (drawing){
			
			// repulsionField.update();
		
			background(0);

			sendUpdateTrigger() ;
			
			// drawParticles();
			 
		}
	}

	// ---- some output methods ---------------------------
	
	private void drawParticles(){
		
		// nbrParticles = repulsionField.getNumberOfParticles();
		
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
			// particles = getGraphParticles();
			/*
			// if ((index<0) || (index>particles.size()-1)){ return;}
			// particle = particles.get(index);
			
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
			*/
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

	
	private void sendUpdateTrigger(){
		SomFluidFactory sFactory ;
		SomFluidRequestPackageIntf requestObject ;
		
		
		if (triggerIsActive ){ // and connectionEstablished
			return;
		}
		
		triggerIsActive = true;
		// will be set to false only on next returning message
		
		// bare bones constructor... we do NOT need it here for a working SomFluid,
		// we just need it for getting the requestObject
		sfFactory = SomFluidFactory.get();	
		
		requestObject = sfFactory.createRequestPackage() ; 
     
		requestObject.setRequestConcern("Lattice::Display") ;
		
		// is wrapping the object into the Glue XML
		srcInstance.prepareData( requestObject , 0);

		// will send the message prepared before
		srcInstance.send(0);
	}
 
	 
}




/**
 * 
 * 
 */
class SomDisplaySrcInstance implements Runnable, ResultReceiverIntf{ 

	// 
	ParticipantFactory factory ;
	MessageBoardFactoryProperties factoryProperties ;
	ParticipantSourceIntf participant ;
	 
	
	
	// helper objects
	Thread iThrd; 
	PrintLog out = new PrintLog(2,true);
	
	
	public SomDisplaySrcInstance (){
		// we start the instance into a thread, which is independent from the PApplet-process
		(new Thread(this,"SrcInstance-main")).start() ;
	}	 
		
	public void run() {
		 create();
	}

	private void create(){
		 
		
		try{
			factoryProperties = new MessageBoardFactoryProperties()  ;
			
			// ...not yet functional 
			// factoryProperties.loadProperties( "D:/dev/java/data/test/mb/properties/source" );

			// defining that we are a client = participant, not the board
			factoryProperties.setTypeOfInstance( MessageBoardFactoryProperties.variant_SOURCE );
			// factoryProperties.setTypeOfInstance( MessageBoardFactoryProperties.variant_BSOURCE );
			
			// if this is empty, the user home directory will be taken <user-home>/mb/...
			factoryProperties.setHomePath( "D:/dev/java/data/SomFluid/mb/c/" ) ;
			
			factoryProperties.setProtocolId( MessageBoardFactoryProperties.protocolTCP ) ;
			
			factoryProperties.setRemoteInstancesFlag( false ) ; // false == only local addresses
			// factoryProperties.setProtocolId(MessageBoardFactoryProperties.protocolHREST);
			
			// VM dev box
			// factoryProperties.setHostAddressName("192.168.0.13", 7070) ;
			
			// dell box
			// factoryProperties.setHostAddressName("192.168.0.6", 7070) ;

			// linux susi box 
		    // factoryProperties.setHostAddressName("192.168.46.132", 7070) ;  
			
			
			// TODO: this... would turn the participants into some kind of integrating units       
			factoryProperties.setInstanceBivalence(false); //only for participants
			// factory.config().setSecondaryDocType("doc,*") ; // by default the same as the primary type
			// the message board will NOT relay the document back to the issuer !
			
			// ------------------------------------------------------------
			
			factory = new ParticipantFactory( factoryProperties , this ) ;

			// factory.config().setActiveReceiptsExchange( true ) ;
			factory.config().setName( "SomDisplay") ;
			
			// that has to be used by the producer of the requested data too !!!
			// there must be just a receptor defining are least one matching label in its own set
			// we could add further categories in order to organize routing of particular messages to particular clients
			factory.config().setDefaultDocType("somdisplay,somclass") ;
			
			// <=0 = Off; 1=perfect order , <n> groups of results are aligned, but not necessarily results inside the group 
			// alignment is crucial for parallel digestion of serial input, like sentences
			factory.config().setResultAlignment(1) ;

			// these properties have appropriate defaults, which reflect arrangements in local networks
			factory.config().setNumberOfConnectRetry(5) ; // default = 3, -1 = forever
			factory.config().setDelayBetweenConnectRetries(3950); // default = 3000
			factory.config().setDelayProgressionFactor(1.6) ; // note that there is maximum of 3 hours ! 
			
			// allow participants that their messages could be relayed
			factory.config().setGeneralTaskRelayFlag(true) ; // default=true
			
			// if true, only 1 instance of this type (here: SOURCE) will be registered on the current machine (even for http clients)
			// note, that conflicting IDs (requested from different machines) will be resolved
			factory.config().setInstanceAsUnique( true ) ; // default = true
			 
			// we may set a preferred id of this instance, conflicts are resolved by providing the next available id
			// on the same machine there will only be 1 unique instance of course !
			factory.config().setPreferredID(2528);
			

			// if true, the the client will buffer all results locally before finally passing them
			// to the surface layer; it is based on a compound trigger, organized by "setResultsCompletionMarker()"; 
			// such, an application client can get all results at once.
			// it provides an additional mechanism for aligning the results
			factory.config().setCollectResults(true);
			// if it switched OFF even subsequent to defining a trigger, the triggers will not be regarded at all

			
			// create the participant, which is either a SOURCE (as this instance here), or a RECEPTOR
			// ... creating and connecting in a single step
			participant = (ParticipantSourceIntf)factory.connect() ;
			
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	  

	// =============== local methods ===============

	public SomDisplaySrcInstance  prepareData( Object sfRequestObject , int contextmode){

		TaskPackage taskPackage;
		StringedObjects strobj = new StringedObjects();
		
		String filename, contentType = "rawdata";

		contentType = "doc" ;
		
		// could be any serializable object
		DataContainer dataobj=new DataContainer() ;
		
		// optional: TTL = time-to-live for this transaction...
		// without refresh by SOURCE or RECEPTOR, the transaction will be canceled and removed after the provided time span
		factory.setTaskTTL(25,"min");
		
		// creating a TaskPackage
		taskPackage = factory.getTaskPackage( contentType ) ;
		// note, that in case of UDP ONLY references should be transmitted,
		// UDP may cut large messages dependent on the settings/capabilities of the hardware router !!!
		// for large data sets to be included in the taskpackage use TCP
		
		
		String serialObjStr = strobj.encode(sfRequestObject );
		
		// the taskPackage may contain ANY kind of object, ...there are slots for primitive types
		taskPackage.getNums().add(99 ) ;
		taskPackage.setDataString( serialObjStr) ;
		
		// any user-based object format, of course, 
		dataobj.setObjFromFile(serialObjStr) ; // e.g. reading the file and representing it as byte[]
		
		taskPackage.setData(dataobj, "dataobj");
		
		taskPackage.setTypeOfProceedings("task"); // task = expressing expectation of some kind of result
		 							  // "travel" = one way pass through, no results will be returned
									  // "result" = the package returned to the SOURCE !
		
		 
		if (contextmode>=1){
			Context context = describeContextOfTaskMessage(2); // 1=item sets, 2=feature vectors 
			taskPackage.setContext( context );
		}
		
		int z=0;
		while ((participant==null) && (z<200)){
			out.delay(10); z++;
		}
		if (participant!=null){
			participant.setTask(taskPackage);
		}else{
			out.printErr(1, "initialization failed, no sending possible, instance stopped.");
			System.exit(0);
		}
		
		// resetting the optional parameters of the taskpackage factory.setTaskTTL(0,"min");
		return this;
	}
	
	 
	

	// =============== interfaced methods ===============
	
	// =============== local methods ===============
	
	// here we describe the pattern that will be exhibited to the context-matching process
	// this is NOT the subscription, but part of the message
	private Context describeContextOfTaskMessage( int mode  ){
		
		/* 
		  	context info is quite simple, either it is 
				- a whole-sale format like text or image,
				- an item set, or a 
				- feature vector
		 */ 
		
		Context context = new Context();
		 
		if (mode<=1){
			context.addDescribingParameters( "context iSet", new String[]{"k","y","x"} ,0); // 0 = minimum similarity suggested by the issuer
			// if this set should be interpreted as a feature vector (without specified values),
			// then we would have to set the data type to "feature vector" by:  setDataType( ContextInfra._DTYP_FVECTOR );
			
		}
		if (mode>=2){
			try {
				context.addDescribingParameters("context fVec", new String[]{"x","y","k"} , new double[]{0.6,0.2,0.8} , 0.6);
			} catch (Exception e) {
				 
				e.printStackTrace();
			}
		}
			// data are not considered in context matching, but they are transfered to the subscriber,
			// but only if the doc-type matches !
			// if false, the data will not be transferred to the subscriber
			context.setDisclosureAllowData( true );
			
			// if we allow data to be part of of the message relayed to the subscriber,
			// we could advise the MessageBoard to relay the task package ONLY 
			// if the context description matches a subscription
			context.setClientRelayByContext( true ); // default is false
			
			
			// plain name, without uid : if there are several instances of this code, 
			// they all will issue contexted messages using this name ("family" name)
			// the full name which includes a GUID will not be disclosed
			// if the (basic) name is visible, it can be used for context matching
			context.setDisclosureAllowName( true );
			context.setNameOfIssuer( factory.config().getInstanceName() ); 
		 
		
		return context;
	}

	// finally, here we will find the results 
	@Override
	public void resultsProvider( Vector<TaskPackage> resultpackages) {
		DataContainer dc;
		long tid ;
		String str, commentaryStr;
		
		out.print(3, "\n\rMsgSourceInstance :: SrcInstance :: resultsProvider()");
		
		for (int i=0;i<resultpackages.size();i++){
			
			tid = resultpackages.get(i).getTransactionID();
			
			commentaryStr = resultpackages.get(i).getDataString();
			
		 
			// of course, the RECEPTOR should have put a string into it
			// infra.common.DataContainer
			Object obj = resultpackages.get(i).getDataObj();
			dc = (DataContainer)obj ;
			str = (String)dc.getObjData();
			
			out.printErr(1, "\r\nResults for transaction id="+tid+", content : "+str +" ,  experiences : "+ commentaryStr+"\n");
			
		}// i ->
		
	}

	@Override
	public void currentStateMessage(String str) {
		
		out.printErr(4, "MsgSourceInstance :: SrcInstance :: currentStateMessage()");
		out.print(2,"Simple State Message as issued by RECEPTOR : "+str);
		
	}

	@SuppressWarnings("static-access")
	public void DEBUG_STOP(){
	
		try{
		
			Thread.currentThread().sleep(4000);
			disconnect();
			System.exit(0);
			
		}catch(Exception e){
			
		}
		
	
	}
	// =============== wrapped methods ===============
	
	
  

	public void cancelTask(){
		int cancelState;
		/*
		 *    canceling a task needs a transactionID,
		 *    if none is given, the last activated one will be chosen 
		 *    (acc. to option in factoryProperties, factory.config() ) 
		 */
		
		cancelState = participant.cancelTask();
		
		if (cancelState<0){
			// failure of canceling ;
		}
	}
	
	public void connect(){
		if (participant!=null){
			participant.connect();  // TODO block overlapping connect attempts
		} else{
			// factory.scheduleDisconnect() ;
		}
	}
	
	public void disconnect(){
		if (participant!=null){
			participant.disconnect() ;
		}
	}

	public SomDisplaySrcInstance send( int setMarker){
		
				
		// 3 = a parameter that influences the topology of routing the message to
		//     cascaded MessageBoards, the value describes the maximum number of hops
		participant.send(3);  
		
		// the completion marker provides the possibility to initialize a local client-side buffer
		// for results from distributed process. The application will see the results only after all
		// partial results have been received.
		
		// each transaction first will request an ID, and that response is fast...
		// so the chain of events is: (1) request a tid, (2) return to here, 
		// and (3) set the marker which indicates the last transaction in a row,
		// finally (4) transfer the data
		
		if (setMarker>0){
			setResultsCompletionMarker();
		}
		
		return this;
	}
	
	public void setResultsCompletionMarker(){
	    	
		// this takes the last transaction ID and will present the results only upon arrival of the respective transaction
		// this buffering is client-side !
		if (participant!=null){
			participant.setResultsCompletionMarker() ;
		}
	    	
	}
	  
	public void retrieve() {
		// retrieve ALL tasks on the MessageBoard associated with this instance without referring to a transaction ID
		// this works only for hibernated unique instances, since the full name is needed
		//      or for all tasks delivered within the same registration period
		// the client secret of the transaction will NOT be used
		participant.retrieveAll();  
		
	}

	public void delete() {
		// delete ALL tasks on the MessageBoard associated with this instance without referring to a transaction ID
		// this works only for hibernated unique instances, since the full name is needed, 
		//      or for all tasks delivered within the same registration period 
		
		participant.cancelAll();  
	}

	// this is the frontend label for dealing with subscriptions
	public void prepareSubscriptions(){
		FuturesIntf futures;
		
		futures = participant.getFutures();
		
		issueSimpleSubscription(futures) ;
		
		// issueContextSubscription(futures) ;
		
		out.delay(2000);
		
		// now we delete the first simple subscription
		Vector<Future> typedFutures;
		// first we get all subscriptions (=futures) of the simple type (no extensive contextual definitions)
		typedFutures = futures.getFuturesById( factory.getInstanceID(), FutureProperties._DD_SIMPLESET ) ; 
		// then we retrieve its guid and send the unpublish request to the MessageBoard 
		if (typedFutures.size()>0){
			
			// futures.unpublish( typedFutures.get(0).getGuid() );
			
			int n;
			n = futures.size(factory.getInstanceID()) ;
			// out.print(2, "Subscription has been unpublished, now there should be "+n+" subscription(s) active.");
		}
	}
	
	

	@SuppressWarnings("unused")
	private void issueFeatureVectorSubscription( FuturesIntf futures ){
	
		String subguid;
		int r;
		String[] features;
		double[] values ;
		
		FutureIntf subscription ;
		DistanceControlProperties distanceCtrl =null; 
		
		// we do not create the subscription like this... FutureIntf subscription = futures.create(distanceCtrl);
		// instead we request an instance of a subscription from the participant object 
		// (which is the "natural" owner of a subscription)
		subscription = participant.createFuture();
		
		
		// create a simple filter using label, criteria and a context object for defining the comparison, 
		// if context = null, exact match for all particles will be checked 
		
		 
		long iid = factory.getInstanceID() ;
		
		features = (new String[]{"x","s","a"});
		values = (new double[]{0.2, 0.8, 0.4}); // equal number of positions, overlaying ones will be truncated
		
		if (iid%2==0){
			features = (new String[]{"x","y","a"}); 
			values = (new double[]{0.6, 0.3, 0.5});
		}
		
		subscription.addProperty( "profile X", // label 
								  features,    // feature items (fields) of the vector
								  values,      // the numerical values
								  (new String[]{"x","m*"}), // optional: excluding features, suffix-wildcards are allowed here!
				  							   // overlaps with feature set will be removed	
				                  0.5, 0.7 ) ; // that's the minimal match threshold & a relative value for similarity of remaining profiles !
			
		//  adjusting the settings for the calculation of distance
			distanceCtrl = factory.getDistanceCtrlProperties() ;
			   distanceCtrl.setDistanceMethod( DistanceControlProperties.__distanceSPELA ) ;
			   distanceCtrl.setSurrogateDistance(0.11) ;
			   distanceCtrl.setMissingValueHandling( DistanceControlProperties.__MV_ByAdjSurrogateDistance ) ;
			   distanceCtrl.setItemSetContrastalpha(0.2) ;
			   
			subscription.setDistanceControlProperties(distanceCtrl) ;   
		
		subguid = futures.add( subscription ); // create an object from the data
		
		// ----------------------------------------------------------------------------------------------------
	
												out.print(2, "going to publish the subscription... ");
		r = futures.publish( subguid ); // add props.name
		// publishing transfers also the settings about the distance function that should be used by the MessageBoard
		// note that the distance function has to be known also by the MessageBoard! (custom functions can be easily registered there...)
		
			if (r<0){
				out.print(2, "publishing the simple subscription failed! (r="+r+")");
			}else{
				out.print(2, "simple subscription "+subguid+" has been published.");
			}
	}
	
	
	@SuppressWarnings("unused")
	private void issueContextSubscription( FuturesIntf futures) {
		String subguid;
		int r;
		
		  
		ContextIntf context;
		
		
		// FutureIntf subscription = futures.create();
		// not "direct", only through participant, in order to deal corectly with distance control properties
		FutureIntf subscription = participant.createFuture();
		
		
		// subscription may contain complex context definitions
		// create a compound contextual filter; we need at least items, mixes, and weights
		
		context = subscription.getContext();
		
		context.setPersonalization( "source","") ;
		
		// Items
		context.addParameters( "keywords", (new String[]{"x","y","z","k"}) ,0.7);
		
		// amount of missing items, degree of thinning the item group, changing overall length, max distances between any two elements       
		context.addParameters( 0.15, 0.41, 3.2, 3); 
		 
		// weights, 
		context.addParameters( (new double[]{1.0, 0.8, 0.6, 0.65}));
		
		// 
		context.addParameters( ContextInfra._SET_MANDATORY, (new String[]{"x"})  );
		context.addParameters( ContextInfra._SET_EXCLUDING, (new String[]{"a","b"})  );
		
				
		subscription.addProperty( "", context );
		// only java for now: contexts are transferred as encoded objects and recreated by the MessageBoard 
		
		//-> marker that context !
		subguid = futures.add( subscription );
		
		r = futures.publish( subguid );
		
			if (r<0){
				out.print(2, "publishing the contextual subscription failed! (r="+r+")");
			}else{
				out.print(2, "contextual subscription "+subguid+" has been published.");
			}
		
	}
	
	private void issueSimpleSubscription( FuturesIntf futures ){
		String subguid;
		int r;
		
		
		// FutureIntf subscription = futures.create();
		FutureIntf subscription = participant.createFuture();
		
		// create a simple filter using label, criteria and a context object for defining the comparison, 
		// if context = null, exact match for all particles will be checked 
		 
		// 
		subscription.addProperty( "keywords", (new String[]{"x","y","k","m"}), (new String[]{"x"}), 0.7) ;
		subguid = futures.add( subscription ); // create an object from the data
		
		// ----------------------------------------------------------------------------------------------------

		
		// 
												out.print(2, "going to publish the subscription... ");
		r = futures.publish( subguid );
	 
			if (r<0){
				out.print(2, "publishing the simple subscription failed! (r="+r+")");
			}else{
				out.print(2, "simple subscription "+subguid+" has been published.");
			}
		
	}
	
 
	
	
}