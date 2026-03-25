package service;

import model.Income;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IncomeManager {
    private static final String FILE_NAME = "income.txt";

    public static void addIncome(Income income) {
        File file = StorageHelper.resolve(FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(income.toFileString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Income> getAllIncome() {
        List<Income> list = new ArrayList<>();
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

                String[] data = line.split(",", 3);
                if (data.length < 3) {
                    continue;
                }

                list.add(new Income(data[0].trim(), data[1].trim(), Double.parseDouble(data[2].trim())));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return list;
    }
}
