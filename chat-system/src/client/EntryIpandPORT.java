package client;

import javax.swing.*;
import java.awt.*;

public class EntryIpandPORT extends JFrame {
    private JTextField ipField;   // IP 输入框
    private JTextField portField; // 端口输入框
    private JButton confirmButton;

    public EntryIpandPORT() {
        // 设置窗口基本属性
        setTitle("请输入服务器 IP 和端口");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // 窗口居中显示

        // 创建输入面板
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));  // 设置网格布局（3行2列）

        // IP 输入框
        JLabel ipLabel = new JLabel("服务器 IP:");
        ipField = new JTextField("127.0.0.1");  // 默认值

        // 端口输入框
        JLabel portLabel = new JLabel("端口号:");
        portField = new JTextField("6666");  // 默认值

        // 确认按钮
        confirmButton = new JButton("确认");

        // 将组件添加到面板
        panel.add(ipLabel);
        panel.add(ipField);
        panel.add(portLabel);
        panel.add(portField);
        panel.add(new JLabel());  // 空白标签占位
        panel.add(confirmButton);

        // 将面板添加到框架中
        getContentPane().add(panel, BorderLayout.CENTER);

        // 点击按钮后的处理逻辑
        confirmButton.addActionListener(e -> {
            String serverIP = ipField.getText().trim();
            String serverPort = portField.getText().trim();
            try {
                // 创建 Constant 对象来保存 IP 和端口
                new Constant(serverIP, serverPort);
                // 启动 ChatEntryFrame
                new ChatEntryFrame();
                dispose();  // 关闭当前窗口
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "端口号格式错误！请重新输入。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 显示窗口
        setVisible(true);
    }

    public static void main(String[] args) {
        // 启动 IP 和端口输入窗口
        new EntryIpandPORT();
    }
}
