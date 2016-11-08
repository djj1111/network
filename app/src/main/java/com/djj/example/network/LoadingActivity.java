package com.djj.example.network;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by djj on 2016/11/6.
 */

public class LoadingActivity extends Activity {
    private ProgressBar progressBar;
    private String ip,filepath;
    private int port;
    private static final int FTEXT = -11, FPHOTO = -12, FUPDATE=-13,FFINISHED = -14,
            UPDATESUCCESS=-21,UPDATEFAULT=-22,NETWORKSTART=-41;
    private  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1) {
                LoadingActivity.this.setResult(UPDATESUCCESS);
                LoadingActivity.this.finish();
            }
            if (msg.what==-1) {
                LoadingActivity.this.setResult(UPDATEFAULT);
                LoadingActivity.this.finish();
            }
        }
    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //System.out.println("按下了back键   onBackPressed()");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        //progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        Intent intent = this.getIntent();
        ip = intent.getStringExtra("ipaddress");
        port = intent.getIntExtra("port",0);
        filepath = intent.getStringExtra("filepath");
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Socket socket = new Socket(ip,port);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    final String s=in.readUTF();
                    final int t=in.readInt();
                   /* LoadingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoadingActivity.this, s,Toast.LENGTH_SHORT).show();
                        }
                    });*/
                    if (t==NETWORKSTART) {
                        out.writeInt(FTEXT);
                        out.flush();
                        out.writeUTF(filepath);
                        out.flush();
                        out.writeInt(FPHOTO);
                        File file = new File(filepath);
                        out.writeInt((int)file.length());
                        out.flush();
                        FileInputStream filein = new FileInputStream(file);
                        byte[] b = new byte[2048];
                        int length;
                        while ((length = filein.read(b, 0, b.length)) > 0) {
                            out.write(b, 0, length);
                            out.flush();// 发送给服务器
                        }
                        b = null;

                        out.writeInt(FUPDATE);
                        out.flush();
                        if (in.readInt() == UPDATESUCCESS) {
                            out.writeInt(FFINISHED);
                            out.flush();
                            mHandler.sendEmptyMessage(1);
                        } else {
                            mHandler.sendEmptyMessage(-1);//again
                        }
                        out.close();
                        in.close();
                        filein.close();
                        socket.close();

                    } else {
                        out.close();
                        in.close();
                        socket.close();
                    }

                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    LoadingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoadingActivity.this,"没有找到照片文件",Toast.LENGTH_SHORT).show();
                        }
                    });
                    mHandler.sendEmptyMessage(-1);
                }
                catch (IOException e) {
                    LoadingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoadingActivity.this,"网络中断，请重试",Toast.LENGTH_SHORT).show();
                        }
                    });
                    mHandler.sendEmptyMessage(-1);
                }


            }
        }).start();
    }


}
