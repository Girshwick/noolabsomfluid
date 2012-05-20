package org.NooLab.utilities.files;

public class PathFinder {

	public String getAppBinPath() {
		return getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
	}

}
