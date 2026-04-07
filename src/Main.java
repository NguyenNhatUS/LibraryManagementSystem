import service.AccountService;
import service.BookService;
import service.BorrowService;
import service.ReaderService;
import ui.LoginFrame;
import util.FileManager;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager("data/");

        AccountService accountService = new AccountService(fileManager);
        ReaderService readerService  = new ReaderService(fileManager);
        BookService bookService    = new BookService(fileManager);
        BorrowService borrowService  = new BorrowService(fileManager, readerService, bookService);

        SwingUtilities.invokeLater(() ->
                new LoginFrame(accountService, readerService, bookService, borrowService));
    }
}