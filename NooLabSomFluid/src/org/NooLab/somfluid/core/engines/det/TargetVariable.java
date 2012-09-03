package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

import org.NooLab.somfluid.core.engines.det.results.FrequencyList;
import org.NooLab.somfluid.core.engines.det.results.FrequencyListGeneratorIntf;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somtransform.algo.AdaptiveDiscretization;



public class TargetVariable {

	public int tableindex;
	public int dataindex;
	public float TVnum = -1;
	public String TVlabel;
	public int type; // 1=float, 2=int,bin 3=string :: type of TV in raw data

	// ------------------------------------------------------------------------
	public TargetVariable() {

	}
	// ------------------------------------------------------------------------

	
}






