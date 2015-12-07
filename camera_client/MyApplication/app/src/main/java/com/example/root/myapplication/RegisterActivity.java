package com.example.root.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by root on 8/6/15.
 */
public class RegisterActivity extends AppCompatActivity {
    private String m_username;
    private String m_password;
    private String m_email;
    private String m_phone;
    private String m_dev_id;
    private String m_dev_type;
    private String m_dev_os;
    private boolean register_status= false;
    private String error_msg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regiter);
    }


    private void get_username(){

    }

    private void get_password(){

    }

    private void get_email(){
        m_email = "test@websense.com";
    }

    private void get_phone(){
        m_phone = "1233445";
    }

    private void get_dev_id(){
        m_dev_id = "123abc";
    }

    private void get_dev_type(){
        m_dev_type = "Android Phone";
    }

    private void get_dev_os(){
        m_dev_os = "Android 5.0.1";
    }

    public void Btn3OnClick(View view){
        TextView textView7 =(TextView)findViewById(R.id.textView7);
        EditText editText3 =(EditText)findViewById(R.id.editText3);

        m_username=editText3.getText().toString();
        if(m_username.equals("")){
            textView7.setText("username should not be empty");
            return;
        }

        String str_password1="";
        EditText editText4 =(EditText)findViewById(R.id.editText4);
        str_password1=editText4.getText().toString();

        String str_password2="";
        EditText editText5 =(EditText)findViewById(R.id.editText5);
        str_password2=editText5.getText().toString();

        if (!str_password1.equals(str_password2)) {
            textView7.setText("two password not the same");
            return;
        }

        m_password = str_password1;
        if (m_password.equals("")){
            textView7.setText("password should not be empty");
            return;
        }

        get_email();
        get_phone();
        get_dev_id();
        get_dev_type();
        get_dev_os();

        try {
            Thread t = new Thread(register_task);
            t.start();
            t.join();
        }
        catch (Exception e)
        {
            Log.e("error", "error");
            textView7.setText("Register exception: "+e);
        }

        if (register_status){
            textView7.setText("Register success, you need to");
            gotoLoginView();
        }
        else{
            textView7.setText("Register failed: "+error_msg);
        }
    }

    Runnable register_task = new Runnable() {
        @Override
        public void run() {

            String url = "http://10.228.112.200/cgi-py/remote_monitor_server.py";
            String data = String.format("{\"Command\":\"Register\", \"Name\":\"%s\", \"Passwd\":\"%s\", " +
                    "\"Email\":\"%s\", \"Phone\":\"%s\", \"DevID\":\"%s\", \"DevType\":\"%s\", \"DevOS\":\"%s\"}",
                    m_username, m_password, m_email, m_phone, m_dev_id, m_dev_type, m_dev_os);

            HttpPost http_post = new HttpPost(url);
            HttpClient http_client = new DefaultHttpClient();
            try{
                StringEntity se = new StringEntity(data);
                http_post.setEntity(se);
                HttpResponse http_response = http_client.execute(http_post);
                int ret = getResponseResult(http_response);
                if (ret == 200){
                    register_status = true;
                    error_msg = "register success";
                }
                else{
                    register_status = false;
                    error_msg = "register failed, response code: "+ret;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e("error", "exception: "+e);
                register_status = false;
                error_msg = "register failed, exception: "+e;
            }
        }
    };

    private int getResponseResult(HttpResponse response)
    {
        if (null == response)
        {
            Log.e("error", "http response is null");
            return 0;
        }

        int status_code = response.getStatusLine().getStatusCode();
        Log.d("debug", "status code: "+status_code);

        return status_code;
    }

    private void gotoLoginView(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void Btn4OnClick(View view){
        gotoLoginView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
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
