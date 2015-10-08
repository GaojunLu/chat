package com.example.chat.client;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.R;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Administrator on 2015/10/4.
 */
public class ChatActivity extends Activity implements View.OnTouchListener{
    Button button_chat;
    MediaRecorder recorder = new MediaRecorder();
    String fileName, username;
    LinearLayout linearLayout_messege;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what){
                case 0:
                    LinearLayout messege = new LinearLayout(ChatActivity.this);
                    TextView textView = new TextView(ChatActivity.this);
                    Button button = new Button(ChatActivity.this);
                    textView.setText("我：");
                    button.setText("播放");
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MediaPlayer player = new MediaPlayer();
                            if(player.isPlaying()){
                                player.reset();
                            }
                            try {
                                player.setDataSource("/sdcard/" + fileName);
                                player.prepare();
                                player.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    messege.addView(textView);
                    messege.addView(button);
                    linearLayout_messege.addView(messege);
                    break;
                case 1:
                    final String fileName2 = ((String[]) msg.obj)[1];
                    LinearLayout messege1 = new LinearLayout(ChatActivity.this);
                    TextView textView1 = new TextView(ChatActivity.this);
                    Button button1 = new Button(ChatActivity.this);
                    messege1.setGravity(Gravity.RIGHT);
                    textView1.setText("：" + ((String[]) msg.obj)[0]);
                    button1.setText("播放");
                    button1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MediaPlayer player = new MediaPlayer();
                            if (player.isPlaying()) {
                                player.reset();
                            }
                            try {
                                player.setDataSource("/sdcard/" + fileName2);
                                player.prepare();
                                player.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    messege1.addView(button1);
                    messege1.addView(textView1);
                    linearLayout_messege.addView(messege1);
                    break;
                default:
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chat);
        button_chat = (Button)findViewById(R.id.button_chat);
        button_chat.setOnTouchListener(this);
        linearLayout_messege = (LinearLayout)findViewById(R.id.linearlayout_messege);
        username = getIntent().getStringExtra("username");
        new Thread(new Runnable() {
            @Override
            public void run() {
                UDPServer.startServser(ChatActivity.this, username);
            }
        }).start();
    }


    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){                                             //按下录音
            fileName = new Date().getTime()+".amr";
            button_chat.setText("松 开 完 成");
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile("/sdcard/" + fileName);
            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(ChatActivity.this, "录音出错", Toast.LENGTH_SHORT).show();
                return true;
            }
            recorder.start();
        }else if(event.getAction()==MotionEvent.ACTION_UP){                                         //松开发送
            button_chat.setText("按 下 说 话");
            recorder.stop();
            recorder.reset();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket(LoginActivity.IP_ADRESS, LoginActivity.PORT);
                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();
                        byte[] respondbyte = new byte[1024];
                        out.write(("upload" + "," + fileName + "," + new File("/sdcard/" + fileName).length() + "," + username).getBytes());
                        in.read(respondbyte);
                        if((new String(respondbyte)).trim().equalsIgnoreCase("ok")){
                            FileInputStream fis = new FileInputStream("/sdcard/" + fileName);
                            byte[] fileByte = new byte[1024];
                            while(fis.read(fileByte)!=-1){
                                out.write(fileByte);
                                out.flush();
                            }
                            fis.close();
                            out.close();
                            in.close();
                            socket.close();
                            Message message = new Message();
                            message.what = 0;
                            handler.sendMessage(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
        return false;
    }
}
