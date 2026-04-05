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
import java.util.ArrayList;
import java.util.List;

/**
 * BorrowPanel — lập phiếu mượn sách.
 *
 * Layout:
 *  ┌──────────────────────────────────────────────────────────┐
 *  │  Thông tin độc giả (tìm theo ID / CMND)                  │  ← top
 *  ├──────────────────────────────────────────────────────────┤
 *  │  Chọn sách: [tìm ISBN / tên] → bảng sách đã chọn         │  ← center
 *  ├──────────────────────────────────────────────────────────┤
 *  │  [Lập phiếu mượn]                                        │  ← bottom
 *  └──────────────────────────────────────────────────────────┘
 *
 * Validate:
 *  - Thẻ độc giả không hết hạn
 *  - Sách phải còn availableCount > 0
 *  - Không mượn trùng ISBN trong cùng 1 phiếu
 *  - Phải chọn ít nhất 1 sách
 */
public class BorrowPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReaderService readerService;
    private final BookService   bookService;
    private final BorrowService borrowService;

    // ── Thông tin độc giả ─────────────────────────────────────────────────────
    private JTextField tfReaderSearch;
    private JComboBox<String> cbReaderSearchType;
    private JLabel lblReaderId, lblReaderName, lblIdCard,
            lblExpiry, lblExpiryStatus;
    private Reader selectedReader;

    // ── Danh sách sách đã chọn ────────────────────────────────────────────────
    private DefaultTableModel selectedBooksModel;
    private JTable selectedBooksTable;
    private final List<Book> selectedBooks = new ArrayList<>();

    // ── Tìm sách ──────────────────────────────────────────────────────────────
    private JTextField tfBookSearch;
    private JComboBox<String> cbBookSearchType;

    public BorrowPanel(ReaderService readerService,
                       BookService bookService,
                       BorrowService borrowService) {
        this.readerService = readerService;
        this.bookService   = bookService;
        this.borrowService = borrowService;

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildReaderSection(),  BorderLayout.NORTH);
        add(buildBookSection(),    BorderLayout.CENTER);
        add(buildBottomBar(),      BorderLayout.SOUTH);
    }

    // =========================================================================
    // SECTION 1 — Thông tin độc giả
    // =========================================================================
    private JPanel buildReaderSection() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBorder(BorderFactory.createTitledBorder("1. Thông tin độc giả"));

        // ── Tìm độc giả ──────────────────────────────────────────────────────
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cbReaderSearchType = new JComboBox<>(new String[]{
                "Tìm theo mã độc giả", "Tìm theo CMND/CCCD"
        });
        tfReaderSearch = new JTextField(18);
        JButton btnFind = new JButton("Tìm");
        btnFind.addActionListener(e -> handleFindReader());
        tfReaderSearch.addActionListener(e -> handleFindReader());

        searchBar.add(cbReaderSearchType);
        searchBar.add(tfReaderSearch);
        searchBar.add(btnFind);

        // ── Thông tin hiển thị ────────────────────────────────────────────────
        JPanel info = new JPanel(new GridLayout(2, 4, 12, 4));
        info.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));

        lblReaderId    = makeInfoLabel("—");
        lblReaderName  = makeInfoLabel("—");
        lblIdCard      = makeInfoLabel("—");
        lblExpiry      = makeInfoLabel("—");
        lblExpiryStatus = makeInfoLabel("—");

        info.add(makeFieldLabel("Mã độc giả:"));  info.add(lblReaderId);
        info.add(makeFieldLabel("Họ tên:"));      info.add(lblReaderName);
        info.add(makeFieldLabel("CMND/CCCD:"));   info.add(lblIdCard);
        info.add(makeFieldLabel("Ngày hết hạn thẻ:")); info.add(lblExpiry);

        outer.add(searchBar, BorderLayout.NORTH);
        outer.add(info,      BorderLayout.CENTER);
        return outer;
    }

    private void handleFindReader() {
        String keyword = tfReaderSearch.getText().trim();
        if (keyword.isEmpty()) return;

        Reader found = cbReaderSearchType.getSelectedIndex() == 0
                ? readerService.findById(keyword)
                : readerService.findByIdCard(keyword);

        if (found == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy độc giả.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            clearReaderInfo();
            return;
        }

        selectedReader = found;
        lblReaderId.setText(found.getReaderId());
        lblReaderName.setText(found.getFullName());
        lblIdCard.setText(found.getIdCard());
        lblExpiry.setText(found.getCardExpiryDate().format(DATE_FMT));

        // Kiểm tra hạn thẻ
        if (found.getCardExpiryDate().isBefore(LocalDate.now())) {
            lblExpiryStatus.setText("⚠ THẺ HẾT HẠN");
            lblExpiryStatus.setForeground(Color.RED);
        } else {
            lblExpiryStatus.setText("✓ Còn hạn");
            lblExpiryStatus.setForeground(new Color(0, 150, 0));
        }
    }

    private void clearReaderInfo() {
        selectedReader = null;
        lblReaderId.setText("—");
        lblReaderName.setText("—");
        lblIdCard.setText("—");
        lblExpiry.setText("—");
        lblExpiryStatus.setText("—");
        lblExpiryStatus.setForeground(Color.BLACK);
    }

    // =========================================================================
    // SECTION 2 — Chọn sách
    // =========================================================================
    private JPanel buildBookSection() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBorder(BorderFactory.createTitledBorder("2. Chọn sách muốn mượn"));

        // ── Tìm sách ─────────────────────────────────────────────────────────
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cbBookSearchType = new JComboBox<>(new String[]{
                "Tìm theo ISBN", "Tìm theo tên sách"
        });
        tfBookSearch = new JTextField(18);
        JButton btnAddBook    = new JButton("Thêm vào phiếu");
        JButton btnRemoveBook = new JButton("Bỏ chọn");

        btnAddBook.addActionListener(e    -> handleAddBook());
        btnRemoveBook.addActionListener(e -> handleRemoveBook());
        tfBookSearch.addActionListener(e  -> handleAddBook());

        searchBar.add(cbBookSearchType);
        searchBar.add(tfBookSearch);
        searchBar.add(btnAddBook);
        searchBar.add(btnRemoveBook);
        outer.add(searchBar, BorderLayout.NORTH);

        // ── Bảng sách đã chọn ─────────────────────────────────────────────────
        selectedBooksModel = new DefaultTableModel(
                new String[]{"ISBN", "Tên sách", "Tác giả", "Còn lại"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        selectedBooksTable = new JTable(selectedBooksModel);
        selectedBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedBooksTable.setRowHeight(24);

        int[] widths = {110, 220, 150, 70};
        for (int i = 0; i < widths.length; i++) {
            selectedBooksTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        outer.add(new JScrollPane(selectedBooksTable), BorderLayout.CENTER);
        return outer;
    }

    private void handleAddBook() {
        String keyword = tfBookSearch.getText().trim();
        if (keyword.isEmpty()) return;

        Book book = cbBookSearchType.getSelectedIndex() == 0
                ? bookService.findByIsbn(keyword)
                : bookService.searchByTitle(keyword).stream().findFirst().orElse(null);

        if (book == null) {
            showError("Không tìm thấy sách: \"" + keyword + "\"");
            return;
        }

        // Không cho thêm trùng ISBN trong cùng phiếu
        boolean duplicate = selectedBooks.stream()
                .anyMatch(b -> b.getIsbn().equals(book.getIsbn()));
        if (duplicate) {
            showError("Sách \"" + book.getTitle() + "\" đã có trong phiếu mượn.");
            return;
        }

        // Kiểm tra còn sách không
        if (book.getAvailableCount() <= 0) {
            showError("Sách \"" + book.getTitle() + "\" hiện không còn quyển nào.");
            return;
        }

        selectedBooks.add(book);
        selectedBooksModel.addRow(new Object[]{
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getAvailableCount()
        });
        tfBookSearch.setText("");
    }

    private void handleRemoveBook() {
        int viewRow = selectedBooksTable.getSelectedRow();
        if (viewRow < 0) { showError("Vui lòng chọn sách cần bỏ."); return; }

        selectedBooks.remove(viewRow);
        selectedBooksModel.removeRow(viewRow);
    }

    // =========================================================================
    // SECTION 3 — Bottom bar
    // =========================================================================
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        // Hiển thị ngày mượn + ngày trả dự kiến để người dùng biết
        JLabel lblInfo = new JLabel("Ngày mượn: " + LocalDate.now().format(DATE_FMT)
                + "   |   Ngày trả dự kiến: " + LocalDate.now().plusDays(7).format(DATE_FMT));
        lblInfo.setForeground(Color.DARK_GRAY);

        JButton btnSubmit = new JButton("✓ Lập phiếu mượn");
        btnSubmit.setFont(btnSubmit.getFont().deriveFont(Font.BOLD));
        btnSubmit.addActionListener(e -> handleSubmit());

        JButton btnReset = new JButton("Nhập lại");
        btnReset.addActionListener(e -> handleReset());

        bar.add(lblInfo);
        bar.add(Box.createHorizontalStrut(24));
        bar.add(btnSubmit);
        bar.add(btnReset);
        return bar;
    }

    // ── Lập phiếu mượn ───────────────────────────────────────────────────────
    private void handleSubmit() {
        // Validate độc giả
        if (selectedReader == null) {
            showError("Vui lòng tìm và chọn độc giả trước.");
            return;
        }
        if (selectedReader.getCardExpiryDate().isBefore(LocalDate.now())) {
            showError("Thẻ độc giả \"" + selectedReader.getFullName() + "\" đã hết hạn.\nKhông thể lập phiếu mượn.");
            return;
        }

        // Validate sách
        if (selectedBooks.isEmpty()) {
            showError("Vui lòng thêm ít nhất một cuốn sách.");
            return;
        }

        // Tạo danh sách ISBN
        List<String> isbns = selectedBooks.stream()
                .map(Book::getIsbn)
                .toList();

        LocalDate borrowDate         = LocalDate.now();
        LocalDate expectedReturnDate = borrowDate.plusDays(7);

        BorrowSlip slip = new BorrowSlip(
                borrowService.generateSlipId(),
                selectedReader.getReaderId(),
                borrowDate,
                expectedReturnDate,
                null,   // actualReturnDate — chưa trả
                isbns,
                false   // isReturned
        );

        boolean success = borrowService.addBorrowSlip(slip);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Lập phiếu mượn thành công!\n"
                            + "Mã phiếu: " + slip.getSlipId() + "\n"
                            + "Độc giả: " + selectedReader.getFullName() + "\n"
                            + "Số sách: " + isbns.size() + "\n"
                            + "Hạn trả: " + expectedReturnDate.format(DATE_FMT),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            handleReset();
        } else {
            showError("Lập phiếu thất bại. Vui lòng kiểm tra lại.");
        }
    }

    private void handleReset() {
        clearReaderInfo();
        selectedBooks.clear();
        selectedBooksModel.setRowCount(0);
        tfReaderSearch.setText("");
        tfBookSearch.setText("");
    }

    // =========================================================================
    // Utility
    // =========================================================================
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