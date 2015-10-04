package com.example.chat.server;

import com.example.chat.client.LoginActivity;

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
        try {
            ServerSocket serverSocket = new ServerSocket(LoginActivity.PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Login");
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                byte[] cmdByte = new byte[1024];
                in.read(cmdByte);
                String cmd = new String(cmdByte).trim();
                if (cmd.startsWith("login")) {
                    String[] s = cmd.split(",");
                    if (s.length == 3 && s[1] != null && s[2] != null) {
                        if (accountsMap.get(s[1]).equals(s[2])) {
                            out.write("ok".getBytes());
                        } else {
                            out.write("error".getBytes());
                        }
                    }
                } else {
                    out.write("error".getBytes());
                }
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
