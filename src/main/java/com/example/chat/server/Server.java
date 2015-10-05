package com.example.chat.server;

import com.example.chat.client.LoginActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/10/4.
 */
public class Server {
    static HashMap accountsMap = new HashMap();

    static {
        accountsMap.put("qq", "1111");
        accountsMap.put("ww", "1111");
    }

    public static void main(String args[]) {
        try {                                                                                       //建服务器接收处理请求
            ServerSocket serverSocket = new ServerSocket(LoginActivity.PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Login");
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                byte[] cmdByte = new byte[1024];
                in.read(cmdByte);
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
                    out.write("ok".getBytes());
                    FileOutputStream fos = new FileOutputStream("D:/amr/" + cmd.split(",")[1]);       //写音频文件
                    byte[] fileByte = new byte[1024];
                    int readBytes = 0, len = 0;
                    long fileLenght = Long.parseLong(cmd.split(",")[2]);
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
                } else if(cmd.startsWith("dowmload")){                                              //下载语音文件
                    out.write("ok".getBytes());
                }else {                                                                             //其它
                    out.write("error".getBytes());
                }
                out.flush();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
