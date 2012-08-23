package org.NooLab.somfluid.env.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;


import java.sql.*;
import org.h2.*;
import java.io.* ;

import java.util.List;


// LOC 160

public class MetaData {
	/*
	 
    1.  get all data tables in database
    	
 
 	2.	get all columns in a selected table
  	 	SELECT COLUMN_NAME   FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME ='KDATA';
  	 	
  	3.  get data type for any field
		SELECT DATA_TYPE   FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME ='KDATA' AND COLUMN_NAME ='NAME';
  
  	4.  get index field (property for each field)
  		SELECT COLUMN_NAME  FROM INFORMATION_SCHEMA.INDEXES where TABLE_NAME ='KDATA'
  		
	*/
	
	ArrayList<DocoDbTable> tables = new ArrayList<DocoDbTable>() ;
	
	Connection connection;
	String dbName ;
	
	
	public MetaData(Connection conn, String db_name){
		connection = conn;
		dbName = db_name ;	
		
		retrieveMetaData() ;
		
	}
	
	
	public void retrieveMetaData(){
		
		Long recCount=-1L;
		getListofTables();
		
		for (int i=0;i<tables.size();i++){
			DocoDbTable table = tables.get(i) ;
			String tn = table.name;
			
		}
		
		
	}
	
	
	/*
	 * 
	          statement = connection.createStatement();  
              resultSet = statement.executeQuery("SELECT EMPNAME FROM EMPLOYEEDETAILS");  
              while (resultSet.next()) {  
                  System.out.println("EMPLOYEE NAME:" + resultSet.getString("EMPNAME"));  
              } 


	 */
	
	protected void getListofTables(){
		
		String sql, tn;
		ResultSet resultset;
		RowId rowid;
		Array data;
		
		DocoDbTable table;
		
		sql = "SELECT  TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE ='TABLE' ; " ;
		
		tables.clear() ;
		
		try{
			PreparedStatement prep = connection.prepareStatement( sql );
			resultset = prep.executeQuery();
			
			while (resultset.next()){
				
				// rowid = resultset.getRowId(1);
				  data = resultset.getArray("TABLE_NAME");
				
				tn = resultset.getString("TABLE_NAME") ;
				
				
				if ( (tn.length()>0) && (tn.toLowerCase().contentEquals(dbName.toLowerCase())==false)){
					table = new DocoDbTable(connection, tn);
					tables.add(table) ;
				}
			} // still results ?
			
			resultset.close();
			
			/*
			Statement statement = connection.createStatement();  
            resultset = statement.executeQuery("SELECT EMPNAME FROM EMPLOYEEDETAILS");  
            while (resultset.next()) {  
                System.out.println(resultset.getString("TABLE_NAME"));  
            } 		
            */
            
			
		}catch(SQLException sx){
			sx.printStackTrace();
		}
	}
	
	
	public String[] getColumnNames(String tablename){
		String[] fields=null;
		DocoDbTable table;
		
		table = getbyName(tablename);
		
		if (table!=null){
			fields = table.getColumnNames();
		}
		
		return fields;
	}
	

	public ArrayList<DocoDbTable> getTables(){  // 
		 
		
		DocoDbTable table=null;
		String tn ;
		for (int i=0;i<tables.size();i++){
			table = tables.get(i); 
			tn = table.name ;
			if (tn!=null ){
				tables.add(table) ;
			}
		}
		return tables;
		
	}


	public ArrayList<String> getTableNames( int mode){
		
		 
		ArrayList<String> tableNames = new ArrayList<String>();
		
		DocoDbTable table=null;
		String tn ;
		
		
		
		for (int i=0;i<tables.size();i++){
			table = tables.get(i); 
			tn = table.name ;
			if (tn!=null ){
				// acc. to mode, 1=only non-system, 3=only system tables, or 5=all
				if (mode==1){
					
					tableNames.add(tn) ;
				}
			}
		}
		return tableNames;
		
	}

	public DocoDbTable getbyName(String tablename){
	 
		String tn;
		DocoDbTable table=null;
		
		if ((tablename==null) || (tablename.length()==0)){
			return table;
		}
		
		for (int i=0;i<tables.size();i++){
			table = tables.get(i); 
			tn = table.name ;
			if ((tn!=null) && (tn.toLowerCase().contentEquals(tablename))){
				break;
			}
		}
		return table;
	}

	public DBColumn getbyNames(String tablename, String colname){
		 
		String tn;
		DocoDbTable table=null;
		DBColumn col=null;
		
		for (int i=0;i<tables.size();i++){
			table = tables.get(i); 
			tn = table.name ;
			if (tn.toLowerCase().contentEquals(tablename.toLowerCase())){
				col = table.getbyName(colname); 
				break;
			}
		}
		return col;
	} 

} // end of class MetaData


 
