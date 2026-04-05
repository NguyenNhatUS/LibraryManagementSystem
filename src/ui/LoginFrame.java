package ui;

import service.AccountService;
import javax.swing.*;
import java.awt.*;


public class LoginFrame extends JFrame {
    private final AccountService accountService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;

    public LoginFrame(AccountService accountService) {
        this.accountService = accountService;
        initUI();
    }

    private void initUI() {
        setTitle("Thư Viện — Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ THƯ VIỆN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(lblTitle, gbc);

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Tên đăng nhập:"), gbc);

        txtUsername = new JTextField(18);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Mật khẩu:"), gbc);

        txtPassword = new JPasswordField(18);
        txtPassword.addActionListener(e -> handleLogin());
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(txtPassword, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setPreferredSize(new Dimension(120, 30));
        btnLogin.addActionListener(e -> handleLogin());

        btnRegister = new JButton("Tạo tài khoản");
        btnRegister.setPreferredSize(new Dimension(120, 30));
        btnRegister.addActionListener(e -> handleRegister());

        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 0, 0, 0);
        panel.add(btnPanel, gbc);

        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        if (accountService.login(username, password)) {
            dispose();
            new MainFrame(accountService, username);
        } else {
            showError("Sai tên đăng nhập hoặc mật khẩu.");
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    private void handleRegister() {
        JTextField tfNewUser = new JTextField(16);
        JPasswordField pfNewPass = new JPasswordField(16);
        JPasswordField pfConfirm = new JPasswordField(16);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Tên đăng nhập:"));
        form.add(tfNewUser);
        form.add(new JLabel("Mật khẩu:"));
        form.add(pfNewPass);
        form.add(new JLabel("Xác nhận mật khẩu:"));
        form.add(pfConfirm);

        int result = JOptionPane.showConfirmDialog(
                this, form,
                "Tạo tài khoản thủ thư",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        String newUser = tfNewUser.getText().trim();
        String newPass = new String(pfNewPass.getPassword());
        String confirm = new String(pfConfirm.getPassword());


        if (newUser.isEmpty() || newPass.isEmpty()) {
            showError("Tên đăng nhập và mật khẩu không được để trống.");
            return;
        }
        if (!newPass.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }

        boolean success = accountService.createAccount(newUser, newPass);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Tạo tài khoản thành công!\nBạn có thể đăng nhập ngay bây giờ.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtUsername.setText(newUser);
            txtPassword.requestFocus();
        } else {
            showError("Tên đăng nhập \"" + newUser + "\" đã tồn tại.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}