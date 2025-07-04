package com.uas.rondaapp.view;

import com.uas.rondaapp.dao.JadwalDAO;
import com.uas.rondaapp.dao.UserDAO;
import com.uas.rondaapp.model.Jadwal;
import com.uas.rondaapp.model.User;
import com.uas.rondaapp.util.AppSession;
import com.uas.rondaapp.util.CryptographyUtil;
import com.uas.rondaapp.util.SerializationUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MainForm extends JFrame {
    
    // Variabel data
    private User currentUser;
    private ResourceBundle bundle;
    private JadwalDAO jadwalDAO;
    private UserDAO userDAO;
    private List<Jadwal> jadwalCache;
    private List<User> userCache;

    // Komponen GUI
    private JLabel lblWelcome, lblClock, lblClockValue;
    private JMenuBar menuBar;
    private JMenu menuFile, menuLanguage;
    private JMenuItem menuItemLogout, menuItemExit, menuItemIndonesia, menuItemEnglish;
    private JTabbedPane tabbedPane;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JButton btnCreateReport, btnLogout, btnChangeStatus, btnDeleteSchedule, btnAddSchedule;
    private JTextArea reportTextArea;
    
    // Komponen panel admin
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JButton btnAddUser, btnDeleteUser;

    public MainForm() {
        this.jadwalDAO = new JadwalDAO();
        this.userDAO = new UserDAO();
        loadSession();       
        initComponents();    
        startClockThread();  
        loadLanguage("in", "ID"); 
        loadScheduleData();
        loadReportData(); 
        setupUIForRole(); 
    }
    
    private void initComponents() {
        setTitle("Aplikasi Ronda"); setSize(800, 600); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { handleExit(); } });
        
        menuBar = new JMenuBar(); menuFile = new JMenu(); menuLanguage = new JMenu(); menuItemIndonesia = new JMenuItem();
        menuItemEnglish = new JMenuItem(); menuItemLogout = new JMenuItem(); menuItemExit = new JMenuItem();
        menuLanguage.add(menuItemIndonesia); menuLanguage.add(menuItemEnglish);
        menuFile.add(menuLanguage); menuFile.add(new JSeparator()); menuFile.add(menuItemLogout); menuFile.add(menuItemExit);
        menuBar.add(menuFile); setJMenuBar(menuBar);
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 10)); topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        lblWelcome = new JLabel(); lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); lblClock = new JLabel(); lblClockValue = new JLabel("00:00:00");
        lblClockValue.setFont(new Font("Monospaced", Font.BOLD, 16)); clockPanel.add(lblClock); clockPanel.add(lblClockValue);
        topPanel.add(lblWelcome, BorderLayout.WEST); topPanel.add(clockPanel, BorderLayout.EAST);
        
        tabbedPane = new JTabbedPane();
        
        JPanel schedulePanel = new JPanel(new BorderLayout(10,10)); schedulePanel.setBorder(new EmptyBorder(10,10,10,10));
        tableModel = new DefaultTableModel(); scheduleTable = new JTable(tableModel);
        schedulePanel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER); 
        JPanel scheduleButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); 
        btnAddSchedule = new JButton("Tambah Jadwal");
        btnDeleteSchedule = new JButton("Hapus Jadwal");
        btnChangeStatus = new JButton("Ubah Status");
        btnCreateReport = new JButton(); 
        btnLogout = new JButton();
        scheduleButtonPanel.add(btnAddSchedule);
        scheduleButtonPanel.add(btnDeleteSchedule);
        scheduleButtonPanel.add(btnChangeStatus);
        scheduleButtonPanel.add(btnCreateReport); 
        scheduleButtonPanel.add(btnLogout); 
        schedulePanel.add(scheduleButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Jadwal Ronda", schedulePanel);
        
        JPanel reportPanel = new JPanel(new BorderLayout()); reportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        reportTextArea = new JTextArea(); reportTextArea.setEditable(false); reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportTextArea.setLineWrap(true); reportTextArea.setWrapStyleWord(true);
        reportPanel.add(new JScrollPane(reportTextArea), BorderLayout.CENTER);
        tabbedPane.addTab("Laporan Ronda", reportPanel);
        
        JPanel userManagementPanel = new JPanel(new BorderLayout(10,10)); userManagementPanel.setBorder(new EmptyBorder(10,10,10,10));
        userTableModel = new DefaultTableModel(); userTable = new JTable(userTableModel);
        userManagementPanel.add(new JScrollPane(userTable), BorderLayout.CENTER); 
        JPanel userButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnAddUser = new JButton("Tambah Warga"); btnDeleteUser = new JButton("Hapus Warga");
        userButtonPanel.add(btnAddUser); userButtonPanel.add(btnDeleteUser);
        userManagementPanel.add(userButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Kelola Warga", userManagementPanel);
        
        setLayout(new BorderLayout()); add(topPanel, BorderLayout.NORTH); add(tabbedPane, BorderLayout.CENTER);
        
        menuItemLogout.addActionListener(e -> handleLogout()); menuItemExit.addActionListener(e -> handleExit());
        menuItemIndonesia.addActionListener(e -> loadLanguage("in", "ID")); menuItemEnglish.addActionListener(e -> loadLanguage("en", "US"));
        btnCreateReport.addActionListener(e -> handleCreateReport()); btnLogout.addActionListener(e -> handleLogout());
        btnAddUser.addActionListener(e -> handleAddUser()); btnDeleteUser.addActionListener(e -> handleDeleteUser());
        btnChangeStatus.addActionListener(e -> handleChangeStatus()); btnDeleteSchedule.addActionListener(e -> handleDeleteSchedule());
        btnAddSchedule.addActionListener(e -> handleAddSchedule());
    }
    
    private void setupUIForRole() {
        if ("admin".equals(currentUser.getRole())) {
            btnChangeStatus.setVisible(false);
            btnDeleteSchedule.setVisible(true);
            btnAddSchedule.setVisible(true);
            loadUserData();
        } else { 
            int userManagementTabIndex = tabbedPane.indexOfTab("Kelola Warga");
            if (userManagementTabIndex != -1) { tabbedPane.remove(userManagementTabIndex); }
            btnChangeStatus.setVisible(true);
            btnDeleteSchedule.setVisible(false);
            btnAddSchedule.setVisible(false);
        }
    }
    
    private void handleAddSchedule() {
        if (userCache == null || userCache.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data warga belum dimuat atau kosong.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<User> wargaComboBox = new JComboBox<>(userCache.toArray(new User[0]));
        wargaComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getNamaLengkap()));

        JTextField tanggalField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
        String[] shiftOptions = {"Malam 1", "Malam 2"};
        JComboBox<String> shiftComboBox = new JComboBox<>(shiftOptions);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Pilih Warga:")); panel.add(wargaComboBox);
        panel.add(new JLabel("Tanggal (YYYY-MM-DD):")); panel.add(tanggalField);
        panel.add(new JLabel("Pilih Shift:")); panel.add(shiftComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Jadwal Ronda Baru", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                User selectedUser = (User) wargaComboBox.getSelectedItem();
                int userId = selectedUser.getId();
                java.util.Date utilDate = new SimpleDateFormat("yyyy-MM-dd").parse(tanggalField.getText());
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                String shift = (String) shiftComboBox.getSelectedItem();

                jadwalDAO.save(sqlDate, userId, shift);
                JOptionPane.showMessageDialog(this, "Jadwal baru berhasil ditambahkan!");
                loadScheduleData();
            } catch (java.text.ParseException e) {
                JOptionPane.showMessageDialog(this, "Format tanggal salah! Gunakan format YYYY-MM-DD.", "Error Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan jadwal ke database: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // ... (sisa method dari versi final sebelumnya tetap sama dan sudah benar)
    private void loadSession() { try { AppSession session = (AppSession) SerializationUtil.loadSession(); if (session == null) { JOptionPane.showMessageDialog(null, "Sesi tidak ditemukan, silakan login kembali.", "Error", JOptionPane.ERROR_MESSAGE); System.exit(0); } this.currentUser = session.getLoggedInUser(); } catch (Exception e) { JOptionPane.showMessageDialog(null, "Gagal memuat sesi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); System.exit(0); } }
    private void startClockThread() { Thread clockThread = new Thread(() -> { try { while (true) { SwingUtilities.invokeLater(() -> { lblClockValue.setText(new SimpleDateFormat("HH:mm:ss").format(new Date())); }); Thread.sleep(1000); } } catch (InterruptedException e) { } }); clockThread.setDaemon(true); clockThread.start(); }
    private void loadLanguage(String lang, String country) { try { String fileName = "messages_" + lang + "_" + country.toUpperCase() + ".properties"; InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName); if (inputStream == null) { throw new RuntimeException("File resource tidak ditemukan di classpath: " + fileName); } final Properties props = new Properties(); props.load(inputStream); bundle = new ResourceBundle() { @Override protected Object handleGetObject(String key) { return props.getProperty(key); } @Override public Enumeration<String> getKeys() { return (Enumeration<String>) props.propertyNames(); } }; updateTexts(); } catch (Exception e) { JOptionPane.showMessageDialog(this, "Gagal memuat file bahasa: " + e.getMessage(), "Error Kritis", JOptionPane.ERROR_MESSAGE); e.printStackTrace(); System.exit(1); } }
    private void updateTexts() { setTitle(bundle.getString("app.title")); menuFile.setText(bundle.getString("main.menu.file")); menuLanguage.setText(bundle.getString("main.menu.language")); menuItemIndonesia.setText(bundle.getString("label.language.indonesian")); menuItemEnglish.setText(bundle.getString("label.language.english")); menuItemLogout.setText(bundle.getString("main.menu.logout")); menuItemExit.setText(bundle.getString("main.menu.exit")); lblWelcome.setText(bundle.getString("main.welcome") + " " + currentUser.getNamaLengkap()); lblClock.setText(bundle.getString("main.clock.label")); btnCreateReport.setText(bundle.getString("button.create.report")); btnLogout.setText(bundle.getString("main.menu.logout")); tabbedPane.setTitleAt(0, bundle.getString("main.tab.schedule")); tabbedPane.setTitleAt(1, bundle.getString("main.tab.report")); if (tabbedPane.getTabCount() > 2) { tabbedPane.setTitleAt(2, "Kelola Warga"); } tableModel.setColumnIdentifiers(new String[]{ bundle.getString("main.schedule.table.header.date"), bundle.getString("main.schedule.table.header.name"), bundle.getString("main.schedule.table.header.shift"), bundle.getString("main.schedule.table.header.status") }); }
    private void loadScheduleData() { this.jadwalCache = jadwalDAO.findAll(); tableModel.setRowCount(0); SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy"); for (Jadwal jadwal : this.jadwalCache) { tableModel.addRow(new Object[]{ sdf.format(jadwal.getTanggal()), jadwal.getNamaPetugas(), jadwal.getShift(), jadwal.getStatus() }); } }
    private void loadReportData() { String allReports = jadwalDAO.getAllDecryptedReports(); reportTextArea.setText(allReports); reportTextArea.setCaretPosition(0); }
    private void loadUserData() { this.userCache = userDAO.findAllWarga(); userTableModel.setColumnIdentifiers(new String[]{"ID", "Username", "Nama Lengkap", "Role"}); userTableModel.setRowCount(0); for (User user : this.userCache) { userTableModel.addRow(new Object[]{ user.getId(), user.getUsername(), user.getNamaLengkap(), user.getRole() }); } }
    private void handleDeleteSchedule() { int selectedRow = scheduleTable.getSelectedRow(); if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin dihapus.", "Peringatan", JOptionPane.WARNING_MESSAGE); return; } Jadwal selectedJadwal = this.jadwalCache.get(selectedRow); int choice = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus jadwal untuk " + selectedJadwal.getNamaPetugas() + " pada tanggal ini?", "Konfirmasi Hapus Jadwal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); if (choice == JOptionPane.YES_OPTION) { jadwalDAO.deleteJadwalById(selectedJadwal.getId()); JOptionPane.showMessageDialog(this, "Jadwal berhasil dihapus."); loadScheduleData(); } }
    private void handleChangeStatus() { int selectedRow = scheduleTable.getSelectedRow(); if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih jadwal yang statusnya ingin diubah.", "Peringatan", JOptionPane.WARNING_MESSAGE); return; } Jadwal selectedJadwal = this.jadwalCache.get(selectedRow); if ("warga".equals(currentUser.getRole()) && !selectedJadwal.getNamaPetugas().equals(currentUser.getNamaLengkap())) { JOptionPane.showMessageDialog(this, "Anda hanya bisa mengubah status untuk jadwal Anda sendiri.", "Akses Ditolak", JOptionPane.ERROR_MESSAGE); return; } String[] options = { "Sudah Dilaksanakan", "Belum Dilaksanakan", "Izin (Tidak Hadir)" }; String currentStatus = selectedJadwal.getStatus(); Object newStatusObject = JOptionPane.showInputDialog(this, "Pilih status baru untuk jadwal " + selectedJadwal.getNamaPetugas() + ":", "Ubah Status Jadwal", JOptionPane.QUESTION_MESSAGE, null, options, currentStatus); if (newStatusObject != null) { String newStatus = newStatusObject.toString(); if (!newStatus.equals(currentStatus)) { jadwalDAO.updateJadwalStatus(selectedJadwal.getId(), newStatus); JOptionPane.showMessageDialog(this, "Status berhasil diubah menjadi '" + newStatus + "'."); loadScheduleData(); try { scheduleTable.setRowSelectionInterval(selectedRow, selectedRow); } catch (IllegalArgumentException e) {} } } }
    private void handleAddUser() { JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5)); JTextField usernameField = new JTextField(15); JTextField namaLengkapField = new JTextField(15); JPasswordField passwordField = new JPasswordField(15); panel.add(new JLabel("Username:")); panel.add(usernameField); panel.add(new JLabel("Nama Lengkap:")); panel.add(namaLengkapField); panel.add(new JLabel("Password:")); panel.add(passwordField); int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Warga Baru", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); if (result == JOptionPane.OK_OPTION) { String username = usernameField.getText().trim(); String namaLengkap = namaLengkapField.getText().trim(); String password = new String(passwordField.getPassword()); if (username.isEmpty() || namaLengkap.isEmpty() || password.isEmpty()) { JOptionPane.showMessageDialog(this, "Semua kolom harus diisi!", "Error", JOptionPane.ERROR_MESSAGE); return; } if (userDAO.isUsernameExists(username)) { JOptionPane.showMessageDialog(this, "Username '" + username + "' sudah ada!", "Error", JOptionPane.ERROR_MESSAGE); return; } try { String hashedPassword = CryptographyUtil.hashPassword(password); userDAO.saveWarga(username, namaLengkap, hashedPassword); JOptionPane.showMessageDialog(this, "Warga baru berhasil ditambahkan!"); loadUserData(); } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Gagal menambahkan warga: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE); e.printStackTrace(); } } }
    private void handleDeleteUser() { int selectedRow = userTable.getSelectedRow(); if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih warga yang ingin dihapus dari tabel.", "Peringatan", JOptionPane.WARNING_MESSAGE); return; } User selectedUser = this.userCache.get(selectedRow); int choice = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus " + selectedUser.getNamaLengkap() + "?\nSemua jadwal dan laporan miliknya juga akan terhapus.", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); if (choice == JOptionPane.YES_OPTION) { jadwalDAO.deleteSchedulesAndReportsByUserId(selectedUser.getId()); userDAO.delete(selectedUser.getId()); JOptionPane.showMessageDialog(this, "Warga '" + selectedUser.getNamaLengkap() + "' berhasil dihapus."); loadUserData(); loadScheduleData(); loadReportData(); } }
    private void handleCreateReport() { int selectedRow = scheduleTable.getSelectedRow(); if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih jadwal terlebih dahulu dari tabel.", "Peringatan", JOptionPane.WARNING_MESSAGE); return; } Jadwal selectedJadwal = this.jadwalCache.get(selectedRow); if ("warga".equals(currentUser.getRole())) { if (!selectedJadwal.getNamaPetugas().equals(currentUser.getNamaLengkap())) { JOptionPane.showMessageDialog(this, "Anda hanya bisa membuat laporan untuk jadwal Anda sendiri.", "Akses Ditolak", JOptionPane.ERROR_MESSAGE); return; } } int jadwalId = selectedJadwal.getId(); JTextArea textArea = new JTextArea(10, 30); textArea.setLineWrap(true); textArea.setWrapStyleWord(true); int result = JOptionPane.showConfirmDialog(this, new JScrollPane(textArea), bundle.getString("dialog.create.report.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); if (result == JOptionPane.OK_OPTION) { String reportContent = textArea.getText(); if (reportContent.trim().isEmpty()) { JOptionPane.showMessageDialog(this, bundle.getString("error.empty.fields"), "Error", JOptionPane.ERROR_MESSAGE); return; } try { byte[] encryptedReport = CryptographyUtil.encrypt(reportContent); jadwalDAO.createReport(jadwalId, encryptedReport); jadwalDAO.updateJadwalStatus(jadwalId, "Laporan Dibuat"); JOptionPane.showMessageDialog(this, bundle.getString("info.report.save.success"), "Sukses", JOptionPane.INFORMATION_MESSAGE); loadScheduleData(); loadReportData(); } catch (Exception e) { JOptionPane.showMessageDialog(this, bundle.getString("info.report.save.failed") + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); e.printStackTrace(); } } }
    private void handleLogout() { int choice = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin keluar dari sesi ini?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); if (choice == JOptionPane.YES_OPTION) { SerializationUtil.deleteSession(); new LoginForm().setVisible(true); this.dispose(); } }
    private void handleExit() { int choice = JOptionPane.showConfirmDialog(this, bundle.getString("confirm.exit.message"), bundle.getString("confirm.exit.title"), JOptionPane.YES_NO_OPTION); if (choice == JOptionPane.YES_OPTION) { System.exit(0); } }
}