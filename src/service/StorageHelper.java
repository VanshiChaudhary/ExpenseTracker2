package service;

import java.io.File;

public final class StorageHelper {
    private StorageHelper() {
    }

    public static File resolve(String fileName) {
        File workspaceFile = new File(fileName);
        if (workspaceFile.exists()) {
            return workspaceFile;
        }

        File srcFile = new File("src", fileName);
        if (srcFile.exists()) {
            return srcFile;
        }

        return srcFile;
    }
}
