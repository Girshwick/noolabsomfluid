package org.NooLab.field;

import java.util.Random;

import org.math.array.StatisticSample;




public interface FieldHostIntf {

	
	public StatisticSample getStatsSampler();

	public Random getRandom();
	
}
