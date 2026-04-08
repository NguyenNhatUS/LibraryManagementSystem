package service;

import model.Reader;
import util.FileManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ReaderService {

    private final FileManager  fileManager;
    private List<Reader>       readers;

    public ReaderService(FileManager fileManager) {
        this.fileManager = fileManager;
        this.readers     = fileManager.readReaders();
    }

    public List<Reader> getAllReaders() {
        return new ArrayList<>(readers);
    }


    public boolean addReader(Reader reader) {
        validateReader(reader);
        if (findByIdCard(reader.getIdCard()) != null) {
            throw new IllegalArgumentException("CMND/CCCD '" + reader.getIdCard() + "' đã tồn tại.");
        }
        reader.setReaderId(generateReaderId());
        readers.add(reader);
        save();
        return true;
    }

    public boolean updateReader(Reader updated) {
        validateReader(updated);
        int index = indexById(updated.getReaderId());
        if (index == -1) {
            throw new IllegalArgumentException("Không tìm thấy độc giả ID: " + updated.getReaderId());
        }

        Reader existing = findByIdCard(updated.getIdCard());
        if (existing != null && !existing.getReaderId().equals(updated.getReaderId())) {
            throw new IllegalArgumentException("CMND/CCCD '" + updated.getIdCard() + "' đã được dùng bởi độc giả khác.");
        }

        readers.set(index, updated);
        save();
        return true;
    }


    public boolean deleteReader(String readerId) {
        int index = indexById(readerId);
        if (index == -1) {
            throw new IllegalArgumentException("Không tìm thấy độc giả ID: " + readerId);
        }
        readers.remove(index);
        save();
        return true;
    }

    public Reader findByIdCard(String idCard) {
        return readers.stream()
                .filter(r -> r.getIdCard().equals(idCard.trim()))
                .findFirst()
                .orElse(null);
    }

    public List<Reader> findByName(String keyword) {
        String kw = keyword.trim().toLowerCase();
        return readers.stream()
                .filter(r -> r.getFullName().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public Reader findById(String readerId) {
        return readers.stream()
                .filter(r -> r.getReaderId().equals(readerId))
                .findFirst()
                .orElse(null);
    }


    public int getTotalReaders() {
        return readers.size();
    }

    public java.util.Map<String, Integer> countByGender() {
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        for (Reader r : readers) {
            result.merge(r.getGender(), 1, Integer::sum);
        }
        return result;
    }

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
        if (r.getEmail() == null || !r.getEmail().endsWith("@email.com"))
            throw new IllegalArgumentException("Email không hợp lệ.");
        if (r.getDateOfBirth() == null || r.getDateOfBirth().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngày sinh không hợp lệ.");
    }


    public String generateReaderId() {
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