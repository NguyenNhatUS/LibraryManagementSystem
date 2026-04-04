package service;

import model.Book;
import model.BorrowSlip;
import model.Reader;
import util.FileManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class BorrowService {

    private final FileManager   fileManager;
    private final ReaderService readerService;
    private final BookService   bookService;
    private List<BorrowSlip>    slips; // Collection chính

    public BorrowService(FileManager fileManager,
                         ReaderService readerService,
                         BookService bookService) {
        this.fileManager   = fileManager;
        this.readerService = readerService;
        this.bookService   = bookService;
        this.slips         = fileManager.readBorrowSlips();
    }

    public BorrowSlip createBorrowSlip(String readerId, List<String> isbns, LocalDate borrowDate) {
        Reader reader = readerService.findById(readerId);
        if (reader == null) {
            throw new IllegalArgumentException("Không tìm thấy độc giả ID: " + readerId);
        }
        if (!reader.isCardValid()) {
            throw new IllegalArgumentException(
                    "Thẻ độc giả '" + reader.getFullName() + "' đã hết hạn vào " + reader.getCardExpiryDate() + ".");
        }

        boolean hasOpenSlip = slips.stream()
                .anyMatch(s -> s.getReaderId().equals(readerId) && !s.isReturned());
        if (hasOpenSlip) {
            throw new IllegalArgumentException(
                    "Độc giả '" + reader.getFullName() + "' đang có phiếu mượn chưa trả.");
        }


        if (isbns == null || isbns.isEmpty()) {
            throw new IllegalArgumentException("Phiếu mượn phải có ít nhất 1 quyển sách.");
        }
        for (String isbn : isbns) {
            Book book = bookService.findByIsbn(isbn);
            if (book == null) {
                throw new IllegalArgumentException("Không tìm thấy sách ISBN: " + isbn);
            }
            if (!book.isAvailable()) {
                throw new IllegalArgumentException(
                        "Sách '" + book.getTitle() + "' (ISBN: " + isbn + ") đã hết, không thể mượn.");
            }
        }

        String slipId = generateSlipId();
        BorrowSlip slip = new BorrowSlip(slipId, readerId, borrowDate, isbns);

        for (String isbn : isbns) {
            bookService.borrowBook(isbn);
        }

        slips.add(slip);
        save();
        return slip;
    }

    public static class ReturnResult {
        public final BorrowSlip slip;
        public final double     lateFee;
        public final double     lostFee;
        public final double     totalFee;
        public final List<String> lostIsbns;

        public ReturnResult(BorrowSlip slip, double lateFee,
                            double lostFee, List<String> lostIsbns) {
            this.slip      = slip;
            this.lateFee   = lateFee;
            this.lostFee   = lostFee;
            this.lostIsbns = lostIsbns;
            this.totalFee  = lateFee + lostFee;
        }
    }

    public ReturnResult returnBooks(String slipId, LocalDate returnDate, List<String> lostIsbns) {
        BorrowSlip slip = findSlipById(slipId);
        if (slip == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + slipId);
        }
        if (slip.isReturned()) {
            throw new IllegalArgumentException("Phiếu mượn '" + slipId + "' đã được trả trước đó.");
        }


        slip.setActualReturnDate(returnDate);
        double lateFee = slip.getLateFee();


        double lostFee = 0;
        for (String isbn : slip.getBookIsbns()) {
            if (lostIsbns != null && lostIsbns.contains(isbn)) {

                Book book = bookService.findByIsbn(isbn);
                if (book != null) {
                    lostFee += book.getLostPenalty();
                    bookService.reportLostBook(isbn); // Giảm totalCount
                }
            } else {

                bookService.returnBook(isbn); // Tăng availableCount
            }
        }


        slip.markReturned(returnDate);
        save();

        return new ReturnResult(slip, lateFee, lostFee,
                lostIsbns != null ? lostIsbns : new ArrayList<>());
    }

    public List<BorrowSlip> getAllSlips() {
        return new ArrayList<>(slips);
    }

    public BorrowSlip findSlipById(String slipId) {
        return slips.stream()
                .filter(s -> s.getSlipId().equals(slipId))
                .findFirst()
                .orElse(null);
    }

    public List<BorrowSlip> getSlipsByReader(String readerId) {
        return slips.stream()
                .filter(s -> s.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }


    public int getBorrowingBookCount() {
        return slips.stream()
                .filter(s -> !s.isReturned())
                .mapToInt(s -> s.getBookIsbns().size())
                .sum();
    }


    public List<Reader> getLateReaders() {
        return slips.stream()
                .filter(s -> !s.isReturned() && s.isLate())
                .map(s -> readerService.findById(s.getReaderId()))
                .filter(r -> r != null) // Phòng trường hợp data bị lỗi
                .distinct()
                .collect(Collectors.toList());
    }

    public List<BorrowSlip> getLateSlips() {
        return slips.stream()
                .filter(s -> !s.isReturned() && s.isLate())
                .collect(Collectors.toList());
    }

    private String generateSlipId() {
        int max = 0;
        for (BorrowSlip s : slips) {
            try {
                int num = Integer.parseInt(s.getSlipId().substring(1));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("B%03d", max + 1);
    }

    private void save() {
        fileManager.writeBorrowSlips(slips);
    }
}