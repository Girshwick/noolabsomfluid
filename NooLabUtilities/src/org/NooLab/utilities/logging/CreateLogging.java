package org.NooLab.utilities.logging;

import java.util.Vector;
 
import org.apache.log4j.* ; // Appender;

import java.lang.reflect.*;




public class CreateLogging {

	Logger _logger ;
	static Vector<Logger> loggers = new Vector<Logger>();
	/*
    static Logger loggerG = Logger.getLogger("com.gargoylesoftware");  
    static Logger loggerA = Logger.getLogger("org.apache"); 
    static Logger loggerP = Logger.getLogger("com.NooLab.xmlrpc.extensions.PConnector");
    static Logger loggerR = Logger.getRootLogger();
    */
	
    static Appender logAppender;
	static public Level debugLevel = Level.ERROR;//  Level.INFO;

	/*
	defineLogging( loggerG );
	defineLogging( loggerA );
	defineLogging(loggerP);
	*/
	
	public CreateLogging(){
		
	}

	  
	/**
	 * 
	 * @param _loggingTargetIDString
	 */
	public  void setLogging( String _loggingTargetIDString){
		setLogging( _loggingTargetIDString, debugLevel );
	}	
		 
		
	public  void setLogging( String _loggingTargetIDString,  Level llevel ){
		
		_logger = Logger.getLogger(_loggingTargetIDString);
		loggers.add(_logger) ;
		
		_logger.setLevel(llevel) ;
		defineLogging(_logger);

		/* this works only for standard java logging
	    for (Handler handler : _logger.getParent().getHandlers()) {
	        // Find the console handler
	        if (handler.getClass().equals(java.util.logging.ConsoleHandler.class)) {
	            // set level to SEVERE.  
				if (out.getPrintlevel() <= 1) {
					handler.setLevel(Level.ERROR);
				} else {
					if (out.getPrintlevel() <= 2) {
						handler.setLevel(Level.WARNING);
					} else {
						handler.setLevel(Level.FINER);
					}
				}
	        }
	    }
		*/
		
		java.util.logging.Level javaLevel = java.util.logging.Level.SEVERE ;
		
		if ( llevel.isGreaterOrEqual(Level.DEBUG)){ javaLevel = java.util.logging.Level.FINEST ; }
		if ( llevel.isGreaterOrEqual(Level.INFO)) { javaLevel = java.util.logging.Level.INFO ; }
		if ( llevel.isGreaterOrEqual(Level.ERROR)){ javaLevel = java.util.logging.Level.SEVERE ; }

		// some won't switch off, like restlet ...
		java.util.logging.Logger loclogger = java.util.logging.Logger.getLogger(_loggingTargetIDString);
	    for (java.util.logging.Handler handler : loclogger.getParent().getHandlers()) {
	        // Find the console handler
	        if (handler.getClass().equals(java.util.logging.ConsoleHandler.class)) {
	            // set level to SEVERE. We could disable it completely with 
	            // a custom filter but this is good enough.
	        	
	            handler.setLevel( javaLevel );
	        }
	    }
	    
	}
	
	@SuppressWarnings("unused")
	private static void defineLoggingToFile( Logger  _logger){
		
	}
	
	private static void defineLogging( Logger  _logger){
	
		// about logging: http://scherer-it.ch/opensource/log4j.html
		//              http://www.torsten-horn.de/techdocs/java-log4j.htm 
	
	
	    // Logger  myLogger = Logger.getLogger("org.apache");
	    // _logger.setLevel(Level.ERROR);
	    _logger.setLevel( debugLevel ) ; // Level.ALL);  
	            
	    // Define Appender     
	    logAppender = new ConsoleAppender(new SimpleLayout());  
	    
	    // logAppender.setErrorHandler(arg0);
	    
	    //myAppender.setLayout(new SimpleLayout());  
	    _logger.addAppender(logAppender);  	    	   
	           
	    _logger.setLevel( debugLevel ) ;
	    
	    
	
	         // other logAppender.s
	         // # Konfiguration der Log-Datei
	         //  log4j.appender.file=org.apache.log4j.RollingFileAppender
	         //  log4j.appender.file.File=c:/temp/log4j.log
	
		  
	}


	/* Logger logger = LoggerFactory.getLogger(org.apache.ftpserver.FtpServer.class);
    logger.info("Hello World");
    
    
    String str = logger.ROOT_LOGGER_NAME;
    logger = LoggerFactory.getLogger("ROOT");
  
	if (loggers.size()>0){
		_logger = Logger.getRootLogger() ;
		_logger.removeAllAppenders();
	}
	 */
	
	public void controlSL4J(Object main){
		
		String str, intfName;
		int mint;
		
		Class<?> sclass, loadedclass ;
		ClassLoader classLoader; 
		Method[] mm;
		Method m;
		Class[] pcs ;
		Class<?>[] cc ;
		Field[] ff ;
		Field f;
		Class<?> ftyp ;
		
		sclass = main.getClass() ;
		
		classLoader = sclass.getClassLoader();
		
		try {
			
			 
			loadedclass = classLoader.loadClass("org.apache.ftpserver.impl.DefaultFtpServer") ;
			mm = loadedclass.getMethods();
			cc = loadedclass.getDeclaredClasses();
			ff = loadedclass.getDeclaredFields() ;
			
			for (int i=0;i<mm.length;i++){
				m = mm[i] ;
			}

			for (int i=0;i<cc.length;i++){
				 
			}
			// private final Logger LOG = LoggerFactory.getLogger(NioListener.class);
			// private final org.slf4j.Logger org.apache.ftpserver.impl.DefaultFtpServer.LOG
			for (int i = 0; i < ff.length; i++) {
				str = ff[i].getName();
				if (str.contains("LOG")) {
					f = ff[i];
					ftyp = f.getType();
					// = interface org.slf4j.Logger
					intfName = ftyp.getName();

					f.setAccessible(true);

					mint = f.getModifiers(); //18

					Field modifiersField;
					try {

						modifiersField = Field.class.getDeclaredField("modifiers");

						modifiersField.setAccessible(true);

						modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

						//f.set(null, Modifier.PUBLIC);
						// f.set(obj, value);
						
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			/*
			 * pcs = getParameterClasses(parameters); sclass = scope.getClass();
			 * 
			 * method = sclass.getMethod( methodName, pcs );
			 */
		} catch (ClassNotFoundException e) {
			 
			e.printStackTrace();
		}
		 
	}
	public static Level getDebugLevel() {
		return debugLevel;
	}


	/**
	 * 
	 * access to the constants is via static variable Level, like so: Level.INFO
	 * @param severe
	 */
	public static void setDebugLevel( Level llevel) {
		CreateLogging.debugLevel = llevel;
	}
	
	public static void setDebugLevel( int intLevel) {
		Level debuglevel= Level.OFF ;
		if (intLevel==0){
			debuglevel= Level.OFF; 
		}else{
			debuglevel= Level.ERROR;// .INFO; 
		}
		CreateLogging.debugLevel = debuglevel;
	} 	
}
