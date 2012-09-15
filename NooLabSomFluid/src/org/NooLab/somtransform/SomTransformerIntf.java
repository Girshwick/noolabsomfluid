package org.NooLab.somtransform;

import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.env.data.NormValueRangesIntf;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.storage.DataTableCol;
import org.NooLab.somfluid.storage.FileOrganizer;
 
import org.NooLab.somsprite.AnalyticFunctionTransformationsIntf;
import org.NooLab.utilities.datatypes.IndexedDistances;




public interface SomTransformerIntf {
	/*
	/** shifting the transformation to the right or to the left   
	public static final int _ADV_TRANSFORM_LOGSHIFT    = 1;
	
	/** based on multi-modus = several maxima separated by a significant minimum   
	public static final int _ADV_TRANSFORM_MODESPLIT   = 2;
	
	/** responds to negative values with a modus in the slightly positive range  
	public static final int _ADV_TRANSFORM_ZSEMSPLIT   = 3;
	 
	/** residual analysis, either by simple statistics (linear, curvi-lin, or polynomial), 
	 *  or by advanced group-oriented methods (even SOM)
	  
	public static final int _ADV_TRANSFORM_STDEVRESIDS = 6; // utilizes "norm ranges" 
	public static final int _ADV_TRANSFORM_LCORRESIDS  = 7;
	public static final int _ADV_TRANSFORM_CCORRESIDS  = 8;
	public static final int _ADV_TRANSFORM_PCORRESIDS  = 9;
	
	public static final int _ADV_TRANSFORM_CLUSTRESID  = 11;
	public static final int _ADV_TRANSFORM_SOMRESID    = 12;

	
	/** reserved, not used  
	public static final int _ADV_TRANSFORM_CUSTOM_1    = 21;
	public static final int _ADV_TRANSFORM_CUSTOM_2    = 22;
	public static final int _ADV_TRANSFORM_CUSTOM_3    = 23;
	*/
	
	 
	// ....................................................

	public void setDataTable( DataTable inDatatable );
	
	// ......................
	
	public int basicTransformToNumericalFormat();
	
	// ......................
	
	public void applyAdvNumericalTransforms( IndexedDistances listOfPutativeTransforms )  ;
	
	public IndexedDistances createDefaultListOfAdvancedTransforms();

	// ......................
	
	public void perceiveCandidateTransformations( AnalyticFunctionTransformationsIntf candidates, int intoFreshStack) ;
	
	public SomDataObject implementWaitingTransformations( ) ;

	// ......................
	
	public void normalizeData();
	
	public void ensureNormalizedDataRange();
	
	public DataTable writeNormalizedData();
	
	public DataTable getNormalizedDataTable() ;
	
	// ......................
	
	// additional to the SomDataObject, also the SomTransformer allows to add data
	public int addDataColumn( DataTableCol column, String name, int target );
	
	public int addDataRecords( );

	/** 
	 * base and target is the table of normalized data;
	 * 
	 * on saving, these data will be saved too;
	 *  
	 * @param percentage based on the number of original records 
	 * @param mode  1=independent per column, 2=using Cholesky Decomposition
	 * 
	 */
	public void createSurrogateData( double percentage , int mode);
	
	// ......................
	
	public void importExpectedNormValueRanges( String filename );
	
	public void importExpectedNormValueRanges( NormValueRangesIntf valueRanges) ;

	public void saveXml();

	public FileOrganizer getFileorg();

	public int save();

	public void extractTransformationsXML(boolean embed);

	public void initializeTransformationModel();

	public void createDataDescriptions();

	public ArrayList<String> getXmlImage();

	public SomTransformer getSelfReference();

	public int getDerivationLevel();

	public TransformationModel getTransformationModel();

	// ......................
	
	
}
