package com.fuxudong.chatclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import Data.DbManager;
import Model.LocalUser;
import Model.Message;
import Model.User;
import chatClient.ConnectionClient;

public class MainActivity extends AppCompatActivity {

    ArrayList<View> lvs;
    ViewPager pager;
    TextView chat;
    TextView friend;
    TextView self;
    LocalUser localUser;
    ArrayList<User> friends;
    DbManager dbManager;
    Context context;
    ListView chatList;
    ListView friendList;
    Handler myhandle=new Handler();
    ConnectionClient client;

    TreeMap<Integer,String> commDic=new TreeMap<>();

    Runnable show=new Runnable() {
        @Override
        public void run() {
            int count =client.queue.size();
            if(count>=1)
            {
                Message mess=client.queue.elementAt(0);

                Node value=mess.getValue();
               int from=Integer.valueOf(getNodeContent(value,"from"));
                int to=Integer.valueOf(getNodeContent(value,"to"));
                String content=getNodeContent(value,"content");

                if(commDic.get(from)==null)
                {
                    commDic.put(from,content);
                }
                myhandle.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshChatList();
                    }
                });
            }
        }
    };


    void startService()
    {
        Intent ser=new Intent(MainActivity.this,ReceiveComm.class);
        startService(ser);

        bindService(ser, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {


            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        },BIND_AUTO_CREATE);

    }







    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent ser=new Intent(MainActivity.this,ReceiveComm.class);
        stopService(ser);
    }

    void refreshChatList()
    {
        SimpleAdapter chatAdapter = new SimpleAdapter(context, getChatData(), R.layout.friendline
                , new String[]{"name", "lastComm"}, new int[]{R.id.friendLineName, R.id.friendLineId}
        );
        chatList.setAdapter(chatAdapter);
        chatList.setBackgroundColor(Color.GRAY);
        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView p = (TextView) view.findViewById(R.id.friendLineId);
                Intent start = new Intent(MainActivity.this, Communication.class);
                start.putExtra("friendId", Integer.valueOf(p.getText().toString()));
                startActivity(start);
            }
        });
    }

    List<Map<String,String>> getChatData() {
        ArrayList<Map<String, String>> res = new ArrayList<>();
        Iterator<Integer> bianli = commDic.keySet().iterator();
        while (bianli.hasNext())
        {
            int next=bianli.next();
            TreeMap<String,String> temp=new TreeMap<>();
            temp.put("lastComm",commDic.get(next));
            temp.put("name",String.valueOf(next));
            res.add(temp);
        }
        return res;
    }


    private String getNodeContent(Node parent,String nodeName)
    {
        NodeList ls=parent.getChildNodes();
        for(int i=0;i<ls.getLength();i++)
        {
            if(ls.item(i).getNodeName().equals(nodeName))
                return  ls.item(i).getTextContent();
        }
        return  null;
    }


    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView p=(TextView) v;
            switch (p.getText().toString())
            {
                case"聊天":
                    pager.setCurrentItem(0);
                    break;
                case "朋友":
                    pager.setCurrentItem(1);
                    break;
                case "自己":
                    pager.setCurrentItem(2);
                    break;
            }
        }
    };


    Runnable loadData=new Runnable() {
        @Override
        public void run() {

            try
            {
                dbManager=DbManager.getDbManager(context);
                dbManager.checkData();
                localUser=dbManager.getLocalUser();
                friends=localUser.getFriendList();

                myhandle.post(new Runnable() {
                    @Override
                    public void run() {
                        SimpleAdapter chatAdapter = new SimpleAdapter(context, getData(), R.layout.friendline
                                , new String[]{"name", "id"}, new int[]{R.id.friendLineName, R.id.friendLineId}
                        );
                        friendList.setAdapter(chatAdapter);
                        friendList.setBackgroundColor(Color.GRAY);
                        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                TextView p = (TextView) view.findViewById(R.id.friendLineId);
                                Intent start = new Intent(MainActivity.this, Communication.class);
                                start.putExtra("friendId", Integer.valueOf(p.getText().toString()));
                                startActivity(start);
                            }
                        });
                    }
                });
            }
            catch (final Exception e)
            {
                myhandle.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpagelayout);
        pager = (ViewPager) findViewById(R.id.vp);
        lvs = new ArrayList<>();
        View view1 = LayoutInflater.from(this).inflate(R.layout.chatlayout, null);
        View view2 = LayoutInflater.from(this).inflate(R.layout.friendslayout, null);
        View view3 = LayoutInflater.from(this).inflate(R.layout.myself, null);
        lvs.add(view1);
        lvs.add(view2);
        lvs.add(view3);
        Thread load=new Thread(loadData);
        load.start();
        client=ConnectionClient.getInstance(context);
        client.registerRun(show);
        context = getApplicationContext();
        chat = (TextView) findViewById(R.id.chat);
        friend = (TextView) findViewById(R.id.friend);
        self = (TextView) findViewById(R.id.self);
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return lvs.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {

                container.addView(lvs.get(position), 0);
                return lvs.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(lvs.get(position));
            }
        });
        chat.setOnClickListener(listener);
        friend.setOnClickListener(listener);
        self.setOnClickListener(listener);
        chatList = (ListView) view1.findViewById(R.id.chatList);
        friendList = (ListView) view2.findViewById(R.id.friendList);


        /*friendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friends=localUser.getFriendList();
            }
        });*/
    }





    List<Map<String ,String>> getData()
    {
        List<Map<String,String>> result=new ArrayList<Map<String,String>>() ;
        for (int i=0;i<friends.size();i++)
        {
            TreeMap<String,String> temp=new TreeMap<>();
            temp.put("name",friends.get(i).name);
            temp.put("id",String.valueOf(friends.get(i).id) );
            result.add(temp);
        }
        return  result;
    }



}
