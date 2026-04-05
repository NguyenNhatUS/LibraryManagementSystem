import service.AccountService;
import service.ReaderService;
import service.BookService;
import service.BorrowService;
import util.FileManager;
import ui.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager("data/");

        AccountService accountService = new AccountService(fileManager);
        ReaderService  readerService  = new ReaderService(fileManager);
        BookService    bookService    = new BookService(fileManager);
        BorrowService  borrowService  = new BorrowService(fileManager, readerService, bookService);

        SwingUtilities.invokeLater(() ->
                new LoginFrame(accountService, readerService, bookService, borrowService));
    }
}