/**
 * 
 */
package org.NooLab.somfluid.core.engines;

import java.io.Serializable;

import org.NooLab.somfluid.core.engines.det.ClassificationSettings;

 

/**
 * this class is for exporting the SOM as a map, e.g. to the display
 *
 */
public class SomProfiles implements Serializable{

	private static final long serialVersionUID = -1708886943517046217L;
	
	
	NodeVector[][] profilesTable;  // the map
	double[][] nodesize;       // the size of nodes
	String[] profileVariables; // column headers
	String[] profileLabels;    // the label of a node
	
	// possibly the results description, according to ClassficationSettings
	ClassificationSettings classficationSettings;
	
	
	// ========================================================================
	public SomProfiles( int rowcount, int colcount, int vectorSize ){
		
		profilesTable = new NodeVector[rowcount][colcount];
		
		for (int i=0;i<rowcount;i++){
			for (int k=0;k<colcount;k++){
				
				profilesTable[i][k].values = new double[vectorSize];
				
				for (int p=0;p<vectorSize;p++){
					profilesTable[i][k].values[p] = -1.0 ;
				} // p->
			} // k->
		} // i->
		
		profileVariables = new String[colcount];
		profileLabels = new String[rowcount];
		
		
	}
	// ========================================================================



	public NodeVector[][] getProfilesTable() {
		return profilesTable;
	}



	public void setProfilesTable(NodeVector[][] profilesTable) {
		this.profilesTable = profilesTable;
	}



	public double[][] getNodesize() {
		return nodesize;
	}



	public void setNodesize(double[][] nodesize) {
		this.nodesize = nodesize;
	}



	public String[] getProfileVariables() {
		return profileVariables;
	}



	public void setProfileVariables(String[] profileVariables) {
		this.profileVariables = profileVariables;
	}



	public String[] getProfileLabels() {
		return profileLabels;
	}



	public void setProfileLabels(String[] profileLabels) {
		this.profileLabels = profileLabels;
	}



	public ClassificationSettings getClassficationSettings() {
		return classficationSettings;
	}



	public void setClassficationSettings(
			ClassificationSettings classficationSettings) {
		this.classficationSettings = classficationSettings;
	}	
	
	
}

class NodeVector{
	
	public double[] values;
	
}

