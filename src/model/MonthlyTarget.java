package model;

public class MonthlyTarget {
    private final String monthKey;
    private final double amount;

    public MonthlyTarget(String monthKey, double amount) {
        this.monthKey = monthKey;
        this.amount = amount;
    }

    public String getMonthKey() {
        return monthKey;
    }

    public double getAmount() {
        return amount;
    }

    public String toFileString() {
        return monthKey + "," + amount;
    }
}
