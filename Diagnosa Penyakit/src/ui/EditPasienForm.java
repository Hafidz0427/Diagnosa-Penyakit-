package ui;

import config.Koneksi;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EditPasienForm extends JFrame {
    private int idPasien;
    private JTextField txtNama, txtUmur;
    private JComboBox<String> cmbJenisKelamin;
    private JButton btnSimpan, btnBatal;

    public EditPasienForm(int idPasien) {
        this.idPasien = idPasien;
        setTitle("✏️ Edit Data Pasien");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        loadData();
        setVisible(true);
    }

    private void initComponents() {
        JLabel lblNama = new JLabel("Nama:");
        JLabel lblUmur = new JLabel("Umur:");
        JLabel lblJK = new JLabel("Jenis Kelamin:");

        txtNama = new JTextField(20);
        txtUmur = new JTextField(5);
        cmbJenisKelamin = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});

        btnSimpan = new JButton("💾 Simpan");
        btnBatal = new JButton("❌ Batal");

        btnSimpan.addActionListener(e -> simpanData());
        btnBatal.addActionListener(e -> dispose());

        JPanel panelForm = new JPanel(new GridLayout(4, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelForm.add(lblNama); panelForm.add(txtNama);
        panelForm.add(lblUmur); panelForm.add(txtUmur);
        panelForm.add(lblJK); panelForm.add(cmbJenisKelamin);
        panelForm.add(btnSimpan); panelForm.add(btnBatal);

        setContentPane(panelForm);
    }

    private void loadData() {
        try (Connection conn = Koneksi.getKoneksi()) {
            String sql = "SELECT * FROM pasien WHERE id_pasien = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idPasien);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtNama.setText(rs.getString("nama"));
                txtUmur.setText(String.valueOf(rs.getInt("umur")));
                cmbJenisKelamin.setSelectedItem(rs.getString("jenis_kelamin"));
            } else {
                JOptionPane.showMessageDialog(this, "❌ Data pasien tidak ditemukan.");
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Gagal memuat data: " + ex.getMessage());
        }
    }

    private void simpanData() {
        String nama = txtNama.getText().trim();
        String umurStr = txtUmur.getText().trim();
        String jk = (String) cmbJenisKelamin.getSelectedItem();

        if (nama.isEmpty() || umurStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Semua kolom harus diisi.");
            return;
        }

        try {
            int umur = Integer.parseInt(umurStr);
            try (Connection conn = Koneksi.getKoneksi()) {
                String sql = "UPDATE pasien SET nama = ?, umur = ?, jenis_kelamin = ? WHERE id_pasien = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nama);
                ps.setInt(2, umur);
                ps.setString(3, jk);
                ps.setInt(4, idPasien);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "✅ Data berhasil diperbarui.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Gagal memperbarui data.");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ Umur harus berupa angka.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Gagal menyimpan data: " + ex.getMessage());
        }
    }
}
