package org.NooLab.repulsive.components.data;

import java.io.Serializable;
import java.util.ArrayList;

import math.geom2d.conic.Ellipse2D;
import math.geom2d.line.Line2D;
import math.geom2d.polygon.Polygon2D;

import org.NooLab.repulsive.components.topology.ResultObjekt;
import org.NooLab.utilities.ArrUtilities;


public class SurroundResults implements Cloneable,Serializable{

	private static final long serialVersionUID = -2479510886006188766L;

	/** a guid that has been created by the */
	String guid="";
	  
	//
	long contextKey = -1;
	
	RetrievalParamSet paramSet;
	
	double[] coordinate = new double[2] ;
	int[] particleIndexes = new int[0] ;
	double[] particleDistances = new double[0];

	ArrayList<IndexDistance> indexedDistances = new ArrayList<IndexDistance>();
	
	public int particleIndex = -1;

	public long timeflag;

	transient public ArrUtilities arrutil;

	 

	public ArrayList<ResultObjekt> objects = new ArrayList<ResultObjekt>() ;

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
	public ArrayList<IndexDistance> getIndexedDistances() {
		return indexedDistances;
	}
	public void setIndexedDistances(ArrayList<IndexDistance> indexedDistances) {
		this.indexedDistances = indexedDistances;
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
	
	
	public void exportEllipses(ArrayList<Ellipse2D> ellipses) {
		ResultObjekt obj;
		for (int i=0;i<objects.size();i++){
			obj = objects.get(i) ;
			
			if ((obj!=null) && (obj.getDescriptor().contentEquals("E"))){
				ellipses.add( (Ellipse2D)obj.getObj() );
				objects.set(i, null) ;
			}
		}
		
	}

	public void exportLines(ArrayList<Line2D> lines) {
		ResultObjekt obj;
		for (int i=0;i<objects.size();i++){
			obj = objects.get(i) ;
			
			if ((obj!=null) && (obj.getDescriptor().contentEquals("L"))){
				lines.add( (Line2D)obj.getObj() );
				objects.set(i, null) ;
			}
		}
		
	}
	public void exportPolygons(ArrayList<Polygon2D> polys) {
		/*
		if ((obj!=null) && (obj.getDescriptor().contentEquals("L"))){
			lines.add( (Line2D)obj.getObj() );
			objects.set(i, null) ;
		}
		*/
	}
	
	// ========================================================================

	
	public ArrayList<IndexDistance> getParticlesAsIndexedDistances() {
		
		return indexedDistances;
	}
	public long getContextKey() {
		return contextKey;
	}
	public void setContextKey(long contextKey) {
		this.contextKey = contextKey;
	}
	public ArrayList<ResultObjekt> getObjects() {
		return objects;
	}
	public void setObjects(ArrayList<ResultObjekt> objects) {
		this.objects = objects;
	}
	public void setParticleIndex(int particleIndex) {
		this.particleIndex = particleIndex;
	}
	public void setTimeflag(long timeflag) {
		this.timeflag = timeflag;
	}
	
	
	

}
