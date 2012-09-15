package org.NooLab.somfluid.tasks;

import java.util.Observer;




public interface SomFluidSubTaskIntf {

	public String getGuid();

	void removeObserversAll();

	void removeObserver(Observer obs);
}
