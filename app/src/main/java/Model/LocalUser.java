package Model;


import android.content.Context;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import Data.DbManager;
import chatClient.ConnectionClient;

/**
 * Created by 付旭东 on 2017/1/4.
 */

public class LocalUser extends User
{
    ConnectionClient client;
    DbManager dbManager;
    Context myContext;
    /**
     * 最大的尝试次数
     */
    int MaxTimes=5;

    /**
     * 登陆到服务器
     * @return
     */
    public boolean signOn()
    {
        String SignOnStr="<message><action>signOn</action><th>"+getTh()+"</th> "
                +"<value><clientType>Android</clientType><id>"+id+"</id><password>"
                +passWord+"</password></value></message>";
        Message mess=new Message(SignOnStr);
        Message res=client.sendMessageWithConfirm(mess);
        if(res.value.getFirstChild().getNodeName().equals("success"))
        {
            return true;
        }
        else
        {
            return false;
        }

    }


    public LocalUser(Context context, int id, String passCode) {

            client=ConnectionClient.getInstance(myContext);
            dbManager=DbManager.getDbManager(context);
            myContext=context;
            dbManager.setCurrentLocalUser(id,passCode);
            this.id=id;
            this.passWord=passCode;

    }

    public ArrayList<User> getFriendList()
    {
        return dbManager.getUserList();

    }


    public void addToLocalUser()
    {
        dbManager.AddLocalUser(id,passWord);
    }

    public LocalUser(Context context)
    {
        client=ConnectionClient.getInstance(myContext);
        myContext=context;
    }


    public boolean sendMessageToUser(int toId,String content)
    {

        //记录到本地数据库里面

        String send="<message><action>communication</action><th>"+getTh()
                + "</th><value><from>"+id
                + "</from><type>user</type><to>"+toId
                + "</to><content>"+content
                + "</content></value></message>";
        Message mess=new Message(send);
        return client.sendMessage(mess);
    }

    public void sendMessageToGroup(int toId,String content)
    {
        //记录到本地数据库里面

        String send="<message><action>communication</action><th>"+getTh()
                + "</th><value><from>"+id
                + "</from><type>group</type><to>"+toId
                + "</to><content>"+content
                + "</content></value></message>";
        Message mess=new Message(send);
        int times=MaxTimes;
        while(!client.sendMessage(mess) && times-->0);
        throw new RuntimeException("无法发送消息到服务器");
    }

    /**
     * 退出
     */
    public void SignOff()
    {
        String SignOffStr="<message><action>signOff</action><th>"+getTh()+"</th> "
                +"<value><clientType>Android</clientType><id>"+id+"</id><password>"
                +passWord+"</password></value></message>";
        Message mess=new Message(SignOffStr);
        client.sendMessage(mess);
    }

    /**
     * 注册一个账号
     * @return 返回账号的id
     */
    public  int Register(String name,String sex,String birthDay,String colloge,String company,String passcode)
    {
        if(passcode.length()<6)
        throw  new RuntimeException("密码长度小于6位");
        int xuhao= getTh();
        String zhuce="<message><action>signIn</action><th>"+xuhao
                + "</th><value><name>"+name
                + "</name><sex>"+sex
                + "</sex><birthday>"+birthDay
                + "</birthday><colloge>"+colloge
                + "</colloge><company>"+company
                + "</company><password>"+passcode
                + "</password></value></message>";
        Message message=new Message(zhuce);
        Message confirm= client.sendMessageWithConfirm(message);
        Node value=confirm.getValue();
        int id=Integer.valueOf(value.getFirstChild().getTextContent()) ;
        client.sendConfirm(true,xuhao);
        return id;
    }

    int getTh()
    {
        Random suiji=new Random();
        Calendar calendar=Calendar.getInstance();
        suiji.setSeed(calendar.getFirstDayOfWeek()*calendar.getTimeInMillis());
       return suiji.nextInt(99999999);
    }

    public   LocalUser getLocalUser()
    {
       return dbManager.getLocalUser();
    }


}