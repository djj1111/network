package com.djj.example.network;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private static final int TYPE_FILE_IMAGE=-11,TYPE_FILE_VEDIO=-12,
            SYSTEM_CAMERA_REQUESTCODE=16,NETWORK_REQUESTCODE=17,
            UPDATESUCCESS=-21,UPDATEFAULT=-22;
    private TextView textView;
    private EditText editText;
    private EditText multiEditText;
    private Button button,button1;
    private DataInputStream in;
    private String inputdate;
    private String ip;
    private DataOutputStream out;
    private int port=12797;
    private Uri imageFileUri;
    private File file;
    private boolean hasphoto=false;



    /*class uploadThread extends Thread {
        private static final int HOST_PORT = 5432;
        DataInputStream inputStream;
        boolean flag = false;
        Handler phandler;
        Socket skt = null;
        String trueName;
        public void setHandler(Handler handler){
            phandler=handler;
        }
        uploadThread(){
            super();
        }

        @Override
        public void run() {
            try {
                ServerSocket server = new ServerSocket(HOST_PORT);
                while (true) {
                    skt = server.accept();
                    //System.out.println("接收到Socket请求");
                    Log.d("sdf","fuck");
                    //接收客户端文件
                    inputStream = new DataInputStream(skt.getInputStream());
                     trueName= inputStream.readUTF();
                    phandler.sendEmptyMessage(1);
                    inputStream.close();
                    // 服务器发送消息
                    skt.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    uploadThread serverThread;*/

    /*private  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //if (msg.what==1)
                textView.setText("have");
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=(TextView)findViewById(R.id.textView);
        editText=(EditText)findViewById(R.id.editTextip);
        multiEditText=(EditText)findViewById(R.id.editText);
        button=(Button)findViewById(R.id.button);
        button1=(Button)findViewById(R.id.button1);
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(port);
                    socketser = server.accept();
                    //System.out.println("接收到Socket请求");
                    //接收客户端文件
                    inputStream = new DataInputStream(socketser.getInputStream());
                    while (true){
                    inputdate = inputStream.readUTF();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.textView.setText(textView.getText().toString() + "\n" + socketser.getInetAddress().toString().substring(1) + ":" + inputdate);
                        }
                    });
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } ).start();*/




        /*try {
            ServerSocket server = new ServerSocket(5432);
            while (true) {
                socket = server.accept();
                System.out.println("接收到Socket请求");
            }
        }
        catch (IOException e) {
                e.printStackTrace();
        }*/
        //serverThread=new uploadThread(this);
        //serverThread.setHandler(mHandler);
        //serverThread.start();
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (v.getId()==R.id.button) senttext();
                //senttext();
                dotest();
                //network();
            }
        });
        button1.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                //dotest();
                network();
            }
        });
    }

    /*public static boolean isNumeric(String str){
        for(int i=str.length();--i>=0;){
            int chr=str.charAt(i);
            if(chr<48 || chr>57)
                return false;
        }
        return true;
    }*/

    private Uri getOutFileUri(int fileType) {
        return Uri.fromFile(getOutFile(fileType));
    }

    //生成输出文件
    private File getOutFile(int fileType) {

        String storageState = Environment.getExternalStorageState();
        //Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);
        if (Environment.MEDIA_REMOVED.equals(storageState)){
            Toast.makeText(getApplicationContext(), "无内存卡", Toast.LENGTH_SHORT).show();
            return null;
        }

        /*File mediaStorageDir = new File (Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                ,"MyPictures");*/
        File mediaStorageDir = new File (Environment.getExternalStorageDirectory(),"waterwork");
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Toast.makeText(getApplicationContext(), "创建图片存储路径目录失败", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        file = new File(getFilePath(mediaStorageDir,fileType));

        return file;
    }
    //生成输出文件路径
    @Nullable
    private String getFilePath(File mediaStorageDir, int fileType){
        String timeStamp =new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String filePath = mediaStorageDir.getPath() + File.separator;
        if (fileType == TYPE_FILE_IMAGE){
            filePath += ("IMG_" + timeStamp + ".jpg");
        }else if (fileType == TYPE_FILE_VEDIO){
            filePath += ("VIDEO_" + timeStamp + ".mp4");
        }else{
            return null;
        }
        return filePath;
    }

    private void dotest(){
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFileUri = getOutFileUri(TYPE_FILE_IMAGE);//得到一个File Uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        startActivityForResult(intent, SYSTEM_CAMERA_REQUESTCODE);
    }

    private void network(){
        String ip=editText.getText().toString();
        if (!hasphoto) {
            Toast.makeText(MainActivity.this,"没有照片，请先拍照！",Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent=new Intent();
        intent.putExtra("ipaddress",ip);
        intent.putExtra("port",port);
        intent.putExtra("filepath",file.getPath());
        intent.setClass(MainActivity.this,LoadingActivity.class);//跳转到加载界面
        startActivityForResult(intent,NETWORK_REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==SYSTEM_CAMERA_REQUESTCODE)
            if (resultCode==RESULT_OK){
                Toast.makeText(MainActivity.this,"拍照成功",Toast.LENGTH_SHORT).show();
                hasphoto=true;
            }else {
                Toast.makeText(MainActivity.this,"拍照不成功",Toast.LENGTH_SHORT).show();
                hasphoto=false;
            }
        if (requestCode==NETWORK_REQUESTCODE)
            if (resultCode==UPDATESUCCESS){
                if (file.delete()){
                    Toast.makeText(MainActivity.this,"照片临时文件成功删除",Toast.LENGTH_SHORT).show();
                    hasphoto=false;
                } else {
                    Toast.makeText(MainActivity.this,"照片临时文件未能删除，照片路径："+file.getPath(),Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(MainActivity.this,"上传不成功",Toast.LENGTH_SHORT).show();
            }




    }


   /* @Override
    public void finish() {

        try{*//*Socket sockettmp=null;
            if (socketser==null) sockettmp=new Socket("127.0.0.1",port);
            sockettmp.close();
            dos.close();*//*//在发送消息完之后一定关闭，否则服务端无法继续接收信息后处理，手机卡机
                *//*reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                result = Boolean.parseBoolean(reader.readLine().toString());
                System.out.println("上传结果" + result);//运行时总是提示socket关闭，不能接收服务端返回的消息
                reader.close();*//*
            //socket.close();
            *//*inputStream.close();
            // 服务器发送消息
            socketser.close();
            server.close();*//*
        }catch (SocketTimeoutException e) {
            e.printStackTrace();
            //Toast.makeText(MainActivity.this, "超时，上传失败",Toast.LENGTH_LONG).show();
        }catch (IOException e) {
            e.printStackTrace();
        }
        super.finish();

    }*/
}

   /* private void senttext(){

        // String iptemp[]=new String[]{};
        *//*if (ip.contains(".")) iptemp=ip.split(".");
        Toast.makeText(MainActivity.this,ip,Toast.LENGTH_LONG).show();
        if (iptemp.length!=4) {Toast.makeText(MainActivity.this,"error1",Toast.LENGTH_LONG).show();return;}
        for (int i=0;i<4;i++)
            if (isNumeric(iptemp[i])) {Toast.makeText(MainActivity.this,"error2",Toast.LENGTH_LONG).show();return;}*//*

        new Thread(new Runnable() {
            @Override
            public void run() {
                *//*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,ip,Toast.LENGTH_LONG).show();
                    }
                });*//*
                ip=editText.getText().toString();
                try {
                    if (socket==null) {socket = new Socket(ip, port);
                        //Log.i("测试","connect");
                        dos = new DataOutputStream(socket.getOutputStream());}
                    if (!socket.getInetAddress().toString().substring(1).equals(ip)){
                        dos.close();
                        socket.close();
                        socket = new Socket(ip, port);
                        //Log.i("测试","connect");
                        dos = new DataOutputStream(socket.getOutputStream());
                    }

                    dos.writeUTF(multiEditText.getText().toString());

                    dos.flush();


                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    //Toast.makeText(MainActivity.this, "超时，上传失败",Toast.LENGTH_LONG).show();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

*/