package com.kasir.koneksiDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Nanda Fauzi
 */
public class Koneksi {

    private static final String URL  = "jdbc:mysql://localhost:3306/db_KasirToko";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getKoneksi() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        System.out.println("Koneksi Berhasil");
        return conn;
    }
}
