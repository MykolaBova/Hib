package com.tinyorm;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBSQlite {
    public static final Connection c;
    private static Connection _c;
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            _c = DriverManager.getConnection("jdbc:sqlite:test.sqlite"); // TODO config
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        c = _c;
    }
}
