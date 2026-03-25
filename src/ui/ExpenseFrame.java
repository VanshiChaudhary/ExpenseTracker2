package ui;

import model.BillReminder;
import model.Expense;
import model.Income;
import model.MonthlyTarget;
import service.BillReminderManager;
import service.ExpenseManager;
import service.IncomeManager;
import service.MonthlyTargetManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseFrame extends JFrame {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY = new DecimalFormat("0.00");

    private final JTextField expenseDateField = new JTextField(todayText());
    private final JTextField categoryField = new JTextField();
    private final JTextField expenseAmountField = new JTextField();
    private final JTextField descField = new JTextField();
    private final JTextField incomeDateField = new JTextField(todayText());
    private final JTextField incomeSourceField = new JTextField();
    private final JTextField incomeAmountField = new JTextField();
    private final JTextField billTitleField = new JTextField();
    private final JTextField billDateField = new JTextField(todayText());
    private final JTextField billAmountField = new JTextField();
    private final JTextField billNoteField = new JTextField();
    private final JTextField monthlyTargetField = new JTextField();
    private final JTabbedPane tabs = new JTabbedPane();

    private final DefaultTableModel expenseModel = new DefaultTableModel(new String[]{"Date", "Category", "Amount", "Description"}, 0);
    private final DefaultTableModel billModel = new DefaultTableModel(new String[]{"Due Date", "Bill", "Amount", "Note"}, 0);
    private final JTable expenseTable = new JTable(expenseModel);
    private final JTable billTable = new JTable(billModel);

    private final JLabel totalMoneyLabel = statLabel();
    private final JLabel expenditureLabel = statLabel();
    private final JLabel savingsLabel = statLabel();
    private final JLabel upcomingBillLabel = statLabel();
    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel monthlyTargetStatusLabel = new JLabel("Set a monthly target to track spending.");
    private final JLabel targetCaptionLabel = new JLabel("Monthly Spending Target");
    private final JProgressBar monthlyTargetProgress = new JProgressBar();
    private final JToggleButton themeToggle = new JToggleButton("Dark Mode");

    private final CalendarPanel calendarPanel = new CalendarPanel();
    private final ChartPanel financeOverviewChart = new ChartPanel("Money / Spending / Savings", new Color(90, 103, 216));
    private final ChartPanel dailyChartPanel = new ChartPanel("Daily Spending (7 Days)", new Color(239, 126, 77));
    private final ChartPanel monthlyChartPanel = new ChartPanel("Monthly Spending (6 Months)", new Color(64, 145, 108));
    private final PieChartPanel categoryPieChart = new PieChartPanel("Expense By Category");

    private final List<JComponent> cards = new ArrayList<>();
    private final List<JComponent> panels = new ArrayList<>();
    private final List<JLabel> sectionLabels = new ArrayList<>();
    private final List<JComponent> inputFields = new ArrayList<>();
    private final List<AbstractButton> actionButtons = new ArrayList<>();

    private JPanel rootPanel;
    private JPanel summaryPanel;
    private List<Expense> expenses = new ArrayList<>();
    private List<Income> incomes = new ArrayList<>();
    private List<BillReminder> reminders = new ArrayList<>();
    private YearMonth visibleMonth = YearMonth.now();
    private LocalDate selectedCalendarDate = LocalDate.now();
    private boolean darkMode;

    public ExpenseFrame() {
        setTitle("Expense Tracker Dashboard");
        setSize(1450, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        monthlyChartPanel.setBottomPadding(52);

        rootPanel = new JPanel(new BorderLayout(18, 18));
        rootPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(rootPanel);

        rootPanel.add(buildTopPanel(), BorderLayout.NORTH);
        rootPanel.add(buildMainPanel(), BorderLayout.CENTER);

        configureTables();
        configureTabs();
        loadData();
        refreshAll();
        applyTheme(false);
        setVisible(true);
    }

    private JPanel buildTopPanel() {
        JPanel container = new JPanel(new BorderLayout(12, 8));
        container.setOpaque(false);

        JLabel heading = new JLabel("Expense Planner Dashboard");
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));
        heading.setForeground(new Color(220, 96, 60));

        JLabel subtitle = new JLabel("Track expenses, add money, plan bills, and stay inside your monthly target.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(new Color(67, 90, 111));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(heading);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        themeToggle.addActionListener(e -> applyTheme(themeToggle.isSelected()));
        styleButton(themeToggle, new Color(38, 70, 83), Color.WHITE);
        themeToggle.setPreferredSize(new Dimension(130, 34));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(titleBlock, BorderLayout.WEST);

        JPanel toggleWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toggleWrap.setOpaque(false);
        toggleWrap.add(themeToggle);
        topRow.add(toggleWrap, BorderLayout.EAST);

        summaryPanel = buildSummaryPanel();
        container.add(topRow, BorderLayout.NORTH);
        container.add(summaryPanel, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setOpaque(false);
        panel.add(card("Money We Have", totalMoneyLabel, new Color(42, 157, 143)));
        panel.add(card("Total Expenditure", expenditureLabel, new Color(231, 111, 81)));
        panel.add(card("Savings Left", savingsLabel, new Color(61, 64, 91)));
        panel.add(card("Upcoming Bill", upcomingBillLabel, new Color(233, 196, 106)));
        return panel;
    }

    private JPanel buildMainPanel() {
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("Dashboard", buildDashboardPanel());
        tabs.addTab("Calendar", wrapScrollable(buildPlannerPanel()));
        tabs.addTab("Bar Graphs", wrapScrollable(buildChartsPanel()));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = whitePanel(new BorderLayout(12, 12));

        JPanel forms = new JPanel(new GridLayout(1, 2, 12, 0));
        forms.setOpaque(false);
        forms.add(buildExpenseForm());
        forms.add(buildIncomeForm());

        JPanel targetPanel = buildTargetPanel();

        JPanel topStack = new JPanel(new BorderLayout(0, 12));
        topStack.setOpaque(false);
        topStack.add(forms, BorderLayout.NORTH);
        topStack.add(targetPanel, BorderLayout.SOUTH);

        JPanel history = new JPanel(new BorderLayout(8, 8));
        history.setOpaque(false);
        history.add(title("Expense History"), BorderLayout.NORTH);
        expenseTable.setRowHeight(24);
        expenseTable.getTableHeader().setReorderingAllowed(false);
        history.add(new JScrollPane(expenseTable), BorderLayout.CENTER);

        JButton deleteButton = new JButton("Delete Selected Expense");
        deleteButton.addActionListener(this::deleteExpense);
        styleButton(deleteButton, new Color(231, 111, 81), Color.WHITE);
        JButton editButton = new JButton("Edit Selected Expense");
        editButton.addActionListener(this::editExpense);
        styleButton(editButton, new Color(44, 106, 139), Color.WHITE);
        JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrap.setOpaque(false);
        buttonWrap.add(editButton);
        buttonWrap.add(Box.createHorizontalStrut(8));
        buttonWrap.add(deleteButton);
        history.add(buttonWrap, BorderLayout.SOUTH);

        panel.add(topStack, BorderLayout.NORTH);
        panel.add(history, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildChartsPanel() {
        JPanel panel = whitePanel(new BorderLayout(12, 12));
        panel.setPreferredSize(new Dimension(1180, 760));

        JLabel title = title("Expense Bar Graphs");
        panel.add(title, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(2, 2, 12, 12));
        charts.setOpaque(false);
        charts.add(financeOverviewChart);
        charts.add(dailyChartPanel);
        charts.add(monthlyChartPanel);
        charts.add(categoryPieChart);

        panel.add(charts, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPlannerPanel() {
        JPanel panel = whitePanel(new BorderLayout(12, 12));
        panel.setPreferredSize(new Dimension(1180, 720));

        JPanel calendarContainer = whitePanel(new BorderLayout(8, 8));
        calendarContainer.add(buildCalendarHeader(), BorderLayout.NORTH);
        calendarContainer.add(calendarPanel, BorderLayout.CENTER);
        calendarContainer.setPreferredSize(new Dimension(540, 620));
        calendarContainer.setMinimumSize(new Dimension(320, 420));

        JPanel details = new JPanel(new BorderLayout(10, 10));
        details.setOpaque(false);
        details.add(buildBillForm(), BorderLayout.NORTH);

        JPanel bills = new JPanel(new BorderLayout(8, 8));
        bills.setOpaque(false);
        bills.add(title("Bills To Pay"), BorderLayout.NORTH);
        billTable.setRowHeight(24);
        billTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setPreferredSize(new Dimension(0, 260));
        bills.add(scrollPane, BorderLayout.CENTER);
        JButton editBillButton = new JButton("Edit Selected Bill");
        editBillButton.addActionListener(this::editReminder);
        styleButton(editBillButton, new Color(111, 66, 193), Color.WHITE);
        JPanel billsButtonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        billsButtonWrap.setOpaque(false);
        billsButtonWrap.add(editBillButton);
        billsButtonWrap.add(Box.createHorizontalStrut(8));
        JButton deleteBillButton = new JButton("Delete Selected Bill");
        deleteBillButton.addActionListener(this::deleteReminder);
        styleButton(deleteBillButton, new Color(199, 77, 52), Color.WHITE);
        billsButtonWrap.add(deleteBillButton);
        bills.add(billsButtonWrap, BorderLayout.SOUTH);
        details.add(bills, BorderLayout.CENTER);
        details.setPreferredSize(new Dimension(540, 620));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, calendarContainer, details);
        splitPane.setResizeWeight(0.50);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(0.5);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane wrapScrollable(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void configureTables() {
        styleTable(expenseTable);
        styleTable(billTable);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setDefaultRenderer(Object.class, new StripedTableCellRenderer());
    }

    private void configureTabs() {
        tabs.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(new Color(244, 241, 222));
        tabs.setForeground(new Color(52, 62, 84));
    }

    private JPanel buildExpenseForm() {
        JPanel panel = formPanel("Add Expense");
        addField(panel, "Date (dd/MM/yyyy)", expenseDateField);
        addField(panel, "Category", categoryField);
        addField(panel, "Amount", expenseAmountField);
        addField(panel, "Description", descField);
        JButton button = new JButton("Add Expense");
        button.addActionListener(this::addExpense);
        styleButton(button, new Color(215, 91, 59), Color.WHITE);
        panel.add(button);
        return panel;
    }

    private JPanel buildIncomeForm() {
        JPanel panel = formPanel("Add Money");
        addField(panel, "Date (dd/MM/yyyy)", incomeDateField);
        addField(panel, "Source", incomeSourceField);
        addField(panel, "Amount", incomeAmountField);
        panel.add(Box.createVerticalStrut(28));
        JButton button = new JButton("Add Money");
        button.addActionListener(this::addIncome);
        styleButton(button, new Color(34, 139, 123), Color.WHITE);
        panel.add(button);
        return panel;
    }

    private JPanel buildTargetPanel() {
        JPanel panel = whitePanel(new BorderLayout(12, 10));

        JLabel heading = title("Monthly Spending Target");
        heading.setForeground(new Color(110, 61, 161));
        targetCaptionLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        targetCaptionLabel.setForeground(new Color(80, 86, 133));

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setOpaque(false);
        monthlyTargetField.setText(MONEY.format(MonthlyTargetManager.getTarget(currentMonthKey())));
        styleTextField(monthlyTargetField);
        inputPanel.add(monthlyTargetField, BorderLayout.CENTER);

        JButton saveTargetButton = new JButton("Save Target");
        saveTargetButton.addActionListener(this::saveMonthlyTarget);
        styleButton(saveTargetButton, new Color(111, 66, 193), Color.WHITE);
        inputPanel.add(saveTargetButton, BorderLayout.EAST);

        monthlyTargetProgress.setStringPainted(true);
        monthlyTargetProgress.setMinimum(0);
        monthlyTargetProgress.setForeground(new Color(111, 66, 193));

        monthlyTargetStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        monthlyTargetStatusLabel.setForeground(new Color(72, 79, 104));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(targetCaptionLabel, BorderLayout.CENTER);
        top.add(inputPanel, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(monthlyTargetProgress, BorderLayout.CENTER);
        panel.add(monthlyTargetStatusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBillForm() {
        JPanel panel = whitePanel(new GridLayout(2, 5, 10, 10));
        panel.add(colorLabel("Bill", new Color(199, 77, 52)));
        panel.add(colorLabel("Due Date", new Color(56, 96, 125)));
        panel.add(colorLabel("Amount", new Color(42, 140, 121)));
        panel.add(colorLabel("Note", new Color(111, 66, 193)));
        panel.add(new JLabel(""));
        panel.add(billTitleField);
        panel.add(billDateField);
        panel.add(billAmountField);
        panel.add(billNoteField);
        JButton button = new JButton("Add Reminder");
        button.addActionListener(this::addReminder);
        styleButton(button, new Color(38, 70, 83), Color.WHITE);
        panel.add(button);
        registerFields(billTitleField, billDateField, billAmountField, billNoteField);
        return panel;
    }

    private JPanel buildCalendarHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        styleButton(prev, new Color(44, 106, 139), Color.WHITE);
        styleButton(next, new Color(44, 106, 139), Color.WHITE);
        prev.addActionListener(e -> moveMonth(-1));
        next.addActionListener(e -> moveMonth(1));
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        monthLabel.setForeground(new Color(199, 77, 52));
        panel.add(prev, BorderLayout.WEST);
        panel.add(monthLabel, BorderLayout.CENTER);
        panel.add(next, BorderLayout.EAST);
        return panel;
    }

    private void loadData() {
        expenses = ExpenseManager.getAllExpenses();
        incomes = IncomeManager.getAllIncome();
        reminders = BillReminderManager.getAllReminders();
    }

    private void refreshAll() {
        updateMonthLabel();
        refreshExpenseTable();
        refreshBillTable();
        refreshStats();
        refreshMonthlyTarget();
        financeOverviewChart.setData(buildFinanceOverviewData());
        dailyChartPanel.setData(buildDailyData());
        monthlyChartPanel.setData(buildMonthlyData());
        categoryPieChart.setData(buildCategoryData());
        calendarPanel.repaint();
    }

    private void addExpense(ActionEvent event) {
        try {
            Expense expense = new Expense(normalizeDate(expenseDateField.getText()), categoryField.getText().trim(),
                    Double.parseDouble(expenseAmountField.getText().trim()), descField.getText().trim());
            if (expense.getCategory().isEmpty() || expense.getDescription().isEmpty()) {
                throw new IllegalArgumentException();
            }
            ExpenseManager.addExpense(expense);
            expenses.add(expense);
            expenseDateField.setText(todayText());
            categoryField.setText("");
            expenseAmountField.setText("");
            descField.setText("");
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter valid expense details and date like 22/03/2026.");
        }
    }

    private void addIncome(ActionEvent event) {
        try {
            Income income = new Income(normalizeDate(incomeDateField.getText()), incomeSourceField.getText().trim(),
                    Double.parseDouble(incomeAmountField.getText().trim()));
            if (income.getSource().isEmpty()) {
                throw new IllegalArgumentException();
            }
            IncomeManager.addIncome(income);
            incomes.add(income);
            incomeDateField.setText(todayText());
            incomeSourceField.setText("");
            incomeAmountField.setText("");
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter valid money details and date like 22/03/2026.");
        }
    }

    private void addReminder(ActionEvent event) {
        try {
            BillReminder reminder = new BillReminder(normalizeDate(billDateField.getText()), billTitleField.getText().trim(),
                    Double.parseDouble(billAmountField.getText().trim()), billNoteField.getText().trim());
            if (reminder.getTitle().isEmpty()) {
                throw new IllegalArgumentException();
            }
            BillReminderManager.addReminder(reminder);
            reminders.add(reminder);
            billTitleField.setText("");
            billDateField.setText(todayText());
            billAmountField.setText("");
            billNoteField.setText("");
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter valid bill reminder details and date like 22/03/2026.");
        }
    }

    private void saveMonthlyTarget(ActionEvent event) {
        try {
            double amount = Double.parseDouble(monthlyTargetField.getText().trim());
            if (amount < 0) {
                throw new IllegalArgumentException();
            }
            MonthlyTargetManager.saveTarget(new MonthlyTarget(currentMonthKey(), amount));
            refreshMonthlyTarget();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid monthly target amount.");
        }
    }

    private void deleteExpense(ActionEvent event) {
        int row = expenseTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an expense row first.");
            return;
        }
        ExpenseManager.deleteExpense(row);
        expenses.remove(row);
        refreshAll();
    }

    private void deleteReminder(ActionEvent event) {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a bill row first.");
            return;
        }

        BillReminderManager.deleteReminder(row);
        reminders.remove(row);
        refreshAll();
    }

    private void editExpense(ActionEvent event) {
        int row = expenseTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an expense row first.");
            return;
        }

        Expense current = expenses.get(row);
        JTextField date = new JTextField(current.getDate());
        JTextField category = new JTextField(current.getCategory());
        JTextField amount = new JTextField(String.valueOf(current.getAmount()));
        JTextField description = new JTextField(current.getDescription());

        Object[] fields = {
                "Date (dd/MM/yyyy)", date,
                "Category", category,
                "Amount", amount,
                "Description", description
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Edit Expense", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Expense updated = new Expense(
                    normalizeDate(date.getText()),
                    category.getText().trim(),
                    Double.parseDouble(amount.getText().trim()),
                    description.getText().trim()
            );
            if (updated.getCategory().isEmpty() || updated.getDescription().isEmpty()) {
                throw new IllegalArgumentException();
            }
            ExpenseManager.updateExpense(row, updated);
            expenses.set(row, updated);
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter valid expense details before saving.");
        }
    }

    private void editReminder(ActionEvent event) {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a bill row first.");
            return;
        }

        BillReminder current = reminders.get(row);
        JTextField date = new JTextField(current.getDate());
        JTextField title = new JTextField(current.getTitle());
        JTextField amount = new JTextField(String.valueOf(current.getAmount()));
        JTextField note = new JTextField(current.getNote());

        Object[] fields = {
                "Bill", title,
                "Due Date (dd/MM/yyyy)", date,
                "Amount", amount,
                "Note", note
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Edit Bill Reminder", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            BillReminder updated = new BillReminder(
                    normalizeDate(date.getText()),
                    title.getText().trim(),
                    Double.parseDouble(amount.getText().trim()),
                    note.getText().trim()
            );
            if (updated.getTitle().isEmpty()) {
                throw new IllegalArgumentException();
            }
            BillReminderManager.updateReminder(row, updated);
            reminders.set(row, updated);
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter valid bill details before saving.");
        }
    }

    private void refreshExpenseTable() {
        expenseModel.setRowCount(0);
        for (Expense expense : expenses) {
            expenseModel.addRow(new Object[]{expense.getDate(), expense.getCategory(), MONEY.format(expense.getAmount()), expense.getDescription()});
        }
    }

    private void refreshBillTable() {
        billModel.setRowCount(0);
        reminders.sort((a, b) -> safeDate(a.getDate()).compareTo(safeDate(b.getDate())));
        for (BillReminder reminder : reminders) {
            billModel.addRow(new Object[]{reminder.getDate(), reminder.getTitle(), MONEY.format(reminder.getAmount()), reminder.getNote()});
        }
    }

    private void refreshStats() {
        double money = 0;
        double spent = 0;
        for (Income income : incomes) {
            money += income.getAmount();
        }
        for (Expense expense : expenses) {
            spent += expense.getAmount();
        }
        totalMoneyLabel.setText("Rs " + MONEY.format(money));
        expenditureLabel.setText("Rs " + MONEY.format(spent));
        savingsLabel.setText("Rs " + MONEY.format(money - spent));

        BillReminder next = null;
        for (BillReminder reminder : reminders) {
            LocalDate due = safeDate(reminder.getDate());
            if (!due.isBefore(LocalDate.now()) && (next == null || due.isBefore(safeDate(next.getDate())))) {
                next = reminder;
            }
        }
        upcomingBillLabel.setText(next == null ? "No upcoming bill" : next.getTitle() + " on " + next.getDate());
    }

    private void refreshMonthlyTarget() {
        double target = MonthlyTargetManager.getTarget(currentMonthKey());
        double spentThisMonth = currentMonthSpend();
        monthlyTargetField.setText(target > 0 ? MONEY.format(target) : "");
        targetCaptionLabel.setText("Target for " + YearMonth.now().getMonth().name() + " " + YearMonth.now().getYear());

        if (target <= 0) {
            monthlyTargetProgress.setMaximum(100);
            monthlyTargetProgress.setValue(0);
            monthlyTargetProgress.setString("No target set");
            monthlyTargetStatusLabel.setText("Add a target amount and save it to track your monthly budget.");
            monthlyTargetStatusLabel.setForeground(darkMode ? new Color(230, 214, 255) : new Color(72, 79, 104));
            return;
        }

        int progressValue = (int) Math.min(100, Math.round((spentThisMonth / target) * 100));
        monthlyTargetProgress.setMaximum(100);
        monthlyTargetProgress.setValue(progressValue);
        monthlyTargetProgress.setString(progressValue + "% used");

        if (spentThisMonth >= target) {
            monthlyTargetStatusLabel.setText("Target reached: you have spent Rs " + MONEY.format(spentThisMonth) + " out of Rs " + MONEY.format(target) + ".");
            monthlyTargetStatusLabel.setForeground(new Color(214, 69, 65));
        } else {
            double left = target - spentThisMonth;
            monthlyTargetStatusLabel.setText("On track: Rs " + MONEY.format(left) + " left before this month's target is reached.");
            monthlyTargetStatusLabel.setForeground(new Color(34, 139, 123));
        }
    }

    private double currentMonthSpend() {
        YearMonth currentMonth = YearMonth.now();
        double spent = 0;
        for (Expense expense : expenses) {
            if (YearMonth.from(safeDate(expense.getDate())).equals(currentMonth)) {
                spent += expense.getAmount();
            }
        }
        return spent;
    }

    private Map<String, Double> buildDailyData() {
        LinkedHashMap<String, Double> data = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            data.put(day.format(labelFormat), 0.0);
        }
        for (Expense expense : expenses) {
            LocalDate date = safeDate(expense.getDate());
            if (!date.isBefore(today.minusDays(6)) && !date.isAfter(today)) {
                String key = date.format(labelFormat);
                data.put(key, data.get(key) + expense.getAmount());
            }
        }
        return data;
    }

    private Map<String, Double> buildFinanceOverviewData() {
        LinkedHashMap<String, Double> data = new LinkedHashMap<>();
        double money = 0;
        double spent = 0;

        for (Income income : incomes) {
            money += income.getAmount();
        }
        for (Expense expense : expenses) {
            spent += expense.getAmount();
        }

        data.put("Money", money);
        data.put("Spent", spent);
        data.put("Savings", money - spent);
        return data;
    }

    private Map<String, Double> buildCategoryData() {
        LinkedHashMap<String, Double> data = new LinkedHashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory().trim();
            if (category.isEmpty()) {
                category = "Other";
            }
            data.put(category, data.getOrDefault(category, 0.0) + expense.getAmount());
        }
        return data;
    }

    private Map<String, Double> buildMonthlyData() {
        LinkedHashMap<String, Double> data = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);
        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            data.put(month.format(labelFormat), 0.0);
        }
        for (Expense expense : expenses) {
            YearMonth month = YearMonth.from(safeDate(expense.getDate()));
            String key = month.format(labelFormat);
            if (data.containsKey(key)) {
                data.put(key, data.get(key) + expense.getAmount());
            }
        }
        return data;
    }

    private void moveMonth(int delta) {
        visibleMonth = visibleMonth.plusMonths(delta);
        updateMonthLabel();
        calendarPanel.repaint();
    }

    private void updateMonthLabel() {
        monthLabel.setText(visibleMonth.getMonth().name() + " " + visibleMonth.getYear());
    }

    private JPanel whitePanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 215, 196)),
                new EmptyBorder(12, 12, 12, 12)));
        panels.add(panel);
        return panel;
    }

    private JPanel formPanel(String titleText) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(4, 6, 4, 6));
        JLabel label = title(titleText);
        label.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.add(label);
        return panel;
    }

    private void addField(JPanel panel, String labelText, JComponent field) {
        JLabel label = colorLabel(labelText, new Color(74, 86, 106));
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        panel.add(label);
        styleTextField(field);
        panel.add(field);
        panel.add(Box.createVerticalStrut(12));
    }

    private JPanel card(String heading, JLabel value, Color accent) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, accent),
                new EmptyBorder(10, 12, 10, 12)));
        JLabel label = new JLabel(heading);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        label.setForeground(accent);
        panel.add(label, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        cards.add(panel);
        return panel;
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(new Color(199, 77, 52));
        sectionLabels.add(label);
        return label;
    }

    private JLabel colorLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(color);
        sectionLabels.add(label);
        return label;
    }

    private static JLabel statLabel() {
        JLabel label = new JLabel("Rs 0.00");
        label.setFont(new Font("SansSerif", Font.BOLD, 21));
        return label;
    }

    private void styleTextField(JComponent field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        if (field instanceof JTextField textField) {
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 218, 232)),
                    new EmptyBorder(6, 8, 6, 8)));
        }
        inputFields.add(field);
    }

    private void registerFields(JComponent... fields) {
        inputFields.addAll(Arrays.asList(fields));
    }

    private void styleButton(AbstractButton button, Color bg, Color fg) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.putClientProperty("baseColor", bg);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(brighten((Color) button.getClientProperty("baseColor"), 18));
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground((Color) button.getClientProperty("baseColor"));
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(darken((Color) button.getClientProperty("baseColor"), 22));
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(button.contains(event.getPoint())
                            ? brighten((Color) button.getClientProperty("baseColor"), 18)
                            : (Color) button.getClientProperty("baseColor"));
                }
            }
        });
        actionButtons.add(button);
    }

    private static Color brighten(Color color, int amount) {
        return new Color(
                Math.min(255, color.getRed() + amount),
                Math.min(255, color.getGreen() + amount),
                Math.min(255, color.getBlue() + amount)
        );
    }

    private static Color darken(Color color, int amount) {
        return new Color(
                Math.max(0, color.getRed() - amount),
                Math.max(0, color.getGreen() - amount),
                Math.max(0, color.getBlue() - amount)
        );
    }

    private void applyTheme(boolean dark) {
        darkMode = dark;
        themeToggle.setText(dark ? "Light Mode" : "Dark Mode");

        Color rootBg = dark ? new Color(20, 24, 34) : new Color(244, 241, 222);
        Color cardBg = dark ? new Color(34, 39, 54) : Color.WHITE;
        Color panelBg = dark ? new Color(26, 31, 45) : Color.WHITE;
        Color borderColor = dark ? new Color(72, 83, 111) : new Color(224, 215, 196);
        Color textColor = dark ? new Color(234, 236, 241) : new Color(52, 62, 84);
        Color inputBg = dark ? new Color(45, 51, 68) : new Color(255, 255, 255);
        Color tabBg = dark ? new Color(31, 37, 52) : new Color(234, 236, 244);
        Color tabSelected = dark ? new Color(90, 103, 216) : new Color(90, 103, 216);

        rootPanel.setBackground(rootBg);
        summaryPanel.setBackground(rootBg);
        tabs.setBackground(rootBg);
        tabs.setForeground(textColor);
        tabs.setOpaque(true);

        for (JComponent panel : panels) {
            panel.setBackground(panelBg);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor),
                    new EmptyBorder(12, 12, 12, 12)));
        }

        for (JComponent card : cards) {
            card.setBackground(cardBg);
        }

        totalMoneyLabel.setForeground(dark ? new Color(126, 231, 208) : new Color(22, 101, 88));
        expenditureLabel.setForeground(dark ? new Color(255, 161, 138) : new Color(183, 64, 39));
        savingsLabel.setForeground(dark ? new Color(181, 190, 255) : new Color(54, 57, 105));
        upcomingBillLabel.setForeground(dark ? new Color(255, 223, 143) : new Color(162, 121, 15));
        monthLabel.setForeground(dark ? new Color(255, 173, 138) : new Color(199, 77, 52));
        targetCaptionLabel.setForeground(dark ? new Color(222, 185, 255) : new Color(80, 86, 133));

        for (JComponent field : inputFields) {
            field.setBackground(inputBg);
            field.setForeground(textColor);
            if (field instanceof JTextField textField) {
                textField.setCaretColor(textColor);
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(dark ? new Color(94, 104, 136) : new Color(210, 218, 232)),
                        new EmptyBorder(6, 8, 6, 8)));
            }
        }

        expenseTable.setBackground(inputBg);
        expenseTable.setForeground(textColor);
        expenseTable.setSelectionBackground(dark ? new Color(89, 105, 150) : new Color(205, 225, 255));
        expenseTable.setSelectionForeground(dark ? Color.WHITE : new Color(32, 40, 58));
        expenseTable.getTableHeader().setBackground(dark ? new Color(56, 63, 87) : new Color(236, 242, 255));
        expenseTable.getTableHeader().setForeground(textColor);
        expenseTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

        billTable.setBackground(inputBg);
        billTable.setForeground(textColor);
        billTable.setSelectionBackground(dark ? new Color(89, 105, 150) : new Color(205, 225, 255));
        billTable.setSelectionForeground(dark ? Color.WHITE : new Color(32, 40, 58));
        billTable.getTableHeader().setBackground(dark ? new Color(56, 63, 87) : new Color(236, 242, 255));
        billTable.getTableHeader().setForeground(textColor);
        billTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

        calendarPanel.setTheme(dark);
        financeOverviewChart.setTheme(dark);
        dailyChartPanel.setTheme(dark);
        monthlyChartPanel.setTheme(dark);
        categoryPieChart.setTheme(dark);
        monthlyTargetProgress.setBackground(dark ? new Color(49, 55, 74) : new Color(235, 236, 244));
        monthlyTargetProgress.setForeground(dark ? new Color(177, 136, 255) : new Color(111, 66, 193));
        monthlyTargetProgress.setBorder(BorderFactory.createLineBorder(borderColor));

        UIManager.put("TabbedPane.selected", tabSelected);
        UIManager.put("TabbedPane.contentAreaColor", tabBg);
        UIManager.put("TabbedPane.focus", tabSelected);
        UIManager.put("TabbedPane.light", tabBg);
        UIManager.put("TabbedPane.highlight", tabSelected);
        UIManager.put("TabbedPane.shadow", borderColor);
        UIManager.put("TabbedPane.darkShadow", borderColor.darker());
        SwingUtilities.updateComponentTreeUI(tabs);

        repaint();
    }

    private static String todayText() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    private static String normalizeDate(String value) {
        return safeDate(value).format(DATE_FORMAT);
    }

    private static LocalDate safeDate(String value) {
        String text = value == null ? "" : value.trim();
        List<DateTimeFormatter> formatters = Arrays.asList(
                DATE_FORMAT,
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/M/yyyy"),
                DateTimeFormatter.ofPattern("d/MM/yyyy"),
                DateTimeFormatter.BASIC_ISO_DATE
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        if (text.matches("\\d{2}/\\d{6}")) {
            try {
                return LocalDate.parse(text.substring(0, 3) + text.substring(3, 5) + "/" + text.substring(5), DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
            }
        }
        return LocalDate.now();
    }

    private static String shortText(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + ".";
    }

    private String currentMonthKey() {
        return YearMonth.now().toString();
    }

    private class CalendarPanel extends JPanel {
        private boolean dark;

        CalendarPanel() {
            setPreferredSize(new Dimension(0, 560));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    LocalDate clickedDate = getDateAtPoint(event.getPoint());
                    if (clickedDate != null) {
                        selectedCalendarDate = clickedDate;
                        billDateField.setText(clickedDate.format(DATE_FORMAT));
                        repaint();
                    }
                }
            });
        }

        void setTheme(boolean dark) {
            this.dark = dark;
            setBackground(dark ? new Color(30, 35, 49) : new Color(255, 252, 245));
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            int headerHeight = 34;
            int gridTop = headerHeight + 6;
            int gridHeight = Math.max(height - gridTop, 1);
            int cellWidth = Math.max(width / 7, 1);
            int cellHeight = Math.max(gridHeight / 6, 1);
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            for (int i = 0; i < days.length; i++) {
                g2.setColor(dark ? new Color(182, 205, 255) : new Color(61, 64, 91));
                g2.drawString(days[i], i * cellWidth + 12, 24);
            }

            LocalDate first = visibleMonth.atDay(1);
            int offset = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            if (offset < 0) {
                offset += 7;
            }
            LocalDate cursor = first.minusDays(offset);

            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 7; col++) {
                    int x = col * cellWidth;
                    int y = gridTop + row * cellHeight;
                    boolean currentMonth = cursor.getMonthValue() == visibleMonth.getMonthValue() && cursor.getYear() == visibleMonth.getYear();
                    g2.setColor(currentMonth ? (dark ? new Color(42, 47, 66) : Color.WHITE) : (dark ? new Color(25, 29, 40) : new Color(243, 239, 229)));
                    g2.fillRoundRect(x + 3, y + 3, cellWidth - 6, cellHeight - 6, 16, 16);
                    g2.setColor(dark ? new Color(81, 92, 122) : new Color(216, 208, 190));
                    g2.drawRoundRect(x + 3, y + 3, cellWidth - 6, cellHeight - 6, 16, 16);

                    if (cursor.equals(selectedCalendarDate)) {
                        g2.setColor(dark ? new Color(90, 103, 216, 110) : new Color(90, 103, 216, 60));
                        g2.fillRoundRect(x + 3, y + 3, cellWidth - 6, cellHeight - 6, 16, 16);
                    }

                    if (cursor.equals(LocalDate.now())) {
                        g2.setColor(dark ? new Color(71, 171, 156, 80) : new Color(42, 157, 143, 45));
                        g2.fillRoundRect(x + 3, y + 3, cellWidth - 6, cellHeight - 6, 16, 16);
                    }

                    g2.setColor(currentMonth ? (dark ? new Color(242, 245, 250) : new Color(38, 70, 83))
                            : (dark ? new Color(133, 141, 166) : new Color(150, 150, 150)));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                    g2.drawString(String.valueOf(cursor.getDayOfMonth()), x + 12, y + 24);

                    int bubbleY = y + 34;
                    int shown = 0;
                    int totalForDay = 0;
                    for (BillReminder reminder : reminders) {
                        if (safeDate(reminder.getDate()).equals(cursor)) {
                            totalForDay++;
                            if (shown < 2) {
                                g2.setColor(new Color(231, 111, 81));
                                g2.fillRoundRect(x + 10, bubbleY, Math.max(cellWidth - 20, 10), 18, 10, 10);
                                g2.setColor(Color.WHITE);
                                g2.drawString(shortText(reminder.getTitle(), 12), x + 16, bubbleY + 13);
                                bubbleY += 22;
                                shown++;
                            }
                        }
                    }
                    if (totalForDay > 2) {
                        g2.setColor(dark ? new Color(182, 205, 255) : new Color(61, 64, 91));
                        g2.drawString("+" + (totalForDay - 2) + " more", x + 12, y + cellHeight - 12);
                    }
                    cursor = cursor.plusDays(1);
                }
            }
            g2.dispose();
        }

        private LocalDate getDateAtPoint(Point point) {
            int width = getWidth();
            int height = getHeight();
            int headerHeight = 34;
            int gridTop = headerHeight + 6;
            if (point.y < gridTop) {
                return null;
            }

            int gridHeight = Math.max(height - gridTop, 1);
            int cellWidth = Math.max(width / 7, 1);
            int cellHeight = Math.max(gridHeight / 6, 1);
            int column = Math.min(point.x / cellWidth, 6);
            int row = Math.min((point.y - gridTop) / cellHeight, 5);

            LocalDate first = visibleMonth.atDay(1);
            int offset = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            if (offset < 0) {
                offset += 7;
            }

            return first.minusDays(offset).plusDays(row * 7L + column);
        }
    }

    private static class ChartPanel extends JPanel {
        private final String heading;
        private final Color color;
        private Map<String, Double> data = new LinkedHashMap<>();
        private boolean dark;
        private int bottomPadding = 34;

        ChartPanel(String heading, Color color) {
            this.heading = heading;
            this.color = color;
            setPreferredSize(new Dimension(420, 250));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(224, 215, 196)),
                    new EmptyBorder(10, 10, 10, 10)));
        }

        void setTheme(boolean dark) {
            this.dark = dark;
            setBackground(dark ? new Color(30, 35, 49) : new Color(255, 252, 245));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(dark ? new Color(72, 83, 111) : new Color(224, 215, 196)),
                    new EmptyBorder(10, 10, 10, 10)));
        }

        void setData(Map<String, Double> data) {
            this.data = data;
            repaint();
        }

        void setBottomPadding(int bottomPadding) {
            this.bottomPadding = bottomPadding;
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            int width = getWidth();
            int height = getHeight();
            int left = 18;
            int bottom = height - bottomPadding;
            int top = 34;
            g2.setColor(dark ? new Color(234, 236, 241) : new Color(38, 70, 83));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(heading, 10, 18);
            g2.setColor(dark ? new Color(94, 104, 136) : new Color(210, 210, 210));
            g2.drawLine(left, bottom, width - 10, bottom);

            double max = 0;
            for (double value : data.values()) {
                if (value > max) {
                    max = value;
                }
            }

            int count = Math.max(data.size(), 1);
            int gap = Math.max((width - left - 20) / count, 1);
            int barWidth = Math.max(gap - 10, 16);
            int index = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int x = left + index * gap + 4;
                int barHeight = max == 0 ? 0 : (int) ((entry.getValue() / max) * (bottom - top - 8));
                int y = bottom - barHeight;
                g2.setColor(color);
                g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);
                g2.setColor(dark ? new Color(220, 224, 232) : new Color(70, 70, 70));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.drawString(entry.getKey(), x, bottom + 18);
                g2.drawString("Rs " + Math.round(entry.getValue()), x, Math.max(y - 4, top));
                index++;
            }
            g2.dispose();
        }
    }

    private static class PieChartPanel extends JPanel {
        private final String heading;
        private Map<String, Double> data = new LinkedHashMap<>();
        private boolean dark;

        PieChartPanel(String heading) {
            this.heading = heading;
            setPreferredSize(new Dimension(420, 250));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(224, 215, 196)),
                    new EmptyBorder(10, 10, 10, 10)));
        }

        void setTheme(boolean dark) {
            this.dark = dark;
            setBackground(dark ? new Color(30, 35, 49) : new Color(255, 252, 245));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(dark ? new Color(72, 83, 111) : new Color(224, 215, 196)),
                    new EmptyBorder(10, 10, 10, 10)));
        }

        void setData(Map<String, Double> data) {
            this.data = data;
            repaint();
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            g2.setColor(dark ? new Color(234, 236, 241) : new Color(38, 70, 83));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(heading, 10, 18);

            if (data == null || data.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Add expenses to see category distribution.", 10, 42);
                g2.dispose();
                return;
            }

            Color[] palette = {
                    new Color(239, 126, 77),
                    new Color(64, 145, 108),
                    new Color(90, 103, 216),
                    new Color(233, 196, 106),
                    new Color(199, 77, 52),
                    new Color(111, 66, 193),
                    new Color(42, 157, 143)
            };

            double total = 0;
            for (double value : data.values()) {
                total += value;
            }

            int diameter = Math.min(width / 2, height - 70);
            diameter = Math.max(diameter, 120);
            int pieX = 16;
            int pieY = Math.max(34, (height - diameter) / 2);

            int startAngle = 0;
            int index = 0;
            int legendX = pieX + diameter + 20;
            int legendY = 46;

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double value = entry.getValue();
                int arcAngle = total == 0 ? 0 : (int) Math.round((value / total) * 360);
                Color color = palette[index % palette.length];
                g2.setColor(color);
                g2.fillArc(pieX, pieY, diameter, diameter, startAngle, arcAngle);
                startAngle += arcAngle;

                g2.fillRoundRect(legendX, legendY - 10, 12, 12, 4, 4);
                g2.setColor(dark ? new Color(220, 224, 232) : new Color(70, 70, 70));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                int percent = total == 0 ? 0 : (int) Math.round((value / total) * 100);
                g2.drawString(entry.getKey() + " - " + percent + "%", legendX + 18, legendY);
                g2.drawString("Rs " + Math.round(value), legendX + 18, legendY + 14);
                legendY += 34;
                index++;
            }

            g2.dispose();
        }
    }

    private class StripedTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            component.setFont(new Font("SansSerif", Font.PLAIN, 12));
            setBorder(new EmptyBorder(0, 10, 0, 10));

            if (isSelected) {
                component.setForeground(table.getSelectionForeground());
                component.setBackground(table.getSelectionBackground());
            } else {
                component.setForeground(darkMode ? new Color(228, 232, 240) : new Color(55, 63, 79));
                component.setBackground(row % 2 == 0
                        ? (darkMode ? new Color(39, 45, 60) : new Color(250, 251, 255))
                        : (darkMode ? new Color(31, 37, 50) : Color.WHITE));
            }

            return component;
        }
    }
}
