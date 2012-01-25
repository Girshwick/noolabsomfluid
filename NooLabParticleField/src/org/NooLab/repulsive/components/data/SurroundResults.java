package org.NooLab.repulsive.components.data;

public class SurroundResults {

	String guid="";
	  
	RetrievalParamSet paramSet;
	
	double[] coordinate = new double[2] ;
	int[] particleIndexes = new int[0] ;
	double[] particleDistances = new double[0];

	public int particleIndex = -1;

	public long timeflag;

	// -------------------------------------------
	

	public double[] getCoordinate() {
		return coordinate;
	}
	public void setCoordinate(double[] coordinate) {
		this.coordinate = coordinate;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public void setParamSet(RetrievalParamSet paramSet) {
		this.paramSet = paramSet;
	}
	public RetrievalParamSet getParamSet() {
		return paramSet;
	}
	public int[] getParticleIndexes() {
		if (particleIndexes==null){
			particleIndexes = new int[0];
		}
		return particleIndexes;
	}
	
	public void setParticleIndexes(int[] particleIndexes) {
		this.particleIndexes = particleIndexes;
	}
	
	public double[] getParticleDistances() {
		
		if (particleDistances==null){
			particleDistances = new double[0];
		}

		return particleDistances;
	}
	
	public void setParticleDistances(double[] particleDistances) {
		this.particleDistances = particleDistances;
	}
	public int getParticleIndex() {
		return particleIndex;
	}
	public long getTimeflag() {
		return timeflag;
	}
	
}
