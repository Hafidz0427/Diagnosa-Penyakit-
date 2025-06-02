package ui;

import config.Koneksi;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DataPasienForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public DataPasienForm() {
        setTitle("📋 Data Pasien & Hasil Diagnosa");
        setSize(1050, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        // Warna Tema
        Color backgroundColor = new Color(33, 36, 40);
        Color buttonColor = new Color(52, 152, 219);
        Color tableColor = new Color(45, 52, 54);
        Color textColor = Color.WHITE;

        // Kolom pertama (ID) disembunyikan nanti
        String[] columnNames = {
                "ID", "Nama", "Umur", "Jenis Kelamin", "Tanggal", "Diagnosa",
                "Penjelasan", "Penyebab", "Metode Pengobatan", "Daftar Obat"
        };

        model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(tableColor);
        table.setForeground(textColor);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.getTableHeader().setBackground(buttonColor);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Sembunyikan kolom ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(backgroundColor);

        loadData();

        // Tombol-tombol aksi
        JButton btnTambah = createStyledButton("➕ Tambah");
        JButton btnEdit = createStyledButton("✏️ Edit");
        JButton btnHapus = createStyledButton("🗑️ Hapus");
        JButton btnLogout = createStyledButton("🚪 Logout");

        btnTambah.addActionListener(e -> {
            new PasienForm().setVisible(true);
            this.dispose();
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) model.getValueAt(row, 0);
                new EditPasienForm(id);
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Pilih data yang ingin diedit.");
            }
        });


        btnHapus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) model.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi Hapus",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    hapusPasien(id);
                }
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Pilih data yang ingin dihapus.");
            }
        });

        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            this.dispose();
        });

        // Panel tombol
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnLogout);

        // Panel utama
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(130, 40));
        return button;
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = Koneksi.getKoneksi()) {
            String sql = "SELECT p.id_pasien, p.nama, p.umur, p.jenis_kelamin, p.tanggal, d.hasil_Text, " +
                    "pen.penjelasan, pen.penyebab, pen.metode_pengobatan, pen.daftar_obat " +
                    "FROM pasien p " +
                    "LEFT JOIN diagnosa d ON p.id_pasien = d.id_pasien " +
                    "LEFT JOIN penyakit pen ON d.id_penyakit = pen.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id_pasien"),
                        rs.getString("nama"),
                        rs.getInt("umur"),
                        rs.getString("jenis_kelamin"),
                        rs.getDate("tanggal"),
                        rs.getString("hasil_Text") != null ? rs.getString("hasil_Text") : "-",
                        rs.getString("penjelasan") != null ? rs.getString("penjelasan") : "-",
                        rs.getString("penyebab") != null ? rs.getString("penyebab") : "-",
                        rs.getString("metode_pengobatan") != null ? rs.getString("metode_pengobatan") : "-",
                        rs.getString("daftar_obat") != null ? rs.getString("daftar_obat") : "-"
                };
                model.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Gagal memuat data pasien:\n" + ex.getMessage());
        }
    }

    private void hapusPasien(int id) {
        try (Connection conn = Koneksi.getKoneksi()) {
            conn.setAutoCommit(false); // Start transaction

            String sql1 = "DELETE FROM diagnosa WHERE id_pasien = ?";
            String sql2 = "DELETE FROM gejala_pasien WHERE id_pasien = ?";
            String sql3 = "DELETE FROM pasien WHERE id_pasien = ?";

            try (
                    PreparedStatement ps1 = conn.prepareStatement(sql1);
                    PreparedStatement ps2 = conn.prepareStatement(sql2);
                    PreparedStatement ps3 = conn.prepareStatement(sql3)
            ) {
                ps1.setInt(1, id); ps1.executeUpdate();
                ps2.setInt(1, id); ps2.executeUpdate();
                ps3.setInt(1, id); ps3.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "✅ Data pasien berhasil dihapus.");
                loadData();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Gagal menghapus data: " + ex.getMessage());
        }
    }
}
