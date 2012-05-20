package org.NooLab.somfluid.app;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.env.data.NormValueRangesIntf;
import org.NooLab.somsprite.AnalyticFunctionTransformationsIntf;
import org.NooLab.somtransform.SomTransformerIntf;
import org.NooLab.somtransform.TransformationModel;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * taking a table and transforming them the incoming data
 * 
 * 
 * 
 *
 */
public class SomAppTransformer implements 	Serializable,
											SomTransformerIntf,
											DataHandlingPropertiesIntf{

	private static final long serialVersionUID = 3714702636726225524L;


	SomAppTransforms soappTransforms;
	
	SomDataObject somData;
	TransformationModel transformationModel;
	
	String modelname;

	int nodeCount;
	
	transient PrintLog out;
	
	// ========================================================================
	public SomAppTransformer(){
		
		soappTransforms = new SomAppTransforms(this) ; 
		
		somData = new SomDataObject(this); // we will fill the data later

		// this structure we will have to reconstruct from the file
		transformationModel = new TransformationModel( (SomTransformerIntf)this, somData);
		
		out = somData.getOut() ;
	}
	// ========================================================================	





	public void createNodes() {
		soappTransforms.nodes = new ArrayList<SomAppAlgorithm>();
		
		for(int i=0;i<nodeCount;i++){
			
			SomAppAlgorithm sta = new SomAppAlgorithm(i);
			soappTransforms.nodes.add(sta);
		}
		
	}
	
	
	// ========================================================================
	@Override
	public String getDataSrcFilename() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean addDataSource(int sourceType, String filename) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setDataSrcFilename(String dataSrcFilename) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getDataUptakeControl() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setDataUptakeControl(int ctrlValue) {
		// TODO Auto-generated method stub
		
	}
 
	// ========================================================================

	@Override
	public void setDataTable(DataTable inDatatable) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public int basicTransformToNumericalFormat() {
		// TODO Auto-generated method stub
		return 0;
	}





	@Override
	public void applyAdvNumericalTransforms(IndexedDistances listOfPutativeTransforms) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public IndexedDistances createDefaultListOfAdvancedTransforms() {
		// TODO Auto-generated method stub
		return null;
	}





	@Override
	public void perceiveCandidateTransformations(AnalyticFunctionTransformationsIntf candidates,
			int intoFreshStack) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public SomDataObject implementWaitingTransformations() {
		// TODO Auto-generated method stub
		return null;
	}





	@Override
	public void normalizeData() {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void ensureNormalizedDataRange() {
		// TODO Auto-generated method stub
		
	}





	@Override
	public DataTable writeNormalizedData() {
		// TODO Auto-generated method stub
		return null;
	}





	@Override
	public DataTable getNormalizedDataTable() {
		// TODO Auto-generated method stub
		return null;
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
	public void createSurrogateData(double percentage, int mode) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void importExpectedNormValueRanges(String filename) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void importExpectedNormValueRanges(NormValueRangesIntf valueRanges) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
