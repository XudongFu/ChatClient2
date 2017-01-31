package chatClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.widget.Toast;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import Data.ConstValue;
import Model.Message;

/**
 * Created by 付旭东 on 2017/1/4.
 */

public class ConnectionClient {


    private static ConnectionClient client=null;
    Context context;
    private DatagramSocket Socketclient;
    public Thread rec;

    private ConnectionClient(Context context)
    {
        this.context=context;
        if(Socketclient==null)
        {
            try {
                Socketclient = new DatagramSocket();
            }
            catch (SocketException e)
            {
                throw new RuntimeException("与服务器的socket链接发生错误");
            }
        }
        rec=new Thread(new Runnable() {
            @Override
            public void run() {
                receive(null);
            }
        });
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
     * @param message 要发送的消息
     * @return   是否成功发送至服务器
     */


    public boolean sendMessage(Message message)
    {
        try
        {
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

    public void sendConfirm(boolean real,int th) {
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

    /**
     * 接受到的命令式消息列表
     */
    public LinkedBlockingQueue<Message> orders=new LinkedBlockingQueue<>();


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
    
    public synchronized void receiveMessage()
    {
        byte[] data=new byte[2048];
        DatagramPacket pack=new DatagramPacket(data,data.length);
        try {
            Socketclient.receive(pack);
            String str=new String(subBytes(pack.getData(),0,pack.getLength()));
            Message mess=new Message(str);
            if(mess.getAction().equals("communication"))
            {
                queue.add(mess);
                for(Runnable temp:forShow) {
                    temp.run();
                }
            }
            else
            {
                orders.offer(mess);
                notifyAll();
            }
        } catch (IOException e) {
            throw  new RuntimeException("接受数据包异常");
        }
    }


    public synchronized Message getNextMessage()
    {
       if(orders.size()==0)
       {
           try
           {
               wait();
               return orders.take();
           }
           catch (InterruptedException e)
           {
               throw new RuntimeException("获取下一条信息发生异常");
           }
       }
        else {
           try {
               return orders.take();
           } catch (InterruptedException e) {
               throw new RuntimeException("获取下一条信息发生异常");
           }
       }
    }

    public void receive(Handler handler)
    {
        while (true)
        {
            try
            {
                receiveMessage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"获取到一条交互信息",Toast.LENGTH_LONG).show();
                    }
                });
            }
            catch (final Exception e)
            {
                if(handler!=null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

}
