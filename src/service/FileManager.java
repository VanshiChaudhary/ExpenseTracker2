package service;

import model.User;
import java.io.*;

public class FileManager {
    private static final String FILE_NAME = "users.txt";

    public static void saveUser(User user) {
        File file = StorageHelper.resolve(FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(user.toFileString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean userExists(String userId) {
        File file = StorageHelper.resolve(FILE_NAME);
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[1].equals(userId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // file may not exist yet, ignore
        }
        return false;
    }

    public static boolean validateLogin(String userId, String password) {
        File file = StorageHelper.resolve(FILE_NAME);
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[1].equals(userId) && data[2].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // file may not exist yet
        }
        return false;
    }
}
