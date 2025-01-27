package org.NooLab.somtransform;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.NooLab.somfluid.SomFluidPluginSettings;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somtransform.algo.intf.AlgoColumnWriterIntf;
import org.NooLab.somtransform.algo.intf.AlgoMeasurementIntf;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.strings.StringsUtil;

import com.jamesmurty.utils.XMLBuilder;



/**
 * 
 * the "TransformationStack" is a series of "StackedTransformation"s in a fixed order 
 * 
 * a "StackedTransformation" is a container that provides 1 out of 3 possible standardized interfaces:
 * - for purely passive measurement like StatisticalDescriptionStandard, no transformation is applied
 * - a classical transformation, holding a column of in-values, 1+ buffered columns of values and precisely 1 column of out-values 
 * - for generating columns: as a simply copy of values into a new column, or writing    
 * 
 * input is taken as a a list of values of any format, i.e. also strings
 * 
 * any of the fields owns a minimum stack of MV, StatisticalDescriptionStandard, and Linear Normalization
 * 
 * insertion is allowed only for "plaincopy", 
 * 
 * 
 * TODO:
 * 
 * - algorithms need to be Serializable
 * - it should allow dynamic addressing of random-read cells, based on values
 * - 
 *
 */
public class TransformationStack implements Serializable {

	private static final long serialVersionUID = 4830618315665484506L;

	public static final String _DERIVAR_PREFIX = "d[##]_" ;
	
	transient SomFluidPluginSettings pluginSettings;
	transient SomTransformer somTransformer;
	
	// this is really globally unique
	String transformGuid = ""; // The SomTransformer has to maintain a map 
	
	int index;
	
	boolean isExported = true;
	
	/** the variable to which this stack is associated  */
	Variable baseVariable ;
	
	/**  the final variable label as it appears in the data table */
	String varLabel ;
	
	ArrayList<String> inputVarLabels = new ArrayList<String>() ;
	
	/** the list of stack positions, each position contains an object "StackedTransformation"  */
	ArrayList<StackedTransformation> items = new ArrayList<StackedTransformation>();

	ArrayList<String> outputColumnIds = new ArrayList<String>();
	
	/** the positions of measurement items */
	ArrayList<Integer> dataDescriptionItems = new ArrayList<Integer>();
	private int latestDataDescriptionItem = -1;
	
	int firstFormat = -1;
	int lastestFormat = -1;
	
	int firstItemForUpdate = 0;

	private boolean criticalErrorsWillBreak = true ;

	private boolean criticalErrorsAreVisible = true ;

	transient SomFluidXMLHelper xEngine = new SomFluidXMLHelper();

	// ========================================================================
	public TransformationStack( SomTransformerAbstract somtransformer, SomFluidPluginSettings pluginsettings ){
		
		somTransformer = somtransformer.getSelfReference() ;
		
		transformGuid = GUID.randomvalue(); 
		pluginSettings = pluginsettings;
		
		// -> TransformationEnvIntf. get
	}
	 
	
	// ========================================================================	

 
	public String getGuid() {
		return transformGuid;
	}



	public int size() {
		 
		return items.size() ;
	}

	
	private String numerize( double value, int digits){
		String numstr="";
	
		numstr = String.format("%."+digits+"f", value);
		
		numstr = StringsUtil.trimTrailingZeroes(numstr);
		
		return numstr;
	}
	
	private String booleanize( boolean flag){
		String bstr="";
		
		if (flag){ bstr="true";}else{bstr="false";}
		
		return bstr;
	}

	public String getXML(  int zpos) {
		return getXML( null, zpos, true) ;
	}
	
	public String getXML( XMLBuilder builder , int zpos, boolean xStrRequested) {
		
		String xstr = "",xmlstr="",varLabel;
		StackedTransformation stTransform;
		AlgorithmParameters algoparams;
		boolean localXml, isBlack=false, isAbsExcluded=false;
		/*
		ArrayList<String> inputVarLabels = new ArrayList<String>() ;
		ArrayList<StackedTransformation> items = new ArrayList<StackedTransformation>();
		ArrayList<String> outputColumnIds = new ArrayList<String>();
		*/
		
		// preparations
		
		varLabel = baseVariable.getLabel();
		isBlack  = somTransformer.somData.getVariables().getBlacklistLabels().indexOf(varLabel)>=0; 
		isAbsExcluded = somTransformer._sfProperties.getAbsoluteFieldExclusions().indexOf(varLabel)>=0;
		
		
		// opening
		if (builder==null){
			builder = xEngine.getXmlBuilder( "column" ).a( "label", varLabel ).a( "guid", transformGuid ).a("index", ""+zpos);
			localXml = true;
		}else{
			builder = builder.e( "column" ).a( "label", varLabel ).a( "guid", transformGuid ).a("index", ""+zpos);
			localXml = false;
		}
		
if (varLabel.contains("Rechtsform")){
	int k;
	k=0;
}
		// properties: isID idcand tv tvcand isderived isexported
		builder = builder.e("properties")
							 
		           		     .e("id").a("idcolumn",booleanize(baseVariable.isID())).up() 
		           		     .e("tv").a("tvcolumn",booleanize(baseVariable.isTV())).up()
		           		     .e("idcandidate").a("idccolumn",booleanize(baseVariable.isID())).up() 
		           		     .e("tvcandidate").a("tvccolumn",booleanize(baseVariable.isTV())).up()
		           		     .e("derived").a("isderived",booleanize(baseVariable.isDerived())).up()
		           		     .e("format").a("value","").up()
		           		 .up() ;

		builder = builder.e("relations")
						     .e("export").a("value", booleanize(isExported)).up()
						     .e("isExcluded").a("value", booleanize(isAbsExcluded)).up()
						     .e("outcolumn");
						     	 // loop ...
								if ((outputColumnIds!=null) && (outputColumnIds.size()>0)){
									for (int i=0;i<outputColumnIds.size();i++){
										builder = builder.e("item").a("id", outputColumnIds.get(0)).up() ;
									}
								}
						         
							  builder = builder.up()
		           		  .up() ;
		
		
		
		builder = builder.e("algorithms").a("length", ""+items.size());
	
		for (int i=0;i<items.size();i++){
			stTransform = items.get(i);
			
			// xstr = xEngine.transcodeStackedTransformation(i,stTransform) ;
			 
			builder = builder.e("algoritem").a("index", ""+i).a("name", stTransform.algorithmName ).a("type", ""+ stTransform.getAlgorithmType());
			
			if ((stTransform.dataDescription.max != -1) && (stTransform.dataDescription.max != stTransform.dataDescription.min)){ 
				builder = builder.e("datadescription") 
									.e("values")
										.a("min", numerize(stTransform.dataDescription.min,7))
										.a("max", numerize(stTransform.dataDescription.max,7))
										.a("mean", numerize(stTransform.dataDescription.mean,7))
										.a("median", numerize(stTransform.dataDescription.median,7))
										.a("variance", numerize(stTransform.dataDescription.variance,7))

									 .up()	
				.up() ;
			}
			
			// parameters
			XMLBuilder pb, paramsbuilder;
			
			paramsbuilder = xEngine.getXmlBuilder( "parameters" );  
			// builder = builder.e("parameters");
			
						// get parameters from algorithm, this checks the algotype
			          
			
						algoparams = getAlgorithmsParameters( stTransform.algorithm ) ; 
						if (algoparams!=null){
							pb = serialxAlgoParams(algoparams);
							paramsbuilder = paramsbuilder.importXMLBuilder( pb );
						}
						
						if (stTransform.getAlgorithmType() == AlgorithmIntf._ALGOTYPE_WRITER){
							paramsbuilder = paramsbuilder.e("outlabel").a("value", stTransform.outputColumnLabel).up();
						}	
						
					 
			paramsbuilder = paramsbuilder.up().up();
			xmlstr = xEngine.getXmlStr(paramsbuilder, false);
			
			if (xmlstr.contains("<!-- -->")){
				// paramsbuilder = xEngine.getXmlBuilder( "" ); 
			}else{
				builder = builder.importXMLBuilder( paramsbuilder ) ;	
			}
			
			
			builder = builder.up() ;
		} // i->
		
		
		builder = builder.c("algorithms");
		
		xEngine.digestList(inputVarLabels);
		xEngine.digestList(outputColumnIds);

		if ((localXml) || (xStrRequested)){
			xmlstr = xEngine.getXmlStr(builder, false);
		}else{
			xmlstr="\n<!--  -->\n" ;
		}
		// builder.importXMLBuilder(builder)
		return xmlstr ;
	}

	/**
	 * 
	 * everything that is  !null or size>0 will be transcribed
	 * 
	 * @param aps
	 * @return
	 */
	private XMLBuilder serialxAlgoParams(AlgorithmParameters aps){
		String xStr = "", str ;
		XMLBuilder builder = null;
		AlgorithmParameter ap;
		int paramsfound = 0;
		
		
		builder = xEngine.getXmlBuilder( "params" );
		
		for (int i=0;i<aps.getItems().size();i++){
			ap = aps.getItems().get(i);
			builder = builder.e("set").a("index", ""+i);
				
				if ((ap.getIntValues()!=null) && (ap.getIntValues().length>0)){
					str = somTransformer.arrutil.arr2text( ap.getIntValues() ).trim() ;
					str = somTransformer.strgutils.replaceAll(str, " ", ";");
					builder = builder.e("intvalues").a("count", ""+ap.getIntValues().length).a("list", str).up();
					paramsfound++;
				}
			
				if ((ap.getNumValues()!=null) && (ap.getIntValues().length>0)){
					str = somTransformer.arrutil.arr2text( ap.getNumValues() ).trim() ;
					str = somTransformer.strgutils.replaceAll(str, " ", ";");
					builder = builder.e("numvalues").a("count", ""+ap.getNumValues().length).a("list", str).up();
					paramsfound++;
				}
				if ((ap.getStrValues()!=null) && (ap.getStrValues().length>0)){
					str = somTransformer.arrutil.arr2text( ap.getStrValues() ).trim() ;
					str = somTransformer.strgutils.replaceAll(str, " ", ";");
					builder = builder.e("strvalues").a("count", ""+ap.getStrValues().length).a("list", str).up();
					paramsfound++;
				}
				if ((ap.getList()!=null) && (ap.getList().size()>0)){
					// ArrayList<Object> -> serialized
					str="";
					builder = builder.e("objects");
					for (int k=0;k<ap.getList().size();k++){
						Object obj = ap.getList().get(k) ;
						str = somTransformer.strobj.encode( obj ) ;
						builder = builder.e("item").a("encoding", str).up();
					}
					builder = builder.up() ;
					paramsfound++;
				}
				/*
				if ((ap.getValuePairs()!=null) && (ap.getValuePairs().size()>0)){
					Object obj = ap.getValuePairs();
					str = somTransformer.strobj.encode( obj ) ;
					builder = builder.e("valuepairs").a("encoding", str).up();
					paramsfound++;
				}
				*/
				//
				if (ap.getLabel().length()>0){
					builder = builder.e("label").a("value", ap.getLabel());
					paramsfound++;
				}
				if (ap.getNumValue()!=-1.0){
					str = somTransformer.strgutils.numerize(ap.getNumValue(), 7).trim();
					builder = builder.e("label").a("value", str).up();
					paramsfound++;
				}
				/*
				if (ap.getObj()!=null){
					// Object -> serialized
					Object obj = ap.getObj();
					str = somTransformer.strobj.encode( obj ) ;
					builder = builder.e("object").a("encoding", str).up();
					paramsfound++;
				}
				*/
				if (ap.getStrValue().length()>0 ){
					builder = builder.e("string").a("value", ap.getStrValue()).up();
					paramsfound++;
				}
				if (ap.getTypeLabel().length()>0 ){
					builder = builder.e("string").a("value", ap.getTypeLabel()).up();
					paramsfound++;
				}
				
			builder = builder.up();
		}// i->
		
		builder.up();
		
		if (paramsfound==0){
			builder = builder.c(" ") ;
		}
		
		return builder;
	}

	private AlgorithmParameters getAlgorithmsParameters(Object algorithm) {
		AlgorithmIntf genAlgo;
		AlgoTransformationIntf tAlgo ;
		AlgoColumnWriterIntf wAlgo ;
		AlgorithmParameters ap=null ;
		int atyp;
		
		
		genAlgo = (AlgorithmIntf)algorithm;
		atyp = genAlgo.getType();
		
		if (atyp== AlgorithmIntf._ALGOTYPE_VALUE){
			tAlgo = (AlgoTransformationIntf)algorithm ;
			ap = tAlgo.getParameters() ;
		}
		if (atyp== AlgorithmIntf._ALGOTYPE_WRITER){
			wAlgo = (AlgoColumnWriterIntf)algorithm ;
			ap = wAlgo.getParameters() ;
		}
		
		// contains : ArrayList<AlgorithmParameter> items
		
		return ap;
	}



	public void setPluginSettings(SomFluidPluginSettings pluginSettings) {
		this.pluginSettings = pluginSettings;
	}


	public StackedTransformation getItem(int index) {
		StackedTransformation st;
		
		if ((index>=0) && (index<items.size())){
			st = items.get(index);
		}else{
			st = null;
		}
		
		return st;
	}




	public StackedTransformation introduceAlgorithmizedStackPosition( String algoName )
																						throws Exception {

		String newLabel = "";
		StackedTransformation st = new StackedTransformation( pluginSettings );

		// introduce an algorithm
		newLabel = varLabel + "_c";  // TODO: do this by a method which cares for double names...

		st.algorithmName = algoName; //
		
		// looking up the interface that an algorithm implements
		int algorithmtype = determineAlgoType( st.algorithmName );
		
			if (algorithmtype<0){
				throw(new Exception("The requested algorithm "+algoName+" could not be instantiated: unknown type of algorithm.")) ;
			}
			
		//
		st.setAlgorithmType(algorithmtype); // AlgorithmIntf._ALGOTYPE_WRITER;
		st.outputColumnLabel = newLabel;
		
		st.createAlgoObject(st.getAlgorithmType()); // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		
		st.idString = GUID.randomvalue() ;
		
		//
 
		if (st.algorithm == null){
	    	throw(new Exception("The requested algorithm "+algoName+" could not be instantiated: instantiation failed (object=null).")) ;
		}
		
		if (defineAlgorithmObject( st )){
			items.add(st);
		}
	    
		
		return st;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unused" })
	public boolean defineAlgorithmObject( StackedTransformation st ) throws Exception{
		boolean rB=false;

		Class c = null;

		if (st.getAlgorithmType() == AlgorithmIntf._ALGOTYPE_PASSIVE) {
			AlgoMeasurementIntf am = (((AlgoMeasurementIntf) st.algorithm));
			latestDataDescriptionItem = items.size();
			c = am.getClass();
		}
		if (st.getAlgorithmType() == AlgorithmIntf._ALGOTYPE_VALUE) {
			AlgoTransformationIntf at = (((AlgoTransformationIntf) st.algorithm));
			c = at.getClass();
		}
		if (st.getAlgorithmType() == AlgorithmIntf._ALGOTYPE_WRITER) {
			AlgoColumnWriterIntf aw = (((AlgoColumnWriterIntf) st.algorithm));
			c = aw.getClass();
		}

		if (c != null) {
			String className = c.getName();
			Method[] mm = c.getMethods();
			int n = mm.length;

			Class[] interfaces = c.getInterfaces();

			// check whether it implements one of the correct interfaces, and
			// respective methods

			rB=true;
		}

		return rB;
	}
	/**
	 * 
	 * @param mode 0=all upstream , 1=downstream (will set recalc flag to true for linked stacks), 2=all linked,up- & downstream
	 */
	public void updateBranch( int mode ) {

		
	}

	/**
	 * 
	 * update the originating stack 
	 * so update should ensure that, in a recursive manner, until a non-derived variable can be updated from normalized data
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public int update() {
		
		int result=0;
		boolean calced = false;
		StackedTransformation sti,stp, previousStackItem=null ;
		ArrayList<Double> dataColValues, previousOutValues = null ;
		
		int algotype, r,rc;
		AlgorithmIntf algo ;
		AlgoMeasurementIntf malgo;
		AlgoTransformationIntf valgo;
		
		 
		
		try{
			calced = false;
		
			int fp = 0; 
			if ((firstItemForUpdate>=0) && (firstItemForUpdate<items.size())){ 
				fp=firstItemForUpdate;
			} 
			// 
			
			if (items.size()==0){
				return -3;
			}
			

			
			for (int i=fp;i<items.size();i++){
				
				sti = items.get(i) ;
				
if (sti.getAlgorithmName().toLowerCase().contains("nomval")){
	int k;
	k=0;
}				
				if ((firstItemForUpdate>0) && (firstItemForUpdate==i)){
					if (sti.getInData().size()==0){
						stp = items.get(i-1) ;
						previousOutValues = stp.outData ;
						sti.getInData().add( previousOutValues ) ; // 
					}
				}
				// providing the out-data of step (i) as in-data for step (i+1)
				if ((i>0) && (previousOutValues!=null) && (previousOutValues.size()>0)){
					
					// use this only for index 0
					if ((sti.getInData()==null) || (sti.getInData().size()==0) || (sti.multiVarInput==false)){
						if (sti.getInData()==null){ sti.createInDataContainer();} 
						
						sti.getInData().clear(); // possibility for caching here....
						 
						// in case of expressions, we may have several input columns
						// the first variable refers most likely to the first param, all others to the provided
						sti.getInData().add( previousOutValues  ) ;  
					}else{
						// in case of multi variable input, the algorithm knows itself about the other sources,
						// only the first column (==itself) will needs to be dynamic here!
						sti.getInData().set(0, previousOutValues  ) ;
						// any instance of arithmet expression carries its own references for the required data source columns
					}
					
					
				}else{
					// now for this stack item indat -> transform -> outdata
					if (sti.getInData().size()>0){
						dataColValues = (ArrayList<Double>) sti.getInData().get(0) ; // just an abbrev.
					}else{
						r=0;
					}
				}
				
				if ((i==0) && (sti.getInData().size()==0)){
					result = -5;
					break;
				}
				
				// ..............................
				
				algo = ((AlgorithmIntf)sti.algorithm) ;
				algotype = algo.getType() ;
				
				
				if (algotype == AlgorithmIntf._ALGOTYPE_VALUE){
				
					valgo = ((AlgoTransformationIntf)sti.algorithm) ;
					
					if (latestDataDescriptionItem>=0){
						valgo.setDatDescription( items.get(latestDataDescriptionItem).dataDescription ) ;
					}
					
					// valgo.setValues(dataColValues) ;
					r = valgo.setValues( sti.getInData() ) ;
					
					if (r<0){
						// optional: break and throw Exception
						if (criticalErrorsWillBreak){
							if (criticalErrorsAreVisible){
								//throw(new Exception("algo.setValues(inData), result = "+r) );
								result = -11;
								break;
							}
						}else{
							continue;
						}
					}
					rc = 0;
					rc = valgo.calculate() ; 
					
					if ((rc>=0 )){ // rc=0 -> ok, rc>0 it returns the number of records calculated
						sti.createOutData(valgo.getValues(0));

						if (valgo.hasParameters()) {
							// e.g. in case of NumValEnum

						}
					}else{
						result = -9;
						sti.outData = (ArrayList<Double>) sti.getInData().get(0);
					}
					
										     // -1=input data, 0+= (first)+ col of out data,
				} // _ALGOTYPE_VALUE ?
				
				if (algotype == AlgorithmIntf._ALGOTYPE_PASSIVE){
					// 
					// currently, DataTableCol provides the method "calculateBasicStatistics()" and contains "BasicStatisticalDescription"
					
					malgo = ((AlgoMeasurementIntf)sti.algorithm) ;
					
					malgo.setValues( sti.getInData() ) ;
					malgo.calculate() ; // calc of standard stats correct ?
					
					if ((malgo.getParameters().isRecalculationBlocked()) && 
						(sti.dataDescription!=null) && 
						(sti.dataDescription.max>0.0)){
						// do nothing
						int k;
						k=0;
					}else{
						sti.dataDescription = new DataDescription( malgo.retrieveDescriptiveResults()) ;
					}
					sti.createOutData( previousOutValues ) ;
					double vv = items.get(i).dataDescription.max;
					// sti.outData.addAll( previousOutValues );

					dataDescriptionItems.add(i); 
					latestDataDescriptionItem = i;
				} // _ALGOTYPE_PASSIVE ?
				
				if (algotype == AlgorithmIntf._ALGOTYPE_WRITER){
					// update the in-data of the child column, AND as well its own outdata, just in order to keep the chain alive
					AlgoColumnWriterIntf walgo ;
					walgo = ((AlgoColumnWriterIntf)sti.algorithm) ;
					sti.createOutData( previousOutValues );
					sti.update() ;// not yet active
				}
				previousStackItem = sti;
				previousOutValues = sti.outData ;
				
			} // -> all items in stack
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result ;
	}
	
	/**
	 * returns the latest out-data of the stack (=out-data of the last transformation) either as 
	 * reference or as copy;</br>
	 * if the stack needs to get updated an "update()" will be performed.</br></br>
	 * 
	 * null will be returned, if</br>
	 * - there are no items in the stack</br>
	 * - there are no in-data</br> 
	 * 
	 * </br>
	 * @param asCopy 0=as reference, 1=as copy
	 * @return
	 */
	public ArrayList<Double> getOutData( int asCopy) {
		 
		ArrayList<Double> values=null, _cvalues;
		
		
		_cvalues = getLatestColumnValues(0);
		
		if ((items.size()>0) && ((_cvalues==null) || (_cvalues.size()==0))){
			update();
			if ((_cvalues==null) || (_cvalues.size()==0)){
				
			}else{
				_cvalues = getLatestColumnValues(0);
			}
		}
		
		if (asCopy<=0){
			values = _cvalues;
		}else{
			if ((_cvalues!=null) && (_cvalues.size()>0)){
				values = new ArrayList<Double>(_cvalues) ;
			}
		}
		
		return values;
	}



	/**
	 * this visits the last transformation item in the stack, and returns the reference to the values as
	 * they are stored in the stack;</br></br>
	 * 
	 * dependent on the state of the embedding processing, these data could differe from the data in
	 * the tables of the SomDataObject: only after explicitly writing the stack data to the table the
	 * data will be identicial.</br></br>
	 * 
	 * @param checkValueRange  0=no check, 1=throw an exception, 2=set range violation to -1, 3=set range violation to next border of [0..1]
	 * @return
	 */
	public ArrayList<Double> getLatestColumnValues( int checkValueRange ){
		
		ArrayList<Double> colData = new ArrayList<Double>();
		
		StackedTransformation lastTransformation ;
		
		if (items.size()>0){
			lastTransformation = items.get(items.size()-1) ;
			
			// the last transformation need not be the linear norm
			if (lastTransformation != null){
				
				colData = lastTransformation.getOutData() ;
				
				if (checkValueRange>0){
					for (int i=0;i<colData.size();i++){
						if (colData.get(i)<0){
							colData.set(i, 0.0);
						}
						if (colData.get(i)>1.0){
							colData.set(i, 1.0);
						}
						
					} // all values
				}
			}
		
		}
		
		return colData;
	}
	
	
	public StackedTransformation getLastPosition() {
		StackedTransformation st;
		
		st = items.get( items.size()-1) ;
		return st;
	}



	public StackedTransformation getFirstPosition() {
		StackedTransformation st=null ;
		
		if (items.size()==0){
			try {
			
				introduceAlgorithmizedStackPosition("MissingValues") ;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (items.size()>0){
			st = items.get(0) ;
		}
		return st;
	}



	public int determineAlgoType(String algorithmName) {
		int atyp = -1;
		
		StackedTransformation st = new StackedTransformation(pluginSettings);
		
		st.algorithmName = algorithmName;
		st.createAlgoObject( AlgorithmIntf._ALGOTYPE_GENERIC );
		
		atyp = ((AlgorithmIntf)st.algorithm).getType() ;
		
		st = null;
		
		return atyp;
	}



	/**
	 * @return the transformGuid
	 */
	public String getTransformGuid() {
		return transformGuid;
	}



	/**
	 * @return the baseVariable
	 */
	public Variable getBaseVariable() {
		return baseVariable;
	}



	/**
	 * @return the varLabel
	 */
	public String getVarLabel() {
		return varLabel;
	}



	public boolean isExported() {
		return isExported;
	}

	public void setExported(boolean isExported) {
		this.isExported = isExported;
	}


	/**
	 * @return the items
	 */
	public ArrayList<StackedTransformation> getItems() {
		return items;
	}



	public void setLatestFormat(int format) {
		// the stack position also knows about its informat, outformat !!
		lastestFormat = format;
		if (firstFormat<0){
			firstFormat = lastestFormat;
		}
	}

    public int getLatestFormat() {

    	if (items.size()>0){
    		lastestFormat = items.get(items.size()-1).getOutFormat() ;
    	}else{
    		lastestFormat=-1;
    	}

		return lastestFormat;
	}
    
    public int getFirstStackInFormat() {
    	
    	if (items.size()>0){
    		firstFormat = items.get(0).getInFormat();
    	}else{
    		firstFormat=-1;
    	}
    	return firstFormat;
    }


    /**
     * 
     * this checks whether a standard statistics description is necessary or not before 
     * applying algos like LinearNormalization
     *  
     * @return true if it is missing
     * 
     */
	public boolean checkDescriptionPosition() {
		boolean rB = false;
		
		try{
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return rB;
	}



	public boolean stackProvidesOutData() {
		boolean rB = true;
		

		try{
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return rB;
	}



	public boolean algorithmIsLatest(String algoLabel) {
		
		boolean rB=false;
		StackedTransformation item ; 
		
		if (items.size()>0){
			item = items.get(items.size()-1);
			if (item.algorithmName.contentEquals(algoLabel)){
				rB=true;
			}
		}
		
		return rB;
	}



	public void setFirstItemForUpdate(int firststackpos) {

		firstItemForUpdate = firststackpos;
	}


	public  String getInputVariable(int index) {
		String varlabel="";
		
		if ((index>=0) && (index<inputVarLabels.size())){
			varlabel = inputVarLabels.get(index);
		}
		
		return varlabel;
	}
	
	public  ArrayList<String> getInputVariables() {
		return inputVarLabels;
	}
	
	public void setInputVariables(String[] varLabels) {
		 
		if ((varLabels!=null) && (varLabels.length>0)){
			this.inputVarLabels = new ArrayList<String>(Arrays.asList( varLabels ));
		}
		
	}
 
	public ArrayList<String> getInputVarLabels() {
		return inputVarLabels;
	}
 
	public void setInputVarLabels(String[] varLabels) {
		
		if ((varLabels!=null) && (varLabels.length>0)){
			this.inputVarLabels = new ArrayList<String>(Arrays.asList( varLabels ));
		}
	}
	public void setInputVarLabels(ArrayList<String> varLabels) {
		
		if ((varLabels!=null) && (varLabels.size()>0)){
			inputVarLabels.clear();
			inputVarLabels.addAll( varLabels);
		}
	}
	public void addInputVarLabel( String varLabel) {
		
		if (varLabel.length()>0){
			inputVarLabels.add( varLabel );
		}
	}


	// just for serializing ...
	
	public int getIndex() {
		return index;
	}



	public void setIndex(int index) {
		this.index = index;
	}



	public ArrayList<String> getOutputColumnIds() {
		return outputColumnIds;
	}



	public void setOutputColumnIds(ArrayList<String> outputColumnIds) {
		this.outputColumnIds = outputColumnIds;
	}



	public ArrayList<Integer> getDataDescriptionItems() {
		return dataDescriptionItems;
	}



	public void setDataDescriptionItems(ArrayList<Integer> dataDescriptionItems) {
		this.dataDescriptionItems = dataDescriptionItems;
	}



	public int getLatestDataDescriptionItem() {
		return latestDataDescriptionItem;
	}



	public void setLatestDataDescriptionItem(int latestDataDescriptionItem) {
		this.latestDataDescriptionItem = latestDataDescriptionItem;
	}



	public int getFirstFormat() {
		return firstFormat;
	}



	public void setFirstFormat(int firstFormat) {
		this.firstFormat = firstFormat;
	}



	public int getLastestFormat() {
		return lastestFormat;
	}



	public void setLastestFormat(int lastestFormat) {
		this.lastestFormat = lastestFormat;
	}



	public boolean isCriticalErrorsWillBreak() {
		return criticalErrorsWillBreak;
	}



	public void setCriticalErrorsWillBreak(boolean criticalErrorsWillBreak) {
		this.criticalErrorsWillBreak = criticalErrorsWillBreak;
	}



	public boolean isCriticalErrorsAreVisible() {
		return criticalErrorsAreVisible;
	}



	public void setCriticalErrorsAreVisible(boolean criticalErrorsAreVisible) {
		this.criticalErrorsAreVisible = criticalErrorsAreVisible;
	}



	public int getFirstItemForUpdate() {
		return firstItemForUpdate;
	}



	public void setTransformGuid(String transformGuid) {
		this.transformGuid = transformGuid;
	}



	public void setBaseVariable(Variable baseVariable) {
		this.baseVariable = baseVariable;
	}



	public void setVarLabel(String varLabel) {
		this.varLabel = varLabel;
	}



	public void setItems(ArrayList<StackedTransformation> items) {
		this.items = items;
	}
	 
	
}
