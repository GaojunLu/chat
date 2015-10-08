package com.example.chat.client;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Administrator on 2015/10/6.
 * 负责接收服务器发来的UDP包，判断后处理
 */
public class UDPServer implements Runnable {
    public static int PORT = 7000;
    byte[] data;
    static ChatActivity context;
    static String USERNAME;

    public UDPServer(byte[] data) {
        this.data = data;
    }

    public static void startServser(ChatActivity context, String username) {
        UDPServer.context = context;
        UDPServer.USERNAME = username;
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            while (true) {
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                new UDPServer(data).run();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String[] messege = (new String(data)).trim().split(",");//0用户名、1文件名、2IP
        Log.e("asdf", messege[0]+","+messege[1]+","+messege[2]);
        if(!messege[0].equalsIgnoreCase(USERNAME)){                                                 //接收不是自己的消息
            try {
                Socket socket = new Socket(LoginActivity.IP_ADRESS, LoginActivity.PORT);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                out.write(("download," + messege[1]).getBytes());
                out.flush();
                byte[] respon = new byte[1024];
                in.read(respon);                                                                    //读文件长度
                String[] s = (new String(respon)).trim().split(",");
                if(s[0].equalsIgnoreCase("length")){
                    FileOutputStream fos = new FileOutputStream("/sdcard/" +messege[1]);
                    byte[] temp = new byte[1024];
                    long fileLength = Long.parseLong(s[1]);
                    long len = 0;
                    int oneRead;
                    while(true){                                                                        //开始读
                        oneRead = in.read(temp);
                        fos.write(temp, 0, oneRead);
                        fos.flush();
                        len+=oneRead;
                        if(len>=fileLength){
                            break;
                        }
                    }
                    fos.close();
                }
                Message msg = new Message();
                msg.what = 1;
                msg.obj = new String[]{messege[0], messege[1]};
                context.handler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
