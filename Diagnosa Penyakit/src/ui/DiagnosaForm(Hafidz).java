package ui;

import config.Koneksi;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DiagnosaForm extends JFrame {
    public DiagnosaForm(int idPenyakit) {
        setTitle("Hasil Diagnosa Pasien");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Title Label
        JLabel lblJudul = new JLabel("Detail Diagnosa Penyakit");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblJudul.setHorizontalAlignment(SwingConstants.CENTER);

        // TextArea
        JTextArea txtHasil = new JTextArea();
        txtHasil.setEditable(false);
        txtHasil.setLineWrap(true);
        txtHasil.setWrapStyleWord(true);
        txtHasil.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Scroll pane with padding
        JScrollPane scrollPane = new JScrollPane(txtHasil);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Informasi Diagnosa"));

        try (Connection conn = Koneksi.getKoneksi()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM penyakit WHERE id = ?");
            ps.setInt(1, idPenyakit);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String output = "🩺 Nama Penyakit: " + rs.getString("nama_penyakit") + "\n\n"
                        + "📖 Penjelasan:\n" + rs.getString("penjelasan") + "\n\n"
                        + "⚠️ Penyebab:\n" + rs.getString("penyebab") + "\n\n"
                        + "💊 Metode Pengobatan:\n" + rs.getString("metode_pengobatan") + "\n\n"
                        + "📋 Daftar Obat:\n" + rs.getString("daftar_obat");

                txtHasil.setText(output);
                txtHasil.setCaretPosition(0); // Scroll to top
            } else {
                txtHasil.setText("❌ Data penyakit tidak ditemukan.");
            }
        } catch (SQLException ex) {
            txtHasil.setText("❌ Gagal memuat detail penyakit:\n" + ex.getMessage());
        }

        // Button
        JButton btnLihatData = new JButton("⬅ Kembali ke Data Pasien");
        btnLihatData.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLihatData.setBackground(new Color(0x4CAF50));
        btnLihatData.setForeground(Color.WHITE);
        btnLihatData.setFocusPainted(false);
        btnLihatData.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnLihatData.addActionListener(e -> {
            new DataPasienForm().setVisible(true);
            this.dispose();
        });

        // Layout
        JPanel panelUtama = new JPanel(new BorderLayout(10, 10));
        panelUtama.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelUtama.add(lblJudul, BorderLayout.NORTH);
        panelUtama.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBawah.add(btnLihatData);
        panelUtama.add(panelBawah, BorderLayout.SOUTH);

        setContentPane(panelUtama);
    }
}
