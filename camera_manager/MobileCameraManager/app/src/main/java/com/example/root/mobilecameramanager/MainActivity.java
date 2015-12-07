package com.example.root.mobilecameramanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new Drawing2(this));
    }

    @Override
    protected void onPause(){
        super.onPause();
    }


    class Drawing2 extends View implements Runnable {
        Bitmap bitmap;
        Paint paint=new Paint();
        Thread t=new Thread(this);

        private String ipname = "10.236.14.11";
        private  int port = 8887;
        private byte inbuf[] = new byte[1024*1024];

        private byte[] ReadyCMD = {'M', 'R', 'e', 'a', 'd', 'y'};
        private byte[] NextCMD = {'M', 'N', 'e', 'x', 't'};

        public Drawing2(Context context) {
            super(context);
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.r23);

            t.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            System.gc();
            super.onDraw(canvas);
            //设置画布颜色
            canvas.drawColor(Color.GRAY);
            //绘制图片
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }

        public void run() {
            try {
                int i=0;
                while (true) {
                    i++;
                    System.gc();
                    byte[] jpeg_data = new byte[1*1024*1024];
                    int jpeg_data_len = 0;
                    Log.d("debug", "draw num: "+i);

                    Log.d("debug", "will connect to server");
                    // socket get jpeg data

                    Socket sock = new Socket(ipname, port);

                    OutputStream outsocket = sock.getOutputStream();
                    InputStream insocket = sock.getInputStream();

                    outsocket.write(ReadyCMD);

                    int num = 0;
                    while((num = insocket.read(inbuf)) != -1){
                        System.arraycopy(inbuf, 0, jpeg_data, jpeg_data_len, num);
                        jpeg_data_len += num;

                    }


                    String SCMD = new String(jpeg_data, "utf-8");
                    SCMD = SCMD.substring(0, jpeg_data_len);


                    if (SCMD == null || SCMD.length() <= 0){
                        Log.d("debug", "SCMD is empty ");
                    }
                    else if (SCMD.compareTo("GotM")==0){
                        //Thread.sleep(25);
                        Log.d("debug", "not draw...");
                    }
                    else{
                        //
                        //bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.r28);
                        //decodeByteArray ;
                        Bitmap srcBitmap = BitmapFactory.decodeByteArray(jpeg_data, 0, jpeg_data_len);

                        //rotate
                        Matrix matrix = new Matrix();
                        matrix.postScale(1f, 1f);
                        matrix.postRotate(90);
                        bitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(),
                                matrix, true);

                        //refresh
                        this.postInvalidate();

                        Log.d("debug", "draw...");
                    }

                    sock.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
