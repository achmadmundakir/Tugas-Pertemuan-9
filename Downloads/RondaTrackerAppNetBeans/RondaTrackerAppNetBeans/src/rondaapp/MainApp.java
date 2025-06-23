package rondaapp;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Base64;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*; // untuk GUI
import java.awt.BorderLayout;


public class MainApp {
    static Locale currentLocale = new Locale("id");
    static ResourceBundle messages = ResourceBundle.getBundle("rondaapp.LanguageBundle", currentLocale);
    static List<Jadwal<String>> jadwalList = new ArrayList<>();
    static List<Laporan<String>> laporanList = new ArrayList<>();
    static AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("cli")) {
            runTerminal();
        } else {
            runGUI();
        }
    }

    private static void runTerminal() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                DataManager.saveData(jadwalList, "jadwal.dat");
                DataManager.saveData(laporanList, "laporan.dat");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 20, TimeUnit.SECONDS);

        Scanner scanner = new Scanner(System.in);

        while (running.get()) {
            System.out.println("\n" + messages.getString("menu"));
            System.out.println("1. " + messages.getString("input_jadwal"));
            System.out.println("2. " + messages.getString("input_laporan"));
            System.out.println("3. " + messages.getString("lihat_jadwal"));
            System.out.println("4. " + messages.getString("lihat_laporan"));
            System.out.println("5. " + messages.getString("keluar"));

            System.out.print("Pilihan Anda: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Masukkan angka yang valid: ");
                scanner.next();
            }
            int pilihan = scanner.nextInt();
            scanner.nextLine();

            switch (pilihan) {
                case 1 -> {
                    System.out.print(messages.getString("masukkan_nama"));
                    String nama = scanner.nextLine();
                    try {
                        String encrypted = Base64.getEncoder().encodeToString(CryptoUtil.encrypt(nama.getBytes()));
                        jadwalList.add(new Jadwal<>(encrypted));
                    } catch (Exception e) {
                        System.out.println("Gagal mengenkripsi data.");
                    }
                }
                case 2 -> {
                    System.out.print(messages.getString("masukkan_laporan"));
                    String isi = scanner.nextLine();
                    try {
                        String encrypted = Base64.getEncoder().encodeToString(CryptoUtil.encrypt(isi.getBytes()));
                        laporanList.add(new Laporan<>(encrypted));
                    } catch (Exception e) {
                        System.out.println("Gagal mengenkripsi data.");
                    }
                }
                case 3 -> {
                    for (Jadwal<String> j : jadwalList) {
                        try {
                            byte[] decoded = Base64.getDecoder().decode(j.getIsi());
                            String decrypted = new String(CryptoUtil.decrypt(decoded));
                            System.out.println("- " + decrypted);
                        } catch (Exception e) {
                            System.out.println("- (Gagal dekripsi data jadwal)");
                        }
                    }
                }
                case 4 -> {
                    for (Laporan<String> l : laporanList) {
                        try {
                            byte[] decoded = Base64.getDecoder().decode(l.getIsi());
                            String decrypted = new String(CryptoUtil.decrypt(decoded));
                            System.out.println("- " + decrypted);
                        } catch (Exception e) {
                            System.out.println("- (Gagal dekripsi data laporan)");
                        }
                    }
                }
                case 5 -> {
                    running.set(false);
                    executor.shutdown();
                    System.out.println(messages.getString("sampai_jumpa"));
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private static void runGUI() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ronda Tracker App");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLayout(new BorderLayout());

            JTextArea outputArea = new JTextArea();
            JScrollPane scrollPane = new JScrollPane(outputArea);
            frame.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            JButton btnJadwal = new JButton(messages.getString("input_jadwal"));
            JButton btnLaporan = new JButton(messages.getString("input_laporan"));
            JButton btnLihatJadwal = new JButton(messages.getString("lihat_jadwal"));
            JButton btnLihatLaporan = new JButton(messages.getString("lihat_laporan"));
            JButton btnKeluar = new JButton(messages.getString("keluar"));

            buttonPanel.add(btnJadwal);
            buttonPanel.add(btnLaporan);
            buttonPanel.add(btnLihatJadwal);
            buttonPanel.add(btnLihatLaporan);
            buttonPanel.add(btnKeluar);

            frame.add(buttonPanel, BorderLayout.NORTH);

            btnJadwal.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(frame, messages.getString("masukkan_nama"));
                if (input != null && !input.isEmpty()) {
                    try {
                        String encrypted = Base64.getEncoder().encodeToString(CryptoUtil.encrypt(input.getBytes()));
                        jadwalList.add(new Jadwal<>(encrypted));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Gagal menyimpan data terenkripsi.");
                    }
                }
            });

            btnLaporan.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(frame, messages.getString("masukkan_laporan"));
                if (input != null && !input.isEmpty()) {
                    try {
                        String encrypted = Base64.getEncoder().encodeToString(CryptoUtil.encrypt(input.getBytes()));
                        laporanList.add(new Laporan<>(encrypted));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Gagal menyimpan data terenkripsi.");
                    }
                }
            });

            btnLihatJadwal.addActionListener(e -> {
                outputArea.setText("");
                for (Jadwal<String> j : jadwalList) {
                    try {
                        byte[] decoded = Base64.getDecoder().decode(j.getIsi());
                        String decrypted = new String(CryptoUtil.decrypt(decoded));
                        outputArea.append("- " + decrypted + "\n");
                    } catch (Exception ex) {
                        outputArea.append("- (Gagal dekripsi data jadwal)\n");
                    }
                }
            });

            btnLihatLaporan.addActionListener(e -> {
                outputArea.setText("");
                for (Laporan<String> l : laporanList) {
                    try {
                        byte[] decoded = Base64.getDecoder().decode(l.getIsi());
                        String decrypted = new String(CryptoUtil.decrypt(decoded));
                        outputArea.append("- " + decrypted + "\n");
                    } catch (Exception ex) {
                        outputArea.append("- (Gagal dekripsi data laporan)\n");
                    }
                }
            });

            btnKeluar.addActionListener(e -> {
                JOptionPane.showMessageDialog(frame, messages.getString("sampai_jumpa"));
                System.exit(0);
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
