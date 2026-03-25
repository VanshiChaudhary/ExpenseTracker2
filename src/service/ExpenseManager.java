package service;

import model.Expense;
import java.io.*;
import java.util.*;

public class ExpenseManager {

    private static final String FILE_NAME = "expenses.txt";

    // Add Expense
    public static void addExpense(Expense expense) {
        File file = StorageHelper.resolve(FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(expense.toFileString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get All Expenses
    public static List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
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

                // IMPORTANT: order must match your Expense.java
                String date = data[0].trim();
                String category = data[1].trim();
                double amount = Double.parseDouble(data[2].trim());
                String description = data[3].trim();

                list.add(new Expense(date, category, amount, description));
            }

        } catch (IOException | NumberFormatException e) {
            // file may not exist initially
        }

        return list;
    }

    // Delete Expense (simple version)
    public static void deleteExpense(int index) {
        List<Expense> list = getAllExpenses();

        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveAll(list);
        }
    }

    public static void updateExpense(int index, Expense expense) {
        List<Expense> list = getAllExpenses();

        if (index >= 0 && index < list.size()) {
            list.set(index, expense);
            saveAll(list);
        }
    }

    // Save all (used after delete/edit)
    private static void saveAll(List<Expense> list) {
        File file = StorageHelper.resolve(FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Expense e : list) {
                bw.write(e.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
