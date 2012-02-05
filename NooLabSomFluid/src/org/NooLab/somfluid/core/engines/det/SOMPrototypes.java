package org.NooLab.somfluid.core.engines.det;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataRecord;


 

public class SOMPrototypes{
	SomDataObject SOMdata;
	
	private DataRecord[] Prototypes;
	private double[] prototype_values; //see setPrototpeValues(float[] val)
	
	public int typologysize;
	public double[][] prototypicalProfiles=null;
	
	private Vector Profiles;
	
	public SOMPrototypes(){
		//set up****
		Vector Profiles = new Vector(1);
	
	}	

	public void setPrototypeValues(double[] exampleProfile){ 
		//situation 1: deal with data records without predefined prototypes, 
		//             the prototype values is the outcome of a record(exclude in training data)
        //             z.B.  [0,0.25] for the credit data
		//situation 2: deal with predefined prototype data, the prototype value is ID of the prototype data
		//             z.B.  [0,1,2,3,4] as define 5 prototypes with ID of 0,1,2,3,4;
	/*	
		double[] profile_values = null;
		
		// the value of a single prototype
		
		 
		                          
		System.arraycopy(exampleProfile,0,profile_values,0,exampleProfile.length);
		
		
		
		typologysize=typologysize+1;
		Profiles.add( prototype_values );
		int p = prototype_values.length;
	*/
	}
	
	
	public void definePrototypeProfile(int i,double[] val){
		if(i<0||i>=typologysize){
			System.out.println("i<0||i>=typologysize");
			return;
		}
		Prototypes[i]=new DataRecord(i,SOMdata.getRecordSize(),val);
	}
	
	
	public double[] getPrototypeValues(){
		return prototype_values;
	}
	
	
}