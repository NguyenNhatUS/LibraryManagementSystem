package util;

import model.Book;
import model.BorrowSlip;
import model.Reader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;


public class FileManager {

    private static final String DELIMITER       = "|";
    private static final String DELIMITER_REGEX = "\\|";
    private static final String ISBN_SEPARATOR  = ";";

    private final String dataDir;

    public FileManager(String dataDir) {
        this.dataDir = dataDir;
        ensureDataDirExists();
    }

    private void ensureDataDirExists() {
        File dir = new File(dataDir);
        if (!dir.exists()) dir.mkdirs();
    }

    private String path(String filename) {
        return dataDir + File.separator + filename;
    }

    public List<Reader> readReaders() {
        List<Reader> list = new ArrayList<>();
        List<String> lines = readLines("readers.txt");
        for (String line : lines) {
            try {
                String[] f = line.split(DELIMITER_REGEX, -1);
                Reader r = new Reader(
                        f[0],
                        f[1],
                        f[2],
                        LocalDate.parse(f[3]),
                        f[4],
                        f[5],
                        f[6],
                        LocalDate.parse(f[7])
                );
                list.add(r);
            } catch (Exception e) {
                System.err.println("[FileManager] Lỗi parse reader: " + line);
            }
        }
        return list;
    }

    public void writeReaders(List<Reader> readers) {
        List<String> lines = new ArrayList<>();
        for (Reader r : readers) {
            String line = String.join(DELIMITER,
                    r.getReaderId(),
                    r.getFullName(),
                    r.getIdCard(),
                    r.getDateOfBirth().toString(),
                    r.getGender(),
                    r.getEmail(),
                    r.getAddress(),
                    r.getCardIssueDate().toString()
            );
            lines.add(line);
        }
        writeLines("readers.txt", lines);
    }

    public Map<String, Book> readBooks() {
        Map<String, Book> map = new LinkedHashMap<>(); // LinkedHashMap giữ thứ tự nhập
        List<String> lines = readLines("books.txt");
        for (String line : lines) {
            try {
                String[] f = line.split(DELIMITER_REGEX, -1);
                Book b = new Book(
                        f[0],
                        f[1],
                        f[2],
                        f[3],
                        Integer.parseInt(f[4]),
                        f[5],
                        Double.parseDouble(f[6]),
                        Integer.parseInt(f[7]),
                        Integer.parseInt(f[8])
                );
                map.put(b.getIsbn(), b);
            } catch (Exception e) {
                System.err.println("[FileManager] Lỗi parse book: " + line);
            }
        }
        return map;
    }

    public void writeBooks(Map<String, Book> books) {
        List<String> lines = new ArrayList<>();
        for (Book b : books.values()) {
            String line = String.join(DELIMITER,
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getPublisher(),
                    String.valueOf(b.getPublishYear()),
                    b.getGenre(),
                    String.valueOf(b.getPrice()),
                    String.valueOf(b.getTotalCount()),
                    String.valueOf(b.getAvailableCount())
            );
            lines.add(line);
        }
        writeLines("books.txt", lines);
    }


    public List<BorrowSlip> readBorrowSlips() {
        List<BorrowSlip> list = new ArrayList<>();
        List<String> lines = readLines("borrows.txt");
        for (String line : lines) {
            try {
                String[] f = line.split(DELIMITER_REGEX, -1);

                LocalDate actualReturn = f[4].equals("null") ? null : LocalDate.parse(f[4]);

                List<String> isbns = new ArrayList<>();
                if (!f[5].isEmpty()) {
                    isbns.addAll(Arrays.asList(f[5].split(ISBN_SEPARATOR)));
                }

                BorrowSlip slip = new BorrowSlip(
                        f[0],
                        f[1],
                        LocalDate.parse(f[2]),
                        LocalDate.parse(f[3]),
                        actualReturn,
                        isbns,
                        Boolean.parseBoolean(f[6])
                );
                list.add(slip);
            } catch (Exception e) {
                System.err.println("[FileManager] Lỗi parse borrow slip: " + line);
            }
        }
        return list;
    }

    public void writeBorrowSlips(List<BorrowSlip> slips) {
        List<String> lines = new ArrayList<>();
        for (BorrowSlip s : slips) {
            String actualReturn = (s.getActualReturnDate() == null)
                    ? "null"
                    : s.getActualReturnDate().toString();

            String isbns = String.join(ISBN_SEPARATOR, s.getBookIsbns());

            String line = String.join(DELIMITER,
                    s.getSlipId(),
                    s.getReaderId(),
                    s.getBorrowDate().toString(),
                    s.getExpectedReturnDate().toString(),
                    actualReturn,
                    isbns,
                    String.valueOf(s.isReturned())
            );
            lines.add(line);
        }
        writeLines("borrows.txt", lines);
    }

    public Map<String, String> readAccounts() {
        Map<String, String> accounts = new HashMap<>();
        List<String> lines = readLines("accounts.txt");
        for (String line : lines) {
            try {
                String[] f = line.split(DELIMITER_REGEX, -1);
                accounts.put(f[0], f[1]);
            } catch (Exception e) {
                System.err.println("[FileManager] Lỗi parse account: " + line);
            }
        }
        return accounts;
    }

    public void writeAccounts(Map<String, String> accounts) {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : accounts.entrySet()) {
            lines.add(entry.getKey() + DELIMITER + entry.getValue());
        }
        writeLines("accounts.txt", lines);
    }


    private List<String> readLines(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(path(filename));
        if (!file.exists()) return lines;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Không thể đọc file " + filename + ": " + e.getMessage());
        }
        return lines;
    }


    private void writeLines(String filename, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path(filename)), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Không thể ghi file " + filename + ": " + e.getMessage());
        }
    }
}