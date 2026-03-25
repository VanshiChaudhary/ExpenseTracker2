package model;

public class BillReminder {
    private final String date;
    private final String title;
    private final double amount;
    private final String note;

    public BillReminder(String date, String title, double amount, String note) {
        this.date = date;
        this.title = title;
        this.amount = amount;
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public double getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    public String toFileString() {
        return date + "," + title + "," + amount + "," + note;
    }
}
