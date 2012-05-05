package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;



@SuppressWarnings("rawtypes")
public class NomValEnum extends AlgoTransformationAbstract{

	private static final long serialVersionUID = -9150428977976910184L;
	
	String versionStr = "1.00.01" ;
	
	
	// ------------------------------------------------------------------------
	public NomValEnum(){
		
	}
	// ------------------------------------------------------------------------


	@Override
	public String getVersion() {
		return versionStr;
	}


 


	@Override
	public int calculate() {
		
		int result = -1,ix;
		double v, vr, _min, _max;
		String str ;
		
		ArrayList<String> divItems = new ArrayList<String>();  
		IndexDistance ixd;
		IndexedDistances ixds = new IndexedDistances();
		AlgorithmParameter algoparam;
		
		if (isStrData==false){
			result = -7;
			return result;
		}
		
		
		// determine the items
	
		try{
			
			for (int i=1;i<stringvalues.size(); i++){
				
				
				str = (String)stringvalues.get(i); 
				
				if ((divItems.indexOf(str)<0) && (str.length()>0) && (str.toLowerCase().contentEquals("-m.v.")==false)){
					divItems.add(str) ;
					ixd = new IndexDistance( ixds.size()+1, 1.0, str) ;
					ixds.add(ixd) ;
				}else{
					ix = ixds.getIndexByStr(str) ;
					if (ix>=0){
						ixd = ixds.getItem(ix) ;
						ixd.setDistance( ixd.getDistance()+1.0) ;
					}
				}
				
			}// -> all values
			
			ixds.sort(-1) ;
			
			String[] nveStrings = ixds.getStringItems();
			// from that, calculate similarity matrix for the strings
			
			/*
			 * we could apply 
			 * - expressing similarity by string into enum distance
			 * - optimal scaling       -> external by extra and dedicated algorithm
			 * - relative risk weight  -> external by extra and dedicated algorithm
			 * 
			 * so, string similarity could be checked, and if there is a tendency, then we could use it
			 */
			
			result=1;
			
			// now get the translation table from ixds
			algoparam = new AlgorithmParameter();
			
			algoparam.setIntValues( ixds.getIndexItems() ) ;
			algoparam.setStrValues( nveStrings ) ;
			algoparam.setNumValues( ixds.getDistanceItems() ) ;
			
			
			for (int i=0;i<ixds.size();i++){
				ixd = ixds.getItem(i) ;
				algoparam.putValuePair(ixd.getIndex(),ixd.getGuidStr() ) ;
			}
			algoparam.obj = new IndexedDistances(ixds.getItems());
			parameters.add(algoparam) ;
			
			outvalues.clear() ;
			
			for (int i=1;i<stringvalues.size(); i++){
				
				str = (String)stringvalues.get(i); 
				
				if ((divItems.indexOf(str)<0) || (str.length()==0) || (str.toLowerCase().contentEquals("-m.v.") )){
					v = -1.0 ;
				}else{
					ix = ixds.getIndexByStr(str) ;
					ixd = ixds.getItem(ix) ;
					v = (double)ix + 1;
				}
				outvalues.add( v );
				
			}// -> all values
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		// create an encoding and save it to the parameters
		// check similarity of strings !!
		
		
		 
		
		// v = (Double)values.get(i) ;
		
		// create the outValues
		
		// outvalues.add(vr);
		
		
		hasParameters = true;
		
		return result ;
	}


	@Override
	public ArrayList<Double> getValues(int part) {
		
		return outvalues;
	}


	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}


 


	@Override
	public ArrayList<Double> getTransformedValues() {
		return outvalues;
	}


	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
}
