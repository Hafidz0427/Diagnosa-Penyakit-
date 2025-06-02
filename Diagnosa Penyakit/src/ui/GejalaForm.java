package ui;

import config.Koneksi;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class GejalaForm extends JFrame {
    private int idPasien;
    private List<JCheckBox> gejalaCheckBoxList;
    private JButton btnDiagnosa;

    public GejalaForm(int idPasien) {
        this.idPasien = idPasien;
        setTitle("🩺 Pemilihan Gejala Pasien");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        Color backgroundColor = new Color(40, 42, 54);
        Color textColor = new Color(248, 248, 242);
        Color accentColor = new Color(80, 120, 200);
        Font fontRegular = new Font("Segoe UI", Font.PLAIN, 14);
        Font fontBold = new Font("Segoe UI", Font.BOLD, 16);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(backgroundColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        gejalaCheckBoxList = new ArrayList<>();

        try (Connection conn = Koneksi.getKoneksi()) {
            Map<Integer, String> penyakitMap = new HashMap<>();
            Statement stmtPenyakit = conn.createStatement();
            ResultSet rsPenyakit = stmtPenyakit.executeQuery("SELECT id, nama_penyakit FROM penyakit");
            while (rsPenyakit.next()) {
                penyakitMap.put(rsPenyakit.getInt("id"), rsPenyakit.getString("nama_penyakit"));
            }

            Map<Integer, List<JCheckBox>> gejalaByPenyakit = new LinkedHashMap<>();
            gejalaByPenyakit.put(0, new ArrayList<>());

            Statement stmtGejala = conn.createStatement();
            ResultSet rsGejala = stmtGejala.executeQuery("SELECT * FROM gejala ORDER BY id_penyakit");
            while (rsGejala.next()) {
                int idGejala = rsGejala.getInt("id_gejala");
                String namaGejala = rsGejala.getString("nama_gejala");
                int idPenyakit = rsGejala.getInt("id_penyakit");

                JCheckBox cb = new JCheckBox(namaGejala);
                cb.setActionCommand(String.valueOf(idGejala));
                cb.setFont(fontRegular);
                cb.setForeground(textColor);
                cb.setBackground(backgroundColor);
                gejalaCheckBoxList.add(cb);

                gejalaByPenyakit.computeIfAbsent(idPenyakit, k -> new ArrayList<>()).add(cb);
            }

            for (Map.Entry<Integer, List<JCheckBox>> entry : gejalaByPenyakit.entrySet()) {
                int idPenyakit = entry.getKey();
                String label = (idPenyakit == 0) ? " " : " " + penyakitMap.getOrDefault(idPenyakit, "Tidak Diketahui");

                JLabel sectionLabel = new JLabel(label);
                sectionLabel.setFont(fontBold);
                sectionLabel.setForeground(accentColor);
                sectionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
                contentPanel.add(sectionLabel);

                JPanel checkboxPanel = new JPanel();
                checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
                checkboxPanel.setBackground(backgroundColor);
                checkboxPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 0));

                for (JCheckBox cb : entry.getValue()) {
                    checkboxPanel.add(cb);
                }

                contentPanel.add(checkboxPanel);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat gejala: " + ex.getMessage());
        }

        btnDiagnosa = new JButton("🧾 Simpan & Diagnosa");
        styleButton(btnDiagnosa, accentColor, textColor);
        btnDiagnosa.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDiagnosa.addActionListener(e -> prosesDiagnosa());

        JButton btnKembali = new JButton("⬅ Kembali");
        styleButton(btnKembali, new Color(100, 100, 100), textColor);
        btnKembali.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnKembali.addActionListener(e -> {
            new DataPasienForm().setVisible(true);
            this.dispose();
        });

        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(btnDiagnosa);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(btnKembali);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll);
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    private void prosesDiagnosa() {
        Map<Integer, Integer> penyakitCount = new HashMap<>();
        boolean adaGejalaDipilih = false;

        int konfirmasi = JOptionPane.showConfirmDialog(this, "Simpan dan proses diagnosa sekarang?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        try (Connection conn = Koneksi.getKoneksi()) {
            PreparedStatement deleteOld = conn.prepareStatement("DELETE FROM gejala_pasien WHERE id_pasien = ?");
            deleteOld.setInt(1, idPasien);
            deleteOld.executeUpdate();

            PreparedStatement deleteDiagnosa = conn.prepareStatement("DELETE FROM diagnosa WHERE id_pasien = ?");
            deleteDiagnosa.setInt(1, idPasien);
            deleteDiagnosa.executeUpdate();

            for (JCheckBox cb : gejalaCheckBoxList) {
                if (cb.isSelected()) {
                    adaGejalaDipilih = true;
                    int idGejala = Integer.parseInt(cb.getActionCommand());

                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO gejala_pasien (id_pasien, id_gejala) VALUES (?, ?)");
                    ps.setInt(1, idPasien);
                    ps.setInt(2, idGejala);
                    ps.executeUpdate();

                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id_penyakit FROM gejala WHERE id_gejala = " + idGejala);
                    if (rs.next()) {
                        int idPenyakit = rs.getInt("id_penyakit");
                        if (idPenyakit > 0) {
                            penyakitCount.put(idPenyakit, penyakitCount.getOrDefault(idPenyakit, 0) + 1);
                        }
                    }
                }
            }

            int hasilPenyakit = penyakitCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(0);

            PreparedStatement psDiagnosa = conn.prepareStatement(
                    "INSERT INTO diagnosa (id_pasien, id_penyakit, hasil_Text) VALUES (?, ?, ?)");
            psDiagnosa.setInt(1, idPasien);

            if (hasilPenyakit > 0) {
                String namaPenyakit = "Penyakit";
                PreparedStatement psNama = conn.prepareStatement("SELECT nama_penyakit FROM penyakit WHERE id = ?");
                psNama.setInt(1, hasilPenyakit);
                ResultSet rsNama = psNama.executeQuery();
                if (rsNama.next()) {
                    namaPenyakit = rsNama.getString("nama_penyakit");
                }

                psDiagnosa.setInt(2, hasilPenyakit);
                psDiagnosa.setString(3, "Terdeteksi penyakit: " + namaPenyakit);
            } else if (adaGejalaDipilih) {
                psDiagnosa.setNull(2, java.sql.Types.INTEGER);
                psDiagnosa.setString(3, "Gejala umum terdeteksi. Perlu observasi lebih lanjut.");
            } else {
                psDiagnosa.setNull(2, java.sql.Types.INTEGER);
                psDiagnosa.setString(3, "Tidak ada gejala yang dipilih.");
            }

            psDiagnosa.executeUpdate();

            if (hasilPenyakit > 0) {
                new DiagnosaForm(hasilPenyakit).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Diagnosa umum telah disimpan.");
                new DataPasienForm().setVisible(true);
            }
            this.dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan diagnosa: " + ex.getMessage());
        }
    }
}
