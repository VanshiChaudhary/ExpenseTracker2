package model;

public class Income {
    private final String date;
    private final String source;
    private final double amount;

    public Income(String date, String source, double amount) {
        this.date = date;
        this.source = source;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public double getAmount() {
        return amount;
    }

    public String toFileString() {
        return date + "," + source + "," + amount;
    }
}
