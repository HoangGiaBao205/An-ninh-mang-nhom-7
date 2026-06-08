import javax.swing.*;
import java.awt.*;
import java.io.File;

public class DSAApp extends JFrame {
    private DSAAlgorithm dsaAlgo;
    private JLabel lblStatus;
    
    private JTextArea txtDataText, txtSigText;
    
    private JTextField txtFilePath;
    private JTextArea txtSigFile;
    private File selectedFile;

    public DSAApp() {
        dsaAlgo = new DSAAlgorithm();
        
        setTitle("Ứng Dụng Chữ Ký Số DSA - Phiên Bản Pro");
        setSize(780, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBorder(BorderFactory.createTitledBorder("Quản Lý Cặp Khóa (Key Management)"));
        
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnGenKey = new JButton("Tạo Khóa Mới");
        JButton btnSaveKey = new JButton("Lưu Khóa Ra Thư Mục");
        JButton btnLoadKey = new JButton("Tải Khóa Từ Thư Mục");
        
        btnGenKey.setBackground(new Color(220, 240, 255));
        
        panelButtons.add(btnGenKey);
        panelButtons.add(btnSaveKey);
        panelButtons.add(btnLoadKey);

        lblStatus = new JLabel("Trạng thái: Sẵn sàng (Vui lòng Tạo hoặc Tải khóa để bắt đầu)");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 13));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        panelTop.add(panelButtons, BorderLayout.NORTH);
        panelTop.add(lblStatus, BorderLayout.SOUTH);
        add(panelTop, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));
        tabbedPane.addTab("Ký & Xác Thực Văn Bản", createTextPanel());
        tabbedPane.addTab("Ký & Xác Thực Tệp Tin (File)", createFilePanel());
        add(tabbedPane, BorderLayout.CENTER);

        btnGenKey.addActionListener(e -> {
            try {
                dsaAlgo.generateKeys();
                setStatus("Đã tạo cặp khóa (Public/Private) mới thành công!", Color.BLUE);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnSaveKey.addActionListener(e -> {
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Chọn thư mục để lưu khóa");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    dsaAlgo.savePublicKey(path + File.separator + "public.key");
                    dsaAlgo.savePrivateKey(path + File.separator + "private.key");
                    setStatus("Đã lưu public.key và private.key vào: " + path, new Color(0, 153, 0));
                }
            } catch (Exception ex) { showError("Lỗi khi lưu: " + ex.getMessage()); }
        });

        btnLoadKey.addActionListener(e -> {
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Chọn thư mục chứa public.key và private.key");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    File pubFile = new File(path, "public.key");
                    File privFile = new File(path, "private.key");
                    
                    if (!pubFile.exists() || !privFile.exists()) {
                        throw new Exception("Không tìm thấy đủ 2 file public.key và private.key trong thư mục này!");
                    }

                    dsaAlgo.loadPublicKey(pubFile.getAbsolutePath());
                    dsaAlgo.loadPrivateKey(privFile.getAbsolutePath());
                    setStatus("Đã tải cặp khóa thành công! Hệ thống sẵn sàng.", new Color(0, 153, 0));
                }
            } catch (Exception ex) { showError("Lỗi khi tải khóa: " + ex.getMessage()); }
        });
    }

    private JPanel createTextPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelCenter = new JPanel(new GridLayout(2, 1, 10, 10));
        
        JPanel pnlData = new JPanel(new BorderLayout());
        pnlData.add(new JLabel("Nội dung văn bản cần ký:"), BorderLayout.NORTH);
        txtDataText = new JTextArea();
        txtDataText.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtDataText.setLineWrap(true);
        pnlData.add(new JScrollPane(txtDataText), BorderLayout.CENTER);

        JPanel pnlSig = new JPanel(new BorderLayout());
        pnlSig.add(new JLabel("Chữ ký số (Base64):"), BorderLayout.NORTH);
        txtSigText = new JTextArea();
        txtSigText.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtSigText.setLineWrap(true);
        pnlSig.add(new JScrollPane(txtSigText), BorderLayout.CENTER);

        panelCenter.add(pnlData);
        panelCenter.add(pnlSig);
        panel.add(panelCenter, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel();
        JButton btnSign = new JButton("Ký Văn Bản");
        JButton btnVerify = new JButton("Xác Thực Văn Bản");
        panelBottom.add(btnSign);
        panelBottom.add(btnVerify);
        panel.add(panelBottom, BorderLayout.SOUTH);

        btnSign.addActionListener(e -> {
            try {
                if (txtDataText.getText().isEmpty()) throw new Exception("Vui lòng nhập văn bản!");
                String signature = dsaAlgo.signText(txtDataText.getText());
                txtSigText.setText(signature);
                setStatus("Đã ký văn bản thành công!", Color.BLUE);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnVerify.addActionListener(e -> {
            try {
                if (txtSigText.getText().isEmpty()) throw new Exception("Thiếu chữ ký!");
                boolean isValid = dsaAlgo.verifyText(txtDataText.getText(), txtSigText.getText());
                if (isValid) setStatus("THÀNH CÔNG - Văn bản nguyên vẹn, chữ ký đúng!", new Color(0, 153, 0));
                else setStatus("THẤT BẠI - Dữ liệu đã bị sửa đổi hoặc chữ ký sai!", Color.RED);
            } catch (Exception ex) { setStatus("LỖI XÁC THỰC: Khóa hoặc chữ ký không hợp lệ!", Color.RED); }
        });

        return panel;
    }

    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelTop = new JPanel(new BorderLayout(5, 5));
        panelTop.add(new JLabel("Đường dẫn File:"), BorderLayout.WEST);
        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        JButton btnBrowse = new JButton("Chọn File...");
        panelTop.add(txtFilePath, BorderLayout.CENTER);
        panelTop.add(btnBrowse, BorderLayout.EAST);

        JPanel panelCenter = new JPanel(new BorderLayout());
        panelCenter.add(new JLabel("Chữ ký số của File (Base64):"), BorderLayout.NORTH);
        txtSigFile = new JTextArea();
        txtSigFile.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtSigFile.setLineWrap(true);
        panelCenter.add(new JScrollPane(txtSigFile), BorderLayout.CENTER);

        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(panelCenter, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel();
        JButton btnSignFile = new JButton("Ký Tệp Tin");
        JButton btnVerifyFile = new JButton("Xác Thực Tệp Tin");
        panelBottom.add(btnSignFile);
        panelBottom.add(btnVerifyFile);
        panel.add(panelBottom, BorderLayout.SOUTH);

        btnBrowse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                txtFilePath.setText(selectedFile.getAbsolutePath());
                txtSigFile.setText("");
                setStatus("Đã chọn file: " + selectedFile.getName(), Color.BLACK);
            }
        });

        btnSignFile.addActionListener(e -> {
            try {
                if (selectedFile == null) throw new Exception("Vui lòng chọn file trước!");
                String signature = dsaAlgo.signFile(selectedFile);
                txtSigFile.setText(signature);
                setStatus("Đã ký tệp tin thành công!", Color.BLUE);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnVerifyFile.addActionListener(e -> {
            try {
                if (selectedFile == null) throw new Exception("Vui lòng chọn file cần xác thực!");
                if (txtSigFile.getText().isEmpty()) throw new Exception("Thiếu chữ ký!");
                
                boolean isValid = dsaAlgo.verifyFile(selectedFile, txtSigFile.getText());
                if (isValid) setStatus("THÀNH CÔNG - Tệp tin nguyên vẹn!", new Color(0, 153, 0));
                else setStatus("THẤT BẠI - Tệp tin hoặc chữ ký đã bị thay đổi!", Color.RED);
            } catch (Exception ex) { setStatus("LỖI XÁC THỰC: Khóa hoặc chữ ký không hợp lệ!", Color.RED); }
        });

        return panel;
    }

    private void setStatus(String message, Color color) {
        lblStatus.setText("Trạng thái: " + message);
        lblStatus.setForeground(color);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Cảnh báo / Lỗi", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        try {             UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf"); 
        } 
        catch (Exception e) { 
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
            }
        }
        
        SwingUtilities.invokeLater(() -> new DSAApp().setVisible(true));
    }
}