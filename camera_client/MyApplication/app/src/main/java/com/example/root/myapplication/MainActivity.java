package com.example.root.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 8/27/15.
 */
public class MainActivity extends AppCompatActivity {
    private String fileDir = "/sdcard/mobile_camera/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter arrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getFileInfo(fileDir));
        setContentView(R.layout.layout_main);
        ListView videoListView = (ListView) findViewById(R.id.listView);
        videoListView.setAdapter(arrayAdapter1);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private List<String> getFileInfo(String dirPath) {
        List<String> data = new ArrayList<String>();

        File f_dir = new File(dirPath);
        if (!f_dir.exists()) {
            return data;
        }

        File[] files = f_dir.listFiles();
        if (files != null) {
            int count = files.length;
            for (int i = 0; i < count; i++) {
                File file = files[i];
                data.add(file.getPath());
            }
        } else {
            Log.e("error", "can not get any file");
        }

        return data;
    }


    private Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    Runnable video_thumbnail_task = new Runnable() {
        @Override
        public void run() {

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
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

    /**
     * listener
     */
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
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

    public void Btn7OnClick(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
        //finish();
    }
}