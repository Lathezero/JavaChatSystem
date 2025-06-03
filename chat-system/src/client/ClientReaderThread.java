package client;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientReaderThread extends Thread {
    private Socket socket;
    private DataInputStream dis;
    private ClientChatFrame win;

    public ClientReaderThread(Socket socket, ClientChatFrame win) {
        this.win = win;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            while (!socket.isClosed()) {  // 检查连接是否关闭
                try {
                    String type = dis.readUTF();  // 读取消息类型

                    switch (type) {
                        case "LOGING":
                            // 更新在线用户列表
                            updateClientOnLineUserList();
                            break;
                        case "GROUP_MESSAGE":
                            // 群聊
                            getGroupMessage();
                            break;
                        case "PRIVATE_MESSAGE":
                            // 私聊
                            getPrivateMessage();
                            break;
                        case "KICK_OUT":
                            // 处理踢出命令
                            handleKickOut();
                            break;
                        default:
                            break;
                    }
                } catch (SocketException e) {
                    // 连接已关闭，退出线程
                    System.out.println("连接已关闭，停止接收数据");
                    break;  // 退出循环
                } catch (IOException e) {
                    e.printStackTrace();
                    break;  // 发生 I/O 异常时退出循环
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) dis.close();  // 关闭 DataInputStream
                if (socket != null && !socket.isClosed()) socket.close();  // 关闭 Socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // 处理踢出命令
    private void handleKickOut() {
        try {

            String msg = dis.readUTF();  // 获取服务端的踢出消息
            JOptionPane.showMessageDialog(null, msg);  // 弹出踢出提示框
            // 关闭连接
            socket.close();

            win.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getGroupMessage() throws IOException {
        // 接收群聊消息内容
        String message = dis.readUTF();
        // 切换到群聊界面并显示消息
        win.switchToChatPanel("群聊");
        win.appendMessageToChat("群聊", message);
    }

    private void getPrivateMessage() throws IOException {

        // 接收发送者昵称和私聊内容
        String sender = dis.readUTF();
        String message = dis.readUTF();

        // 切换到对应发送者的聊天界面
        win.switchToChatPanel(sender);
        win.appendMessageToChat(sender, "[私聊] " + sender + ": " + message);
    }

    private void updateClientOnLineUserList() throws IOException {
        // 读取在线用户数量
        int count = dis.readInt();
        // 把在线用户存储到数组中
        String[] users = new String[count];
        // 循环读取每个用户的昵称
        for (int i = 0; i < count; i++) {
            users[i] = dis.readUTF();
        }
        // 更新在线用户列表
        win.updateOnlineUsers(users);
    }
}
