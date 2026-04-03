package service;

import util.FileManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


public class AccountService {

    private final FileManager fileManager;
    private Map<String, String> accounts; // username → passwordHash
    private String currentUser;           // null = chưa đăng nhập

    public AccountService(FileManager fileManager) {
        this.fileManager = fileManager;
        this.accounts    = fileManager.readAccounts();
        this.currentUser = null;
    }

    public boolean createAccount(String username, String password) {
        username = username.trim();
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Username và password không được để trống.");
        }
        if (accounts.containsKey(username)) {
            return false; // Username đã tồn tại
        }
        accounts.put(username, hash(password));
        fileManager.writeAccounts(accounts);
        return true;
    }


    public boolean login(String username, String password) {
        String storedHash = accounts.get(username.trim());
        if (storedHash == null) return false;
        if (!storedHash.equals(hash(password))) return false;

        currentUser = username.trim();
        return true;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn()      { return currentUser != null; }
    public String  getCurrentUser()  { return currentUser; }

    public boolean hasAnyAccount()   { return !accounts.isEmpty(); }


    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 không khả dụng.", e);
        }
    }
}