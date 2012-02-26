package org.NooLab.somsprite;

import org.NooLab.somfluid.components.IndexedDistances;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.SpriteSettings;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;
 

/**
 * 
 * TODO: maintenance of a list of formulas func(a,b),
 *       creating a list through reflection and intf/method search
 * 
 */
public class SomSprite {

	DSom dSom ;
	SomDataObject somData;
	
	SomMapTable somMapTable ;
	
	SomSprite spriteMain ;
	SomSpriteProcess sprity;
	boolean spriteProcessIsRunning = false;
	
	ModelingSettings modelingSettings ;
	SpriteSettings spriteSettings ;
	
	Evaluator evaluator ;
	IndexedDistances candidates ;
	
	PrintLog out;
	
	// ========================================================================
	public SomSprite( DSom dsom, ModelingSettings modset) {
		
		dSom = dsom;
		
		modelingSettings = modset;
		spriteSettings = modelingSettings.getSpriteSettings() ;
		
		evaluator = new Evaluator(this);
		createFunctions() ;
		
		spriteMain = this;
		
		out = dSom.getOut() ;
	}
	// ========================================================================
	
	public void createFunctions( ExpressionsCatalog catalog){
		
	}
	
	private void createFunctions(){
		// note tht simple univariate scaling is handled elsewhere !!!
		evaluator.createFunction("add",        "a+b") ;
		evaluator.createFunction("mult",       "a*b") ;
		evaluator.createFunction("safeDiv",    "(a+0.01)/(b+0.01)") ;
		evaluator.createFunction("dRatio",     "1.0 + (a-b)/(2.0*(a+b))") ;
		evaluator.createFunction("logSum",     "log(a+b)") ;
		evaluator.createFunction("safeLogSum", "log(0.01 + a + b)") ;
		evaluator.createFunction("z-scale",    "") ;
		evaluator.createFunction("logRatio",   "log(0.001 + (1+a)/(1+b))") ;
		evaluator.createFunction("exp1", "") ;
		evaluator.createFunction("exp2", "") ;
		evaluator.createFunction("exp3", "") ;
		evaluator.createFunction("", "") ;
		evaluator.createFunction("", "") ;
		evaluator.createFunction("", "") ;
		evaluator.createFunction("", "") ;
		evaluator.createFunction("", "") ;
		evaluator.createFunction("", "") ;
		evaluator.createFunction("", "") ;
		
		// functions for time series analysis: cross-correlation for different windows and shifts,
		
	}
	
	
	public void acquireMapTable( SomMapTable smt) {
		String[] variables;
		
		somMapTable = new SomMapTable(smt) ;
	}



	public void startSpriteProcess(int wait) {
		// will wait for completion
		// start only if the tabe is ok
		
		startSpriteProcess();
		
		if (wait>0){
			while (spriteProcessIsRunning){
				out.delay(50) ;
			}
		}
	}



	public void startSpriteProcess() {
		// will immediately return after starting the process
		sprity = new SomSpriteProcess(); 
		sprity.startSprite() ;
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
			dScreener.go();
			candidates = dScreener.getListOfCandidatePairs();
			
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

	
	
}
