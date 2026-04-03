package service;

import model.Reader;
import util.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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

    public void addReader(Reader reader) {
        validateReader(reader);
        if (findByIdCard(reader.getIdCard()) != null) {
            throw new IllegalArgumentException("CMND/CCCD '" + reader.getIdCard() + "' đã tồn tại.");
        }
        reader.setReaderId(generateReaderId());
        readers.add(reader);
        save();
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

    public Reader findByIdCard(String idCard) {
        return readers.stream()
                .filter(r -> r.getIdCard().equals(idCard.trim()))
                .findFirst()
                .orElse(null);
    }

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