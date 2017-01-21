package com.fuxudong.chatclient;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
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
    Button button;
    Context context;
    Button registerUser;
    Button test;
    Handler solve=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signon);
        context=getApplicationContext();
        idNumber= (EditText) findViewById(R.id.id);
        pass= (EditText) findViewById(R.id.passWord);
        button= (Button) findViewById(R.id.makeSure);
        test=(Button)findViewById(R.id.test);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread sign=new Thread(p);
                sign.start();
            }
        });

        registerUser= (Button) findViewById(R.id.registerUser);

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

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread re=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            Intent p = new Intent(SignOnActivity.this, MainActivity.class);
                            startActivity(p);
                        }
                        catch (final Exception e)
                        {
                            solve.post(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.printf(e.getMessage());
                                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                });
                re.start();
            }
        });

    }

    void shenqing()
    {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "我想申请权限", Toast.LENGTH_LONG).show();
            }
    }







    Runnable p=new Runnable() {
        @Override
        public void run() {

            try
            {
                int id = Integer.valueOf(idNumber.getText().toString());
                LocalUser user = new LocalUser(context, id, pass.getText().toString());
                if (user.signOn()) {
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
                        Log.d("error", e.getMessage());
                        e.printStackTrace();
                        System.out.printf(e.getMessage());
                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };



}
