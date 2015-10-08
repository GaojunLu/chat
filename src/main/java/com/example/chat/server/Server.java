package com.example.chat.server;

import android.util.Log;

import com.example.chat.client.LoginActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Administrator on 2015/10/4.
 */
public class Server implements Runnable {
    static ServerSocket serverSocket;
    Socket socket;
    static HashSet<String> IPs = new HashSet<String>();
    static HashMap accountsMap = new HashMap();

    static {
        accountsMap.put("qq", "1111");
        accountsMap.put("ww", "1111");
    }

    public static void main(String args[]) {
        try {                                                                                       //建服务器接收处理请求
            serverSocket = new ServerSocket(LoginActivity.PORT);
            while (true) {
                new Server(serverSocket.accept()).run();
                System.out.println("Login");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Server(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            byte[] cmdByte = new byte[1024];
            in.read(cmdByte, 0, cmdByte.length);
            String cmd = new String(cmdByte).trim();
            if (cmd.startsWith("login")) {                                                      //登录
                String[] s = cmd.split(",");
                if (s.length == 3 && s[1] != null && s[2] != null) {
                    if (accountsMap.get(s[1]) != null && accountsMap.get(s[1]).equals(s[2])) {
                        out.write("ok".getBytes());
                    } else {
                        out.write("error".getBytes());
                    }
                }
            } else if (cmd.startsWith("upload")) {                                              //上传语音文件
                String[] s = cmd.split(",");
                String username = s[3];
                String filename = s[1];
                long fileLenght = Long.parseLong(cmd.split(",")[2]);
                out.write("ok".getBytes());
                FileOutputStream fos = new FileOutputStream("D:/amr/" + filename);       //写音频文件
                byte[] fileByte = new byte[1024];
                int readBytes = 0, len = 0;
                while (true) {
                    len = in.read(fileByte);
                    readBytes += len;
                    fos.write(fileByte, 0, len);
                    fos.flush();
                    if (readBytes >= fileLenght) {
                        break;
                    }

                }
                fos.close();
                String ip = socket.getInetAddress().getHostAddress();
                IPs.add(ip);
                byte[] data = (username + "," + filename + "," + ip).getBytes();
                Pool pool = new Pool(data);                                                     //给消息队列加消息，包括用户名、文件名、ip
                pool.run();
            } else if (cmd.startsWith("download")) {                                              //下载语音文件
                FileInputStream fis = new FileInputStream("D:/amr/" + cmd.split(",")[1]);
                out.write(("length," + new File("D:/amr/" + cmd.split(",")[1]).length()).getBytes());//发文件长度
                byte[] temp = new byte[1024];
                while (fis.read(temp) != -1) {
                    out.write(temp);                                                            //发文件
                    out.flush();
                }
                fis.close();
            } else {                                                                             //其它
                out.write("error".getBytes());
            }
            out.flush();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
