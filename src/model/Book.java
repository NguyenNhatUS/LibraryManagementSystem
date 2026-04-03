package model;


public class Book {

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int    publishYear;
    private String genre;
    private double price;
    private int    totalCount;
    private int    availableCount;


    public Book(String isbn, String title, String author, String publisher,
                int publishYear, String genre, double price,
                int totalCount, int availableCount) {
        this.isbn           = isbn;
        this.title          = title;
        this.author         = author;
        this.publisher      = publisher;
        this.publishYear    = publishYear;
        this.genre          = genre;
        this.price          = price;
        this.totalCount     = totalCount;
        this.availableCount = availableCount;
    }

    public boolean borrow() {
        if (availableCount <= 0) return false;
        availableCount--;
        return true;
    }


    public void returnBook() {
        if (availableCount < totalCount) {
            availableCount++;
        }
    }


    public void reportLost() {
        if (totalCount > 0) totalCount--;
    }


    public double getLostPenalty() {
        return price * 2.0;
    }

    public boolean isAvailable() {
        return availableCount > 0;
    }


    public String getIsbn()                 { return isbn; }
    public void   setIsbn(String v)         { this.isbn = v; }

    public String getTitle()                { return title; }
    public void   setTitle(String v)        { this.title = v; }

    public String getAuthor()               { return author; }
    public void   setAuthor(String v)       { this.author = v; }

    public String getPublisher()            { return publisher; }
    public void   setPublisher(String v)    { this.publisher = v; }

    public int  getPublishYear()            { return publishYear; }
    public void setPublishYear(int v)       { this.publishYear = v; }

    public String getGenre()                { return genre; }
    public void   setGenre(String v)        { this.genre = v; }

    public double getPrice()                { return price; }
    public void   setPrice(double v)        { this.price = v; }

    public int  getTotalCount()             { return totalCount; }
    public void setTotalCount(int v)        { this.totalCount = v; }

    public int  getAvailableCount()         { return availableCount; }
    public void setAvailableCount(int v)    { this.availableCount = v; }

    @Override
    public String toString() {
        return String.format("Book{isbn='%s', title='%s', available=%d/%d}",
                isbn, title, availableCount, totalCount);
    }
}