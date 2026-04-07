package ui;


import model.Reader;
import model.BorrowSlip;
import service.BookService;
import service.BorrowService;
import service.ReaderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


public class StatisticsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BookService   bookService;
    private final ReaderService readerService;
    private final BorrowService borrowService;

    public StatisticsPanel(BookService bookService,
                           ReaderService readerService,
                           BorrowService borrowService) {
        this.bookService   = bookService;
        this.readerService = readerService;
        this.borrowService = borrowService;

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tổng quan sách",        buildBookSummaryTab());
        tabs.addTab("Sách theo thể loại",    buildBookByGenreTab());
        tabs.addTab("Tổng quan độc giả",     buildReaderSummaryTab());
        tabs.addTab("Độc giả theo giới tính", buildReaderByGenderTab());
        tabs.addTab("Sách đang mượn",        buildBorrowingTab());
        tabs.addTab("Độc giả trễ hạn",       buildLateReadersTab());
        
        JButton btnRefresh = new JButton("↺ Làm mới tất cả thống kê");
        btnRefresh.addActionListener(e -> {
            int selected = tabs.getSelectedIndex();
            tabs.removeAll();
            tabs.addTab("Tổng quan sách",        buildBookSummaryTab());
            tabs.addTab("Sách theo thể loại",    buildBookByGenreTab());
            tabs.addTab("Tổng quan độc giả",     buildReaderSummaryTab());
            tabs.addTab("Độc giả theo giới tính", buildReaderByGenderTab());
            tabs.addTab("Sách đang mượn",        buildBorrowingTab());
            tabs.addTab("Độc giả trễ hạn",       buildLateReadersTab());
            tabs.setSelectedIndex(selected);
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(btnRefresh);

        add(topBar, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
    }

    private JPanel buildBookSummaryTab() {
        int totalTitles    = bookService.getAllBooks().size();
        int totalCopies    = bookService.getTotalBookCount();
        int borrowingCount = borrowService.getBorrowingBookCount();
        int availableCount = totalCopies - borrowingCount;

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 24, 12, 24);
        g.anchor = GridBagConstraints.WEST;

        addStatRow(p, g, 0, "Số đầu sách (title):",      String.valueOf(totalTitles));
        addStatRow(p, g, 1, "Tổng số quyển:",             String.valueOf(totalCopies));
        addStatRow(p, g, 2, "Số quyển đang được mượn:",   String.valueOf(borrowingCount));
        addStatRow(p, g, 3, "Số quyển còn trong thư viện:", String.valueOf(availableCount));

        return wrapWithTitle(p, "Thống kê tổng quan sách");
    }


    private JPanel buildBookByGenreTab() {
        Map<String, Integer> byGenre = bookService.countByGenre();

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Thể loại", "Số quyển"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        int total = 0;
        for (Map.Entry<String, Integer> entry : byGenre.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
            total += entry.getValue();
        }
        model.addRow(new Object[]{"──────────", "──────"});
        model.addRow(new Object[]{"Tổng cộng", total});

        JTable table = makeTable(model, new int[]{300, 100});
        return wrapWithTitle(new JScrollPane(table), "Số sách theo thể loại");
    }


    private JPanel buildReaderSummaryTab() {
        List<Reader> all    = readerService.getAllReaders();
        long total          = all.size();
        long active         = all.stream().filter(Reader::isCardValid).count();
        long expired        = total - active;

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 24, 12, 24);
        g.anchor = GridBagConstraints.WEST;

        addStatRow(p, g, 0, "Tổng số độc giả:",       String.valueOf(total));
        addStatRow(p, g, 1, "Thẻ còn hạn:",            String.valueOf(active));
        addStatRow(p, g, 2, "Thẻ hết hạn:",            String.valueOf(expired));

        return wrapWithTitle(p, "Thống kê tổng quan độc giả");
    }

    private JPanel buildReaderByGenderTab() {
        List<Reader> all = readerService.getAllReaders();

        Map<String, Long> byGender = new java.util.LinkedHashMap<>();
        for (Reader r : all) {
            byGender.merge(r.getGender(), 1L, Long::sum);
        }

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Giới tính", "Số độc giả"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        long total = 0;
        for (Map.Entry<String, Long> entry : byGender.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
            total += entry.getValue();
        }
        model.addRow(new Object[]{"──────────", "──────"});
        model.addRow(new Object[]{"Tổng cộng", total});

        JTable table = makeTable(model, new int[]{200, 120});
        return wrapWithTitle(new JScrollPane(table), "Số độc giả theo giới tính");
    }

    private JPanel buildBorrowingTab() {
        List<BorrowSlip> activeSlips = borrowService.getAllSlips().stream()
                .filter(s -> !s.isReturned())
                .toList();

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Mã phiếu", "Mã độc giả", "Họ tên", "Ngày mượn", "Hạn trả", "Số sách"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (BorrowSlip s : activeSlips) {
            Reader r = readerService.findById(s.getReaderId());
            String name = (r != null) ? r.getFullName() : "—";
            model.addRow(new Object[]{
                    s.getSlipId(),
                    s.getReaderId(),
                    name,
                    s.getBorrowDate().format(DATE_FMT),
                    s.getExpectedReturnDate().format(DATE_FMT),
                    s.getBookIsbns().size()
            });
        }

        JLabel lblTotal = new JLabel("Tổng số quyển đang mượn: "
                + borrowService.getBorrowingBookCount());
        lblTotal.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));

        JTable table = makeTable(model, new int[]{80, 90, 150, 90, 90, 70});
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(lblTotal, BorderLayout.SOUTH);

        return wrapWithTitle(p, "Sách đang được mượn");
    }


    private JPanel buildLateReadersTab() {
        List<BorrowSlip> lateSlips = borrowService.getLateSlips();

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Mã phiếu", "Mã độc giả", "Họ tên", "Hạn trả", "Trễ (ngày)", "Tiền phạt (đ)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (BorrowSlip s : lateSlips) {
            Reader r = readerService.findById(s.getReaderId());
            String name     = (r != null) ? r.getFullName() : "—";
            long   lateDays = java.time.temporal.ChronoUnit.DAYS.between(
                    s.getExpectedReturnDate(), java.time.LocalDate.now());
            long   fine     = lateDays * 5_000L;

            model.addRow(new Object[]{
                    s.getSlipId(),
                    s.getReaderId(),
                    name,
                    s.getExpectedReturnDate().format(DATE_FMT),
                    lateDays,
                    String.format("%,d", fine)
            });
        }

        JLabel lblTotal = new JLabel("Tổng số độc giả trễ hạn: "
                + borrowService.getLateReaders().size());
        lblTotal.setForeground(Color.RED);
        lblTotal.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));

        JTable table = makeTable(model, new int[]{80, 90, 150, 90, 80, 110});
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(lblTotal, BorderLayout.SOUTH);

        return wrapWithTitle(p, "Danh sách độc giả trễ hạn");
    }

    private void addStatRow(JPanel p, GridBagConstraints g, int row,
                            String label, String value) {
        g.gridx = 0; g.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 14f));
        p.add(lbl, g);

        g.gridx = 1;
        JLabel val = new JLabel(value);
        val.setFont(val.getFont().deriveFont(Font.BOLD, 18f));
        val.setForeground(new Color(0, 102, 204));
        p.add(val, g);
    }

    private JTable makeTable(DefaultTableModel model, int[] widths) {
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return table;
    }


    private JPanel wrapWithTitle(Component content, String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 14f));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        p.add(lbl,     BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }
}