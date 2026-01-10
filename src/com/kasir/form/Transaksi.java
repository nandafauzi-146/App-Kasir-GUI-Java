package com.kasir.form;


import com.kasir.koneksiDB.Koneksi;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.*;




/**
 *
 * @author Nanda Fauzi
 */
public class Transaksi extends javax.swing.JFrame {
    
    
    

    private void tampilkanWaktu() {
    Timer timer = new Timer(1000, e -> {
        Date sekarang = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        lblWaktu.setText(sdf.format(sekarang));
    });
    timer.start();
}
   
    private int KodeBarang = 1;
    
    

   private String generateKodeBarang() {
    return String.format("KDB%03d", KodeBarang);
}

    
    private String formatRupiah(int angka) {
    return "Rp " + NumberFormat
            .getInstance(new Locale("id", "ID"))
            .format(angka);
}
    
    private void ambilKodeBarangTerakhir() {
    try {
        Connection conn = Koneksi.getKoneksi();
        String sql = "SELECT kode_barang FROM detail_transaksi ORDER BY id_detail DESC LIMIT 1";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if(rs.next()) {
            String lastKode = rs.getString("kode_barang");
            KodeBarang = Integer.parseInt(lastKode.replaceAll("[^0-9]", "")) + 1;
        } else {
            KodeBarang = 1;
        }
    } catch (Exception e) {
        KodeBarang = 1;
    }
}

    
    
    
    
   private void hitungTotalBayar() {
    int total = 0;

    // Hitung total dari tabel
    for (int i = 0; i < tblTransaksi.getRowCount(); i++) {
        total += Integer.parseInt(tblTransaksi.getValueAt(i, 6).toString());
    }

    if (tblTransaksi.getRowCount() == 0) {
        lblTotalBayar.setText("Rp 0"); // Reset jika tidak ada transaksi
        return;
    }

    // Jika txtDiskon BELUM mengandung %, tampilkan total normal
    if (!txtDiskon.getText().contains("%")) {
        lblTotalBayar.setText(formatRupiah(total));
        return;
    }

    // Ambil angka diskon (hapus %)
    int diskon = Integer.parseInt(txtDiskon.getText().replaceAll("[^\\d]", ""));
    if (diskon > 100) diskon = 100;

    int potongan = total * diskon / 100;
    int totalAkhir = total - potongan;

     lblTotalBayar.setText(formatRupiah(totalAkhir));
}


   
   
   private void aturFieldAwal() {
    txtNamaBarang.setEnabled(true);
    cmbKategori.setEnabled(false);
    txtHarga.setEnabled(false);
    cmbSatuan.setEnabled(false);
    txtJumlah.setEnabled(false);
    txtJumlah.setText("");
    
}


    DefaultTableModel model;  
   
    // ===== DATA FINAL TRANSAKSI =====
    private int finalTotalBayar = 0;
    private int finalBayar = 0;
    private int finalKembalian = 0;
    private String finalDiskon = "0%";
    private String finalJenisBayar = "";

    
    public Transaksi() {
    initComponents();
    tampilkanWaktu(); 
    aturFieldAwal();
    loadDetailTransaksi();
    ambilKodeBarangTerakhir();
    
    txtKodeBarang.setText(generateKodeBarang());
    txtKodeBarang.setEditable(false); 
    btnStruk.setEnabled(false);

    model = (DefaultTableModel) tblTransaksi.getModel();
    txtKembalian.setEditable(false);
     

}
    private boolean sudahBayar = false;
    private String kodeBarangEdit = null;
    private boolean isEdit = false;



    private String potong(String teks, int max) {
    if (teks == null) return "";
    if (teks.length() > max) {
        return teks.substring(0, max - 1);
    }
    return teks;
}
    
    private void hitungKembalianOtomatis() {
    try {
        // Ambil total bayar dari label
        int totalBayar = Integer.parseInt(
            lblTotalBayar.getText()
                .replace("Rp", "")
                .replace(".", "")
                .trim()
        );

        // Jika txtBayar kosong → reset kembalian
        if (txtBayar.getText().isEmpty()) {
            txtKembalian.setText("");
            return;
        }

        int bayar = Integer.parseInt(
            txtBayar.getText().replaceAll("[^\\d]", "")
        );

        int kembalian = bayar - totalBayar;

        if (kembalian < 0) {
            txtKembalian.setText("Rp 0");
        } else {
            txtKembalian.setText(formatRupiah(kembalian));
        }

    } catch (NumberFormatException e) {
        txtKembalian.setText("");
    }
}


    
    private void tampilkanStruk() {
    StringBuilder struk = new StringBuilder();
    String tanggal = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

    struk.append("========= KASIR TOKO ABADI =========\n");
    struk.append(lblAlamat.getText()).append("\n");
    struk.append("-------------------------------------\n");
    struk.append("Tanggal      : ").append(tanggal).append("\n");
    struk.append("Waktu        : ").append(lblWaktu.getText()).append("\n");
    struk.append("Jenis Bayar  : ").append(finalJenisBayar).append("\n");
    struk.append("Diskon       : ").append(finalDiskon).append("\n");

    struk.append("-------------------------------------------------\n");

    // HEADER TABEL STRUK
    struk.append(String.format(
        "%-12s %-10s %-8s %-6s %-10s\n",
        "Barang", "Harga", "Satuan", "Jumlah", "Total"
    ));
    struk.append("--------------------------------------------------\n");

    // ISI BARANG (KESAMPING)
    for (int i = 0; i < tblTransaksi.getRowCount(); i++) {

        String nama   = tblTransaksi.getValueAt(i, 1).toString();
        int harga     = Integer.parseInt(tblTransaksi.getValueAt(i, 3).toString());
        String satuan = tblTransaksi.getValueAt(i, 4).toString();
        int jumlah    = Integer.parseInt(tblTransaksi.getValueAt(i, 5).toString());
        int total     = Integer.parseInt(tblTransaksi.getValueAt(i, 6).toString());

        struk.append(String.format(
            "%-12s %-10s %-8s %-6d %-10s\n",
            potong(nama, 12),
            formatRupiah(harga),
            satuan,
            jumlah,
            formatRupiah(total)
        ));
    }

    struk.append("-----------------------------------\n");
    struk.append("Total Bayar : ").append(formatRupiah(finalTotalBayar)).append("\n");
    struk.append("Bayar       : ").append(formatRupiah(finalBayar)).append("\n");
    struk.append("Kembalian   : ").append(formatRupiah(finalKembalian)).append("\n");
    struk.append("===================================\n");
    struk.append("Terima Kasih");


    JTextArea area = new JTextArea(struk.toString());
    area.setEditable(false);
    area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

    JOptionPane.showMessageDialog(
        this,
        new JScrollPane(area),
        "STRUK PEMBAYARAN",
        JOptionPane.INFORMATION_MESSAGE
    );
}
    

    
    
    
    
    

    private void loadDetailTransaksi() {
    DefaultTableModel model = (DefaultTableModel) tblTransaksi.getModel();
    model.setRowCount(0);

    try {
        Connection conn = Koneksi.getKoneksi();
        String sql = "SELECT * FROM detail_transaksi";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("kode_barang"),
                rs.getString("nama_barang"),
                rs.getString("kategori"),
                rs.getInt("harga"),
                rs.getString("satuan"),
                rs.getInt("jumlah"),
                rs.getInt("total")
            });
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }

    // Perbarui total bayar setelah reload tabel
    hitungTotalBayar();
}

    private void resetFieldPembayaran() {
    txtDiskon.setText("");
    txtBayar.setText("");
    txtKembalian.setText("");
}


    private void resetInputBarang() {
    txtNamaBarang.setText("");
    txtHarga.setText("");
    txtJumlah.setText("");
    cmbKategori.setSelectedIndex(0);
    cmbSatuan.setSelectedIndex(0);
    txtNamaBarang.requestFocus();
    
    txtKodeBarang.setEnabled(true);
}
    




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblTotalBayar = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblAlamat = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNamaBarang = new javax.swing.JTextField();
        txtKodeBarang = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtHarga = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        cmbKategori = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        cmbSatuan = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        txtJumlah = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTransaksi = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtBayar = new javax.swing.JTextField();
        cmbJenisBayar = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtDiskon = new javax.swing.JTextField();
        txtKembalian = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        btnBayar = new javax.swing.JButton();
        btnStruk = new javax.swing.JButton();
        btnTambah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        lblWaktu = new javax.swing.JLabel();
        btnEdit = new javax.swing.JButton();
        btnOke = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel4.setBackground(new java.awt.Color(51, 102, 255));

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("TOTAL BAYAR");

        jLabel2.setFont(new java.awt.Font("News701 BT", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kasir/icon/logo.png"))); // NOI18N
        jLabel2.setText("KASIR TOKO ABADI");

        lblTotalBayar.setFont(new java.awt.Font("Krungthep", 1, 36)); // NOI18N
        lblTotalBayar.setForeground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );

        lblAlamat.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        lblAlamat.setForeground(new java.awt.Color(255, 255, 255));
        lblAlamat.setText("Jl. Magelang KM 7.5, Sendangadi, Mlati, Sleman Yogyakarta");

        jSeparator1.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel2))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(81, 81, 81)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblAlamat))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTotalBayar, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(21, 21, 21))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(lblTotalBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(lblAlamat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1)))
                .addGap(19, 19, 19))
        );

        jLabel3.setText("Nama Barang");

        jLabel4.setText("Kode Barang");

        txtNamaBarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNamaBarangKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNamaBarangKeyTyped(evt);
            }
        });

        jLabel7.setText("Harga");

        txtHarga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHargaActionPerformed(evt);
            }
        });
        txtHarga.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtHargaKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtHargaKeyTyped(evt);
            }
        });

        jLabel8.setText("Kategori");

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Makanan", "Minuman", "ATK", "Sembako" }));
        cmbKategori.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbKategoriItemStateChanged(evt);
            }
        });

        jLabel9.setText("Satuan");

        cmbSatuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Unit", "Pcs", "Kg", "Liter" }));
        cmbSatuan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSatuanItemStateChanged(evt);
            }
        });

        jLabel10.setText("Jumlah");

        txtJumlah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtJumlahKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtJumlahKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtJumlahKeyTyped(evt);
            }
        });

        tblTransaksi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Kd_Barang", "Nama Barang", "Kategori", "Harga", "Satuan", "Jumlah", "Total"
            }
        ));
        tblTransaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTransaksiMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblTransaksi);

        jPanel3.setBackground(new java.awt.Color(0, 102, 255));

        jLabel11.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kasir/icon/Bayar.png"))); // NOI18N
        jLabel11.setText("PEMBAYARAN");

        jLabel13.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Bayar");

        txtBayar.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        txtBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBayarActionPerformed(evt);
            }
        });
        txtBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBayarKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBayarKeyTyped(evt);
            }
        });

        cmbJenisBayar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tunai", "Non Tunai" }));

        jLabel14.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("Jenis Bayar");

        jLabel15.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Diskon");

        txtDiskon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDiskonKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDiskonKeyTyped(evt);
            }
        });

        txtKembalian.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        txtKembalian.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtKembalianKeyReleased(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Kembalian");

        btnBayar.setFont(new java.awt.Font("Krungthep", 1, 18)); // NOI18N
        btnBayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kasir/icon/Logo Bayar.png"))); // NOI18N
        btnBayar.setText("BAYAR");
        btnBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBayarActionPerformed(evt);
            }
        });

        btnStruk.setBackground(new java.awt.Color(255, 153, 0));
        btnStruk.setFont(new java.awt.Font("Krungthep", 1, 18)); // NOI18N
        btnStruk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kasir/icon/receipt.png"))); // NOI18N
        btnStruk.setText("STRUK");
        btnStruk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStrukActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(187, 187, 187)
                        .addComponent(jLabel11))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addComponent(btnBayar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnStruk)
                                .addGap(56, 56, 56))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel13))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtKembalian, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                                    .addComponent(cmbJenisBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtBayar)
                                    .addComponent(txtDiskon))))))
                .addGap(17, 17, 17))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel11)
                .addGap(59, 59, 59)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbJenisBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addGap(26, 26, 26)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtDiskon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addGap(29, 29, 29)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(txtKembalian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBayar)
                    .addComponent(btnStruk))
                .addGap(37, 37, 37))
        );

        btnTambah.setBackground(new java.awt.Color(102, 255, 102));
        btnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kasir/icon/plus-symbol-button (1).png"))); // NOI18N
        btnTambah.setText("TAMBAH");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        btnHapus.setBackground(new java.awt.Color(255, 51, 51));
        btnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kasir/icon/bin.png"))); // NOI18N
        btnHapus.setText("HAPUS");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        lblWaktu.setFont(new java.awt.Font("Helvetica Neue", 1, 36)); // NOI18N

        btnEdit.setBackground(new java.awt.Color(102, 153, 255));
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kasir/icon/pencil.png"))); // NOI18N
        btnEdit.setText("EDIT");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnOke.setBackground(new java.awt.Color(0, 255, 204));
        btnOke.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kasir/icon/checked.png"))); // NOI18N
        btnOke.setText("OKE");
        btnOke.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 596, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel8))
                        .addGap(41, 41, 41)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtKodeBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(52, 52, 52)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtHarga, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                            .addComponent(cmbSatuan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtJumlah))
                        .addGap(61, 61, 61)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnTambah)
                            .addComponent(btnHapus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblWaktu, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(55, 55, 55))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnOke, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(txtHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtKodeBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel9)
                                    .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnTambah)
                                    .addComponent(btnEdit))
                                .addGap(18, 18, 18))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addComponent(lblWaktu, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(21, 21, 21)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel8)
                                .addComponent(jLabel10)
                                .addComponent(txtJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnHapus))
                            .addComponent(btnOke))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtHargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHargaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtHargaActionPerformed

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        String kode = txtKodeBarang.getText().trim();
    String nama = txtNamaBarang.getText().trim();
    String kategori = cmbKategori.getSelectedItem() != null
            ? cmbKategori.getSelectedItem().toString()
            : "";
    String satuan = cmbSatuan.getSelectedItem() != null
            ? cmbSatuan.getSelectedItem().toString()
            : "";
    String hargaStr = txtHarga.getText().trim();
    String jumlahStr = txtJumlah.getText().trim();

    // ==========================
    // VALIDASI INPUT
    // ==========================
    if (nama.isEmpty() || kategori.isEmpty() || satuan.isEmpty()
            || hargaStr.isEmpty() || jumlahStr.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "Semua field harus diisi!",
                "Peringatan",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int harga, jumlah;

    try {
        harga = Integer.parseInt(hargaStr.replaceAll("[^\\d]", ""));
        jumlah = Integer.parseInt(jumlahStr);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
                this,
                "Harga dan jumlah harus berupa angka!",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (jumlah <= 0) {
        JOptionPane.showMessageDialog(
                this,
                "Jumlah harus lebih dari 0!",
                "Peringatan",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // ==========================
    // PROSES DATABASE
    // ==========================
    try (Connection conn = Koneksi.getKoneksi()) {
        conn.setAutoCommit(false);

        boolean sudahAda = false;
        int jumlahLama = 0;

        // ==========================
        // 1️⃣ CEK BARANG (LOGIKA SAMA PERSIS SEPERTI MAU KAMU)
        // ==========================
        String cekSql = """
            SELECT jumlah
            FROM detail_transaksi
            WHERE nama_barang = ?
              AND kategori = ?
              AND satuan = ?
              AND harga = ?
        """;

        try (PreparedStatement psCek = conn.prepareStatement(cekSql)) {
            psCek.setString(1, nama);
            psCek.setString(2, kategori);
            psCek.setString(3, satuan);
            psCek.setInt(4, harga);

            try (ResultSet rs = psCek.executeQuery()) {
                if (rs.next()) {
                    jumlahLama = rs.getInt("jumlah");
                    sudahAda = true;
                }
            }
        }

        if (sudahAda) {
            // ==========================
            // 2️⃣ UPDATE JUMLAH & TOTAL
            // ==========================
            int jumlahBaru = jumlahLama + jumlah;
            int totalBaru = jumlahBaru * harga;

            String updateSql = """
                UPDATE detail_transaksi
                SET jumlah = ?, total = ?
                WHERE nama_barang = ?
                  AND kategori = ?
                  AND satuan = ?
                  AND harga = ?
            """;

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setInt(1, jumlahBaru);
                psUpdate.setInt(2, totalBaru);
                psUpdate.setString(3, nama);
                psUpdate.setString(4, kategori);
                psUpdate.setString(5, satuan);
                psUpdate.setInt(6, harga);
                psUpdate.executeUpdate();
            }

        } else {
            // ==========================
            // 3️⃣ INSERT BARANG BARU
            // ==========================
            int total = harga * jumlah;

            String insertSql = """
                INSERT INTO detail_transaksi
                (kode_barang, nama_barang, kategori, harga, satuan, jumlah, total)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setString(1, kode);
                psInsert.setString(2, nama);
                psInsert.setString(3, kategori);
                psInsert.setInt(4, harga);
                psInsert.setString(5, satuan);
                psInsert.setInt(6, jumlah);
                psInsert.setInt(7, total);
                psInsert.executeUpdate();
            }

            // kode barang hanya naik jika INSERT
            KodeBarang++;
            txtKodeBarang.setText(generateKodeBarang());
        }

        conn.commit();

        // ==========================
        // REFRESH UI
        // ==========================
        loadDetailTransaksi();
        resetInputBarang();
        aturFieldAwal();

        JOptionPane.showMessageDialog(
                this,
                "Barang berhasil diproses!",
                "Sukses",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                "Gagal memproses barang:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
    int row = tblTransaksi.getSelectedRow();
    if (row < 0) {
        JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Apakah yakin ingin menghapus data ini?",
        "Konfirmasi Hapus",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) return;

    Connection conn = null;

    try {
        conn = Koneksi.getKoneksi();
        conn.setAutoCommit(false);

        String kode = tblTransaksi.getValueAt(row, 0).toString();

        try (PreparedStatement ps =
             conn.prepareStatement("DELETE FROM detail_transaksi WHERE kode_barang = ?")) {

            ps.setString(1, kode);
            int affected = ps.executeUpdate();

            if (affected == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan!");
                return;
            }
        }

        // Update KodeBarang
        String sqlLast = "SELECT kode_barang FROM detail_transaksi ORDER BY id_detail DESC LIMIT 1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlLast)) {

            if (rs.next()) {
                String lastKode = rs.getString("kode_barang");
                int lastNumber = Integer.parseInt(lastKode.replaceAll("[^0-9]", ""));
                KodeBarang = lastNumber + 1;
            } else {
                KodeBarang = 1;
            }
        }

        // Reset AUTO_INCREMENT jika kosong
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM detail_transaksi")) {

            if (rs.next() && rs.getInt(1) == 0) {
                st.execute("ALTER TABLE detail_transaksi AUTO_INCREMENT = 1");
                KodeBarang = 1;
            }
        }

        conn.commit();

        // ✔ HAPUS DARI TABLE MODEL (SEKALI SAJA)
        model.removeRow(row);
        
        if (model.getRowCount() == 0) {
        resetFieldPembayaran();   // ⬅️ INI INTINYA
    }

        txtKodeBarang.setText(generateKodeBarang());
        resetInputBarang();
        btnStruk.setEnabled(false);
        tblTransaksi.clearSelection();
        hitungTotalBayar();

        JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");

    } catch (Exception e) {
        try {
            if (conn != null) conn.rollback(); // ✔ rollback BENAR
        } catch (Exception ex) {}

        JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage());
        e.printStackTrace();

    } finally {
        try {
            if (conn != null) conn.close();
        } catch (Exception e) {}
    }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnStrukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStrukActionPerformed
       try {
        if (tblTransaksi.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada transaksi!");
            return;
        }

        int totalBayar = Integer.parseInt(
                lblTotalBayar.getText()
                        .replace("Rp", "")
                        .replace(".", "")
                        .trim()
        );

        int bayar = Integer.parseInt(
                txtBayar.getText().replaceAll("[^\\d]", "")
        );

        if (bayar < totalBayar) {
            JOptionPane.showMessageDialog(this, "Uang bayar kurang!");
            return;
        }

        int kembalian = bayar - totalBayar;

        // ===== SIMPAN DATA FINAL (INI PENTING) =====
        finalTotalBayar = totalBayar;
        finalBayar = bayar;
        finalKembalian = kembalian;
        finalDiskon = txtDiskon.getText().isEmpty() ? "0%" : txtDiskon.getText();
        finalJenisBayar = cmbJenisBayar.getSelectedItem().toString();

        // ===== STATUS =====
        sudahBayar = true;
        btnStruk.setEnabled(true);
        tampilkanStruk();

       

        // ===== RESET INPUT (BOLEH, TIDAK MASALAH) =====
        txtBayar.setText("");
        txtDiskon.setText("");
        txtKembalian.setText("");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Input pembayaran tidak valid!");
    }
    }//GEN-LAST:event_btnStrukActionPerformed

    private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBayarActionPerformed
        try {
        // Total bayar SUDAH FINAL (sudah diskon)
        int totalBayar = Integer.parseInt(
            lblTotalBayar.getText()
                .replace("Rp", "")
                .replace(".", "")
                .trim()
        );

        // Bayar (format Rp)
        int bayar = Integer.parseInt(
            txtBayar.getText().replaceAll("[^\\d]", "")
        );

        if (bayar < totalBayar) {
            JOptionPane.showMessageDialog(this, "Uang bayar kurang!");
            return;
        }

        int kembalian = bayar - totalBayar;
        txtKembalian.setText(formatRupiah(kembalian));

        // Validasi tabel
        if (tblTransaksi.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada transaksi!");
            return;
        }

        
        JOptionPane.showMessageDialog(this, "Pembayaran berhasil");

        // flag internal
        sudahBayar = true;
        btnStruk.setEnabled(true);
        

       

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Input pembayaran tidak valid!");
    }
    }//GEN-LAST:event_btnBayarActionPerformed

    private void txtBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBayarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBayarActionPerformed

    private void txtNamaBarangKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNamaBarangKeyTyped
        char c = evt.getKeyChar();

    // Izinkan huruf, spasi, dan backspace
    if (!Character.isLetter(c) && c != ' ' && c != '\b') {
        evt.consume(); // BLOK INPUT
    }
    }//GEN-LAST:event_txtNamaBarangKeyTyped

    private void txtHargaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtHargaKeyTyped
         char c = evt.getKeyChar();

    // Hanya izinkan angka & backspace
    if (!Character.isDigit(c) && c != '\b') {
        evt.consume(); // blok input
    }
    }//GEN-LAST:event_txtHargaKeyTyped

    private void txtHargaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtHargaKeyReleased
         if (!txtHarga.getText().isEmpty()) {
        cmbSatuan.setEnabled(true);
    }
        
        String text = txtHarga.getText();

    // Hapus semua selain angka
    text = text.replaceAll("[^\\d]", "");

    if (text.isEmpty()) {
        txtHarga.setText("");
        return;
    }

    try {
        long value = Long.parseLong(text);

        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        rupiah.setMaximumFractionDigits(0);

        txtHarga.setText(rupiah.format(value));

    } catch (NumberFormatException e) {
        // abaikan error
    }
    }//GEN-LAST:event_txtHargaKeyReleased

    private void txtJumlahKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJumlahKeyTyped
        char c = evt.getKeyChar();

    // Hanya izinkan angka dan backspace
    if (!Character.isDigit(c) && c != '\b') {
        evt.consume(); // BLOK INPUT
    }
    }//GEN-LAST:event_txtJumlahKeyTyped

    private void txtDiskonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDiskonKeyReleased
         hitungTotalBayar();
    }//GEN-LAST:event_txtDiskonKeyReleased

    private void txtDiskonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDiskonKeyTyped
        char c = evt.getKeyChar();

    if (!Character.isDigit(c) && c != '%' && c != '\b') {
        evt.consume();
    }

    // % hanya boleh satu
    if (c == '%' && txtDiskon.getText().contains("%")) {
        evt.consume();
    }
    }//GEN-LAST:event_txtDiskonKeyTyped

    private void txtNamaBarangKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNamaBarangKeyReleased
    if (!txtNamaBarang.getText().trim().isEmpty()) {
        cmbKategori.setEnabled(true);
    } else {
        cmbKategori.setEnabled(false);
        txtHarga.setEnabled(false);
        cmbSatuan.setEnabled(false);
        txtJumlah.setEnabled(false);
    }
    }//GEN-LAST:event_txtNamaBarangKeyReleased

    private void cmbKategoriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbKategoriItemStateChanged
      if (cmbKategori.getSelectedIndex() >= 0) {
        txtHarga.setEnabled(true);
    }
    }//GEN-LAST:event_cmbKategoriItemStateChanged

    private void txtJumlahKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJumlahKeyReleased
        if (cmbSatuan.getSelectedIndex() >= 0) {
        txtJumlah.setEnabled(true);
    }
    }//GEN-LAST:event_txtJumlahKeyReleased

    private void cmbSatuanItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSatuanItemStateChanged
       if (cmbSatuan.isEnabled() && cmbSatuan.getSelectedIndex() >= 0) {
        txtJumlah.setEnabled(true);
        txtJumlah.requestFocus();
    }
    }//GEN-LAST:event_cmbSatuanItemStateChanged

    private void txtJumlahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJumlahKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {

        // Validasi terakhir sebelum tambah
        if (txtNamaBarang.getText().isEmpty() ||
            txtHarga.getText().isEmpty() ||
            txtJumlah.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Data belum lengkap!");
            return;
        }

        // ⬅️ ENTER = KLIK TOMBOL TAMBAH
        btnTambah.doClick();
    }
    }//GEN-LAST:event_txtJumlahKeyPressed

    private void txtKembalianKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtKembalianKeyReleased
       hitungKembalianOtomatis();
    }//GEN-LAST:event_txtKembalianKeyReleased

    private void txtBayarKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBayarKeyTyped
       char c = evt.getKeyChar();

    if (!Character.isDigit(c) && c != '\b') {
        evt.consume();
    }
    }//GEN-LAST:event_txtBayarKeyTyped

    private void txtBayarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBayarKeyReleased
    String text = txtBayar.getText();

    // Ambil angka saja
    text = text.replaceAll("[^\\d]", "");

    if (text.isEmpty()) {
        txtBayar.setText("");
        txtKembalian.setText("");
        return;
    }

    try {
        long value = Long.parseLong(text);

        NumberFormat rupiah =
        NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        rupiah.setMaximumFractionDigits(0);

        // Set kembali ke txtBayar
        txtBayar.setText(rupiah.format(value));

        // ⬅️ Hitung kembalian otomatis
        hitungKembalianOtomatis();

    } catch (NumberFormatException e) {
        // abaikan
    }
    }//GEN-LAST:event_txtBayarKeyReleased

    private void tblTransaksiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblTransaksiMouseClicked
        if (sudahBayar) {
        btnStruk.setEnabled(true);
    } else {
        btnStruk.setEnabled(false);
    }
    }//GEN-LAST:event_tblTransaksiMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
      if (sudahBayar) {
        JOptionPane.showMessageDialog(this,
            "Transaksi sudah dibayar, tidak bisa diedit!",
            "Akses Ditolak",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int row = tblTransaksi.getSelectedRow();
    if (row < 0) {
        JOptionPane.showMessageDialog(this, "Pilih data yang akan diedit!");
        return;
    }

    // SIMPAN KODE BARANG LAMA (UNTUK WHERE UPDATE)
    kodeBarangEdit = tblTransaksi.getValueAt(row, 0).toString();

    // ISI FORM
    txtKodeBarang.setText(kodeBarangEdit);
    txtNamaBarang.setText(tblTransaksi.getValueAt(row, 1).toString());
    cmbKategori.setSelectedItem(tblTransaksi.getValueAt(row, 2).toString());
    txtHarga.setText(tblTransaksi.getValueAt(row, 3).toString());
    cmbSatuan.setSelectedItem(tblTransaksi.getValueAt(row, 4).toString());
    txtJumlah.setText(tblTransaksi.getValueAt(row, 5).toString());

    // 🔒 KODE BARANG TIDAK BOLEH DIUBAH
    txtKodeBarang.setEnabled(false);

    // FIELD LAIN BOLEH DIUBAH
    txtNamaBarang.setEnabled(true);
    cmbKategori.setEnabled(true);
    txtHarga.setEnabled(true);
    cmbSatuan.setEnabled(true);
    txtJumlah.setEnabled(true);

    // STATUS MODE EDIT
    isEdit = true;

    btnOke.setEnabled(true);
    txtNamaBarang.requestFocus();

    }//GEN-LAST:event_btnEditActionPerformed

    private void btnOkeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkeActionPerformed
     if (kodeBarangEdit == null) {
        JOptionPane.showMessageDialog(this, "Tidak ada data yang diedit!");
        return;
    }

    if (txtNamaBarang.getText().trim().isEmpty()
        || txtHarga.getText().trim().isEmpty()
        || txtJumlah.getText().trim().isEmpty()) {

        JOptionPane.showMessageDialog(this, "Semua field wajib diisi!");
        return;
    }

    try (Connection conn = Koneksi.getKoneksi()) {

        String nama = txtNamaBarang.getText();
        String kategori = cmbKategori.getSelectedItem().toString();
        String satuan = cmbSatuan.getSelectedItem().toString();

        int harga = Integer.parseInt(
            txtHarga.getText().replaceAll("[^\\d]", "")
        );
        int jumlah = Integer.parseInt(txtJumlah.getText());

        if (harga <= 0 || jumlah <= 0) {
            JOptionPane.showMessageDialog(this, "Harga dan jumlah harus > 0!");
            return;
        }

        int total = harga * jumlah;

        String sql = """
            UPDATE detail_transaksi
            SET 
                nama_barang = ?,
                kategori = ?,
                harga = ?,
                satuan = ?,
                jumlah = ?,
                total = ?
            WHERE kode_barang = ?
        """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, nama);
        ps.setString(2, kategori);
        ps.setInt(3, harga);
        ps.setString(4, satuan);
        ps.setInt(5, jumlah);
        ps.setInt(6, total);
        ps.setString(7, kodeBarangEdit);

        ps.executeUpdate();

        // REFRESH DATA
        loadDetailTransaksi();
        hitungTotalBayar();

        // RESET TOTAL FORM & STATUS
        resetInputBarang();
        btnOke.setEnabled(false);
        kodeBarangEdit = null;
        isEdit = false;
        
        //WAJIB generate kode baru
        txtKodeBarang.setText(generateKodeBarang());

        sudahBayar = false;
        btnStruk.setEnabled(false);

        JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal update: " + e.getMessage());
        e.printStackTrace();
    }

    }//GEN-LAST:event_btnOkeActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Transaksi().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBayar;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnOke;
    private javax.swing.JButton btnStruk;
    private javax.swing.JButton btnTambah;
    private javax.swing.JComboBox<String> cmbJenisBayar;
    private javax.swing.JComboBox<String> cmbKategori;
    private javax.swing.JComboBox<String> cmbSatuan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblAlamat;
    private javax.swing.JLabel lblTotalBayar;
    private javax.swing.JLabel lblWaktu;
    private javax.swing.JTable tblTransaksi;
    private javax.swing.JTextField txtBayar;
    private javax.swing.JTextField txtDiskon;
    private javax.swing.JTextField txtHarga;
    private javax.swing.JTextField txtJumlah;
    private javax.swing.JTextField txtKembalian;
    private javax.swing.JTextField txtKodeBarang;
    private javax.swing.JTextField txtNamaBarang;
    // End of variables declaration//GEN-END:variables
}
