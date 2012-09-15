package org.NooLab.somfluid.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.Random;


import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.math.array.StatisticSample;


import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.files.PathFinder;
import org.NooLab.utilities.files.StartupProperties;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.strings.ArrUtilities;
 
import org.NooLab.field.repulsive.components.data.SurroundResults;
 
 
import org.NooLab.somfluid.app.astor.query.SomQueryIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.SomQueryTargetIntf;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivity;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivityIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamics;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurface;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.categories.similarity.Similarity;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.nodes.LatticeIntf;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.core.*;
import org.NooLab.somfluid.env.communication.LatticeFutureVisorIntf;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.storage.DataTableCol;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatisticalDescriptionIntf;
import org.NooLab.somfluid.util.NumUtils;

 



/**
 * 
 * this class establishes the environment for the list of nodes that are making up the lattice;</br>
 *
 * it establishes its own Guid and knows about it upon restart, by means of a small ini storage
 * 
 * 
 *
 */
public class VirtualLattice 
								implements 
											LatticeIntf,       // perspective for use within SomFluid
											SomQueryTargetIntf // perspective used to organize and to address queries to the som
											                   // it is being used by SomQuery
											{

	public static final double __DEFAULT_NODE_INIT_STDEV = 0.16;

	boolean _DEBUG = false;
	
	// ..........................................

	
	//SomFluid somFluidParent;
	SomProcessIntf somProcessParent  ;
	
	SomDataObject somData;

	ArrayList<Long> nodeGuids = new ArrayList<Long>();
	Map<Long,Object> nodeUidMap = new TreeMap<Long, Object>();
	Map<Long,Long> nodeSerialsMap = new TreeMap<Long, Long>();
	

	long numGuid = 0L;
	
	boolean isInitializing;	
	
	int somType=0 , gridType=0;
	LatticePropertiesIntf latticeProperties;

	ArrayList<MetaNode> nodes = new ArrayList<MetaNode>();
	
	Map<Long, Integer> nodeIndexMap = new TreeMap<Long, Integer>() ;
	
	OpenLatticeFutures openLatticeFutures = new OpenLatticeFutures();  
	
	ArrayList<SurroundResults> selectionResultsQueue = new ArrayList<SurroundResults>(); 
	SelectionResultsQueueDigester selectionResultsQueueDigester;
	
	Map<String, Object> selectionResultsQueryMap = new HashMap<String, Object>()  ;
	
	private ModelProperties  modelProperties; 
	
	// ------------------------------------------
	// initialize imports of external structural components
	// the import objects will act as simple factories and instantiate the classes
	// important: no semantics in constructing them = no parameters for a general interface
	
	/*
	 * these components refer to the lattice as a whole ! not to the nodes...
	 * i.e., they reflect the lattice AS a node,;
	 * the nodes inside this lattice have their own instances, since these components store values individually
	 */
	SimilarityIntf similarityConcepts = new Similarity();  
	IntensionalitySurfaceIntf intensionalitySurface = new IntensionalitySurface();
	ExtensionalityDynamicsIntf extensionalityDynamics ;
	MetaNodeConnectivityIntf nodeConnectivity = new MetaNodeConnectivity() ;
	
	// ------------------------------------------
	
	int latticeQuery = 0;
	
	// ------------------------------------------
	
	int surroundError=0;
	
	StatisticSample statsSampler;
	Random rndInstance = new Random();
	

	private boolean fieldIsDynamic = false;
	private double averagePhysicalDistance = 1.0;

	public boolean bmuBufferActivated = false;

	private double initialRandomDivergence = 0.2;
	private double stDevForNodeInitialization = __DEFAULT_NODE_INIT_STDEV ;

	private int dataSize = 0;
	long latestNodeIndex = -1L;
	
	
	StringedObjects sob = new StringedObjects();
	PrintLog out = new PrintLog(2,true);

	
	// ========================================================================
	public VirtualLattice( SomProcessIntf parent, LatticePropertiesIntf latticeProps, int svlIndex ){
		  
		latticeProperties = latticeProps;
		somProcessParent = parent;
		somData = somProcessParent.getSomDataObject() ;
		
		somType = latticeProps.getSomType();  
		gridType = latticeProps.getSomGridType() ;
		
		extensionalityDynamics = new ExtensionalityDynamics(somData, somProcessParent, somType) ; 
		
		// ..........................................................
		
		selectionResultsQueueDigester = new SelectionResultsQueueDigester( svlIndex ) ;
		// out.printErr(2, "startSelectionResultsQueueDigester(a)");
		
		out.delay(60);
		
		/*
		 * the properties of the lattice if used/interpreted as a model
		 */
		modelProperties = new ModelProperties() ; 
		
		int seed = 9357;
		rndInstance.setSeed( seed );
		statsSampler = new StatisticSample( seed ) ;
		 
		 
		//  
		numGuid = (Long)(new StartupProperties()).careForInstanceGuid("~lattice", 0, Long.class, this.getClass() );
		//                                              "0" == enum value of instance
		
		numGuid = numGuid+1-1;
		// 521015310199101495
	} 
	
	// constructor for cloning an existing lattice of nodes, INCLUSIVE the nodes !!
	public VirtualLattice(VirtualLattice latticeNodes) {
		
	}

	
	// ========================================================================


	public void setSomData(SomDataObject somDataObject) {
		 
		
		somData = somDataObject;
	}

	/**
	 * 
	 * this method returns a list of particles within the diameter that is defined at a given stage of learning;
	 * nothing is done so far about the lateral control mechanism
	 * 
	 * the updating callback from ParticleField arrive in SomFluid, 
	 * hence we have to switch into the reverse facade
	 * 
	 */
	@Override
	public ArrayList<IndexDistanceIntf> getNeighborhoodNodes( int index, int nodeCount, int controlparam ) {
		
		ArrayList<IndexDistanceIntf> selectedNodes = new ArrayList<IndexDistanceIntf>();
		
	
		/*
		 * ONLY FOR DEBUG TO PREVENT PARALLEL CALLS
		 */
			if ((_DEBUG == true) && (latticeQuery>0)){
				return selectedNodes;
			}
			latticeQuery = 1;
		/*
		 * 
		 */
		
		// forking the request immediately to its own name space  
		selectedNodes = (new ParticleSelectionQuery(controlparam)).getNodes( index, (int) (nodeCount*1.3) );
		
		 
		return selectedNodes;
	}
	
	
	/**  on option, we use node statistics for optimization, or decisions about growth    */
	@SuppressWarnings("unchecked")
	public void establishProperNodeStatistics() {
		
		int extRecCount, ix,fix,fc ;
		Long rix;
		boolean reCalc;
		MetaNode node;
		Variables variables;
		double v;
		ArrayList<Double> fieldValues = new ArrayList<Double>();
		ArrayList<Long> recIndexes;
		ArrayList<Integer> useIndexes ;
		variables = somData.getVariables() ;
		
		ArrayList<BasicStatisticalDescriptionIntf> bsds;
		BasicStatisticalDescriptionIntf bsd ;
		
		reCalc = false;
		// 
		for (int i=0;i<nodes.size();i++){
			node = nodes.get(i);
			useIndexes = (ArrayList<Integer>) variables.transcribeUseIndications(node.getIntensionality().getUsageIndicationVector());
			// like: [7, 8, 10] , without TV !

			extRecCount = node.getExtensionality().getListOfRecords().size();
			if (extRecCount > 0) {
				// node.getSimilarity().getUseIndicatorArray();
				bsds = (ArrayList<BasicStatisticalDescriptionIntf>) node.getExtensionality().getStatistics().getFieldValues();
				for (int f=0;f<useIndexes.size();f++){
					ix = useIndexes.get(f);
					fc = bsds.get(ix).getCount();
					if (fc!=extRecCount){
						reCalc = true;
						break ;
					}
				} // f->
				if (reCalc ){
					break ;
				}
			}
		}
		
		if (reCalc){
			
			for (int i=0;i<nodes.size();i++){
				node = nodes.get(i);
				useIndexes = (ArrayList<Integer>) variables.transcribeUseIndications(node.getIntensionality().getUsageIndicationVector());
				// like: [7, 8, 10] , without TV !

				extRecCount = node.getExtensionality().getListOfRecords().size();
				bsds = (ArrayList<BasicStatisticalDescriptionIntf>) node.getExtensionality().getStatistics().getFieldValues();
				if (extRecCount > 0) {
					
					// all fields (variables)...
					for (int f=0;f<useIndexes.size();f++){

						fix = useIndexes.get(f);
						bsd = bsds.get(fix);
						bsd.reset() ;
						 
						
						DataTableCol dcol = somData.getNormalizedSomData().getColumn(fix);
						fieldValues.clear() ;
						// re-calculate -> 1. collecting the values into a temp list
						// for field f, and record indexes as in list of records from extensionality
						recIndexes = node.getExtensionality().getListOfRecords() ;
						for (int r=0;r<extRecCount;r++){
							rix = recIndexes.get(r) ;
							v = dcol.getCellValues().get( (int)Math.round(1.0*rix) ) ; 
							fieldValues.add(v) ;
						} // r->
						
						// sending values to the stats container and calculating stats
						bsd.introduceValues(fieldValues) ;
						v = bsd.getMean() ;
					} // f->
					
				}else{
					
				}
			}
			
			
		}// reCalc ?
		ix=0;
	}

	
	
	/**
	 * this exports the profiles of all nodes, such that the profiles contain values </br> </br>
	 * for all of the active variables : </br>
	 * ...excluded: target variables, index variables, blacklist, absolutely excluded variables </br> </br>
	 * 
	 * for that, we will retrieve the indices of the records collected in the nodes (as extension)
	 * 
	 * @return SomMapTable
	 */
	public SomMapTable exportExtendedSomMapTable() {
		 
		SomMapTable  smt = new SomMapTable();
	 
		try{
			// smt = exportSomMapTable(0) ; // 0 = include only the used variables
			
			// smt = exportSomMapTable(2) ; // 2 = only the TV column 

			smt = exportSomMapTable(1) ; // 1 = include all "non-blacks", 
		
			// the not used variables may contain inadequate values  ....
			
		}catch(Exception e){
			
			e.printStackTrace();
		}
				
		
		
		return  smt;
	}

	/**
	 * 
	 * extracting the profiles from the SomLattice into a simple table.
	 * 
	 * this then may be used for SomSprite or for SomIdeals
	 * 
	 * 
	 */
	public SomMapTable exportSomMapTable() {
		return exportSomMapTable(0) ;
	}
	/**
	 * 
	 * @param modus =0->only the variables contained in the metric ; =1->all variables
	 * @return
	 */
	public SomMapTable exportSomMapTable(int modus) {
		
		SomMapTable smt = new SomMapTable();
		
		 	
			
		 
		double profileValueForVar;
		boolean hb, nodeIsApplicable,sorted=false;
		String varLabel, variableLabel;
		int refNodeCount=0, vix,err=0, tvindex=-1 ;
		
		// ArrayList<MetaNode> nodes;
		MetaNode node;
		
		// nodes = dSom.somLattice.getNodes() ;
		ProfileVectorIntf  profileVector; 
		
		ArrayList<Double> pValues;
		Variables  variables;
		Variable variable ;
		
		ArrayList<String>  usedVarLabels;
		ArrayList<String>  nodeVarStr ;// = new ArrayList<String>();
		ArrayList<String>  activeVarStr = new ArrayList<String>();
		ArrayList<String>  compoundVarStr = new ArrayList<String>();
		
		
		ArrayList<Double> useIndicators ;
		
		variables = somData.getVariables() ;
		// not implemented: varList = variables.getActiveVariables();
		
		//ArrayList<Double> latticeuseIndicators = getSimilarityConcepts().getUsageIndicationVector() ;
		
		if (modus>=10){
			modus= modus-10;
			sorted=true;
		}

		/*
		 * we need to (re)calculate the whole profile, non-used profile entries are 0.0 by default ! 
		 */
		if (modus==1){
			//profileVector = nodes.get(5).getIntensionality().getProfileVector();
			updateIntensionalProfiles( modus ) ;
			
		}
		// profileVector = nodes.get(5).getIntensionality().getProfileVector();
		
		/*
		 * we need two loops, since compared/extracted nodes may be of different structure 
		 * the first one finding the compound vector that can be used to describe all nodes, */
		int zn = Math.min( 3, nodes.size()) ; // actually, zn is dependent on the differential structure of nodes: are nodes different ?
		for (int i=0;i<zn;i++){
			
			usedVarLabels = variables.getLabelsForUseIndicationVector(variables, useIndicators = nodes.get(0).getSimilarity().getUsageIndicationVector() ) ;	
			
			
			node = nodes.get(i) ;
			useIndicators = node.getSimilarity().getUsageIndicationVector();
			
			// ATTENTION this does NOT contain the target variable
			// also: blacklist..., 
			
			profileVector = node.getIntensionality().getProfileVector();
			
			nodeVarStr = node.getIntensionality().getProfileVector().getVariablesStr() ;
			
			// we do this for each node, though in most cases this is redundant, 
			// -> , nodes are NOT necessarily showing the same assignates/features !!
			for (int v=0;v<nodeVarStr.size();v++){ // nodeVarStr
				
				// exclude variables that have -1 as profile values (mv portion too large)
				varLabel = nodeVarStr.get(v) ;
				
				vix = variables.getIndexByLabel( varLabel ); 
				if (vix<0){continue;}
				
				variable = variables.getItem(vix) ;
				  
				hb = true;
			  
				variableLabel = variable.getLabel() ;
				 
				if (modus<=0){
					hb = ( (variable.isTV() || (useIndicators.get(v)>0.0) || (useIndicators.get(v)==-2.0)))  && 
					  	   (
								(variable.isID()==false) && 
								(variables.getBlacklistLabels().indexOf(varLabel)<0) && 
								(variables.getAbsoluteFieldExclusions().indexOf(varLabel)<0 ) &&
								(variable.isIndexcandidate()== false)
						    );
				}
				if (modus==1){
					
					hb = (variables.getBlacklistLabels().indexOf(varLabel)<0) && (variables.getAbsoluteFieldExclusions().indexOf(varLabel)<0 );
					if (hb){
					      hb = (variable.isIndexcandidate()== false) && (variable.isID()==false) ;
					}
					if (hb){
						hb = (variable.isTV() ) || (variable.isTVcandidate() == false)  ;
					}
				}
				if (modus>=2){
					hb = variable.isTV() ;
				}
				
				if (hb){
					// adding to the list of variables of the SomMapTable 	
					if (compoundVarStr.indexOf( varLabel )<0 ){
						compoundVarStr.add( variableLabel ) ;
						if (variable.isTV()){
							tvindex = v; 
						}
					}
				}
			} // v-> all variables
			
			// we might use some filter, such as MV in profile, size of node, or a filter by value of any variable
			// such filters may reduce the number of nodes we are effectively referring to 
			refNodeCount++;
			
		} // i-> all nodes
		
		refNodeCount = nodes.size() ;
		
		if (compoundVarStr.size()==0){
			return smt;
		}
		
		String tvLabel = somData.getVariables().getTargetVariable().getLabel() ;
		if (compoundVarStr.indexOf(tvLabel)<0){
			compoundVarStr.add(tvLabel) ;
			tvindex = compoundVarStr.size()-1 ;
		}
		if (tvindex<0){
			tvindex = compoundVarStr.indexOf(tvLabel);
		}
			
		smt.values = new double[refNodeCount][compoundVarStr.size()] ; 
		smt.variables = new String[ compoundVarStr.size()] ; 
		// that would be wrong!
		// smt.tvIndex = dSom.getSomData().getVariables().getTvColumnIndex() ;
		smt.tvIndex = compoundVarStr.indexOf(tvLabel) ; 
		int rnc=0;
		
		// compoundVarStr could be in a different order as compared to the table,
		// hence we should determine the indices in the variables-list, sort
		// the compoundVarStr accordingly and then proceed ...
		
		try{
			

			for (int i=0;i<nodes.size();i++){
				
				node = nodes.get(i) ;
				useIndicators = node.getSimilarity().getUsageIndicationVector();
				// also: blacklist...
				
				nodeIsApplicable=true;
				// apply the filter that acts on the node
				if (nodeIsApplicable==false){
					continue;
				}
				int nodesize = node.getExtensionality().getListOfRecords().size();
				profileVector = node.getIntensionality().getProfileVector();
				
				// we export used vars + TV
				pValues = profileVector.getValues() ;
				
				// we do this for each node, though in most cases this is redundant, 
				// e.g., nodes are NOT necessarily showing the same assignates/features !!
				for (int v=0;v<variables.size();v++){
					
					variable = variables.getItem(v) ;
					varLabel = variable.getLabel() ;
					
					/*
					hb = (useIndicators.get(v)>0.0) || (variable.isTV());
					if (hb){
						hb = (variable.isIndexcandidate()==false) && (variable.isID()==false) ;	
					}
					*/
					hb = compoundVarStr.indexOf(varLabel)>=0;
					
					if (hb){
						
						profileValueForVar = pValues.get(v);
						
						if ((modus==1) && (profileValueForVar<=0.0)){
							profileValueForVar = node.explicitReCalcOfProfilePosition(v) ;
							pValues.set(v,profileValueForVar) ;
						}
						if (activeVarStr.indexOf(varLabel)<0){
							activeVarStr.add( varLabel ) ;
						}
						// activeProfileValue = pValues.get(v) ;
						// activeProfileValues.add(activeProfileValue) ;
						
						vix = compoundVarStr.indexOf(varLabel) ;
						if (vix>=0){
							smt.variables[vix] = varLabel;
							smt.values[rnc][vix] = profileValueForVar;
							if (variable.isTV()){
								smt.tvIndex = vix;
							}
						}
					}
					
				} // v-> all variables
				rnc++;
				
				
			} // i-> all nodes
	
			vix=0;
			
		}catch(Exception e){
			int rc=smt.values.length;
			int cc=smt.values[0].length;
			
			out.printErr(2, "Error code = "+err+"\ntable dimensions, rows="+rc+", expected="+refNodeCount+" , columns="+cc+" , expected="+compoundVarStr.size()+" ") ;
			e.printStackTrace();
		}
				
		return smt;
	}

	
	/**
	 * by default, the profile is calculated only for those variables which are part of the actual selection;
	 * the reason is speed of processing in case of large values for the product (#variables x #records);</br></br>
	 *  
	 * yet, in some cases we need the statistics even for the disregarded variables: any kind of explorative investigation;</br></br>
	 * 
	 * by default, we only calc the very basic stats (mean, var, coeffvar), on (TODO) option for lattice we calc 
	 * the full stats incl histograms and higher moments;</br></br> 
	 * 
	 * note that it is sometimes advantageous to exclude the outliers from the extensional list for calculation of the intensions,
	 * for instance when screening dependencies, a limited idealization is oK... it will be tested anyway...
	 * 
	 * </br></br>
	 * another (TODO) option on level of lattice is to calc only those columns which are =0.0 in the profile
	 * 
	 * @param mode  0=only used; 1=all except black, 2=all all, incl. TV, blacklisted,</br> 
	 *              10,11,12 = additionally only top 80% of most similar records, if record count>=10 </br></br>  
	 */
	@SuppressWarnings("unused")
	public void updateIntensionalProfiles(int mode) {

		boolean calcThis;
		String varLabel = "" ;
		MetaNode node;
		ExtensionalityDynamicsIntf extension;
		
		int recordIndex, rcount, nodix=-1 ;
		ArrayList<IndexDistanceIntf> ixdsList; IndexedDistances ixds = new IndexedDistances();
		DataTable data;
		DataTableCol column;
		Double vqsum,vsum, value ;
		
		ArrayList<Double> useIndicators,blacklist ;
		ArrayList<String> varLabels ; 
		
		
		try {

			data = somData.getNormalizedSomData() ;
			
			for (int i = 0; i < nodes.size(); i++) {

				nodix = i;
				node = nodes.get(i);
				extension = node.getExtensionality();
				
				ixdsList = node.getListOfQualifiedIndexes();
				ixds.addAll( ixdsList ); 
				
				if (mode>=10){
					ixds.sort(1) ; // smallest distances first
					if (ixds.size()>=10){
						rcount = (int) (ixds.size()* 0.8) ;
					}else{
						rcount = ixds.size() ;
					}
				}else{
					rcount = ixdsList.size() ;
				}
				
				varLabels = node.getVariableLabels();
				
				// recixes = node.exportDataFromNode(-1, 0, false);
				useIndicators = node.getSimilarity().getUsageIndicationVector();
				blacklist = node.getSimilarity().getBlacklistIndicationVector() ;
				
				// all variables
				for (int v=0;v<varLabels.size();v++){
					
					varLabel = varLabels.get(v);
					// blacklisted? mode instead?
					calcThis = true;
					
					// Object obj = blacklist.get(v);
					// int b = (int) Math.round( (Double) obj );
					calcThis = ( blacklist.get(v) <= 0.0  ); // ? not a black listed variable ?
					
					// tv?
					if (calcThis){
						// varLabel, v 
						
					}
					if (calcThis){
						
						column = data.getColumn( varLabel) ;
						
						if (column==null){
							// on option: silent or Exception
							String str = "Critical error in VirtualLattice.updateIntensionalProfiles(), column not found by <varLabel> :\n"+
							             "   variable label : "+varLabel+" \n"+
				             			 "   node index     : "+nodix+"\n";
							throw(new Exception(str)) ;
						}
						// looping through all values in column colix of normalized data, collecting data, calculating basic stats, 
						// and saving it to stats description
						
						if (rcount > column.size()){
							rcount = column.size(); // just to be sure, should actually never happen
						}
						vsum=0.0; vqsum=0.0; int n=0;
						
						for (int k = 0; k < rcount; k++) {
							recordIndex = ixds.getItem(k).getIndex() ;
							value = -1.0;
							if (recordIndex>column.getCellValues().size()){
								// TODO: on option: exception, or silent
							}
							value = column.getCellValues().get(recordIndex);
							if ( value >= 0.0 ){
								vsum = vsum  + value;
								vqsum = vqsum  + value*value;
								n++;
							}
						}
						double _mean , _variance , coeffvar=-1.0;
						// post-calc & store
						_mean = vsum/((double)n);
						_variance = NumUtils.lazyVariance(vsum,vqsum,n);
						if (_mean!=0.0){
							coeffvar = _variance /_mean ;
						}
						
						if (Double.isNaN(_mean)){
							_mean = -1.0 ;
						}
						node.getProfileVector().getValues().set(v, _mean) ;
					} // calcThis ?
					
				}// v->
				
				
				
			} // i-> all nodes
			
			value=0.0;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * the lattice maintains master versions of variable lists usevectors etc.
	 * all nodes must know individually about this in the beginning, so this simply spreads the settings 
	 * into the population of nodes
	 * 
	 */
	public void spreadVariableSettings() {
		 
		ArrayList<Double> usagevector;
		int ix;
		boolean vlattMasterAvailable=false;
		ProfileVectorIntf vlattProfile;
		ArrayList<String> nodeVarStr ;
		
		usagevector =  new ArrayList<Double>();
		usagevector.addAll( similarityConcepts.getUsageIndicationVector() );
		
		vlattProfile = this.intensionalitySurface.getProfileVector(); 
		if ((vlattProfile.getVariables()!=null) && (vlattProfile.getVariables().size()>0)){
			vlattMasterAvailable=true;
		}
		            						String str = ArrUtilities.arr2Text(usagevector, 0);
		            						out.printErr(4, "spreadVariableSettings(), usagevector : "+str) ;
		//for (MetaNode node:nodes){
		for (int i=0;i<nodes.size() ;i++){
			
			MetaNode node = nodes.get(i) ;
				
			node.setSomData( this.somData);			
			node.getExtensionality().clear();
			
			
			 
			node.getExtensionality().getStatistics().resetFieldStatisticsAll() ; // statistics.fieldvalues ,  .variables
			
			if (vlattMasterAvailable){
				node.getExtensionality().getStatistics().setVariables( vlattProfile.getVariables() ) ;
			}
			
			ProfileVectorIntf  pv = node.getIntensionality().getProfileVector() ;
			// pv.g
			 
			int n =-1;
			ArrayList<Double> uv = node.getIntensionality().getUsageIndicationVector()  ;
			if (uv!=null){
				n = uv.size();
			}
			n=n+1-1;
			int tvix = somData.getVariables().getTvColumnIndex();
			
			node.getIntensionality().setUsageIndicationVector(usagevector);
			node.getIntensionality().setTargetVariableIndex( tvix ) ; 
			nodeVarStr = vlattProfile.getVariablesStr() ;
			
			if (vlattMasterAvailable){
				pv.setVariables( vlattProfile.getVariables() ); // the "values" vector will be adjusted accordingly ....
				pv.setVariablesStr(nodeVarStr) ;
				node.setVariableLabels(nodeVarStr);    
			}
			 
			// int zz = node.getIntensionality().getProfileVector().getVariablesStr().size();
			
			// this will set the reference, no copy is created !
			node.getSimilarity().setUsageIndicationVector(usagevector);
			
			ix = this.similarityConcepts.getIndexTargetVariable() ;
			if (ix<0){
				ix = tvix;
			}
			if (ix>=0){
				node.getSimilarity().setIndexTargetVariable(ix);
				//usagevector.set(ix, -2.0) ;
			}
			
			ix = this.similarityConcepts.getIndexIdColumn() ;
			if (ix>=0)node.getSimilarity().setIndexIdColumn(ix);
		}
		ix=0;
	}

	public void refreshDataSourceLink() {
		
		int iix=-1;
		DataTable datatable;
		
		ArrayList<Variable> variablesList;
		ArrayList<String>  varlabels ;
		ProfileVectorIntf profile;
		 
		
		datatable = somData.getNormalizedSomData() ;
		varlabels = datatable.getColumnHeaders() ;
		variablesList = somData.getVariables().getActiveVariables();
		
		similarityConcepts.adjustLengthOfUsageIndicationVector( varlabels.size()) ;
		
		iix = somData.getVariables().getIdColumnIndex() ;
		if (iix>=0){
			similarityConcepts.setIndexIdColumn( iix );
		}
		profile = intensionalitySurface.getProfileVector(); 
		
		profile.setVariables( variablesList ) ;
		profile.setVariablesStr(varlabels) ;
		 
		spreadVariableSettings();
		
		// test
		
		ArrayList<Double> useIndicators = nodes.get(0).getSimilarity().getUsageIndicationVector();
		ArrayList<String> nodeVarStr = nodes.get(0).getIntensionality().getProfileVector().getVariablesStr() ;
		
		int n= nodeVarStr.size();
			n= useIndicators.size();
		n=n+1-1;
	}

	// ..........................................
	
	public boolean setPreferredInitializationValues(ArrayList<String> pvars, ArrayList<Double> pvalues) {
		boolean rB=false;
		if ((pvars==null) || (pvars.size()<=1) || (pvalues==null) || (pvalues.size()<=1)){
			return false;
		}
		
		// else translate into preferred profile: default value =0.5, for pvar(i) take pvalue(i)
		
		
		return rB;
	}

	public void reInitNodeData(){
		MetaNode node;
		selectionResultsQueueDigester.isRunning=false;
		
		for (int i=0; i < nodes.size();i++){
			node = nodes.get(i) ;
			node.getExtensionality().clear() ;
			
		}
	}

	public void reInitNodeData(int mode){
		reInitNodeData();
		if (mode>0){
			// TODO reinit with random profiles
			
		}
	}

	/**
	 * queries can take very different amounts of time, thus we need a GUID identifier for correct returns 
	 *  
	 * 
	 */
	class ParticleSelectionQuery{

		SurroundResults results;
		String queryGuid  ;
		String rguid = "" ;
		int control=0;
		
		
		public ParticleSelectionQuery(int controlparam) {
			 control = controlparam;
		}

		public ArrayList<IndexDistanceIntf> getNodes(int index, int surroundN) {
			 
			ArrayList<IndexDistanceIntf> particlesIntf = new ArrayList<IndexDistanceIntf>();
			ArrayList<IndexDistance> particles = new ArrayList<IndexDistance>();
			
			// define selection size, otherwise the field will take the default !!!
			// this call returns immediately, providing the GUID as issued by the RepulsionField
			
			// somProcessParent: e.g. SomTargetedModeling
			queryGuid = somProcessParent.getNeighborhoodNodes( index ,surroundN);   
											out.printErr(3, "Lattice released a request for getNeighborhoodNodes, waiting for guid = "+queryGuid);

            
			// putting this to a map <guid,null>, the matching result object will contain the same Guid
			// but only, if it does not exist so far: if retrieval is not threaded, then it will be already there!								
			if (selectionResultsQueryMap.containsKey(queryGuid)==false){
				selectionResultsQueryMap.put(queryGuid,null) ; 
				
			}

			//if (fieldIsDynamic)
			{
				
				
				/* now waiting here ... this should not be too short !!!
				 * since the results have to be routed trough an event :
				 *    (e.g. sin SomFluid, SomTargetedModeling, onSelectionRequestCompleted()
				 * into  somLattice.getSelectionResultsQueue().add( results )
				 * and this queue will be handled by a digester (here below) and the
				 * "ParticleSelectionDispatcher" compartment
				 * 			
				 * The actual speed is limited by the waiting times in the queue handling 
				 * e.g. SelectionResultsQueueDigester
				 */
				long waitingStartTime = System.currentTimeMillis();
				int z=0; 
				int zlimit = 300;
				if (control>0){ // >0 == if the first trial failed
					zlimit=1200; // e.g. after shaking / reorganizing the field
				}
				while ( (z<zlimit) && // 500ms 
						(getGuidItemFromResultsQueue(queryGuid)==null)&&
						(selectionResultsQueryMap.get(queryGuid)==null)){ // (z<300) && // activate for NON _DEBUG abc124
					minidelay(2); // wait effectively 1ms 
					z++;
					
				}
				
				long waitingEndTime = System.currentTimeMillis();
				long waitingTime =  waitingEndTime- waitingStartTime;
											// out.print(5, "lattice have been waiting for selection: "+waitingTime +" ms") ;
								out.print(4, " - - - - - node list returned after : "+waitingTime+" ms...");
				rguid =  rguid + " ";
				if (selectionResultsQueryMap.get(queryGuid)==null){
					
					// really empty ?? or just the map?
					if (selectionResultsQueryMap.containsKey(queryGuid)){
						                     
						results = (SurroundResults) selectionResultsQueryMap.get(queryGuid);
						selectionResultsQueryMap.remove(queryGuid) ;
						
						if (results==null){

							results = getGuidItemFromResultsQueue(queryGuid);
							
							// remove this guid also from the simple queue
							removeGuidItemFromResultsQueue(queryGuid);

						}	
					} // Map.containsKey(queryGuid) ??
				
					if (results==null){
					
						out.printErr(2, "request for getNeighborhoodNodes (p-index:"+index+") NOT found after waiting "+waitingTime+" ms, would-be guid = "+queryGuid);
						out.delay(50);
						// We will repeat the query 1x !!

					    /*
						  	IndexDistance ixd = new IndexDistance(index, 0.0, "");
							ArrayList<IndexDistance> ixds = new ArrayList<IndexDistance>();
							ixds.add(ixd);
						*/
						particlesIntf = new ArrayList<IndexDistanceIntf>();
					}
					
				}else{
					// else::if Map.get(queryGuid)==null
					
					results = (SurroundResults) selectionResultsQueryMap.get(queryGuid);
					selectionResultsQueryMap.remove(queryGuid) ;
				}
				// only remove it here from incoming list !!!!
												out.print(5, "size of selectionResultsQueryMap : "+selectionResultsQueryMap.size()) ;
				
			} // fluid? => separate thread with necessity to wait for !
			
			selectionResultsQueueDigester.removeResultsFromDeliveryQueue(queryGuid);
				
			

			if (results==null){
				out.printErr(3, "retrieval of surround for index <"+index+"> was unexpectedly empty.");
				surroundError++;

				if (selectionResultsQueryMap.containsKey(queryGuid) )selectionResultsQueryMap.remove(queryGuid) ;

				// create & return an empty dummy
				return particlesIntf;
			}

			
			if ((results!=null) && (results.getParticlesAsIndexedDistances()!=null)){
				
				particles =  results.getParticlesAsIndexedDistances();
				particlesIntf = new ArrayList<IndexDistanceIntf>( particles );
				/* from here, it will return to 
				 * - the family of getSurround methods, or
				 * - getAffectedNodes() in class "DSomDataPerception"
				 */
				// 
			}
			
			
			results = null;
			latticeQuery = 0;
			return particlesIntf;
		}
	} // inner class ParticleSelectionQuery
	
	
	private void removeGuidItemFromResultsQueue(String qGuid) {
	
		SurroundResults r,_results = null;
		
		for (int i=0;i<selectionResultsQueue.size();i++){
			r = selectionResultsQueue.get(i);
			if (r.getGuid().contentEquals(qGuid)){
				selectionResultsQueue.remove(i);
				break;
			}
		}
		
	}

	private SurroundResults getGuidItemFromResultsQueue(String qGuid) {
		SurroundResults r,_results = null;
		int k=0;
		r=null;
		
		for (int i=0;i<selectionResultsQueue.size();i++){
			r = selectionResultsQueue.get(i);
			if (r.getGuid().contentEquals(qGuid)){
				_results = r; 
				break;
			}
		}
		k=k+1-1;
		return _results;
	}

	// ..........................................
	
	
	// ------------------------------------------------------------------------
	public int size(){
		return nodes.size();
	}
	
	public void clear(){
		
		for (int i=0;i<nodes.size();i++){
			nodes.get(i).clear(); 
		}

		if (selectionResultsQueue!=null)selectionResultsQueue.clear() ;
		if(selectionResultsQueryMap!=null)selectionResultsQueryMap.clear();
	
	}
	
	public void close(){
		clear() ;
		
		nodes.clear();
		nodeIndexMap.clear();

		selectionResultsQueueDigester.stop() ;
		
		modelProperties.close();
		// modelProperties=null;
		statsSampler =null;
		
		extensionalityDynamics.getListOfRecords().clear();
		extensionalityDynamics=null;
		
		selectionResultsQueueDigester.stop();
		out.delay(10);
		
		
		out.print(5, "closing VirtualLattice (address: "+this.toString()+")");
		System.gc();
	}
	
	@SuppressWarnings("static-access")
	public void minidelay(int nanos){
		try {
			Thread.currentThread().yield();
			Thread.currentThread().sleep(0,nanos);
		} catch (Exception e) {}
	}

	public MetaNode getNodeByNumId( long nodeGUID ){
		
		MetaNode node= null, _node;
		
		// TODO: use a TreeMap for this instead...
		for (int i=0;i<nodes.size();i++){
			_node = nodes.get(i);
			if (_node.getNodeNumGuid()==nodeGUID){
				node = _node;
				break;
			}
		}
		
		return node;
	}

	public MetaNode getNodeBySerialId( long serid ){
		
		MetaNode node= null, _node;
		
		// TODO: use a TreeMap for this instead...
		for (int i=0;i<nodes.size();i++){
			_node = nodes.get(i);
			if (_node.getSerialID()==serid){
				node = _node;
				break;
			}
		}
		
		return node;
	}

	
	public MetaNode getNode( int index ){
		MetaNode node=null ;
		if ((index>=0) && (index<nodes.size())){
			node = nodes.get(index) ;
		}
		// nodes.get(index) ;
		return node;
	}
	
	public ArrayList<MetaNode> getNodes() {
		return nodes;
	}


	public void indexOf( MetaNode node){
		nodes.indexOf(node);
	}
	
	public int indexOfSerial( long serialID ){
		// it is indeed monotonely increasing, though not starting with 1 and not without gaps;
		// -> We can check the middle position of the array, recursively
		// or we use a map.
		
		int nix = -1;
		
		if ((nodeIndexMap!=null) && (nodeIndexMap.containsKey(serialID))){
			nix = nodeIndexMap.get(serialID) ;
		}
		
		return nix;
	}
	
	public void addNode( MetaNode node){
		
		node.setLatticeProperties(latticeProperties);
		nodes.add(node) ;
	}
	
	public void removeNode( int index ){
		nodes.remove(index) ;
	}
	public void removeNodes( int[] index ){
		
	}
	public void removeNodesBeyondIndex( int index ){
		
		for (int i=index;i<nodes.size();i++){
			nodes.remove(i) ;
		}
	}
	public void removeNodes( ArrayList<Integer> index ){
		
	}
	 
	public void setNode(int index , MetaNode nodeObj){
		nodes.set(index, nodeObj);
	}


	public int getLatticeQuery() {
		return latticeQuery;
	}


	public Random getRndInstance() {
		return rndInstance;
	}


	public void setAveragePhysicalDistance(double dValue) {
		//  
		averagePhysicalDistance = dValue;
	}
	public double getAveragePhysicalDistance() {
		return averagePhysicalDistance;
	}
	public SimilarityIntf getSimilarityConcepts() {
		return similarityConcepts;
	}


	public IntensionalitySurfaceIntf getIntensionalitySurface() {
		return intensionalitySurface;
	}

	public void setExtensionalityDynamics(ExtensionalityDynamicsIntf extensionalityDynamics) {
		this.extensionalityDynamics = extensionalityDynamics;
	}

	public int getSurroundError() {
		return surroundError;
	}


	public void setSurroundError(int surroundError) {
		this.surroundError = surroundError;
	}


	public PrintLog getOut() {
		return out;
	}


	public int getMultiProcessingLevel() {		 
		return somProcessParent.getSfProperties().getMultiProcessingLevel() ;
	}// e.g. SomAppClassifier, moz, etc. ==SomProcessIntf 

	private MetaNode getBySerial(long nodeSerialID){
		MetaNode node=null;
		
		// TODO
		return node;
	}
	
						// registering call back LatticeFutureVisor 
	public String openLatticeFuture( LatticeFutureVisorIntf visor , int taskId, long nodeSerialID) {
		
		String guid = GUID.randomvalue() ;
		/*
		ArrayList<LatticeFutureVisorIntf>  superVisors 
		
		 */
		LatticeFuture f = new LatticeFuture(guid, nodeSerialID );
		
		openLatticeFutures.add(f);
		
		getBySerial(nodeSerialID).takeLatticeFuture(guid,taskId);
		
		return guid;
	}
	
	public String openLatticeFuture( LatticeFutureVisorIntf visor , int taskId ) {

		String guid = GUID.randomvalue() ;
		
		LatticeFuture f = new LatticeFuture(guid, nodes.size() );
		
		// now we have to link the particular callback LatticeFutureVisorIntf (its a dedicated object)
		// to this LatticeFuture object (which stores the parameters for counting down)
		f.registerCallbackInterests(visor) ;
		
		openLatticeFutures.add(f);
		
											out.print(4, "opening a new Lattice Future for all nodes, guid = "+guid);  
		
		// this guid is issued by the lattice, which knows about it
		// the lattice will provide this guid to all nodes, each of which maintain a FiFo list of such GUIDs
		// once they have finished, the send a signal to the lattice, which counts down -1 for each node,
		// if the count down arrives at 0, it will release the event to here
		
		for (int i=0;i<this.nodes.size();i++){
			nodes.get(i).takeLatticeFuture(guid, taskId);
		}
		
		return guid;
	}


	public void nodeInformsAboutCompletedTask(String guid) {
		// count down for the appropriate future, need to be in its own small object space, $
		// since the calls are unpredictable and parallel
		
		new LatticeFutureCountDown(guid); 

		
	}

	class LatticeFutureCountDown{
		
		public LatticeFutureCountDown(String guid){
			
			LatticeFuture f;
			
						 
			
			f = openLatticeFutures.getByGuid(guid);
			
			if (f==null){
				out.printErr(2, "   --- --- --- LatticeFutureCountDown, guid not found: "+guid);
				out.print(3, "   --- --- --- length of guid list : "+(openLatticeFutures.getItems().size())+" ,  "+openLatticeFutures.getItemsStr());
				return;
			}
			f.nCount = f.nCount - 1 ;
											out.print(4, "task completion count down (LatticeFuture), n="+f.nCount+" , guid: "+guid );
			if (f.nCount<=0){
				// call back to the "LatticeFutureVisor" object
				f.sendCompletionEvent();
				// openLatticeFutures.removeByGuid(guid) ;
			}
		}
		

	}

	public LatticePropertiesIntf getLatticeProperties() {
		return latticeProperties;
	}


	public void setLatticeProperties(LatticePropertiesIntf latticeProperties) {
		this.latticeProperties = latticeProperties;
	}


	public ModelProperties  getModelProperties() {
		return modelProperties;
	}


	public void setModelProperties(ModelProperties modelProperties) {
		this.modelProperties = modelProperties;
	}


	public boolean selectionResultsQueueDigesterAlive(){
		boolean hb = (selectionResultsQueueDigester!=null) && (selectionResultsQueueDigester.isRunning) ; 
		       	
		if (hb){
			// hb = (selectionResultsQueueDigester.vslSelectionDigest.isAlive()) ;
		}
		
		return hb ;
	}
	public void stop(){
		selectionResultsQueueDigester.isRunning = false;
	}

	public void startSelectionResultsQueueDigester(int z ){
		
		if (selectionResultsQueueDigester==null){
			out.printErr(2, "startSelectionResultsQueueDigester(b)");
			selectionResultsQueueDigester = new SelectionResultsQueueDigester(z) ;
		}
	}
	
	
	public void addResultsToWaitingQueue(SurroundResults _results) {
		selectionResultsQueue.add( _results );
	}

	public ArrayList<SurroundResults> getSelectionResultsQueue() {
		return selectionResultsQueue;
	}

	/**
	 * 
	 * this class is the backbone for the acceptance of messages issued by the RepulsionField about
	 * the selected indexes of nodes
	 * 
	 */
	class SelectionResultsQueueDigester implements Runnable{

		boolean isRunning =false, isWorking=false;
		
		Thread vslSelectionDigest;
		
		SurroundResults _results ;
		ArrayList<SurroundResults> waitingResults = new ArrayList<SurroundResults>();  
		
		public SelectionResultsQueueDigester(int z){
		
			vslSelectionDigest = new Thread (this,"vslSelectionDigest-"+z); 
			vslSelectionDigest.start() ;
		}
		
		public void stop(){
			
			isRunning=false;
			while (isWorking){
				out.delay(1) ;	
			}
			isWorking=false;
			
			if (selectionResultsQueue != null) {
				selectionResultsQueue.clear();
				out.delay(20);
				selectionResultsQueue = null;
			}
		}
		
		public void removeResultsFromDeliveryQueue( String guidStr){
			
			if (waitingResults==null){
				return;
			}
			int i=0;
			while (i<waitingResults.size()){
				SurroundResults wr = waitingResults.get(i);
				// waitingResults is used only for fluid som ?
				if (wr!=null){

					String gs = wr.getGuid();
					if ((gs!=null) && (gs.contentEquals(guidStr))){
						waitingResults.remove(i);
						i--;
					}
					
				}
				i++;
			}
			
		}
		
		@Override		
		public void run() {
			isRunning = true;
			int dt;
			isWorking=false;
			
			selectionResultsQueue = new ArrayList<SurroundResults>();
			
			try{
				while (isRunning){
					
					if (selectionResultsQueue.size()>0){
						out.print(5, "... selections waiting : "+selectionResultsQueue.size());
					}
					if (isWorking==false){
						isWorking = true;
						
						try{
							
							if ((selectionResultsQueue.size()>0) && 
								(waitingResults.indexOf(selectionResultsQueue.get(0))<0)){
								
								_results = selectionResultsQueue.get(0) ;
								waitingResults.add(_results);
								
								digestParticleSelection(_results) ;
								
								// not removing here !!! selectionResultsQueue.remove(0) ;
							}
						}catch(Exception e){}
						
						isWorking = false;
					} // isWorking ?
					
					if (selectionResultsQueue.size()==0){
						dt = 1 ;
						minidelay(dt);
						// a delay in nanoseconds...
					}else{
						// as long as there are items in the queue, there will be no waiting time
						dt = 0 ;
					}
					
				}// -> isRunning?
				
			}catch(Exception e){
				e.printStackTrace();
			}
			// out.printErr(2, "SelectionResultsQueueDigester() -> STOPPED");
			
		}
		
	}
	
	// the event arrives in SomFluid, which is calling this method here
	public void digestParticleSelection( SurroundResults results ) {
		// SurroundResults contains particle indexes, distances to request center, and the request GUID !
		
	
		// String str = arrutil.arr2text( results.getParticleIndexes() ) ;
		// out.print(2,"results returned to virtual lattice, "+str);
		
		new ParticleSelectionDispatcher( results );
		 
	}

	class ParticleSelectionDispatcher implements Runnable{

		String guidStr;
		SurroundResults results;
		Thread psd;
		
		public ParticleSelectionDispatcher(SurroundResults results) {
			this.results = results ;
			// psd = new Thread(this,"psd") ; psd.start();
			perform();
		}
		
		private void perform(){	
			guidStr = results.getGuid();
			
											out.print(3, "   - - - lattice is now checking match for received results by guid="+guidStr);
			
			if (selectionResultsQueryMap.containsKey(guidStr))
			{
				// we may even introduce a local selection buffer here, too !
				selectionResultsQueryMap.put(guidStr, results);
				         if (results==null){
				        	 out.print(2, "result for guid "+guidStr+" received by lattice, but container is empty!\n");
				         }else{
											out.print(3, "result for guid "+guidStr+"\n"+
													     "                put to <selectionResultsQueryMap>... lattice should be able to fetch it now...");
				         }
				// the map serves as a queue for named items !
											out.print(5, "   - - - ...match found!");
			}
			/*else{
				out.printErr(4, "   - - - lattice could not qualify results-guid = "+guidStr);	
			}
			*/
		}

		@Override
		public void run() {
			
			perform();
		}
		
	}
	 
	// ========================================================================
	
	public SimilarityIntf distributeSimilarityConcept() {
		 
		return similarityConcepts;
	}

	public SimilarityIntf distributeSimilarityConcept( long serialID ) {

		
		int nodeIndex;
		
		nodeIndex = indexOfSerial( serialID );
		
		if (nodeIndex>=0){
			similarityConcepts = nodes.get(nodeIndex).getSimilarity();
		}
		if (similarityConcepts==null){
			similarityConcepts =  new Similarity() ;
		}
		
		return similarityConcepts;
		 
	}

	public IntensionalitySurfaceIntf distributeIntensionalitySurface() {
		 
		
		return intensionalitySurface;
		
	}
	
	public IntensionalitySurfaceIntf distributeIntensionalitySurface( long serialID ) {
		
		IntensionalitySurfaceIntf intensionSurf = null ;
		int nodeIndex;
		
		nodeIndex = indexOfSerial( serialID );
		
		if (nodeIndex>=0){
			intensionSurf = nodes.get(nodeIndex).getIntensionality();
		}
		if (intensionSurf==null){
			intensionSurf = new IntensionalitySurface();
		}
		
		return intensionSurf;
		
	}

	
	public ExtensionalityDynamicsIntf distributeExtensionalityDynamics() {

		return extensionalityDynamics;
	}

	public ExtensionalityDynamicsIntf distributeExtensionalityDynamics(long serialID) {
		ExtensionalityDynamicsIntf extensiony = null ;
		int nodeIndex;
		
		nodeIndex = indexOfSerial( serialID );
		
		if (nodeIndex>=0){
			extensiony = nodes.get(nodeIndex).getExtensionality() ;
		}
		if (extensiony==null){
			extensiony = new ExtensionalityDynamics(somData, somProcessParent, somType);
		}
		// extensiony.setProcessHost( somProcessParent );
		return extensiony;
	}

	public MetaNodeConnectivityIntf distributeNodeConnectivity() {

		return nodeConnectivity;
	}

	public MetaNodeConnectivityIntf distributeNodeConnectivity(long serialID) {

		MetaNodeConnectivityIntf nodeconnexy = null ;
		int nodeIndex;
		
		nodeIndex = indexOfSerial( serialID );
		
		if (nodeIndex>=0){
			nodeconnexy = nodes.get(nodeIndex).getMetaNodeConnex() ;
		}
		if (nodeconnexy==null){
			nodeconnexy = new MetaNodeConnectivity();
		}
		
		return nodeconnexy;
	}


	public void activateNodes() {
		//
		for (int i=0;i<nodes.size();i++){
			nodes.get(i).setActivation(1);
		}
		
	}

	/**
	 * 
	 * this calculates the similarity of all records to profile 
	 * 
	 */
	public void calculateInternals() {
		 
		for (int i=0;i<this.nodes.size();i++){
			nodes.get(i).evaluateExtensions() ;
			
		}// i-> all nodes
		
		
		
	}


	public double getInitialRandomDivergence() {
		return initialRandomDivergence;
	}

	public void setInitialRandomDivergence(double initialRndDiv) {
		this.initialRandomDivergence = initialRndDiv;
	}


	public double getStDevForNodeInitialization() {
		return stDevForNodeInitialization;
	}


	public void setStDevForNodeInitialization(double stDevForNodeInitialization) {
		this.stDevForNodeInitialization = stDevForNodeInitialization;
	}


	public StatisticSample getRndSamplerInstance() {
		return statsSampler;
	}

	public boolean isFieldIsDynamic() {
		return fieldIsDynamic;
	}

	public void setFieldIsDynamic(boolean fieldIsDynamic) {
		this.fieldIsDynamic = fieldIsDynamic;
	}

	public void setDataSize(int totalRecordCount) {
		dataSize = totalRecordCount;
	}

	/**
	 * @return the dataSize = total number of records fed into the lattice
	 */
	public int getDataSize() {
		return dataSize;
	}

	public void handlingRoutedSelectionEvent(SurroundResults results) {
		// TODO: this should be immediately forked into objects, since the requests could be served in parallel
		//       XXX XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX    URGENT  abc124 

		SurroundResults clonedResults;
		String str ;
		int[] particleIndexes;
		
		if (results==null){
			out.printErr(2, "event <onSelectionRequestCompleted()> received message with <resultsObj>, but container was empty!") ;
		}else{
			out.print(4, "event <onSelectionRequestCompleted()> received message with <resultsObj> for guid "+results.getGuid()+"... ") ;
		}
		/*
		 *  here we have to use a message queue running in its own process, otherwise
		 *  the SurroundRetrieval Process will NOT be released...
		 *  We have a chain of direct calls
		 *  
		 */
		
		// we have to prepare the results in the particlefield!
		// we need the list of lists: 
		// for each particle he have a list of indexes ArrayList<Long> getIndexesOfAllDataObject()
		particleIndexes = results.getParticleIndexes();
		
		clonedResults = (SurroundResults) sob.decode( sob.encode(results) );
					if ((clonedResults==null) || (clonedResults.getParticleIndexes()==null)){
						return ;
					}
											int n = clonedResults.getParticleIndexes().length   ;
											str = clonedResults.getGuid() ;
												
											out.print(5, "particlefield delivered a selection (n="+n+") for GUID="+str);
	
		
		// this result will then be taken as a FiFo by a digesting process, that
		// will call the method "digestParticleSelection(results);"
		// yet, it is completely decoupled, such that the current thread can return and finish
		
		if (selectionResultsQueueDigesterAlive()==false){
											out.print(3, "restarting selection-results queue digester...");
			startSelectionResultsQueueDigester(-1);
			out.delay(50);
		}
		
		ArrayList<SurroundResults> rQueue = getSelectionResultsQueue(); 
		// rQueue.add( clonedResults );
		addResultsToWaitingQueue(clonedResults );
		// .getSelectionResultsQueue().add( clonedResults );
		
		//out.print(2, "size of rQueue : "+rQueue.size());
		return; 
	}

	public boolean isInitializing() {
		return isInitializing;
	}
	// this is set by the master process, on the level of class DSomCore
	public void setInitializing(boolean flag) {
		isInitializing = flag;
	}

	public long setLatestNodeIndex(long idvalue) {
		
		if (latestNodeIndex < idvalue){
			latestNodeIndex = idvalue;
		}else{
			latestNodeIndex = idvalue;
		}
		
		return latestNodeIndex;
	}

	public long getLatestNodeIndex() {
		return latestNodeIndex;
	}

	public long getNextNodeSerial() {
		latestNodeIndex++;
		return latestNodeIndex;
	}

	public ArrayList<Long> getNodeGuids() {
		if (nodeGuids==null){
			nodeGuids = new ArrayList<Long>();
		}
		return nodeGuids;
	}

	public void createGuidEntries(long serialID, long numID, MetaNodeIntf node) {
		
		nodeGuids.add(numID);
		nodeUidMap.put(numID, (Object)node); // MetaNodeIntf
		nodeSerialsMap.put(numID, serialID) ;
	}
	
	public MetaNodeIntf getNodeByNumGuid( long numID ){
		MetaNodeIntf  node=null;
		
		try{
		
			if (nodeUidMap.containsKey(numID)){
				node = (MetaNodeIntf) nodeUidMap.get(numID);
			}
			
		}catch(Exception e){
			
		}
		
		return node;
	}

	/** this is the (almost) unique identifier pointing to the this lattice as a long
	 * we need it as a reference in the table that connects nodes and documents 
	 */
	public long getNumGuid() {
		return numGuid;
	}

	public Map<Long, Object> getNodeUidMap() {
		return nodeUidMap;
	}

	public Map<Long, Long> getNodeSerialsMap() {
		return nodeSerialsMap;
	}

	public IndexedDistances getNodeSizes( boolean sortList) {
		// ArrayList<Integer> sizes = new ArrayList<Integer> ();
		IndexedDistances ixds = new IndexedDistances (); 
		int n;
		n=0;
		
		for (int i=0;i<nodes.size();i++){
			n = nodes.get(i).getExtensionality().getCount() ; 
			ixds.add( new IndexDistance(i,(double)n,"") );
		}
		if (sortList){
			ixds.sort(-1);
		}
		
		return ixds;
	}
	
		
}











