package org.NooLab.somfluid.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import java.util.Random;


import org.math.array.StatisticSample;


import org.NooLab.utilities.ArrUtilities; 
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
 
import org.NooLab.repulsive.components.data.SurroundResults;
 
 
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
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.communication.LatticeFutureVisorIntf;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.NumUtils;
 




public class VirtualLattice implements LatticeIntf{

	public static final double __DEFAULT_NODE_INIT_STDEV = 0.16;

	LatticePropertiesIntf latticeProperties;

	ArrayList<MetaNode> nodes = new ArrayList<MetaNode>();
	
	Map<Long, Integer> nodeIndexMap = new TreeMap<Long, Integer>() ;
	
	//SomFluid somFluidParent;
	SomProcessIntf somProcessParent  ;
	
	SomDataObject somData;
	
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
	
	ArrUtilities arrutil = new ArrUtilities (); 
	PrintLog out = new PrintLog(2,true);

	private double averagePhysicalDistance = 1.0;

	public boolean bmuBufferActivated = false;

	private double initialRandomDivergence = 0.2;
	private double stDevForNodeInitialization = __DEFAULT_NODE_INIT_STDEV ;

	private int dataSize;
	
	
	// ========================================================================
	public VirtualLattice(SomProcessIntf parent, LatticePropertiesIntf latticeProps, int svlIndex){
		  
		latticeProperties = latticeProps;
		somProcessParent = parent;
		
		 
		
		extensionalityDynamics = new ExtensionalityDynamics(somData) ; 
		
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
	public ArrayList<IndexDistanceIntf> getNeighborhoodNodes( int index, int nodeCount ) {
		
		ArrayList<IndexDistanceIntf> selectedNodes = new ArrayList<IndexDistanceIntf>();
		
	
		/*
		 * ONLY FOR DEBUG TO PREVENT PARALLEL CALLS
		 */
			if (latticeQuery>0){
				return selectedNodes;
			}
			latticeQuery = 1;
		/*
		 * 
		 */
		
		// forking the request immediately to its own name space  
		selectedNodes = (new ParticleSelectionQuery()).getNodes( index, (int) (nodeCount*1.3) );
		
		 
		return selectedNodes;
	}
	
	
	/**  on option, we use node statistics for optimization, or decisions about growth    */
	@SuppressWarnings("unchecked")
	public void establishProperNodeStatistics() {
		
		int extRecCount, ix,fix,fc,rix ;
		boolean reCalc;
		MetaNode node;
		Variables variables;
		double v;
		ArrayList<Double> fieldValues = new ArrayList<Double>();
		ArrayList<Integer> recIndexes;
		ArrayList<Integer> useIndexes ;
		variables = somData.variables ;
		
		ArrayList<BasicStatisticalDescription> bsds;
		BasicStatisticalDescription bsd ;
		
		reCalc = false;
		// 
		for (int i=0;i<nodes.size();i++){
			node = nodes.get(i);
			useIndexes = (ArrayList<Integer>) variables.transcribeUseIndications(node.getIntensionality().getUsageIndicationVector());
			// like: [7, 8, 10] , without TV !

			extRecCount = node.getExtensionality().getListOfRecords().size();
			if (extRecCount > 0) {
				// node.getSimilarity().getUseIndicatorArray();
				bsds = node.getExtensionality().getStatistics().getFieldValues();
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
				bsds = node.getExtensionality().getStatistics().getFieldValues();
				if (extRecCount > 0) {
					
					// all fields (variables)...
					for (int f=0;f<useIndexes.size();f++){

						fix = useIndexes.get(f);
						bsd = bsds.get(fix);
						bsd.reset() ;
						 
						
						DataTableCol dcol = somData.normalizedSomData.getColumn(fix);
						fieldValues.clear() ;
						// re-calculate -> 1. collecting the values into a temp list
						// for field f, and record indexes as in list of records from extensionality
						recIndexes = node.getExtensionality().getListOfRecords() ;
						for (int r=0;r<extRecCount;r++){
							rix = recIndexes.get(r) ;
							v = dcol.getCellValues().get(rix) ;
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
		/*
		 	double[][] values = new double[0][0] ; 
			String[]   variables = new String[0] ;
		 */
		
		boolean hb, nodeIsApplicable;
		String varLabel;
		int refNodeCount=0, vix, tvindex=-1 ;
		
		// ArrayList<MetaNode> nodes;
		MetaNode node;
		
		// nodes = dSom.somLattice.getNodes() ;
		ProfileVectorIntf  profileVector; 
		
		ArrayList<Double> pValues;
		Variables  variables;
		Variable variable ;
		
		
		ArrayList<String>  nodeVarStr ;// = new ArrayList<String>();
		ArrayList<String>  activeVarStr = new ArrayList<String>();
		ArrayList<String>  compoundVarStr = new ArrayList<String>();
		
		
		ArrayList<Double> useIndicators ;
		
		variables = somData.getVariables() ;
		// not implemented: varList = variables.getActiveVariables();
		
		//ArrayList<Double> latticeuseIndicators = getSimilarityConcepts().getUsageIndicationVector() ;
		
		/*
		 * we need to (re)calculate the whole profile, non-used profile entries are 0.0 by default ! 
		 */
		if (modus>0){
			updateIntensionalProfiles( modus ) ;
		}
		if (modus>1)modus=1;
		
		/*
		 * we need two loops, since compared/extracted nodes may be of different structure 
		 * the first one finding the compound vector that can be used to describe all nodes,
		 */
		for (int i=0;i<nodes.size();i++){
			
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
				
				// hb =  variable.isTV(); // variable.isUsed() ||
				{
					if ((hb) || (variable.isTV() )){
						hb = (variable.isIndexcandidate()==false) && (variable.isID()==false) && (variable.isTVcandidate()==false) ;	
					}
					if (hb==false){
						continue;
					}
				}
				
				varLabel = variable.getLabel() ;
				
				if (hb){
					hb = (useIndicators.get(v)>0.0) || (useIndicators.get(v)==-2.0) ;
				}
				if (modus>=1){
					hb = ((useIndicators.get(v)>=0.0) || (useIndicators.get(v)==-2.0) )&& (variables.getBlacklistLabels().indexOf(varLabel)<0) ;
				}
				if (hb){
					
					if (compoundVarStr.indexOf( varLabel )<0 ){
						compoundVarStr.add( variable.getLabel() ) ;
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
		smt.tvIndex = tvindex ; 
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
				
				profileVector = node.getIntensionality().getProfileVector();
				
				// we export used vars + TV
				pValues = profileVector.getValues() ;
				
				// we do this for each node, though in most cases this is redundant, 
				// et, nodes are NOT necessarily showing the same assignates/features !!
				for (int v=0;v<variables.size();v++){
					
					variable = variables.getItem(v) ;
					varLabel = variable.getLabel() ;
					
					hb = (useIndicators.get(v)>0.0) || (variable.isTV());
					if (hb){
						hb = (variable.isIndexcandidate()==false) && (variable.isID()==false) ;	
					}
					hb = compoundVarStr.indexOf(varLabel)>=0;
					
					if (hb){
						
						
						activeVarStr.add( varLabel ) ;
						
						// activeProfileValue = pValues.get(v) ;
						// activeProfileValues.add(activeProfileValue) ;
						
						vix = compoundVarStr.indexOf(varLabel) ;
						if (vix>=0){
							smt.variables[vix] = varLabel;
							smt.values[rnc][vix] = pValues.get(v);
							if (variable.isTV()){
								smt.tvIndex = vix;
							}
						}
					}
					
				} // v-> all variables
				rnc++;
				
				
			} // i-> all nodes
	
			
			
			
		}catch(Exception e){
			
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

			data = somData.normalizedSomData ;
			
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
		            						String str = arrutil.arr2text(usagevector, 0);
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
			int tvix = somData.variables.getTvColumnIndex();
			
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
		 
		
		datatable = somData.normalizedSomData ;
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
		
		public ParticleSelectionQuery() {
			 
		}

		public ArrayList<IndexDistanceIntf> getNodes(int index, int surroundN) {
			 
			ArrayList<IndexDistanceIntf> particlesIntf = new ArrayList<IndexDistanceIntf>();
			ArrayList<IndexDistance> particles = new ArrayList<IndexDistance>();
			
			// define selection size, otherwise the field will take the default !!!
			// this call returns immediately, providing the GUID as issued by the RepulsionField
			queryGuid = somProcessParent.getNeighborhoodNodes( index ,surroundN);   
											out.print(4, "request for getNeighborhoodNodes, waiting for guid = "+queryGuid);
											
			// putting this to a map <guid,null>, the matching result object will contain the same Guid
			// but only, if it does not exist so far: if retrieval is not threaded, then it will be already there!								
			if (selectionResultsQueryMap.containsKey(queryGuid)==false){
				selectionResultsQueryMap.put(queryGuid,null) ;
			}
			// now waiting here XXX			
			int z=0;
			while ((z<2000) && (selectionResultsQueryMap.get(queryGuid)==null)){ // (z<300) && // activate for NON _DEBUG abc124
				minidelay(10); 
				z++;
			}
			
			if (selectionResultsQueryMap.get(queryGuid)==null){
				out.print(3, "request for getNeighborhoodNodes NOT found, would-be guid = "+queryGuid);
				IndexDistance ixd = new IndexDistance(index,0.0,"");
				ArrayList<IndexDistance> ixds = new ArrayList<IndexDistance>();
				ixds.add(ixd);
				particlesIntf = new ArrayList<IndexDistanceIntf>( ixds);
			}
											out.print(5, "size of selectionResultsQueryMap : "+selectionResultsQueryMap.size()) ;
			if (selectionResultsQueryMap.containsKey(queryGuid)){
				results = (SurroundResults) selectionResultsQueryMap.get(queryGuid);
				selectionResultsQueryMap.remove(queryGuid) ;
			}else{
				// create an empty dummy
				return particlesIntf;
			}
			
			if ((results!=null) && (results.getParticlesAsIndexedDistances()!=null)){
				particles =  results.getParticlesAsIndexedDistances();
				particlesIntf = new ArrayList<IndexDistanceIntf>( particles );
			}else{
				out.printErr(3, "retrieval of surround for index <"+index+"> was unexpectedly empty.");
				surroundError++;
				if (selectionResultsQueryMap.containsKey(queryGuid) )selectionResultsQueryMap.remove(queryGuid) ;
			}
			
			results = null;
			latticeQuery = 0;
			return particlesIntf;
		}
	
		@SuppressWarnings("static-access")
		public void minidelay(int nanos){
			try {
				Thread.currentThread().yield();
				Thread.currentThread().sleep(0,nanos);
			} catch (Exception e) {}
		}
	} // inner class ParticleSelectionQuery
	
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
	
	public MetaNode getNodeByNumId( long nodeID ){
		
		MetaNode node= null, _node;
		
		// TODO: use a TreeMap for this instead...
		for (int i=0;i<nodes.size();i++){
			_node = nodes.get(i);
			if (_node.getNumID()==nodeID){
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
				out.print(3, "   --- --- --- length of guid list : "+(openLatticeFutures.items.size())+" ,  "+openLatticeFutures.getItemsStr());
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

	// the event arrives in SomFluid, which is calling this method here
	public void digestParticleSelection( SurroundResults results ) {
		// SurroundResults contains particle indexes, distances to request center, and the request GUID !
		
	 
		// String str = arrutil.arr2text( results.getParticleIndexes() ) ;
		// out.print(2,"results returned to virtual lattice, "+str);
		
		new ParticleSelectionDispatcher( results );
		 
	}
	
	public ArrayList<SurroundResults> getSelectionResultsQueue() {
		return selectionResultsQueue;
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
							
							if (selectionResultsQueue.size()>0){
								_results = selectionResultsQueue.get(0) ;
								
								digestParticleSelection(_results) ;
								
								selectionResultsQueue.remove(0) ;
							}
						}catch(Exception e){}
						
						isWorking = false;
					} // isWorking ?
					
					if (selectionResultsQueue.size()==0){
						dt = 1 ;
						out.delay(dt);
					}else{
						dt = 0 ;
					}
					
				}// -> isRunning?
				
			}catch(Exception e){
				e.printStackTrace();
			}
			// out.printErr(2, "SelectionResultsQueueDigester() -> STOPPED");
			
		}
		
	}
	
	class ParticleSelectionDispatcher{

		String guidStr;
		
		public ParticleSelectionDispatcher(SurroundResults results) {
			
			guidStr = results.getGuid();
			
											out.print(5, "   - - - lattice is now checking match for results by guid="+guidStr);
			
			if (selectionResultsQueryMap.containsKey(guidStr)){
				// we may even introduce a local selection buffer here, too !
				selectionResultsQueryMap.put(guidStr, results);
				// the map serves as a queue for named items !
											out.print(5, "   - - - ...match found!");
			}else{
				out.printErr(4, "   - - - lattice could not qualify results-guid = "+guidStr);	
			}
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
			extensiony = new ExtensionalityDynamics(somData);
		}
		
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

	public void setDataSize(int totalRecordCount) {
		dataSize = totalRecordCount;
	}

	/**
	 * @return the dataSize = total number of records fed into the lattice
	 */
	public int getDataSize() {
		return dataSize;
	}

	
		
}











