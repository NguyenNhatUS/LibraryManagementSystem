import service.AccountService;
import ui.LoginFrame;
import util.FileManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager("data/");
        AccountService accountService = new AccountService(fileManager);
        SwingUtilities.invokeLater(() -> new LoginFrame(accountService));

        System.out.println("hi");

    }
}