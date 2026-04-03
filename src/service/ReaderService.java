package service;

import model.Reader;
import util.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReaderService — Quản lý độc giả
 *
 * Collection dùng: ArrayList<Reader>
 *  - Lý do chọn List (không phải Map): thứ tự nhập liệu quan trọng,
 *    thường xuyên duyệt toàn bộ để tìm kiếm theo tên.
 *  - Nếu cần lookup theo ID thường xuyên hơn thì đổi sang HashMap.
 */
public class ReaderService {

    private final FileManager  fileManager;
    private List<Reader>       readers; // Collection chính

    public ReaderService(FileManager fileManager) {
        this.fileManager = fileManager;
        this.readers     = fileManager.readReaders();
    }

    // ─── Xem danh sách ────────────────────────────────────────────────────────

    /** Trả về bản sao để UI không thể modify trực tiếp */
    public List<Reader> getAllReaders() {
        return new ArrayList<>(readers);
    }

    // ─── Thêm độc giả ─────────────────────────────────────────────────────────

    /**
     * Thêm độc giả mới.
     * @throws IllegalArgumentException nếu CMND đã tồn tại hoặc dữ liệu không hợp lệ
     */
    public void addReader(Reader reader) {
        validateReader(reader);
        if (findByIdCard(reader.getIdCard()) != null) {
            throw new IllegalArgumentException("CMND/CCCD '" + reader.getIdCard() + "' đã tồn tại.");
        }
        reader.setReaderId(generateReaderId());
        readers.add(reader);
        save();
    }

    // ─── Chỉnh sửa ────────────────────────────────────────────────────────────

    /**
     * Cập nhật thông tin độc giả (tìm theo readerId).
     * @throws IllegalArgumentException nếu không tìm thấy hoặc dữ liệu không hợp lệ
     */
    public void updateReader(Reader updated) {
        validateReader(updated);
        int index = indexById(updated.getReaderId());
        if (index == -1) {
            throw new IllegalArgumentException("Không tìm thấy độc giả ID: " + updated.getReaderId());
        }

        // Kiểm tra CMND mới có bị trùng với người KHÁC không
        Reader existing = findByIdCard(updated.getIdCard());
        if (existing != null && !existing.getReaderId().equals(updated.getReaderId())) {
            throw new IllegalArgumentException("CMND/CCCD '" + updated.getIdCard() + "' đã được dùng bởi độc giả khác.");
        }

        readers.set(index, updated);
        save();
    }

    // ─── Xóa ──────────────────────────────────────────────────────────────────

    /**
     * Xóa độc giả theo ID.
     * Lưu ý: BorrowService nên check độc giả không có phiếu mượn đang mở trước khi gọi hàm này.
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    public void deleteReader(String readerId) {
        int index = indexById(readerId);
        if (index == -1) {
            throw new IllegalArgumentException("Không tìm thấy độc giả ID: " + readerId);
        }
        readers.remove(index);
        save();
    }

    // ─── Tìm kiếm ─────────────────────────────────────────────────────────────

    /** Tìm chính xác theo CMND/CCCD. Trả về null nếu không có. */
    public Reader findByIdCard(String idCard) {
        return readers.stream()
                .filter(r -> r.getIdCard().equals(idCard.trim()))
                .findFirst()
                .orElse(null);
    }

    /** Tìm theo tên — không phân biệt hoa/thường, chứa chuỗi là match */
    public List<Reader> findByName(String keyword) {
        String kw = keyword.trim().toLowerCase();
        return readers.stream()
                .filter(r -> r.getFullName().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    /** Tìm theo ID — dùng nội bộ và từ các Service khác */
    public Reader findById(String readerId) {
        return readers.stream()
                .filter(r -> r.getReaderId().equals(readerId))
                .findFirst()
                .orElse(null);
    }

    // ─── Thống kê ─────────────────────────────────────────────────────────────

    public int getTotalReaders() {
        return readers.size();
    }

    /**
     * Thống kê số độc giả theo giới tính.
     * Key = giới tính ("Nam", "Nữ", "Khác"), Value = số lượng.
     */
    public java.util.Map<String, Integer> countByGender() {
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        for (Reader r : readers) {
            result.merge(r.getGender(), 1, Integer::sum);
        }
        return result;
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private int indexById(String readerId) {
        for (int i = 0; i < readers.size(); i++) {
            if (readers.get(i).getReaderId().equals(readerId)) return i;
        }
        return -1;
    }

    private void validateReader(Reader r) {
        if (r.getFullName() == null || r.getFullName().trim().isEmpty())
            throw new IllegalArgumentException("Họ tên không được để trống.");
        if (r.getIdCard() == null || r.getIdCard().trim().isEmpty())
            throw new IllegalArgumentException("CMND/CCCD không được để trống.");
        if (r.getEmail() == null || !r.getEmail().contains("@"))
            throw new IllegalArgumentException("Email không hợp lệ.");
        if (r.getDateOfBirth() == null || r.getDateOfBirth().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngày sinh không hợp lệ.");
    }

    /**
     * Tự sinh ID dạng R001, R002, ...
     * Tìm số lớn nhất hiện có rồi +1, tránh trùng khi có xóa giữa chừng.
     */
    private String generateReaderId() {
        int max = 0;
        for (Reader r : readers) {
            try {
                int num = Integer.parseInt(r.getReaderId().substring(1));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("R%03d", max + 1);
    }

    private void save() {
        fileManager.writeReaders(readers);
    }
}