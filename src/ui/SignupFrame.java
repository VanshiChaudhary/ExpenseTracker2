package ui;

import model.User;
import service.FileManager;

import javax.swing.*;

public class SignupFrame extends JFrame {

    JTextField emailField, userIdField;
    JPasswordField passwordField;

    public SignupFrame() {
        setTitle("Sign Up");
        setSize(300, 250);
        setLayout(null);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 30, 80, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(120, 30, 120, 25);
        add(emailField);

        JLabel userLabel = new JLabel("User ID:");
        userLabel.setBounds(30, 70, 80, 25);
        add(userLabel);

        userIdField = new JTextField();
        userIdField.setBounds(120, 70, 120, 25);
        add(userIdField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 110, 80, 25);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(120, 110, 120, 25);
        add(passwordField);

        JButton signupBtn = new JButton("Sign Up");
        signupBtn.setBounds(90, 150, 100, 30);
        add(signupBtn);

        signupBtn.addActionListener(e -> signup());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void signup() {
        String email = emailField.getText();
        String userId = userIdField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required!");
            return;
        }

        if (FileManager.userExists(userId)) {
            JOptionPane.showMessageDialog(this, "User already exists!");
            return;
        }

        FileManager.saveUser(new User(email, userId, password));
        JOptionPane.showMessageDialog(this, "Signup Successful!");

        new LoginFrame();
        dispose();
    }
}