package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {
    // 初始化在线的客户端和昵称的映射关系集合
    public static final Map<Socket, String> onLineSockets = new HashMap<>();

    // 初始化不良词汇的集合
    private static Set<String> badWords = new HashSet<>();

    public static void main(String[] args) {
        Thread thred = new Thread();
        System.out.println("启动服务端系统.....");

        // 读取 bad_message.txt 文件，加载不良词汇
        loadBadWords("chat-server/src/server/bad_message.txt");

        // 启动管理员界面
        ServerAdminFrame.getInstance();

        System.out.println(MegType.KICK_OUT);
        try {
            // 注册端口。
            ServerSocket serverSocket = new ServerSocket(Constant.PORT);
            // 主线程负责接受客户端的连接请求
            while (true) {
                // 调用accept方法，获取到客户端的Socket对象
                System.out.println("等待客户端的连接.....");

                // 阻塞监听，等待客户端的连接，然后返回一个Socket对象
                Socket socket = serverSocket.accept();

                // 把这个管道交给一个独立的线程来处理：以便支持很多客户端可以同时进来通信。
                new ServerReaderThread(socket).start();

                System.out.println("一个客户端连接成功.....");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 加载不良词汇
    private static void loadBadWords(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 将每行的词添加到集合中，忽略大小写
                badWords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 检查消息并替换不良词汇
    public static String filterBadWords(String message) {
        for (String badWord : badWords) {
            // 使用正则表达式替换不良词汇
            message = message.replaceAll("(?i)" + badWord, "**");
        }
        return message;
    }
}
