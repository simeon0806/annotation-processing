package com.nevexis.connectionpool;

import java.util.Objects;

public class ConnectionPoolCreator {
	
	private static ConnectionPool connectionPoolIstance;

	
	public static ConnectionPool getInstance() {
		if (Objects.isNull(connectionPoolIstance)) {
			connectionPoolIstance = new ConnectionPoolImpl();
		}

		return connectionPoolIstance;
	}
}
