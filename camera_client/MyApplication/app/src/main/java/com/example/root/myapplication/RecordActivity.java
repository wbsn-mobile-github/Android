package com.example.root.myapplication;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * Created by root on 8/10/15.
 */
public class RecordActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CAMERA";
    private int btn_time_delay = 1000;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean previewRunning = false;
    private boolean recordRunning = false;

    private MediaRecorder mediaRecorder;
    private final int maxDurationInMs = 1000*60*60*24;
    private final long maxFileSizeInBytes = 500000;
    private final int videoFramesPerSecond = 20;
    private boolean mutex = false;
    private Chronometer timer;

    private String cameraServerIP = "10.236.14.11";
    private int cameraServerPort = 8887;
    private int frame_num = 0;

    private int VideoFormatIndex;
    private int VideoWidth;
    private int VideoHeight;
    private double VideoWidthRatio=1;
    private double VideoHeightRatio=1;
    private int VideoQuality=15;

    private boolean readyToSendPic = false;

    private Socket dataSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        timer = (Chronometer)findViewById(R.id.chronometer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        frame_num = 0;

    }

    //start recording
    public void Btn5OnClick(View view) throws InterruptedException {
        if (recordRunning == true) {
            return;
        }
        while(mutex){
            Log.d("debug", "mutex is true, waiting...");
            Thread.sleep(btn_time_delay);
        }
        mutex = true;
        startRecording();

        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        Button btn_stop = (Button)findViewById(R.id.button6);
        btn_stop.setClickable(false);
        Thread.sleep(btn_time_delay);
        btn_stop.setClickable(true);
        mutex = false;

    }

    //stop recording
    public void Btn6OnClick(View view) throws InterruptedException {
        while(mutex){
            Log.d("debug","mutex is true, waiting...");
            Thread.sleep(btn_time_delay);
        }
        mutex = true;
        stopRecording();

        timer.stop();

        Button btn_start = (Button)findViewById(R.id.button5);
        btn_start.setClickable(false);
        Thread.sleep(btn_time_delay);
        btn_start.setClickable(true);
        mutex = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("verbose", "==================surfaceCreated");
        camera = Camera.open();

        camera.setPreviewCallback(RecordActivity.this);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v("verbose", "==================surfaceChanged");
        if (previewRunning){
            camera.stopPreview();
        }
        Camera.Parameters p = camera.getParameters();
        Size size = p.getPreviewSize();
        VideoWidth=size.width;
        VideoHeight=size.height;
        VideoFormatIndex=p.getPreviewFormat();
        p.set("orientation", "portrait");
        camera.setDisplayOrientation(90);
        camera.setParameters(p);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            previewRunning = true;
        }
        catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("verbose", "==================surfaceDestroyed");
        camera.stopPreview();

        camera.setPreviewCallback(null);

        camera.release();

        previewRunning = false;

    }

    public boolean startRecording(){
        try {
            camera.unlock();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mediaRecorder.setMaxDuration(maxDurationInMs);

            long ms = new Date().getTime();
            String recording_file_name = "mobile_camera_"+ms+".dat";
            Log.d("debug", "recording_file_name: " + recording_file_name);

            String recording_file_path = "/sdcard/mobile_camera/";
            File f_dir = new File(recording_file_path);
            if(!f_dir.exists()){
                f_dir.mkdir();
            }

            File f_recording = new File(recording_file_path,recording_file_name);
            mediaRecorder.setOutputFile(f_recording.getPath());

            //mediaRecorder.setVideoFrameRate(videoFramesPerSecond);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            mediaRecorder.setMaxFileSize(maxFileSizeInBytes);

            mediaRecorder.prepare();
            mediaRecorder.start();
            recordRunning = true;
            return true;
        } catch (IllegalStateException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
            recordRunning = false;
            return false;
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
            recordRunning = false;
            return false;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.v("verbose", "===preview..................");
        frame_num++;
        Log.d("debug", "===frame number: " + frame_num);
        if (frame_num %2 == 0){
            return;
        }
        try {
            if (true){
                Log.d("debug", "will send cmd to server");
                ByteArrayOutputStream outstreamc = new ByteArrayOutputStream();
                byte[] toSendCMD = {'C', 'R', 'e', 'a', 'd', 'y'};

                try{
                    outstreamc.write(toSendCMD);
                }catch (Exception e) {
                    Log.e("error", "exception: "+e);
                }

                Thread thc = new SendCMDThread(outstreamc);
                thc.start();
                thc.join();
            }

            if (readyToSendPic) {
                YuvImage image = new YuvImage(data, VideoFormatIndex, VideoWidth, VideoHeight, null);
                if (image != null) {
                    ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, (int) (VideoWidthRatio * VideoWidth),
                            (int) (VideoHeightRatio * VideoHeight)), VideoQuality, outstream);
                    outstream.flush();

                    Log.d("debug", "will send pic to server");
                    Thread thf = new SendFileThread(outstream, frame_num);
                    thf.start();
                    thf.join();
                }
                readyToSendPic = false;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SendFileThread extends Thread{
        private int fnum;
        private byte byteBuffer[] = new byte[10*1024];
        private OutputStream outsocket;
        private InputStream insocket;
        private ByteArrayOutputStream myoutputstream;

        public SendFileThread(ByteArrayOutputStream myoutputstream,int frame_num){
            this.myoutputstream = myoutputstream;
            this.fnum=frame_num;
            try {
                myoutputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try{
                if (dataSocket == null){
                    dataSocket = new Socket(cameraServerIP,cameraServerPort);
                    Log.d("debug", "socket created");
                }
                else {
                    Log.d("debug", "socket: "+dataSocket);
                }
                outsocket = dataSocket.getOutputStream();
                insocket = dataSocket.getInputStream();

                Log.d("debug", "***totoal size: "+ myoutputstream.size());
                ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
                int bnum = inputstream.available();
                Log.d("debug", "***total num: "+bnum);
                int amount;
                while ((amount = inputstream.read(byteBuffer)) != -1) {
                    outsocket.write(byteBuffer, 0, amount);
                    Log.d("debug", "***amount: "+amount);
                }
                Log.d("debug", "===after send pic");
                outsocket.write(byteBuffer, 0, 0);

                myoutputstream.flush();
                myoutputstream.close();

                dataSocket.close();
                dataSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SendCMDThread extends Thread{
        private byte byteBuffer[] = new byte[1024*1024];
        private OutputStream outsocket;
        private InputStream insocket;
        private ByteArrayOutputStream myoutputstream;

        public SendCMDThread(ByteArrayOutputStream myoutputstream){
            this.myoutputstream = myoutputstream;
            Log.d("debug", "socket: "+dataSocket);
            try {
                myoutputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Log.d("debug", "11111111111111111111");
            try{
                if (dataSocket == null){
                    dataSocket = new Socket(cameraServerIP,cameraServerPort);
                    Log.d("debug", "socket created");
                }
                else {
                    Log.d("debug", "socket: "+dataSocket);
                }
                outsocket = dataSocket.getOutputStream();
                insocket = dataSocket.getInputStream();

                Log.d("debug", "***totoal size: "+ myoutputstream.size());
                ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
                int bnum = inputstream.available();
                Log.d("debug", "***total num: " + bnum);
                int amount;
                while ((amount = inputstream.read(byteBuffer)) != -1) {
                    outsocket.write(byteBuffer, 0, amount);
                    Log.d("debug", "***amount: "+amount);
                }
                Log.d("debug", "===after send cmd");
                outsocket.write(byteBuffer, 0, 0);

                byte[] inbuf = new byte[16];

                int num = insocket.read(inbuf);
                Log.d("debug", "read data byte: "+num);

                String SCMD = new String(inbuf, "utf-8");
                SCMD = SCMD.substring(0, num);
                Log.d("debug", num + "SCMD: " + SCMD);

                if (SCMD.compareTo("RReady")==0){
                    readyToSendPic = true;
                }
                else{
                    readyToSendPic = false;
                }

                myoutputstream.flush();
                myoutputstream.close();

                dataSocket.close();
                dataSocket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Runnable network_task = new Runnable() {
        @Override
        public void run() {

            try {
                String str = "client";

                byte _data[] = str.getBytes();
                DatagramPacket dpc = new DatagramPacket(_data, _data.length, serverAddress, 21567);
                socket.send(dpc);
                //Log.v("verbose", "length: "+ sdata.length);
                //DatagramPacket dp = new DatagramPacket(sdata, sdata.length, serverAddress, 21567);
                //socket.send(dp);
            }catch (Exception e){
                Log.e("error", "error2"+e);
            }


        }
    };
    */

    public void stopRecording(){
        if (recordRunning == false)
            return;
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            recordRunning = false;
        }catch(Exception e) {
            Log.e("error", "exception: "+e);
        }
    }
/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            // create exit dialog
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            // set title
            isExit.setTitle("prompt");
            // set msg
            isExit.setMessage("Are you sure to exit?");
            // add button listener
            isExit.setButton("Ok", listener);
            isExit.setButton2("Cancel", listener);
            // show dialog
            isExit.show();

        }

        return false;
    }

    /**listener*/
    /*
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    break;
                default:
                    break;
            }
        }
    };
    */
}