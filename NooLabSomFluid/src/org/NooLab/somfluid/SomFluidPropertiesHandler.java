package org.NooLab.somfluid;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.NooLab.field.FieldIntf;
import org.NooLab.itexx.storage.DataStreamProviderIntf;
import org.NooLab.itexx.storage.TexxDataBaseSettings;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.data.VariableSettingsHandler;
import org.NooLab.somfluid.data.VariableSettingsHandlerIntf;
import org.NooLab.somfluid.properties.AstorSettings;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.somtransform.SomFluidXMLHelper;
import org.NooLab.somtransform.algo.externals.AlgorithmPluginsLoader;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.ValuePair;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;

import com.jamesmurty.utils.XMLBuilder;



/**
 * 
 * note that there are a lot more of parameters that could be set for the SOM in order to exert full control,
 * e.g. in the context of commercial predictions;
 * yet, there are reasonable default values, and in the long run those parameters are 
 * adjusted autonomously anyway by the system itself  
	 
	 sfProperties.importTransformationParameterDefaults("standards.ini");

 * 
 */
public class SomFluidPropertiesHandler implements SomFluidPropertiesHandlerIntf{

	SomFluidProperties sfProperties;
	
	ModelingSettings ms;
	ClassificationSettings cs;
	PersistenceSettings ps;

	AstorSettings astorSettings ;
	TexxDataBaseSettings databaseSettings;
	
	VariableSettingsHandlerIntf variableSettings ;

	
	
	
	
	String algorithmsConfigPath ="" ;
	
	private String projectName="", dataSourceName="";
	String supervisedOnlineFolder="" ;
	int nodeCount = 3;
	int srctype = 1;
	int surrogateActivated = 0;
	
	private int numberOfSimulatedRecords;

	private int targetMode = -1;
	String startupTraceInfo ="" ; 
	
	private boolean publishAppActive = false;
	private String  publishAppBasepath = "";

	
	// ---- some helpers ----
	transient DFutils fileutil = new DFutils();
	transient StringsUtil strgutil = new StringsUtil();
	transient PrintLog out = new PrintLog(2,false);
	transient ArrUtilities arrutil = new ArrUtilities();
	transient XmlStringHandling xMsg = new XmlStringHandling() ;

	String dbDefinitionResource = "";

	

	
	// ========================================================================
	public SomFluidPropertiesHandler( SomFluidProperties properties){
		sfProperties = properties;

		ms = sfProperties.getModelingSettings();
		cs = ms.getClassifySettings() ;
		ps = sfProperties.getPersistenceSettings() ;
		
		out.setPrefix("[SomFluid-init]");
	}
	// ========================================================================
	
	@Override
	public void initializeDefaults() {
		
		setPlugins( true ); // make this dynamic
		
		sfProperties.setAutoAdaptResolutionAllowed(1) ;				// this will allow SomFluid to choose a proper size and a proper 
																	// number of particles (resolution), dependent on the data 

		 
							// this comes from the out-most application layer (user, or remote control)  
							String rootFolder = SomFluidStartup.getProjectBasePath(); // IniProperties.fluidSomProjectBasePath ;
							rootFolder = DFutils.createPath(rootFolder, "/") ;
							
		sfProperties.setPathToSomFluidSystemRootDir(rootFolder);	// within this dir all project base directories are located
										// sth like "D:/data/projects/"
		
		
		 
		 
		
		ps.setProjectName( SomFluidStartup.getLastProjectName() );	// will be used also for output files
		            // sth like "bank2" , i.e. the simple name of a folder where all the project files are located 
		
		ps.setKeepPreparedData(true); 								// includes persistence of transformer model
		ps.autoSaveSomFluidModels(true);
		 
		
		sfProperties.addDataSource( srctype, dataSourceName);       // if the persistence settings are available, the relative path will be used
		
							// this way we could also provide a file (or database) from an arbitrary location
							// sfProperties.addDataSource( srctype,"D:/data/projects/bank2/data/raw/bankn_d2.txt"); 
						
		sfProperties.setDataUptakeControl(0);                       // if negative, the data won't load automatically into the SOM
		
		 
	  
		sfProperties.setRestrictionForSelectionSize(678) ;			// no more than [N] nodes will be selected as neighborhood
					// that means, if the SOM grows beyond a certain size, a symmetry break will occur in the map ;
		   			// if the size of a SOM grows to  sqrt(mapsize) > 3.5*sqrt(n/pi)
	       			// then the coarse-som preprocessing will be organized, if it is allowed

		
		sfProperties.setGrowthMode( ModelingSettings._SOM_GROWTH_PRESELECT);// growth modes can be combined ! PRESELECT -> coarse-som preprocessing 
		sfProperties.setGrowthMode( ModelingSettings._SOM_GROWTH_LATERAL); //  
		sfProperties.removeGrowthMode( ModelingSettings._SOM_GROWTH_LATERAL); 
		sfProperties.setGrowthSizeAdaptationIntensity( 5 ) ;			   // 5=normal == default, 1=greedy -> large nodes, rather small lattice, 10=allowing sparsely filled nodes 
				
 
		sfProperties.setAbsoluteRecordLimit(-1); // 435
		
	   
		
		sfProperties.setGlueInstanceType(1);
		sfProperties.setMessagingActive(false) ;					    // if true, the SomFluidFactory will start the glueClient ;
		   																// the SomFluid is then accessible through messaging (see the SomController application)
		sfProperties.setglueModuleMode( 0 ) ;							// 0 = off, others: act as source, receptor, or server

		// general environment

		sfProperties.activateMultithreadedProcesses(false);
		
		assimilateVariableSettings();
		
		  
		
		sfProperties.setShowSomProgress( SomFluidProperties._SOMDISPLAY_PROGRESS_BASIC ) ;
		
		prepareStartupTraceInfo();
	}
	
	
	@Override
	public void initializeDefaultsFromFile(String inifilename) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public String checkForVariableDescriptionFile(int type) {

		return checkForVariableDescriptionFile(type, "");
	}
		
	@Override
	public String checkForVariableDescriptionFile(int type, String cfgfilepath) {

		String availableDescrFile = "" ;
		String datafolder ="" ;
		String filename ="", dfolder="",folder ="" ;
		
		String[] mandys ;
		
		boolean rB;
		
		
		try {
		
			if ((cfgfilepath.length()==0) || (DFutils.fileExists(cfgfilepath)==false)){
				
				mandys = new String[]{ "[id", "[tv"};
				
				folder = SomFluidStartup.getProjectBasePath();
				
				folder = fileutil.createpath( folder, SomFluidStartup.lastProjectName ) ;
				dfolder = fileutil.createpath( folder, "data/description");
				
				if (fileutil.direxists(dfolder)==false){
					return "";
				}
			
				filename = SomFluidStartup.getLastDataSet() ;
				
				if ((filename.indexOf("/")<0) && (filename.indexOf("\\")<0)){
					datafolder = fileutil.createpath( folder, "data/raw");
					filename = fileutil.createpath( datafolder, filename); 
				}
				
				
				if (fileutil.fileexists(filename)){
					// bankn_d2-variables.txt
					File f= new File(filename);
					filename = f.getName();
					String ext = StringsUtil.getExtensionFromFilename(filename,1); // 0 = without dot, 1 = with dot
					int p = filename.lastIndexOf(ext) ;
					if (p>0){
						filename = filename.substring(0,p) ;
					}
					filename = filename+"-variables.txt" ;
					
					filename = fileutil.createpath( dfolder, filename);
				}	
			} // filepath was "" ?
			else{
				mandys = new String[]{ "[id", "[word"};
				
				
				
			}
			
			
			if (fileutil.fileexists(filename)){
				
					String fc = fileutil.readFile2String( filename) ;
					int n= strgutil.frequencyOfStrings(fc, new String[]{"[","]"}); 
					int nt = strgutil.frequencyOfStrings(fc, new String[]{"<",">"});
					
					if ((n>0) && (n%2==0) && (nt<6)){
						fc= fc.toLowerCase();
						rB = fc.indexOf(mandys[0])>0;
						if ((rB) && (mandys.length>1)){
							rB = fc.indexOf(mandys[1])>0;
						}
					} else{
						// xml ?
						rB = fc.indexOf( "<?xml version=\"1.0\" encoding=\"")>=0;
						// any relevant content ? 
					}
					
					availableDescrFile = filename;
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		return availableDescrFile;
	}

	@Override
	public boolean loadVariableSettingsFromFile(String  filename) throws IOException {
		boolean rB=false;
		
		if (fileutil.fileexists(filename)==false){
			return false;
		}
		
		// check format
		
		// simplified [] format
			
		rB = loadVariableSettingsFromSimplifiedIniFile(filename);
	
		// rB = loadVariableSettingsFromXmlFile(filename);
		
		if (rB){
			this.sfProperties.setVariableSettings(variableSettings) ;

			// we have to transfer the group-settings++ to classifySettings...
			
			ClassificationSettings cs = sfProperties.getModelingSettings().getClassifySettings() ;
			ModelingSettings ms = sfProperties.getModelingSettings() ;
			
			
			// blacklists whitelist ... can be introduced only after accessing the data
			// we need the structure "Variables" for that!
			
			variableSettings.getBlackListedVariables();
			sfProperties.setAbsoluteFieldExclusions( variableSettings.getAbsoluteExclusions() );
			
			ms.addInitialVariableSelection( variableSettings.getInitialSelection() );
			ms.setBlacklistedVariablesRequest( variableSettings.getBlackListedVariables() );

			
			if ((sfProperties.getSomType() == FieldIntf._SOMTYPE_MONO ) ){ // && (cs.getTargetMode() == ClassificationSettings._TARGETMODE_MULTI)
				
				
				String tv = variableSettings.getTargetVariable();
				
				if ((tv!=null) && (tv.length()>0)){
					cs.setActiveTargetVariable(tv) ;
				}
				if (variableSettings.getTargetVariableCandidates().indexOf(tv)<0){
					variableSettings.getTargetVariableCandidates().add(tv);
				}
				cs.setTGlabels( variableSettings.getTargetVariableCandidates() );
				ms.setTargetVariableCandidates( variableSettings.getTargetVariableCandidates() );
				                                variableSettings.getTvGroupLabels() ;
				ArrayList<ValuePair> tvgints = variableSettings.getTvGroupIntervals() ;
				cs.setTargetGroupDefinition( tvgints );
				// labels of tg group.. ?
				
				// (cs.getTargetMode() == ClassificationSettings._TARGETMODE_MULTI)
				
				String modmodeStr = variableSettings.getSomModelingMode();
				int modmode=-1 ;
				if (strgutil.isNumericX(modmodeStr)){
					modmode = Integer.parseInt(modmodeStr) ;
				}else{
					if (tvgints.size()>0){
						modmode= ClassificationSettings._TARGETMODE_SINGLE;
					}
				}
				cs.setTargetMode( modmode );
				
					// cs.setTargetGroupDefinition( new double[]{0.28, 0.62, 1.0});	// applies only to MULTI mode, at least 2 values are required (for 1 group interval)
					cs.setTargetGroupDefinitionAuto(false);							//  - if [0,1] AND if _TARGETMODE_MULTI , then the target groups will be inferred from the data
																					//  - TODO in this case, one should be able to provide a "nominal column" that indeed contains the "names"
					// cs.setTargetGroupDefinitionExclusions( new double[]{0.4} );		// these values are NOT recognized as belonging to any of the target groups, == singular dot-like holes in the intervals
				
			}

		}
		return rB ;
	}
	
	
	private boolean loadVariableSettingsFromSimplifiedIniFile(String  filename) {
	
		boolean loadingOk=false;
		String str,section, rawXmlStr="" ;
		String[] iniContent,sectionItems,lines ;
		ArrayList<String> items ;
		
		
		ModelingSettings ms = sfProperties.getModelingSettings();
	
		if (fileutil.fileexists(filename)==false){
			return false;
		}
		try {
		// check format
		
		// simplified [] format
		
		str = fileutil.readFile2String(filename) ;
		
		lines = str.split("\n");
		for (int i=0;i<lines.length;i++){
			if (lines[i].trim().indexOf(";")==0){
				lines[i]="";
			}
		}
		str = arrutil.arr2text(lines, " \n");
		
		str = strgutil.replaceAll(str, "[", "##[[");
		iniContent = str.split("(##\\[)");
		
		if (iniContent.length<=1){
			return false;
		}
		
		variableSettings = getVariableSettingsHandler();
		
		for(int i=0;i<iniContent.length;i++){
			str = iniContent[i].trim();
			
			if (str.length()==0){
				continue;
			}
			str = str.replace("]", " "); str = str.replace("[", " ");
			
			sectionItems = str.split("\n");
			
			section = sectionItems[0].trim() ;
			sectionItems[0] = "" ;
			
			//for (int s=1;s<sectionItems.length;s++)
			{
				// str = sectionItems[s];
				
				items = new ArrayList<String>( strgutil.changeArrayStyle(sectionItems) );
				items = arrutil.removeempty(items) ;
				 
				for (int k=0;k<items.size();k++){
					str = items.get(k);
					str = str.trim().replace("\r", "") ;
					items.set(k,str);
				}
				if (section.toLowerCase().contentEquals("id")){
					if (items.size()>0){
						str = items.get(0).trim().replace("\r", "") ;
						variableSettings.setIdVariable( str ) ;  
					}
				}
				if (section.toLowerCase().contentEquals("ids")){
					if (items.size()>0){
						variableSettings.setIdVariableCandidates( items );  
					}
				}
				if (section.toLowerCase().contentEquals("tv")){
					if (items.size()>0){
						variableSettings.setTargetVariable( items.get(0)) ;  
					}
				}
				if (section.toLowerCase().contentEquals("tvs")){
					if (items.size()>0){
						variableSettings.setTargetVariableCandidates( items );  
					}
				}
				
				if (section.toLowerCase().contentEquals("initial")){
					if (items.size()>0){
						variableSettings.setInitialSelection( items );
					}
				}
				
				if (section.toLowerCase().contentEquals("blacklist")){
					if (items.size()>0){
						variableSettings.setBlackListedVariables( items ) ;
					}
				}
				if (section.toLowerCase().contentEquals("whitelist")){
					if (items.size()>0){
						variableSettings.setWhiteListedVariables( items ) ;
					}
				}
				if (section.toLowerCase().contentEquals("absolute exclude")){
					if (items.size()>0){
						variableSettings.setAbsoluteExclusions( items , 1);
					}
				}
				if (section.toLowerCase().contentEquals("treatment")){
					if (items.size()>0){
						variableSettings.setTreatmentDesignVariables( items ); 
					}
				}
				if (section.toLowerCase().contentEquals("group")){
					if (items.size()>0){
						variableSettings.setGroupIndicatorDesignVariables( items ); 
					}
				}
				if (section.toLowerCase().contentEquals("tgs")){
					// target group ..
					importingTargetGroupSettings( items );
				}
			}
		} // i-> all sections in iniContent
		
		loadingOk = true;
		
		
		} catch (IOException e) {
			iniContent = new String[0];
			e.printStackTrace();
		}
		
		
		return loadingOk;
	}

	
	private void importingTargetGroupSettings(ArrayList<String> items) {
		// variableSettings
		String itemstr, groupdefStr,groupIndexLabel ;
		int ix;

		ix=0;
		
		//                              in "items" search from item 0 for "mode" where 1= <before> "=" // 2=after     
		ix = arrutil.getListItemIndexByContent( items, 0,       // list, start index
												"mode", 1, "=", // search for, 1=before, "="
												0,0);           // relaxed, inverted
		if (ix>=0){
			itemstr = items.get(ix) ;
			// mode = single
			itemstr = itemstr.replace("mode","");
			itemstr = itemstr.replace("=","").trim();
			variableSettings.setSomModelingMode(itemstr);
			 
		} // "mode" defined ?
		
		// ................
		ix = arrutil.getListItemIndexByContent( items, 0, "excludedvalues", 1, "=", 0,0);
		if (ix>=0){
			itemstr = items.get(ix) ;
			// a list of doubles...
			int p = itemstr.indexOf("=");
			if ((itemstr.length()>0) && (p>0)){
				itemstr = itemstr.substring( p+1, itemstr.length()).trim() ;
				String[] valuesStrs = itemstr.split(";") ;
				for (int n=0;n<valuesStrs.length;n++){
					if (valuesStrs[n].length()>0){
						double v = Double.parseDouble(valuesStrs[n]) ;
					}
				} // ->
			}// ?
			
		} // "excludedvalues" defined ?
		// ................

		for (int i=0;i<items.size();i++){
			itemstr = items.get(i) ;
			if (itemstr.toLowerCase().trim().startsWith("group")){  // sth like: "group.1 = intermediate: 0.53;0.72"
				String[] groupdefs =  itemstr.split("=");
				groupIndexLabel = groupdefs[0].replace("group", "").trim();
				if (groupIndexLabel.startsWith(".")){
					groupIndexLabel=groupIndexLabel.substring(1,groupIndexLabel.length()) ;
				}
				groupdefStr = groupdefs[1];
				// assimilate
				assimilateTargetGroupDefItem( groupIndexLabel , groupdefStr);
			}
			
		} // i-> all entries
	}
	
	
	private void assimilateTargetGroupDefItem(String groupIndexLabel, String groupdefStr) {
	// for entry : group.1 = intermediate: 0.53;0.72 => we get:  1  ,  intermediate: 0.53;0.72
		String tgLabel="";
		double[] values = new double[2] ;
		
		groupdefStr = groupdefStr.trim() ;
		int pdp = groupdefStr.indexOf(":");
		if (pdp>0){
			tgLabel = groupdefStr.substring(0,pdp);
			tgLabel = tgLabel.replace(":", "");
			groupdefStr = groupdefStr.substring(pdp+1,groupdefStr.length());
		}else{
			groupdefStr = groupdefStr.replace(":", "");
		}
		// now we have a pair of values: groupdefStr = 0.53;0.72
		groupdefStr = groupdefStr.trim() ;
		String[] valuesStrs = groupdefStr.split(";") ;
		
		if (valuesStrs[0].length()==0){
			valuesStrs[0]="0.5";
		}
		if (valuesStrs[1].length()==0){
			valuesStrs[1]="1.0";
		}
		
		values[0] = Double.parseDouble( valuesStrs[0].trim());
		values[1] = Double.parseDouble( valuesStrs[1].trim());
		                                    // criterionLowerLimit,criterionUpperLimit, label
		variableSettings.addSingleTargetGroupDefinition( values[0],values[1], tgLabel);
		
	}
	
	
	
	@SuppressWarnings("unused")
	private boolean loadVariableSettingsFromXmlFile(String  filename) throws IOException {

		boolean loadingOk=false;
		String str, rawXmlStr="";
		
		 
		
		
		rawXmlStr = fileutil.readFile2String(filename) ;
	 
		
		if (rawXmlStr.length()==0){
			return false;
		}
		
		xMsg.clear() ;
		xMsg.setContentRoot( "properties") ;
		
		str = xMsg.getSpecifiedInfo(rawXmlStr, "//properties", "target") ; // = "somfluid" ?
		str = xMsg.getSpecifiedInfo(rawXmlStr, "//properties", "section") ;// = "variables" ?
		
		
		
		
		// we provide a small interface for dealing with initial variable settings all at once
		VariableSettingsHandlerIntf variableSettings = getVariableSettingsHandler();
	
		variableSettings.setInitialSelection( new String[]{"Stammkapital","Bonitaet","Bisher","Branchenscore"});
		variableSettings.setBlackListedVariables( new String[]{"Name","KundenNr"} ) ;
		variableSettings.setAbsoluteExclusions( new String[]{"Name","KundenNr","Land","Region"} , 1);
	
		variableSettings.setTargetVariables("*TV"); 		// of course only if instance = "som" (or transformer! for certain transformations)
														// if wildcard is used, the first one found is used as active, unless defined otherwise
														// by setActiveTargetVariable("")
		
		
		sfProperties.getModelingSettings().setRequestForBlacklistVariablesByLabel( new String[]{"Name","KundenNr"}) ;
	
		
																		  // these variables are excluded once and for all -> they won't get transformed either
																		  // if mode 1+ then they even won't get imported
		sfProperties.setAbsoluteFieldExclusions( new String[]{"Name","KundenNr","Land","Region"} , 1);
	
		ms.setActiveTvLabel("*TV") ;       								// the target variable; wildcarded templates like "*TV" are possible
		ms.setTvGroupLabels("Label") ; 	   								// optional, if available this provides the label of the column that contains the labels for the target groups, if there are any
																		// the only effect will be a "nicer" final output
		//sfProperties.getModelingSettings().setTvLabelAuto("TV") ; 	// the syllable(s) that will be used to identify the target variable as soon as data are available
																	    // allows a series of such identifiers
	 	
		ms.setInitialVariableSelection( new String[]{"Stammkapital","Bonitaet","Bisher","Branchenscore"});
	    
		loadingOk = true;
		
		
		
		return loadingOk;
	}
	
	
	@Override
	public String getStartupTraceInfo() {
		 
		return startupTraceInfo;
	}

	
	/**
	 * 
	 * called after all settings have been defined
	 * creating a simple string that is used to create a "boot file": SomFluid then is able to know 
	 * about the project of the last run, in case a "resume" is requested
	 * 
	 * @return
	 */
	private void prepareStartupTraceInfo() {
		
		String cfgroot, userdir, lastproject, infoStr="";
		
		
		cfgroot =  sfProperties.getSystemRootDir() ;
		lastproject = sfProperties.getPersistenceSettings().getProjectName() ;
		
		// TODO better -> several categories: load file, modify it, write it back
		infoStr =  "cfgroot::"+cfgroot+"\n" +
		           "project::"+lastproject+"\n" ;
				                           	   

		
		startupTraceInfo = infoStr;
	}

	
	
	public void setPlugins( boolean loadNow){
		
		 
		AlgorithmPluginsLoader lap ;
		
		// loading "builtinscatalog.xml" which is necessary for global indexing and activation of built-in algorithms
		sfProperties.setAlgorithmsConfigPath( algorithmsConfigPath ); 

		// here we need an absolute path, the file is needed also for advanced auto assignments of built-in algorithms, 
		// even if there are no custom algorithms
		sfProperties.getPluginSettings().setBaseFilePath(algorithmsConfigPath, "catalog.xml") ;
		// the plugin jarfiles are expected to sit in a relative sub-dir "transforms/" to this base-dir...
		
		
		String catfilename = fileutil.createpath( algorithmsConfigPath , "catalog.xml");
		if (fileutil.fileexists(catfilename)==false){
			out.printErr(1, "File <catalog.xml> not found in provided folder : \n"+
							"   "+ catfilename +"\n"+
							"...thus no plugins for transformation algorithms will be loaded!");
			loadNow=false;
		}
		String algofolder = sfProperties.getPluginSettings().getBaseFilePath();
		
		int n = fileutil.enumerateFiles( "", ".jar", algofolder) ;
		
		if ((fileutil.direxists( algofolder) ==false) || (n<=0)){
			out.printErr(1, "Catalog file has been found, yet the expected folder  \n"+
							"   "+ algofolder +"\n"+
							"does not exist, or does not contain any file.");
				loadNow=false;
		}else{
			if (sfProperties.isPluginsAllowed()){
				String plur = "s" ; if (n==1)plur="" ;
				out.print(2, n+" plugin file"+plur+" found.\n") ;
			}
		}
		
		sfProperties.setPluginsAllowed( loadNow ) ; // could be controlled form the outside in a dynamic manner

		try {
			if (sfProperties.isPluginsAllowed()){
				lap = new AlgorithmPluginsLoader(sfProperties, true);
				if (lap.isPluginsAvailable()) {
					lap.load();
					int k = lap.getNotLoadedJars().size();
					if (k>0){
						out.printErr(2,k +" jarfiles failed to load.");
					}
				}
			}
			// feedback about how much plugins loaded ...
			
			//
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setAlgorithmsConfigPath(String pathToCatalogFolder) throws Exception {

		algorithmsConfigPath = pathToCatalogFolder ; 
		
		algorithmsConfigPath = algorithmsConfigPath.trim();
		algorithmsConfigPath = strgutil.replaceAll(algorithmsConfigPath, "\\", "/");
		algorithmsConfigPath = strgutil.replaceAll(algorithmsConfigPath, "//", "/");
		
		if (fileutil.direxists( algorithmsConfigPath)==false){
			throw(new Exception("requested path does not exist : "+pathToCatalogFolder));
		}
		if (algorithmsConfigPath.endsWith("/")==false){
			algorithmsConfigPath = algorithmsConfigPath+"/"; 
		}
	}

	@Override
	public void setInstance(String description, int ...nodecount ) {
	 
		description = description.trim().toLowerCase();
		
		
		if (description.startsWith("som")){
			sfProperties.setInstanceType( FieldIntf._INSTANCE_TYPE_OPTIMIZER ) ;   
				// the main role the module is exhibiting, MANDATORY !!!
				// _SOM  _OPTIMIZER _TRANSFORM

				// target oriented modeling lattice
			sfProperties.setSomType( FieldIntf._SOMTYPE_MONO ) ;   // we define to create a SOM for targeted modeling, target group settings must be supplied 

			ms.setTargetedModeling(true) ;     								// VERY important setting, determines the fundamental mode in which the SOM will run
			   																// invokes validation, and eventually feature selection via evo + sprite
			   																// by default, mode is "_TARGETMODE_SINGLE"
			
			if ((nodecount!=null) && (nodecount.length>0)){
				nodeCount = nodecount[0] ;
			}else{
				nodeCount = 100; 
				
			}
			sfProperties.setInitialNodeCount(nodeCount);                    // initial size; yet it does not matter much since the SomFluid could grow anyway
			
			sfProperties.setWinnersCountMultiple(1) ; 							   // max 5, if=1 == default = single winner
		       // only the best winner will be actually updated by the data ;
			   // the further winners only update their profile
			   // in most cases, a singular winner (n=1) provides the best results
            // more winners leads to "smearing" of information

		} // som for targeted modeling
		
		
		// som map as associative storage: no target variable, but some choice of internal consistency / contrast measures
		// modes of working: plain, +3-sigma, +internal variance (combined with 3-sigma)
		// important: list of indices, list of profile columns or column numbers such as [3,12-65, 120-all], 
		if (description.startsWith("astor")){
			
			sfProperties.setSomType( FieldIntf._SOMTYPE_PROB ) ;
			sfProperties.setInstanceType( FieldIntf._INSTANCE_TYPE_ASTOR ) ;  
			
			if ((nodecount!=null) && (nodecount.length>0)){
				nodeCount = nodecount[0] ;	
			}else{
				nodeCount = 1000; 
			}
			sfProperties.setInitialNodeCount(nodeCount);
		} // som map as associative storage
		
		 
		ps = sfProperties.getPersistenceSettings() ;
	}

	@Override
	public void setGridType(int somGridtype) {
		// 
		 
		sfProperties.setSomGridType( somGridtype ) ; 
		
	}

	@Override
	public void setSomTargetMode(int targetmode) {
		
		/*
		 *   _TARGETMODE_MULTI  	requires the determination of values that define an interval for a target group
		 							a virtual column will be created which encodes these settings (by SomTransformer)
		 							
		 	 _TARGETMODE_SINGLE 	standard modeling, target groups can be translated to a binary flag: is target=preferred outcome, or not 
		*/
		
		cs.setTargetMode(targetMode) ;
		targetMode = targetmode;
	}

	public void setDatabaseDefinitionResource( String alias) {
		setDatabaseDefinitionResource("" ,  alias);
	}
	@Override
	public void setDatabaseDefinitionResource(String dbDefResource,String alias) {
		
		if (dbDefResource.length()==0){
			dbDefResource = "definition-db-sql-xml" ;
		}
		dbDefinitionResource  = dbDefResource ;
		sfProperties.setDatabaseDefinitionResource( dbDefinitionResource );
		
		// analyze for table names, columns
		try {
		
			sfProperties.getDatabaseDefinitionInfo();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void setDataSourcing(String srcDescription, int activateOnlineMode) {
		
		srcDescription = srcDescription.trim().toLowerCase();
		
		if (ps==null){
			ps = sfProperties.getPersistenceSettings() ;
		}
		if (srcDescription.startsWith( DataStreamProviderIntf._DSP_SOURCED_FILE )){// "file"
			
			sfProperties.setSourceType( SomFluidProperties._SRC_TYPE_FILE );
			srctype = SomFluidProperties._SRC_TYPE_FILE;			// alternatives: db (not realized), online learning on continuous stream (not realized)
			// SomFluidProperties._SRC_TYPE_XONLINE
			
			if (activateOnlineMode>0){
				String rootFolder = SomFluidStartup.getProjectBasePath(); // IniProperties.fluidSomProjectBasePath ;
				if (fileutil.direxists(rootFolder)==false){
					// create tmp
				}
				if (fileutil.direxists(rootFolder) ){
					String prjname = SomFluidStartup.getLastProjectName();
					supervisedOnlineFolder = fileutil.createpath(rootFolder, "online/incoming") ;
					ps.setIncomingDataSupervisionDir(supervisedOnlineFolder);
				}
				ps.setIncomingDataSupervisionActive(true);
				
				
			} // activateOnlineMode>0 ?
			else {
				ps.setIncomingDataSupervisionActive(false);
			}
			
		} // "file" ?
		if (srcDescription.startsWith( DataStreamProviderIntf._DSP_SOURCED_DB )){
			
			sfProperties.setSourceType( SomFluidProperties._SRC_TYPE_DB );
			srctype = SomFluidProperties._SRC_TYPE_DB;
			
			
			
			
		} // "db"
		
		
		
	}

	@Override
	public void setDataSourceName(String srcName) {
		
		dataSourceName = srcName;
	}
	

	@Override
	public void setSupervisedDirectory(String folder) throws Exception {
		
		if (srctype != SomFluidProperties._SRC_TYPE_FILE ){
			ps.setIncomingDataSupervisionActive(false);
			throw(new Exception("Supervising folders for online learning mode is possible only if typw of source is FILE !"));
		}
		
		ps.setIncomingDataSupervisionDir(folder);
		sfProperties.setExtendingDataSourceEnabled(false); 				   // default=false; true for data updates via internal Glue-client or via directory supervision for online learning
		

	}


	@Override
	public String getSupervisedDirectory() {
		return ps.getIncomingDataSupervisionDir();
	}

	@Override
	public void setDataSimulationByPrototypes(int numberOfRecords) {
		
		numberOfSimulatedRecords = numberOfRecords;
		
		if (numberOfSimulatedRecords>100){
			sfProperties.setSimulationSize(3000) ; 							   // applies only if simulation mode <> _SIM_NONE
			sfProperties.setSimulationMode( SomFluidProperties._SIM_PROFILES );  
		}else{
			sfProperties.setSimulationMode( SomFluidProperties._SIM_NONE);     
			   // default=_NONE; can be used to create data from (apriori) profiles, 
		       //                or to extend the body of data by surrogate data (random, but same distribution and same covar)
			   // use _SIM_PROFILES if the provided data describe prototypical apriori profiles
			
		}

	}

	@Override
	public void setMaxNumberOfPrototypes(int n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMethodforInitialVariableSelection(int mode) {
	
		ms.setInitialAutoVariableSelection(mode);
	}

	
	
	@Override
	public VariableSettingsHandlerIntf getVariableSettingsHandler() {
	
		if (variableSettings==null){
			variableSettings = new VariableSettingsHandler();
		}
		return variableSettings ;
	}

	@Override
	public void setSingleTargetDefinition( String level, double criterionLowerLimit, double criterionUpperLimit, String label) {
	 
		int defLevel = 0;
		
		level = level.trim().toLowerCase() ;
		if (level.length()==0)level="raw";
		
		if (level.startsWith("raw")){
			defLevel = ClassificationSettings._TARGET_DEFLEVEL_RAW;
		}else{
			defLevel = ClassificationSettings._TARGET_DEFLEVEL_NORM ;
		}
		cs.setTargetGroupDefinitionLevel(defLevel) ;
		
		cs.setSingleTargetGroupDefinition( criterionLowerLimit,criterionUpperLimit,"intermediate");	// min max of the interval [0|1][ min|max] in case of _TARGETMODE_SINGLE, ineffective if _TARGETMODE_MULTI
		// the label is helpful for selecting models

		/*
		 * the "outcome" or target can be mapped onto a binary variable
		 */
		 
		setSomTargetMode(ClassificationSettings._TARGETMODE_SINGLE ) ; 
		// alternatively: ClassificationSettings._TARGETMODE_MULTI : simultaneous multi-class modeling
		// the difference concerns the interpretation of the individual nodes

	}

	@Override
	public void preferSmallerModels(boolean flag, int preferredMinimumSize) {

		ms.setCanonicalReduction(true);	   								   // not implemented yet
		ms.setCanonicalReductionLimit(preferredMinimumSize) ;
		ms.setCanonicalExploration( ModelingSettings._CANONIC_BOTTOM,1,3); // n,m: n=removals from a given model, m=canonic loops
		ms.setCanonicalExploration( ModelingSettings._CANONIC_TOP ,1, 3);  // top=exclude best predictors, bottom=exclude worst predictors


	}
	
	
	@Override
	public void setOptimizerStoppingCriteria( int absoluteStepCount, double ...stoppingConstraints) {

		int explorationDepth  = -3 ; 
		int explorationLength = -3 ; // -1 = not set, -3 = auto, as soon as data are known
		double timeConstraint = -3 ; // 
		  
		
		if (stoppingConstraints!=null){
			
			
		} // stoppingConstraints = null ?
		
		
		   // whichever of these 4 stopping criteria for the optimizer is reached first...
		ms.getOptimizerSettings().setMaxStepsAbsolute( absoluteStepCount );	// low only for testing, or initial exploration, typically 500+
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -       
        				// note that this step-count applies WITHIN a step on L2 as 
		   				// controlled by "setMaxL2LoopCount" (see below, typically max=4)

		ms.getOptimizerSettings().setMaxAvgVariableVisits(21) ;  			// required for comparing models across population regarding the variable selections 
		ms.getOptimizerSettings().setDurationHours(0.8) ;        			// an absolute time limit in hours 
		ms.getOptimizerSettings().setStepsAtLeastWithoutChange(100) ; 		// stop if there is no improvement for some time



		ms.getOptimizerSettings().setBalancedEvolutionaryExploration(true) ;


	}

	public void setAutoBagging() {
	
		sfProperties.setAutoSomBags( false ) ; 	// if true (=default) no settings have to be explicitly defined, 
												// adaptive default values will be taken		
	}
	
	@Override
	public void setBagging(int maxNodeCount, int recordsPerNode, int... maxRecordCount) {
		
		int maxRecords = -1;
		boolean apply = true;
		maxNodeCount=0;
		recordsPerNode=0;
		// defines bagging, based on provided parameters
		sfProperties.defineSomBags( maxNodeCount, recordsPerNode, maxRecords); 			
		
		apply = false;
		
		if ((maxNodeCount<=1) || (recordsPerNode<=8)){
			apply=false;
		}
		sfProperties.applySomBags( apply ) ;		// p1: min records per node, p2: max number of nodes, p3: max number of records 	
		
		boolean hb =sfProperties.getModelingSettings().getSomBagSettings().isApplySomBags() ;
		 
			 
		 
		// we also may consider to split-bag in case of NVE : config: max number of groups target variables,
		// such to build small soms for any of the items, and combine them to a 
		// compound model
		

	}

	@Override
	public void activateGrowingOfSom(boolean flag, int maxNodeSize, double avgQuantile) {


		sfProperties.setActivationOfGrowing( flag );                       // activates/deactivates growing without removing the settings, default=true
	}

	@Override
	public void setBooleanAdaptiveSampling(boolean b) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * returns the effective result folder
	 */
	@Override
	public String setResultsPersistence(int i) {
		
		String path;
		boolean createOutFiles = i>=1;
		
		
		sfProperties.getOutputSettings().setWriteResultFiles(createOutFiles);
		
		path = sfProperties.getSystemRootDir() ;
		path = ps.getPathToSomFluidSystemRootDir();
		path = fileutil.createpath(path, ps.getProjectName()) ; // bank2\export\results
		path = fileutil.createpath(path, "export/results/") ;
		sfProperties.getOutputSettings().setResultfileOutputPath(path) ;     
				// there is a default ! ...which is based on
				// sfProperties.setPathToSomFluidSystemRootDir("D:/data/projects/");
				// and : PersistenceSettings.setProjectName("bank2");
		
		sfProperties.getOutputSettings().setResultFilenames( null); // there are defaults, but the user might set his own preferences
		
		sfProperties.getOutputSettings().setAsXml(false);           // default = false
		sfProperties.getOutputSettings().setHtmlCompatible(true) ;  // if as xml, we then will use html compatible tags
		
		sfProperties.getOutputSettings().createZipPackageFromResults(true);// default = true
		
		sfProperties.getOutputSettings().exportApplicationModel(false);    // controls exporting the file package that is necessary for applying the data to new data
		sfProperties.getOutputSettings().setIncludeDataToExportedPackages(true);  // some parameters for that export...
		sfProperties.getOutputSettings().setIncludeResultsToExportedPackages(true);
		sfProperties.getOutputSettings().setZippedExportedPackages(true);
		
		// sfProperties.sampleVariableContributionsThruTopModels( int n_models = 10 )
		// defining what should happen upon results: saving, sending, displaying, nothing

		return  sfProperties.getOutputSettings().getResultfileOutputPath();
	}
	
	@Override
	public String getResultBaseFolder() {
		return sfProperties.getOutputSettings().getResultfileOutputPath();
	}

	public void assimilateVariableSettings(){
		assimilateVariableSettings(variableSettings);
	}
	
	public void assimilateVariableSettings( VariableSettingsHandlerIntf varsettings ) {
		
		variableSettings = varsettings;
		 
		if (variableSettings==null){
			return;
		}
		// 
		String[] strings ;
		
		if (variableSettings.getInitialSelection().size()>0){
			strings = (String[])(arrutil.changeArrayStyle( variableSettings.getInitialSelection()));
			ms.setInitialVariableSelection( strings );
		}
		

		if(variableSettings.getWhiteListedVariables().size()>0){
			strings = (String[]) arrutil.changeArrayStyle( variableSettings.getWhiteListedVariables());
			ms.setRequestForWhitelistVariablesByLabel( strings );
		}

		
		if(variableSettings.getAbsoluteExclusions().size()>0){
			strings = (String[]) arrutil.changeArrayStyle( variableSettings.getAbsoluteExclusions());
			sfProperties.setAbsoluteFieldExclusions( strings , 1);
		}
		
		if(variableSettings.getTargetVariable().length()==0){
			ms.setActiveTvLabel( "*TV") ;
		}else{
			ms.setActiveTvLabel( variableSettings.getTargetVariable() );// "*TV") ;
		}
		// ms.setTvGroupLabels("Label") ;
		
		
		if (variableSettings.getGroupDesignVariables().size()>0){
			variableSettings.getBlackListedVariables().addAll( variableSettings.getGroupDesignVariables() );
			sfProperties.setGroupDesignVariables(variableSettings.getGroupDesignVariables());
		}

		if (variableSettings.getTreatmentDesignListedVariables().size()>0){
			variableSettings.getBlackListedVariables().addAll( variableSettings.getTreatmentDesignListedVariables() );
			sfProperties.setTreatmentDesignVariables(variableSettings.getTreatmentDesignListedVariables());
		}
		
		if(variableSettings.getBlackListedVariables().size()>0){
			strings = (String[]) arrutil.changeArrayStyle( variableSettings.getBlackListedVariables());
			ms.setRequestForBlacklistVariablesByLabel( strings );
			
		}
 
		sfProperties.setVariableSettings(variableSettings);
	}
	
	@Override
	public void exportVariableSettings( VariableSettingsHandlerIntf variablesettings, String xfilename) {
		
		if (variablesettings!=null){
			variableSettings = variablesettings;
		}
		
		if (variableSettings==null){
			return;
		}
		// check if actually used/filled...
		
		ArrayList<String> strList;
		SomFluidXMLHelper xEngine = new SomFluidXMLHelper();
		
		XMLBuilder  builder = xEngine.getXmlBuilder( "properties" ).a("target", "somfluid").a("section","variables");
		
		String xstr, str;
		 
		try{
			
			
			// ............................................
			builder = builder.e("general")
								.e("date").a("value", "").up()
								.e("project").a("name", "").up();
			
			builder = builder.up();
			// ............................................
			
			builder = builder.e("variables");
				strList = variableSettings.getInitialSelection();
				str = xEngine.digestStringList( strList ) ;
			
				builder = builder.e("initial").a("list", str);
				
				strList = variableSettings.getBlackListedVariables();
				str = xEngine.digestStringList( strList ) ;
			
				builder = builder.e("blacklist").a("list", str);
				
				strList = variableSettings.getAbsoluteExclusions() ;
				str = xEngine.digestStringList( strList ) ;
				int mode = variableSettings.getAbsoluteExclusionsMode() ;
				
				builder = builder.e("excluded").a("list", str).a("mode", ""+mode);
				

			builder = builder.up();
			
			// ............................................

			builder = builder.e("target");
				strList = variableSettings.getTargetVariableCandidates() ;
				str = xEngine.digestStringList( strList ) ; 
				
				builder = builder.e("variable")
									.e("activetarget").a("label", variableSettings.getTargetVariable() ).up()
									.e("candidates").a("label", str).up();
				
				builder = builder.up();
				
				strList = variableSettings.getTvGroupLabels() ;
				str = xEngine.digestStringList( strList ) ; 
				
				builder = builder.e("groups")
									.e("labels").a("label", str).up()
									.e("values").a("separationvalues", "").up();
				builder = builder.up();
			
			builder = builder.up();
			// ............................................

			
			
			// ............................................

			
			
			// ............................................

			
			
			// ............................................
			xstr = xEngine.getXmlStr(builder, false);
		
			fileutil.writeFileSimple( xfilename, xstr);
			
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public void publishApplicationPackage(boolean flag, String basepath) {
		
		publishAppActive = flag ;
		publishAppBasepath = basepath ; 
	}


	
	
	
	
	
}
