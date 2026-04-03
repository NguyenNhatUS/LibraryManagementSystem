package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


public class BorrowSlip {

    public static final long   MAX_BORROW_DAYS  = 7;
    public static final double LATE_FEE_PER_DAY = 5_000.0;

    private String          slipId;
    private String          readerId;
    private LocalDate       borrowDate;
    private LocalDate       expectedReturnDate;
    private LocalDate       actualReturnDate;
    private List<String>    bookIsbns;
    private boolean         isReturned;

    public BorrowSlip(String slipId, String readerId, LocalDate borrowDate,
                      List<String> bookIsbns) {
        this.slipId             = slipId;
        this.readerId           = readerId;
        this.borrowDate         = borrowDate;
        this.expectedReturnDate = borrowDate.plusDays(MAX_BORROW_DAYS);
        this.actualReturnDate   = null;
        this.bookIsbns          = new ArrayList<>(bookIsbns);
        this.isReturned         = false;
    }

    public BorrowSlip(String slipId, String readerId, LocalDate borrowDate,
                      LocalDate expectedReturnDate, LocalDate actualReturnDate,
                      List<String> bookIsbns, boolean isReturned) {
        this.slipId             = slipId;
        this.readerId           = readerId;
        this.borrowDate         = borrowDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate   = actualReturnDate;
        this.bookIsbns          = new ArrayList<>(bookIsbns);
        this.isReturned         = isReturned;
    }


    public boolean isLate() {
        LocalDate compareDate = (actualReturnDate != null) ? actualReturnDate : LocalDate.now();
        return compareDate.isAfter(expectedReturnDate);
    }


    public long getLateDays() {
        if (!isLate()) return 0;
        LocalDate compareDate = (actualReturnDate != null) ? actualReturnDate : LocalDate.now();
        return ChronoUnit.DAYS.between(expectedReturnDate, compareDate);
    }


    public double getLateFee() {
        return getLateDays() * LATE_FEE_PER_DAY;
    }

    public void markReturned(LocalDate returnDate) {
        this.actualReturnDate = returnDate;
        this.isReturned       = true;
    }


    public String getSlipId()                       { return slipId; }
    public void   setSlipId(String v)               { this.slipId = v; }

    public String getReaderId()                     { return readerId; }
    public void   setReaderId(String v)             { this.readerId = v; }

    public LocalDate getBorrowDate()                        { return borrowDate; }
    public void      setBorrowDate(LocalDate v)             { this.borrowDate = v; }

    public LocalDate getExpectedReturnDate()                { return expectedReturnDate; }
    public void      setExpectedReturnDate(LocalDate v)     { this.expectedReturnDate = v; }

    public LocalDate getActualReturnDate()                  { return actualReturnDate; }
    public void      setActualReturnDate(LocalDate v)       { this.actualReturnDate = v; }

    public List<String> getBookIsbns()              { return bookIsbns; }
    public void         setBookIsbns(List<String> v){ this.bookIsbns = new ArrayList<>(v); }

    public boolean isReturned()                     { return isReturned; }
    public void    setReturned(boolean v)           { this.isReturned = v; }

    @Override
    public String toString() {
        return String.format("BorrowSlip{id='%s', reader='%s', books=%s, returned=%b, late=%b}",
                slipId, readerId, bookIsbns, isReturned, isLate());
    }
}