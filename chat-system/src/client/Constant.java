package client;

public class Constant {
    // 创建服务器地址和端口属性
    private static String SERVER_IP;
    private static String SERVER_PORT;

    // 构造器
    public Constant(String serverIP, String serverPort) {
        SERVER_IP = serverIP;
        SERVER_PORT = serverPort;
    }
    public static String getServerIP() {
        return SERVER_IP;
    }
    public static String getServerPort() {
        return SERVER_PORT;
    }
}
