package com.example.root.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class LoginActivity extends AppCompatActivity {

    private boolean loginStatus = false;
    private String error_msg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void Btn1OnClick(View view){
        try {
            Thread t = new Thread(network_task);
            t.start();
            t.join();
        }
        catch (Exception e)
        {
            Log.e("error","thread error: "+ e);
            return;
        }
        if (loginStatus) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            TextView testView_log_info = (TextView)findViewById(R.id.textView10);
            testView_log_info.setText(error_msg);
        }
    }

    Runnable network_task = new Runnable() {
        @Override
        public void run() {

            String str_username="";
            EditText editText1 =(EditText)findViewById(R.id.editText);
            str_username=editText1.getText().toString();

            String str_password="";
            EditText editText2 =(EditText)findViewById(R.id.editText2);
            str_password=editText2.getText().toString();

            if (str_username.equals("") || str_password.equals("")){
                return;
            }

            String url = "http://10.228.112.200/cgi-py/remote_monitor_server.py";
            String data = String.format("{\"Command\":\"Login\", \"Name\":\"%s\", \"Passwd\":\"%s\"}", str_username, str_password);

            //final String APPLICATION_JSON = "application/json";
            //final String CONTENT_TEXT = "text/json";

            HttpPost http_post = new HttpPost(url);
            //http_post.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
            HttpClient http_client = new DefaultHttpClient();

            try{
                //StringEntity se = new StringEntity(data);
                //http_post.setEntity(se);
                //HttpResponse http_response = http_client.execute(http_post);
                int ret = 200;//getResponseResult(http_response);
                if (ret == 200){
                    loginStatus = true;
                    error_msg = "Login success";
                }
                else{
                    loginStatus = false;
                    error_msg = "Login failed, response code: "+ ret;
                }

            } catch (Exception e) {
                e.printStackTrace();
                loginStatus = false;
                error_msg = "Login failed, network error: "+e;
            }


        }
    };

    private int getResponseResult(HttpResponse response)
    {
        if (null == response)
        {
            Log.d("debug", "http response is null");
            return 0;
        }

        int status_code = response.getStatusLine().getStatusCode();
        Log.d("debug", "status code: "+status_code);

        return status_code;
    }

    public void Btn2OnClick(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setTitle("prompt");
            isExit.setMessage("Are you sure to exit?");
            isExit.setButton("Ok", listener);
            isExit.setButton2("Cancel", listener);
            isExit.show();
        }

        return false;
    }
    /**listener*/
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
}
