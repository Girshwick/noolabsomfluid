package org.NooLab.somfluid.env.communication;

import java.util.Vector;

import org.NooLab.compare.utilities.math.DistanceControlProperties;
import org.NooLab.glue.common.DataContainer;
import org.NooLab.glue.components.MessageBoardFactoryProperties;
import org.NooLab.glue.components.pkg.TaskPackage;
import org.NooLab.glue.instances.ParticipantFactory;
import org.NooLab.glue.instances.ParticipantReceptorIntf;
import org.NooLab.glue.instances.SubscribersIntf;
import org.NooLab.glue.subscription.Future;
import org.NooLab.glue.subscription.FutureIntf;
import org.NooLab.glue.subscription.FuturesIntf;
import org.NooLab.glue.subscription.context.Context;
import org.NooLab.glue.subscription.context.ContextInfra;
import org.NooLab.glue.subscription.context.ContextIntf;
import org.NooLab.glue.subscription.publisher.SubscriptionPublisher;
import org.NooLab.glue.transaction.Transaction;
import org.NooLab.utilities.logging.PrintLog;


public class GlueReceptorInstance implements Runnable, SubscribersIntf{

    
	// =================================

	// object references ..............
	ParticipantFactory factory ;
	MessageBoardFactoryProperties factoryProperties ;
	// this is one of two differences between sources and receptors: just use another interface
	ParticipantReceptorIntf participant ;

	GlueBindings glueBindings ;
	// main variables / properties ....
	long transactID=-1;
	
	
	// volatile variables .............
	
	boolean connected=false;
	boolean contextMarking = false;
	String[] startupArguments ;
	
	// helper objects
	Thread iThrd; 
	PrintLog out = new PrintLog(3,true);
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public GlueReceptorInstance( GlueBindings binding ){
		glueBindings = binding;
		init(null);
	}
	
	
	private void init(String[] startuparguments ){
		
		out.setPrefix("[GLUE-SF-RC]");
		
		// we start then instance into a thread, which is independent from the PApplet
		(new Thread(this,"somFluidGlueReceptor")).start() ;
	}	 
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	
	public void run() {
		 create();
	}


	
	private void create(){
	 
		try{

			String str = ClassLoader.getSystemClassLoader().getResource(".").getPath();
			str = GlueReceptorInstance.class.getClassLoader().getResource(".").getPath();

			factoryProperties = new MessageBoardFactoryProperties();

			factoryProperties.setStartupArguments(startupArguments); 
			
			
			// define the type of the client... IMPORTANT : do this first !!! 
			factoryProperties.setTypeOfInstance(MessageBoardFactoryProperties.variant_RECEPTOR);

			factoryProperties.setHomePath("D:/dev/java/data/somfluid/mb/s/");
			
			factoryProperties.setProtocolId(MessageBoardFactoryProperties.protocolTCP);
			
			// false: only local addresses
			factoryProperties.setRemoteInstancesFlag(false);
			// factoryProperties.setProtocolId(MessageBoardFactoryProperties.protocolHREST);

			factoryProperties.setHostAddressName("192.168.0.8", 7070) ;
			// for susi linux box
			// factoryProperties.setHostAddressName("192.168.46.132", 7070) ; 
			

			// ------------------------------------------------------------
			factory = new ParticipantFactory(factoryProperties, this);

			factory.config().setNumberOfConnectRetry(6) ;
			
			factory.config().setActiveReceiptsExchange(true);
			factory.config().setInstanceAsUnique(true);   // TODO: difficulties to fall back from unique to pool ...
			
			factory.config().setName("somfluidProcessor");
			factory.config().setDefaultDocType("somdisplay,somclass");

			// factory.config().setPreferredID(2601); 
			// this overrules the previous call "setInstanceAsUnique(false)"
			
			participant = (ParticipantReceptorIntf) (factory.getInstance());

			participant.connect();
			/**
			 * if we are running in httpc-mode, upon connect a poll for
			 * resources will be initiated, such that the data then could be downloaded !
			 */
			connected = true;
			prepareSubscriptions(-1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	  
	// this is used by the application object, which is hosting this instance (linkage to GUI) 
	public ParticipantReceptorIntf getInstance(){
		return participant;
	}

	// =============== interfaced methods ===============
	
	@Override
	public void interControlMessage(String str) {

		out.printErr(2, "RecInstance :: interControlMessage()\n\r"+ str) ;
	}

	@Override
	public void handlingControlRequest(long transactionID, String controlName) {

		if (controlName.contentEquals("cancel")){
			out.printErr(2, "RECEPTOR: Handling cancel request...") ;
		}
		
		
	}

	
	
	
	@Override
	public void taskProvider(TaskPackage taskpackage) {
		 
		out.printErr(4, "MsgReceptorInstance :: RecInstance :: resultsProvider()");	
		
		 
		_digestingReceivedMsg( taskpackage ) ;
		
	}
	
	
	
	// =============== local methods ===============
	
	private void _digestingReceivedMsg( TaskPackage taskPackage ){
		
		int state=0;
		TaskPackage resultPackage ;
		Transaction mTransact;
		DataContainer dataobj, dataOut;
		String str, ctype ;
		long tid=0;
		
		
												out.print(5,"\nRECEPTOR is working on provided task object...\n");
	
		if (taskPackage==null){
		
			// some message ...
			return ;
		}
		
		
		
		// the TaskPackage contains the complete mirrored transaction, as it is known by 
		// the MessageReceptor after receiving the message
		// we need this in order to reply using the correct reference, then the MessageBoard can
		// return the reply to the correct SOURCE
		mTransact = taskPackage.getTransactionMirror() ;
		transactID = mTransact.getTransactionID(); // taskPackage.getTransactionID() ;<- this one i1 1 too low ???
		

		
		dataobj = (DataContainer) taskPackage.getDataObj() ;
		
		ctype = taskPackage.getContentType() ;
		str = taskPackage.getDataString() ;
												    out.print(2,"\nRECEPTOR is working on provided task object...\n");
	
												    if (mTransact!=null){
												    	tid= mTransact.getTransactionID();
												    }
													out.print(2,"transaction ID of task (type of content: "+ctype+") : "+ tid ) ;
													out.print(2,"content of task data :\n               "+ str) ;
													out.print(2,"     ... simulating calculation time") ;
	    out.delay(1000);	
	 // we may also send simple state messages
	    participant.startProcessNotification(tid);// this is the simulation of a working process
		//  via interface to ReceptorParticipant
	    
		
		if (transactID%2==0){
			out.delay(2000);
		}
		if (transactID%3==0){
			out.delay(3000);
		}
		if (transactID%4==0){
			out.delay(4000);
		}
		
		state = participant.getTransactionControlState() ;
		
		resultPackage = participant.getResultPackage();
		
		 
		if (state == Transaction.TS_TASK_CANCELED){
 
			resultPackage.setTransactionID( mTransact.getTransactionID() );
			resultPackage.setTypeOfProceedings("stop::code="+Transaction.TS_ERR_STOPPED);
			
			resultPackage.setStateFlag( Transaction.TS_TASK_CANCELED );
			// 
			out.print(2,"     ... calculations canceled.") ;

			return;
		} 
		 
		if (state != Transaction.TS_TASK_CANCELED){
			// preparing resultPackage
			tid = mTransact.getTransactionID();
												
			resultPackage.setStateFlag(0);
			resultPackage.setTransactionID( tid );
			resultPackage.setTypeOfProceedings("result");
			
			resultPackage.setContentType(ctype);
			
			// 
			String processingResultStr = "String processed: "+ str.toUpperCase().replace("E", "I").replace("A", "I") ;
			
			dataOut = new DataContainer();
			dataOut.setObjData( processingResultStr ) ;
			resultPackage.setDataObj( dataOut ) ;
			resultPackage.setDataString("results of a funny processing...") ;
			
			resultPackage.setContext(null) ;
												out.print(2,"     ... calculations on transaction "+tid+" finished.") ;
												
			if (contextMarking){
				// creating a context, filling it, and also setting xml path: //context/datadisclose", "value")
				// if this has been allowed by the SOURCE
				// the source also determines by its transaction the style of the context.
				// other ways to control the behavior is through broadcasted control message
				Context context = createReceptorsContext();
				resultPackage.setContext(context) ;
			}
			
			// for transferring untyped data: resultPackage.setData(object,
			// identifier)
			// use identifier to indicate the cast of the object

			// resultPackage.setContentType("RESULT");
			// resultPackage is defined in super-class "MessageBoardParticipant"
		}
		
	}
	
	private Context createReceptorsContext(){
		Context context = new Context();
		
		
		return context;
	}
	
	
	public void activateContextmarker(){
		
		contextMarking = contextMarking==false; 
	}

  
 


	// this is the frontend label for dealing with subscriptions
	// it demonstrates how to create and publish subscriptions
	public void prepareSubscriptions( int subscriptType ){
		 
		// just an abbreviation...
		FuturesIntf futures = participant.getFutures(); 
		
		
		if ((subscriptType<=0) || (subscriptType>=3)){
			return;
		}
		
		issueSubscription( subscriptType, futures );
		
		
		
	}
	
	public void removeAllSubscriptions(){
		Vector<Future> typedFutures;
		FuturesIntf futures = participant.getFutures(); 
		
		// first we get all subscriptions (=futures) 
		typedFutures = futures.getFuturesById( factory.getInstanceID() ) ;
		// to get futures by type, use additional parameters: FutureProperties._SIMPLESET, or FutureProperties._VECTOR ... 
		
		// then we retrieve its guid and send the unpublish request to the MessageBoard 
		if (typedFutures.size()>0){
			
			for (int i=0;i<futures.size();i++){
				futures.unpublish( typedFutures.get(0).getGuid() );
			}
			
			int n = futures.size(factory.getInstanceID()) ;
			out.print(2, "Subscription has been unpublished, now there should be "+n+" subscription(s) active.");
		}
		
	}
	
	public boolean isConnected() {
		return connected;
	}


	private void issueSubscription( int subscriptType, FuturesIntf futures){
	
		if (subscriptType<=1){
			issueSimpleItemSetSubscription(futures) ;  
		}
		 
		if (subscriptType==2){
			issueFeatureVectorSubscription(futures) ;  
		}
		
		if (subscriptType==3){
			issueContextSubscription(futures) ;  
		}
		if (subscriptType==4){
			issueFullTextSubscription(futures) ;  
		}
	}

	/**
	 * preparing an example of a subscription based on a simple set 
	 * 
	 * just defining some data and setting some parameters before publishing ...  
	 * 
	 */
	private void issueSimpleItemSetSubscription( FuturesIntf futures){
		
		long iid;
		String subguid ;
		String[] items, excludingitems;
		
		DistanceControlProperties distanceCtrl; 
		SubscriptionPublisher subpub ;
		 
		
		subpub = new SubscriptionPublisher( factory, futures );
		

		iid = factory.getInstanceID() ; // just for debug , to create different item sets automatically 

		items = (new String[]{"x","s","a"});       // this set of items is expected to match the context description 
		if (iid%2==0){
			items = (new String[]{"x","y","a"});   // alternative set
		}
		excludingitems = (new String[]{"x","m"}) ; // if present, these items would prevent a positive match
												   // "x" will be removed here, since it overlaps with items !
			
		subpub.setSubscriptionLabel( "keywords" ) ;     // the label of the subscription 
		subpub.setItemsOfFeatureSet( items );           // items of the defining set
		subpub.setItemsOfFeatureSet( excludingitems );  // excluding items, any contained in defining set will be removed
		subpub.setMinimalMatchThreshold( 0.5 );          // minimal match threshold ... 3 items : 0.33 < x < 0.66 -> at least 1 match
		 

		// if we want to use other than default settings for similarity (=distance) calculations,
		//  ... we even could to create a new instance (param=1), 
		distanceCtrl = subpub.getDistanceCtrl() ;
		// then set the field values as desired ...
		distanceCtrl.setItemSetContrastsigma(1.0) ;
		distanceCtrl.setItemSetContrastalpha(0.2) ;
		distanceCtrl.setItemSetContrastbeta(0.4) ;
		
		 
		try {
			
			// the type of the description is determined by the called method (no parameter)
			subguid = subpub.publishPreparedIssueItemset();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void issueFeatureVectorSubscription( FuturesIntf futures){

		long iid;
		String subguid ;
		String[] features, excludingitems;
		 
		double[] values ;
		
		
		DistanceControlProperties distanceCtrl; 
		SubscriptionPublisher subpub ;
		
		
		subpub = new SubscriptionPublisher( factory,  futures ); 
		  
		iid = factory.getInstanceID() ; // just for debug , to create different item sets automatically 

		features = (new String[]{"e","a","x"});       // this set of items is expected to match the context description 
		values = (new double[]{0.51, 0.43, 0.32});    // equal number of positions, overlaying ones will be truncated
		
		if (iid%2==0){
			features = (new String[]{"a","y","x"});   // alternative set, choice based on id of instance
			values = (new double[]{0.4, 0.2, 0.5});
		}
		 
		
		excludingitems = (new String[]{"x","m"}) ;    // if present, these items would prevent a positive match
												      // "x" will be removed here, since it overlaps with items !
			
		subpub.setSubscriptionLabel( "profile X" ) ;  // the label of the subscription
		subpub.setItemsOfFeatureSet( features );      // items of the feature vector
		subpub.setFeatureValues(values);
		subpub.setExcludingItems( excludingitems );   // excluding items, any contained in defining set will be removed
		subpub.setMinimalMatchThreshold( 0.5 );       // minimal match threshold ... 3 items : 0.33 < x < 0.66 -> at least 1 match
		subpub.setMinimalSimilarityThreshold( 0.87 );

		distanceCtrl = subpub.getDistanceCtrl() ;
		distanceCtrl.setDistanceMethod( DistanceControlProperties.__distanceSPELA ) ;  
		distanceCtrl.setSurrogateDistance(0.11) ;
		 
		
		try {
			
			// the type of the description is determined by the called method (no parameter)
			// adding the subscription to the futures
			subguid = subpub.publishPreparedIssueFeatureVector();

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	 
	}
  
	
	@SuppressWarnings("unused")
	private void issueContextSubscription( FuturesIntf futures) {
		String subguid;
		int r;
		
		  
		ContextIntf context;
		
		
		// FutureIntf subscription = futures.create();
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
		
				
		subscription.addProperty( "context H", context );
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
	  
	private void issueFullTextSubscription( FuturesIntf futures ){
		String subguid, fulltext="";
		int r;
		String[] items;
		
		FutureIntf subscription ;
		DistanceControlProperties distanceCtrl =null; 
		
		// we do not create the subscription like this... FutureIntf subscription = futures.create(distanceCtrl);
		// instead we request an instance of a subscription from the participant object 
		// (which is the "natural" owner of a subscription)
		subscription = participant.createFuture();
		
		
		// create a simple filter using label, criteria and a context object for defining the comparison, 
		// if context = null, exact match for all particles will be checked 
		 
		subscription.addProperty( 	"fulltext Z",               // label 
								    fulltext,   				// items of the defining set
									(new String[]{"x","m"}),    // excluding items, any contained in defining set will be removed
									0.5, "") ;                      // minimal match threshold ... >0.33 <0.66 -> at least 1 match
		
		// if we want to use other than default settings for similarity (=distance) calculations,
		// we get a grip onto the object (... we even could to create a new instance (param=1))... 
			distanceCtrl = factory.getDistanceCtrlProperties() ;
		// ... then set the field values as desired ...
			distanceCtrl.setItemSetContrastalpha(0.2) ;
			distanceCtrl.setItemSetContrastbeta(0.4) ;
			
		 
		subguid = futures.add( subscription ); // create an object from the data
		
		// ----------------------------------------------------------------------------------------------------

												out.print(2, "going to publish the subscription... ");
		r = futures.publish( subguid );
		
			if (r<0){
				out.print(2, "publishing the simple subscription failed! (r="+r+")");
			}else{
				out.print(2, "simple subscription "+subguid+" has been published.");
			}
		
	}
	
 
	
}
