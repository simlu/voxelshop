package com.vitco.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    private static Connection conn = null;
    private static String dbHost = "174.37.227.98";
    private static String dbPort = "3306";
    private static String dbUser = "AmY";
    private static String dbPassword = "jLpbwjshdaMvYM2P";

    private MySQLConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
                    + dbPort + "?" + "user=" + dbUser + "&"
                    + "password=" + dbPassword);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found.");
        } catch (SQLException e) {
            System.out.println("Connection failed.");
        }

        // make sure connection is closed on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static Connection getInstance()
    {
        if(conn == null)
            new MySQLConnection();
        return conn;
    }
}
