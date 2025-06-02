package ui;

import config.Koneksi;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class PasienForm extends JFrame {
    private JTextField tfNama, tfUmur;
    private JComboBox<String> cbJK;
    private JButton btnLanjut;

    public PasienForm() {
        setTitle("Form Data Pasien");
        setSize(450, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); // padding
        panel.setBackground(new Color(245, 245, 250)); // soft background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // spacing between fields
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Form Pendaftaran Pasien");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(new Color(50, 50, 100));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Nama
        panel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1;
        tfNama = new JTextField(20);
        panel.add(tfNama, gbc);

        // Umur
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Umur:"), gbc);
        gbc.gridx = 1;
        tfUmur = new JTextField();
        panel.add(tfUmur, gbc);

        // Jenis Kelamin
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        cbJK = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
        panel.add(cbJK, gbc);

        // Tombol
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        btnLanjut = new JButton("Lanjut ke Gejala");
        btnLanjut.setBackground(new Color(70, 130, 180));
        btnLanjut.setForeground(Color.WHITE);
        btnLanjut.setFocusPainted(false);
        btnLanjut.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(btnLanjut, gbc);

        btnLanjut.addActionListener(e -> simpanPasien());

        add(panel);
    }

    private void simpanPasien() {
        String nama = tfNama.getText().trim();
        String umurStr = tfUmur.getText().trim();
        String jk = cbJK.getSelectedIndex() == 0 ? "L" : "P";

        if (nama.isEmpty() || umurStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan umur harus diisi.");
            return;
        }

        int umur;
        try {
            umur = Integer.parseInt(umurStr);
            if (umur <= 0) {
                JOptionPane.showMessageDialog(this, "Umur harus lebih dari 0.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Umur harus berupa angka.");
            return;
        }

        try (Connection conn = Koneksi.getKoneksi()) {
            String sql = "INSERT INTO pasien (nama, umur, jenis_kelamin, tanggal) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nama);
            ps.setInt(2, umur);
            ps.setString(3, jk);
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int idPasien = rs.getInt(1);
                new GejalaForm(idPasien).setVisible(true);
                this.dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data pasien: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PasienForm().setVisible(true));
    }
}
