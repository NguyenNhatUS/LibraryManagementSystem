package ui;

import model.Reader;
import service.ReaderService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;


public class ReaderPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUMNS = {
            "Mã độc giả", "Họ tên", "CMND/CCCD", "Ngày sinh",
            "Giới tính", "Email", "Địa chỉ", "Ngày lập thẻ", "Ngày hết hạn"
    };


    private final ReaderService readerService;


    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cbSearchType;

    public ReaderPanel(ReaderService readerService) {
        this.readerService = readerService;
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildBottom(),  BorderLayout.SOUTH);

        loadTable(readerService.getAllReaders());
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        cbSearchType = new JComboBox<>(new String[]{
                "Tìm theo tên",
                "Tìm theo CMND/CCCD",
                "Tìm theo mã độc giả"
        });
        txtSearch    = new JTextField(20);
        JButton btnSearch = new JButton("Tìm");
        JButton btnReset  = new JButton("Hiển thị tất cả");

        btnSearch.addActionListener(e -> handleSearch());
        btnReset.addActionListener(e  -> loadTable(readerService.getAllReaders()));

        txtSearch.addActionListener(e -> handleSearch());

        bar.add(cbSearchType);
        bar.add(txtSearch);
        bar.add(btnSearch);
        bar.add(btnReset);
        return bar;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);


        table.setRowSorter(new TableRowSorter<>(tableModel));


        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) handleEdit();
            }
        });


        int[] widths = {90, 140, 110, 90, 70, 150, 160, 90, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        return new JScrollPane(table);
    }


    private JPanel buildBottom() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton btnAdd    = new JButton("Thêm");
        JButton btnEdit   = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");

        btnAdd.addActionListener(e    -> handleAdd());
        btnEdit.addActionListener(e   -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());
        btnRefresh.addActionListener(e -> loadTable(readerService.getAllReaders()));

        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);
        bar.add(Box.createHorizontalStrut(16));
        bar.add(btnRefresh);
        return bar;
    }


    private void loadTable(List<Reader> readers) {
        tableModel.setRowCount(0);
        for (Reader r : readers) {
            tableModel.addRow(new Object[]{
                    r.getReaderId(),
                    r.getFullName(),
                    r.getIdCard(),
                    r.getDateOfBirth().format(DATE_FMT),
                    r.getGender(),
                    r.getEmail(),
                    r.getAddress(),
                    r.getCardIssueDate().format(DATE_FMT),
                    r.getCardExpiryDate().format(DATE_FMT)
            });
        }
    }

    private String getSelectedReaderId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (String) tableModel.getValueAt(modelRow, 0);
    }


    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadTable(readerService.getAllReaders());
            return;
        }

        List<Reader> result;
        switch (cbSearchType.getSelectedIndex()) {
            case 0 -> result = readerService.findByName(keyword);
            case 1 -> {
                Reader found = readerService.findByIdCard(keyword);
                result = (found != null) ? List.of(found) : List.of();
            }
            case 2 -> {
                Reader found = readerService.findById(keyword);
                result = (found != null) ? List.of(found) : List.of();
            }
            default -> result = List.of();
        }

        loadTable(result);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy độc giả nào.",
                    "Kết quả", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleAdd() {
        ReaderForm form = new ReaderForm();
        int result = JOptionPane.showConfirmDialog(this, form.getPanel(),
                "Thêm độc giả mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        Reader reader = form.buildReader(null);
        if (reader == null) return;

        boolean success = readerService.addReader(reader);
        if (success) {
            loadTable(readerService.getAllReaders());
            JOptionPane.showMessageDialog(this, "Thêm độc giả thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showError("Mã độc giả đã tồn tại.");
        }
    }

    private void handleEdit() {
        String id = getSelectedReaderId();
        if (id == null) { showError("Vui lòng chọn một độc giả để sửa."); return; }

        Reader existing = readerService.findById(id);
        if (existing == null) { showError("Không tìm thấy độc giả."); return; }

        ReaderForm form = new ReaderForm(existing); // pre-fill
        int result = JOptionPane.showConfirmDialog(this, form.getPanel(),
                "Sửa thông tin độc giả", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        Reader updated = form.buildReader(id);
        if (updated == null) return;

        boolean success = readerService.updateReader(updated);
        if (success) {
            loadTable(readerService.getAllReaders());
        } else {
            showError("Cập nhật thất bại.");
        }
    }


    private void handleDelete() {
        String id = getSelectedReaderId();
        if (id == null) { showError("Vui lòng chọn một độc giả để xóa."); return; }

        Reader r = readerService.findById(id);
        if (r == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa độc giả \"" + r.getFullName() + "\"?\nHành động này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = readerService.deleteReader(id);
        if (success) {
            loadTable(readerService.getAllReaders());
        } else {
            showError("Xóa thất bại — độc giả có thể đang có phiếu mượn.");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }


    private class ReaderForm {

        private final JTextField tfId       = new JTextField(16);
        private final JTextField tfName     = new JTextField(16);
        private final JTextField tfIdCard   = new JTextField(16);
        private final JTextField tfDob      = new JTextField(10);
        private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        private final JTextField tfEmail    = new JTextField(16);
        private final JTextField tfAddress  = new JTextField(24);
        private final JTextField tfIssueDate = new JTextField(10);

        ReaderForm() {
            tfIssueDate.setText(LocalDate.now().format(DATE_FMT));
        }


        ReaderForm(Reader r) {
            tfId.setText(r.getReaderId());
            tfId.setEditable(false);
            tfName.setText(r.getFullName());
            tfIdCard.setText(r.getIdCard());
            tfDob.setText(r.getDateOfBirth().format(DATE_FMT));
            cbGender.setSelectedItem(r.getGender());
            tfEmail.setText(r.getEmail());
            tfAddress.setText(r.getAddress());
            tfIssueDate.setText(r.getCardIssueDate().format(DATE_FMT));
        }

        JPanel getPanel() {
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 6, 4, 6);
            g.fill   = GridBagConstraints.HORIZONTAL;

            addRow(p, g, 0, "Mã độc giả:",    tfId);
            addRow(p, g, 1, "Họ tên *:",       tfName);
            addRow(p, g, 2, "CMND/CCCD *:",    tfIdCard);
            addRow(p, g, 3, "Ngày sinh *:",     tfDob);
            addRow(p, g, 4, "Giới tính:",       cbGender);
            addRow(p, g, 5, "Email:",           tfEmail);
            addRow(p, g, 6, "Địa chỉ:",         tfAddress);
            addRow(p, g, 7, "Ngày lập thẻ *:",  tfIssueDate);

            JLabel hint = new JLabel("* Định dạng ngày: dd/MM/yyyy — Ngày hết hạn tự tính (+48 tháng)");
            hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
            hint.setForeground(Color.GRAY);
            g.gridx = 0; g.gridy = 8; g.gridwidth = 2;
            p.add(hint, g);

            return p;
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, Component field) {
            g.gridwidth = 1; g.weightx = 0;
            g.gridx = 0; g.gridy = row;
            p.add(new JLabel(label), g);
            g.gridx = 1; g.weightx = 1;
            p.add(field, g);
        }


        Reader buildReader(String fixedId) {
            String name      = tfName.getText().trim();
            String idCard    = tfIdCard.getText().trim();
            String dobStr    = tfDob.getText().trim();
            String issueStr  = tfIssueDate.getText().trim();
            String gender    = (String) cbGender.getSelectedItem();
            String email     = tfEmail.getText().trim();
            String address   = tfAddress.getText().trim();

            if (name.isEmpty() || idCard.isEmpty() || dobStr.isEmpty() || issueStr.isEmpty()) {
                showError("Vui lòng điền đầy đủ các trường có dấu *.");
                return null;
            }

            LocalDate dob, issueDate;
            try {
                dob       = LocalDate.parse(dobStr,   DATE_FMT);
                issueDate = LocalDate.parse(issueStr, DATE_FMT);
            } catch (DateTimeParseException ex) {
                showError("Ngày không hợp lệ. Định dạng đúng: dd/MM/yyyy");
                return null;
            }


            String readerId = (fixedId != null) ? fixedId
                    : (tfId.getText().trim().isEmpty()
                    ? readerService.generateReaderId()
                    : tfId.getText().trim());


            return new Reader(readerId, name, idCard, dob, gender,
                    email, address, issueDate);
        }
    }
}