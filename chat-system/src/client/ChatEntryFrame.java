package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.net.Socket;

public class ChatEntryFrame extends JFrame {

    private JTextField nicknameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton cancelButton;
    private Socket socket;

    public ChatEntryFrame() {
        setTitle("登录 - 局域网聊天室");
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
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // 2 行 2 列，水平和垂直间隔为 10
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

        // 将标签和输入框添加到 topPanel 中
        topPanel.add(nicknameLabel);
        topPanel.add(nicknameField);
        topPanel.add(passwordLabel);
        topPanel.add(passwordField);
        //设置居中
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // 添加 topPanel 到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.decode("#F0F0F0"));

        loginButton = new JButton("登录");
        loginButton.setFont(new Font("楷体", Font.BOLD, 16));
        loginButton.setBackground(Color.decode("#007BFF"));
        loginButton.setForeground(Color.WHITE);

        registerButton = new JButton("注册");
        registerButton.setFont(new Font("楷体", Font.BOLD, 16));
        registerButton.setBackground(Color.decode("#28A745"));
        registerButton.setForeground(Color.WHITE);

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("楷体", Font.BOLD, 16));
        cancelButton.setBackground(Color.decode("#DC3545"));
        cancelButton.setForeground(Color.WHITE);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 登录按钮监听器
        loginButton.addActionListener(e -> {
            String username = nicknameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入用户名和密码!");
            } else {
                if (AccountManager.login(username, password)) {
                    try {
                        socket = new Socket(Constant.getServerIP(), Integer.parseInt(Constant.getServerPort()));
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF(MegType.LOGING.name()); // 登录消息类型
                        dos.writeUTF(username);
                        dos.flush();
                        new ClientChatFrame(username, socket);
                        this.dispose(); // 关闭登录窗口
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "登录失败: 用户名或密码错误");
                }
            }
        });

        // 注册按钮监听器
        registerButton.addActionListener(e -> {
            // 跳转到注册界面
            new RegisterFrame();
            this.dispose(); // 关闭登录窗口
        });

        // 取消按钮监听器，回到输入端口界面
        cancelButton.addActionListener(e -> {
            new EntryIpandPORT();
            this.dispose(); // 关闭登录窗口
        });

        this.setVisible(true); // 显示窗口
    }

    public static void main(String[] args) {
        new ChatEntryFrame();
    }
}
