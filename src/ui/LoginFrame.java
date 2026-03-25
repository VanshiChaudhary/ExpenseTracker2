package ui;

import service.FileManager;
import javax.swing.*;

public class LoginFrame extends JFrame {

    JTextField userIdField;
    JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Login");
        setSize(300, 200);
        setLayout(null);

        JLabel userLabel = new JLabel("User ID:");
        userLabel.setBounds(30, 30, 80, 25);
        add(userLabel);

        userIdField = new JTextField();
        userIdField.setBounds(120, 30, 120, 25);
        add(userIdField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 70, 80, 25);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(120, 70, 120, 25);
        add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(90, 110, 100, 30);
        add(loginBtn);

        loginBtn.addActionListener(e -> login());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void login() {
        String userId = userIdField.getText();
        String password = new String(passwordField.getPassword());

        if (FileManager.validateLogin(userId, password)) {
            JOptionPane.showMessageDialog(this, "Login Successful!");

            new ExpenseFrame(); // 🔥 opens next screen
            dispose(); // 🔥 closes login

        } else {
            JOptionPane.showMessageDialog(this, "Invalid Credentials!");
        }
    }
}