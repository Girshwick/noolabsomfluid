package org.NooLab.field;

import java.util.ArrayList;

import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.SelectionConstraints;
import org.NooLab.utilities.datatypes.IndexDistance;

public interface FieldGridSubstrateIntf {


	public final static int _STRING  = 1; // like in a violine, or in a model of atomic bonds ("bar-like electron clouds")
	public final static int _RECT    = 3;
	public final static int _CIRCLE  = 4;
	public final static int _ELLIPSE = 9; // will be a subset of circle, i.e. it is inscribed
	
	
	// some topological properties of the area regarding the border  
	public static final int __BORDER_ALL  = 1; // rectangle
	public static final int __BORDER_NONE = 2; // torus
	public static final int __BORDER_OPEN_L = 5; // 
	public static final int __BORDER_OPEN_R = 6; // 
	public static final int __BORDER_OPEN_T = 7; // 
	public static final int __BORDER_OPEN_D = 8; //
	public static final int __BORDER_OPEN_ZA = 15; //
	public static final int __BORDER_OPEN_ZB = 16; //
	public static final int __BORDER_OPEN_Z  = 17; //
	
	public static final int __BORDER_LR_ONLY = 20; // 
	public static final int __BORDER_SN_ONLY = 21; // SN = south-north 
	public static final int __BORDER_EW_ONLY = 22; // 
	public static final int __BORDER_ZA_ONlY = 23; // 
	public static final int __BORDER_ZB_ONLY = 24; // 
	
	public static final int __BORDER_SE_ONLY = 21; //
	public static final int __BORDER_NW_ONLY = 21; //
	
	
	// ....................................................
	
	public int getIndexNear(double x, double y);

	public FieldIntf getField();
	
	public SelectionConstraints getSelectionConstraints();

	public double getRadiusCorrectionFactor();

	public int[] extractIndexesFromIndexedDistances(ArrayList<IndexDistance> indexedDistances);


	public void setSelectionConstraints(SelectionConstraints selectionConstraints);

	public double getResolution();

	public void setRadiusCorrectionFactor(double d);

	public void setResolution(double d);

	
	
}
