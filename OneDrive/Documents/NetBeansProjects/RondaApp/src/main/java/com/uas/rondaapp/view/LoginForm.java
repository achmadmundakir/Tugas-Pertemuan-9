package com.uas.rondaapp.view;

import com.uas.rondaapp.model.User;
import com.uas.rondaapp.util.AppSession;
import com.uas.rondaapp.util.CryptographyUtil;
import com.uas.rondaapp.util.DatabaseConnection;
import com.uas.rondaapp.util.SerializationUtil;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class LoginForm extends JFrame {

    private JLabel lblUsername, lblPassword;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginForm() {
        initComponents();
        setTitle("Login - Aplikasi Ronda");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void initComponents() {
        lblUsername = new JLabel("Nama Pengguna:");
        lblPassword = new JLabel("Kata Sandi:");
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        btnLogin = new JButton("Masuk");

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblUsername, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblPassword, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtPassword, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; panel.add(btnLogin, gbc);

        btnLogin.addActionListener((ActionEvent e) -> {
            loginActionPerformed();
        });

        this.add(panel);
    }

    private void loginActionPerformed() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hashedPassword = CryptographyUtil.hashPassword(password);
        
        // Query SQL diubah untuk mengambil kolom 'role' juga
        String sql = "SELECT id, nama_lengkap, role FROM users WHERE TRIM(username) = ? AND password_hash = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Membuat objek User dengan menyertakan 'role'
                    User loggedInUser = new User(
                        rs.getInt("id"),
                        username,
                        rs.getString("nama_lengkap"),
                        rs.getString("role") // <-- Mengambil role dari database
                    );
                    
                    try {
                        SerializationUtil.saveSession(new AppSession(loggedInUser));
                    } catch (IOException ex) {
                        System.err.println("Gagal menyimpan sesi: " + ex.getMessage());
                    }

                    JOptionPane.showMessageDialog(this, "Login Berhasil!");
                    
                    SwingUtilities.invokeLater(() -> new MainForm().setVisible(true));
                    this.dispose();
                    
                } else {
                    JOptionPane.showMessageDialog(this, "Username atau Password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}