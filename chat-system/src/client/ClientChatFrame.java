package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientChatFrame extends JFrame {
    private String nickname;  // 当前用户名
    private Socket socket;
    private JList<String> onlineUsersList;  // 在线用户列表
    private JPanel chatPanelsContainer;  // 用于存放不同聊天界面的容器
    private CardLayout cardLayout;  // 用于切换不同聊天界面

    // 存储不同的聊天面板
    private java.util.Map<String, JPanel> chatPanels = new java.util.HashMap<>();

    public ClientChatFrame(String nickname, Socket socket) {
        this.nickname = nickname;
        this.socket = socket;

        // 主窗口设置
        setTitle(nickname + " WeChat ");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.decode("#F7F7F7"));

        // 左侧：在线用户列表
        onlineUsersList = new JList<>();
        onlineUsersList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        onlineUsersList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        onlineUsersList.setBackground(Color.decode("#FFFFFF"));
        onlineUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane userListScrollPane = new JScrollPane(onlineUsersList);
        userListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        userListScrollPane.setPreferredSize(new Dimension(200, 0));

        // 右侧：聊天区域
        chatPanelsContainer = new JPanel();
        cardLayout = new CardLayout();
        chatPanelsContainer.setLayout(cardLayout);

        // 预留群聊面板
        JPanel groupChatPanel = createChatPanel("群聊");
        chatPanels.put("群聊", groupChatPanel);
        chatPanelsContainer.add(groupChatPanel, "群聊");

        // 主布局
        add(userListScrollPane, BorderLayout.WEST);
        add(chatPanelsContainer, BorderLayout.CENTER);

        setVisible(true);

        // 启动消息接收线程
        new ClientReaderThread(socket, this).start();

        // 添加在线用户选择监听
        onlineUsersList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = onlineUsersList.getSelectedValue();
                if (selectedUser != null) {
                    switchToChatPanel(selectedUser);
                }
            }
        });
    }
    // 追加在线用户到聊天面板
    public void updateOnlineUsers(String[] users) {
        String[] updatedUsers = new String[users.length + 1];
        updatedUsers[0] = "群聊"; // 默认群聊选项
        System.arraycopy(users, 0, updatedUsers, 1, users.length);
        onlineUsersList.setListData(updatedUsers);
    }
    // 追加消息到聊天面板
    private JPanel createChatPanel(String targetUser) {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(Color.WHITE);

        // 消息显示区域
        JTextArea messageDisplayArea = new JTextArea();
        messageDisplayArea.setEditable(false);
        messageDisplayArea.setLineWrap(true);
        messageDisplayArea.setWrapStyleWord(true);
        messageDisplayArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageDisplayArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageDisplayArea.setBackground(Color.decode("#F5F5F5"));
        JScrollPane messageScrollPane = new JScrollPane(messageDisplayArea);

        // 消息输入区域
        JTextArea messageInputArea = new JTextArea(4, 40);
        messageInputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageInputArea.setLineWrap(true);
        messageInputArea.setWrapStyleWord(true);
        messageInputArea.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#CCCCCC"), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
        );
        // 回车键发送消息
        messageInputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 判断是否为回车键
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // 判断是否按下Shift
                    if (e.isShiftDown()) {
                        // 按下Shift+Enter，换行
                        messageInputArea.append("\n");
                        messageDisplayArea.getCaretPosition();
                    } else {
                        // 按下Enter, 发送消息
                        sendMessage(targetUser, messageInputArea, messageDisplayArea);
                        //消费事件，避免换行
                        e.consume();
                    }
                }
            }
        });

        // 发送按钮
        JButton sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendButton.setBackground(Color.decode("#09BB07"));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sendButton.addActionListener(e -> sendMessage(targetUser, messageInputArea, messageDisplayArea));

        // 底部输入区布局
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.add(new JScrollPane(messageInputArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(messageScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        return chatPanel;
    }

    private void sendMessage(String targetUser, JTextArea messageInputArea, JTextArea messageDisplayArea) {
        String msg = messageInputArea.getText().trim();
        if (!msg.isEmpty()) {
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                if (targetUser != null && !targetUser.equals("群聊")) {
                    // 私聊
                    dos.writeUTF(MegType.PRIVATE_MESSAGE.name()); // 私聊类型
                    dos.writeUTF(targetUser); // 目标用户
                    dos.writeUTF(msg);
                    // 如果不是发给自己的消息，才显示
                    if (!targetUser.equals(nickname)) {
                        messageDisplayArea.append("[私聊] 给 " + targetUser + ": " + msg + "\n");
                    }
                } else {
                    // 群聊
                    dos.writeUTF(MegType.GROUP_MESSAGE.name()); // 群聊类型
                    dos.writeUTF(msg);
                    //messageDisplayArea.append("[群聊] " + msg + "\n");
                }
                // 刷新数据
                dos.flush();

                messageInputArea.setText(""); // 清空输入框
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ClientChatFrame("User", new Socket());
    }

    public void appendMessageToChat(String targetUser, String message) {
        if (chatPanels.containsKey(targetUser)) {
            // 获取目标用户的聊天面板
            JPanel chatPanel = chatPanels.get(targetUser);
            JScrollPane scrollPane = (JScrollPane) chatPanel.getComponent(0);
            JTextArea messageDisplayArea = (JTextArea) scrollPane.getViewport().getView();

            // 追加消息
            messageDisplayArea.append(message + "\n");

            // 自动滚动到底部
            SwingUtilities.invokeLater(() -> messageDisplayArea.setCaretPosition(messageDisplayArea.getDocument().getLength()));
        }
    }

    public void switchToChatPanel(String targetUser) {
        if (!chatPanels.containsKey(targetUser)) {
            // 如果没有该用户的聊天面板，则创建一个新的
            JPanel newChatPanel = createChatPanel(targetUser);
            chatPanels.put(targetUser, newChatPanel);
            chatPanelsContainer.add(newChatPanel, targetUser);
        }
        // 切换到目标用户的聊天面板
        cardLayout.show(chatPanelsContainer, targetUser);
    }

}
