package org.NooLab.somfluid.env.data.db;

public class XColumn {

	
	public String table;
	
	public String domain;
	
	public String id;
	public String name;
	public String quality;
	public String sema;

	// ------------------------------------------------------------------------	
	public XColumn(){
		
	}
	// ------------------------------------------------------------------------

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getSema() {
		return sema;
	}

	public void setSema(String sema) {
		this.sema = sema;
	}
	
	
}
