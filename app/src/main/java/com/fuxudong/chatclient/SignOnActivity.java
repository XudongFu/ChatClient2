package com.fuxudong.chatclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import Data.DbManager;
import Model.LocalUser;

/**
 * Created by 付旭东 on 2017/1/4.
 */

public class SignOnActivity extends AppCompatActivity {

    EditText idNumber;
    EditText pass;
    Button signOn;
    Context context;
    Button registerUser;
    Button test;
    Handler solve;
    ReceiveComm.MyBinder myBinder;
    Intent ser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signon);
        context=getApplicationContext();
        idNumber= (EditText) findViewById(R.id.id);
        pass= (EditText) findViewById(R.id.passWord);
        signOn= (Button) findViewById(R.id.makeSure);
        test=(Button)findViewById(R.id.test);
        registerUser= (Button) findViewById(R.id.registerUser);
        signOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread sign=new Thread(p);
                sign.start();
            }
        });
        ser = new Intent(SignOnActivity.this, ReceiveComm.class);
        solve=new Handler();
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Thread re=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DbManager.getDbManager(context);
                        Intent p = new Intent(SignOnActivity.this, RegisterActivity.class);
                        startActivity(p);
                    }
                });
                re.start();
            }
        });
        startService();
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    void showMessage(final String mess) {
        solve.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,mess,Toast.LENGTH_LONG).show();
            }
        });

    }

    ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            myBinder = (ReceiveComm.MyBinder) service;
            myBinder.receiveMessage(solve);
            showMessage("已经链接到服务");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            showMessage("服务断开");
        }
    };


    void startService()
    {
        startService(ser);
        bindService(ser,conn , BIND_AUTO_CREATE);
        showMessage("后台服务启动");
    }


    Runnable p=new Runnable() {
        @Override
        public void run() {
            try
            {
                int id = Integer.valueOf(idNumber.getText().toString());
                LocalUser user = new LocalUser(context, id, pass.getText().toString());
                if (user.signOn())
                {
                    user.addToLocalUser();
                    Intent p = new Intent(SignOnActivity.this, MainActivity.class);
                    DbManager.getDbManager(context).setCurrentLocalUser(id,pass.getText().toString());
                    startActivity(p);
                }
                else
                {
                    solve.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,"账号或者密码错误",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            catch (final Exception e)
            {
                solve.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };


}
