package client;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class RegisterFrame extends JFrame {

    private JTextField nicknameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;

    public RegisterFrame() {
        setTitle("注册 - 局域网聊天室");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 设置背景颜色
        getContentPane().setBackground(Color.decode("#F0F0F0"));

        // 创建主面板并设置布局
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.decode("#F0F0F0"));
        add(mainPanel);

        // 创建顶部面板
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 10, 10)); // 4 行 2 列，水平和垂直间隔为 10
        topPanel.setBackground(Color.decode("#F0F0F0"));

        // 标签和文本框
        JLabel nicknameLabel = new JLabel("用户名:");
        nicknameLabel.setFont(new Font("楷体", Font.BOLD, 16));
        nicknameField = new JTextField(10);
        nicknameField.setFont(new Font("楷体", Font.PLAIN, 16));

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("楷体", Font.BOLD, 16));
        passwordField = new JPasswordField(10);
        passwordField.setFont(new Font("楷体", Font.PLAIN, 16));

        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setFont(new Font("楷体", Font.BOLD, 16));
        confirmPasswordField = new JPasswordField(10);
        confirmPasswordField.setFont(new Font("楷体", Font.PLAIN, 16));

        // 将标签和输入框添加到 topPanel 中
        topPanel.add(nicknameLabel);
        topPanel.add(nicknameField);
        topPanel.add(passwordLabel);
        topPanel.add(passwordField);
        topPanel.add(confirmPasswordLabel);
        topPanel.add(confirmPasswordField);

        // 添加 topPanel 到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.decode("#F0F0F0"));

        registerButton = new JButton("注册");
        registerButton.setFont(new Font("楷体", Font.BOLD, 16));
        registerButton.setBackground(Color.decode("#28A745"));
        registerButton.setForeground(Color.WHITE);

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("楷体", Font.BOLD, 16));
        cancelButton.setBackground(Color.decode("#DC3545"));
        cancelButton.setForeground(Color.WHITE);

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 注册按钮监听器
        registerButton.addActionListener(e -> {
            String username = nicknameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // 输入验证
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请填写所有字段!");
                return;
            }

            // 用户名长度验证
            if (username.length() < 3 || username.length() > 15) {
                JOptionPane.showMessageDialog(this, "用户名长度应为 3 到 15 个字符!");
                return;
            }

            // 密码长度验证
            if (password.length() < 6 || password.length() > 20) {
                JOptionPane.showMessageDialog(this, "密码长度应为 6 到 20 个字符!");
                return;
            }

            // 密码一致性检查
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "密码不一致，请重新输入!");
                return;
            }

            // 注册逻辑
            if (AccountManager.register(username, password)) {
                JOptionPane.showMessageDialog(this, "注册成功，请登录!");
                new ChatEntryFrame();
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "注册失败: 用户名已存在");
            }
        });

        // 取消按钮监听器
        cancelButton.addActionListener(e -> {
            new ChatEntryFrame(); // 返回登录界面
            this.dispose(); // 关闭当前注册界面
        });

        this.setVisible(true); // 显示窗口
    }

    public static void main(String[] args) {
        new RegisterFrame();
    }
}
