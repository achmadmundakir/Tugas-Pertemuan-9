// LOKASI: Main.java (versi final)
package com.uas.rondaapp.main;

import com.uas.rondaapp.view.LoginForm;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}