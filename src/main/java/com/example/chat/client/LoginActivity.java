package com.example.chat.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chat.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 2015/10/4.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    EditText editText_account, editText_password;
    Button button_login, button_exit;
    public static final String IP_ADRESS = "192.168.1.4";                                              //主机ip
    public static final int PORT = 6000;                                                            //TCP端口
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(LoginActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
            if (((String)msg.obj).equals("登录成功")) {
                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                intent.putExtra("username", editText_account.getText().toString());
                startActivity(intent);
                finish();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        editText_account = (EditText) findViewById(R.id.edittext_account);
        editText_password = (EditText) findViewById(R.id.edittext_password);
        button_login = (Button) findViewById(R.id.button_login);
        button_exit = (Button) findViewById(R.id.button_exit);
        button_login.setOnClickListener(this);
        button_exit.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                sendToServer("login," + editText_account.getText().toString() + "," + editText_password.getText().toString());
                break;
            case R.id.button_exit:
                finish();
                break;
            default:
                break;
        }
    }

    public void sendToServer(final String cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(IP_ADRESS, PORT);
                    InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream();
                    byte[] cmdByte = cmd.getBytes();
                    out.write(cmdByte);
                    out.flush();
                    byte[] respondByte = new byte[1024];
                    in.read(respondByte);
                    if (new String(respondByte).trim().equalsIgnoreCase("OK")) {
                        Message message = new Message();
                        message.obj = "登录成功";
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.obj = "用户或密码错误";
                        handler.sendMessage(message);
//                        Log.d("asdf", "用户或密码错误");
                    }
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.obj = "网络错误";
                    handler.sendMessage(message);
//                    Log.d("asdf", "网络错误");
                }
            }
        }).start();
    }


}
