package com.nevexis.connectionpool;

import java.sql.Connection;

public interface ConnectionPool {
	
	public Connection getConnection();
	
	public void releaseConection(Connection con);

}
