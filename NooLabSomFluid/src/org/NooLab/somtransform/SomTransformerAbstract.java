package org.NooLab.somtransform;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.data.NormValueRangesIntf;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somsprite.AnalyticFunctionTransformationsIntf;
import org.NooLab.somtransform.SomTransformer.SomTransformerInitialization;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.strings.StringsUtil;

public abstract class SomTransformerAbstract implements SomTransformerIntf {

 
	
	protected transient SomDataObject somData;
	
	SomFluidProperties _sfProperties;
	SomFluidAppGeneralPropertiesIntf sfProperties;
	
	
	
	/** data as it has been imported */
	DataTable dataTableObj ;
	/** transformed data, as defined by the transformation model */
	DataTable dataTableNormalized ;
	
	/** TODO: they are always and immediately saved to file  */
	transient ArrayList<CandidateTransformation> candidateTransformations = new ArrayList<CandidateTransformation> ();
	
	ArrayList<Integer> addedVariablesByIndex = new ArrayList<Integer>();
	
	protected TransformationModel transformationModel;
 
	SomAssignatesDerivations somDerivations ;
	ArrayList<String> chainedVariables  = new ArrayList<String>();
	
	SomTransformerInitialization initialization;
	
	
	
	transient SomFluidXMLHelper xEngine = new SomFluidXMLHelper();
	
	transient PrintLog out = new PrintLog(2,false);
	transient StringedObjects strobj = new StringedObjects();
	transient ArrUtilities arrutil = new ArrUtilities();
	transient StringsUtil strgutils = new StringsUtil ();
	transient FileOrganizer fileorg ;
	transient DFutils fileutil = new DFutils();
	
	// ========================================================================
	public SomTransformerAbstract(){
		
	}
	
	public SomTransformerAbstract(SomDataObject sdo, SomFluidAppGeneralPropertiesIntf sfprops) {
		somData = sdo;
		
		sfProperties = sfprops;
		
		_sfProperties = sfprops.getSelfReference(); // null in case of SomApplication ...
		 
		
		dataTableObj = somData.getDataTable() ; // this now about its serialized filebuffer 
		
		transformationModel = new TransformationModel(this, somData);
		
		out = somData.getOut() ;
		fileorg = sfProperties.getFileOrganizer();
	}
	// ========================================================================

	
	
	@Override
	public void setDataTable(DataTable inDatatable) {
		// TODO Auto-generated method stub
		
	}

	

	/**
	 * 
	 * 
	 * @param target  0=base data table , 1=normalized data table (the usual target)
	 * @param srcVarTStack
	 * @return
	 */
	private TransformationStack createAddVariableByCopy( int target, TransformationStack srcVarTStack){
		return createAddVariableByCopy(target, srcVarTStack,"");
	}

	/**
	 * 
	 * 
	 * @param target  0=base data table , 1=normalized data table (the usual target)
	 * @param srcVarTStack
	 * @param proposedVariableName
	 * @return
	 */
	protected TransformationStack createAddVariableByCopy( int target, TransformationStack srcVarTStack, String proposedVariableName ) {
	
		StackedTransformation st;
		TransformationStack newTStack=null;
		DataTable _dataTable;
		int newIndex,currentFormat,srcvarix=-1;
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
			_dataTable.getColumnHeaders().set(newIndex, newVarLabel);
			
			// else, we have to create a new TransformationStack that reflects the new variable
			newTStack = new TransformationStack( this,sfProperties.getPluginSettings() );
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
	 * this provides numeric versions of the data in the provided table.</br>
	 * this transformation is accomplished by means of a transformation stack;</br></br>
	 * 
	 * this method applies the minimal stack for such "numericalization":</br>
	 * - missing values interpreter </br>
	 * - calculation of basic statistics, without histograms </br>
	 * - linear normalization </br></br>
	 * 
	 * in case of non-numerical input data, conversions are applied where possible (and reasonable)</br>
	 * - string data with a few different values are transformed into ordinal values by simple enumeration</br>
	 * - date formats are translated into a serial integer, applying a base data of 1.1.1850 ;</br></br>
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int basicTransformToNumericalFormat(){
			
			int result = -1, cn,n,n1,n2 , recordCount=0;
			boolean  copyIsMandatory;
			int currentFormat;
			String varLabel ;
			ArrayList<Double> numvals;
			ArrayList<String> strvals ;
			DataTableCol dtc ;
			
			Variable v;
	
			StackedTransformation st,sta ;
			Variables variables;
			
			TransformationStack varTStack ;
			
			// known: dataTableObj = the data table , the items in the tStack have access to the variable objects
			// uses transformations as algorithms even for basic stuff !!
			
			// int fieldExclusionsMode = sfProperties.getAbsoluteFieldExclusionsMode() ;
			ArrayList<String> absoluteBlackList = sfProperties.getSelfReference().getAbsoluteFieldExclusions() ;
			boolean isBlacklisted;  
			if (absoluteBlackList==null)absoluteBlackList = new ArrayList<String>();
			
			try{
				variables =  somData.getVariables() ;
				if (somData.getDataTable().getColcount()<=0){
					return -103;
				}
				recordCount = somData.getDataTable().getColumn(0).getRowcount() ;
				// repeat until all stack positions have a defined outgoing data format = num
				boolean numFormatsAvail=false;
				if (recordCount<=3){ return -107;}
				cn = transformationModel.variableTransformations.size() ;
				
											int outlevel=3;
											if (cn*recordCount>600000)outlevel=2;  out.print(outlevel, "transforming data to numerical format...");

				while (numFormatsAvail==false){	
					
					int i=-1;
					
					// transformationModel.variableTransformations size remains 0 for raw variables ???
					while (i<cn-1){
						i++;
											if (provideProgress(i,cn,recordCount)){
											}	
											out.printprc(outlevel, i,cn, cn/5, "");
											
						varTStack = transformationModel.variableTransformations.get(i) ;
						v = varTStack.baseVariable ;
						
						varLabel = v.getLabel() ;
						
if (varLabel.toLowerCase().contains("_c")){
	n=0;
}
						currentFormat = varTStack.getLatestFormat() ; // is a sibling of the call "varTStack.getFormatAtStackPos()" ;
						if (currentFormat<0){
							currentFormat = v.getRawFormat() ;
						}
						
						if (varTStack.firstFormat<0){
							varTStack.firstFormat = v.getRawFormat() ;
							if (varTStack.firstFormat<0){
								// determine it again ...
								
							}
						}
						
						isBlacklisted = absoluteBlackList.indexOf(varLabel)>=0 ;
						if (isBlacklisted){
							continue;
						}

						// some variables need to remain in raw state, such as ID or TV columns;
						// for those we have to create a copy directly from the raw values
						copyIsMandatory = false;
						if (((v.isID()) || (v.isIndexcandidate() )) && (v.isDerived()==false)){ // copies are not "mandatory"(!) if variable is already a derived one 
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
							 
							createAddVariableByCopy( 0, varTStack);
 							// it will be added to the right of the table, so we will meet it later in the loop, 
							// no need to deal with it right now (like this: TransformationStack newTStack = ...)
							
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
								  varTStack.introduceAlgorithmizedStackPosition("StatisticalDescriptionStandard") ;
								  
							dtc = somData.getDataTable().getColumn(i);
							strvals = dtc.getCellValueStr(true) ; // here, we remove the header of string columns !!!
							st.getInData().add( strvals );        // the algorithm and the stack do not know anything about "headers"
							
							varTStack.update();
							
							varTStack.setLatestFormat( 1 ) ;
							v.setRawFormat(1) ;
							continue;
						}
						
	if (i>=18){
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
							
							varTStack.introduceAlgorithmizedStackPosition("StatisticalDescriptionStandard") ;
							
							// somData.getDataTable().getColumn(i).getCellValues() ) ;
							dtc = somData.getDataTable().getColumn(i);
							strvals = dtc.getCellValueStr() ;
							st.getInData().add( strvals );
							
	if (varLabel.toLowerCase().contains("_c")){
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
						if ((v.getRawFormat() >= DataTable.__FORMAT_ID) && (v.getRawFormat() <= DataTable.__FORMAT_BIN)){// __FORMAT_INT)){
							
							if (varTStack.items.size()==0){
								varTStack.firstFormat = v.getRawFormat() ;
								// especially for non-target binary variables we will modify the linear normalization !!
								sta = varTStack.introduceAlgorithmizedStackPosition("MissingValues") ;
								      varTStack.introduceAlgorithmizedStackPosition("StatisticalDescriptionStandard") ;
							}
							
							sta = varTStack.getItems().get(0) ;  
							
						if (sta.getInData().size() == 0) {
							int ix = i;
							if (v.isDerived()) { // v.parentTransformID =
													// ba9a0d28-c345-477e-be9d-0b8997bca1a9
								//
								connectTransformStacksForData(varTStack, 1, false); //

							} else {

								if (ix >= 0) {
									// these data will be replaced in the
									// StackedTransformation will be replaced by
									// normalized values !
									sta.getInData().add(somData.getDataTable().getColumn(ix).getCellValues());
									n = sta.getInData().size();

									if (varLabel.toLowerCase().contains("sales")) {
										n = 0;
										
									}

								} else {
									ix = -1;
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
					double vmax =0.0;
					
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
						
						if (st.getInData().size()==0){
							n=0;
						}
						
						if (st.getInData().size()>0){
							ArrayList<?> vlist = st.getInData().get(0) ;
							
							if ((vlist.size()>0) && ( strgutils.isNumericX( vlist.get(0)) )){
								primaryInValues = (ArrayList<Double>)vlist ;	
								vmax  = arrutils.arrayMax(primaryInValues, -3.0) ;
								if (vmax>1){
									// ???
								}
							}
						}
						
						varTStack.firstItemForUpdate=0;
						 
						sta = varTStack.getLastPosition() ;
						if ((sta!=null) && (sta.outData.size()>0)){
							continue;
						}
						// propagates very first indata through the stack
						varTStack.update() ;
						
						n = varTStack.items.get(varTStack.size()-1).getInData().size() ;
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
	
			
			n = somData.getDataTable().getColumnHeaders().size() ; 
											out.print(2, "# columns in headers : "+n) ;
			/*
			n = somData.getDataTable().getDataTable().size()     ; out.print(2, "# columns in table   : "+n) ;
			n = somData.getVariables().size()                    ; out.print(2, "# variables          : "+n) ;
			*/
			return result;		
		}

	

	/**
	 * @return the addedVariablesByIndex
	 */
	public ArrayList<Integer> getAddedVariablesByIndex() {
		return addedVariablesByIndex;
	}


	public int connectTransformStacksForData(TransformationStack varTStack, int mode, boolean overwrite) {
		
		int result = -1;
		int ix,ptix;
		boolean normdataAvail, parentStackAvail ;
		ArrayList<Double> outValues = new ArrayList<Double>();
		String tid;
		StackedTransformation st ;
		Variables variables;
		Variable parentVariable ;
		TransformationStack parentStack ;
		
		
		try{
			
	
			variables = this.somData.getVariables() ;
			// Variable variable = variables.getItemByLabel(varTStack.varLabel) ;
			
			tid = varTStack.transformGuid ;          // abfa0368-0862-4715-8b04-fedd7b5b0f71
			// this tid refers to one of the outputColumnIds in the parent stack
			
			// tid2 = variable.getParentTransformID() ;  // 632b039f-bd97-4b9c-9c35-74933c0f585d
			
			// int vix = variables.getIndexByLabel(varTStack.varLabel) ;
			ptix = transformationModel.getIndexByOutputReferenceGuid( tid ) ; // reference to parent stack
			
			if (ptix<0){
				result = -3;
				return result;
			}
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
						varTStack.getItem(0).getInData().clear();
					}
					varTStack.getItem(0).getInData().add(outValues) ;
				}
				return 0;
			}
			
			if (((mode==0) && (normdataAvail)) || ((mode>0) && (parentStackAvail==false) && (normdataAvail))){
				// normalized data from table
				DataTable dt = this.somData.getNormalizedDataTable();
				ix = dt.getColumnHeaders().indexOf(varTStack.varLabel);
	
				if (ix>=0){
					outValues = dt.getColumn(ix).getCellValues();
					if (overwrite){
						varTStack.getItem(0).getInData().clear();
					}
					varTStack.getItem(0).getInData().add(outValues) ;
				}
	
				return 0;
			}
			boolean found = false;
			if ((mode>0) && (parentStackAvail)){
			// ((ix>=0) && (varTStack.items.size()>0) ){
				// get the out data from the last stack position WRONG !!!! from the respective WRITER position
				if (parentStack.size()>0){
					
					boolean isSrc =  false;
					for (int i=parentStack.size()-1;i>=0;i--){
						st = parentStack.getItem(i);
						// 1. is the algorithm a writer? (only writers can transfer data to other columns)
						isSrc = ((AlgorithmIntf)st.algorithm).getType() == AlgorithmIntf._ALGOTYPE_WRITER ;
						// 2. is this WRITER - algorithm indeed the source? -> check by out guid
						if (isSrc){
							String src_guid ,col_guid;
							
							src_guid = st.outputColumnId;
							col_guid = varTStack.transformGuid ; // 3ed35fff-d20a-4be7-a933-5202349cdb15
							isSrc = src_guid.contentEquals( col_guid); 
						}
						
						//if this 
						if ((isSrc) && (st.outData.size()>0)){
							outValues = st.outData ;
							found = true;
							break;
						}
					}
					if (overwrite){
						varTStack.getItem(0).getInData().clear();
					}
					if (varTStack.size()>0){
						varTStack.getItem(0).getInData().add(outValues) ;
					}
				}else{
					out.print(2,"???") ;
				}
				// st = varTStack.items.get(ix);
				// ensureInDataForFirstTransformation()
				result =0;
			}
			if (found==false){
				out.printErr(2, "problem in <connectTransformStacksForData()> : writer of source data not found.");
				result = -4;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * 
	 * 
	 * @param dataTable 
	 * @param varTStack
	 * @param basevarLabel 
	 * @param mode   0=source is from base table, 1=source is from originating variable
	 */
	protected void ensureInDataForFirstTransformation( DataTable ndataTable, TransformationStack varTStack, String basevarLabel, int mode) {
		
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
		
		if (st.getInData().size()==0){
			
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
			
			if (sti.getInData().size()==0){
				sti.getInData().add( ndataTable.getDataTable().get(vix).getCellValues() ); 
			}
			
			
			// update the originating stack,
			// so update should ensure that, in a recursive manner, until a non-derived variable 
			// can be updated from normalized data
			originatingTStack.update() ;
			
			// get the out data from the originating stack 
			stitems = originatingTStack.items ;
			
			for (int i=stitems.size()-1;i>=0;i--){
				st = stitems.get(i) ;
				if ((st.outData!=null) && (st.outData.size()>0)){
					varTStack.items.get(0).getInData().add( st.outData );
					break ;
				}
			}
			
		}
		
	}


	/**
	 * 
	 * according to the sourcing options for creating the child field, which are controlled by a rather global state variable,
	 * we take the raw data as they are flowing into the stack's item at position 0, or from the last items output  
	 *   
	 * @param varTStack
	 * @param sourcemode 
	 * @return
	 */
	protected StackedTransformation getDataSourceFromParentStack(TransformationStack varTStack, int sourceposition ) {
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


	
	

	private boolean provideProgress(int i, int cn, int recordCount) {
		// TODO Auto-generated method stub
		return false;
	}


	


	/**
		 * 
		 * creating the table of normalized data:
		 * - if a transformation stack is available && -> if a linear norm is at the end && -> [min,max] is from  [0,1]
		 *   then take the values from tStack
		 * - else take values from raw DataTable and set col to absoluteAccessible = -1  
		 * 
		 * @return
		 */

		public DataTable writeNormalizedData( ){  
			 
			String varLabel,errmsg="";
			int i,cn,rc;
			boolean emptyList;
			TransformationStack varTStack;
			Variable v;
			Variables variables;
			
			DataTableCol colNorm;
			ArrayList<String> colheaders;
			ArrayList<Double> normvalues = new ArrayList<Double>();
			ArrayList<DataTableCol> normDataTable;
			
			
			try{
			
				dataTableNormalized = new DataTable(somData, true); // the whole object
				// ------------------------------------------------
				
				variables = somData.getVariables() ;
				cn = transformationModel.variableTransformations.size() ;
				// int vn = variables.size() ; 
				
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
					// boolean rawList = false;
					
					varTStack = transformationModel.variableTransformations.get(i) ;
					v = varTStack.baseVariable ;
					varLabel = v.getLabel() ;

					
					colNorm = new DataTableCol(dataTableNormalized,i);
					
					if (v.getRawFormat() == DataTable.__FORMAT_ID){
						colNorm.setIndexColumnCandidate(true);
					}
					
					
												errmsg = "variable: "+v.getLabel()+" , row : "+i ;
					if ((variables.getAbsoluteAccessible().get(i)>=1) || (v.isDerived()) || (v.getRawFormat() == DataTable.__FORMAT_ID)){
						
						if (v.getRawFormat() == DataTable.__FORMAT_ID){
							if (v.isDerived()==false){
								normvalues = dataTableObj.getColumn( varLabel ).getCellValues();
								double _max = arrutil.arrayMax(normvalues, -1.0) ;
								if (_max>1){
									// just be sure to exclude it absolutely
									variables.getAbsoluteAccessible().set(i,0);
									variables.addBlacklistLabel(varLabel);
								}
							}else{
								normvalues = varTStack.getLatestColumnValues(1);
							}
							colNorm.setDataFormat( DataTable.__FORMAT_NUM ) ;
						}else{
							
							normvalues = varTStack.getLatestColumnValues(1) ; // 1 = mode of value checking (1=yes)
	 
							colNorm.setDataFormat( DataTable.__FORMAT_NUM ) ;
							dataTableNormalized.getColumnHeaders().set(i, varTStack.varLabel);
							
						}
						colNorm.setNumeric(true);
					
						
					}else{
						//  rawList = true;
					}
					
					emptyList = (normvalues==null) || (normvalues.size()<=1) ;
					
					if (emptyList){
						// all to -1 ; 
						for (int k=0;k<rc;k++){
							normvalues.add(-1.0) ;
						}
					}
					
					colNorm.setCellValues(normvalues) ;
					colNorm.setSerialID(i) ;
					normDataTable.add(colNorm);
					
				} // while ->
				
				
			}catch(Exception e){
				out.printErr(1, errmsg);
				e.printStackTrace();
			}
			
			out.print(4, "SomTransformer instance @ writeNormalizedData : "+this.toString() ) ;
			
			return dataTableNormalized;
	}

		
	/**
	 * this takes data from the main DataTable and for each variable then feeds
	 * the data into the TransformationStack.</br> According to the type of the
	 * in-data the TransformationStack then will be extended by the appropriate
	 * methods.</br></br>
	 * 
	 * The data will NOt be written to the normalized table, which is also part
	 * of the SomDataObject!
	 * 
	 * 
	 */
	public void normalizeData() {

		int cn, i, sn, tvindex=-1;
		boolean hb;
		String varLabel,tvLabel;

		TransformationStack varTStack;
		Variable v;
		Variables variables;

			try{
				
				variables = somData.getVariables() ;
				
				if (variables.size()<=1){
					if (sfProperties.getModelingSettings().getVariables().size()>1){
						
						variables = sfProperties.getModelingSettings().getVariables() ; 
						somData.setVariables(variables) ;
						variables = somData.getVariables() ;
					}
				}
				
				tvindex = variables.getTvColumnIndex() ;
				if (tvindex<0){
					// ??? 
					// after switching to another project...
				}else{
					tvLabel = variables.getItem(tvindex).getLabel();
				}
				/*
				if (variables.getTvColumnIndex()<0){
					
					if (variables.getTargetVariable()==null){
					
						if (.getTargetedModeling()){
							if (sfProperties.getModelingSettings().getTargetVariableCandidates()){
								
							}
						}
					}
					
				}else{
					
				}
				*/
				/*
				 *	the normalized table will still contain non-num columns,
				 *  yet we will create an structural identifier, that contains only those headers which are numeric 
				 */
		
				
				
				cn = transformationModel.variableTransformations.size() ;
				i = -1;
				
				while (i<cn-1){
					i++;
		
					varTStack = transformationModel.variableTransformations.get(i) ;
					v = varTStack.baseVariable ;
					
					varLabel = v.getLabel() ;
					variables.getAbsoluteAccessible().add(0) ;
					
					if (v.getRawFormat() > DataTable.__FORMAT_BIN){// __FORMAT_ORGINT)
						variables.getAbsoluteAccessible().set(i,0) ;
						variables.addBlacklistLabel( v.getLabel() ) ;
						continue;
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
					hb = (v.getRawFormat() > DataTable.__FORMAT_ID) && (v.getRawFormat() <= DataTable.__FORMAT_BIN); // .__FORMAT_INT
					// 
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
								varTStack.introduceAlgorithmizedStackPosition("StatisticalDescriptionStandard") ;
							}
							
							hb = varTStack.algorithmIsLatest("LinearNormalization");
							
							if (hb==false){
								StackedTransformation sti;
								sti = varTStack.introduceAlgorithmizedStackPosition("LinearNormalization") ;
								// NOT for target variables !!!! or target variable candidates 
								if ((variables.getIndexByLabel(varLabel)!=variables.getTvColumnIndex()) &&
									(variables.isTargetVariableCandidate(v.getLabel(),1)==false) &&
									(varTStack.firstFormat == DataTable.__FORMAT_BIN)){
									sti.inFormat = DataTable.__FORMAT_BIN ;
									
									AlgorithmParameters algorithmParams = new AlgorithmParameters( (AlgorithmIntf)(sti.algorithm));
									AlgorithmParameter algoparam = new AlgorithmParameter();
									algoparam.setStrValue("bin");
									algoparam.setIntValues( new int[]{1}) ; // intensity of "flattening" :1->[0.3,0.7] , 2->[0.4, 0.6]
									algorithmParams.add(algoparam);
									((AlgorithmIntf)(sti.algorithm)).setParameters(algorithmParams);
								}else{
									sti.inFormat = DataTable.__FORMAT_NUM ;
								}
							}
								
							varTStack.setFirstItemForUpdate( varTStack.items.size()-1);
							varTStack.update();
							
						
					} // format ?
					
				} // ->

			cn = 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * basic normalization outside of transformation model
	 * 
	 * @param variables
	 * @return
	 */
	public DataTable normalizeData( Variables variables ) {

		DataTableCol col, colNorm;
		ArrayList<DataTableCol> inDataTable = dataTableObj.getDataTable();
		ArrayList<DataTableCol> normDataTable;

		try {

			dataTableNormalized = new DataTable(somData, true); // the whole object

			dataTableNormalized.setSourceFileName(dataTableObj.getSourceFilename());
			dataTableNormalized.setTableHasHeader(dataTableObj.isTableHasHeader());
			dataTableNormalized.setFormats(dataTableObj.getFormats().clone());
			dataTableNormalized.setColcount(dataTableObj.getColcount());
			dataTableNormalized.setRowcount(dataTableObj.getRowcount());
			dataTableNormalized.setColumnHeaders(new ArrayList<String>(dataTableObj.getColumnHeaders()));

			normDataTable = dataTableNormalized.getDataTable();

			for (int i = 0; i < inDataTable.size(); i++) {

				col = inDataTable.get(i);

				if ((col.getRecalculationIndicator() > 0)) {

					if (col.isIndexColumnCandidate() == false) {
						col.calculateBasicStatistics();
					}
					// ArrayList<Double> cellValues
				} else {
					continue;
				}

				// we do not normalize index columns, we won't include it in the
				// analysis anyway
				col.setRecalculationIndicator(0);
				colNorm = new DataTableCol(dataTableNormalized, col);// dataTableNormalized, i

				if ((col.getDataFormat() <= DataTable.__FORMAT_DATETIME) && (col.isIndexColumnCandidate() == false)) {

					colNorm.normalize(col.getStatisticalDescription().getMini(), col.getStatisticalDescription().getMaxi());

					// calculate stats for the normalized column
					colNorm.calculateBasicStatistics();
					// store the statistical description for the raw data in the column
					// that contain the normalized data... such we will be able to translate !
					
					colNorm.setRawDataStatistics(col.getStatisticalDescription());
					colNorm.setNumeric(true);
					colNorm.setDataFormat(DataTable.__FORMAT_NUM);

				} else {
					col.setRecalculationIndicator(-3); // ignore, like
														// blacklisted variables
					colNorm.setDataFormat(DataTable.__FORMAT_IGNORE);
				}

				colNorm.setSerialID(i);
				normDataTable.add(colNorm);

			} // all columns

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataTableNormalized;
	}
	
	
	@Override
	public void ensureNormalizedDataRange() {
		// TODO Auto-generated method stub
		
	}
 

	@Override
	public DataTable getNormalizedDataTable() {
		return dataTableNormalized;
	}

	@Override
	public int addDataColumn(DataTableCol column, String name, int target) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addDataRecords() {
		// TODO Auto-generated method stub
		return 0;
	}

  
	@Override
	public FileOrganizer getFileorg() {
		
		return fileorg; 
	}

	@Override
	public int save() {
		return -1;
	}
 
	@Override
	abstract public SomTransformer getSelfReference() ;

	public SomFluidAppGeneralPropertiesIntf getSfProperties() {
		return sfProperties;
	}

	public DataTable getDataTableObj() {
		return dataTableObj;
	}

	public DataTable getDataTableNormalized() {
		return dataTableNormalized;
	}

	public void setDataTableNormalized( DataTable normalizedDataTable) {
		dataTableNormalized = normalizedDataTable ;
	}
	
	
	public TransformationModel getTransformationModel() {
		return transformationModel;
	}

	public SomAssignatesDerivations getSomDerivations() {
		return somDerivations;
	}

	public SomDataObject getSomData() {
		return somData;
	}

	public void setSomData(SomDataObject somData) {
		this.somData = somData;
	}


	public ArrayList<String> getChainedVariables() {
		return chainedVariables;
	}

	// === used only by some descendants ======================================
	@Override
	public void applyAdvNumericalTransforms(IndexedDistances listOfPutativeTransforms) {
	}

	@Override
	public IndexedDistances createDefaultListOfAdvancedTransforms() {
		return null;
	}
 
	@Override
	public void perceiveCandidateTransformations( AnalyticFunctionTransformationsIntf candidates,
												  int intoFreshStack) {
	}
 
	@Override
	public SomDataObject implementWaitingTransformations() {
		return null;
	}
  
 
	@Override
	public void createSurrogateData(double percentage, int mode) {
	}
 
	@Override
	public void importExpectedNormValueRanges(String filename) {
	}
 
	@Override
	public void importExpectedNormValueRanges(NormValueRangesIntf valueRanges) {
	}
 
	@Override
	public void saveXml() {
	}
 
	@Override
	public void extractTransformationsXML(boolean embed) {
	}
 
	@Override
	public void initializeTransformationModel() {
	}
 
	@Override
	public void createDataDescriptions() {
	}
 
	@Override
	public ArrayList<String> getXmlImage() {
		return null;
	}
	
}
