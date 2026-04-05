package service;

import model.Book;
import util.FileManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class BookService {

    private final FileManager      fileManager;
    private Map<String, Book>      books;

    public BookService(FileManager fileManager) {
        this.fileManager = fileManager;
        this.books       = fileManager.readBooks();
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    public boolean addBook(Book book) {
        validateBook(book);
        if (books.containsKey(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN '" + book.getIsbn() + "' đã tồn tại.");
        }
        books.put(book.getIsbn(), book);
        save();
        return true;
    }


    public boolean updateBook(Book updated) {
        Book existing = books.get(updated.getIsbn());
        if (existing == null) return false;


        int diff = updated.getTotalCount() - existing.getTotalCount();
        updated.setAvailableCount(existing.getAvailableCount() + diff);

        books.put(updated.getIsbn(), updated);
        save();
        return true;
    }

    public boolean deleteBook(String isbn) {
        if (!books.containsKey(isbn)) {
            throw new IllegalArgumentException("Không tìm thấy sách ISBN: " + isbn);
        }
        books.remove(isbn);
        save();
        return true;
    }

    public Book findByIsbn(String isbn) {
        return books.get(isbn.trim());
    }

    public List<Book> searchByTitle(String keyword) {
        String kw = keyword.trim().toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public List<Book> searchByAuthor(String keyword) {
        String kw = keyword.trim().toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }


    public List<Book> searchByGenre(String keyword) {
        String kw = keyword.trim().toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public boolean borrowBook(String isbn) {
        Book book = books.get(isbn);
        if (book == null) return false;
        boolean ok = book.borrow();
        if (ok) save();
        return ok;
    }


    public void returnBook(String isbn) {
        Book book = books.get(isbn);
        if (book != null) {
            book.returnBook();
            save();
        }
    }

    public void reportLostBook(String isbn) {
        Book book = books.get(isbn);
        if (book != null) {
            book.reportLost();
            save();
        }
    }


    public int getTotalBookCount() {
        return books.values().stream()
                .mapToInt(Book::getTotalCount)
                .sum();
    }


    public Map<String, Integer> countByGenre() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Book b : books.values()) {
            result.merge(b.getGenre(), b.getTotalCount(), Integer::sum);
        }
        return result;
    }

    private void validateBook(Book b) {
        if (b.getIsbn() == null || b.getIsbn().trim().isEmpty())
            throw new IllegalArgumentException("ISBN không được để trống.");
        if (b.getTitle() == null || b.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Tên sách không được để trống.");
        if (b.getAuthor() == null || b.getAuthor().trim().isEmpty())
            throw new IllegalArgumentException("Tác giả không được để trống.");
        if (b.getPrice() < 0)
            throw new IllegalArgumentException("Giá sách không được âm.");
        if (b.getTotalCount() < 0)
            throw new IllegalArgumentException("Số quyển không được âm.");
        if (b.getAvailableCount() < 0)
            throw new IllegalArgumentException("Số quyển khả dụng không được âm.");
    }

    private void save() {
        fileManager.writeBooks(books);
    }
}