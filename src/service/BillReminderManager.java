package service;

import model.BillReminder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BillReminderManager {
    private static final String FILE_NAME = "bills.txt";

    public static void addReminder(BillReminder reminder) {
        File file = StorageHelper.resolve(FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(reminder.toFileString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<BillReminder> getAllReminders() {
        List<BillReminder> list = new ArrayList<>();
        File file = StorageHelper.resolve(FILE_NAME);

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(",", 4);
                if (data.length < 4) {
                    continue;
                }

                double amount = Double.parseDouble(data[2].trim());
                list.add(new BillReminder(data[0].trim(), data[1].trim(), amount, data[3].trim()));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        list.sort(Comparator.comparing(BillReminder::getDate));
        return list;
    }

    public static void updateReminder(int index, BillReminder reminder) {
        List<BillReminder> list = getAllReminders();

        if (index >= 0 && index < list.size()) {
            list.set(index, reminder);
            saveAll(list);
        }
    }

    public static void deleteReminder(int index) {
        List<BillReminder> list = getAllReminders();

        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveAll(list);
        }
    }

    private static void saveAll(List<BillReminder> list) {
        File file = StorageHelper.resolve(FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (BillReminder reminder : list) {
                bw.write(reminder.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
