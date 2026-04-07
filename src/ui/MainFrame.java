package ui;

import service.AccountService;
import service.BookService;
import service.BorrowService;
import service.ReaderService;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final AccountService accountService;
    private final ReaderService readerService;
    private final BookService bookService;
    private final BorrowService borrowService;

    private final String currentUser;

    private JPanel contentPanel;

    public MainFrame(AccountService accountService, ReaderService readerService, BookService bookService, BorrowService borrowService, String currentUser) throws HeadlessException {
        this.accountService = accountService;
        this.readerService = readerService;
        this.bookService = bookService;
        this.borrowService = borrowService;
        this.currentUser = currentUser;

        initUI();
    }

    private void initUI() {
        setTitle("Hệ Thống Quản Lý Thư Viện");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);


        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);


        contentPanel = new JPanel(new BorderLayout());
        showPlaceholder("Chào mừng, " + currentUser + "!\nChọn chức năng từ menu bên trái.");
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }


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

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        sidebar.setPreferredSize(new Dimension(180, 0));


        sidebar.add(sidebarGroup("ĐỘC GIẢ"));
        sidebar.add(sidebarBtn("Quản lý độc giả", () -> swapPanel(new ReaderPanel(readerService))));


        sidebar.add(sidebarGroup("SÁCH"));
        sidebar.add(sidebarBtn("Quản lý sách sách", () -> swapPanel(new BookPanel(bookService))));

        sidebar.add(sidebarGroup("PHIẾU MƯỢN / TRẢ"));
        sidebar.add(sidebarBtn("Lập phiếu mượn sách", () -> swapPanel(new BorrowPanel(readerService, bookService, borrowService))));
        sidebar.add(sidebarBtn("Lập phiếu trả sách",        () -> swapPanel(new ReturnPanel(readerService, bookService, borrowService))));

        sidebar.add(sidebarGroup("CÁC THỐNG KÊ CƠ BẢN"));
        sidebar.add(sidebarBtn("Xem thống kê", () -> swapPanel(
                new StatisticsPanel(bookService, readerService, borrowService))));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }


    private JLabel sidebarGroup(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(Color.GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }


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


    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame(accountService, readerService, bookService, borrowService);
        }
    }

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