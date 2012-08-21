package org.NooLab.field.fixed.components;


import org.NooLab.field.FieldIntf;
import org.NooLab.field.FieldParticlesIntf;




public interface FixedFieldParticlesIntf extends FieldParticlesIntf{

	int size();

	double getDensity();

	double getAverageDistance(FieldIntf field);

}
