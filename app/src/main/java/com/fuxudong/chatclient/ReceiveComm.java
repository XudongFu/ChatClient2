package com.fuxudong.chatclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import chatClient.ConnectionClient;

/**
 * Created by 付旭东 on 2017/1/14.
 */

public class ReceiveComm extends Service
{
    Context context;
    ConnectionClient client;

    Binder mybinder=new Binder(){

        public void receiveMessage()
        {
            client.receive();
        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return mybinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        client=ConnectionClient.getInstance(context);
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
