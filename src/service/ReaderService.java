package service;

import model.Reader;
import util.FileManager;
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


}