package multibahasa;

import java.util.Locale;
import javax.swing.*;

public class MainFrame extends javax.swing.JFrame {

    private String[] languageNames = {"English", "Bahasa", "Español"};
    private String[] languageCodes = {"en", "id", "es"};

    public MainFrame() {
        initComponents();
        setLocationRelativeTo(null);
        setTitle(LanguageManager.get("title"));
        labelGreeting.setText(LanguageManager.get("greeting"));

        comboLanguage.setModel(new javax.swing.DefaultComboBoxModel<>(languageNames));
        comboLanguage.setSelectedIndex(0);

        comboLanguage.addActionListener(e -> {
            int selected = comboLanguage.getSelectedIndex();
            LanguageManager.setLanguage(languageCodes[selected]);
            updateTexts();
        });
    }

    private void updateTexts() {
        setTitle(LanguageManager.get("title"));
        labelGreeting.setText(LanguageManager.get("greeting"));
    }

    private void initComponents() {
        labelGreeting = new JLabel();
        comboLanguage = new JComboBox<>();
        JLabel labelSelect = new JLabel("Bahasa:");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLayout(null);

        labelGreeting.setBounds(120, 30, 200, 30);
        add(labelGreeting);

        comboLanguage.setBounds(100, 80, 120, 30);
        add(comboLanguage);

        labelSelect.setBounds(40, 80, 60, 30);
        add(labelSelect);

        setSize(350, 200);
    }

    private JLabel labelGreeting;
    private JComboBox<String> comboLanguage;

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}