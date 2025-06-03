package server;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class ServerReaderThread extends Thread {
    private Socket socket;
    private String nickname;

    public ServerReaderThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 接收的消息可能有很多种类型：1、登录消息（包含昵称） 2、群聊消息 3、私聊消息 4、管理员踢出命令
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            while (true) {
                String type = dis.readUTF();
                switch (type) {
                    case "LOGING":
                        // 处理登录请求
                        handleLogin(dis);
                        // 更新管理员界面上的在线用户列表
                        ServerAdminFrame.getInstance().updateUserList();
                        break;
                    case "GROUP_MESSAGE":
                        // 处理群聊消息
                        handleGroupMessage(dis);
                        break;
                    case "PRIVATE_MESSAGE":
                        // 处理私聊消息
                        handlePrivateMessage(dis);
                        break;
                    case "KICK_OUT":
                        // 处理管理员踢出命令
                        handleKickUserCommand(dis);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("客户端下线了：" + socket.getInetAddress().getHostAddress());
            Server.onLineSockets.remove(socket); // 把下线的客户端socket从在线集合中移除
            ServerAdminFrame.getInstance().updateUserList();
            updateClientOnLineUserList(); // 下线了用户也需要更新全部客户端的在线人数列表。
        }
    }

    // 处理用户登录
    private void handleLogin(DataInputStream dis) throws IOException {
        nickname = dis.readUTF();

        // 将用户添加到在线集合中
        Server.onLineSockets.put(socket, nickname);

        // 更新在线用户列表
        updateClientOnLineUserList();
    }

    // 处理群聊消息
    private void handleGroupMessage(DataInputStream dis) throws IOException {
        String msg = dis.readUTF();
        // 过滤不良词汇
        String filteredMessage = Server.filterBadWords(msg);

        sendMsgToAll(filteredMessage);
    }

    // 处理私聊消息
    private void handlePrivateMessage(DataInputStream dis) throws IOException {
        String targetUser = dis.readUTF();  // 接收目标用户昵称
        String privateMsg = dis.readUTF(); // 接收私聊内容
        // 过滤不良词汇
        String filteredMessage = Server.filterBadWords(privateMsg);
        sendPrivateMsg(targetUser, filteredMessage);
    }

    // 处理管理员踢出命令
    private void handleKickUserCommand(DataInputStream dis) throws IOException {
            String targetUser = dis.readUTF(); // 接收要踢出的用户昵称
            kickUser(targetUser);
    }

    // 踢出指定用户
    private void kickUser(String targetUser) {
        Socket targetSocket = null;
        for (Map.Entry<Socket, String> entry : Server.onLineSockets.entrySet()) {
            if (entry.getValue().equals(targetUser)) {
                targetSocket = entry.getKey();
                break;
            }
        }

        if (targetSocket != null) {
            try {
                // 向客户端发送踢出消息
                DataOutputStream dos = new DataOutputStream(targetSocket.getOutputStream());
                dos.writeUTF(MegType.KICK_OUT.name()); // 踢出消息类型
                dos.writeUTF("你已被管理员踢出服务器！"); // 踢出理由
                dos.flush();

                // 断开连接
                targetSocket.close();

                // 从在线列表中移除
                Server.onLineSockets.remove(targetSocket);
                updateClientOnLineUserList();  // 更新在线用户列表
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 给全部在线socket推送当前客户端发来的消息
    private void sendMsgToAll(String msg) {
        // 获取当前发送消息的用户昵称
        String name = Server.onLineSockets.get(socket);

        // 拼接消息内容
        String msgResult = name + " 说: " + msg + "\r\n";  // 群聊消息格式

        // 推送给全部客户端socket
        for (Socket socket : Server.onLineSockets.keySet()) {
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(MegType.GROUP_MESSAGE.name()); // 消息类型：群聊
                dos.writeUTF(msgResult); // 发送群聊消息内容
                dos.flush(); // 刷新数据！
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 私聊消息发送逻辑
    private void sendPrivateMsg(String targetUser, String privateMsg) {
        for (Map.Entry<Socket, String> entry : Server.onLineSockets.entrySet()) {
            if (entry.getValue().equals(targetUser)) {
                try {
                    DataOutputStream dos = new DataOutputStream(entry.getKey().getOutputStream());
                    dos.writeUTF(MegType.PRIVATE_MESSAGE.name()); // 消息类型：私聊
                    dos.writeUTF(Server.onLineSockets.get(socket)); // 发送者昵称
                    dos.writeUTF(privateMsg); // 私聊内容
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // 更新客户端的在线人数列表
    private void updateClientOnLineUserList() {
        for (Socket socket : Server.onLineSockets.keySet()) {
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(MegType.LOGING.name());  // 消息类型：在线用户列表更新
                dos.writeInt(Server.onLineSockets.size());  // 在线用户数量
                for (String nickname : Server.onLineSockets.values()) {
                    dos.writeUTF(nickname);  // 发送每个在线用户的昵称
                }
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
