package com.leadspotting.emailSender;

import java.sql.*;

/************************************************************/
/* Class: SniperDB                                           */
/* purpose: constructure to the class                        */
/* connects to the database                                 */
/************************************************************/
public class SniperDB {
	static public boolean isConnected = false;
	static private final String dbMaster = "jdbc:mysql://db2.coovtny31m6n.us-east-2.rds.amazonaws.com:3306/";
	static private final String user = "api";
	static private final String passwd = "1q2w3e4R";
	private static Connection connectionMaster;
	private static Connection connectionSlave;
	private static String currentDB = "leadspot_main";
	private static String encoding = "?characterEncoding=utf8&character_set_server=utf8mb4&useUnicode=true&autoReconnect=true&socketTimeout=500000&autoReconnectForPools=true&enableQueryTimeouts=false&tcpKeepAlive=true&sendStringParametersAsUnicode=false&allowMultiQueries=true";

	/************************************************************/
	/* function: VideoCoreDb */
	/* purpose: contructure to the class */
	/* connects to the databse */
	/************************************************************/

	/**
	 * function: ExaminatorDb purpose: constructur to the class connects to the
	 * databse
	 */
	public SniperDB() {
		connectToDB();
	}

	/**
	 * function: connectToDB connects to the databse
	 * 
	 * @return
	 */
	public static boolean connectToDB() {
		try {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
			} catch (Exception e) {
			}
			if (connectionMaster == null || connectionMaster.isClosed()) {
				System.out.println("connecting...");
				connectionMaster = DriverManager.getConnection(dbMaster + currentDB + encoding, user, passwd);
				isConnected = true;
				System.out.println("connected to Master DB");
			}
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage() + " Error two in connecting to DB");
			return false;
		}
	}

	public static void verifyOpenDBConnection() {
		try {
			pingServer();
			if (connectionMaster == null || connectionMaster.isClosed()) {
				connectToDB();
				System.out.println("connected to Master DB again");
			} else {
			}

		} catch (Exception e1) {
			e1.printStackTrace();

		}

	}

	private static void pingServer() {
		try {
			Statement s = connectionMaster.createStatement();
			s.execute("/* ping */ SELECT 1");
		} catch (Exception e1) {
			try {
				System.out.println("Ping Failed - sleeping");
				Thread.sleep(1000);
			} catch (Exception e2) {

			}

		}
	}

	/**
	 *
	 * @return
	 */
	public static Connection getMasterConnection() {
		return connectionMaster;
	}

	public static Connection getConnectionFromPool() {
		return connectionMaster;

	}

	public static Connection getslaveConnection() {
		return connectionSlave;
	}

	public static void closeConnection() {

		try {
			if (connectionMaster != null)
				connectionMaster.close();
			connectionMaster = null;
			System.out.println("connection is closed");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}