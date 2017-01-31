package com.fuxudong.chatclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import chatClient.ConnectionClient;

/**
 * Created by 付旭东 on 2017/1/14.
 */

public class ReceiveComm extends Service
{
    Context context;
    ConnectionClient client;

    MyBinder binder=new MyBinder();

    class MyBinder extends Binder
    {
        public void receiveMessage(final Handler handle)
        {
            context=getApplicationContext();
            client=ConnectionClient.getInstance(context);
            client.rec.start();
            handle.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"接受消息的线程启动",Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
