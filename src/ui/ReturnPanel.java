package ui;

import model.Book;
import model.BorrowSlip;
import model.Reader;
import service.BookService;
import service.BorrowService;
import service.ReaderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class ReturnPanel extends JPanel {
    private static final DateTimeFormatter DATE_FMT    = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final long              FINE_PER_DAY = 5_000L;
    private static final double            LOST_RATE    = 2.0;

    private final ReaderService readerService;
    private final BookService   bookService;
    private final BorrowService borrowService;

    private BorrowSlip selectedSlip;

    private JTextField tfSearch;
    private JComboBox<String> cbSearchType;
    private JLabel lblSlipId, lblReaderId, lblReaderName,
            lblBorrowDate, lblExpectedReturn, lblStatus;

    private DefaultTableModel booksModel;
    private JTable booksTable;


    private JLabel lblLateFine, lblLostFine, lblTotalFine;

    public ReturnPanel(ReaderService readerService,
                       BookService bookService,
                       BorrowService borrowService) {
        this.readerService = readerService;
        this.bookService   = bookService;
        this.borrowService = borrowService;

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildSearchSection(), BorderLayout.NORTH);
        add(buildSlipSection(),   BorderLayout.CENTER);
        add(buildFineSection(),   BorderLayout.SOUTH);
    }


    private JPanel buildSearchSection() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBorder(BorderFactory.createTitledBorder("1. Tìm phiếu mượn"));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cbSearchType = new JComboBox<>(new String[]{
                "Tìm theo mã phiếu", "Tìm theo mã độc giả"
        });
        tfSearch = new JTextField(18);
        JButton btnFind = new JButton("Tìm");

        btnFind.addActionListener(e -> handleSearch());
        tfSearch.addActionListener(e -> handleSearch());

        bar.add(cbSearchType);
        bar.add(tfSearch);
        bar.add(btnFind);
        outer.add(bar, BorderLayout.CENTER);
        return outer;
    }

    private void handleSearch() {
        String keyword = tfSearch.getText().trim();
        if (keyword.isEmpty()) return;

        List<BorrowSlip> results;
        if (cbSearchType.getSelectedIndex() == 0) {

            BorrowSlip found = borrowService.findBySlipId(keyword);
            results = (found != null) ? List.of(found) : List.of();
        } else {

            results = borrowService.findUnreturnedByReaderId(keyword);
        }

        if (results.isEmpty()) {
            showError("Không tìm thấy phiếu mượn chưa trả.");
            clearSlipInfo();
            return;
        }

        if (results.size() == 1) {
            displaySlip(results.get(0));
        } else {
            BorrowSlip chosen = chooseSlip(results);
            if (chosen != null) displaySlip(chosen);
        }
    }


    private BorrowSlip chooseSlip(List<BorrowSlip> slips) {
        String[] options = slips.stream()
                .map(s -> s.getSlipId() + " — mượn " + s.getBorrowDate().format(DATE_FMT)
                        + " (" + s.getBookIsbns().size() + " sách)")
                .toArray(String[]::new);

        String chosen = (String) JOptionPane.showInputDialog(this,
                "Độc giả có nhiều phiếu chưa trả. Chọn phiếu cần xử lý:",
                "Chọn phiếu mượn", JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (chosen == null) return null;
        int idx = java.util.Arrays.asList(options).indexOf(chosen);
        return slips.get(idx);
    }


    private JPanel buildSlipSection() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBorder(BorderFactory.createTitledBorder("2. Thông tin phiếu mượn"));

        JPanel info = new JPanel(new GridLayout(2, 6, 10, 4));
        info.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));

        lblSlipId         = makeInfoLabel("—");
        lblReaderId       = makeInfoLabel("—");
        lblReaderName     = makeInfoLabel("—");
        lblBorrowDate     = makeInfoLabel("—");
        lblExpectedReturn = makeInfoLabel("—");
        lblStatus         = makeInfoLabel("—");

        info.add(makeFieldLabel("Mã phiếu:"));       info.add(lblSlipId);
        info.add(makeFieldLabel("Mã độc giả:"));     info.add(lblReaderId);
        info.add(makeFieldLabel("Họ tên:"));         info.add(lblReaderName);
        info.add(makeFieldLabel("Ngày mượn:"));      info.add(lblBorrowDate);
        info.add(makeFieldLabel("Hạn trả:"));        info.add(lblExpectedReturn);
        info.add(makeFieldLabel("Trạng thái:"));     info.add(lblStatus);


        booksModel = new DefaultTableModel(
                new String[]{"ISBN", "Tên sách", "Giá (đ)", "Tình trạng"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        booksTable = new JTable(booksModel);
        booksTable.setRowHeight(24);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Bình thường", "Mất sách"});
        booksTable.getColumnModel().getColumn(3)
                .setCellEditor(new DefaultCellEditor(statusCombo));
        booksModel.addTableModelListener(e -> recalculateFine());

        int[] widths = {110, 250, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            booksTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        outer.add(info, BorderLayout.NORTH);
        outer.add(new JScrollPane(booksTable), BorderLayout.CENTER);
        return outer;
    }

    private void displaySlip(BorrowSlip slip) {
        selectedSlip = slip;

        Reader reader = readerService.findById(slip.getReaderId());
        String readerName = (reader != null) ? reader.getFullName() : "Không tìm thấy";

        lblSlipId.setText(slip.getSlipId());
        lblReaderId.setText(slip.getReaderId());
        lblReaderName.setText(readerName);
        lblBorrowDate.setText(slip.getBorrowDate().format(DATE_FMT));
        lblExpectedReturn.setText(slip.getExpectedReturnDate().format(DATE_FMT));


        if (LocalDate.now().isAfter(slip.getExpectedReturnDate())) {
            long days = ChronoUnit.DAYS.between(slip.getExpectedReturnDate(), LocalDate.now());
            lblStatus.setText("⚠ Trễ " + days + " ngày");
            lblStatus.setForeground(Color.RED);
        } else {
            lblStatus.setText("✓ Đúng hạn");
            lblStatus.setForeground(new Color(0, 150, 0));
        }


        booksModel.setRowCount(0);
        for (String isbn : slip.getBookIsbns()) {
            Book book = bookService.findByIsbn(isbn);
            String title = (book != null) ? book.getTitle() : "(Không tìm thấy)";
            String price = (book != null) ? String.format("%,.0f", book.getPrice()) : "—";
            booksModel.addRow(new Object[]{isbn, title, price, "Bình thường"});
        }

        recalculateFine();
    }

    private void clearSlipInfo() {
        selectedSlip = null;
        lblSlipId.setText("—");
        lblReaderId.setText("—");
        lblReaderName.setText("—");
        lblBorrowDate.setText("—");
        lblExpectedReturn.setText("—");
        lblStatus.setText("—");
        lblStatus.setForeground(Color.BLACK);
        booksModel.setRowCount(0);
        lblLateFine.setText("0 đ");
        lblLostFine.setText("0 đ");
        lblTotalFine.setText("0 đ");
    }


    private JPanel buildFineSection() {
        JPanel outer = new JPanel(new BorderLayout(8, 0));
        outer.setBorder(BorderFactory.createTitledBorder("3. Tiền phạt & Xác nhận"));


        JPanel fineInfo = new JPanel(new GridLayout(3, 2, 8, 4));
        fineInfo.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        lblLateFine  = makeInfoLabel("0 đ");
        lblLostFine  = makeInfoLabel("0 đ");
        lblTotalFine = makeInfoLabel("0 đ");
        lblTotalFine.setForeground(Color.RED);

        fineInfo.add(makeFieldLabel("Phạt trễ hạn:"));  fineInfo.add(lblLateFine);
        fineInfo.add(makeFieldLabel("Phạt mất sách:")); fineInfo.add(lblLostFine);
        fineInfo.add(makeFieldLabel("Tổng phạt:"));     fineInfo.add(lblTotalFine);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));

        JButton btnConfirm = new JButton("✓ Xác nhận trả sách");
        btnConfirm.setFont(btnConfirm.getFont().deriveFont(Font.BOLD));
        btnConfirm.addActionListener(e -> handleConfirmReturn());

        JButton btnReset = new JButton("Nhập lại");
        btnReset.addActionListener(e -> clearSlipInfo());

        btnPanel.add(btnReset);
        btnPanel.add(btnConfirm);

        outer.add(fineInfo,  BorderLayout.CENTER);
        outer.add(btnPanel,  BorderLayout.EAST);
        return outer;
    }

    private void recalculateFine() {
        if (selectedSlip == null) return;


        long lateDays = ChronoUnit.DAYS.between(
                selectedSlip.getExpectedReturnDate(), LocalDate.now());
        long lateFine = (lateDays > 0) ? lateDays * FINE_PER_DAY : 0;


        long lostFine = 0;
        for (int i = 0; i < booksModel.getRowCount(); i++) {
            String status = (String) booksModel.getValueAt(i, 3);
            if ("Mất sách".equals(status)) {
                String isbn = (String) booksModel.getValueAt(i, 0);
                Book book = bookService.findByIsbn(isbn);
                if (book != null) {
                    lostFine += (long) (book.getPrice() * LOST_RATE);
                }
            }
        }

        long total = lateFine + lostFine;
        lblLateFine.setText(String.format("%,d đ", lateFine));
        lblLostFine.setText(String.format("%,d đ", lostFine));
        lblTotalFine.setText(String.format("%,d đ", total));
    }


    private void handleConfirmReturn() {
        if (selectedSlip == null) {
            showError("Vui lòng tìm phiếu mượn trước.");
            return;
        }

        List<String> lostIsbns = new java.util.ArrayList<>();
        for (int i = 0; i < booksModel.getRowCount(); i++) {
            if ("Mất sách".equals(booksModel.getValueAt(i, 3))) {
                lostIsbns.add((String) booksModel.getValueAt(i, 0));
            }
        }

        String msg = "Xác nhận trả phiếu " + selectedSlip.getSlipId() + "?\n"
                + "Sách mất: " + lostIsbns.size() + " quyển\n"
                + "Tổng tiền phạt: " + lblTotalFine.getText();

        int confirm = JOptionPane.showConfirmDialog(this, msg,
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            BorrowService.ReturnResult result = borrowService.returnBooks(
                    selectedSlip.getSlipId(), LocalDate.now(), lostIsbns);

            JOptionPane.showMessageDialog(this,
                    "Trả sách thành công!\n"
                            + String.format("Phạt trễ hạn : %,.0f đ%n", result.lateFee)
                            + String.format("Phạt mất sách: %,.0f đ%n", result.lostFee)
                            + String.format("Tổng thu     : %,.0f đ",   result.totalFee),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            clearSlipInfo();
            tfSearch.setText("");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }


    private JLabel makeInfoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        return lbl;
    }

    private JLabel makeFieldLabel(String text) {
        return new JLabel(text);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}