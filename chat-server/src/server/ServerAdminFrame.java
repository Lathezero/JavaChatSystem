package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerAdminFrame extends JFrame {
    private static ServerAdminFrame instance;  // 单例实例
    private JList<String> onlineUserList;  // 在线用户列表
    private JButton kickButton;            // 踢出按钮
    private List<String> onlineUsers = new ArrayList<>();  // 在线用户的昵称列表

    // 私有构造方法，确保无法外部直接创建实例
    private ServerAdminFrame() {
        setTitle("管理员控制面板");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // 不关闭程序
        setLocationRelativeTo(null);  // 窗口居中显示

        // 创建面板
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // 在线用户列表
        onlineUserList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(onlineUserList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 踢出按钮
        kickButton = new JButton("踢出选中用户");
        panel.add(kickButton, BorderLayout.SOUTH);

        // 设置按钮点击事件
        kickButton.addActionListener(e -> {
            // 获取选中的用户
            String selectedUser = onlineUserList.getSelectedValue();
            if  (selectedUser != null) {
                // 向服务器发送踢出命令
                kickUser(selectedUser);
            }
        });

        add(panel);

        // 添加关闭窗口时的事件监听器,管理员界面无法被关闭
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 弹出一个窗口,提示请输入“确认关闭”，点击确认后检测是否为“确认关闭”，
                // 如果是，则关闭窗口，否则不关闭窗口

                String text = JOptionPane.showInputDialog(null, "请输入“确认关闭”", "提示", JOptionPane.INFORMATION_MESSAGE);
                if (text != null && text.equals("确认关闭")) {
                    JOptionPane.showMessageDialog(null, "哈哈哈，骗你的，老弟，关不了", "提示", JOptionPane.INFORMATION_MESSAGE);
                }else {
                    JOptionPane.showMessageDialog(null, "请输入正确的指令！", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    // 获取单例实例
    public static ServerAdminFrame getInstance() {
        if (instance == null) {
            instance = new ServerAdminFrame();
        }
        return instance;
    }

    // 更新在线用户列表
    public void updateUserList() {
        onlineUsers.clear();
        for (Map.Entry<Socket, String> entry : Server.onLineSockets.entrySet()) {
            onlineUsers.add(entry.getValue());
        }
        onlineUserList.setListData(onlineUsers.toArray(new String[0]));  // 更新 JList 的数据
    }

    // 踢出用户
    private void kickUser(String username) {
        for (Map.Entry<Socket, String> entry : Server.onLineSockets.entrySet()) {
            if (entry.getValue().equals(username)) {
                Socket targetSocket = entry.getKey();
                try {
                    DataOutputStream dos = new DataOutputStream(targetSocket.getOutputStream());
                    dos.writeUTF(MegType.KICK_OUT.name()); // 踢出消息类型
                    dos.writeUTF("你已被管理员踢出服务器！");
                    dos.flush();

                    targetSocket.close(); // 断开连接
                    Server.onLineSockets.remove(targetSocket);  // 从在线用户列表中移除
                    updateUserList();  // 更新在线用户列表
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static void main(String[] args) {
        new ServerAdminFrame();  // 创建管理员界面
    }
}
