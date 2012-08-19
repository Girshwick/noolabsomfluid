package org.NooLab.field.fixed;

import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.repulsive.intf.particles.ParticlesIntf;

public interface FixedFieldWintf {

	void registerEventMessaging(FixedNodeFieldEventsIntf eventSink);

	void setBorderMode(int borderAll);

	void init(int nbrParticles);

	void setSelectionSize(int _selectionsize);

	int getSelectionSize();

	void update();

	int getNumberOfParticles();

	void close();

	ParticlesIntf getParticles();

	String getSurround(int i, int j, boolean b);

}
