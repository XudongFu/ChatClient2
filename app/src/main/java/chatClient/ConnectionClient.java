package chatClient;

import android.content.Context;
import android.net.ConnectivityManager;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;
import Data.ConstValue;
import Model.Message;

/**
 * Created by 付旭东 on 2017/1/4.
 */

public class ConnectionClient {


    private static ConnectionClient client=null;
    Context context;
    DatagramSocket Socketclient;

    private ConnectionClient(Context context)
    {
        this.context=context;
    }


    private boolean deviceIsOnline()
    {
        ConnectivityManager cwjManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cwjManager.getActiveNetworkInfo().isAvailable();
    }

    public static ConnectionClient getInstance(Context context)
    {
        if(client==null)
        {
            client=new ConnectionClient(context);
            return client;
        }
        else
            return client;
    }


    /**
     *
     * @param message 要发送的消息
     * @return   是否成功发送至服务器
     */
    public boolean sendMessage(Message message)
    {
        try
        {
            if(Socketclient==null)
            Socketclient = new DatagramSocket();
            byte[] data=message.toString().getBytes();
            InetAddress end= InetAddress.getByName( ConstValue.Server);
            DatagramPacket sendPacket = new DatagramPacket(data ,data.length ,end , ConstValue.port);
            Socketclient.send(sendPacket);

            return  true;
        }
        catch (SocketException e)
        { return false;

        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {

            return false;
        }
    }


    private boolean sendString(String str)
    {
        try
        {
            byte[] data=str.getBytes();
            InetAddress end= InetAddress.getByName( ConstValue.Server);
            DatagramPacket sendPacket = new DatagramPacket(data ,data.length ,end , ConstValue.port);
            Socketclient.send(sendPacket);
            return  true;
        }
        catch (SocketException e)
        {
            return false;
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public Message sendMessageWithConfirm(Message in)
    {
        if(sendMessage(in))
            return getNextMessage();
        return  null;
    }

    public void sendConfirm(boolean real,int th)
    {
        if(real)
        {
            String confirm="<message>" +
                    "<action>" +
                    "confirm" +
                    "</action>" +
                    "<th>"+th+"</th>" +
                    "<value><success></success></value>" +
                    "</message>";
            sendString(confirm);
        }
        else
        {
            String confirm="<message>" +
                    "<action>" +
                    "confirm" +
                    "</action>" +
                    "<th>"+th+"</th>" +
                    "<value><fail></fail></value>" +
                    "</message>";
            sendString(confirm);
        }
    }

    /**
     * 接受到的消息列表
     */
    public Vector<Message> queue=new Vector<>();

    ArrayList<Runnable> forShow=new ArrayList<>();


    public void registerRun(Runnable run)
    {
        forShow.add(run);
    }


    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    private Message getConfirmMessage(int th)
    {

        return  null;
    }



    public Message getNextMessage()
    {
        byte[] data=new byte[1024];
        DatagramPacket pack=new DatagramPacket(data,data.length);
        try {
            Socketclient.receive(pack);
            String str=new String(subBytes(pack.getData(),0,pack.getLength()));
            Message mess=new Message(str);
            if(mess.getAction().equals("communication"))
            {
                queue.add(mess);
                for(Runnable temp:forShow)
                {
                    temp.run();
                }
                return getNextMessage();
            }
            return  mess;

        } catch (IOException e) {
           throw  new RuntimeException("接受数据包异常");
        }
    }

    public void receive()
    {
        while (true)
        {
            queue.add(getNextMessage());
        }
    }

}
