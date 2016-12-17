package com.djj.example.network;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by djj on 2016/11/6.
 */

public class LoadingActivity extends Activity {
    private ProgressBar progressBar;
    private String ip;
    private ArrayList<String> filepath;
    private int port;
    private TextView textView;
    private static final int FTEXT = -11, FPHOTO = -12, FUPDATE=-13,FFINISHED = -14,
            UPDATESUCCESS = -21, UPDATEFAULT = -22, DATEBASEERROR = -23, NETWORKSTART = -41;
    private  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1) {
                LoadingActivity.this.setResult(UPDATESUCCESS);
                LoadingActivity.this.finish();
            }
            if (msg.what==-1) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("filepath", filepath);
                intent.putExtra("bundle", bundle);
                LoadingActivity.this.setResult(UPDATEFAULT, intent);
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
        Bundle bundle = intent.getBundleExtra("bundle");
        ip = bundle.getString("ipaddress", "127.0.0.1");
        port = bundle.getInt("port", 0);
        filepath = bundle.getStringArrayList("filepath");
        textView = (TextView) findViewById(R.id.textload);
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Socket socket = new Socket();
                    socket.connect((new InetSocketAddress(ip, port)), 3000);
                    socket.setSoTimeout(10000);
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
                        Iterator iter = filepath.iterator();
                        while (iter.hasNext()) {
                            out.writeInt(FTEXT);
                            out.flush();
                            final String filepathone = (String) iter.next();
                            out.writeUTF(filepathone);
                            out.flush();
                            out.writeInt(FPHOTO);
                            LoadingActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String[] s = filepathone.split("/");
                                    textView.setText(s[s.length - 1]);
                                }
                            });
                            File file = new File(filepathone);
                            out.writeInt((int) file.length());
                            out.flush();
                            FileInputStream filein = new FileInputStream(file);
                            byte[] b = new byte[2048];
                            int length;
                            while ((length = filein.read(b, 0, b.length)) > 0) {
                                out.write(b, 0, length);
                                out.flush();// 发送给服务器
                            }
                            b = null;
                            filein.close();
                            out.writeInt(FUPDATE);
                            out.flush();
                            int update_result = in.readInt();
                            if (update_result == UPDATESUCCESS) {
                                iter.remove();
                            } else if (update_result == DATEBASEERROR) {
                                LoadingActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoadingActivity.this, "数据库连接错误", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            } else if (update_result == UPDATEFAULT) {
                                LoadingActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoadingActivity.this, "数据库更新错误", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            } else {
                                LoadingActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoadingActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            }

                        }
                        out.writeInt(FFINISHED);
                        out.flush();
                        if (filepath.size() == 0) {
                            //if (in.readInt() == UPDATESUCCESS) {

                            mHandler.sendEmptyMessage(1);
                        } else {
                            mHandler.sendEmptyMessage(-1);//again
                        }
                        out.close();
                        in.close();

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
                            Toast.makeText(LoadingActivity.this, "没有找到文件", Toast.LENGTH_SHORT).show();
                        }
                    });
                    mHandler.sendEmptyMessage(-1);
                } catch (SocketException e) {
                    e.printStackTrace();
                    LoadingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoadingActivity.this, "网络超时，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                    mHandler.sendEmptyMessage(-1);
                } catch (IOException e) {
                    e.printStackTrace();
                    LoadingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoadingActivity.this, "未连接到服务器，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        }).start();
    }


}
