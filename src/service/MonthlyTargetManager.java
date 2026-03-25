package service;

import model.MonthlyTarget;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MonthlyTargetManager {
    private static final String FILE_NAME = "targets.txt";

    public static double getTarget(String monthKey) {
        return loadTargets().getOrDefault(monthKey, 0.0);
    }

    public static void saveTarget(MonthlyTarget target) {
        Map<String, Double> targets = loadTargets();
        targets.put(target.getMonthKey(), target.getAmount());
        File file = StorageHelper.resolve(FILE_NAME);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Double> entry : targets.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Double> loadTargets() {
        Map<String, Double> targets = new LinkedHashMap<>();
        File file = StorageHelper.resolve(FILE_NAME);

        if (!file.exists()) {
            return targets;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(",", 2);
                if (data.length < 2) {
                    continue;
                }

                targets.put(data[0].trim(), Double.parseDouble(data[1].trim()));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return targets;
    }
}
