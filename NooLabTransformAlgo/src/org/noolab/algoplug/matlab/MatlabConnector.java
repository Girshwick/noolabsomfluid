package org.noolab.algoplug.matlab;

//  com.Mathworks.jmi




public class MatlabConnector {

	Object matLab = null;

	// Matlab

	public void connect() {
		try {
			
			// matLab = new Matlab();
			
		} catch (Exception i) {
			// "NO MATLAB CONNECTION AVAILABLE","Info",1);
		}

		String command = "x = 'Hallo MATLAB'";

		try {
			
			// matlab.eval(command);
			
		// } catch (MatlabException ex) {
		} catch (Exception ex) {
			// ex.toString(), "Error", 1);
		}
	}

}
