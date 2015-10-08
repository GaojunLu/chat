package com.example.chat.server;

import android.util.Log;

import com.example.chat.client.*;
import com.example.chat.client.UDPServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;

/**
 * Created by Administrator on 2015/10/6.
 * 收到的信消息由此类处理：
 * 1、加入集合
 * 2、每隔n秒全部发送一次
 * 1）建立登录用户信息，包括用户名和ip
 * 2）把消息发给其它用户
 * 3）清空消息
 */
public class Pool implements Runnable {
    //    String[] data;  //0用户名、1文件名、2IP
    byte[] data;

    public Pool(byte[] data) {
//        this.data = new String(data).split(",");
        this.data = data;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            Iterator<String> it = Server.IPs.iterator();
            while (it.hasNext()) {
                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(it.next()), UDPServer.PORT);
                System.out.println(Server.IPs.size());
                socket.send(packet);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
