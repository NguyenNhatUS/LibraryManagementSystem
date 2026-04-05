package ui;

import model.Book;
import service.BookService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * BookPanel — quản lý sách (xem, thêm, sửa, xóa, tìm kiếm).
 *
 * Layout:
 *  ┌─────────────────────────────────────────────────────┐
 *  │  [Tìm theo...]  [TextField]  [Tìm]  [Hiển thị tất cả] │  ← toolbar
 *  ├─────────────────────────────────────────────────────┤
 *  │  JTable — danh sách sách                            │  ← center
 *  ├─────────────────────────────────────────────────────┤
 *  │  [Thêm]  [Sửa]  [Xóa]  [Làm mới]                   │  ← bottom
 *  └─────────────────────────────────────────────────────┘
 */
public class BookPanel extends JPanel {

    private static final String[] COLUMNS = {
            "ISBN", "Tên sách", "Tác giả", "NXB",
            "Năm XB", "Thể loại", "Giá (đ)", "Tổng SL", "Còn lại"
    };

    private final BookService bookService;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cbSearchType;

    public BookPanel(BookService bookService) {
        this.bookService = bookService;
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildBottom(),  BorderLayout.SOUTH);

        loadTable(bookService.getAllBooks());
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        cbSearchType = new JComboBox<>(new String[]{
                "Tìm theo tên sách",
                "Tìm theo ISBN",
                "Tìm theo tác giả",
                "Tìm theo thể loại"
        });
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Tìm");
        JButton btnReset  = new JButton("Hiển thị tất cả");

        btnSearch.addActionListener(e -> handleSearch());
        btnReset.addActionListener(e  -> loadTable(bookService.getAllBooks()));
        txtSearch.addActionListener(e -> handleSearch());

        bar.add(cbSearchType);
        bar.add(txtSearch);
        bar.add(btnSearch);
        bar.add(btnReset);
        return bar;
    }

    // ── Bảng dữ liệu ──────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        // Double-click = Sửa
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) handleEdit();
            }
        });

        int[] widths = {100, 180, 120, 120, 60, 100, 90, 70, 70};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        return new JScrollPane(table);
    }

    // ── Bottom buttons ────────────────────────────────────────────────────────
    private JPanel buildBottom() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton btnAdd     = new JButton("Thêm");
        JButton btnEdit    = new JButton("Sửa");
        JButton btnDelete  = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");

        btnAdd.addActionListener(e     -> handleAdd());
        btnEdit.addActionListener(e    -> handleEdit());
        btnDelete.addActionListener(e  -> handleDelete());
        btnRefresh.addActionListener(e -> loadTable(bookService.getAllBooks()));

        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);
        bar.add(Box.createHorizontalStrut(16));
        bar.add(btnRefresh);
        return bar;
    }

    // ── Load dữ liệu vào bảng ─────────────────────────────────────────────────
    private void loadTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getPublisher(),
                    b.getPublishYear(),
                    b.getGenre(),
                    String.format("%,.0f", b.getPrice()),
                    b.getTotalCount(),
                    b.getAvailableCount()
            });
        }
    }

    // ── Lấy ISBN của dòng đang chọn ──────────────────────────────────────────
    private String getSelectedIsbn() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (String) tableModel.getValueAt(modelRow, 0);
    }

    // ── Tìm kiếm ─────────────────────────────────────────────────────────────
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadTable(bookService.getAllBooks());
            return;
        }

        List<Book> result;
        switch (cbSearchType.getSelectedIndex()) {
            case 0 -> result = bookService.searchByTitle(keyword);
            case 1 -> {
                Book found = bookService.findByIsbn(keyword);
                result = (found != null) ? List.of(found) : List.of();
            }
            case 2 -> result = bookService.searchByAuthor(keyword);
            case 3 -> result = bookService.searchByGenre(keyword);
            default -> result = List.of();
        }

        loadTable(result);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sách nào.",
                    "Kết quả", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Thêm sách ─────────────────────────────────────────────────────────────
    private void handleAdd() {
        BookForm form = new BookForm();
        int result = JOptionPane.showConfirmDialog(this, form.getPanel(),
                "Thêm sách mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        Book book = form.buildBook();
        if (book == null) return;

        boolean success = bookService.addBook(book);
        if (success) {
            loadTable(bookService.getAllBooks());
            JOptionPane.showMessageDialog(this, "Thêm sách thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showError("ISBN \"" + book.getIsbn() + "\" đã tồn tại.");
        }
    }

    // ── Sửa sách ──────────────────────────────────────────────────────────────
    private void handleEdit() {
        String isbn = getSelectedIsbn();
        if (isbn == null) { showError("Vui lòng chọn một cuốn sách để sửa."); return; }

        Book existing = bookService.findByIsbn(isbn);
        if (existing == null) { showError("Không tìm thấy sách."); return; }

        BookForm form = new BookForm(existing);
        int result = JOptionPane.showConfirmDialog(this, form.getPanel(),
                "Sửa thông tin sách", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        Book updated = form.buildBook();
        if (updated == null) return;

        boolean success = bookService.updateBook(updated);
        if (success) {
            loadTable(bookService.getAllBooks());
        } else {
            showError("Cập nhật thất bại.");
        }
    }

    // ── Xóa sách ──────────────────────────────────────────────────────────────
    private void handleDelete() {
        String isbn = getSelectedIsbn();
        if (isbn == null) { showError("Vui lòng chọn một cuốn sách để xóa."); return; }

        Book b = bookService.findByIsbn(isbn);
        if (b == null) return;

        // Không cho xóa nếu còn sách đang được mượn
        if (b.getAvailableCount() < b.getTotalCount()) {
            showError("Không thể xóa — vẫn còn " +
                    (b.getTotalCount() - b.getAvailableCount()) + " quyển đang được mượn.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa sách \"" + b.getTitle() + "\"?\nHành động này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = bookService.deleteBook(isbn);
        if (success) {
            loadTable(bookService.getAllBooks());
        } else {
            showError("Xóa thất bại.");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================================
    // Inner class: Form nhập liệu — dùng chung cho Add và Edit
    // =========================================================================
    private class BookForm {

        private final JTextField tfIsbn        = new JTextField(16);
        private final JTextField tfTitle       = new JTextField(24);
        private final JTextField tfAuthor      = new JTextField(20);
        private final JTextField tfPublisher   = new JTextField(20);
        private final JTextField tfPublishYear = new JTextField(6);
        private final JTextField tfGenre       = new JTextField(16);
        private final JTextField tfPrice       = new JTextField(10);
        private final JTextField tfTotalCount  = new JTextField(6);

        /** Form trống — dùng cho Thêm */
        BookForm() {}

        /** Form pre-filled — dùng cho Sửa */
        BookForm(Book b) {
            tfIsbn.setText(b.getIsbn());
            tfIsbn.setEditable(false); // ISBN là key, không cho sửa
            tfTitle.setText(b.getTitle());
            tfAuthor.setText(b.getAuthor());
            tfPublisher.setText(b.getPublisher());
            tfPublishYear.setText(String.valueOf(b.getPublishYear()));
            tfGenre.setText(b.getGenre());
            tfPrice.setText(String.valueOf((long) b.getPrice()));
            tfTotalCount.setText(String.valueOf(b.getTotalCount()));
        }

        JPanel getPanel() {
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 6, 4, 6);
            g.fill   = GridBagConstraints.HORIZONTAL;

            addRow(p, g, 0, "ISBN *:",         tfIsbn);
            addRow(p, g, 1, "Tên sách *:",     tfTitle);
            addRow(p, g, 2, "Tác giả *:",      tfAuthor);
            addRow(p, g, 3, "Nhà xuất bản:",   tfPublisher);
            addRow(p, g, 4, "Năm xuất bản:",   tfPublishYear);
            addRow(p, g, 5, "Thể loại:",        tfGenre);
            addRow(p, g, 6, "Giá sách (đ) *:", tfPrice);
            addRow(p, g, 7, "Số lượng *:",      tfTotalCount);

            return p;
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, Component field) {
            g.gridwidth = 1; g.weightx = 0;
            g.gridx = 0; g.gridy = row;
            p.add(new JLabel(label), g);
            g.gridx = 1; g.weightx = 1;
            p.add(field, g);
        }

        Book buildBook() {
            String isbn      = tfIsbn.getText().trim();
            String title     = tfTitle.getText().trim();
            String author    = tfAuthor.getText().trim();
            String publisher = tfPublisher.getText().trim();
            String yearStr   = tfPublishYear.getText().trim();
            String genre     = tfGenre.getText().trim();
            String priceStr  = tfPrice.getText().trim();
            String countStr  = tfTotalCount.getText().trim();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()
                    || priceStr.isEmpty() || countStr.isEmpty()) {
                showError("Vui lòng điền đầy đủ các trường có dấu *.");
                return null;
            }

            int publishYear;
            double price;
            int totalCount;

            try {
                publishYear = yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                showError("Năm xuất bản phải là số nguyên.");
                return null;
            }
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showError("Giá sách không hợp lệ.");
                return null;
            }
            try {
                totalCount = Integer.parseInt(countStr);
                if (totalCount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showError("Số lượng phải là số nguyên dương.");
                return null;
            }

            // Khi thêm mới: availableCount = totalCount
            // Khi sửa: availableCount giữ nguyên — BookService tự xử lý
            return new Book(isbn, title, author, publisher, publishYear,
                    genre, price, totalCount, totalCount);
        }
    }
}