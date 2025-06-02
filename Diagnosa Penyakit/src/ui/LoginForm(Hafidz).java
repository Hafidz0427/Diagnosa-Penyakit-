package ui;

import config.Koneksi;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin;

    public LoginForm() {
        setTitle("Login - Diagnosa Penyakit");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        // Warna Tema
        Color backgroundColor = new Color(45, 52, 54); // abu tua
        Color formColor = new Color(99, 110, 114);     // abu muda
        Color buttonColor = new Color(9, 132, 227);    // biru elegan
        Color textColor = Color.WHITE;

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);

        JLabel lblTitle = new JLabel("Silakan Login", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(textColor);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setForeground(textColor);
        tfUsername = new JTextField(15);
        tfUsername.setBackground(formColor);
        tfUsername.setForeground(Color.WHITE);
        tfUsername.setCaretColor(Color.WHITE);
        tfUsername.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setForeground(textColor);
        pfPassword = new JPasswordField(15);
        pfPassword.setBackground(formColor);
        pfPassword.setForeground(Color.WHITE);
        pfPassword.setCaretColor(Color.WHITE);
        pfPassword.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        btnLogin = new JButton("Login");
        btnLogin.setBackground(buttonColor);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Posisi elemen di grid
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblUsername, gbc);

        gbc.gridx = 1;
        formPanel.add(tfUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblPassword, gbc);

        gbc.gridx = 1;
        formPanel.add(pfPassword, gbc);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(btnLogin, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        add(panel);

        // Aksi tombol
        btnLogin.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String user = tfUsername.getText();
        String pass = String.valueOf(pfPassword.getPassword());

        try (Connection conn = Koneksi.getKoneksi()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login berhasil!");
                new PasienForm().setVisible(true); // ganti sesuai form lanjutan
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username atau Password salah!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginForm::new);
    }
}
