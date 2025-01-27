package org.NooLab.somsprite;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.SpriteSettings;

import org.NooLab.somtransform.CandidateTransformation;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.ArrUtilities;
import org.apache.commons.collections.CollectionUtils;
 

/**
 * 
 * TODO: maintenance of a list of formulas func(a,b) that is provided by the library "jep"
 *       creating a list through reflection and intf/method search
 *       
 * This class organizes the sprite process for a given table;
 * 
 * Here, some default functions are declared (such as "a+b") upon construction 
 * by invoking "evaluator.createFunction("", ...)";
 * 
 * The functions all have two variates;
 * 
 * The user then has to call "startSpriteProcess()", which may wait (non-blocking) for the results
 * or will return immediately;
 * the second alternative needs to provide a callback via the interface ProcessCompletionMsgIntf,
 * which the caller has to implement 
 * 
 * 
 * TODO:
 *       - Mahalanobis (taking from JavaML)
 *       - KullBackLeibler divergence if there are enough data
 *       - interpreting columns as rows, then applying our distance measure
 * 
 */
public class SomSprite {

	// DSom dSom ;
	SomTransformer transformer;
	
	SomDataObject somData;
	
	SomMapTable somMapTable ;
	
	ProcessCompletionMsgIntf msgOnCompletion;
	
	SomSprite spriteMain ;
	SomSpriteProcess sprity;
	boolean spriteProcessIsRunning = false;
	
	protected SomFluidProperties sfProperties;
	ModelingSettings modelingSettings ;
	SpriteSettings spriteSettings ;
	
	Evaluator evaluator ;
	
	// ArrayList<AnalyticFunctionSpriteImprovement> candidates = new ArrayList<AnalyticFunctionSpriteImprovement>();
	AnalyticFunctionTransformationsIntf candidates = new AnalyticFunctionTransformations();
	
	ArrayList<AnalyticFunctionSpriteImprovement> previousProposals = new ArrayList<AnalyticFunctionSpriteImprovement>();
	
	ArrUtilities arrutil = new ArrUtilities();
	PrintLog out = new PrintLog(2,true) ;
	
	// ========================================================================
	public SomSprite( SomDataObject somDataObj, SomTransformer transformer, SomFluidProperties sfProperties ){
		
		this.transformer = transformer;
		this.sfProperties = sfProperties;
		
		out.setPrefix("[SomFluid-sprite]");
		
		modelingSettings = sfProperties.getModelingSettings() ;
	 
		spriteSettings = modelingSettings.getSpriteSettings() ;
		
		evaluator = new Evaluator(this);
		createFunctions(evaluator) ;
		
		spriteMain = this;
		
		somData = somDataObj;
		
	}
	// ========================================================================
	
	public void createFunctions( ExpressionsCatalog catalog){
		
	}
	
	protected void createFunctions(Evaluator evaluator){
		FunctionCohortParameters cohortParameters ;
		
		
		// note that simple univariate scaling is handled elsewhere !!!
		evaluator.createFunction("add",        "a+b") ;
		evaluator.createFunction("mult",       "a*b") ;
		evaluator.createFunction("safeDiv",    "(a+0.01)/(b+0.01)") ;
		evaluator.createFunction("dRatio",     "1.0 + (a-b)/(2.0*(a+b))") ;
		evaluator.createFunction("logSum",     "log(a+b)") ;
		evaluator.createFunction("safeLogSum", "log(0.01 + a + b)") ;
		evaluator.createFunction("z-scale",    "") ;
		evaluator.createFunction("logRatio",   "log(0.001 + (1+a)/(1+b))") ;
		evaluator.createFunction("ellips", "(1+a-b)/(1+a+b)") ;
		
		          cohortParameters = new FunctionCohortParameters("k",0.001,1.0,10,"log");
		evaluator.createFunction("foldedRatio", "abs(a-k*b)/(1+a+b)", cohortParameters) ;
		evaluator.createFunction("", "") ;

		evaluator.createFunction("exp1", "") ;
		evaluator.createFunction("exp2", "") ;
		evaluator.createFunction("exp3", "") ;
		evaluator.createFunction("sqdiv1", "sqrt((a+1)/(b+1))") ;
		
		// "range" is the keyword for a screening parameter that runs through an intervall
		// the syntax is:   [intervall:lo,hi], steps,type(lin,log)
		          cohortParameters = new FunctionCohortParameters("k",0.001,1.0,10,"log");
		evaluator.createFunction("sqdiv2", "log(k + sqrt((a+1)/(b+1)))", cohortParameters) ;
		evaluator.createFunction("sqrtmult", "sqrt(a*b)") ;
		evaluator.createFunction("spin1", "a / (sqrt(b)) ") ;
		evaluator.createFunction("spin2", "(a+b)/((a-b)*(a-b))") ;
		evaluator.createFunction("spin3", "log(a/sqrt(0.01 + 0.9*b))") ;
		evaluator.createFunction("sinhyp", "sinh(a/b)") ;
		
		/*
		evaluator.createCompundFunction("tanhyp1", "v", "v = tanh(log(a*b)) ; log(1/v)", new double[]{-1.0,0.0}) ;
		evaluator.createCompundFunction("tanhyp2", "v", "v = tanh(a+b) ; log(1/(1+v))" , new double[]{-1.0,0.0}) ;
		evaluator.createCompundFunction("tanhyp3", "v", "v = tanh( abs(log(a*b))) ; log( abs(1/(1+v)))" , new double[]{-1.0,0.0}) ;
		*/
		
		evaluator.createFunction("tanhyp4", "tanh( ((a*b)-3.2)/6.4 )") ;
		evaluator.createFunction("log1", "") ; // value1 * log10( 1 + value2 )
		evaluator.createFunction("", "spin(a,b)/ellips(a,b)") ; 
		evaluator.createFunction("", "") ; // expr_result :=  value1 * abs(log10( value2 ))
		evaluator.createFunction("", "") ; // expr_result :=  abs(log10( value1 + value2 ))
		evaluator.createFunction("", "") ; // expr_result :=  log10( value1 * value2 )
		evaluator.createFunction("", "") ; // expr_result :=  abs(value1 * log10( value2 ))
		evaluator.createFunction("", "") ;
		evaluator.createFunction("", "") ;
		evaluator.createFunction("Cauchy1", "1.0/(a + sqrt( b ))") ; //  1/(value1 + sqrt( value2 ) )
		evaluator.createFunction("Cauchy2", "log(1.0/(a + sqrt( b ) ))") ; // log10(1/(value1 + sqrt( value2 ) ))
		evaluator.createFunction("Cauchy3", "log(1.0+a/( sqrt( b ) ))") ; // log10(1+value1/( sqrt( value2 ) ))
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("logist1", "(1 / ( 1.0 + ( 1.0 / exp(3 * ( a - b )) ) ))^2") ; // sqr(1 / ( 1 + ( 1 / exp(3 * ( value1 - value2 )) ) ))
		// evaluator.createFunction("logist2", "1 / ( 1 + ( 1 / exp(3 * ( log(1+ max( 10,(a / b) ))) ) )") ; // 1 / ( 1 + ( 1 / exp(3 * ( log(1+ max( 10,(value1 / value2) ))) ) )
		// so far, jep2 does not comprise max,min function
		evaluator.createFunction("logist2", "1 / ( 1 + ( 1 / exp(3 * ( log(1+ (if(a / b>10.0,10.0,a / b)) ))) ))") ; // 1 / ( 1 + ( 1 / exp(3 * ( log(1+ max( 10,(value1 / value2) ))) ) )
		       
		
				  cohortParameters = new FunctionCohortParameters("k",0.001,1.0,10,"log");
		evaluator.createFunction("logist3", "1 / ( 1 + ( 1 / exp(3 * ( log(1+ (if(a /( k*b)>10,10,a /( k*b)) ))) ) ))",cohortParameters) ; 
		
		evaluator.createFunction("logist4", "pow( a, ( 1 / (1 - b) )  )") ; // power( value1, ( 1 / (1 - value2) )  )
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		evaluator.createFunction("", "") ; // 
		/*
		evaluator.createFunction("max", "if(a>b,a,b)") ; //
		evaluator.createFunction("min", "if(a<b,a,b)") ; //
		evaluator.createServiceFunction("notZero", "(a != 0)") ; //
		evaluator.createServiceFunction("largerZero", "(a > 0)") ; //
		*/
		// functions for time series analysis: cross-correlation for different windows and shifts,
		/*
		 *  nesting: 
		 *  1. check the list of all expressionNames, 
		 *  2. extract a sequence with matching number of brackets,
		 *  3. replace by a new symbol, and define the extracted part as preceding expression e.g. v1 = <expression for name>
		 *  4. repeat until no expressoinName is present any more 
		 *  
		 */
	}

	 
   
	
	public void acquireMapTable( SomMapTable smt) {
		
		somMapTable = new SomMapTable(smt) ;
	}



	public int startSpriteProcess(int wait) {
		// will wait for completion
		// start only if the table is ok
		
		if (spriteProcessIsRunning){
			return -3;
		}
		
		startSpriteProcess( null , wait);
		
		return 0;
	}



	public void startSpriteProcess( ProcessCompletionMsgIntf msgOnCompletion ,int wait) {
		// will immediately return after starting the process
		
		if (msgOnCompletion!=null){
			this.msgOnCompletion = msgOnCompletion;
		}
		
		sprity = new SomSpriteProcess(); 
		sprity.startSprite() ;
		out.delay(10) ;
		
		while (spriteProcessIsRunning==false){
			out.delay(5) ;	
		}
		
		
		if ((wait>0) && (spriteProcessIsRunning)){
			out.print(2, "sprite process has been started...");
			
			while (spriteProcessIsRunning){
				out.delay(50) ;
			}
		}
		out.print(2, "sprite process has been completed.");

	}


	public void stopSpriteProcess(){
	
		spriteProcessIsRunning = false ;
	}
	
	class SomSpriteProcess implements Runnable{

		Thread spritThrd;
		
		public SomSpriteProcess(){
		
			spritThrd = new Thread(this,"spritThrd") ;
		}
		
		 
		public void startSprite(){
			spritThrd.start() ;
		}
		
		private void perform(){
			DependencyScreener dScreener ;
			
			dScreener = new DependencyScreener( spriteMain, somMapTable , evaluator);
			
			// it will also remove those items that have been imported as known ones...
			dScreener.go(); 
			
			
			candidates = dScreener.getListOfCandidatePairs();
			
			// send candidates into SomTransformer, they will be put just to a queue, 
			// but NOTHING will be changed regarding the transformations...  
			// implementation will be triggered by instances of SomHostIntf (such like ModelOptimizer)
			if ((transformer!=null) && (candidates.getItems().size()>0)){
				// TODO if some candidate transformations are already available, they will be filtered out over there
				transformer.perceiveCandidateTransformations(candidates,1) ;
				 
			}
			dScreener.close() ;
		}
		
		@Override
		public void run() {
			spriteProcessIsRunning = true;
			
			perform();
			
			
			spriteProcessIsRunning = false;
		}
		
		
	}

	// ------------------------------------------------------------------------

	public boolean isSpriteProcessIsRunning() {
		return spriteProcessIsRunning;
	}

	public void setSpriteProcessIsRunning(boolean spriteProcessIsRunning) {
		this.spriteProcessIsRunning = spriteProcessIsRunning;
	}

	public AnalyticFunctionTransformationsIntf getProposedCandidates() {
		return candidates;
	}

	/**
	 * @return the candidates
	 */
	public AnalyticFunctionTransformationsIntf getCandidates() {
		return candidates;
	}

	/**
	 * @return the somData
	 */
	public SomDataObject getSomData() {
		return somData;
	}

	/**
	 * @return the previousProposals
	 */
	public ArrayList<AnalyticFunctionSpriteImprovement> getPreviousProposals() {
		return previousProposals;
	}

	
	
	
	/**
	 * @param previousProposals the previousProposals to set
	 */
	public void setPreviousProposals( ArrayList<AnalyticFunctionSpriteImprovement> prevproposals) {
		
		try{
		
			if ((prevproposals!=null) && (prevproposals.size()>0)){
				previousProposals = new ArrayList<AnalyticFunctionSpriteImprovement>();
				
				for (int i=0;i<prevproposals.size();i++){
					previousProposals.add( new AnalyticFunctionSpriteImprovement(prevproposals.get(i)) ) ;
				}
			} // ?
		}catch(Exception e){
		}
		
	}
	
	public void addProposalsAsKnown( ArrayList<AnalyticFunctionSpriteImprovement> proposals) {
		
		boolean hb;
		
		try{
			if (previousProposals==null)previousProposals = new ArrayList<AnalyticFunctionSpriteImprovement>();

			if ((proposals!=null) && (proposals.size()>0)){
				
				for (int i=0;i<proposals.size();i++){
					
					hb = false;
				
					for (int k=0;k<previousProposals.size();k++){
						hb = (previousProposals.get(k).isEqual( proposals.get(i)));
						if (hb){
							break;
						}
					} // k-> 

					if (hb==false){
						previousProposals.add( new AnalyticFunctionSpriteImprovement(proposals.get(i)) ) ;
					}

				} // i->
				  
			} // ?
			
		}catch(Exception e){
		}
		
	}

	public boolean isProposalKnown( AnalyticFunctionSpriteImprovement fs) {
		boolean hb,rB=false;;
		
		if ((previousProposals==null) || (previousProposals.size()==0)){
			return rB;
		}
		
		for (int k=0;k<previousProposals.size();k++){
			
			hb = (previousProposals.get(k).isEqual( fs ));
			if (hb){
				rB=hb;
				break;
			}
		}
		
		return rB;
	}
	
	
}
