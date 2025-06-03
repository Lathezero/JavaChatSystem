package client;

import java.io.*;

public class AccountManager {
    private static final String FILE_PATH = "accounts.txt"; // 存储账号信息的文件路径

    // 注册新账号
    public static boolean register(String username, String password) {
        // 检查用户名是否已存在
        if (isUsernameExists(username)) {
            return false; // 用户名已存在
        }

        // 将新用户信息写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(username + ":" + password);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 登录验证
    public static boolean login(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true; // 找到匹配的用户名和密码
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // 没有匹配的用户名或密码
    }

    // 检查用户名是否已存在
    private static boolean isUsernameExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username)) {
                    return true; // 用户名已存在
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
