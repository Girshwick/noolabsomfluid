package org.NooLab.somtransform;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somtransform.algo.intf.AlgoColumnWriterIntf;
import org.NooLab.somtransform.algo.intf.AlgoMeasurementIntf;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.utilities.net.GUID;



/**
 * 
 * the "TransformationStack" is a series of "StackedTransformation"s in a fixed order 
 * 
 * a "StackedTransformation" is a container that provides 1 out of 3 possible standardized interfaces:
 * - for purely passive measurement like StandardStatistics, no transformation is applied
 * - a classical transformation, holding a column of in-values, 1+ buffered columns of values and precisely 1 column of out-values 
 * - for generating columns: as a simply copy of values into a new column, or writing    
 * 
 * input is taken as a a list of values of any format, i.e. also strings
 * 
 * any of the fields owns a minimum stack of MV, StandardStatistics, and Linear Normalization
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
public class TransformationStack implements Serializable{

	private static final long serialVersionUID = 4830618315665484506L;

	public static final String _DERIVAR_PREFIX = "d[##]_" ;
	
	// this is really globally unique
	String transformGuid = ""; // The SomTransformer has to maintain a map 
	
	int index;
	
	/** the variable to which this stack is associated  */
	Variable baseVariable ;
	/**  the fnal variable label as it appears in the data table */
	String varLabel ;
	
	ArrayList<String> inputVarLabels = new ArrayList<String>() ;
	
	/** the list of stack positions, each position contains an object "StackedTransformation"  */
	ArrayList<StackedTransformation> items = new ArrayList<StackedTransformation>();

	ArrayList<String> outputColumnIds = new ArrayList<String>();
	
	/** the positions of measurement items */
	ArrayList<Integer> dataDescriptionItems = new ArrayList<Integer>();
	int latestDataDescriptionItem = -1;
	
	private int firstFormat = -1;
	private int lastestFormat = -1;
	
	int firstItemForUpdate = 0;

	private boolean criticalErrorsWillBreak = true ;

	private boolean criticalErrorsAreVisible = true ;

	

	// ========================================================================
	public TransformationStack(){
		
		transformGuid = GUID.randomvalue(); 
		
	}
	// ========================================================================	


	 
	public String getGuid() {
		return transformGuid;
	}



	public int size() {
		 
		return items.size() ;
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



	@SuppressWarnings({ "rawtypes", "unused" })
	public StackedTransformation introduceAlgorithmizedStackPosition( String algoName )
																						throws Exception {

		String newLabel = "";
		StackedTransformation st = new StackedTransformation();

		// introduce an algorithm
		newLabel = varLabel + "_c";  // TODO: do this by a method which cares for double names...

		st.algorithmName = algoName; //
		
		// looking up the interface that an algo implements
		int algorithmtype = determineAlgoType( st.algorithmName );
		
			if (algorithmtype<0){
				throw(new Exception("The requested algorithm "+algoName+" could not be instantiated: unknown type of algorithm.")) ;
			}
			
		//
		st.algorithmType = algorithmtype; // AlgorithmIntf._ALGOTYPE_WRITER;
		st.outputColumnLabel = newLabel;
		st.createAlgoObject(st.algorithmType);
		st.idString = GUID.randomvalue() ;
		
		//
 
		if (st.algorithm == null){
	    	throw(new Exception("The requested algorithm "+algoName+" could not be instantiated: instantiation failed (object=null).")) ;
		}
	    
		Class c=null ;
		
		if (st.algorithmType == AlgorithmIntf._ALGOTYPE_PASSIVE ){
				AlgoMeasurementIntf am = (((AlgoMeasurementIntf) st.algorithm));
				c = am.getClass();
		}
		if (st.algorithmType == AlgorithmIntf._ALGOTYPE_VALUE ){
				AlgoTransformationIntf at = (((AlgoTransformationIntf) st.algorithm));
				c = at.getClass();
		}
		if (st.algorithmType == AlgorithmIntf._ALGOTYPE_WRITER ){
				AlgoColumnWriterIntf aw = (((AlgoColumnWriterIntf) st.algorithm));
				c = aw.getClass();
		} 
		
		if (c!=null){
			String className = c.getName();
			Method[] mm = c.getMethods();
			int n = mm.length;

			Class[] interfaces = c.getInterfaces();

			// check whether it implements one of the correct interfaces, and respective methods
			
			items.add(st);
		}
		
		return st;
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
	 * so update should ensure that, in a recursive manner, until a nonderived variable can be updated from normalized data
	 * 
	 * 
	 */
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
				
				if ((firstItemForUpdate>0) && (firstItemForUpdate==i)){
					if (sti.inData.size()==0){
						stp = items.get(i-1) ;
						previousOutValues = stp.outData ;
						sti.inData.add( previousOutValues ) ; // XXX in case of arithmet expression, there could be several sources !
					}
				}
				// providing the outdata of step (i) as indata for step (i+1)
				if ((i>0) && (previousOutValues!=null) && (previousOutValues.size()>0)){
					
					
					// use this only for index 0
					if ((sti.inData.size()==0) || (sti.multiVarInput==false)){
						sti.inData.clear(); // possibility for caching here....
						sti.inData.add( previousOutValues  ) ;  // in case of expressions, we may have several input columns
					}else{
						// in case of multi variable input, the algo knows itself about the other sources,
						// only the first column (==itself) will needs to be dynamic here!
						sti.inData.set(0, previousOutValues  ) ;
						// any instance of arithmet expression carries its own references for the required data source columns
					}
					
					
				}else{
					// now for this stack item indat -> transform -> outdata
					if (sti.inData.size()>0){
						dataColValues = (ArrayList<Double>) sti.inData.get(0) ; // just an abbrev.
					}else{
						r=0;
					}
				}
				
				if ((i==0) && (sti.inData.size()==0)){
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
					r = valgo.setValues( sti.inData ) ;
					
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
					valgo.calculate() ; 
					
					if ((rc>=0 ) && (rc<10)){
						sti.createOutData(valgo.getValues(0));

						if (valgo.hasParameters()) {
							// e.g. in case of NumValEnum

						}
					}else{
						result = -9;
					}
					
										     // -1=input data, 0+= (first)+ col of out data,
				} // _ALGOTYPE_VALUE ?
				
				if (algotype == AlgorithmIntf._ALGOTYPE_PASSIVE){
					// 
					// currently, DataTableCol provides the method "calculateBasicStatistics()" and contains "BasicStatisticalDescription"
					
					malgo = ((AlgoMeasurementIntf)sti.algorithm) ;
					
					malgo.setValues( sti.inData ) ;
					malgo.calculate() ;
					
					sti.dataDescription = malgo.retrieveDescriptiveResults() ;
					sti.createOutData( previousOutValues ) ;
					 
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



	private int determineAlgoType(String algorithmName) {
		int atyp = -1;
		
		StackedTransformation st = new StackedTransformation();
		
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
	
}
