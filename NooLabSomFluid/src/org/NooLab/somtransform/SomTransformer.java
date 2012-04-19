package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;

import org.NooLab.somsprite.AnalyticFunctionSpriteImprovement;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;



/**
 * 
 * the most basic functionality of this is normalization of input data;
 * 
 * it also maintains a "transformation model", which is a collecotin of stacks of elementary transformations;
 * any of the variables can be assigned such a stack 
 * 
 * Handling the algorithm stacks
 * (1) transformation and creating new columns is strictly separated
 * (2) algorithms can change the format from in-data to out-data 
 * (3) stack can be supplemented with plug-in algorithms, that need to implement 
 *     one of three interfaces: for transforming, writing new columns, or measurements
 * 
 * 
 * 1+2 mark a strong difference to Prospero's transformer
 * 
 * the very first position in the transformation stack receives raw, NON_NORMALIZED data !!!!
 * 
 * TODO: analysis of residuals relative to outcome, also on the level of modeling
 * 
 */
public class SomTransformer {

	//SomFluidFactory sfFactory;
	SomDataObject somData;
	SomFluidProperties sfProperties;
	
	
	/** data as it has been imported */
	DataTable dataTableObj ;
	/** transformed data, as defined by the transformation model */
	DataTable dataTableNormalized ;
	
	/** TODO: they are always and immediately saved to file  */
	ArrayList<CandidateTransformation> candidateTransformations = new ArrayList<CandidateTransformation> ();
	
	ArrayList<Integer> addedVariablesByIndex = new ArrayList<Integer>();
	
	TransformationModel transformationModel;
 
	SomTransformerInitialization initialization;
	
	/**
	 * for any variable that is already know as blacklisted apriori, we may block its transformation 
	 */
	boolean excludeBlacklisted = false ;
	int realizedCount;
	
	
	PrintLog out ;
	private ArrUtilities arrutil = new ArrUtilities();
	private StringsUtil strgutils = new StringsUtil ();
	
	// ========================================================================
	public SomTransformer( SomDataObject sdo, SomFluidProperties sfprops) {

		
		somData = sdo;
		sfProperties = sfprops ;
		
		dataTableObj = somData.getDataTable() ; // this nows about its serialized filebuffer 
		
		transformationModel = new TransformationModel(this, somData);
		
		initialization = new SomTransformerInitialization();
		
		out = somData.getOut() ;
	}

 
	/**
	 * creates the basic structures : 
	 * for each of the variables a TransformationStack will be initialized
	 * 
	 */
	public void initializeTransformationModel(){
		
		int ix;
		Variables variables;
		Variable variable;
		TransformationStack tstack ;
		
		variables = somData.getVariables() ;
		if (transformationModel.variableTransformations==null){
			transformationModel.variableTransformations = new ArrayList<TransformationStack>();
		}
		// transformationModel.variableTransformations.clear() ;
		
		for (int i=0;i<variables.size();i++){
			
			variable = variables.getItem(i) ;
			
			ix = this.transformationModel.findTransformationStackByVariable( variable ); // not the label, but the object !
			
			if (ix<0){
				tstack = new TransformationStack();
			
				// such the stack knows about the raw in-format (valueScaleNiveau), the label, basic min & max 
				tstack.baseVariable = variable; 
				tstack.varLabel = variable.getLabel() ;
			
				// TODO: the variable should know from type checking, whether there are any negative values,
				//       we conclude that neg values are semantically different, 
				transformationModel.variableTransformations.add(tstack) ;
			}
		}
		 
		initialization.setInitialized(true, somData.getRecordCount(), variables.size()  );
	}
 

	@SuppressWarnings("unchecked")
	public int basicTransformToNumericalFormat(){
			
			int result = -1;
			int cn,n,n1,n2 ;
			boolean treatit, copyIsMandatory;
			int newIndex, currentFormat;
			String varLabel ;
			ArrayList<Double> numvals;
			ArrayList<String> strvals ;
			DataTableCol dtc ;
			
			Variable v,newVariable ;
	
			StackedTransformation st,sta,stp ;
			Variables variables;
			
			TransformationStack newTStack,varTStack ;
			
			// known: dataTableObj = the data table , the items in the tstack have access to the variable objects
			// uses transformations as algos even for basic stuff !!
			
			int fieldExclusionsMode = sfProperties.getAbsoluteFieldExclusionsMode() ;
			ArrayList<String> absoluteBlackList = sfProperties.getAbsoluteFieldExclusions() ;
			boolean isBlacklisted, importIsDenied;  
			
			try{
				
				variables = this.somData.getVariables() ;
				
				
				
				// repeat until all stackpositions have a defined outgoing data format = num
				boolean numFormatsAvail=false;
				
				while (numFormatsAvail==false){
					
					cn = transformationModel.variableTransformations.size() ;
					
					int i=-1;
					
					
					// transformationModel.variableTransformations size remains 0 for raw variables ???
					while (i<cn-1){
						i++;
						
						
						varTStack = transformationModel.variableTransformations.get(i) ;
						v = varTStack.baseVariable ;
						
						varLabel = v.getLabel() ;
						currentFormat = varTStack.getLatestFormat() ; // is a sibling of the call "varTStack.getFormatAtStackPos()" ;
						if (currentFormat<0){
							currentFormat = v.getRawFormat() ;
						}
						
						isBlacklisted = absoluteBlackList.indexOf(varLabel)>=0 ;
						if (isBlacklisted){
							continue;
						}

						// some variables need to remain in raw state, such as ID or TV columns;
						// for those we have to create a copy directly from the raw values
						copyIsMandatory = false;
						if ((v.isID()) || (v.isIndexcandidate() )){ // (v.isTV())
							copyIsMandatory = true;
						}
						if ((v.getRawFormat() >= DataTable.__FORMAT_ORDSTR ) || (v.getRawFormat() == DataTable.__FORMAT_DATE)){
							// 
							copyIsMandatory = true;
						}
						
						// 
						if ((copyIsMandatory) && (v.isDerived()==false)){
	
							// create a copy, incl. data in the table, extension of the transformation model
							// it takes the format id from its predecessor 
							// we address the base table here , since we are ahead of normalization 
							// this also sets "inputVarLabels" in created stack
							newTStack = createAddVariableByCopy( 0, varTStack);
 							
							cn = transformationModel.variableTransformations.size() ;
							st = varTStack.getLastPosition();
							st = varTStack.getFirstPosition();
							 
							//if (v.getRawFormat() <= DataTable.__FORMAT_INT)
							{
								
								dtc = somData.getDataTable().getColumn(i);
								strvals = dtc.getCellValueStr() ; 
								numvals = dtc.getCellValues() ;
								double maxOfNumVals = arrutil.arrayMax(numvals, -1.0) ; 
								//st.outData = (ArrayList<Double>) st.inData.get(0);
								n1 = strvals.size();
								n2 = numvals.size();
								if ((n1>n2) || ((n1>0) && (maxOfNumVals<0)) ){
									st.getInData().add( strvals );
								}else{
									st.getInData().add( numvals );
								}
							}
							
							varTStack.update() ;
							// newTStack is not prepared yet here...  we will anyway meet it later in the loop 
							// connectTransformStacksForData( newTStack, 1,false ) ; //
							
							continue;
						}
						
						
						if ((v.getRawFormat() == DataTable.__FORMAT_DATE) && (v.isDerived() )){
							
							varLabel = v.getLabel() ;
							
							st  = varTStack.introduceAlgorithmizedStackPosition("DateConverter") ;
								  varTStack.introduceAlgorithmizedStackPosition("MissingValues") ;
								  varTStack.introduceAlgorithmizedStackPosition("StandardStatistics") ;
								  
							dtc = somData.getDataTable().getColumn(i);
							strvals = dtc.getCellValueStr() ;
							st.getInData().add( strvals );
							
							varTStack.update();
							
							varTStack.setLatestFormat( 1 ) ;
							v.setRawFormat(1) ;
							continue;
						}
						
	if (i>=16){
		result = -2;
	}
						// note that mv in strings is encoded as "-M.V."
						// once transformed into num, we have to change the format in the normalized table
						if ((v.getRawFormat() == DataTable.__FORMAT_ORDSTR) && (v.isDerived() )){
							
							varLabel = v.getLabel() ;
							
							st  = varTStack.introduceAlgorithmizedStackPosition("NomValEnum") ;
								  varTStack.introduceAlgorithmizedStackPosition("MissingValues") ;
								  
							String tvVarLabel="";
							int tvix = -1 ;
							
							boolean tgmod = sfProperties.getModelingSettings().getTargetedModeling();
							tvVarLabel = sfProperties.getModelingSettings().getActiveTvLabel();
							if (tvVarLabel.length()==0){
								this.somData.getVariables().getTargetVariable().getLabel() ;
							}
							tvix = this.dataTableObj.getColumnHeaders().indexOf(tvVarLabel) ;
						tgmod = false ;	
							if ( (tgmod) && (tvVarLabel.length()>0) && (tvix>=0)){
								
								sta = varTStack.introduceAlgorithmizedStackPosition("OptimalScaling") ;
	
								ArrayList<Object> params = new ArrayList<Object>(); 
								params.add( "tv:"+tvVarLabel ) ; // should be parameterized and user-based ....
							    ((AlgorithmIntf)sta.algorithm).setParameters(params) ;
							} 
							
							varTStack.introduceAlgorithmizedStackPosition("StandardStatistics") ;
							
							// somData.getDataTable().getColumn(i).getCellValues() ) ;
							dtc = somData.getDataTable().getColumn(i);
							strvals = dtc.getCellValueStr() ;
							st.getInData().add( strvals );
							
	if (varLabel.toLowerCase().contains("sales")){
		n=0;
	}
							varTStack.update();
							 
							
							varTStack.setLatestFormat( 1 ) ;
							v.setRawFormat(1) ;
							continue;
						}	
						 
						// if it is a string, check for possible NVE (should be indicated as format = 5)
						
						// if it is a string, and no NVE can be applied, check for numerical extracts
						// are there constant parts in the string? hyphens? other punctuations?
	
						// is it a date? -> create serial representation, 
						// rel date = age (assuming some lowest possible date from the data as 5 times the max age)
						
						
						// is it a time? -> create serial representation, indicate a particular data type: circular data (min=0=max)
						
						
						// 
						if ((v.getRawFormat() >= DataTable.__FORMAT_ID) && (v.getRawFormat() <= DataTable.__FORMAT_INT)){
							
							if (varTStack.items.size()==0){
								
								sta = varTStack.introduceAlgorithmizedStackPosition("MissingValues") ;
								      varTStack.introduceAlgorithmizedStackPosition("StandardStatistics") ;
							}
							
							sta = varTStack.getItems().get(0) ;
							
							if (sta.getInData().size()==0){
								int ix = i;
								if (v.isDerived()){ // v.parentTransformID = ba9a0d28-c345-477e-be9d-0b8997bca1a9
									//  
									connectTransformStacksForData( varTStack, 1,false ) ; // 
									
								} else{
									
									if (ix>=0){
										// these data will be replaced in the StackedTransformation will be replaced by 
										// normalized values !
										sta.getInData().add( somData.getDataTable().getColumn(ix).getCellValues() ) ;
										n=sta.getInData().size() ;
										
if (varLabel.toLowerCase().contains("sales")){
	n=0;
}								
										
									}else{
										ix=-1;
									}	
								}
								 
								
								varTStack.update();
								n=0;
							} // col is num && no data defined so far?
							
						}
						 
						// in case of large tables -> write column to a buffer file
						
					} // i-> all variables == all positions in transformationmodel = list of tstacks
					
					
					ArrayList<Double> primaryInValues ;
					ArrUtilities arrutils = new ArrUtilities();
					double vmax ;
					
					for (int t=0;t<transformationModel.variableTransformations.size();t++){
						
						varTStack = transformationModel.variableTransformations.get(t) ;
						
						if ((variables.getBlacklistLabels().indexOf( varTStack.varLabel )>=0) ||
							(variables.getItemByLabel(varTStack.varLabel).isID() ) ||
							(varTStack.size()==0)){
							continue;
						}
						
						varLabel = varTStack.varLabel ;
						
						if (varTStack.items.size()==0){
							n=0;
						}
						
						st = varTStack.items.get(0);
						
						if (st.inData.size()==0){
							n=0;
						}
						
						if (st.inData.size()>0){
							ArrayList<?> vlist = st.inData.get(0) ;
							
							if ((vlist.size()>0) && ( strgutils.isNumericX( vlist.get(0)) )){
								primaryInValues = (ArrayList<Double>)vlist ;	
								vmax  = arrutils.arrayMax(primaryInValues, -3.0) ;
							}
						}
						
						varTStack.firstItemForUpdate=0;
						 
						sta = varTStack.getLastPosition() ;
						if ((sta!=null) && (sta.outData.size()>0)){
							continue;
						}
						// propagates very first indata through the stack
						varTStack.update() ;
						
						n = varTStack.items.get(varTStack.size()-1).inData.size() ;
						n = varTStack.items.get(varTStack.size()-1).outData.size() ;
						 
						if (n==0){
							n=0;
						}
						
					}
					
					result = 0 ;
					numFormatsAvail=true ; // TODO  DEBUG ONLY, replace with an active check 
	
					// loop end: all formats = num, or non-num columns have a derivation that is = num
				}
			}catch(Exception e){
				result = -100 ;
				e.printStackTrace();
			}
	
			
			n = somData.getDataTable().getColumnHeaders().size() ; out.print(2, "# columns in headers : "+n) ;
			/*
			n = somData.getDataTable().getDataTable().size()     ; out.print(2, "# columns in table   : "+n) ;
			n = somData.getVariables().size()                    ; out.print(2, "# variables          : "+n) ;
			*/
			return result;		
		}


	private void connectTransformStacksForData(TransformationStack varTStack, int mode, boolean overwrite) {
		
		int ix,ptix,vix;
		boolean normdataAvail, parentStackAvail ;
		ArrayList<Double> outValues = new ArrayList<Double>();
		String tid;
		StackedTransformation st ;
		Variables variables;
		Variable variable, parentVariable ;
		TransformationStack parentStack ;
		
		
		try{
			

			variables = this.somData.getVariables() ;
			variable = variables.getItemByLabel(varTStack.varLabel) ;
			
			tid = varTStack.transformGuid ;          // abfa0368-0862-4715-8b04-fedd7b5b0f71
			// this tid refers to one of the outputColumnIds in the parent stack
			
			// tid2 = variable.getParentTransformID() ;  // 632b039f-bd97-4b9c-9c35-74933c0f585d
			
			vix = variables.getIndexByLabel(varTStack.varLabel) ;
			ptix = transformationModel.getIndexByOutputReferenceGuid( tid ) ; // reference to parent stack
			
			parentVariable = variables.getItemByLabel( varTStack.getInputVariable(0) );
			
			parentStack = transformationModel.variableTransformations.get(ptix) ;
			parentVariable = variables.getItemByLabel(parentStack.varLabel) ;
				
			normdataAvail = (parentVariable.isID()==false) && 
							(parentVariable.isIndexcandidate()==false) && 
							(variables.getBlacklistLabels().indexOf(parentStack.varLabel)<0);
			 
			parentStackAvail = (parentStack!=null) && (parentStack.size()>0); 
			
			if ((mode<0) || ((mode==0) && (normdataAvail==false) && (parentStackAvail==false))){
				// raw data from table
				DataTable dt = this.somData.getDataTable();
				ix = dt.getColumnHeaders().indexOf(varTStack.varLabel);
				if (ix>=0){
					outValues = dt.getColumn(ix).getCellValues();
					
					if (overwrite){
						varTStack.getItem(0).inData.clear();
					}
					varTStack.getItem(0).inData.add(outValues) ;
				}
				return;
			}
			
			if (((mode==0) && (normdataAvail)) || ((mode>0) && (parentStackAvail==false) && (normdataAvail))){
				// normalized data from table
				DataTable dt = this.somData.getNormalizedDataTable();
				ix = dt.getColumnHeaders().indexOf(varTStack.varLabel);

				if (ix>=0){
					outValues = dt.getColumn(ix).getCellValues();
					if (overwrite){
						varTStack.getItem(0).inData.clear();
					}
					varTStack.getItem(0).inData.add(outValues) ;
				}

				return;
			}

			if ((mode>0) && (parentStackAvail)){
			// ((ix>=0) && (varTStack.items.size()>0) ){
				// get the out data from the last stack position
				if (parentStack.size()>0){
					for (int i=parentStack.size()-1;i>=0;i--){
						st = parentStack.getItem(i);
						if (st.outData.size()>0){
							outValues = st.outData ;
							break;
						}
					}
					if (overwrite){
						varTStack.getItem(0).inData.clear();
					}
					if (varTStack.size()>0){
						varTStack.getItem(0).inData.add(outValues) ;
					}
				}else{
					out.print(2,"???") ;
				}
				// st = varTStack.items.get(ix);
				// ensureInDataForFirstTransformation()
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}


	/**
	 * 
	 * shifting distributions (kurtosis, skewness), 
	 * splitting (deciling) variables based on histogram splines, outlier compression, NVE
	 * this extends the basic transformer model
	 * 
	 */
	public void applyBasicNumericalAdjustments() {
		
		TransformationStack varTStack ;
		Variable v;
		
		
		
		try{
			
			int cn = transformationModel.variableTransformations.size() ;
	
			// here we trat ONLY NUMERICAL COLUMNS !!!
			for (int i=0;i<cn; i++){
				
				varTStack = transformationModel.variableTransformations.get(i) ;
				v = varTStack.baseVariable ;
				
				// check numerical characteristics: log shift? deciling? semantic zero?
				// where is the description of the histogram ?
					
				if ((v.getRawFormat() > DataTable.__FORMAT_ID) && (v.getRawFormat() <= DataTable.__FORMAT_INT)){
					
					// NumPropertiesChecker npc = new NumPropertiesChecker(this, dataTableObj, i) ;
					
					// npc.performChecks( new int[]{1,2,3}) ;
						
					// dependent on the result we introduce this or that transformation
					
					
				} // num col ?
				
				
				
			} // i-> all variables == all positions in transformatoinmodel = list of tstacks
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}


	// send candidates into SomTransformer, they will be put just to a queue, 
	// but NOTHING will be changed regarding the transformations...  until a refresh or request for implementation will be sent...
	public void perceiveCandidateTransformations(ArrayList<AnalyticFunctionSpriteImprovement> candidates, int intoFreshStack) {
		
		int n;
		String expr,exprName;
		String[] varStr = new String[2] ;
		int[] varix = new int[2] ;
		AnalyticFunctionSpriteImprovement item;
		CandidateTransformation ctrans ;
		
		try{
			

			if (intoFreshStack<=0)intoFreshStack=-1;
			if (intoFreshStack>1) intoFreshStack= 1;
if (somData.getVariablesLabels().size()<=1){
	n=0;
	n = somData.getVariables().size() ;
}

			// we translate it into a more economic form, just the variables and the formula
			for (int i=0;i<candidates.size();i++){
				item = candidates.get(i) ;
				       expr = item.getExpression();
				       exprName = item.getExpressionName() ;
				       varix[0] = item.varIndex1 ;
				       varix[1] = item.varIndex2 ;
				       
				       varStr[0] = somData.getVariables().getItem( varix[0]).getLabel() ;
				       varStr[1] = somData.getVariables().getItem( varix[1]).getLabel() ;
				       
				       // varStr[0] = somData.getVariablesLabels().get(varix[0]);
				       // varStr[1] = somData.getVariablesLabels().get(varix[1]);
				       
				ctrans = new CandidateTransformation(exprName,expr,varix,varStr);
				ctrans.setDemandForFreshStack(intoFreshStack) ;
				candidateTransformations.add(ctrans) ;
			}
			
			
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		
	}


	/**
	 * TODO: putting format conversion algos into the queue: especially for date columns
	 *       and implementing it BEFORE the modeloptimizer
	 * 
	 * 
	 * the transformation task itself knows whether it should be placed into a new variable or
	 * whether the transformation should be added to the stack
	 * 
	 *  
	 * @param target 0=base data table , 1=normalized data table
	 * @return
	 */
	public SomDataObject implementWaitingTransformations( int target ) {
		
		int nv,ra,tix=-1,n,vix,arexIndex ;
		String str, newVarLabel="", cn;
		boolean nameAdj = false;
		CandidateTransformation ct;
		// Map<String,Integer> varMapping ;
		IndexedDistances varMapping = new IndexedDistances();
		char chr;
		
		ArrayList<Double> colvalues;
		
		TransformationStack varTStack,newVarTStack , srcstack;
		StackedTransformation st , stDataSourceStackItem ;
		ArrayList<Object> params;
		Variables variables;
		Variable variable;
		int sourcemode=1;
		
		// putting candidateTransformations to the transformation model, 
		// creating new "Variable"s and extending the SomDataObject (saving the old version, 
		// and by using a different name, also the new version
		
		
		// adding columns should check whether all data need to be transformed, or better: which one
		
		// SomDataObject should be extended smartly !!!!!!
		// in order to avoid a full transformation (hundreds of variables, 10000's of records), by just adding a single variable

		/*
		 * note that the original data table must remain unchanged if target=1 !
		 * 
		 */
		variables = somData.getVariables() ;
		realizedCount=0;
		
		for (int i=0; i<candidateTransformations.size(); i++){
			nameAdj = false;
			// varMapping = new TreeMap<String,Integer> () ;
			// varMapping.clear();
			varMapping = new IndexedDistances();
			
			ct = candidateTransformations.get(i) ;
			
			str = ct.expression ;
			
			if ((str==null) || (str.length()==0)){
				continue;
			}
			
			// int[] ct.getVariablesIx();
			
			for (int vi=0;vi<ct.getVariablesIx().length;vi++){
				vix = ct.getVariablesIx()[vi] ;
				chr = (char)(97+vi) ;
				str = Character.toString(chr);
				varMapping.add( new IndexDistance(vi,vix, str) ); //  a, 10 --> expression variable "a" means columnindex 10
			}  //  char c = s.charAt(0);
			
			//
			nv = transformationModel.variableTransformations.size() ;
			 
			String basevarLabel = ct.getVariablesStr()[0] ;
			
if (basevarLabel.contains("Bisher_c2")){
	n=0;
}
			tix = transformationModel.getIndexByLabel(basevarLabel) ;
			vix = variables.getIndexByLabel(basevarLabel) ;
			variable = variables.getItem(vix) ;
			 
			varTStack = transformationModel.variableTransformations.get(tix) ;
			// contains now a reference to the parent in the tree, == the first variable in the expression
			             // 
			// should be able to deal with string also ... StringExpression
			try {
				if (ct.demandForFreshStack>0){
					// create a new variable
					newVarLabel = createLabelForSpriteDerivedVariable( ct.variablesStr ) ;
					
					// should also define single-column input data
					newVarTStack = createAddVariableByCopy( target, varTStack, newVarLabel); 
					// 
					varTStack = newVarTStack;  
				}else{
					return null;
				}
				
				if (varTStack.size() == 0) {
					st = varTStack.introduceAlgorithmizedStackPosition("MissingValues");
					
				}
				
				// ensuring the indata for the first transformation
				ensureInDataForFirstTransformation( this.somData.getNormalizedDataTable(),  varTStack , basevarLabel, 1 );
				// connectTransformStacksForData( varTStack, 1, false); 
				
				st = varTStack.introduceAlgorithmizedStackPosition("ArithmetExpression");
				arexIndex = varTStack.size()-1 ;
				
				varTStack.baseVariable.setParentItems( ct.variablesStr ) ;
				varTStack.setInputVariables( ct.variablesStr );
				
				// provide parameters
				params = new ArrayList<Object>();

				params.add(ct.expression);
				params.add(varMapping);

				((AlgorithmIntf) st.algorithm).setParameters(params);

				
				// indata has n columns  (n>=1), dependent on transformation ...
				varTStack.items.get(0).inData.clear() ;
				varTStack.items.get(arexIndex).inData.clear() ;
				
				for (int vi=0;vi<ct.getVariablesIx().length;vi++){
					// get column index
					vix = ct.getVariablesIx()[vi] ;
					tix = transformationModel.getIndexByLabel( ct.getVariablesStr()[vi] ) ;
					
					srcstack = transformationModel.variableTransformations.get(tix) ;
					// update parent columns as indicated by the label
					// srcstack.getOutdataUpdated() ; // !!!!  CARE about copyplain ! 
					
					if ((srcstack.size()==0) || 
						(srcstack.getItems().get(0).outData.size()<=1) || 
						(srcstack.getItems().get(srcstack.size()-1).outData.size()<=1) ){
						srcstack.update() ;
					}

					// from the parent stack, get a reference to the stack item that will be used as a data source 
					// since we have just updated, the last item in the source stack should be able to provide data
					// TODO: instead of the last one, we should search for the last one that provides data !!
					stDataSourceStackItem = getDataSourceFromParentStack(srcstack, srcstack.size()-1 ) ;


					// get column data according to sourcing option as expressed by "StackedTransformation.stackPosForInData"
					colvalues = getColumnData( stDataSourceStackItem, vix, tix , sourcemode);                            
					
					// supplementing the first item in stack with input data without conditions = always
					if (vi==0){
						varTStack.items.get(0).inData.add( colvalues );
						//               0 == first pos in stack
					}
					
						// if we have arithmet
						st = varTStack.items.get(arexIndex) ;
						cn = st.algorithm.getClass().getSimpleName() ;
						
					if (cn.contains("ArithmetExpression")){
						st.inData.add( colvalues );
						st.multiVarInput = true;
						// we need to update the GUIDs of the backward links
						st.inputColumnIDs.add( srcstack.transformGuid );
						st.inputColumnLabels.add( srcstack.varLabel );
						// ... and adopting the out label name of the latest copy operator in the source stack
						// it looks for the transformation = stack item, that has the property outputColumnId = transformGuid
						if (nameAdj==false){
							nameAdj = setOutcolumnLabelForLastWriter( target, srcstack, varTStack.transformGuid , varTStack.varLabel );
							
						}
					}
					
				}
				
				
				/*	wrong length, for adjustment !!
						somDataObj.variables.setAbsoluteAccessible( )
						somDataObj.variables.usageindicationvector
						somDataObj.variableLables
				 */
				
				
				nv = transformationModel.variableTransformations.size();
				addedVariablesByIndex.add(nv-1);
				
				ra = varTStack.update() ;
				
				if (ra==0){
					realizedCount++;
				}
				n = variables.size() ;
				if (variables.getUsageIndicationVector().size()<n){
					double uv = 0.0;
					// if (ct.)
					{
						uv=1.0;
					}
					variables.getUsageIndicationVector().add(uv);
				}
				if (variables.getAbsoluteAccessible().size()<n){
					variables.getAbsoluteAccessible().add(1) ;
				}
				String varLabel = varTStack.varLabel;
				if (somData.getVariablesLabels().indexOf( varLabel)<0){
					somData.getVariablesLabels().add( varLabel ) ;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}// i-> all waiting candidateTransformations
		 
		candidateTransformations.clear();
		return somData;
		// later we will adapt the latticeuseIndicators, which are from getSimilarityConcepts().getUsageIndicationVector() ;
		// 
	}


	/**
	 * 
	 * 
	 * @param dataTable 
	 * @param varTStack
	 * @param basevarLabel 
	 * @param mode   0=source is from base table, 1=source is from originating variable
	 */
	private void ensureInDataForFirstTransformation( DataTable ndataTable, TransformationStack varTStack, String basevarLabel, int mode) {
		
		int tix,vix ;
		StackedTransformation st ;
		Variables variables;
		Variable variable ;
		StackedTransformation sti ;
		TransformationStack originatingTStack ;
		ArrayList<StackedTransformation> stitems ;
		
		
		if ((varTStack==null) || (varTStack.size()==0)){
			return;
			
		}
		

		variables = this.somData.getVariables() ;
		st = varTStack.items.get(0) ;
		
		if (st.inData.size()==0){
			
			tix = transformationModel.getIndexByLabel(basevarLabel) ;
			vix = variables.getIndexByLabel(basevarLabel) ;
			variable = variables.getItem(vix) ;
			
			
			originatingTStack = transformationModel.variableTransformations.get(tix) ;
			
			if (variable.isDerived()){
				String bvar = originatingTStack.baseVariable.getLabel() ;
				if (bvar.length()==0){
					bvar = originatingTStack.varLabel;
				}
				ensureInDataForFirstTransformation(ndataTable,originatingTStack, bvar, 1) ;
			}
			
			sti = originatingTStack.items.get(0) ;
			
			if (sti.inData.size()==0){
				sti.inData.add( ndataTable.getDataTable().get(vix).getCellValues() ); 
			}
			
			
			// update the originating stack, XXX again, the first item of the stack has NO indata  !!!!!!!!!!!!!
			// so update should ensure that, in a recursive manner, until a nonderived variable can be updated from normalized data
			originatingTStack.update() ;
			
			// get the out data from the originating stack 
			stitems = originatingTStack.items ;
			
			for (int i=stitems.size()-1;i>=0;i--){
				st = stitems.get(i) ;
				if ((st.outData!=null) && (st.outData.size()>0)){
					varTStack.items.get(0).inData.add( st.outData );
					break ;
				}
			}
			
		}
		
	}


	/**
	 * 
	 * accorind to the sourcing options for creating the child field, which are controlled by a rather global state variable,
	 * we take the raw data as they are flowing into the stack's item at position 0, or from the last items output  
	 *   
	 * @param varTStack
	 * @param sourcemode 
	 * @return
	 */
	private StackedTransformation getDataSourceFromParentStack(TransformationStack varTStack, int sourceposition ) {
		StackedTransformation st=null;
		int sn = -1;
		
		if (sourceposition>=0){
			sn = varTStack.size()-1 ; 
		}else{
			sn = -1 ;
		}
		
		if (sn>=0){
			st = varTStack.getItems().get(sn) ;
		}
			
		return st;
	}


	/**
	 * 
	 * providing the raw values here (which is default) or the normalized is controlled by an OPTION !!! 
	 * we have to provide the index of the stack position which we should use as input
	 * @param stDataSourceSTackItem 
	 * 
	 * @param vix
	 * @param tix
	 * @return
	 */
	private ArrayList<Double> getColumnData( StackedTransformation dataSourceStackItem, int vix, int tix , int mode) {
		
		ArrayList<Double>  colvalues = new ArrayList<Double>();
		
		if ((mode<=0) || (dataSourceStackItem==null)){
			if (mode<0){
				colvalues = somData.getDataTable().getColumn(vix).getCellValues() ;
			}else{
				colvalues = somData.getNormalizedDataTable().getColumn(vix).getCellValues() ;
			}
		}else{
			colvalues = dataSourceStackItem.outData;
		}
		
		return colvalues ;
	}


	public void implementTransformations(){
		// putting candidateTransformations to the transformation model
		
	}



	private String createLabelForSpriteDerivedVariable(String[] varStrings) {
		
		String newLabel = "",dLevelIndicationStr;
		int ix,dmax = 0;
		int exptotalLength = 5;
		String[] prepVarStrings = new String[varStrings.length];
		
		// we have to check the stack for similar name... etc.
		
		
		
		// we need to get the max derivation level of the provided parts
		// also determine the expected total length
		
		for (int i=0;i<varStrings.length;i++){

			prepVarStrings[i] = varStrings[i] ;
		} // all provided parts
		

		// then we reduce the names to a shorter version: suffixes, for each part as defined by capital letters
		
		for (int i=0;i<prepVarStrings.length;i++){
 
		}
		
		// 
		dmax++;
		
		dLevelIndicationStr = "d"+dmax+"_" ;
		newLabel = dLevelIndicationStr;
		
		for (int i=0;i<prepVarStrings.length;i++){
			newLabel = newLabel + prepVarStrings[i];
			if (i<prepVarStrings.length-1){
				newLabel = newLabel + "_" ;
			}
		} // all provided parts

		// loop: if this label already exists, we have to modify it inserting a char-enm (a,b,c)
		//       just before the "_" , e.g. as d2b_ , from "dLevelIndicationStr"
		
		String dIndicatstr = dLevelIndicationStr ;
		boolean varNameConflict = true;
		int z=0;
		while (varNameConflict){
			
			ix = transformationModel.getIndexByLabel(newLabel);
			
			varNameConflict = ix>=0;
			if (varNameConflict){
				z++;
				int p = dIndicatstr.length() ;
				newLabel = newLabel.substring(p, newLabel.length()) ;
				
				dIndicatstr = ((char)(z+97))+"_" ;
				dIndicatstr = dLevelIndicationStr.replace("_", dIndicatstr);
				newLabel = dIndicatstr + newLabel;
			}
		}
			
			
		return newLabel;
	}


	private TransformationStack createAddVariableByCopy( int target, TransformationStack srcVarTStack){
		return createAddVariableByCopy(target, srcVarTStack,"");
	}
	
	private TransformationStack createAddVariableByCopy( int target, TransformationStack srcVarTStack, String proposedVariableName ) {

		StackedTransformation st;
		TransformationStack newTStack=null;
		DataTable _dataTable;
		int cn,newIndex,currentFormat,srcvarix=-1;
		String varLabel, newVarLabel="" ;
		Variable newVariable, v;
		
		try {
			
			if ( srcVarTStack.size()==0 ){
				srcVarTStack.introduceAlgorithmizedStackPosition("MissingValues") ;
			}
			st = srcVarTStack.introduceAlgorithmizedStackPosition("CopyPlain") ;
			st.stackPosForInData = -1; // -1 == latest = default
			
			v = srcVarTStack.baseVariable ;
			
			currentFormat = srcVarTStack.getLatestFormat() ;
			if (currentFormat<0){
				currentFormat = v.getRawFormat() ;
			}  
			
			// which table (of type DataTable)? basic, or normalized
			if (target==0){
				_dataTable = dataTableObj;
			} else{
				_dataTable = somData.getNormalizedDataTable() ;
			}

			// opening the derived column to the data table, will copy the data from the originating column
			newIndex = _dataTable.addDerivedColumn( srcVarTStack, st,1 ) ;

			int[] colFormats = _dataTable.getFormats() ;
			int[] newColFormats = new int[colFormats.length+1] ;
			System.arraycopy(colFormats, 0, newColFormats, 0, colFormats.length) ;
			
			int ncf = 1; 
			if ( currentFormat ==0)ncf=0 ;
			
			newColFormats[newColFormats.length-1] = ncf;
			_dataTable.setFormats(newColFormats) ;
			
			 
			varLabel = srcVarTStack.getVarLabel() ;
			
			srcvarix = _dataTable.getColumnHeaders().indexOf(varLabel) ; 
			
			if (proposedVariableName.length()==0){
				newVarLabel = _dataTable.getColumnHeader(newIndex);
			}else{
				newVarLabel = proposedVariableName;
			}
			
			if (newVarLabel.length()==0){
				newVarLabel = "var_"+transformationModel.variableTransformations.size();
			}
			
			// also add the new variable to the list of variables
			// TODO at other locations, we probably must do a check for the target too!!! 
			newVariable = somData.addDerivedVariable( newIndex , varLabel, newVarLabel, st.idString );
			newVariable.setRawFormat( v.getRawFormat()) ;
			newVariable.setDerived(true);
			
			srcVarTStack.setLatestFormat( currentFormat ) ; // no format change
			
			// else, we have to create a new TransformationStack that reflects the new variable
			newTStack = new TransformationStack();
			newTStack.baseVariable = newVariable ;
			newTStack.varLabel = newVarLabel ;
			newTStack.setLatestFormat(currentFormat) ;
			newTStack.inputVarLabels.add( srcVarTStack.baseVariable.getLabel() ) ;  
			
			
			transformationModel.variableTransformations.add( newTStack ) ;
			
			// announcing the reference both in the stack element as well as in the stack (adding to the list)
			// we need this since names of columns may change
			st.outputColumnId = newTStack.getGuid();
			srcVarTStack.outputColumnIds.add( newTStack.getGuid() );
			// updating the list of column headers
			int adjix = _dataTable.getColumnHeaders().indexOf(newVarLabel) ;
			if (adjix>=0){
				_dataTable.getColumn(adjix).setCopyofColumn( srcvarix ) ;
			}
			
			
			// transformationModel.variableTransformations.set(i ,newTStack ) ;
			// XXX TODO WE GET 40 instead of 20 !!!!!!!!!!!!!!
			
			// we will meet it later again, as it is added at the end of the list of columns/variables
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return newTStack;
	}
	
	 
	
	/**
	 * 
	 * this has to ake place in SomTransformer, because we need the reference to the variables. 
	 * 
	 * ... and adopting the out label name of the latest copy operator in the source stack
	 * it looks for the transformation = stack item, that has the property outputColumnId = transformGuid
	 * @param dataTableObj 
	 * 					 
	 * @param targetColGuid
	 * @param adjLabel
	 * @return 
	 */
	public boolean setOutcolumnLabelForLastWriter( int target, TransformationStack tstack, String targetColGuid, String adjLabel ) {
		
		boolean rB=false;
		String idstr, initialOutColumnLabel="";
		DataTable _dataTable;
		
		// which table (of type DataTable)? basic, or normalized
		if (target==0){
			_dataTable = dataTableObj;
		} else{
			_dataTable = somData.getNormalizedDataTable() ;
		}
		
		
		for (int i= tstack.items.size()-1;i>=0;i--){
			
			idstr = tstack.items.get(i).outputColumnId ;
			if (idstr.contentEquals(targetColGuid)){
				
				initialOutColumnLabel = tstack.items.get(i).outputColumnLabel;
				tstack.items.get(i).outputColumnLabel = adjLabel;
				
				rB=true;
				break;
			}
			
		} // i->
		
		if (initialOutColumnLabel.length()>0){
			int ix = _dataTable.getColumnHeaders().indexOf(initialOutColumnLabel) ;
			if (ix>=0){
				_dataTable.getColumnHeaders().set(ix,adjLabel  );
			}
			// TODO: we even need a callback / event that allows us to update the added variables...
			ix = somData.getVariables().getIndexByLabel( initialOutColumnLabel);
			if (ix>=0){
				String str ;
				Variable v ;
				
				v = somData.getVariables().getItem(ix);
				str = v.getLabel() ;
				if (str.contentEquals(initialOutColumnLabel)){
					v.setLabel( adjLabel );
				}

			}
		}
		 
		return rB;
	}


	public void applyAprioriLinkChecking() {
	 
		try{
			
			
			
		}catch(Exception e){
			
		}
	}


	public void normalizeData(){
		
		int cn,i,n,sn,j;
		boolean hb;
		String varLabel;
		StackedTransformation st;
		TransformationStack varTStack;
		Variable v;
		Variables variables;
		DataTable normedDataTable;
		
		try{
			
			/*
			 *	the normalized table will still contain non-num columns,
			 *  yet we will create an structural identifier, that contains only those headers which are numeric 
			 */
	
			variables = somData.getVariables() ;
			
			cn = transformationModel.variableTransformations.size() ;
			i = -1;
			
			while (i<cn-1){
				i++;
if (i>15){
	n=0;
}
				varTStack = transformationModel.variableTransformations.get(i) ;
				v = varTStack.baseVariable ;
				
				varLabel = v.getLabel() ;
				variables.getAbsoluteAccessible().add(0) ;
				
				if (v.getRawFormat() > DataTable.__FORMAT_ORGINT){
					variables.getAbsoluteAccessible().set(i,0) ;
					variables.addBlacklistLabel( v.getLabel() ) ;
					continue;
				}
				
if (varLabel.toLowerCase().contains("id_c")){
	n=0;
}	
				
				if (v.getRawFormat() == DataTable.__FORMAT_ID){
					// put to index candidates if it is not already there, and to blacklist					 
					{
						v.setIndexcandidate(true);
						
						if ( v.isDerived()){
							variables.getAbsoluteAccessible().set(i,1) ;
							variables.addBlacklistLabel(varLabel);
						}else{
							// ensure that raw values are transferred
							sn = varTStack.items.size();
							
							if (sn==0){
								varTStack.introduceAlgorithmizedStackPosition("MissingValues") ;
							}
							varTStack.update() ;
						}
						if ( v.isDerived()){
							varTStack.introduceAlgorithmizedStackPosition("LinearNormalization") ;
							varTStack.update() ;
						}
						
						variables.addBlacklistLabel( v.getLabel() ) ;
						continue;
					}
					
				}
				
				variables.getAbsoluteAccessible().set(i,1) ;  
				
				// note that freshly added columns are automatically set to num
				hb = (v.getRawFormat() > DataTable.__FORMAT_ID) && (v.getRawFormat() <= DataTable.__FORMAT_INT);
				
				if (hb){
					hb = (varTStack.items.size()>0) ;
				}
				if (hb){
					hb = varTStack.stackProvidesOutData() ;
				}
				if (hb){

					 
						// does it contain stdstats below of other transforms?  if not, add it
						
						hb = varTStack.checkDescriptionPosition();
						
						if (hb){
							st = varTStack.introduceAlgorithmizedStackPosition("StandardStatistics") ;
						}
						
						hb = varTStack.algorithmIsLatest("LinearNormalization");
						
						if (hb==false){
							st = varTStack.introduceAlgorithmizedStackPosition("LinearNormalization") ;
						}
						varTStack.setFirstItemForUpdate( varTStack.items.size()-1);
						varTStack.update();
						j=0;
					
				} // format ?
				
			} // ->
			
			cn=0;
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		 
	}


	/**
	 * 
	 * creating the table of normalized data:
	 * - if a transformation stack is available && -> if a linear norm is at the end && -> [min,max] is from  [0,1]
	 *   then take the values tron tstack
	 * - else take values from raw datatable and set col to absoluteAccessible = -1  
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DataTable writeNormalizedData( ){  
		 
		String varLabel,errmsg="";
		int i,cn,n,rc,vn,j,tix;
		boolean emptyList,rawList;
		TransformationStack varTStack;
		Variable v;
		Variables variables;
		
		DataTableCol col, colNorm;
		ArrayList<String> colheaders;
		ArrayList<Double> normvalues = new ArrayList<Double>();
		ArrayList<DataTableCol> normDataTable;
		
		StackedTransformation st ;
		
		
		try{
		
			dataTableNormalized = new DataTable(somData, true); // the whole object
			// ------------------------------------------------
			
			variables = somData.getVariables() ;
			cn = transformationModel.variableTransformations.size() ;
			vn = variables.size() ; 
			
			dataTableNormalized.setSourceFileName( dataTableObj.getSourceFilename() );
			dataTableNormalized.setTableHasHeader( dataTableObj.isTableHasHeader() ) ;
			dataTableNormalized.setFormats( dataTableObj.getFormats().clone() ) ;
			dataTableNormalized.setColcount( variables.size() ) ;
			rc = dataTableObj.getRowcount();
			dataTableNormalized.setRowcount( rc ) ;
			
				colheaders = new ArrayList<String>( dataTableObj.getColumnHeaders());
				colheaders = variables.getLabelsForVariablesList(variables) ;
			dataTableNormalized.setColumnHeaders( colheaders );
			
			normDataTable = dataTableNormalized.getDataTable();
			
			
			
			i = -1;
			
			while (i<cn-1){
				i++;
				normvalues.clear() ;
				emptyList  =true;
				rawList = false;
				
				varTStack = transformationModel.variableTransformations.get(i) ;
				v = varTStack.baseVariable ;
				varLabel = v.getLabel() ;
if ((i>15)){
	n=0;
}
 
				colNorm = new DataTableCol(dataTableNormalized,i);
				
				if (v.getRawFormat() == DataTable.__FORMAT_ID){
					colNorm.setIndexColumnCandidate(true);
				}
				
				
											errmsg = "variable: "+v.getLabel()+" , row : "+i ;
				if ((variables.getAbsoluteAccessible().get(i)>=1) | (v.isDerived())){
					
					if (v.getRawFormat() == DataTable.__FORMAT_ID){
						if (v.isDerived()==false){
							normvalues = dataTableObj.getColumn( varLabel ).getCellValues(); 
						}else{
							normvalues = varTStack.getLatestColumnValues(1);
						}
						colNorm.setDataFormat(1) ;
					}else{
						
						normvalues = varTStack.getLatestColumnValues(1) ; // 1 = mode of value checking (1=yes)
 
						colNorm.setDataFormat(1) ;
						dataTableNormalized.getColumnHeaders().set(i, varTStack.varLabel);
						
					}
					colNorm.setNumeric(true);
				
					
				}else{
					rawList = true;
				}
				
				emptyList = (normvalues==null) || (normvalues.size()<=1) ;
				
				if (emptyList){
					j=0; // all to -1 ; 
					for (int k=0;k<rc;k++){
						normvalues.add(-1.0) ;
					}
				}
				
				colNorm.setCellValues(normvalues) ;
				colNorm.setSerialID(i) ;
				normDataTable.add(colNorm);
				
			} // while ->
			
			tix=0;
			
			
		}catch(Exception e){
			out.printErr(1, errmsg);
			e.printStackTrace();
		}
		
		out.print(4, "SomTransformer instance @ writeNormalizedData : "+this.toString() ) ;
		
		return dataTableNormalized;
	}


	/**
	 * 
	 * basic normalization outside of transformation model
	 * 
	 * @param variables
	 * @return
	 */
	public DataTable normalizeData( Variables variables) {
		int z;
		DataTableCol col, colNorm;
		ArrayList<DataTableCol> inDataTable = dataTableObj.getDataTable();
		ArrayList<DataTableCol> normDataTable;
		
		
		try{
			
			dataTableNormalized = new DataTable(somData, true); // the whole object
			
			dataTableNormalized.setSourceFileName( dataTableObj.getSourceFilename() );
			dataTableNormalized.setTableHasHeader( dataTableObj.isTableHasHeader() ) ;
			dataTableNormalized.setFormats( dataTableObj.getFormats().clone() ) ;
			dataTableNormalized.setColcount( dataTableObj.getColcount() ) ;
			dataTableNormalized.setRowcount( dataTableObj.getRowcount() ) ;
			dataTableNormalized.setColumnHeaders( new ArrayList<String>(dataTableObj.getColumnHeaders()) );
			
			normDataTable = dataTableNormalized.getDataTable();
			
			z=0;
			
			for (int i=0; i<inDataTable.size();i++){
				
				col = inDataTable.get(i);
				
				if ((col.getRecalculationIndicator()>0)){
					
					if (col.isIndexColumnCandidate()==false){
						col.calculateBasicStatistics();
					}
					// ArrayList<Double> cellValues
				}else{
					continue;
				}
				
				// we do not normalize index columns, we won't include it in the analysis anyway
				col.setRecalculationIndicator(0);
				colNorm = new DataTableCol(dataTableNormalized, col);//  dataTableNormalized, i
				
				if ((col.getDataFormat()<8) && (col.isIndexColumnCandidate()==false)){
					
					colNorm.normalize( col.getStatisticalDescription().getMini(), col.getStatisticalDescription().getMaxi() );
	
					// calculate stats for the normalized column
					colNorm.calculateBasicStatistics();
					// store the statistical description for the raw data in the column  
					// that contain the normalized data... such we will be able to translate ! 
					colNorm.setRawDataStatistics(col.getStatisticalDescription()) ;
					colNorm.setNumeric(true);
					colNorm.setDataFormat(1) ;
					
				}else{
					col.setRecalculationIndicator(-3); // ignore, like blacklisted variables
					colNorm.setDataFormat(17) ;
				}
				
				colNorm.setSerialID(i) ;
				normDataTable.add(colNorm);
				 
			} // all columns
			
			z=0 ;
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return dataTableNormalized;
	}


	public void setDataTable( DataTable inDatatable ) {
		 
		try{
			
			// creates a deep clone with content of inDatatable 
			dataTableObj = new DataTable( inDatatable ) ; 
			
		}catch(Exception e){
			
		}
	}


	public DataTable getDataTableNormalized() {
		return dataTableNormalized;
	}


	/**
	 * @return the addedVariablesByIndex
	 */
	public ArrayList<Integer> getAddedVariablesByIndex() {
		return addedVariablesByIndex;
	}


	class SomTransformerInitialization{
		
		boolean done = false;
		int rowCount = 0;
		int colCount = 0;
		
		long timeStamp =0;
		
		public SomTransformerInitialization(){
			
		}
		
		public void setInitialized( boolean flag, int rowcnt, int colcnt){
			
			if (flag){
				timeStamp = System.currentTimeMillis() ;
				done = true;
				rowCount = rowcnt ; 
				colCount = colcnt ;
			}
		}
	}
}
