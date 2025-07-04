package com.uas.rondaapp.dao;

import com.uas.rondaapp.model.Jadwal;
import com.uas.rondaapp.util.CryptographyUtil;
import com.uas.rondaapp.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JadwalDAO {

    /**
     * Menyimpan jadwal ronda baru ke database.
     * @param tanggal Tanggal jadwal dalam format java.sql.Date.
     * @param userId ID dari user yang bertugas.
     * @param shift Shift tugas ('Malam 1' atau 'Malam 2').
     * @throws SQLException jika terjadi error SQL.
     */
    public void save(java.sql.Date tanggal, int userId, String shift) throws SQLException {
        String sql = "INSERT INTO jadwal_ronda (tanggal, user_id, shift) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, tanggal);
            pstmt.setInt(2, userId);
            pstmt.setString(3, shift);
            pstmt.executeUpdate();
        }
    }

    public List<Jadwal> findAll() {
        List<Jadwal> jadwalList = new ArrayList<>();
        String sql = "SELECT jr.id, jr.tanggal, u.nama_lengkap, jr.shift, jr.status " +
                     "FROM jadwal_ronda jr JOIN users u ON jr.user_id = u.id ORDER BY jr.tanggal DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                jadwalList.add(new Jadwal(
                    rs.getInt("id"),
                    rs.getDate("tanggal"),
                    rs.getString("nama_lengkap"),
                    rs.getString("shift"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jadwalList;
    }

    public void createReport(int jadwalId, byte[] encryptedReport) throws SQLException {
        String sql = "INSERT INTO laporan_ronda (jadwal_id, isi_laporan_encrypted) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jadwalId);
            pstmt.setBytes(2, encryptedReport);
            pstmt.executeUpdate();
        }
    }
    
    public void updateJadwalStatus(int jadwalId, String newStatus) {
        String sql = "UPDATE jadwal_ronda SET status = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, jadwalId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteJadwalById(int jadwalId) {
        Connection conn = DatabaseConnection.getConnection();
        String deleteReportsSql = "DELETE FROM laporan_ronda WHERE jadwal_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteReportsSql)) {
            pstmt.setInt(1, jadwalId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        
        String deleteSchedulesSql = "DELETE FROM jadwal_ronda WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSchedulesSql)) {
            pstmt.setInt(1, jadwalId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteSchedulesAndReportsByUserId(int userId) {
        Connection conn = DatabaseConnection.getConnection();
        String deleteReportsSql = "DELETE FROM laporan_ronda WHERE jadwal_id IN (SELECT id FROM jadwal_ronda WHERE user_id = ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteReportsSql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        
        String deleteSchedulesSql = "DELETE FROM jadwal_ronda WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSchedulesSql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getAllDecryptedReports() {
        StringBuilder allReports = new StringBuilder();
        String sql = "SELECT lr.waktu_laporan, u.nama_lengkap, jr.shift, lr.isi_laporan_encrypted FROM laporan_ronda lr JOIN jadwal_ronda jr ON lr.jadwal_id = jr.id JOIN users u ON jr.user_id = u.id ORDER BY lr.waktu_laporan DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
            while (rs.next()) {
                try {
                    String decryptedReport = CryptographyUtil.decrypt(rs.getBytes("isi_laporan_encrypted"));
                    allReports.append("----------------------------------------------------------\n");
                    allReports.append("Waktu Laporan: ").append(sdf.format(rs.getTimestamp("waktu_laporan"))).append("\n");
                    allReports.append("Petugas      : ").append(rs.getString("nama_lengkap")).append(" (").append(rs.getString("shift")).append(")\n");
                    allReports.append("Laporan      :\n").append(decryptedReport).append("\n\n");
                } catch (Exception e) {
                    allReports.append("--- GAGAL MEMBACA LAPORAN ---\n\n");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        if (allReports.length() == 0) { return "Belum ada laporan yang dibuat."; }
        return allReports.toString();
    }
}