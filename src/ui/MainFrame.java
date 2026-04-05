package ui;

import service.AccountService;
import javax.swing.*;
import java.awt.*;


public class MainFrame extends JFrame {

    private final AccountService accountService;
    private final String currentUser;

    // Content area — swap panel vào đây
    private JPanel contentPanel;

    public MainFrame(AccountService accountService, String currentUser) {
        this.accountService = accountService;
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setTitle("Hệ Thống Quản Lý Thư Viện");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── Header ───────────────────────────────────────────────────────────
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ── Sidebar ──────────────────────────────────────────────────────────
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Content (placeholder) ─────────────────────────────────────────────
        contentPanel = new JPanel(new BorderLayout());
        showPlaceholder("Chào mừng, " + currentUser + "!\nChọn chức năng từ menu bên trái.");
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(51, 102, 153));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel lblTitle = new JLabel("THƯ VIỆN — HỆ THỐNG QUẢN LÝ");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblUser = new JLabel("Thủ thư: " + currentUser);
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.addActionListener(e -> handleLogout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(lblUser);
        right.add(btnLogout);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        sidebar.setPreferredSize(new Dimension(180, 0));

        // Nhóm menu
        sidebar.add(sidebarGroup("ĐỘC GIẢ"));
        sidebar.add(sidebarBtn("Danh sách độc giả",    () -> swapPanel(new PlaceholderPanel("ReaderPanel — sắp có"))));
        sidebar.add(sidebarBtn("Thêm độc giả",         () -> swapPanel(new PlaceholderPanel("AddReaderPanel — sắp có"))));

        sidebar.add(sidebarGroup("SÁCH"));
        sidebar.add(sidebarBtn("Danh sách sách",       () -> swapPanel(new PlaceholderPanel("BookPanel — sắp có"))));
        sidebar.add(sidebarBtn("Thêm sách",            () -> swapPanel(new PlaceholderPanel("AddBookPanel — sắp có"))));

        sidebar.add(sidebarGroup("PHIẾU MƯỢN / TRẢ"));
        sidebar.add(sidebarBtn("Lập phiếu mượn",       () -> swapPanel(new PlaceholderPanel("BorrowPanel — sắp có"))));
        sidebar.add(sidebarBtn("Lập phiếu trả",        () -> swapPanel(new PlaceholderPanel("ReturnPanel — sắp có"))));

        sidebar.add(sidebarGroup("THỐNG KÊ"));
        sidebar.add(sidebarBtn("Xem thống kê",         () -> swapPanel(new PlaceholderPanel("StatisticsPanel — sắp có"))));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    /** Label nhóm nhỏ trong sidebar */
    private JLabel sidebarGroup(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(Color.GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    /** Nút menu trong sidebar */
    private JButton sidebarBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(240, 240, 240));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());

        // Hover effect nhẹ
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(210, 225, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(240, 240, 240));
            }
        });

        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    // ── Swap content ─────────────────────────────────────────────────────────
    /** Thay panel trong vùng content */
    public void swapPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPlaceholder(String message) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>",
                SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setForeground(Color.GRAY);
        p.add(lbl);
        swapPanel(p);
    }

    // ── Đăng xuất ────────────────────────────────────────────────────────────
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame(accountService);
        }
    }

    // ── Inner placeholder panel (xóa khi có panel thật) ──────────────────────
    private static class PlaceholderPanel extends JPanel {
        PlaceholderPanel(String msg) {
            setLayout(new GridBagLayout());
            JLabel lbl = new JLabel(msg, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.ITALIC, 13));
            lbl.setForeground(Color.GRAY);
            add(lbl);
        }
    }
}