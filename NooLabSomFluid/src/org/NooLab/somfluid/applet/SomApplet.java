package org.NooLab.somfluid.applet;



import processing.core.PApplet;


import org.NooLab.somfluid.app.up.IniProperties;
import org.NooLab.somfluid.app.up.SomFluidStartup;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;




public class SomApplet extends PApplet{

	private static final long serialVersionUID = -1206243743183135630L;

	AstorSomInstance astorInstance ;
	String sourceForProperties = "" ;
	PApplet applet;

	public SomApplet(){
		super();
	}
	
	public PApplet getInstance(){
		applet = this;
		
		//init();
		
		// applet.setVisible(true) ;
		PApplet.main( new String[] { "org.NooLab.docserver.applet.DocumentObserverApplet"});
		
		PrintLog.Delay(200) ;
		
		
		return this;
	}

	
	public void setup(){
		 
		applet = this;
		background( 208,188,188);
		 
		size(220,200);
		
		// applet.setLocation(50, 50) ;
		setLocation( 50, 50 );
		if (frame!=null){
		frame.setLocation( 50, 50 ); 
		frame.setTitle(" Document Observer") ;
		}
		
		try {
			SomFluidStartup.setApplicationID("DocServer",this.getClass()); 
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		  
		showKeyCommands();
		
		
		draw();
		
		// this.frame.setTitle("DocumentObserver") ;
		// start this in its own thread...
		
		// use LogControl to control output of log messages
		LogControl.globeScope = 2; // 1=local definitions  default = 2
		LogControl.Level = 1 ;     // the larger the more detailed logging... (could be really verbose!)
  
		

	}
	
	public void draw(){
		background( 218,228,238);
		if (frame!=null){
			frame.setLocation( 50, 50 ); 
		}
		this.noLoop();
	}

	private void showKeyCommands(){

		println();
		println("Welcome to DocumentObserver!\n");
		println("the following key commands are available for minimalistic user-based control...");
		println();
		println("   c  ->  start collecting file names into a persistent todo list ");
		println("   s  ->  start observation ");
		println();
		println("   a  ->  add folder to list of observations ");
		println("   r  ->  exclude folder from list of observations ");
		println("   e  ->  edit configuration file"); // should use an xml editor
		println();
		println("   D  ->  clear documents table  ");
		println("   d  ->  start database server  ");
		println();
		println("   n  ->  create a new (sub-)project in the current project space, extending the last corpus used  ");
		println("   o  ->  open existing project for activating previous settings ");
		println();
		println("   p  ->  select a different base folder (=project space) for all subsequent projects (very first step!) ");
		println("   P  ->  create a new project space  ");
		println();
		println("   x  ->  exit");
		println();
		println("------------------------------------------------------------------");
		println();

		_showCurrentInputSettings();
	}
	
	private void _showCurrentInputSettings(){
		String qs="";
		 
		println("current project : "+ SomFluidStartup.getLastProjectName());
		
		{
		println("config source   : "+ SomFluidStartup.getLastDataSet() ) ;
		}

	}
	
	public void keyPressed() {

		 
	}
	
	
	private String prepareEngines(){
		
		boolean rB;
		String cfg="" ;
		
		
		if (IniProperties.lastProjectName.length()==0){
			try {
				// this is just a config file in the respective directory
				SomFluidStartup.selectActiveProject(); // project is the directory
				rB=true;
			} catch (Exception e) {
				rB=false;
			}
		}else{
			rB=true;
		}
		String prj = SomFluidStartup.getLastProjectName() ;
		
		// datasrc = DocoServStartup.introduceDataSet("") ;
		// "docoserv-config.xml"
				
		cfg = SomFluidStartup.getLastDataSet();

		if ((cfg==null) || (cfg.length()==0)){
			
			 
			cfg = SomFluidStartup.getLastDataSet();
		}

		return cfg;
	}
	
	
	/**
	 * 
	 * @param somtype 0=default: simple, single run; 1=som optimizer
	 * @param lastProjectName2 
	 */
	private void startEngines( int worktype ){
		 
	}
	
	private void openProject(){
		
		 
	}

	private void clearDataBaseTable( String tablename){
		
	}
	
	
}


class AstorSomInstance implements   Runnable 
							  				// for messages from the engine to the outer application layers like this module
											 {

	
	public AstorSomInstance(){
		
	}
	
	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}


	
}