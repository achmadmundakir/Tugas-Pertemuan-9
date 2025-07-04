package com.uas.rondaapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Kelas ini bertanggung jawab untuk membuat dan mengelola satu koneksi ke database.
 * Ini adalah satu-satunya tempat di mana detail koneksi (URL, user, password) didefinisikan.
 */
public class DatabaseConnection {
    
    private static Connection connection;

    // Alamat ke server database MySQL dan nama database yang akan digunakan.
    private static final String URL = "jdbc:mysql://localhost:3306/db_ronda";
    
    // Username untuk login ke server database. 'root' adalah default untuk XAMPP.
    private static final String USER = "root";
    
    // Password untuk login ke server database. Kosong ("") adalah default untuk XAMPP.
    private static final String PASSWORD = "";

    /**
     * Method ini akan memberikan koneksi ke database.
     * Jika koneksi belum pernah dibuat, ia akan membuatnya terlebih dahulu.
     * Jika sudah ada, ia akan menggunakan koneksi yang sama (pola Singleton).
     * @return Objek Connection yang aktif ke database.
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Mendaftarkan driver JDBC MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Membuat koneksi menggunakan URL, USER, dan PASSWORD
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                
            } catch (ClassNotFoundException | SQLException e) {
                // Jika koneksi gagal, tampilkan pesan error dan hentikan program.
                JOptionPane.showMessageDialog(null, "Koneksi ke database gagal: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace(); // Tampilkan detail error di console untuk debugging.
                System.exit(0); // Hentikan aplikasi karena tidak bisa berfungsi tanpa database.
            }
        }
        return connection;
    }
}