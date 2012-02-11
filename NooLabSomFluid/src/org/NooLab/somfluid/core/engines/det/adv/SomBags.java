package org.NooLab.somfluid.core.engines.det.adv;

/**
 *
 *  SomBags are collections of Som models that have been created on different
 *  samples of the data, where
 *  - samples may overlap,
 *  - samples are much smaller than original data size 
 *
 *  The idea is, instead to use all data for a single model, to split
 *  the data into several pieces and to build a model for each portion.
 *  
 *  This projects into a natural extension into a system that  
 *  - is continuously learning, just by adding a new SOM based on the new data
 *  - is able to abstract, since the SomBag is built from a SOM that evaluates
 *    the classification (and the deviation from the targets) instead of the data
 *
 *  SomBags will(shaould be positioned within SomFactory 
 *
 */
public class SomBags {

	public SomBags(){
		
	}
}
