package com.nevexis.connectionpool;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Stack;

public class ConnectionPoolImpl implements ConnectionPool {

	private static final int CONNECTION_NUM = 10;
	private Stack<Connection> connectionPool = new Stack<>();

	ConnectionPoolImpl(){

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < CONNECTION_NUM; i++) {
			connectionPool.add(createProxyedConnection());
		}

	}

	private Connection createProxyedConnection() {
		try {
			Connection conn = DriverManager.getConnection(ConnectionData.URL, ConnectionData.USER_NAME,
					ConnectionData.PASSWORD);

			return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
					new Class[] { Connection.class }, (proxyy, method, args) -> {
						if ("close".equals(method.getName())) {
							this.releaseConection(conn);
							return null;
						}
						return method.invoke(conn, args);
					});
		} catch (SQLException e) {
			System.out.println("Error creating connections! ->  " + e);
			return null;
		}
	}

	@Override
	public synchronized Connection getConnection() {
		return connectionPool.pop();
	}

	@Override
	public synchronized void releaseConection(Connection con) {
		connectionPool.push(con);
	}

}
