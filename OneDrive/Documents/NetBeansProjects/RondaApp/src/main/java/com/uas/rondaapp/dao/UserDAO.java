package com.uas.rondaapp.dao;

import com.uas.rondaapp.model.User;
import com.uas.rondaapp.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) untuk mengelola semua operasi
 * yang berhubungan dengan tabel 'users' di database.
 */
public class UserDAO {

    /**
     * Mengambil semua pengguna yang memiliki peran 'warga'.
     * @return List dari objek User.
     */
    public List<User> findAllWarga() {
        List<User> userList = new ArrayList<>();
        // Query ini hanya mengambil pengguna dengan peran 'warga'
        String sql = "SELECT * FROM users WHERE role = 'warga' ORDER BY nama_lengkap ASC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                userList.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("nama_lengkap"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Menyimpan pengguna baru ke database dengan peran 'warga'.
     * @param username Username baru.
     * @param namaLengkap Nama lengkap pengguna.
     * @param hashedPassword Password yang sudah di-hash.
     * @throws SQLException Jika terjadi error SQL.
     */
    public void saveWarga(String username, String namaLengkap, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, nama_lengkap, role) VALUES (?, ?, ?, 'warga')";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, namaLengkap);
            pstmt.executeUpdate();
        }
    }

    /**
     * Menghapus pengguna dari database berdasarkan ID mereka.
     * @param id ID pengguna yang akan dihapus.
     */
    public void delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mengecek apakah sebuah username sudah ada di database.
     * @param username Username yang akan dicek.
     * @return true jika username sudah ada, false jika belum.
     */
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}