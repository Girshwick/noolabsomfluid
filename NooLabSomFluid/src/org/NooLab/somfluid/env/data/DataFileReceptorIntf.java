package org.NooLab.somfluid.env.data;

public interface DataFileReceptorIntf {

	void loadFromFile(String filename) throws Exception;

	String getLoadedFileName();

}
