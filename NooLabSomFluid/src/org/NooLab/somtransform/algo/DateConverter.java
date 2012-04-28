package org.NooLab.somtransform.algo;

import java.util.ArrayList;
import java.util.Date;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.util.DateConversion;



public class DateConverter extends AlgoTransformationAbstract  {

	private static final long serialVersionUID = -3084734378478287309L;
	

	int typeInfo = AlgorithmIntf._ALGOTYPE_VALUE ;
	
	String versionStr = "1.00.01" ;
	
	
	// ------------------------------------------------------------------------
	public DateConverter(){
		
	}
	// ------------------------------------------------------------------------


	@Override
	public String getVersion() {
		return versionStr;
	}


	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}


	public String normalizeRawDateString( String inStr){
		
		
		return inStr;
	}
	
	
	@Override
	public int calculate() {
		 
		int rc;
		String vstr ,cdstr, dstr ;
		DateConversion dateConversion = new DateConversion();
		Date date;
		int r = -1 ,datevalue;
		
		try{
			
			outvalues.clear() ;
			
			// first we scan 100 items: for each part of the date we determine min,max,mean
			rc = stringvalues.size();
			if (rc>100)rc=100;
			
			// for (int i=1;i<rc; i++){ }
			rc=0;
			//
			for (int i=1;i<stringvalues.size(); i++){
				
				vstr = (String)stringvalues.get(i); 
				dstr = normalizeRawDateString(vstr) ;
				
				r = dateConversion.convert(dstr) ;
				if (r==0){
					datevalue = dateConversion.getSerialDateValue() ;
				}else{
					datevalue = -1;
				}
				
				outvalues.add( (double)datevalue) ;
			} // 
			
			r = outvalues.size() ;

		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return r;
	}


	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
}
















