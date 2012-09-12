package org.NooLab.somtransform;

import java.util.ArrayList;




public interface SomTransformerClientIntf {

	
	public void introduceTransformation( );
	
	public void introduceTransformations( String filename );


	/**
	 * 
	 * @param colHeaders
	 * @param values ArrayList&lt;Double&gt; ,or as ArrayList&lt;String&gt; rawValues
	 */
	public void addObservations( ArrayList<String> colHeaders, ArrayList values);
		
	
	public void reFreshCalculation( );
	
	
}
