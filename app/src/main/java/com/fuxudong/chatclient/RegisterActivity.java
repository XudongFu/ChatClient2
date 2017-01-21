package com.fuxudong.chatclient;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import Model.LocalUser;

public class RegisterActivity extends AppCompatActivity
{

    Button register;
    Context context;
    EditText name;
    EditText company;
    EditText sex;
    EditText place;
    EditText colloge;
    EditText pass;
    EditText passAgain;
    EditText idText;
    Handler   solve=new Handler();
    Looper loop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registerlayout);
        name= (EditText) findViewById(R.id.name);
        company=(EditText)findViewById(R.id.company);
        sex=(EditText)findViewById(R.id.sex);
        place=(EditText)findViewById(R.id.place);
        colloge=(EditText)findViewById(R.id.colloge);
        pass=(EditText)findViewById(R.id.pass);
        passAgain=(EditText)findViewById(R.id.passWord);
        idText=(EditText)findViewById(R.id.id);
        register= (Button) findViewById(R.id.register);
        context=getApplicationContext();
        register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    p.start();
                }
            });
    }
    Thread p = new Thread(new Runnable() {
        @Override
        public void run() {
            try
            {
                String mima=pass.getText().toString();
                String mima2=passAgain.getText().toString();

                if(mima!=null && mima.equals(mima2))
                {
                    String nameTemp=name.getText().toString();
                    String com=company.getText().toString();
                    String sx=sex.getText().toString();
                    String plac=place.getText().toString();
                    String coll=colloge.getText().toString();
                    LocalUser user = new LocalUser(context);
                    final int id = user.Register(nameTemp,sx,"",coll,com,mima);
                    solve.post(new Runnable() {
                        @Override
                        public void run() {
                            idText.setText(String.valueOf(id));
                            Toast.makeText(context, "注册账号成功", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else
                {
                    solve.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "请确认密码不为空，且两者相同", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            catch (final Exception e)
            {
                solve.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    });
}
