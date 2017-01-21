package com.fuxudong.chatclient;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import Data.DbManager;
import Model.LocalUser;
import Model.Message;
import Model.User;
import chatClient.ConnectionClient;

public class Communication extends AppCompatActivity {

    EditText input;
    Button send;
    TextView friendInfo;
    LinearLayout messageList;
    int friendId;
    LocalUser localUser;
    Context context;
    DbManager manager;
    String friendName="friendName";
    Handler myHandle=new Handler();
    ConnectionClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        context=getApplicationContext();
        input= (EditText) findViewById(R.id.chatInput);
        send= (Button) findViewById(R.id.send);
        friendInfo= (TextView) findViewById(R.id.friendInfo);
        messageList= (LinearLayout) findViewById(R.id.ScrolContent);
        friendId=getIntent().getIntExtra("friendId",0);
        manager=DbManager.getDbManager(context);
        localUser= manager.getLocalUser();
        client=ConnectionClient.getInstance(context);

        if(friendId==0)
        {
            Toast.makeText(context,"获取朋友的id失败",Toast.LENGTH_LONG).show();
        }
        else
        {
           User friend=manager.getUser(friendId);
            friendName=friend.name;
            friendInfo.setText(friend.name);
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                Thread temp=new Thread(new Runnable() {
                    @Override
                    public void run() {

                            final String message = input.getText().toString();
                                if ( !message.equals("")) {
                                    if( localUser.sendMessageToUser(friendId,message))
                                     {
                                        myHandle.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                friendhowMessage(message, false);
                                                input.setText("");
                                            }
                                        });
                                    } else {
                                        myHandle.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "无法发送消息到服务器", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                        }
                });
                temp.start();

                } catch (final Exception e) {
                    myHandle.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

    }


    Runnable thoughThread=new Runnable() {
        @Override
        public void run() {
            int count=client.queue.size();
            for(int i=0;i<count;i++)
            {
                Message mess=client.queue.elementAt(0);
                client.queue.remove(0);
            }
        }
    };



    void friendhowMessage(String message,boolean left)
    {
        if(left)
        {
            RelativeLayout  containerLeft= (RelativeLayout) getLayoutInflater().inflate(R.layout.message,null);
            TextView friend= (TextView) containerLeft.findViewById(R.id.friendName);
            TextView content= (TextView) containerLeft.findViewById(R.id.content);
            friend.setText(friendName);
            content.setText(message);
            messageList.addView(containerLeft);
        }
        else
        {
            RelativeLayout  containerRight= (RelativeLayout) getLayoutInflater().inflate(R.layout.localmessage,null);
            TextView friend= (TextView) containerRight.findViewById(R.id.friendName);
            TextView content= (TextView) containerRight.findViewById(R.id.content);
            friend.setText(friendName);
            content.setText(message);
            messageList.addView(containerRight);
        }

    }








}
